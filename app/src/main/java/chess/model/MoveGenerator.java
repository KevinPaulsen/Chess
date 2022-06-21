package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static chess.ChessCoordinate.*;
import static chess.model.GameModel.BLACK;
import static chess.model.GameModel.WHITE;
import static chess.model.pieces.Direction.LEFT;
import static chess.model.pieces.Direction.RIGHT;
import static chess.model.pieces.Directions.ALL_DIRECTIONS;
import static chess.model.pieces.Piece.*;

/**
 * This class is responsible for generating moves.
 */
public class MoveGenerator {

    private static final int BITS_IN_BYTE = 8;

    private static final byte[] LEADING_ZEROS = {
            8, 7, 6, 6, 5, 5, 5, 5,
            4, 4, 4, 4, 4, 4, 4, 4,
            3, 3, 3, 3, 3, 3, 3, 3,
            3, 3, 3, 3, 3, 3, 3, 3,
            2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    /**
     * Accessed by
     * coordinateToMask[posIdx][direction.ordinal]
     */
    private static final long[][] coordinateToMask = createMaskMap();

    private static final long[] knightMoveMask = createKnightMoveMask();

    private final GameModel game;
    private final BoardModel board;
    private long checkRayMap;
    private long hvPinRayMap;
    private long d12PinRayMap;
    private final FastMap opponentAttackMap;
    private final Map<Long, List<Move>> cachedPositions;
    private List<Move> moves;
    private boolean inCheck;
    private boolean inDoubleCheck;
    private boolean pinsExistInPosition;
    private Piece friendlyPawn;
    private Piece friendlyKnight;
    private Piece friendlyBishop;
    private Piece friendlyRook;
    private Piece friendlyQueen;
    private Piece attackingPawn;
    private Piece attackingKnight;
    private Piece attackingBishop;
    private Piece attackingRook;
    private Piece attackingQueen;
    private ChessCoordinate friendlyKingCoord;
    private ChessCoordinate attackingKingSquare;

    private char friendlyColor;
    private char attackingColor;

    public MoveGenerator(GameModel game) {
        this.game = game;
        this.board = game.getBoard();
        this.moves = new ArrayList<>(150);
        this.inCheck = false;
        this.inDoubleCheck = false;
        this.pinsExistInPosition = false;

        this.cachedPositions = new MoveLRUCache(11_000);

        this.checkRayMap = 0;
        this.hvPinRayMap = 0;
        this.d12PinRayMap = 0;
        this.opponentAttackMap = new FastMap();
    }

    /**
     * Updates the given sliding piece's attack map. Adds all the squares this piece
     * can move to to the given map. This will go though the firendly king, and
     * continue the ray past it.
     *
     * @param board          the board the piece is on.
     * @param attackingPiece the attacking piece to update.
     * @param coordinate     the coordinate this piece is on
     * @param map            the map to mark.
     */
    private static void updateSlidingAttackPiece(BoardModel board, Piece attackingPiece,
                                                 ChessCoordinate coordinate, FastMap map) {
        List<List<ChessCoordinate>> reachableCoordinateMap = attackingPiece.getReachableCoordinateMapFrom(coordinate);
        for (int rayIdx = 0; rayIdx < reachableCoordinateMap.size(); rayIdx++) {
            List<ChessCoordinate> potentialRay = reachableCoordinateMap.get(rayIdx);
            for (int coordIdx = 0; coordIdx < potentialRay.size(); coordIdx++) {
                ChessCoordinate potentialCoordinate = potentialRay.get(coordIdx);
                map.mergeMask(potentialCoordinate.getBitMask());

                if (board.isOccupied(potentialCoordinate)
                        && (board.isOccupiedByColor(potentialCoordinate, attackingPiece.getColor())
                        || !board.isKing(potentialCoordinate))) {
                    break;
                }
            }
        }
    }

    private static boolean areAligned(ChessCoordinate coordinate1, ChessCoordinate coordinate2,
                                      ChessCoordinate coordinate3) {
        float product1 = (coordinate1.getRank() - coordinate2.getRank()) * (coordinate1.getFile() - coordinate3.getFile());
        float product2 = (coordinate1.getFile() - coordinate2.getFile()) * (coordinate1.getRank() - coordinate3.getRank());
        return product1 == product2;
    }

    private static long[][] createMaskMap() {
        long[][] coordinateToMask = new long[Long.BYTES * BITS_IN_BYTE][ALL_DIRECTIONS.directions.size()];

        for (int coordIdx = 0; coordIdx < coordinateToMask.length; coordIdx++) {
            for (Direction direction : ALL_DIRECTIONS.directions) {
                long mask = 0x0;
                ChessCoordinate coordinate = direction.next(ChessCoordinate.getChessCoordinate(coordIdx));

                while (coordinate != null) {
                    mask |= coordinate.getBitMask();
                    coordinate = direction.next(coordinate);
                }

                if (Long.bitCount(mask) > 7) throw new IllegalStateException();
                coordinateToMask[coordIdx][direction.ordinal()] = mask;
            }
        }

        return coordinateToMask;
    }

    private static long[] createKnightMoveMask() {
        long[] knightMoveMask = new long[64];

        for (int coordIdx = 0; coordIdx < knightMoveMask.length; coordIdx++) {
            ChessCoordinate coordinate = getChessCoordinate(coordIdx);
            long mask = 0x0;

            if (coordinate.getFile() > 0 && coordinate.getRank() > 1) mask |= (coordinate.getBitMask() >>> 17);
            if (coordinate.getFile() < 7 && coordinate.getRank() > 1) mask |= (coordinate.getBitMask() >>> 15);
            if (coordinate.getFile() > 1 && coordinate.getRank() > 0) mask |= (coordinate.getBitMask() >>> 10);
            if (coordinate.getFile() < 6 && coordinate.getRank() > 0) mask |= (coordinate.getBitMask() >>> 6);
            if (coordinate.getFile() > 1 && coordinate.getRank() < 7) mask |= (coordinate.getBitMask() << 6);
            if (coordinate.getFile() < 6 && coordinate.getRank() < 7) mask |= (coordinate.getBitMask() << 10);
            if (coordinate.getFile() > 0 && coordinate.getRank() < 6) mask |= (coordinate.getBitMask() << 15);
            if (coordinate.getFile() < 7 && coordinate.getRank() < 6) mask |= (coordinate.getBitMask() << 17);

            knightMoveMask[coordIdx] = mask;
        }

        return knightMoveMask;
    }

    private static byte getRay(long relevantBits, ChessCoordinate coordinate, Direction direction) {
        final int rowMask = 0xFF;
        byte ray = 0x0;

        switch (direction) {
            case LEFT, RIGHT -> ray = (byte) (relevantBits >>> (8 * coordinate.getRank()));
            case UP_RIGHT, UP_LEFT, DOWN_RIGHT, DOWN_LEFT -> {
                ray |= relevantBits & rowMask;
                ray |= (relevantBits >>> 8) & rowMask;
                ray |= (relevantBits >>> 16) & rowMask;
                ray |= (relevantBits >>> 24) & rowMask;
                ray |= (relevantBits >>> 32) & rowMask;
                ray |= (relevantBits >>> 40) & rowMask;
                ray |= (relevantBits >>> 48) & rowMask;
                ray |= (relevantBits >>> 56) & rowMask;
            }
            case UP, DOWN -> {
                int file = coordinate.getFile();
                ray |= (relevantBits & rowMask) >>> file;
                ray |= ((relevantBits >>> 8) & rowMask) >>> file << 1;
                ray |= ((relevantBits >>> 16) & rowMask) >>> file << 2;
                ray |= ((relevantBits >>> 24) & rowMask) >>> file << 3;
                ray |= ((relevantBits >>> 32) & rowMask) >>> file << 4;
                ray |= ((relevantBits >>> 40) & rowMask) >>> file << 5;
                ray |= ((relevantBits >>> 48) & rowMask) >>> file << 6;
                ray |= ((relevantBits >>> 56) & rowMask) >>> file << 7;
            }
        }

        return ray;
    }

    private static long getMap(byte ray, ChessCoordinate coordinate, Direction direction) {
        long relevantBits = 0x0L;

        switch (direction) {
            case LEFT, RIGHT -> relevantBits = (((long) ray & 0xFF) << (8 * coordinate.getRank()));
            case UP_LEFT -> {
                for (int file = coordinate.getFile() - 1, rank = coordinate.getRank() + 1;
                     file >= 0 && rank < 8; file--, rank++) {
                    relevantBits |= (ray & (0b1L << file)) << (8 * rank);
                }
            }
            case UP_RIGHT -> {
                for (int file = coordinate.getFile() + 1, rank = coordinate.getRank() + 1;
                     file < 8 && rank < 8; file++, rank++) {
                    relevantBits |= (ray & (0b1L << file)) << (8 * rank);
                }
            }
            case DOWN_RIGHT -> {
                for (int file = coordinate.getFile() + 1, rank = coordinate.getRank() - 1;
                     file < 8 && rank >= 0; file++, rank--) {
                    relevantBits |= (ray & (0b1L << file)) << (8 * rank);
                }
            }
            case DOWN_LEFT -> {
                for (int file = coordinate.getFile() - 1, rank = coordinate.getRank() - 1;
                     file >= 0 && rank >= 0; file--, rank--) {
                    relevantBits |= (ray & (0b1L << file)) << (8 * rank);
                }
            }
            case UP -> {
                int file = coordinate.getFile();
                for (int rank = coordinate.getRank() + 1; rank < 8; rank++) {
                    long isolatedBit = ray & (0b1L << rank);
                    long correctedFile = rank > file ? isolatedBit >> (rank - file) : (isolatedBit << (file - rank));
                    relevantBits |= correctedFile << (rank * 8);
                }
            }
            case DOWN -> {
                int file = coordinate.getFile();
                for (int rank = coordinate.getRank() - 1; rank >= 0; rank--) {
                    long isolatedBit = ray & (0b1L << rank);
                    long correctedFile = rank > file ? isolatedBit >> (rank - file) : (isolatedBit << (file - rank));
                    relevantBits |= correctedFile << (rank * 8);
                }
            }
        }
        return relevantBits;
    }

    private void resetState() {
        this.moves = new ArrayList<>(150);
        this.inCheck = false;
        this.inDoubleCheck = false;
        this.pinsExistInPosition = false;

        this.checkRayMap = 0;
        this.hvPinRayMap = 0;
        this.d12PinRayMap = 0;
        this.opponentAttackMap.clear();

        this.friendlyColor = game.getTurn();
        this.attackingColor = friendlyColor == WHITE ? BLACK : WHITE;

        if (friendlyColor == WHITE) {
            friendlyPawn = WHITE_PAWN;
            friendlyKnight = WHITE_KNIGHT;
            friendlyBishop = WHITE_BISHOP;
            friendlyRook = WHITE_ROOK;
            friendlyQueen = WHITE_QUEEN;

            attackingPawn = BLACK_PAWN;
            attackingKnight = BLACK_KNIGHT;
            attackingBishop = BLACK_BISHOP;
            attackingRook = BLACK_ROOK;
            attackingQueen = BLACK_QUEEN;

            friendlyKingCoord = board.getWhiteKingCoord();
            attackingKingSquare = board.getBlackKingCoord();
        } else {
            attackingPawn = WHITE_PAWN;
            attackingKnight = WHITE_KNIGHT;
            attackingBishop = WHITE_BISHOP;
            attackingRook = WHITE_ROOK;
            attackingQueen = WHITE_QUEEN;

            friendlyPawn = BLACK_PAWN;
            friendlyKnight = BLACK_KNIGHT;
            friendlyBishop = BLACK_BISHOP;
            friendlyRook = BLACK_ROOK;
            friendlyQueen = BLACK_QUEEN;

            friendlyKingCoord = board.getBlackKingCoord();
            attackingKingSquare = board.getWhiteKingCoord();
        }
    }

    public List<Move> generateMoves() {
        if (cachedPositions.containsKey(game.getZobristHash())) {
            moves = new ArrayList<>(cachedPositions.get(game.getZobristHash()));
        } else {
            resetState();
            calculateAttackData();

            generateKingMoves();

            if (!inDoubleCheck) {
                generateRookAndBishopMoves(board.getRooks(friendlyRook) & ~d12PinRayMap, hvPinRayMap, friendlyRook);
                generateRookAndBishopMoves(board.getBishops(friendlyBishop) & ~hvPinRayMap, d12PinRayMap, friendlyBishop);
                generateQueenMoves();
                generateKnightMoves();
                generatePawnMoves();
            }

            cachedPositions.put(game.getZobristHash(), moves);
        }
        return moves;
    }

    private void calculateAttackData() {
        opponentAttackMap.merge(calculateSlidingAttackMap());

        // Calculate the pin and check rays
        List<ChessCoordinate> attackingQueens = attackingQueens();
        if (attackingRooks().size() > 0 || attackingQueens.size() > 0) {
            calculatePinsAndCheckRays(board, WHITE_ROOK.getReachableCoordinateMapFrom(friendlyKingCoord), false);
        }
        if (attackingBishops().size() > 0 || attackingQueens.size() > 0) {
            calculatePinsAndCheckRays(board, WHITE_BISHOP.getReachableCoordinateMapFrom(friendlyKingCoord), true);
        }

        // Calculate Knight Attacks
        List<ChessCoordinate> attackingKnights = attackingKnights();
        for (int idx = 0; idx < attackingKnights.size(); idx++) {
            ChessCoordinate knightCoord = attackingKnights.get(idx);
            List<List<ChessCoordinate>> reachableCoordinates = WHITE_KNIGHT.getReachableCoordinateMapFrom(knightCoord);
            for (int rayIdx = 0; rayIdx < reachableCoordinates.size(); rayIdx++) {
                updateAttackingSquares(friendlyKingCoord, knightCoord, reachableCoordinates, rayIdx);
            }
        }

        // Calculate Pawn Attacks
        Piece pawn = friendlyColor == WHITE ? BLACK_PAWN : WHITE_PAWN;
        List<ChessCoordinate> attackingPawns = attackingPawns();
        for (int idx = 0; idx < attackingPawns.size(); idx++) {
            ChessCoordinate pawnCoord = attackingPawns.get(idx);
            List<List<ChessCoordinate>> reachableCoordinates = pawn.getReachableCoordinateMapFrom(pawnCoord);
            for (int rayIdx = 1; rayIdx <= 2; rayIdx++) {
                updateAttackingSquares(friendlyKingCoord, pawnCoord, reachableCoordinates, rayIdx);
            }
        }

        Piece attackingKing = friendlyColor == WHITE ? BLACK_KING : WHITE_KING;

        // Calculate King Attacks
        List<List<ChessCoordinate>> reachableCoordinates = attackingKing.getReachableCoordinateMapFrom(attackingKingSquare);
        for (int rayIdx = 0; rayIdx < 8; rayIdx++) {
            updateAttackingSquares(friendlyKingCoord, attackingKingSquare, reachableCoordinates, rayIdx);
        }
        if (checkRayMap == 0) checkRayMap = ~checkRayMap;
    }

    private void updateAttackingSquares(ChessCoordinate kingCoord, ChessCoordinate pieceCoord,
                                        List<List<ChessCoordinate>> reachableCoordinates, int rayIdx) {
        List<ChessCoordinate> ray = reachableCoordinates.get(rayIdx);
        for (int coordIdx = 0; coordIdx < ray.size(); coordIdx++) {
            ChessCoordinate targetCoord = ray.get(coordIdx);
            opponentAttackMap.mergeMask(targetCoord.getBitMask());
            if (targetCoord.equals(kingCoord)) {
                inDoubleCheck = inCheck;
                inCheck = true;
                checkRayMap |= pieceCoord.getBitMask();
            }
        }
    }

    private void calculatePinsAndCheckRays(BoardModel board, List<List<ChessCoordinate>> raysToCheck,
                                           boolean isDiagonal) {
        for (int rayIdx = 0; rayIdx < raysToCheck.size(); rayIdx++) {
            List<ChessCoordinate> ray = raysToCheck.get(rayIdx);
            if (inDoubleCheck) {
                break;
            }

            boolean friendlyRay = false;
            long rayMap = 0x0;

            for (int coordIdx = 0; coordIdx < ray.size(); coordIdx++) {
                ChessCoordinate coordinate = ray.get(coordIdx);
                rayMap |= coordinate.getBitMask();

                if (board.isOccupied(coordinate)) { // There is a piece on this square
                    if (board.isOccupiedByColor(coordinate, friendlyColor)) { // This piece is friendly
                        if (!friendlyRay) { // This is the first friendly piece we found
                            friendlyRay = true;
                        } else { // this is the second friendly piece we found.
                            break;
                        }
                    } else { // Piece is an enemy piece.
                        if (board.isQueen(coordinate)
                                || (isDiagonal && board.isBishop(coordinate))
                                || (!isDiagonal && board.isRook(coordinate))) {
                            if (friendlyRay) {
                                pinsExistInPosition = true;
                                if (isDiagonal) {
                                    d12PinRayMap |= rayMap;
                                } else {
                                    hvPinRayMap |= rayMap;
                                }
                            } else {
                                checkRayMap |= rayMap;
                                inDoubleCheck = inCheck;
                                inCheck = true;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Calculate and return the attack map of all the opponents sliding moves.
     *
     * @return the map of all the squares attacked by the sliding pieces.
     */
    private FastMap calculateSlidingAttackMap() {
        FastMap slidingAttackMap = new FastMap();

        Piece piece = friendlyColor == WHITE ? BLACK_QUEEN : WHITE_QUEEN;
        List<ChessCoordinate> attackingQueens = attackingQueens();
        for (int idx = 0; idx < attackingQueens.size(); idx++) {
            ChessCoordinate queenCoord = attackingQueens.get(idx);
            updateSlidingAttackPiece(board, piece, queenCoord, slidingAttackMap);
        }

        piece = friendlyColor == WHITE ? BLACK_ROOK : WHITE_ROOK;
        List<ChessCoordinate> attackingRooks = attackingRooks();
        for (int idx = 0; idx < attackingRooks.size(); idx++) {
            ChessCoordinate rookCoord = attackingRooks.get(idx);
            updateSlidingAttackPiece(board, piece, rookCoord, slidingAttackMap);
        }

        piece = friendlyColor == WHITE ? BLACK_BISHOP : WHITE_BISHOP;
        List<ChessCoordinate> attackingBishops = attackingBishops();
        for (int idx = 0; idx < attackingBishops.size(); idx++) {
            ChessCoordinate bishopCoord = attackingBishops.get(idx);
            updateSlidingAttackPiece(board, piece, bishopCoord, slidingAttackMap);
        }

        return slidingAttackMap;
    }

    private void generateKingMoves() {
        Piece movingKing = friendlyColor == WHITE ? WHITE_KING : BLACK_KING;
        List<List<ChessCoordinate>> possibleEndCoordinates = movingKing.getReachableCoordinateMapFrom(friendlyKingCoord);

        // Add moves for the regular king moves
        for (int endCoordIdx = 0; endCoordIdx < 8; endCoordIdx++) {
            List<ChessCoordinate> targetCoords = possibleEndCoordinates.get(endCoordIdx);
            for (int targetCoordIdx = 0; targetCoordIdx < targetCoords.size(); targetCoordIdx++) {
                ChessCoordinate targetCoord = targetCoords.get(targetCoordIdx);

                if (opponentAttackMap.isMarked(targetCoord.getOndDimIndex())) {
                    continue;
                }

                if (board.isOccupied(targetCoord)) {
                    // Skip endCoordinates that are occupied by friendly pieces.
                    if (board.isOccupiedByColor(targetCoord, friendlyColor)) continue;

                    moves.add(new Move(friendlyKingCoord, targetCoord, targetCoord, null));
                } else {
                    moves.add(new Move(friendlyKingCoord, targetCoord));
                }
            }
        }

        // Add castling moves
        for (int endCoordIdx = 8; endCoordIdx <= 9; endCoordIdx++) {
            List<ChessCoordinate> targetCoords = possibleEndCoordinates.get(endCoordIdx);
            for (int targetCoordIdx = 0; targetCoordIdx < targetCoords.size(); targetCoordIdx++) {
                ChessCoordinate targetCoord = targetCoords.get(targetCoordIdx);

                if (board.isOccupied(targetCoord)) continue;

                boolean canCastle = true;
                if (targetCoord.getFile() == 6) {
                    if (game.canKingSideCastle(movingKing.getColor())) {
                        ChessCoordinate searchCoord = friendlyKingCoord;
                        for (int numChecked = 0; numChecked < 3;
                             numChecked++, searchCoord = RIGHT.next(searchCoord)) {
                            if (numChecked > 0 && board.isOccupied(searchCoord)
                                    || opponentAttackMap.isMarked(searchCoord.getOndDimIndex())) {
                                canCastle = false;
                                break;
                            }
                        }
                        if (canCastle) {
                            makeCastleMove(friendlyKingCoord, movingKing, targetCoord, H1, H8, F1, F8);
                        }
                    }
                } else {
                    if (game.canQueenSideCastle(movingKing.getColor())) {
                        ChessCoordinate searchCoord = friendlyKingCoord;
                        for (int numChecked = 0; numChecked < 3;
                             numChecked++, searchCoord = LEFT.next(searchCoord)) {
                            if (numChecked > 0 && board.isOccupied(searchCoord)
                                    || opponentAttackMap.isMarked(searchCoord.getOndDimIndex())) {
                                canCastle = false;
                                break;
                            }
                        }
                        canCastle = canCastle && !board.isOccupied(searchCoord);
                        if (canCastle) {
                            makeCastleMove(friendlyKingCoord, movingKing, targetCoord, A1, A8, D1, D8);
                        }
                    }
                }
            }
        }
    }

    private void makeCastleMove(ChessCoordinate movingKingCoord, Piece movingKing,
                                ChessCoordinate kingEndCoord, ChessCoordinate whiteRookStart,
                                ChessCoordinate blackRookStart, ChessCoordinate whiteRookEnd,
                                ChessCoordinate blackRookEnd) {
        ChessCoordinate rookStart = kingEndCoord.getRank() == 0 ? whiteRookStart : blackRookStart;
        ChessCoordinate endRookCoord = kingEndCoord.getRank() == 0 ? whiteRookEnd : blackRookEnd;
        moves.add(new Move(movingKingCoord, kingEndCoord, rookStart, endRookCoord));
    }

    private void generateRookAndBishopMoves(long board, long pinRayMap, Piece friendlyPiece) {
        long pinnedPieces = board & pinRayMap;
        long unpinnedPieces = board & ~pinRayMap;

        generateSlidingPieceMoves(friendlyPiece, pinnedPieces, pinRayMap);
        generateSlidingPieceMoves(friendlyPiece, unpinnedPieces, 0xFFFFFFFFFFFFFFFFL);
    }

    private void generateQueenMoves() {
        long queens = board.getQueens(friendlyQueen);
        long hvPinnedQueens = queens & hvPinRayMap;
        long d12PinnedQueens = queens & d12PinRayMap;
        long unpinnedQueens = queens & ~(hvPinRayMap | d12PinRayMap);

        generateSlidingPieceMoves(friendlyQueen, hvPinnedQueens, hvPinRayMap);
        generateSlidingPieceMoves(friendlyQueen, d12PinnedQueens, d12PinRayMap);
        generateSlidingPieceMoves(friendlyQueen, unpinnedQueens, 0xFFFFFFFFFFFFFFFFL);
    }

    private void generateSlidingPieceMoves(Piece piece, long pieceMap, long pinMask) {
        BitIterator bitIterator = new BitIterator(pieceMap);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();

            long moveMask = 0x0;
            Directions directions = piece.getDirections();
            for (Direction direction : directions) {
                moveMask |= getMoveMask(coordinate, direction, board.getOccupancyMap());
            }
            moveMask &= ~board.getOccupancyMap(game.getTurn()) & checkRayMap & pinMask;
            addMoves(friendlyQueen, coordinate, moveMask);
        }
    }

    private void addMoves(Piece piece, ChessCoordinate startingCoordinate, long moveMask) {
        int movesStart = moves.size();
        int numBits = Long.bitCount(moveMask);
        BitIterator bitIterator = new BitIterator(moveMask);
        while (bitIterator.hasNext()) {
            ChessCoordinate endingCoordinate = bitIterator.next();

            if (board.getPieceOn(endingCoordinate) != null) {
                moves.add(new Move(startingCoordinate, endingCoordinate, endingCoordinate, null));
            } else {
                moves.add(new Move(startingCoordinate, endingCoordinate));
            }
        }
        if (numBits != moves.size() - movesStart) {
            System.out.println(moveMask);
        }
    }

    private long getMoveMask(ChessCoordinate coordinate, Direction direction, long board) {
        long moveMask = 0;

        long relevantBits = board & coordinateToMask[coordinate.getOndDimIndex()][direction.ordinal()];
        byte movementRay = getRay(relevantBits, coordinate, direction);
        byte position = getRay(coordinate.getBitMask(), coordinate, direction);

        switch (direction) {
            case UP, UP_RIGHT, RIGHT, DOWN_RIGHT -> {
                byte leftBits = (byte) (-position ^ position);
                byte rightBits = (byte) (((Integer.lowestOneBit(movementRay) - 1) << 1) + 1);
                moveMask = getMap((byte) (leftBits & rightBits), coordinate, direction);
            }
            case UP_LEFT, LEFT, DOWN_LEFT, DOWN -> {
                byte rightBits = (byte) (position - 1);
                byte leftBits = (byte) (((byte) 0x80) >> LEADING_ZEROS[movementRay & 0xFF]);
                moveMask = getMap((byte) (leftBits & rightBits), coordinate, direction);
            }
        }

        return moveMask;
    }

    private void generateKnightMoves() {
        long knights = board.getKnights(friendlyKnight) & ~(hvPinRayMap | d12PinRayMap);
        BitIterator bitIterator = new BitIterator(knights);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            long moveMask = knightMoveMask[coordinate.getOndDimIndex()] & ~board.getOccupancyMap(friendlyColor) & checkRayMap;
            addMoves(friendlyKnight, coordinate, moveMask);
        }
    }

    private void generatePawnMoves() {
        Piece pawn = friendlyColor == WHITE ? WHITE_PAWN : BLACK_PAWN;
        for (ChessCoordinate coordinate : friendlyPawns()) {
            List<List<ChessCoordinate>> finalCoordinates = pawn.getReachableCoordinateMapFrom(coordinate);
            boolean isPinned = isPinned(coordinate);

            List<ChessCoordinate> targetCoords = finalCoordinates.get(0);
            for (int targetCoordIdx = 0; targetCoordIdx < targetCoords.size(); targetCoordIdx++) {
                ChessCoordinate targetCoord = targetCoords.get(targetCoordIdx);
                if (!isPinned || areAligned(coordinate, friendlyKingCoord, targetCoord)) {
                    if (!board.isOccupied(targetCoord)) {
                        if (!inCheck || ((checkRayMap & targetCoord.getBitMask()) != 0)) {
                            if ((pawn == WHITE_PAWN && targetCoord.getRank() == 7)
                                    || (pawn == BLACK_PAWN && targetCoord.getRank() == 0)) {
                                // Promotion square
                                if (pawn.getColor() == WHITE) {
                                    makePromotionMoves(coordinate, targetCoord, null,
                                            WHITE_QUEEN, WHITE_ROOK, WHITE_BISHOP, WHITE_KNIGHT);
                                } else {
                                    makePromotionMoves(coordinate, targetCoord, null,
                                            BLACK_QUEEN, BLACK_ROOK, BLACK_BISHOP, BLACK_KNIGHT);
                                }
                            } else {
                                moves.add(new Move(coordinate, targetCoord));
                            }
                        }
                    } else {
                        break;
                    }
                }
            }

            targetCoords = finalCoordinates.get(1);
            for (int targetCoordIdx = 0; targetCoordIdx < targetCoords.size(); targetCoordIdx++) {
                ChessCoordinate captureRight = targetCoords.get(targetCoordIdx);
                addAttackingPawnMoves(coordinate, isPinned, captureRight, RIGHT);
            }

            targetCoords = finalCoordinates.get(2);
            for (int targetCoordIdx = 0; targetCoordIdx < targetCoords.size(); targetCoordIdx++) {
                ChessCoordinate captureLeft = targetCoords.get(targetCoordIdx);
                addAttackingPawnMoves(coordinate, isPinned, captureLeft, LEFT);
            }
        }
    }

    private void addAttackingPawnMoves(ChessCoordinate coordinate, boolean isPinned,
                                       ChessCoordinate captureCoord, Direction direction) {
        Piece pawn = friendlyColor == WHITE ? WHITE_PAWN : BLACK_PAWN;

        if (isPinned && !areAligned(coordinate, friendlyKingCoord, captureCoord)) {
            return;
        }

        if (!board.isOccupied(captureCoord)) {
            // Must try for enPassant
            ChessCoordinate enPassantTarget = game.getEnPassantTarget();
            if (Objects.equals(enPassantTarget, captureCoord)) {
                ChessCoordinate targetCoord = direction.next(coordinate);

                if ((!inCheck // Proceed if not in check
                        || (checkRayMap & captureCoord.getBitMask()) != 0    // Proceed if move blocks check
                        || (checkRayMap & targetCoord.getBitMask()) != 0)     // Proceed if move captures attacker
                        && (board.isOccupied(targetCoord) // Ensure that the targetPiece is a pawn
                        && (!areAligned(coordinate, friendlyKingCoord, targetCoord) // Proceed if not aligned with king
                        || !longRangeAttacker(coordinate, targetCoord)))) { // Proceed if not attacker
                    moves.add(new Move(coordinate, captureCoord, targetCoord, null));
                }
            }
        } else {
            if (!inCheck || (checkRayMap & captureCoord.getBitMask()) != 0) {
                if (board.isOccupiedByColor(captureCoord, attackingColor)) {
                    if (pawn == WHITE_PAWN && captureCoord.getRank() == 7) {
                        makePromotionMoves(coordinate, captureCoord, captureCoord, WHITE_QUEEN, WHITE_ROOK, WHITE_BISHOP, WHITE_KNIGHT);
                    } else if (pawn == BLACK_PAWN && captureCoord.getRank() == 0) {
                        makePromotionMoves(coordinate, captureCoord, captureCoord, BLACK_QUEEN, BLACK_ROOK, BLACK_BISHOP, BLACK_KNIGHT);
                    } else {
                        moves.add(new Move(coordinate, captureCoord, captureCoord, null));
                    }
                }
            }
        }
    }

    private void makePromotionMoves(ChessCoordinate coordinate, ChessCoordinate targetCoord,
                                    ChessCoordinate interactingPieceStart,
                                    Piece queen, Piece rook, Piece bishop, Piece knight) {
        moves.add(new Move(coordinate, targetCoord, interactingPieceStart, null, queen));
        moves.add(new Move(coordinate, targetCoord, interactingPieceStart, null, rook));
        moves.add(new Move(coordinate, targetCoord, interactingPieceStart, null, bishop));
        moves.add(new Move(coordinate, targetCoord, interactingPieceStart, null, knight));
    }

    private boolean longRangeAttacker(ChessCoordinate coordinate, ChessCoordinate capturedCoord) {
        int xDiff = coordinate.getFile() - friendlyKingCoord.getFile();
        Direction kingToPawn = xDiff > 0 ? RIGHT : LEFT;
        boolean longRangeAttackerExists = false;

        for (ChessCoordinate searchCoord = kingToPawn.next(friendlyKingCoord);
             searchCoord != null;
             searchCoord = kingToPawn.next(searchCoord)) {

            if (!board.isOccupied(searchCoord) || searchCoord == coordinate || searchCoord == capturedCoord) {
                continue;
            }

            if (board.isOccupiedByColor(searchCoord, attackingColor) &&
                    (board.isQueen(searchCoord) || board.isRook(searchCoord))) {
                longRangeAttackerExists = true;
            }
            break;
        }

        return longRangeAttackerExists;
    }

    private boolean isPinned(ChessCoordinate coordinate) {
        return pinsExistInPosition
                && ((hvPinRayMap & coordinate.getBitMask()) != 0
                || (d12PinRayMap & coordinate.getBitMask()) != 0);
    }

    public FastMap getOpponentAttackMap() {
        return opponentAttackMap;
    }

    private List<ChessCoordinate> friendlyPawns() {
        return board.getLocations(friendlyPawn);
    }

    private List<ChessCoordinate> friendlyKnights() {
        return board.getLocations(friendlyKnight);
    }

    private List<ChessCoordinate> friendlyBishops() {
        return board.getLocations(friendlyBishop);
    }

    private List<ChessCoordinate> friendlyRooks() {
        return board.getLocations(friendlyRook);
    }

    private List<ChessCoordinate> friendlyQueens() {
        return board.getLocations(friendlyQueen);
    }

    private List<ChessCoordinate> attackingPawns() {
        return board.getLocations(attackingPawn);
    }

    private List<ChessCoordinate> attackingKnights() {
        return board.getLocations(attackingKnight);
    }

    private List<ChessCoordinate> attackingBishops() {
        return board.getLocations(attackingBishop);
    }

    private List<ChessCoordinate> attackingRooks() {
        return board.getLocations(attackingRook);
    }

    private List<ChessCoordinate> attackingQueens() {
        return board.getLocations(attackingQueen);
    }

    private static class MoveLRUCache extends LinkedHashMap<Long, List<Move>> {
        private final int max_entries;

        public MoveLRUCache(int max_entries) {
            super((int) (max_entries / 0.75), 0.75f, true);
            this.max_entries = max_entries;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, List<Move>> eldest) {
            return size() > max_entries;
        }
    }

    private static class BitIterator implements Iterator<ChessCoordinate> {

        private long bits;
        private int index;

        public BitIterator(long bits) {
            this.index = Long.numberOfTrailingZeros(bits) + 1;
            this.bits = bits >>> index;
        }

        @Override
        public boolean hasNext() {
            return index < 65;
        }

        @Override
        public ChessCoordinate next() {
            ChessCoordinate nextCoordinate = ChessCoordinate.getChessCoordinate(index - 1);
            int difference = Long.numberOfTrailingZeros(bits) + 1;
            index += difference;
            bits >>>= difference;
            return nextCoordinate;
        }
    }
        }
