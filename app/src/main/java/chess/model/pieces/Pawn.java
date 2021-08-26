package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;

import java.util.Set;

/**
 * This is an implementation of Piece. This class contains all
 * movement information of a pawn.
 */
public class Pawn extends Piece {

    /**
     * The direction this pawn moves
     */
    private final Direction straightMove;

    /**
     * The direction this pawn captures left
     */
    private final Direction captureLeft;

    /**
     * The direction this pawn captures right
     */
    private final Direction captureRight;

    /**
     * Creates a pawn with the given color and coordinate
     *
     * @param color      the color of this pawn
     * @param coordinate the coordinate of this pawn
     */
    public Pawn(char color, ChessCoordinate coordinate) {
        super(color, coordinate);
        straightMove = new Direction(color == 'w' ? 1 : -1, 0);
        captureLeft = new Direction(color == 'w' ? 1 : -1, -1);
        captureRight = new Direction(color == 'w' ? 1 : -1, 1);
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
        attackingCoords.clear();

        // Straight Moving Moves
        ChessCoordinate nextCoordinate = straightMove.next(coordinate);
        if (board.getPieceOn(nextCoordinate) == null) {
            moves.add(new Move(nextCoordinate, this));

            if (timesMoved == 0) {
                nextCoordinate = straightMove.next(nextCoordinate);
                if (board.getPieceOn(nextCoordinate) == null) {
                    moves.add(new Move(nextCoordinate, this));
                }
            }
        }

        // Left Capture
        addMove(board, captureLeft.next(coordinate));

        // Right Capture
        addMove(board, captureRight.next(coordinate));

        // TODO: EnPassant
        return moves;
    }

    /**
     * Returns true if this pawn can take EnPassant in the given direction.
     *
     * @param lastMove  the last move made
     * @param direction the direction we are attempting to take EnPassant
     * @return if we can take EnPassant
     */
    private boolean canPassant(Move lastMove, int direction) {
        return lastMove != null && lastMove.getMovingPiece() instanceof Pawn
                && lastMove.getMovingPiece().color != color
                && lastMove.getEndingCoordinate().equals(BoardModel
                .getChessCoordinate(coordinate.getFile() + direction, coordinate.getRank()))
                && Math.abs(lastMove.getStartingCoordinate().getRank()
                - lastMove.getEndingCoordinate().getRank()) == 2;
    }

    @Override
    public String toString() {
        return "";
    }
}
