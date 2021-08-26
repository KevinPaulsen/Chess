package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;
import chess.model.GameModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is an implementation of Piece. This class implements a Knight Piece that
 * moves how a Knight should.
 */
public class Knight extends Piece {

    /**
     * Creates a Knight with the given color and coordinate.
     *
     * @param color the color of this knight
     * @param coordinate the coordinate of this knight
     */
    public Knight(char color, ChessCoordinate coordinate) {
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

        for (Direction direction : Directions.KNIGHTS.directions) {
            ChessCoordinate coordinate = direction.next(getCoordinate());
            addMove(board, coordinate);
        }
        return moves;
    }

    @Override
    public String toString() {
        return "N";
    }
}
