package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.BoardModel;

import java.util.Set;

public class King extends Piece {

    public King(char color, ChessCoordinate coordinate) {
        super(coordinate, color);
        movementRules = getMovementRules();
    }

    public boolean canMoveTo(ChessCoordinate endCoordinate, Piece[][] pieceArray) {
        return false;
    }

    private Set<MovementRule> getMovementRules() {
        return Set.of(
                new MovementRule(new Direction(1, 1), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(1, 0), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(1, -1), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, 1), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, -1), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, 1), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, 0), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, -1), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, 2), 1, color, getCastleMoveMaker()),
                new MovementRule(new Direction(0, -2), 1, color, getCastleMoveMaker())
        );
    }

    public boolean isInCheck(ChessCoordinate coordinate) {
        return false;
    }

    private MoveMaker getCastleMoveMaker() {
        return (start, end, piece, game) -> {
            if (!hasMoved && end.getFile() == 2 && end.getFile() == 6) {
                int direction = end.getFile() == 2 ? -1 : 1;
                BoardModel board = game.getBoard();
                ChessCoordinate coordinate1 = BoardModel.getChessCoordinate(
                        coordinate.getFile() + direction, coordinate.getRank());
                ChessCoordinate coordinate2 = BoardModel.getChessCoordinate(
                        coordinate.getFile() + 2 * direction, coordinate.getRank());
                Piece rook = board.getPieceOn(BoardModel.getChessCoordinate(
                        direction == 1 ? 7 : 1, start.getRank()));

                if (rook instanceof Rook && !rook.hasMoved
                        && board.getPieceOn(coordinate1) == null
                        && board.getPieceOn(coordinate2) == null
                        && !isInCheck(coordinate)
                        && !isInCheck(coordinate1)
                        && !isInCheck(coordinate2)) {
                    return new Move(end, piece, coordinate1, rook);
                }
            }
            return null;
        };
    }
}
