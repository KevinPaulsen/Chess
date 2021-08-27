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
        clearMoves(board.getSudoLegalMoves());
        clearAttacking(board);

        // Straight Moving Moves
        ChessCoordinate nextCoordinate = straightMove.next(coordinate);

        if (board.getPieceOn(nextCoordinate) == null) {
            if (nextCoordinate.getRank() % 7 == 0) {
                addAllPromotions(nextCoordinate, null);
            } else {
                moves.add(new Move(nextCoordinate, this));
            }

            if (timesMoved == 0) {
                nextCoordinate = straightMove.next(nextCoordinate);
                if (board.getPieceOn(nextCoordinate) == null) {
                    moves.add(new Move(nextCoordinate, this));
                }
            }
        }

        // Diagonal Captures
        addCapture(board, captureLeft, lastMove);
        addCapture(board, captureRight, lastMove);

        // TODO: promotion

        syncMoves(board);
        return moves;
    }

    private void addAllPromotions(ChessCoordinate coordinate, Piece capturedPiece) {
        moves.add(new Move(coordinate, this, null, capturedPiece, new Bishop(this)));
        moves.add(new Move(coordinate, this, null, capturedPiece, new Knight(this)));
        moves.add(new Move(coordinate, this, null, capturedPiece, new Rook(this)));
        moves.add(new Move(coordinate, this, null, capturedPiece, new Queen(this)));
    }

    /**
     * Checks to see if a capture is possible in the given direction. If it is,
     * it adds the move to the available captures.
     *
     * @param board the boardModel this piece is on.
     * @param direction the direction to check captures.
     */
    private void addCapture(BoardModel board, Direction direction, Move lastMove) {
        ChessCoordinate nextCoord = direction.next(coordinate);
        if (nextCoord != null) {
            Piece piece = board.getPieceOn(nextCoord);

            attackingCoords.add(nextCoord);
            board.getSquare(nextCoord).addAttacker(this);

            if (piece != null && piece.color != color) {
                if (nextCoord.getRank() % 7 == 0) {
                    addAllPromotions(nextCoord, piece);
                } else {
                    moves.add(new Move(nextCoord, this, null, piece));
                }
            }

            if (canPassant(lastMove, direction.getRun())) {
                moves.add(new Move(nextCoord, this, null, lastMove.getMovingPiece()));
            }
        }
    }

    /**
     * Returns true if this pawn can take EnPassant in the given direction.
     *
     * @param lastMove  the last move made
     * @param direction the direction we are attempting to take EnPassant
     * @return if we can take EnPassant
     */
    private boolean canPassant(Move lastMove, int direction) {
        if (lastMove == null) {
            return false;
        }
        boolean pawnMovedLast = lastMove.getMovingPiece() instanceof Pawn;
        boolean pieceIsOpposingColor = lastMove.getMovingPiece().color != color;
        boolean pieceIsNextToThis = lastMove.getEndingCoordinate().equals(BoardModel
                .getChessCoordinate(coordinate.getFile() + direction, coordinate.getRank()));
        boolean pieceMovedTwoSpaces = Math.abs(lastMove.getStartingCoordinate().getRank()
                - lastMove.getEndingCoordinate().getRank()) == 2;
        return pawnMovedLast && pieceIsOpposingColor && pieceIsNextToThis && pieceMovedTwoSpaces;
    }

    @Override
    public String toString() {
        return "";
    }
}
