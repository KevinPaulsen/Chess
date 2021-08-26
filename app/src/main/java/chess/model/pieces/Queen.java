package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;

import java.util.Set;

/**
 * This is an implementation of Piece. This implementation specifies the
 * movement and movement of a Queen.
 */
public class Queen extends Piece {

    /**
     * Creates a new Rook with the given color and placed on the given
     * coordinate. This sets the Movement rules to the movement rules of
     * a rook.
     *
     * @param color the color of this rook
     * @param coordinate the coordinate of this rook
     */
    public Queen(char color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    /**
     * Updates the set of all legal moves this piece can make.
     *
     * @param board    the board this piece is on.
     * @param lastMove the last made move.
     */
    @Override
    public Set<Move> updateLegalMoves(BoardModel board, Move lastMove) {
        moves.clear();
        clearAttacking(board);

        for (Direction direction : Directions.ALL_DIRECTIONS.directions) {
            for (ChessCoordinate coordinate : getOpenCoordinatesInDirection(board, direction)) {
                addMove(board, coordinate);
            }
        }
        return moves;
    }

    @Override
    public String toString() {
        return "Q";
    }
}
