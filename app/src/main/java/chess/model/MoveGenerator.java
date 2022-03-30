package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static chess.ChessCoordinate.*;
import static chess.model.pieces.Piece.*;

/**
 * This class is responsible for generating moves.
 */
public class MoveGenerator {

    private final GameModel game;
    private final BoardModel board;
    private final FastMap checkRayMap;
    private final FastMap pinRayMap;
    private final FastMap opponentAttackMap;
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

    public MoveGenerator(GameModel game) {
        this.game = game;
        this.board = game.getBoard();
        this.moves = new ArrayList<>(150);
        this.inCheck = false;
        this.inDoubleCheck = false;
        this.pinsExistInPosition = false;

        this.checkRayMap = new FastMap();
        this.pinRayMap = new FastMap();
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
                Piece targetPiece = board.getPieceOn(potentialCoordinate);
                map.mergeMask(potentialCoordinate.getBitMask());

                if (targetPiece != null && (targetPiece.getColor() == attackingPiece.getColor()
                        || targetPiece != WHITE_KING && targetPiece != BLACK_KING)) {
                    break;
                }//*/
            }
        }
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

        this.checkRayMap.clear();
        this.pinRayMap.clear();
        this.opponentAttackMap.clear();

        int friendlyColor = game.getTurn();

        if (friendlyColor == GameModel.WHITE) {
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
        resetState();
        calculateAttackData();

        generateKingMoves();

        if (inDoubleCheck) {
            return moves;
        }

        generateSlidingMoves();
        generateKnightMoves();
        generatePawnMoves();//*/

        return moves;
    }

    private void calculateAttackData() {
        opponentAttackMap.merge(calculateSlidingAttackMap(game));

        char turn = game.getTurn();

        // Calculate the pin and check rays
        if (attackingRooks().size() > 0 || attackingQueens().size() > 0) {
            calculatePinsAndCheckRays(board, WHITE_ROOK.getReachableCoordinateMapFrom(friendlyKingCoord),
                    false, turn);
        }
        if (attackingBishops().size() > 0 || attackingQueens().size() > 0) {
            calculatePinsAndCheckRays(board, WHITE_BISHOP.getReachableCoordinateMapFrom(friendlyKingCoord),
                    true, turn);
        }

        // Calculate Knight Attacks
        for (ChessCoordinate knightCoord : attackingKnights()) {
            List<List<ChessCoordinate>> reachableCoordinates = WHITE_KNIGHT.getReachableCoordinateMapFrom(knightCoord);
            for (int rayIdx = 0; rayIdx < reachableCoordinates.size(); rayIdx++) {
                updateAttackingSquares(friendlyKingCoord, knightCoord, reachableCoordinates, rayIdx);
            }
        }

        // Calculate Pawn Attacks
        for (ChessCoordinate pawnCoord : attackingPawns()) {
            Piece pawn = board.getPieceOn(pawnCoord);
            List<List<ChessCoordinate>> reachableCoordinates = pawn.getReachableCoordinateMapFrom(pawnCoord);
            for (int rayIdx = 1; rayIdx <= 2; rayIdx++) {
                updateAttackingSquares(friendlyKingCoord, pawnCoord, reachableCoordinates, rayIdx);
            }
        }

        Piece opponentKing = board.getPieceOn(attackingKingSquare);

        // Calculate King Attacks
        List<List<ChessCoordinate>> reachableCoordinates = opponentKing.getReachableCoordinateMapFrom(attackingKingSquare);
        for (int rayIdx = 0; rayIdx < 8; rayIdx++) {
            updateAttackingSquares(friendlyKingCoord, attackingKingSquare, reachableCoordinates, rayIdx);
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

        for (ChessCoordinate queenCoord : attackingQueens()) {
            Piece queen = board.getPieceOn(queenCoord);
            updateSlidingAttackPiece(board, queen, queenCoord, slidingAttackMap);
        }

        for (ChessCoordinate rookCoordinate : attackingRooks()) {
            Piece rook = board.getPieceOn(rookCoordinate);
            updateSlidingAttackPiece(board, rook, rookCoordinate, slidingAttackMap);
        }

        for (ChessCoordinate bishopCoordinate : attackingBishops()) {
            Piece bishop = board.getPieceOn(bishopCoordinate);
            updateSlidingAttackPiece(board, bishop, bishopCoordinate, slidingAttackMap);
        }

        return slidingAttackMap;
    }

    private void generateKingMoves() {
        Piece movingKing = board.getPieceOn(friendlyKingCoord);
        List<List<ChessCoordinate>> possibleEndCoordinates = movingKing.getReachableCoordinateMapFrom(friendlyKingCoord);

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

                    moves.add(new Move(friendlyKingCoord, targetCoord, movingKing,
                            targetCoord, null, targetPiece));
                } else {
                    moves.add(new Move(friendlyKingCoord, targetCoord, movingKing));
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
                            makeCastleMove(board, friendlyKingCoord, movingKing, targetCoord, H1, H8, F1, F8);
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
                            makeCastleMove(board, friendlyKingCoord, movingKing, targetCoord, A1, A8, D1, D8);
                        }
                    }
                }
            }
        }
    }

    private void makeCastleMove(BoardModel board, ChessCoordinate movingKingCoord, Piece movingKing,
                                ChessCoordinate kingEndCoord, ChessCoordinate whiteRookStart,
                                ChessCoordinate blackRookStart, ChessCoordinate whiteRookEnd,
                                ChessCoordinate blackRookEnd) {
        ChessCoordinate rookStart = kingEndCoord.getRank() == 0 ? whiteRookStart : blackRookStart;
        ChessCoordinate endRookCoord = kingEndCoord.getRank() == 0 ? whiteRookEnd : blackRookEnd;
        Piece piece = board.getPieceOn(rookStart);
        moves.add(new Move(movingKingCoord, kingEndCoord, movingKing, rookStart, endRookCoord, piece));
    }

    private void generateSlidingMoves() {
        for (ChessCoordinate coordinate : friendlyQueens()) {
            Piece piece = board.getPieceOn(coordinate);
            generateSlidingPieceMoves(piece, coordinate);
        }
        for (ChessCoordinate coordinate : friendlyRooks()) {
            Piece piece = board.getPieceOn(coordinate);
            generateSlidingPieceMoves(piece, coordinate);
        }
        for (ChessCoordinate coordinate : friendlyBishops()) {
            Piece piece = board.getPieceOn(coordinate);
            generateSlidingPieceMoves(piece, coordinate);
        }
    }

    private void generateSlidingPieceMoves(Piece piece, ChessCoordinate coordinate) {
        boolean isPinned = isPinned(coordinate);

        // If pinned and in check, this piece can't move.
        if (!inCheck || !isPinned) {
            List<List<ChessCoordinate>> rays = piece.getReachableCoordinateMapFrom(coordinate);
            for (int rayIdx = 0; rayIdx < rays.size(); rayIdx++) {
                List<ChessCoordinate> ray = rays.get(rayIdx);
                // If we are pinned, then we can only move along pin.
                if ((ray.size() != 0 && (!isPinned || areAligned(coordinate, ray.get(0), friendlyKingCoord)))) {
                    for (int coordIdx = 0; coordIdx < ray.size(); coordIdx++) {
                        ChessCoordinate targetCoord = ray.get(coordIdx);
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
        for (ChessCoordinate coordinate : friendlyKnights()) {
            if (isPinned(coordinate)) {
                continue;
            }
            Piece knight = board.getPieceOn(coordinate);

            List<List<ChessCoordinate>> rays = knight.getReachableCoordinateMapFrom(coordinate);
            for (int rayIdx = 0; rayIdx < rays.size(); rayIdx++) {
                List<ChessCoordinate> ray = rays.get(rayIdx);
                for (int coordIdx = 0; coordIdx < ray.size(); coordIdx++) {
                    ChessCoordinate targetCoordinate = ray.get(coordIdx);
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
        for (ChessCoordinate coordinate : friendlyPawns()) {
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

    private void addAttackingPawnMoves(ChessCoordinate coordinate, boolean isPinned, ChessCoordinate captureCoord, Direction direction) {
        Piece pawn = board.getPieceOn(coordinate);

        if (isPinned && !areAligned(coordinate, friendlyKingCoord, captureCoord)) {
            return;
        }

        Piece targetPiece = board.getPieceOn(captureCoord);
        if (targetPiece == null) {
            // Must try for enPassant
            ChessCoordinate enPassantTarget = game.getEnPassantTarget();
            if (Objects.equals(enPassantTarget, captureCoord)) {
                ChessCoordinate targetCoord = direction.next(coordinate);

                targetPiece = board.getPieceOn(targetCoord);
                if ((!inCheck // Proceed if not in check
                        || checkRayMap.isMarked(captureCoord.getOndDimIndex())   // Proceed if move blocks check
                        || checkRayMap.isMarked(targetCoord.getOndDimIndex()))   // Proceed if move captures attacker
                        && (targetPiece != null // Ensure that the targetPiece is a pawn
                        && (!areAligned(coordinate, friendlyKingCoord, targetCoord) // Proceed if not aligned with king
                        || !longRangeAttacker(coordinate, targetCoord)))) { // Proceed if not attacker
                    moves.add(new Move(coordinate, captureCoord, pawn, targetCoord, null, targetPiece));
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
                    && ((targetPiece == WHITE_QUEEN || targetPiece == BLACK_QUEEN) ||
                    (targetPiece == WHITE_ROOK || targetPiece == BLACK_ROOK))) {
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

    private Set<ChessCoordinate> friendlyPawns() {
        return board.getLocations(friendlyPawn);
    }

    private Set<ChessCoordinate> friendlyKnights() {
        return board.getLocations(friendlyKnight);
    }

    private Set<ChessCoordinate> friendlyBishops() {
        return board.getLocations(friendlyBishop);
    }

    private Set<ChessCoordinate> friendlyRooks() {
        return board.getLocations(friendlyRook);
    }

    private Set<ChessCoordinate> friendlyQueens() {
        return board.getLocations(friendlyQueen);
    }

    private Set<ChessCoordinate> attackingPawns() {
        return board.getLocations(attackingPawn);
    }

    private Set<ChessCoordinate> attackingKnights() {
        return board.getLocations(attackingKnight);
    }

    private Set<ChessCoordinate> attackingBishops() {
        return board.getLocations(attackingBishop);
    }

    private Set<ChessCoordinate> attackingRooks() {
        return board.getLocations(attackingRook);
    }

    private Set<ChessCoordinate> attackingQueens() {
        return board.getLocations(attackingQueen);
    }
}
