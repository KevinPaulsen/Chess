package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Bishop;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
import chess.model.pieces.King;
import chess.model.pieces.Knight;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import chess.model.pieces.Queen;
import chess.model.pieces.Rook;
import chess.util.FastMap;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for generating moves.
 */
public class MoveGenerator {

    private final GameModel game;
    private final List<Move> moves;
    private boolean inCheck;
    private boolean inDoubleCheck;
    private boolean pinsExistInPosition;

    private final FastMap checkRayMap;
    private final FastMap pinRayMap;
    private final FastMap opponentAttackMap;

    public MoveGenerator(GameModel game) {
        this.game = game;
        this.moves = new ArrayList<>();
        this.inCheck = false;
        this.inDoubleCheck = false;
        this.pinsExistInPosition = false;

        this.checkRayMap = new FastMap();
        this.pinRayMap = new FastMap();
        this.opponentAttackMap = new FastMap();
    }

    public List<Move> generateMoves() {

        calculateAttackData();

        generateKingMoves();

        return moves;
    }

    private void calculateAttackData() {
        FastMap slidingAttackMap = calculateSlidingAttackMap(game);

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
                    opponentAttackMap.markSquare(targetCoord.getBitMask());
                    if (targetCoord.equals(kingCoord)) {
                        inDoubleCheck = inCheck;
                        checkRayMap.markSquare(knight.getCoordinate().getBitMask());
                    }
                }
            }
        }

        // Calculate Pawn Attacks
        for (Pawn pawn : board.getPawns(attackingColor)) {
            for (int rayIdx = 1; rayIdx <= 2; rayIdx++) {
                for (ChessCoordinate targetCoord : pawn.getFinalCoordinates().get(rayIdx)) {
                    opponentAttackMap.markSquare(targetCoord.getBitMask());
                    if (targetCoord.equals(kingCoord)) {
                        inDoubleCheck = inCheck;
                        checkRayMap.markSquare(pawn.getCoordinate().getBitMask());
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
                rayMap.markSquare(coordinate.getBitMask());

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

        for (Queen queen : game.getBoard().getQueens(game.getTurn())) {
            updateSlidingAttackPiece(game.getBoard(), queen, slidingAttackMap);
        }

        for (Rook rook : game.getBoard().getRooks(game.getTurn())) {
            updateSlidingAttackPiece(game.getBoard(), rook, slidingAttackMap);
        }
        for (Bishop bishop : game.getBoard().getBishops(game.getTurn())) {
            updateSlidingAttackPiece(game.getBoard(), bishop, slidingAttackMap);
        }

        return slidingAttackMap;
    }

    /**
     * Updates the given sliding piece's attack map. Adds all the squares this piece
     * can move to to the given map. This will go though the firendly king, and
     * continue the ray past it.
     *
     * @param board the board the piece is on.
     * @param attackingPiece the attacking piece to update.
     * @param map the map to mark.
     */
    private static void updateSlidingAttackPiece(BoardModel board, Piece attackingPiece, FastMap map) {
        for (List<ChessCoordinate> potentialRay : attackingPiece.getFinalCoordinates()) {
            for (ChessCoordinate coordinate : potentialRay) {
                Piece targetPiece = board.getPieceOn(coordinate);
                map.markSquare(coordinate.getBitMask());

                if (targetPiece != null && !(targetPiece instanceof King
                        && targetPiece.getColor() != attackingPiece.getColor())) {
                    break;
                }
            }
        }
    }

    /*

    squareInCheckRay(coord)
    squareIsAttacked(coord)

     */

    private void generateKingMoves() {

        BoardModel board = game.getBoard();
        King movingKing = game.getTurn() == 'w' ? board.getWhiteKing() : board.getBlackKing();
        List<List<ChessCoordinate>> possibleEndCoordinates = movingKing.getFinalCoordinates();

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

        for (int endCoordIdx = 8; endCoordIdx <= 9; endCoordIdx++) {
            for (ChessCoordinate targetCoord : possibleEndCoordinates.get(endCoordIdx)) {
                Piece targetPiece = board.getPieceOn(targetCoord);

                if (targetPiece != null) {
                    continue;
                }

                if (targetCoord.getFile() == 6) {
                    if (game.canKingSideCastle(movingKing.getColor())) {
                        ChessCoordinate searchCoord = targetCoord;
                        for (int numChecked = 0; numChecked < 3;
                             numChecked++, searchCoord = Directions.RIGHT.next(targetCoord)) {
                            if (board.getPieceOn(searchCoord) != null
                                    || opponentAttackMap.isMarked(searchCoord.getOndDimIndex())) {
                                break;
                            }
                        }
                        Piece piece = board.getPieceOn(BoardModel.getChessCoordinate(7, targetCoord.getRank()));
                        ChessCoordinate endInteracting = BoardModel.getChessCoordinate(5, targetCoord.getRank());
                        moves.add(new Move(targetCoord, movingKing, endInteracting, piece));
                    }
                } else {
                    if (game.canQueenSideCastle(movingKing.getColor())) {
                        ChessCoordinate searchCoord = targetCoord;
                        for (int numChecked = 0; numChecked < 3;
                             numChecked++, searchCoord = Directions.LEFT.next(targetCoord)) {
                            if (board.getPieceOn(searchCoord) != null
                                    || opponentAttackMap.isMarked(searchCoord.getOndDimIndex())) {
                                break;
                            }
                        }
                        Piece piece = board.getPieceOn(BoardModel.getChessCoordinate(0, targetCoord.getRank()));
                        ChessCoordinate endInteracting = BoardModel.getChessCoordinate(3, targetCoord.getRank());
                        moves.add(new Move(targetCoord, movingKing, endInteracting, piece));
                    }
                }
            }
        }
    }

    private static List<List<ChessCoordinate>> getReachableCoords(
            List<List<ChessCoordinate>>[][] reachableCoordinatesMap, ChessCoordinate coordinate) {
        return reachableCoordinatesMap[coordinate.getFile()][coordinate.getRank()];
    }

}
