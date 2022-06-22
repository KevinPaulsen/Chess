package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
import chess.model.pieces.Piece;

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

    private static final long ONES = 0xFFFFFFFFFFFFFFFFL;
    private static final int BITS_IN_BYTE = 8;

    private static final long[] FILE_MASKS = {
            0x0101010101010101L,
            0x0202020202020202L,
            0x0404040404040404L,
            0x0808080808080808L,
            0x1010101010101010L,
            0x2020202020202020L,
            0x4040404040404040L,
            0x8080808080808080L,
    };

    private static final long[] ROW_MASKS = {
            0x00000000000000FFL,
            0x000000000000FF00L,
            0x0000000000FF0000L,
            0x00000000FF000000L,
            0x000000FF00000000L,
            0x0000FF0000000000L,
            0x00FF000000000000L,
            0xFF00000000000000L,
    };

    /**
     * Accessed by
     * coordinateToMask[posIdx][direction.ordinal]
     */
    private static final long[][] coordinateToMask = createDirectionMaskMap();

    private static final long[] KNIGHT_MOVE_MASKS = createKnightMoveMasks();

    private static final long[] KING_MOVE_MASKS = createKingMoveMasks();

    private final GameModel game;
    private final BoardModel board;
    private long checkRayMask;
    private long hvPinRayMap;
    private long d12PinRayMap;
    private long opponentAttackMap;
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

        this.checkRayMask = 0;
        this.hvPinRayMap = 0;
        this.d12PinRayMap = 0;
        this.opponentAttackMap = 0x0;
    }

    private static boolean areAligned(ChessCoordinate coordinate1, ChessCoordinate coordinate2,
                                      ChessCoordinate coordinate3) {
        float product1 = (coordinate1.getRank() - coordinate2.getRank()) * (coordinate1.getFile() - coordinate3.getFile());
        float product2 = (coordinate1.getFile() - coordinate2.getFile()) * (coordinate1.getRank() - coordinate3.getRank());
        return product1 == product2;
    }

    private void resetState() {
        this.moves = new ArrayList<>(150);
        this.inCheck = false;
        this.inDoubleCheck = false;
        this.pinsExistInPosition = false;

        this.checkRayMask = 0;
        this.hvPinRayMap = 0;
        this.d12PinRayMap = 0;
        this.opponentAttackMap = 0;

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
        opponentAttackMap |= calculateSlidingAttackMap();

        // Calculate the pin and check rays
        boolean hasQueens = board.getQueens(attackingQueen) != 0;
        if (board.getRooks(attackingRook) != 0 || hasQueens) {
            calculatePinsAndCheckRays(board, WHITE_ROOK.getReachableCoordinateMapFrom(friendlyKingCoord), false);
        }
        if (board.getBishops(attackingBishop) != 0 || hasQueens) {
            calculatePinsAndCheckRays(board, WHITE_BISHOP.getReachableCoordinateMapFrom(friendlyKingCoord), true);
        }

        // Calculate Knight Attacks
        long friendlyKingMask = friendlyKingCoord.getBitMask();
        BitIterator bitIterator = new BitIterator(board.getKnights(attackingKnight));
        while (bitIterator.hasNext()) {
            ChessCoordinate knightCoord = bitIterator.next();
            long attackMask = KNIGHT_MOVE_MASKS[knightCoord.getOndDimIndex()];
            opponentAttackMap |= attackMask;

            if ((attackMask & friendlyKingMask) != 0) {
                inDoubleCheck = inCheck;
                inCheck = true;
                checkRayMask |= knightCoord.getBitMask();
            }
        }

        long pawns = board.getPawns(attackingPawn);
        long pawnAttackSquares;
        if (friendlyColor == BLACK) {
            pawnAttackSquares = ((pawns & ~FILE_MASKS[0]) << 7) | ((pawns & ~FILE_MASKS[7]) << 9);
        } else {
            pawnAttackSquares = ((pawns & ~FILE_MASKS[0]) >>> 9) | ((pawns & ~FILE_MASKS[7]) >>> 7);
        }

        opponentAttackMap |= pawnAttackSquares;
        if ((pawnAttackSquares & friendlyKingMask) != 0) {
            inDoubleCheck = inCheck;
            inCheck = true;
            int kingFile = friendlyKingCoord.getFile();
            long attackingPieceMask = ROW_MASKS[friendlyKingCoord.getRank() + ((friendlyColor == WHITE) ? 1 : -1)]
                    & ((kingFile > 0 ? FILE_MASKS[friendlyKingCoord.getFile() - 1] : 0)
                    | (kingFile < 7 ? FILE_MASKS[friendlyKingCoord.getFile() + 1] : 0));
            checkRayMask |= pawns & attackingPieceMask;
        }//*/

        // Calculate Pawn Attacks
        /*bitIterator = new BitIterator(board.getPawns(attackingPawn));
        while (bitIterator.hasNext()) {
            ChessCoordinate pawnCoord = bitIterator.next();
            List<List<ChessCoordinate>> reachableCoordinates = attackingPawn.getReachableCoordinateMapFrom(pawnCoord);
            for (int rayIdx = 1; rayIdx <= 2; rayIdx++) {
                updateAttackingSquares(friendlyKingCoord, pawnCoord, reachableCoordinates, rayIdx);
            }
        }//*/

        // Calculate King Attacks
        opponentAttackMap |= KING_MOVE_MASKS[attackingKingSquare.getOndDimIndex()];

        if (checkRayMask == 0) checkRayMask = ~checkRayMask;
    }

    private void updateAttackingSquares(ChessCoordinate kingCoord, ChessCoordinate pieceCoord,
                                        List<List<ChessCoordinate>> reachableCoordinates, int rayIdx) {
        List<ChessCoordinate> ray = reachableCoordinates.get(rayIdx);
        for (int coordIdx = 0; coordIdx < ray.size(); coordIdx++) {
            ChessCoordinate targetCoord = ray.get(coordIdx);
            opponentAttackMap |= targetCoord.getBitMask();
            if (targetCoord.equals(kingCoord)) {
                inDoubleCheck = inCheck;
                inCheck = true;
                checkRayMask |= pieceCoord.getBitMask();
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
                                checkRayMask |= rayMap;
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
    private long calculateSlidingAttackMap() {
        long slidingAttackMap = 0x0;
        long board = this.board.getOccupancyMap() ^ friendlyKingCoord.getBitMask();

        Piece piece = friendlyColor == WHITE ? BLACK_QUEEN : WHITE_QUEEN;
        BitIterator bitIterator = new BitIterator(this.board.getQueens(piece));
        while (bitIterator.hasNext()) {
            slidingAttackMap |= generateSlidingPieceMoves(piece, bitIterator.next(), board);
        }

        piece = friendlyColor == WHITE ? BLACK_ROOK : WHITE_ROOK;
        bitIterator = new BitIterator(this.board.getQueens(piece));
        while (bitIterator.hasNext()) {
            slidingAttackMap |= generateSlidingPieceMoves(piece, bitIterator.next(), board);
        }

        piece = friendlyColor == WHITE ? BLACK_BISHOP : WHITE_BISHOP;
        bitIterator = new BitIterator(this.board.getQueens(piece));
        while (bitIterator.hasNext()) {
            slidingAttackMap |= generateSlidingPieceMoves(piece, bitIterator.next(), board);
        }

        return slidingAttackMap;
    }

    private void generateKingMoves() {
        Piece movingKing = friendlyColor == WHITE ? WHITE_KING : BLACK_KING;
        List<List<ChessCoordinate>> possibleEndCoordinates = movingKing.getReachableCoordinateMapFrom(friendlyKingCoord);

        // Add moves for the regular king moves
        long kingMoveMask = KING_MOVE_MASKS[friendlyKingCoord.getOndDimIndex()]
                & ~(board.getOccupancyMap(friendlyColor) | opponentAttackMap);
        addMoves(movingKing, friendlyKingCoord, kingMoveMask);

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
                                    || ((opponentAttackMap >>> searchCoord.getOndDimIndex()) & 1) == 1) {
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
                                    || ((opponentAttackMap >>> searchCoord.getOndDimIndex()) & 1) == 1) {
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

    private void generateRookAndBishopMoves(long slidingPieceMask, long pinMask, Piece friendlyPiece) {
        long pinnedPieces = slidingPieceMask & pinMask;
        long unpinnedPieces = slidingPieceMask & ~pinMask;

        BitIterator bitIterator = new BitIterator(pinnedPieces);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            long legalMoveMap = generateSlidingPieceMoves(friendlyPiece, coordinate, board.getOccupancyMap())
                    & pinMask & checkRayMask & ~board.getOccupancyMap(friendlyColor);
            addMoves(friendlyPiece, coordinate, legalMoveMap);
        }

        bitIterator = new BitIterator(unpinnedPieces);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            long legalMoveMap = generateSlidingPieceMoves(friendlyPiece, coordinate, board.getOccupancyMap())
                    & ONES & checkRayMask & ~board.getOccupancyMap(friendlyColor);
            addMoves(friendlyPiece, coordinate, legalMoveMap);
        }
    }

    private void generateQueenMoves() {
        long queens = board.getQueens(friendlyQueen);
        long hvPinnedQueens = queens & hvPinRayMap;
        long d12PinnedQueens = queens & d12PinRayMap;
        long unpinnedQueens = queens & ~(hvPinRayMap | d12PinRayMap);

        BitIterator bitIterator = new BitIterator(hvPinnedQueens);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            long legalMoveMap = generateSlidingPieceMoves(friendlyQueen, coordinate, board.getOccupancyMap())
                    & hvPinRayMap & checkRayMask & ~board.getOccupancyMap(friendlyColor);
            addMoves(friendlyQueen, coordinate, legalMoveMap);
        }

        bitIterator = new BitIterator(d12PinnedQueens);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            long legalMoveMap = generateSlidingPieceMoves(friendlyQueen, coordinate, board.getOccupancyMap())
                    & d12PinRayMap & checkRayMask & ~board.getOccupancyMap(friendlyColor);
            addMoves(friendlyQueen, coordinate, legalMoveMap);
        }

        bitIterator = new BitIterator(unpinnedQueens);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            long legalMoveMap = generateSlidingPieceMoves(friendlyQueen, coordinate, board.getOccupancyMap())
                    & ONES & checkRayMask & ~board.getOccupancyMap(friendlyColor);
            addMoves(friendlyQueen, coordinate, legalMoveMap);
        }
    }

    private long generateSlidingPieceMoves(Piece piece, ChessCoordinate coordinate, long board) {
        long moveMask = 0x0;
        Directions directions = piece.getDirections();
        for (Direction direction : directions) {
            moveMask |= getMoveMask(coordinate, direction, board);
        }
        return moveMask;
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

        long rayMask = coordinateToMask[coordinate.getOndDimIndex()][direction.ordinal()];
        long relevantBits = board & rayMask;

        switch (direction) {
            case DOWN, LEFT, DOWN_LEFT, DOWN_RIGHT -> {
                long leftBits = coordinate.getBitMask() - 1;
                long rightBits = relevantBits == 0 ? ONES : -Long.highestOneBit(relevantBits);
                moveMask = leftBits & rightBits & rayMask;
            }
            case UP_LEFT, UP, UP_RIGHT, RIGHT -> {
                long leftBits = Long.lowestOneBit(relevantBits << 1) - 1;
                long rightBits = -coordinate.getBitMask() << 1;
                moveMask = leftBits & rightBits & rayMask;
            }
        }

        return moveMask;
    }

    private void generateKnightMoves() {
        long knights = board.getKnights(friendlyKnight) & ~(hvPinRayMap | d12PinRayMap);
        BitIterator bitIterator = new BitIterator(knights);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            long moveMask = KNIGHT_MOVE_MASKS[coordinate.getOndDimIndex()] & ~board.getOccupancyMap(friendlyColor) & checkRayMask;
            addMoves(friendlyKnight, coordinate, moveMask);
        }
    }

    private void generatePawnMoves() {
        BitIterator bitIterator = new BitIterator(board.getPawns(friendlyPawn));
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            List<List<ChessCoordinate>> finalCoordinates = friendlyPawn.getReachableCoordinateMapFrom(coordinate);
            boolean isPinned = isPinned(coordinate);

            List<ChessCoordinate> targetCoords = finalCoordinates.get(0);
            for (int targetCoordIdx = 0; targetCoordIdx < targetCoords.size(); targetCoordIdx++) {
                ChessCoordinate targetCoord = targetCoords.get(targetCoordIdx);
                if (!isPinned || areAligned(coordinate, friendlyKingCoord, targetCoord)) {
                    if (!board.isOccupied(targetCoord)) {
                        if (!inCheck || ((checkRayMask & targetCoord.getBitMask()) != 0)) {
                            if ((friendlyPawn == WHITE_PAWN && targetCoord.getRank() == 7)
                                    || (friendlyPawn == BLACK_PAWN && targetCoord.getRank() == 0)) {
                                // Promotion square
                                if (friendlyPawn.getColor() == WHITE) {
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
                        || (checkRayMask & captureCoord.getBitMask()) != 0    // Proceed if move blocks check
                        || (checkRayMask & targetCoord.getBitMask()) != 0)     // Proceed if move captures attacker
                        && (board.isOccupied(targetCoord) // Ensure that the targetPiece is a pawn
                        && (!areAligned(coordinate, friendlyKingCoord, targetCoord) // Proceed if not aligned with king
                        || !longRangeAttacker(coordinate, targetCoord)))) { // Proceed if not attacker
                    moves.add(new Move(coordinate, captureCoord, targetCoord, null));
                }
            }
        } else {
            if (!inCheck || (checkRayMask & captureCoord.getBitMask()) != 0) {
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

    public long getOpponentAttackMap() {
        return opponentAttackMap;
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

    private static long[][] createDirectionMaskMap() {
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

    private static long[] createKnightMoveMasks() {
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

    private static long[] createKingMoveMasks() {
        long[] kingMoveMasks = new long[64];

        for (int coordIdx = 0; coordIdx < kingMoveMasks.length; coordIdx++) {
            ChessCoordinate coordinate = getChessCoordinate(coordIdx);
            long mask = 0x0;

            if (coordinate.getFile() > 0) mask |= (coordinate.getBitMask() >>> 1);
            if (coordinate.getFile() > 0 && coordinate.getRank() > 0) mask |= (coordinate.getBitMask()) >>> 9;
            if (coordinate.getFile() > 0 && coordinate.getRank() < 7) mask |= (coordinate.getBitMask()) << 7;
            if (coordinate.getFile() < 7) mask |= (coordinate.getBitMask() << 1);
            if (coordinate.getFile() < 7 && coordinate.getRank() > 0) mask |= (coordinate.getBitMask() >> 7);
            if (coordinate.getFile() < 7 && coordinate.getRank() < 7) mask |= (coordinate.getBitMask() << 9);
            if (coordinate.getRank() > 0) mask |= (coordinate.getBitMask() >>> 8);
            if (coordinate.getRank() < 7) mask |= (coordinate.getBitMask() << 8);

            kingMoveMasks[coordIdx] = mask;
        }

        return kingMoveMasks;
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
