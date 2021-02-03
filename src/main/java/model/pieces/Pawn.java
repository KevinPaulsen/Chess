package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.BoardModel;

import java.util.Set;

/**
 * This is an implementation of Piece. This class contains all
 * movement information of a pawn.
 */
public class Pawn extends Piece {

    public static final int QUEEN_PROMOTION = 0;
    public static final int ROOK_PROMOTION = 1;
    public static final int KNIGHT_PROMOTION = 2;
    public static final int BISHOP_PROMOTION = 3;

    private final MoveMaker straightMovement = (start, end, game, code) -> {
        Piece piece = game.getBoard().getPieceOn(start);
        Piece occupyingPiece = game.getBoard().getPieceOn(end);
        if (occupyingPiece == null && (Math.abs(end.getRank() - start.getRank()) == 1 || !hasMoved())) {
            if (end.getRank() == (color == 'w' ? 7 : 0)) {
                return new Move(end, piece, null, null, makePiece(code));
            } else {
                return new Move(end, piece, null, null);
            }
        }
        return null;
    };

    private final int direction;

    /**
     * Creates a pawn with the given color and coordinate
     *
     * @param color      the color of this pawn
     * @param coordinate the coordinate of this pawn
     */
    public Pawn(char color, ChessCoordinate coordinate) {
        super(coordinate, color);
        movementRules = getMovementRules(color);
        direction = color == 'w' ? 1 : -1;
    }

    /**
     * Return the set of movement rules for this pawn.
     *
     * @param color the color of this pawn.
     * @return the set of MovementRules for this pawn.
     */
    private Set<MovementRule> getMovementRules(char color) {
        int direction = (color == 'w') ? 1 : -1;
        return Set.of(
                new MovementRule(new Direction(direction, 1), 1, getDiagonalMoveMaker(1)),
                new MovementRule(new Direction(direction, -1), 1, getDiagonalMoveMaker(-1)),
                new MovementRule(new Direction(direction, 0), 2, straightMovement)
        );
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
                && lastMove.getEndingCoordinate().equals(BoardModel
                .getChessCoordinate(coordinate.getFile() + direction, coordinate.getRank()))
                && Math.abs(lastMove.getStartingCoordinate().getRank()
                - lastMove.getEndingCoordinate().getRank()) == 2;
    }

    /**
     * Returns the MoveMaker for diagonal movement. The MoveMaker will make a
     * move based on the following: if there is a piece to capture, a normal
     * capture will occur. If no piece to capture EnPassant will be checked,
     * If we can take EnPassant, then an EnPassant move will be returned. If
     * neither is possible, null will be returned.
     *
     * @param direction the direction to take EnPassant
     * @return the MoveMaker for diagonal movement.
     */
    private MoveMaker getDiagonalMoveMaker(int direction) {
        return (start, end, game, code) -> {
            Piece piece = game.getBoard().getPieceOn(start);
            Piece capturedPiece = game.getBoard().getPieceOn(end);

            if (canPassant(game.getLastMove(), direction)) {
                capturedPiece = game.getBoard().getPieceOn(
                        BoardModel.getChessCoordinate(start.getFile() + direction, start.getRank()));
            } else if ((capturedPiece == null || capturedPiece.color == color)) {
                return null;
            }
            if (end.getRank() == (color == 'w' ? 7 : 0)) {
                return new Move(end, piece, null, capturedPiece, makePiece(code));
            } else {
                return new Move(end, piece, null, capturedPiece);
            }
        };
    }

    private Piece makePiece(int promotionCode) {
        Piece piece = null;
        switch (promotionCode) {
            case QUEEN_PROMOTION:
                piece = new Queen(color, null);
                break;
            case ROOK_PROMOTION:
                piece = new Rook(color, null);
                break;
            case KNIGHT_PROMOTION:
                piece = new Knight(color, null);
                break;
            case BISHOP_PROMOTION:
                piece = new Bishop(color, null);
                break;
        }
        return piece;
    }
}
