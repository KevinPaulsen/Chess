package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Bishop;
import chess.model.pieces.BlackPawn;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
import chess.model.pieces.King;
import chess.model.pieces.Knight;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import chess.model.pieces.Queen;
import chess.model.pieces.Rook;
import chess.model.pieces.WhitePawn;
import chess.util.FastMap;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for generating moves.
 */
public class MoveGenerator {

    private final GameModel game;
    private final List<Move> moves;
    private final King friendlyKing;
    private final FastMap checkRayMap;
    private final FastMap pinRayMap;
    private final FastMap opponentAttackMap;
    private boolean inCheck;
    private boolean inDoubleCheck;
    private boolean pinsExistInPosition;

    public MoveGenerator(GameModel game) {
        this.game = game;
        this.moves = new ArrayList<>();
        this.inCheck = false;
        this.inDoubleCheck = false;
        this.pinsExistInPosition = false;

        this.friendlyKing = game.getTurn() == 'w' ? game.getBoard().getWhiteKing() : game.getBoard().getBlackKing();

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
     * @param map            the map to mark.
     */
    private static void updateSlidingAttackPiece(BoardModel board, Piece attackingPiece, FastMap map) {
        for (List<ChessCoordinate> potentialRay : attackingPiece.getFinalCoordinates()) {
            for (ChessCoordinate coordinate : potentialRay) {
                Piece targetPiece = board.getPieceOn(coordinate);
                map.mergeMask(coordinate.getBitMask());

                if (targetPiece != null && !(targetPiece instanceof King
                        && targetPiece.getColor() != attackingPiece.getColor())) {
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

    private static List<List<ChessCoordinate>> getReachableCoords(
            List<List<ChessCoordinate>>[][] reachableCoordinatesMap, ChessCoordinate coordinate) {
        return reachableCoordinatesMap[coordinate.getFile()][coordinate.getRank()];
    }

    public List<Move> generateMoves() {
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

    private void calculateAttackData() {
        opponentAttackMap.merge(calculateSlidingAttackMap(game));

        BoardModel board = game.getBoard();
        char turn = game.getTurn();
        char attackingColor = game.getTurn() == 'w' ? 'b' : 'w';
        ChessCoordinate kingCoord = turn == 'w' ? board.getWhiteKing().getCoordinate()
                : board.getBlackKing().getCoordinate();

        // Calculate the pin and check rays
        if (board.getRooks(attackingColor).size() > 0 || board.getQueens(attackingColor).size() > 0) {
            calculatePinsAndCheckRays(board, getReachableCoords(Rook.REACHABLE_COORDINATES_MAP, kingCoord),
                    false, turn);
        }
        if (board.getBishops(attackingColor).size() > 0 || board.getQueens(attackingColor).size() > 0) {
            calculatePinsAndCheckRays(board, getReachableCoords(Bishop.REACHABLE_COORDINATES_MAP, kingCoord),
                    true, turn);
        }

        // Calculate Knight Attacks
        for (Knight knight : board.getKnights(attackingColor)) {
            for (List<ChessCoordinate> ray : knight.getFinalCoordinates()) {
                for (ChessCoordinate targetCoord : ray) {
                    opponentAttackMap.mergeMask(targetCoord.getBitMask());
                    if (targetCoord.equals(kingCoord)) {
                        inDoubleCheck = inCheck;
                        inCheck = true;
                        checkRayMap.mergeMask(knight.getCoordinate().getBitMask());
                    }
                }
            }
        }

        // Calculate Pawn Attacks
        for (Pawn pawn : board.getPawns(attackingColor)) {
            for (int rayIdx = 1; rayIdx <= 2; rayIdx++) {
                for (ChessCoordinate targetCoord : pawn.getFinalCoordinates().get(rayIdx)) {
                    opponentAttackMap.mergeMask(targetCoord.getBitMask());
                    if (targetCoord.equals(kingCoord)) {
                        inDoubleCheck = inCheck;
                        inCheck = true;
                        checkRayMap.mergeMask(pawn.getCoordinate().getBitMask());
                    }
                }
            }
        }
    }

    private void calculatePinsAndCheckRays(BoardModel board, List<List<ChessCoordinate>> raysToCheck,
                                           boolean isDiagonal, char turn) {
        for (List<ChessCoordinate> ray : raysToCheck) {
            if (inDoubleCheck) {
                break;
            }

            boolean friendlyRay = false;
            FastMap rayMap = new FastMap();

            for (ChessCoordinate coordinate : ray) {
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
                        if (targetPiece instanceof Queen
                                || (isDiagonal && targetPiece instanceof Bishop)
                                || (!isDiagonal && targetPiece instanceof Rook)) {
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
        char attackingColor = game.getTurn() == 'w' ? 'b' : 'w';

        for (Queen queen : game.getBoard().getQueens(attackingColor)) {
            updateSlidingAttackPiece(game.getBoard(), queen, slidingAttackMap);
        }

        for (Rook rook : game.getBoard().getRooks(attackingColor)) {
            updateSlidingAttackPiece(game.getBoard(), rook, slidingAttackMap);
        }
        for (Bishop bishop : game.getBoard().getBishops(attackingColor)) {
            updateSlidingAttackPiece(game.getBoard(), bishop, slidingAttackMap);
        }

        return slidingAttackMap;
    }

    private void generateKingMoves() {
        BoardModel board = game.getBoard();
        King movingKing = game.getTurn() == 'w' ? board.getWhiteKing() : board.getBlackKing();
        List<List<ChessCoordinate>> possibleEndCoordinates = movingKing.getFinalCoordinates();

        // Add moves for the regular king moves
        for (int endCoordIdx = 0; endCoordIdx < 8; endCoordIdx++) {
            for (ChessCoordinate targetCoord : possibleEndCoordinates.get(endCoordIdx)) {
                Piece targetPiece = board.getPieceOn(targetCoord);

                if (opponentAttackMap.isMarked(targetCoord.getOndDimIndex())) {
                    continue;
                }

                if (targetPiece != null) {
                    // Skip endCoordinates that are occupied by friendly pieces.
                    if (targetPiece.getColor() == movingKing.getColor()) {
                        continue;
                    }

                    moves.add(new Move(targetCoord, movingKing, null, targetPiece));
                } else {
                    moves.add(new Move(targetCoord, movingKing));
                }
            }
        }

        // Add castling moves
        for (int endCoordIdx = 8; endCoordIdx <= 9; endCoordIdx++) {
            for (ChessCoordinate targetCoord : possibleEndCoordinates.get(endCoordIdx)) {
                Piece targetPiece = board.getPieceOn(targetCoord);

                if (targetPiece != null) {
                    continue;
                }

                boolean canCastle = true;
                if (targetCoord.getFile() == 6) {
                    if (game.canKingSideCastle(movingKing.getColor())) {
                        ChessCoordinate searchCoord = friendlyKing.getCoordinate();
                        for (int numChecked = 0; numChecked < 3;
                             numChecked++, searchCoord = Directions.RIGHT.next(searchCoord)) {
                            if (numChecked > 0 && board.getPieceOn(searchCoord) != null
                                    || opponentAttackMap.isMarked(searchCoord.getOndDimIndex())) {
                                canCastle = false;
                                break;
                            }
                        }
                        if (canCastle) {
                            Piece piece = board.getPieceOn(BoardModel.getChessCoordinate(7, targetCoord.getRank()));
                            ChessCoordinate endInteracting = BoardModel.getChessCoordinate(5, targetCoord.getRank());
                            moves.add(new Move(targetCoord, movingKing, endInteracting, piece));
                        }
                    }
                } else {
                    if (game.canQueenSideCastle(movingKing.getColor())) {
                        ChessCoordinate searchCoord = friendlyKing.getCoordinate();
                        for (int numChecked = 0; numChecked < 3;
                             numChecked++, searchCoord = Directions.LEFT.next(searchCoord)) {
                            if (board.getPieceOn(searchCoord) != null
                                    || opponentAttackMap.isMarked(searchCoord.getOndDimIndex())) {
                                canCastle = false;
                                break;
                            }
                        }
                        if (canCastle) {
                            Piece piece = board.getPieceOn(BoardModel.getChessCoordinate(0, targetCoord.getRank()));
                            ChessCoordinate endInteracting = BoardModel.getChessCoordinate(3, targetCoord.getRank());
                            moves.add(new Move(targetCoord, movingKing, endInteracting, piece));
                        }
                    }
                }
            }
        }
    }

    private void generateSlidingMoves() {
        for (Piece piece : game.getBoard().getQueens(game.getTurn())) {
            generateSlidingPieceMoves(piece);
        }
        for (Piece piece : game.getBoard().getRooks(game.getTurn())) {
            generateSlidingPieceMoves(piece);
        }
        for (Piece piece : game.getBoard().getBishops(game.getTurn())) {
            generateSlidingPieceMoves(piece);
        }
    }

    private void generateSlidingPieceMoves(Piece piece) {
        BoardModel board = game.getBoard();
        boolean isPinned = isPinned(piece.getCoordinate());

        // If pinned and in check, this piece can't move.
        if (!inCheck || !isPinned) {
            for (List<ChessCoordinate> ray : piece.getFinalCoordinates()) {
                // If we are pinned, then we can only move along pin.
                if ((ray.size() != 0 && (!isPinned || areAligned(piece.getCoordinate(), ray.get(0), friendlyKing.getCoordinate())))) {
                    for (ChessCoordinate targetCoord : ray) {
                        Piece targetPiece = board.getPieceOn(targetCoord);
                        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
                            break;
                        }
                        if (!inCheck || checkRayMap.isMarked(targetCoord.getOndDimIndex())) {
                            moves.add(new Move(targetCoord, piece, null, targetPiece));
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
        for (Knight knight : board.getKnights(game.getTurn())) {
            if (isPinned(knight.getCoordinate())) {
                continue;
            }

            for (List<ChessCoordinate> ray : knight.getFinalCoordinates()) {
                for (ChessCoordinate targetCoordinate : ray) {
                    Piece targetPiece = board.getPieceOn(targetCoordinate);
                    if ((targetPiece == null || targetPiece.getColor() != knight.getColor())
                            && (!inCheck || checkRayMap.isMarked(targetCoordinate.getOndDimIndex()))) {
                        moves.add(new Move(targetCoordinate, knight, null, targetPiece));
                    }
                }
            }
        }
    }

    private void generatePawnMoves() {
        BoardModel board = game.getBoard();

        for (Pawn pawn : board.getPawns(game.getTurn())) {
            List<List<ChessCoordinate>> finalCoordinates = pawn.getFinalCoordinates();
            boolean isPinned = isPinned(pawn.getCoordinate());

            for (ChessCoordinate targetCoord : finalCoordinates.get(0)) {
                if (!isPinned || areAligned(pawn.getCoordinate(), friendlyKing.getCoordinate(), targetCoord)) {
                    Piece targetPiece = board.getPieceOn(targetCoord);
                    if (targetPiece == null) {
                        if (!inCheck || checkRayMap.isMarked(targetCoord.getOndDimIndex())) {
                            if ((pawn instanceof WhitePawn && targetCoord.getRank() == 7)
                                    || (pawn instanceof BlackPawn && targetCoord.getRank() == 0)) {
                                // Promotion square
                                moves.add(new Move(targetCoord, pawn, new Queen(pawn)));
                                moves.add(new Move(targetCoord, pawn, new Rook(pawn)));
                                moves.add(new Move(targetCoord, pawn, new Bishop(pawn)));
                                moves.add(new Move(targetCoord, pawn, new Knight(pawn)));
                            } else {
                                moves.add(new Move(targetCoord, pawn));
                            }
                        }
                    } else {
                        break;
                    }
                }
            }

            for (ChessCoordinate captureRight : finalCoordinates.get(1)) {
                addAttackingPawnMoves(pawn, isPinned, captureRight, Directions.RIGHT);
            }

            for (ChessCoordinate captureLeft : finalCoordinates.get(2)) {
                addAttackingPawnMoves(pawn, isPinned, captureLeft, Directions.LEFT);
            }
        }
    }

    private void addAttackingPawnMoves(Pawn pawn, boolean isPinned, ChessCoordinate captureCoord, Direction left) {
        BoardModel board = game.getBoard();
        if (isPinned && !areAligned(pawn.getCoordinate(), friendlyKing.getCoordinate(), captureCoord)) {
            return;
        }

        Piece targetPiece = board.getPieceOn(captureCoord);
        if (targetPiece == null) {
            ChessCoordinate enPassantTarget = game.getEnPassantTarget();
            if (enPassantTarget != null && enPassantTarget.equals(captureCoord)) {
                if (!inCheck || checkRayMap.isMarked(captureCoord.getOndDimIndex())) {
                    // TODO: Deal with enPassant causes check
                    targetPiece = board.getPieceOn(left.next(pawn.getCoordinate()));
                    moves.add(new Move(captureCoord, pawn, null, targetPiece));
                }
                // TODO: Deal with enPassant discovery
            }
        } else {
            if (!inCheck || checkRayMap.isMarked(captureCoord.getOndDimIndex())) {
                if (targetPiece.getColor() != pawn.getColor()) {
                    if ((pawn instanceof WhitePawn && captureCoord.getRank() == 7)
                            || (pawn instanceof BlackPawn && captureCoord.getRank() == 0)) {
                        moves.add(new Move(captureCoord, pawn, null, targetPiece, new Queen(pawn)));
                        moves.add(new Move(captureCoord, pawn, null, targetPiece, new Rook(pawn)));
                        moves.add(new Move(captureCoord, pawn, null, targetPiece, new Bishop(pawn)));
                        moves.add(new Move(captureCoord, pawn, null, targetPiece, new Knight(pawn)));
                    } else {
                        moves.add(new Move(captureCoord, pawn, null, targetPiece));
                    }
                }
            }
        }
    }

    private boolean isPinned(ChessCoordinate coordinate) {
        return pinsExistInPosition && pinRayMap.isMarked(coordinate.getOndDimIndex());
    }
}
