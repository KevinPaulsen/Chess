package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;
import chess.model.GameModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is an implementation of Piece. This class hold the information
 * for how a King moves. This class can also calculate if a given chessCoordinate
 * is attacked by the opposite color.
 */
public class King extends Piece {

    /**
     * Creates a new king with the given color and coordinate.
     *
     * @param color the given color
     * @param coordinate the given coordinate
     */
    public King(char color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    /**
     * Returns the set of all legal moves this piece can make.
     *
     * @param board    the board this piece is on.
     * @param lastMove the last made move.
     * @return the set of all legal moves this piece can make.
     */
    @Override
    public Set<Move> updateLegalMoves(BoardModel board, Move lastMove) {
        moves.clear();
        attackingCoords.clear();

        for (Direction direction : Directions.ALL_DIRECTIONS.directions) {
            ChessCoordinate coordinate = direction.next(getCoordinate());
            addMove(board, coordinate);
        }

        // TODO: Castling Logic
        return moves;
    }

    @Override
    public String toString() {
        return "K";
    }
}
