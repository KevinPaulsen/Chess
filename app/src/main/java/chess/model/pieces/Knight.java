package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;

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
     * Creates a Knight from a pawn. This is used for promotion. The UID and
     * all other properties of this piece are the exact same as the given pawn.
     *
     * @param pawn the pawn that is promoted.
     */
    public Knight(Pawn pawn) {
        super(pawn);
    }

    /**
     * Updates the set of all legal moves this piece can make.
     *
     * @param board    the board this piece is on.
     * @param lastMove the last made move.
     */
    @Override
    public Set<Move> updateLegalMoves(BoardModel board, Move lastMove) {
        clearMoves(board);
        clearAttacking(board);

        for (Direction direction : Directions.KNIGHTS.directions) {
            ChessCoordinate coordinate = direction.next(getCoordinate());
            addMove(board, coordinate);
        }
        syncMoves(board);
        return moves;
    }

    @Override
    public String toString() {
        return "N";
    }
}
