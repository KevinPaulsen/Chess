package main.java.model;

import main.java.ChessCoordinate;
import main.java.Move;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameModel {

    private final BoardModel board;
    private final Set<Move> legalMoves;
    private final List<Move> moveList;

    private char turn;

    public GameModel() {
        this.board = ChessBoardFactory.createNormalBoard();
        this.legalMoves = new HashSet<>();
        this.turn = 'w';
        this.moveList = new ArrayList<>();
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
                if (move.getStartingCoordinate().equals(startCoordinate) && move.getEndingCoordinate().equals(endCoordinate)) {
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
            updateLegalMoves();
            turn = (turn == 'w') ? 'b' : 'w';
            didMove = true;
        }

        return didMove;
    }

    public Move getLastMove() {
        return moveList.get(moveList.size() - 1);
    }

    private void updateLegalMoves() {

    }
}
