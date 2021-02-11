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
    private final List<Move> moveHistory;
    private final List<Integer> pastPositionHashes;

    private char turn;

    public GameModel() {
        this.board = ChessBoardFactory.createNormalBoard();
        this.turn = 'w';
        this.moveHistory = new ArrayList<>();
        this.pastPositionHashes = new ArrayList<>(List.of(hashCode()));
        checkRep();
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
        checkRep();
        boolean didMove = false;

        // Check that moves are both on screen.
        if (startCoordinate != null && endCoordinate != null) {
            Move currentMove = null;
            Piece movingPiece = board.getPieceOn(startCoordinate);
            if (movingPiece != null) {
                for (Move move : movingPiece.getLegalMoves(this)) {
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

        if (move != null && move.getMovingPiece().getLegalMoves(this).contains(move) && move.getMovingPiece().getColor() == turn) {
            board.move(move);
            moveHistory.add(move);
            turn = (turn == 'w') ? 'b' : 'w';
            pastPositionHashes.add(hashCode());
            didMove = true;
        }

        checkRep();
        return didMove;
    }

    public void undoMove(Move move) {
        checkRep();
        if (moveHistory.size() > 0 && moveHistory.get(moveHistory.size() - 1).equals(move)) {
            board.undoMove(move);
            moveHistory.remove(moveHistory.size() - 1);
            pastPositionHashes.remove(pastPositionHashes.size() - 1);
            turn = (turn == 'w') ? 'b' : 'w';
        }
        checkRep();
    }

    public Move getLastMove() {
        return moveHistory.size() == 0 ? null : moveHistory.get(moveHistory.size() - 1);
    }

    /*private void updateLegalMoves() {
        legalMoves.clear();
        for (Piece[] file : board.getPieceArray()) {
            for (Piece piece : file) {
                if (piece != null) {
                    legalMoves.addAll(piece.getLegalMoves(this));
                }
            }
        }
    }//*/

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
        return turn == gameModel.turn && board.equals(gameModel.board) && moveHistory.equals(gameModel.moveHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, moveHistory, turn);
    }

    private void checkRep() {
        if (board == null || moveHistory == null || pastPositionHashes == null ||
                pastPositionHashes.size() == 0 || pastPositionHashes.get(pastPositionHashes.size() - 1) != hashCode()) {
            throw new RuntimeException("Representation is incorrect.");
        }
    }
}
