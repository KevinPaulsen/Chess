package main.java.model;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.pieces.Piece;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GameModel {

    private final BoardModel board;
    private final Set<Move> legalMoves;
    private final List<Move> moveHistory;

    private char turn;

    public GameModel() {
        this.board = ChessBoardFactory.createNormalBoard();
        this.legalMoves = new HashSet<>();
        this.turn = 'w';
        this.moveHistory = new ArrayList<>();
        updateLegalMoves();
    }

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
            for (Move move : legalMoves) {
                if (startCoordinate.equals(move.getStartingCoordinate()) && endCoordinate.equals(move.getEndingCoordinate())) {
                    currentMove = move;
                    break;
                }
            }
            didMove = move(currentMove);
        }

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
        boolean didMove = false;

        if (move != null && legalMoves.contains(move) && move.getMovingPiece().getColor() == turn) {
            board.move(move);
            moveHistory.add(move);
            turn = (turn == 'w') ? 'b' : 'w';
            updateLegalMoves();
            didMove = true;
        }

        return didMove;
    }

    public boolean undoMove(Move move) {
        boolean didMove = false;

        if (moveHistory.size() > 0 && moveHistory.get(moveHistory.size() - 1).equals(move)) {
            board.undoMove(move);
            moveHistory.remove(moveHistory.size() - 1);
            turn = (turn == 'w') ? 'b' : 'w';
            updateLegalMoves();
            didMove = true;
        }

        return didMove;
    }

    public Move getLastMove() {
        return moveHistory.size() == 0 ? null : moveHistory.get(moveHistory.size() - 1);
    }

    private void updateLegalMoves() {
        legalMoves.clear();
        for (Piece[] file : board.getPieceArray()) {
            for (Piece piece : file) {
                if (piece != null) {
                    legalMoves.addAll(piece.getLegalMoves(this));
                }
            }
        }
    }

    public Set<Move> getLegalMoves() {
        return legalMoves;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public char getTurn() {
        return turn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameModel)) return false;
        GameModel gameModel = (GameModel) o;
        return turn == gameModel.turn && board.equals(gameModel.board) && legalMoves.equals(gameModel.legalMoves) && moveHistory.equals(gameModel.moveHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, legalMoves, moveHistory, turn);
    }
}
