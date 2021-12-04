package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
import chess.model.pieces.Piece;
import chess.util.FastMap;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static chess.model.pieces.Piece.*;

/**
 * This class is responsible for generating moves.
 */
public class MoveGenerator {

    private final GameModel game;
    private List<Move> moves;
    private final FastMap checkRayMap;
    private final FastMap pinRayMap;
    private final FastMap opponentAttackMap;
    private boolean inCheck;
    private boolean inDoubleCheck;
    private boolean pinsExistInPosition;

    private final List<ChessCoordinate> friendlyQueens;
    private final List<ChessCoordinate> attackingQueens;
    private final List<ChessCoordinate> friendlyRooks;
    private final List<ChessCoordinate> attackingRooks;
    private final List<ChessCoordinate> friendlyBishops;
    private final List<ChessCoordinate> attackingBishops;
    private final List<ChessCoordinate> friendlyKnights;
    private final List<ChessCoordinate> attackingKnights;
    private final List<ChessCoordinate> friendlyPawns;
    private final List<ChessCoordinate> attackingPawns;

    public MoveGenerator(GameModel game) {
        this.game = game;
        this.moves = new ArrayList<>(100);
        this.inCheck = false;
        this.inDoubleCheck = false;
        this.pinsExistInPosition = false;

        this.checkRayMap = new FastMap();
        this.pinRayMap = new FastMap();
        this.opponentAttackMap = new FastMap();

        this.friendlyQueens = new ArrayList<>(8);
        this.attackingQueens = new ArrayList<>(8);
        this.friendlyRooks = new ArrayList<>(8);
        this.attackingRooks = new ArrayList<>(8);
        this.friendlyBishops = new ArrayList<>(8);
        this.attackingBishops = new ArrayList<>(8);
        this.friendlyKnights = new ArrayList<>(8);
        this.attackingKnights = new ArrayList<>(8);
        this.friendlyPawns = new ArrayList<>(8);
        this.attackingPawns = new ArrayList<>(8);
    }

    private void resetState() {
        this.moves = new ArrayList<>(100);
        this.inCheck = false;
        this.inDoubleCheck = false;
        this.pinsExistInPosition = false;

        this.checkRayMap.clear();
        this.pinRayMap.clear();
        this.opponentAttackMap.clear();

        this.friendlyQueens.clear();
        this.attackingQueens.clear();
        this.friendlyRooks.clear();
        this.attackingRooks.clear();
        this.friendlyBishops.clear();
        this.attackingBishops.clear();
        this.friendlyKnights.clear();
        this.attackingKnights.clear();
        this.friendlyPawns.clear();
        this.attackingPawns.clear();
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
                Piece targetPiece = board.getPieceOn(potentialCoordinate);
                map.mergeMask(potentialCoordinate.getBitMask());

                if (targetPiece != null && (targetPiece.getColor() == attackingPiece.getColor()
                        || targetPiece != WHITE_KING && targetPiece != BLACK_KING)) {
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

    public List<Move> generateMoves() {
        resetState();
        findPieces();
        calculateAttackData();

        generateKingMoves();

        if (inDoubleCheck) {
            return moves;
        }

        generateSlidingMoves();
        generateKnightMoves();
        generatePawnMoves();

        return moves;
    }

    private void findPieces() {
        BoardModel board = game.getBoard();
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                ChessCoordinate coordinate = BoardModel.getChessCoordinate(file, rank);
                Piece piece = board.getPieceOn(coordinate);
                char turn = game.getTurn();
                if (piece != null) {
                    switch (piece) {
                        case WHITE_QUEEN:
                            if (turn == 'w') friendlyQueens.add(coordinate);
                            else attackingQueens.add(coordinate);
                            break;
                        case WHITE_ROOK:
                            if (turn == 'w') friendlyRooks.add(coordinate);
                            else attackingRooks.add(coordinate);
                            break;
                        case WHITE_BISHOP:
                            if (turn == 'w') friendlyBishops.add(coordinate);
                            else attackingBishops.add(coordinate);
                            break;
                        case WHITE_KNIGHT:
                            if (turn == 'w') friendlyKnights.add(coordinate);
                            else attackingKnights.add(coordinate);
                            break;
                        case WHITE_PAWN:
                            if (turn == 'w') friendlyPawns.add(coordinate);
                            else attackingPawns.add(coordinate);
                            break;
                        case BLACK_QUEEN:
                            if (turn == 'b') friendlyQueens.add(coordinate);
                            else attackingQueens.add(coordinate);
                            break;
                        case BLACK_ROOK:
                            if (turn == 'b') friendlyRooks.add(coordinate);
                            else attackingRooks.add(coordinate);
                            break;
                        case BLACK_BISHOP:
                            if (turn == 'b') friendlyBishops.add(coordinate);
                            else attackingBishops.add(coordinate);
                            break;
                        case BLACK_KNIGHT:
                            if (turn == 'b') friendlyKnights.add(coordinate);
                            else attackingKnights.add(coordinate);
                            break;
                        case BLACK_PAWN:
                            if (turn == 'b') friendlyPawns.add(coordinate);
                            else attackingPawns.add(coordinate);
                            break;
                    }
                }
            }
        }
    }

    private void calculateAttackData() {
        opponentAttackMap.merge(calculateSlidingAttackMap(game));

        BoardModel board = game.getBoard();
        char turn = game.getTurn();
        ChessCoordinate kingCoord = turn == 'w' ? board.getWhiteKingCoord() : board.getBlackKingCoord();

        // Calculate the pin and check rays
        if (attackingRooks.size() > 0 || attackingQueens.size() > 0) {
            calculatePinsAndCheckRays(board, WHITE_ROOK.getReachableCoordinateMapFrom(kingCoord),
                    false, turn);
        }
        if (attackingBishops.size() > 0 || attackingQueens.size() > 0) {
            calculatePinsAndCheckRays(board, WHITE_BISHOP.getReachableCoordinateMapFrom(kingCoord),
                    true, turn);
        }

        // Calculate Knight Attacks
        for (int knightCoordIdx = 0; knightCoordIdx < attackingKnights.size(); knightCoordIdx++) {
            ChessCoordinate knightCoord = attackingKnights.get(knightCoordIdx);
            List<List<ChessCoordinate>> reachableCoordinates = WHITE_KNIGHT.getReachableCoordinateMapFrom(knightCoord);
            for (int rayIdx = 0; rayIdx < reachableCoordinates.size(); rayIdx++) {
                updateAttackingSquares(kingCoord, knightCoord, reachableCoordinates, rayIdx);
            }
        }

        // Calculate Pawn Attacks
        for (int pawnCoordIdx = 0; pawnCoordIdx < attackingPawns.size(); pawnCoordIdx++) {
            ChessCoordinate pawnCoord = attackingPawns.get(pawnCoordIdx);
            Piece pawn = board.getPieceOn(pawnCoord);
            List<List<ChessCoordinate>> reachableCoordinates = pawn.getReachableCoordinateMapFrom(pawnCoord);
            for (int rayIdx = 1; rayIdx <= 2; rayIdx++) {
                updateAttackingSquares(kingCoord, pawnCoord, reachableCoordinates, rayIdx);
            }
        }

        ChessCoordinate opponentKingCoord = turn == 'w' ? board.getBlackKingCoord() : board.getWhiteKingCoord();
        Piece opponentKing = board.getPieceOn(opponentKingCoord);

        // Calculate King Attacks
        List<List<ChessCoordinate>> reachableCoordinates = opponentKing.getReachableCoordinateMapFrom(opponentKingCoord);
        for (int rayIdx = 0; rayIdx < reachableCoordinates.size(); rayIdx++) {
            updateAttackingSquares(kingCoord, kingCoord, reachableCoordinates, rayIdx);
        }
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
                checkRayMap.mergeMask(pieceCoord.getBitMask());
            }
        }
    }

    private void calculatePinsAndCheckRays(BoardModel board, List<List<ChessCoordinate>> raysToCheck,
                                           boolean isDiagonal, char turn) {
        for (int rayIdx = 0; rayIdx < raysToCheck.size(); rayIdx++) {
            List<ChessCoordinate> ray = raysToCheck.get(rayIdx);
            if (inDoubleCheck) {
                break;
            }

            boolean friendlyRay = false;
            FastMap rayMap = new FastMap();

            for (int coordIdx = 0; coordIdx < ray.size(); coordIdx++) {
                ChessCoordinate coordinate = ray.get(coordIdx);
                Piece targetPiece = board.getPieceOn(coordinate);
                rayMap.mergeMask(coordinate.getBitMask());

                if (targetPiece != null) { // There is a piece on this square
                    if (targetPiece.getColor() == turn) { // This piece is our color
                        if (!friendlyRay) { // This is the first friendly piece we found
                            friendlyRay = true;
                        } else { // this is the second friendly piece we found.
                            break;
                        }
                    } else { // Piece is an enemy piece.
                        if ((targetPiece == WHITE_QUEEN || targetPiece == BLACK_QUEEN)
                                || (isDiagonal && (targetPiece == WHITE_BISHOP || targetPiece == BLACK_BISHOP))
                                || (!isDiagonal && (targetPiece == WHITE_ROOK || targetPiece == BLACK_ROOK))) {
                            if (friendlyRay) {
                                pinsExistInPosition = true;
                                pinRayMap.merge(rayMap);
                            } else {
                                checkRayMap.merge(rayMap);
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
     * @param game the relevant game.
     * @return the map of all the squares attacked by the sliding pieces.
     */
    private FastMap calculateSlidingAttackMap(GameModel game) {
        FastMap slidingAttackMap = new FastMap();

        for (int queenIdx = 0; queenIdx < attackingQueens.size(); queenIdx++) {
            ChessCoordinate queenCoord = attackingQueens.get(queenIdx);
            Piece queen = game.getBoard().getPieceOn(queenCoord);
            updateSlidingAttackPiece(game.getBoard(), queen, queenCoord, slidingAttackMap);
        }

        for (int rookIdx = 0; rookIdx < attackingRooks.size(); rookIdx++) {
            ChessCoordinate rookCoordinate = attackingRooks.get(rookIdx);
            Piece rook = game.getBoard().getPieceOn(rookCoordinate);
            updateSlidingAttackPiece(game.getBoard(), rook, rookCoordinate, slidingAttackMap);
        }

        for (int bishopIdx = 0; bishopIdx < attackingBishops.size(); bishopIdx++) {
            ChessCoordinate bishopCoordinate = attackingBishops.get(bishopIdx);
            Piece bishop = game.getBoard().getPieceOn(bishopCoordinate);
            updateSlidingAttackPiece(game.getBoard(), bishop, bishopCoordinate, slidingAttackMap);
        }

        return slidingAttackMap;
    }

    private void generateKingMoves() {
        BoardModel board = game.getBoard();
        ChessCoordinate movingKingCoord = game.getTurn() == 'w' ? board.getWhiteKingCoord() : board.getBlackKingCoord();
        Piece movingKing = board.getPieceOn(movingKingCoord);
        ChessCoordinate friendlyKingCoord = game.getTurn() == 'w' ?
                board.getWhiteKingCoord() : board.getBlackKingCoord();
        List<List<ChessCoordinate>> possibleEndCoordinates = movingKing.getReachableCoordinateMapFrom(movingKingCoord);

        // Add moves for the regular king moves
        for (int endCoordIdx = 0; endCoordIdx < 8; endCoordIdx++) {
            List<ChessCoordinate> targetCoords = possibleEndCoordinates.get(endCoordIdx);
            for (int targetCoordIdx = 0; targetCoordIdx < targetCoords.size(); targetCoordIdx++) {
                ChessCoordinate targetCoord = targetCoords.get(targetCoordIdx);
                Piece targetPiece = board.getPieceOn(targetCoord);

                if (opponentAttackMap.isMarked(targetCoord.getOndDimIndex())) {
                    continue;
                }

                if (targetPiece != null) {
                    // Skip endCoordinates that are occupied by friendly pieces.
                    if (targetPiece.getColor() == movingKing.getColor()) {
                        continue;
                    }

                    moves.add(new Move(movingKingCoord, targetCoord, movingKing,
                            targetCoord, null, targetPiece));
                } else {
                    moves.add(new Move(movingKingCoord, targetCoord, movingKing));
                }
            }
        }

        // Add castling moves
        for (int endCoordIdx = 8; endCoordIdx <= 9; endCoordIdx++) {
            List<ChessCoordinate> targetCoords = possibleEndCoordinates.get(endCoordIdx);
            for (int targetCoordIdx = 0; targetCoordIdx < targetCoords.size(); targetCoordIdx++) {
                ChessCoordinate targetCoord = targetCoords.get(targetCoordIdx);
                Piece targetPiece = board.getPieceOn(targetCoord);

                if (targetPiece != null) {
                    continue;
                }

                boolean canCastle = true;
                if (targetCoord.getFile() == 6) {
                    if (game.canKingSideCastle(movingKing.getColor())) {
                        ChessCoordinate searchCoord = friendlyKingCoord;
                        for (int numChecked = 0; numChecked < 3;
                             numChecked++, searchCoord = Directions.RIGHT.next(searchCoord)) {
                            if (numChecked > 0 && board.getPieceOn(searchCoord) != null
                                    || opponentAttackMap.isMarked(searchCoord.getOndDimIndex())) {
                                canCastle = false;
                                break;
                            }
                        }
                        if (canCastle) {
                            ChessCoordinate rookStart = BoardModel.getChessCoordinate(7, targetCoord.getRank());
                            Piece piece = board.getPieceOn(rookStart);
                            ChessCoordinate endInteracting = BoardModel.getChessCoordinate(5, targetCoord.getRank());
                            moves.add(new Move(movingKingCoord, targetCoord, movingKing, rookStart, endInteracting, piece));
                        }
                    }
                } else {
                    if (game.canQueenSideCastle(movingKing.getColor())) {
                        ChessCoordinate searchCoord = friendlyKingCoord;
                        for (int numChecked = 0; numChecked < 3;
                             numChecked++, searchCoord = Directions.LEFT.next(searchCoord)) {
                            if (numChecked > 0 && board.getPieceOn(searchCoord) != null
                                    || opponentAttackMap.isMarked(searchCoord.getOndDimIndex())) {
                                canCastle = false;
                                break;
                            }
                        }
                        canCastle = canCastle && board.getPieceOn(searchCoord) == null;
                        if (canCastle) {
                            ChessCoordinate rookStart = BoardModel.getChessCoordinate(0, targetCoord.getRank());
                            Piece piece = board.getPieceOn(rookStart);
                            ChessCoordinate endInteracting = BoardModel.getChessCoordinate(3, targetCoord.getRank());
                            moves.add(new Move(movingKingCoord, targetCoord, movingKing, rookStart, endInteracting, piece));
                        }
                    }
                }
            }
        }
    }

    private void generateSlidingMoves() {
        for (ChessCoordinate coordinate : friendlyQueens) {
            Piece piece = game.getBoard().getPieceOn(coordinate);
            generateSlidingPieceMoves(piece, coordinate);
        }
        for (ChessCoordinate coordinate : friendlyRooks) {
            Piece piece = game.getBoard().getPieceOn(coordinate);
            generateSlidingPieceMoves(piece, coordinate);
        }
        for (ChessCoordinate coordinate : friendlyBishops) {
            Piece piece = game.getBoard().getPieceOn(coordinate);
            generateSlidingPieceMoves(piece, coordinate);
        }
    }

    private void generateSlidingPieceMoves(Piece piece, ChessCoordinate coordinate) {
        BoardModel board = game.getBoard();
        ChessCoordinate friendlyKingCoord = game.getTurn() == 'w' ?
                board.getWhiteKingCoord() : board.getBlackKingCoord();
        boolean isPinned = isPinned(coordinate);

        // If pinned and in check, this piece can't move.
        if (!inCheck || !isPinned) {
            for (List<ChessCoordinate> ray : piece.getReachableCoordinateMapFrom(coordinate)) {
                // If we are pinned, then we can only move along pin.
                if ((ray.size() != 0 && (!isPinned || areAligned(coordinate, ray.get(0), friendlyKingCoord)))) {
                    for (ChessCoordinate targetCoord : ray) {
                        Piece targetPiece = board.getPieceOn(targetCoord);
                        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
                            break;
                        }
                        if (!inCheck || checkRayMap.isMarked(targetCoord.getOndDimIndex())) {
                            moves.add(new Move(coordinate, targetCoord, piece, targetCoord, null, targetPiece));
                        }
                        if (targetPiece != null) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private void generateKnightMoves() {
        BoardModel board = game.getBoard();
        for (ChessCoordinate coordinate : friendlyKnights) {
            if (isPinned(coordinate)) {
                continue;
            }
            Piece knight = board.getPieceOn(coordinate);

            for (List<ChessCoordinate> ray : knight.getReachableCoordinateMapFrom(coordinate)) {;
                for (ChessCoordinate targetCoordinate : ray) {
                    Piece targetPiece = board.getPieceOn(targetCoordinate);
                    if ((targetPiece == null || targetPiece.getColor() != knight.getColor())
                            && (!inCheck || checkRayMap.isMarked(targetCoordinate.getOndDimIndex()))) {
                        moves.add(new Move(coordinate, targetCoordinate, knight, targetCoordinate,
                                null, targetPiece));
                    }
                }
            }
        }
    }

    private void generatePawnMoves() {
        BoardModel board = game.getBoard();
        ChessCoordinate friendlyKingCoord = game.getTurn() == 'w' ?
                board.getWhiteKingCoord() : board.getBlackKingCoord();

        for (int coordIdx = 0; coordIdx < friendlyPawns.size(); coordIdx++) {
            ChessCoordinate coordinate = friendlyPawns.get(coordIdx);
            Piece pawn = board.getPieceOn(coordinate);
            List<List<ChessCoordinate>> finalCoordinates = pawn.getReachableCoordinateMapFrom(coordinate);
            boolean isPinned = isPinned(coordinate);

            List<ChessCoordinate> targetCoords = finalCoordinates.get(0);
            for (int targetCoordIdx = 0; targetCoordIdx < targetCoords.size(); targetCoordIdx++) {
                ChessCoordinate targetCoord = targetCoords.get(targetCoordIdx);
                if (!isPinned || areAligned(coordinate, friendlyKingCoord, targetCoord)) {
                    Piece targetPiece = board.getPieceOn(targetCoord);
                    if (targetPiece == null) {
                        if (!inCheck || checkRayMap.isMarked(targetCoord.getOndDimIndex())) {
                            if ((pawn == WHITE_PAWN && targetCoord.getRank() == 7)
                                    || (pawn == BLACK_PAWN && targetCoord.getRank() == 0)) {
                                // Promotion square
                                if (pawn.getColor() == 'w') {
                                    makePromotionMoves(coordinate, targetCoord, pawn, null,
                                            WHITE_QUEEN, WHITE_ROOK, WHITE_BISHOP, WHITE_KNIGHT);
                                } else {
                                    makePromotionMoves(coordinate, targetCoord, pawn, null,
                                            BLACK_QUEEN, BLACK_ROOK, BLACK_BISHOP, BLACK_KNIGHT);
                                }
                            } else {
                                moves.add(new Move(coordinate, targetCoord, pawn));
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
                addAttackingPawnMoves(coordinate, isPinned, captureRight, Directions.RIGHT);
            }

            targetCoords = finalCoordinates.get(2);
            for (int targetCoordIdx = 0; targetCoordIdx < targetCoords.size(); targetCoordIdx++) {
                ChessCoordinate captureLeft = targetCoords.get(targetCoordIdx);
                addAttackingPawnMoves(coordinate, isPinned, captureLeft, Directions.LEFT);
            }
        }
    }

    private void addAttackingPawnMoves(ChessCoordinate coordinate, boolean isPinned, ChessCoordinate captureCoord, Direction left) {
        BoardModel board = game.getBoard();
        Piece pawn = board.getPieceOn(coordinate);
        ChessCoordinate friendlyKingCoord = game.getTurn() == 'w' ?
                board.getWhiteKingCoord() : board.getBlackKingCoord();
        if (isPinned && !areAligned(coordinate, friendlyKingCoord, captureCoord)) {
            return;
        }

        Piece targetPiece = board.getPieceOn(captureCoord);
        if (targetPiece == null) {
            ChessCoordinate enPassantTarget = game.getEnPassantTarget();
            if (enPassantTarget != null && enPassantTarget.equals(captureCoord)) {
                ChessCoordinate targetCoord = left.next(coordinate);
                targetPiece = board.getPieceOn(targetCoord);
                if (!inCheck || checkRayMap.isMarked(captureCoord.getOndDimIndex())
                        || (targetCoord != null && checkRayMap.isMarked(targetCoord.getOndDimIndex()))) {
                    if (targetPiece != null && !areAligned(coordinate, friendlyKingCoord, targetCoord)
                            || !longRangeAttacker(coordinate, targetCoord)) {
                        moves.add(new Move(coordinate, captureCoord, pawn, targetCoord,
                                null, targetPiece));
                    }
                }
            }
        } else {
            if (!inCheck || checkRayMap.isMarked(captureCoord.getOndDimIndex())) {
                if (targetPiece.getColor() != pawn.getColor()) {
                    if (pawn == WHITE_PAWN && captureCoord.getRank() == 7) {
                        makePromotionMoves(coordinate, captureCoord, pawn, targetPiece, WHITE_QUEEN, WHITE_ROOK, WHITE_BISHOP, WHITE_KNIGHT);
                    } else if (pawn == BLACK_PAWN && captureCoord.getRank() == 0) {
                        makePromotionMoves(coordinate, captureCoord, pawn, targetPiece, BLACK_QUEEN, BLACK_ROOK, BLACK_BISHOP, BLACK_KNIGHT);
                    } else {
                        moves.add(new Move(coordinate, captureCoord, pawn, captureCoord,
                                null, targetPiece));
                    }
                }
            }
        }
    }

    private void makePromotionMoves(ChessCoordinate coordinate, ChessCoordinate targetCoord,
                                    Piece pawn, Piece targetPiece, Piece queen, Piece rook,
                                    Piece bishop, Piece knight) {
        moves.add(new Move(coordinate, targetCoord, pawn, targetCoord,
                null, targetPiece, queen));
        moves.add(new Move(coordinate, targetCoord, pawn, targetCoord,
                null, targetPiece, rook));
        moves.add(new Move(coordinate, targetCoord, pawn, targetCoord,
                null, targetPiece, bishop));
        moves.add(new Move(coordinate, targetCoord, pawn, targetCoord,
                null, targetPiece, knight));
    }

    private boolean longRangeAttacker(ChessCoordinate coordinate, ChessCoordinate capturedCoord) {
        BoardModel board = game.getBoard();
        Piece pawn = board.getPieceOn(coordinate);
        ChessCoordinate friendlyKingCoord = game.getTurn() == 'w' ?
                board.getWhiteKingCoord() : board.getBlackKingCoord();

        int xDiff = coordinate.getFile() - friendlyKingCoord.getFile();
        Direction kingToPawn = xDiff > 0 ? Directions.RIGHT : Directions.LEFT;
        boolean longRangeAttackerExists = false;

        for (ChessCoordinate searchCoord = kingToPawn.next(friendlyKingCoord);
             searchCoord != null;
             searchCoord = kingToPawn.next(searchCoord)) {
            Piece targetPiece = board.getPieceOn(searchCoord);

            if (targetPiece == null || searchCoord == coordinate || searchCoord == capturedCoord) {
                continue;
            }

            if (targetPiece.getColor() != game.getTurn()
                    && (targetPiece == WHITE_QUEEN || targetPiece == BLACK_QUEEN) ||
                    (targetPiece == WHITE_ROOK || targetPiece == BLACK_ROOK)) {
                // TODO: BUG? shouldn't bishop be here?
                longRangeAttackerExists = true;
            }
            break;
        }

        return longRangeAttackerExists;
    }

    private boolean isPinned(ChessCoordinate coordinate) {
        return pinsExistInPosition && pinRayMap.isMarked(coordinate.getOndDimIndex());
    }

    public FastMap getOpponentAttackMap() {
        return opponentAttackMap;
    }
}
