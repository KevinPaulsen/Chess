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
                    && board.getSquare(coordinate).numAttackers(color == 'w' ? 'b' : 'w') == 0
                    && coordinate1 != null && coordinate2 != null
                    && board.getSquare(coordinate1).numAttackers(color == 'w' ? 'b' : 'w') == 0
                    && board.getSquare(coordinate2).numAttackers(color == 'w' ? 'b' : 'w') == 0) {
                return new Move(end, board.getPieceOn(start), coordinate1, rook);
            }
        }
        return null;
    };

    private final MoveMaker normalMoveMaker = (start, end, game, code) -> {
        Piece capturedPiece = game.getBoard().getPieceOn(end);
        Piece movingPiece = game.getBoard().getPieceOn(start);
        if (game.getBoard().getSquare(end).numAttackers(color) == 0
                && (capturedPiece == null || capturedPiece.color != movingPiece.color)) {
            return new Move(end, movingPiece, null, capturedPiece);
        } else {
            return null;
        }
    };

    /**
     * Creates a new king with the given color and coordinate.
     *
     * @param color the given color
     * @param coordinate the given coordinate
     */
    public King(char color, ChessCoordinate coordinate) {
        super(color, coordinate);
        movementRules.addAll(getMovementRules());
    }

    /**
     * Constructs a piece from the given piece. The UID of this
     * piece will be the same as the piece given.
     *
     * @param piece the piece to create this piece with
     */
    public King(Piece piece) {
        super(piece);
        movementRules.addAll(getMovementRules());
    }

    /**
     * If able to move, creates the move to the given coordinate. Assumes that
     * no piece blocks, and isn't pinned.
     *
     * @param gameModel  the game this move occurs in
     * @param coordinate the ending coordinate
     * @return the move that moves from this.coordinate to coordinate
     */
    @Override
    protected Move makeMove(GameModel gameModel, ChessCoordinate coordinate) {
        Move move = castleMoveMaker.getMove(this.coordinate, coordinate, gameModel, Pawn.QUEEN_PROMOTION);
        if (move == null) {
            move = normalMoveMaker.getMove(this.coordinate, coordinate, gameModel, Pawn.QUEEN_PROMOTION);
        }
        return move;
    }

    /**
     * Returns the set of movement rules for this King.
     *
     * @return the set of movement rules for this King.
     */
    private Set<MovementRule> getMovementRules() {
        Set<MovementRule> movementRules = new HashSet<>();
        for (Direction direction : Directions.STRAIGHTS.directions) {
            movementRules.add(new MovementRule(direction, 1, normalMoveMaker));
        }
        for (Direction direction : Directions.DIAGONALS.directions) {
            movementRules.add(new MovementRule(direction, 1, normalMoveMaker));
        }
        movementRules.add(new MovementRule(new Direction(0, 2), 1, castleMoveMaker));
        movementRules.add(new MovementRule(new Direction(0, -2), 1, castleMoveMaker));
        return Collections.unmodifiableSet(movementRules);
    }

    @Override
    public String toString() {
        return "K";
    }
}
