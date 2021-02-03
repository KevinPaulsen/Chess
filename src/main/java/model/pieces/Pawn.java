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

    /**
     * Creates a pawn with the given color and coordinate
     *
     * @param color the color of this pawn
     * @param coordinate the coordinate of this pawn
     */
    public Pawn(char color, ChessCoordinate coordinate) {
        super(coordinate, color);
        movementRules = getMovementRules(color);
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
                new MovementRule(new Direction(direction, 0), 2, (start, end, piece, game) -> {
                    Piece occupyingPiece = game.getBoard().getPieceOn(end);
                    if (occupyingPiece == null && (Math.abs(end.getRank() - start.getRank()) == 1 || !hasMoved())) {
                        return new Move(end, piece, null, null);
                    }
                    return null;
                })
        );
    }

    /**
     * Returns true if this pawn can take EnPassant in the given direction.
     *
     * @param lastMove the last move made
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
        return (start, end, piece, game) -> {
            Piece capturedPiece = game.getBoard().getPieceOn(end);

            if (capturedPiece != null && capturedPiece.color != color) {
                // If piece on square, and captured piece is opposite color, make capture move.
                return new Move(end, piece, null, capturedPiece);
            } else if (canPassant(game.getLastMove(), direction)) {
                // if can passant, capture
                capturedPiece = game.getBoard().getPieceOn(
                        BoardModel.getChessCoordinate(start.getFile() + direction, start.getRank()));
                return new Move(end, piece, null, capturedPiece);
            } else {
                // If captured Piece is null, or captured piece is same color, and cant EnPassant, return null.
                return null;
            }
        };
    }
}
