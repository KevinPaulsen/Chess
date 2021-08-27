package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
import chess.model.pieces.King;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents the model for a standard chess game. This
 * class is responsible for game rules and managing all the parts of
 * the chess game.
 */
public class GameModel {

    /**
     * Toggle for if check rep should be called.
     */
    private static final boolean DEBUG_MODE = false;

    /**
     * The model for the bard of the chess game.
     */
    private final BoardModel board;

    /**
     * The list of past moves that have occurred in this chess game.
     */
    private final List<Move> moveHistory;

    /**
     * This set of all pieces that need to be updated. This gets cleared and
     * updated at the end of each move.
     */
    private final Set<Piece> piecesToUpdate;

    /**
     * The tracker for which players turn it is to move.
     */
    private char turn;

    /**
     * The flag for when the game is over.
     */
    private boolean isOver;

    /**
     * The tracer for which player has won, this field is uninitialized
     * until a winner has won.
     */
    private char winner;

    /**
     * The default constructor that creates a normal game.
     */
    public GameModel() {
        this.board = ChessBoardFactory.createNormalBoard();
        this.turn = 'w';
        this.moveHistory = new ArrayList<>();
        this.piecesToUpdate = new HashSet<>();
        initPieces();
        checkRep();
    }

    /**
     * TODO: FIX THIS
     *
     * @param gameModel
     */
    public GameModel(GameModel gameModel) {
        this.board = new BoardModel(gameModel.getBoard());
        this.moveHistory = new ArrayList<>(gameModel.moveHistory);
        this.piecesToUpdate = new HashSet<>(gameModel.piecesToUpdate);
        //moveHistory.replaceAll(Move::new);
        this.turn = gameModel.getTurn();
        this.isOver = gameModel.isOver;
        this.winner = gameModel.winner;
        initPieces();
        checkRep();
    }

    /**
     * Initializes all the pieces. This method linearly goes through
     * each piece and ensures all the piece-board data is properly set.
     */
    private void initPieces() {
        for (Piece piece : board.getBlackPieces()) {
            if (piece != null) {
                piece.updateLegalMoves(board, getLastMove());
            }
        }
        for (Piece piece : board.getWhitePieces()) {
            if (piece != null) {
                piece.updateLegalMoves(board, getLastMove());
            }
        }
    }

    /**
     * @return the board model of this game.
     */
    public BoardModel getBoard() {
        return board;
    }

    /**
     * Attempts to make a move given two Coordinates. If the coordinates correspond
     * to a legal move, the move will be made. True is returned if the move was
     * successful, false otherwise.
     *
     * @param startCoordinate the starting coordinate of the moving Piece.
     * @param endCoordinate the ending coordinate of the moving Piece.
     * @return weather or not the move was successful.
     */
    public boolean move(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        boolean didMove = false;

        // Check that moves are both on screen.
        if (startCoordinate != null && endCoordinate != null) {
            Move currentMove = null;
            Piece movingPiece = board.getPieceOn(startCoordinate);
            if (movingPiece != null) {
                for (Move move : movingPiece.getMoves()) {
                    if (startCoordinate.equals(move.getStartingCoordinate()) && endCoordinate.equals(move.getEndingCoordinate())) {
                        currentMove = move;
                        break;
                    }
                }
            }
            didMove = move(currentMove);
        }

        checkRep();
        return didMove;
    }

    /**
     * Attempts to make the given move. If the move is legal, true is returned,
     * otherwise, false is returned.
     *
     * @param move a non-null move.
     * @return true if the move is successful, false otherwise.
     */
    public boolean move(Move move) {
        checkRep();
        boolean didMove = false;

        if (move != null && move.getMovingPiece().getColor() == turn) {
            board.move(move);
            moveHistory.add(move);
            updateAfterMove(move);

            if (legalPosition()) {
                turn = (turn == 'w') ? 'b' : 'w';
                didMove = true;
            } else {
                board.undoMove(move);
                moveHistory.remove(move);
                updateAfterMove(move);
            }
        }

        checkRep();
        return didMove;
    }

    /**
     * Update add and update all the relevant pieces after a move has occurred.
     *
     * @param move the move that just occurred
     */
    private void updateAfterMove(Move move) {
        // Update moving piece beginning and end square
        piecesToUpdate.addAll(getPiecesToAdd(move.getStartingCoordinate()));
        piecesToUpdate.addAll(getPiecesToAdd(move.getEndingCoordinate()));

        // Update interacting piece beginning and end square
        piecesToUpdate.addAll(getPiecesToAdd(move.getInteractingPieceStart()));
        piecesToUpdate.addAll(getPiecesToAdd(move.getInteractingPieceEnd()));

        // Update black and white king square
        piecesToUpdate.add(board.getWhiteKing());
        piecesToUpdate.add(board.getBlackKing());

        // Check if there are any pawns that need to be updated due to blockage
        updateRelevantPawns(move.getStartingCoordinate());
        updateRelevantPawns(move.getEndingCoordinate());

        // Check if a pawn needs to be updated due to EnPassant possibilities
        List<Pawn> pawnUpdate = new ArrayList<>();
        if (move.getMovingPiece() instanceof Pawn) {
            for (Direction direction : Directions.LATERAL.directions) {
                Pawn pawn = checkDirectionForPawn(move.getEndingCoordinate(), direction);
                if (pawn != null) {
                    pawnUpdate.add(pawn);
                }
            }
        }

        // Update each piece, then clear the set of piece to update, then add all possible pawns.
        piecesToUpdate.forEach(piece -> piece.updateLegalMoves(board, getLastMove()));
        piecesToUpdate.clear();
        piecesToUpdate.addAll(pawnUpdate);
    }

    /**
     * Return the set of pieces to update associated with a particular square.
     *
     * @param coordinate the coordinate to get pieces to update from.
     * @return the set of pieces to update.
     */
    private Set<Piece> getPiecesToAdd(ChessCoordinate coordinate) {
        if (coordinate == null) {
            return Set.of();
        }
        Square square = board.getSquare(coordinate);
        Set<Piece> attackers = square.getAttackers();
        if (square.getPiece() != null) {
            attackers.add(square.getPiece());
        }

        return attackers;
    }

    /**
     * Look for a pawn in the given direction. If there is a pawn,
     * add it to pieces to update and return the pawn.
     *
     * @param coordinate the coordinate to look from.
     * @param direction the direction to look in.
     * @return the pawn found or null if no pawn is found.
     */
    private Pawn checkDirectionForPawn(ChessCoordinate coordinate, Direction direction) {
        Piece possiblePawn = board.getPieceOn(direction.next(coordinate));
        if (possiblePawn instanceof Pawn) {
            piecesToUpdate.add(possiblePawn);
            return (Pawn) possiblePawn;
        }
        return null;
    }

    private void updateRelevantPawns(ChessCoordinate coordinate) {
        for (Direction direction : Directions.VERTICAL.directions) {
            checkDirectionForPawn(coordinate, direction);
        }
    }

    /**
     * @return weather the current position is legal to end on. If it is not, then
     *         false is returned. Otherwise true is returned.
     */
    private boolean legalPosition() {
        King relevantKing = (turn == 'w') ? board.getWhiteKing() : board.getBlackKing();
        return !relevantKing.isAttacked(board);
    }

    private void checkGameOver(final boolean isWhitesMove) {
        King relevantKing = isWhitesMove ? board.getWhiteKing() : board.getBlackKing();
        boolean foundMove = false;
        Collection<Piece> relevantPieces = isWhitesMove ? board.getWhitePieces() : board.getBlackPieces();
        for (Piece piece : relevantPieces) {
            if (piece != null && !piece.updateLegalMoves(board, getLastMove()).isEmpty()) {
                foundMove = true;
                break;
            }
        }//*/
        if (foundMove) {
            isOver = false;
            winner = 'N';
        } else {
            isOver = true;
            if (board.getSquare(relevantKing.getCoordinate()).numAttackers(isWhitesMove ? 'b' : 'w') != 0) {
                winner = isWhitesMove ? 'b' : 'w';
            } else {
                winner = 's';
            }
        }
    }

    public void undoMove(Move move) {
        checkRep();
        if (moveHistory.size() > 0 && moveHistory.get(moveHistory.size() - 1).equals(move)) {
            board.undoMove(move);
            moveHistory.remove(moveHistory.size() - 1);
            turn = (turn == 'w') ? 'b' : 'w';
            isOver = false;
            winner = 'N';
        }
        checkRep();
    }

    public List<Move> getLegalMoves(char color) {
        List<Move> result = new ArrayList<>(40);

        Collection<Piece> relevantPieces = color == 'w' ? board.getWhitePieces() : board.getBlackPieces();

        for (Piece piece: relevantPieces) {
            if (piece != null) {
                result.addAll(piece.updateLegalMoves(board, getLastMove()));
            }
        }

        return result;
    }

    public Move cloneMove(Move move) {
        return new Move(move.getEndingCoordinate(), board.getPieceOn(move.getStartingCoordinate()),
                move.getInteractingPieceEnd(), board.getPieceOn(move.getInteractingPieceStart()),
                move.getPromotedPiece());
    }

    public Move getLastMove() {
        return moveHistory.size() == 0 ? null : moveHistory.get(moveHistory.size() - 1);
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public char getTurn() {
        return turn;
    }

    public boolean isOver() {
        return isOver;
    }

    public char getWinner() {
        return winner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameModel)) return false;
        GameModel gameModel = (GameModel) o;
        return turn == gameModel.turn && board.equals(gameModel.board) && moveHistory.equals(gameModel.moveHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, moveHistory, turn);
    }

    private void checkRep() {
        if (DEBUG_MODE) {
            if (board == null || moveHistory == null) {
                throw new RuntimeException("Representation is incorrect.");
            }
        }
    }
}
