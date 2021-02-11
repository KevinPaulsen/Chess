package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.BoardModel;
import main.java.model.GameModel;

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
     * The move maker for a castle move. This MoveMaker ensures that the
     * King does not travel over an attacked coordinate, and isn't in check.
     * It also guarantees that there are no pieces in-between this and the rook,
     * also that neither the rook nor the king have moved. If any of these
     * conditions are false, then null is returned.
     */
    private final MoveMaker castleMoveMaker = (start, end, game, code) -> {
        if (!hasMoved() && (end.getFile() == 2 || end.getFile() == 6)) {
            int direction = end.getFile() == 2 ? -1 : 1;
            BoardModel board = game.getBoard();

            // The coordinate directly next to the king in the direction we are castling.
            ChessCoordinate coordinate1 = BoardModel.getChessCoordinate(
                    coordinate.getFile() + direction, coordinate.getRank());
            // The coordinate 1 space away from the king in the direction we are castling.
            ChessCoordinate coordinate2 = BoardModel.getChessCoordinate(
                    coordinate.getFile() + 2 * direction, coordinate.getRank());
            // The coordinate next to rook on the A file.
            ChessCoordinate coordinate3 = BoardModel.getChessCoordinate(
                    coordinate.getFile() + -3, coordinate.getRank());
            // The rook we are castling with.
            Piece rook = board.getPieceOn(BoardModel.getChessCoordinate(
                    direction == 1 ? 7 : 0, start.getRank()));

            if (rook instanceof Rook && !rook.hasMoved()
                    && board.getPieceOn(coordinate1) == null
                    && board.getPieceOn(coordinate2) == null
                    && (direction == 1 || board.getPieceOn(coordinate3) == null)
                    && !isAttacked(coordinate, game.getBoard())
                    && !isAttacked(coordinate1, game.getBoard())
                    && !isAttacked(coordinate2, game.getBoard())) {
                return new Move(end, game.getBoard().getPieceOn(start), coordinate1, rook);
            }
        }
        return null;
    };

    /**
     * Creates a new king with the given color and coordinate.
     *
     * @param color the given color
     * @param coordinate the given coordinate
     */
    public King(char color, ChessCoordinate coordinate) {
        super(coordinate, color);
        movementRules = getMovementRules();
    }

    /**
     * Returns the set of movement rules for this King.
     *
     * @return the set of movement rules for this King.
     */
    private Set<MovementRule> getMovementRules() {
        Set<MovementRule> movementRules = new HashSet<>();
        for (Direction direction : Directions.STRAIGHTS.directions) {
            movementRules.add(new MovementRule(direction, 1, STANDARD_MOVE_MAKER));
        }
        for (Direction direction : Directions.DIAGONALS.directions) {
            movementRules.add(new MovementRule(direction, 1, STANDARD_MOVE_MAKER));
        }
        movementRules.add(new MovementRule(new Direction(0, 2), 1, castleMoveMaker));
        movementRules.add(new MovementRule(new Direction(0, -2), 1, castleMoveMaker));
        return Collections.unmodifiableSet(movementRules);
    }

    /**
     * Returns true if the given coordinate is being attacked by any piece in
     * game.
     *
     * @param coordinate The coordinate to check if there is an attacker.
     * @param board the board the coordinate is in.
     * @return true if coordinate is being attacked.
     */
    public boolean isAttacked(ChessCoordinate coordinate, BoardModel board) {

        ChessCoordinate searchCoordinate;
        int distance;

        // Check Diagonal Attackers
        for (Direction direction : Directions.DIAGONALS.directions) {
            distance = 1;
            searchCoordinate = direction.next(coordinate);
            while (searchCoordinate != null) {
                Piece occupyingPiece = board.getPieceOn(searchCoordinate);
                if (occupyingPiece != null) {
                    if (occupyingPiece.color != color
                            && (occupyingPiece instanceof Bishop
                            || occupyingPiece instanceof Queen
                            || (distance == 1
                            && (occupyingPiece instanceof King
                            || occupyingPiece instanceof Pawn)))) {
                        return true;
                    }
                    break;
                }
                distance++;
                searchCoordinate = direction.next(searchCoordinate);
            }
        }

        // Check Straight Attackers
        for (Direction direction : Directions.STRAIGHTS.directions) {
            distance = 1;
            searchCoordinate = direction.next(coordinate);
            while (searchCoordinate != null) {
                Piece occupyingPiece = board.getPieceOn(searchCoordinate);
                if (occupyingPiece != null) {
                    if (occupyingPiece.color != color
                            && (occupyingPiece instanceof Rook
                            || occupyingPiece instanceof Queen
                            || (distance == 1 && occupyingPiece instanceof King))) {
                        return true;
                    }
                    break;
                }
                distance++;
                searchCoordinate = direction.next(searchCoordinate);
            }
        }

        // Check Knight Attackers
        for (Direction direction : Directions.KNIGHTS.directions) {
            searchCoordinate = direction.next(coordinate);
            Piece occupyingPiece = board.getPieceOn(searchCoordinate);
            if (occupyingPiece instanceof Knight && occupyingPiece.color != color) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "K";
    }
}
