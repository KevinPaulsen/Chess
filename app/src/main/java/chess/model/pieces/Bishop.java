package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;

import java.util.Set;

/**
 * This is an implementation of Piece. This implementation specifies the
 * movement and movement of a Bishop.
 */
public class Bishop extends Piece {

    /**
     * Creates a new bishop with the given color and placed on the given
     * coordinate. This sets the Movement rules to the movement rules of
     * a bishop.
     *
     * @param color the color of this Bishop
     * @param coordinate the coordinate of this Bishop
     */
    public Bishop(char color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    /**
     * Creates a Bishop from a pawn. This is used for promotion. The UID and
     * all other properties of this piece are the exact same as the given pawn.
     *
     * @param pawn the pawn that is promoted.
     */
    public Bishop(Pawn pawn) {
        super(pawn);
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
        clearMoves(board.getSudoLegalMoves());
        clearAttacking(board);

        for (Direction direction : Directions.DIAGONALS.directions) {
            for (ChessCoordinate coordinate : getOpenCoordinatesInDirection(board, direction)) {
                addMove(board, coordinate);
            }
        }
        syncMoves(board);
        return moves;
    }

    @Override
    public String toString() {
        return "B";
    }
}
