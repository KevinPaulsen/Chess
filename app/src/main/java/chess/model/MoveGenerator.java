package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static chess.ChessCoordinate.*;
import static chess.model.GameModel.BLACK;
import static chess.model.GameModel.WHITE;
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

    private final Map<Long, List<Move>> cachedPositions;

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

    private void resetState() {
        this.moves = new ArrayList<>(150);
        this.inCheck = false;
        this.inDoubleCheck = false;
        this.pinsExistInPosition = false;

        this.checkRayMap.clear();
        this.pinRayMap.clear();
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
                generateSlidingMoves();
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
                                           boolean isDiagonal) {
        for (int rayIdx = 0; rayIdx < raysToCheck.size(); rayIdx++) {
            List<ChessCoordinate> ray = raysToCheck.get(rayIdx);
            if (inDoubleCheck) {
                break;
            }

            boolean friendlyRay = false;
            FastMap rayMap = new FastMap();

            for (int coordIdx = 0; coordIdx < ray.size(); coordIdx++) {
                ChessCoordinate coordinate = ray.get(coordIdx);
                rayMap.mergeMask(coordinate.getBitMask());

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

                    moves.add(new Move(friendlyKingCoord, targetCoord, movingKing, targetCoord, null));
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

                if (board.isOccupied(targetCoord)) continue;

                boolean canCastle = true;
                if (targetCoord.getFile() == 6) {
                    if (game.canKingSideCastle(movingKing.getColor())) {
                        ChessCoordinate searchCoord = friendlyKingCoord;
                        for (int numChecked = 0; numChecked < 3;
                             numChecked++, searchCoord = Directions.RIGHT.next(searchCoord)) {
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
                             numChecked++, searchCoord = Directions.LEFT.next(searchCoord)) {
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
        moves.add(new Move(movingKingCoord, kingEndCoord, movingKing, rookStart, endRookCoord));
    }

    private void generateSlidingMoves() {
        Piece piece = friendlyColor == WHITE ? WHITE_QUEEN : BLACK_QUEEN;
        for (ChessCoordinate coordinate : friendlyQueens()) {
            generateSlidingPieceMoves(piece, coordinate);
        }

        piece = friendlyColor == WHITE ? WHITE_ROOK : BLACK_ROOK;
        for (ChessCoordinate coordinate : friendlyRooks()) {
            generateSlidingPieceMoves(piece, coordinate);
        }

        piece = friendlyColor == WHITE ? WHITE_BISHOP : BLACK_BISHOP;
        for (ChessCoordinate coordinate : friendlyBishops()) {
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
                        if (board.isOccupiedByColor(targetCoord, piece.getColor())) break;

                        if (!inCheck || checkRayMap.isMarked(targetCoord.getOndDimIndex())) {
                            if (board.isOccupied(targetCoord)) {
                                moves.add(new Move(coordinate, targetCoord, piece, targetCoord, null));
                                break;
                            } else {
                                moves.add(new Move(coordinate, targetCoord, piece));
                            }
                        }

                        if (board.isOccupied(targetCoord)) break;
                    }
                }
            }
        }
    }

    private void generateKnightMoves() {
        Piece knight = friendlyColor == WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
        for (ChessCoordinate coordinate : friendlyKnights()) {
            if (isPinned(coordinate)) {
                continue;
            }

            List<List<ChessCoordinate>> rays = knight.getReachableCoordinateMapFrom(coordinate);
            for (int rayIdx = 0; rayIdx < rays.size(); rayIdx++) {
                List<ChessCoordinate> ray = rays.get(rayIdx);
                for (int coordIdx = 0; coordIdx < ray.size(); coordIdx++) {
                    ChessCoordinate targetCoordinate = ray.get(coordIdx);
                    if (!board.isOccupiedByColor(targetCoordinate, friendlyColor)
                            && (!inCheck || checkRayMap.isMarked(targetCoordinate.getOndDimIndex()))) {
                        if (board.isOccupied(targetCoordinate)) {
                            moves.add(new Move(coordinate, targetCoordinate, knight, targetCoordinate, null));
                        } else {
                            moves.add(new Move(coordinate, targetCoordinate, knight));
                        }
                    }
                }
            }
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
                        if (!inCheck || checkRayMap.isMarked(targetCoord.getOndDimIndex())) {
                            if ((pawn == WHITE_PAWN && targetCoord.getRank() == 7)
                                    || (pawn == BLACK_PAWN && targetCoord.getRank() == 0)) {
                                // Promotion square
                                if (pawn.getColor() == WHITE) {
                                    makePromotionMoves(coordinate, targetCoord, null, pawn,
                                            WHITE_QUEEN, WHITE_ROOK, WHITE_BISHOP, WHITE_KNIGHT);
                                } else {
                                    makePromotionMoves(coordinate, targetCoord, null, pawn,
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
                        || checkRayMap.isMarked(captureCoord.getOndDimIndex())   // Proceed if move blocks check
                        || checkRayMap.isMarked(targetCoord.getOndDimIndex()))   // Proceed if move captures attacker
                        && (board.isOccupied(targetCoord) // Ensure that the targetPiece is a pawn
                        && (!areAligned(coordinate, friendlyKingCoord, targetCoord) // Proceed if not aligned with king
                        || !longRangeAttacker(coordinate, targetCoord)))) { // Proceed if not attacker
                    moves.add(new Move(coordinate, captureCoord, pawn, targetCoord, null));
                }
            }
        } else {
            if (!inCheck || checkRayMap.isMarked(captureCoord.getOndDimIndex())) {
                if (board.isOccupiedByColor(captureCoord, attackingColor)) {
                    if (pawn == WHITE_PAWN && captureCoord.getRank() == 7) {
                        makePromotionMoves(coordinate, captureCoord, captureCoord, pawn, WHITE_QUEEN, WHITE_ROOK, WHITE_BISHOP, WHITE_KNIGHT);
                    } else if (pawn == BLACK_PAWN && captureCoord.getRank() == 0) {
                        makePromotionMoves(coordinate, captureCoord, captureCoord, pawn, BLACK_QUEEN, BLACK_ROOK, BLACK_BISHOP, BLACK_KNIGHT);
                    } else {
                        moves.add(new Move(coordinate, captureCoord, pawn, captureCoord, null));
                    }
                }
            }
        }
    }

    private void makePromotionMoves(ChessCoordinate coordinate, ChessCoordinate targetCoord,
                                    ChessCoordinate interactingPieceStart,
                                    Piece pawn, Piece queen, Piece rook,
                                    Piece bishop, Piece knight) {
        moves.add(new Move(coordinate, targetCoord, pawn, interactingPieceStart, null, queen));
        moves.add(new Move(coordinate, targetCoord, pawn, interactingPieceStart, null, rook));
        moves.add(new Move(coordinate, targetCoord, pawn, interactingPieceStart, null, bishop));
        moves.add(new Move(coordinate, targetCoord, pawn, interactingPieceStart, null, knight));
    }

    private boolean longRangeAttacker(ChessCoordinate coordinate, ChessCoordinate capturedCoord) {
        int xDiff = coordinate.getFile() - friendlyKingCoord.getFile();
        Direction kingToPawn = xDiff > 0 ? Directions.RIGHT : Directions.LEFT;
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
        return pinsExistInPosition && pinRayMap.isMarked(coordinate.getOndDimIndex());
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
}
