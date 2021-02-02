package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.BoardModel;

import java.util.Set;

public class Pawn extends Piece {

    public Pawn(char color, ChessCoordinate coordinate) {
        super(coordinate, color);
        movementRules = getMovementRules(color);
    }

    private Set<MovementRule> getMovementRules(char color) {
        int direction = (color == 'w') ? 1 : -1;
        return Set.of(
                new MovementRule(new Direction(direction, 1), 1, color, getDiagonalMoveMaker(1)),
                new MovementRule(new Direction(direction, -1), 1, color, getDiagonalMoveMaker(-1)),
                new MovementRule(new Direction(direction, 0), 2, color, (start, end, piece, game) -> {
                    Piece occupyingPiece = game.getBoard().getPieceOn(end);
                    if (occupyingPiece == null && (Math.abs(end.getRank() - start.getRank()) == 1 || !hasMoved)) {
                        return new Move(end, piece, null, null);
                    }
                    return null;
                })
        );
    }

    private boolean canPassant(Move lastMove, int direction) {
        return lastMove.getMovingPiece() instanceof Pawn
                && lastMove.getEndingCoordinate().equals(BoardModel
                .getChessCoordinate(coordinate.getFile() + direction, coordinate.getRank()))
                && Math.abs(lastMove.getStartingCoordinate().getRank()
                - lastMove.getEndingCoordinate().getRank()) == 2;
    }

    private MoveMaker getDiagonalMoveMaker(int direction) {
        return (start, end, piece, game) -> {
            Piece capturedPiece = game.getBoard().getPieceOn(end);
            if (capturedPiece != null) {
                return new Move(end, piece, null, capturedPiece);
            } else if (canPassant(game.getLastMove(), direction)) {
                capturedPiece = game.getBoard().getPieceOn(
                        BoardModel.getChessCoordinate(start.getFile() + direction, start.getRank()));
                return new Move(end, piece, null, capturedPiece);
            }
            return null;
            };
    }

    public boolean canMoveTo(ChessCoordinate endCoordinate, Piece[][] pieceArray) {
        return false;
    }
}
