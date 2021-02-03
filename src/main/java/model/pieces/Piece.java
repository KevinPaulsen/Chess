package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.GameModel;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is an abstract representation of a Piece. Any piece can implement
 * this class. A piece contains information about how the piece moves, and its
 * color, and where it is on the board.
 */
public abstract class Piece {

    /**
     * A standard move in which the piece moves to the square, and captures
     * the piece currently occupying that square.
     */
    protected static final MoveMaker STANDARD_MOVE_MAKER = (startCoordinate, endCoordinate, movingPiece, game) -> {
        Piece capturedPiece = game.getBoard().getPieceOn(endCoordinate);
        if (capturedPiece == null || capturedPiece.color != movingPiece.color) {
            return new Move(endCoordinate, movingPiece, null, capturedPiece);
        } else {
            return null;
        }
    };
    protected static final int LONG_MOVING_MAX = 8;
    // The color of this piece
    protected final char color;
    // The set of movement rules that define how this piece moves.
    protected Set<MovementRule> movementRules;
    // The coordinate this piece is currently on.
    protected ChessCoordinate coordinate;
    // Weather or not this piece has moved.
    protected boolean hasMoved;

    /**
     * Constructs a piece with no movement rules, and is on the given
     * coordinate, and is of the color specified.
     *
     * @param coordinate the coordinate this piece is on.
     * @param color      the color of this piece.
     */
    public Piece(ChessCoordinate coordinate, char color) {
        this.movementRules = Set.of();
        this.coordinate = coordinate;
        this.color = color;
        hasMoved = false;
    }

    /**
     * Moves the piece to given coordinate, the given coordinate does
     * not need to follow this pieces movement rules.
     *
     * @param coordinate the coordinate this piece is moving to.
     */
    public void moveTo(ChessCoordinate coordinate) {
        this.coordinate = coordinate;
        hasMoved = true;
    }

    /**
     * Returns the set of all possible moves this piece can move to. This
     * Method takes into account position of other pieces, and moves according
     * to the MovementRules of this Piece. If no moves are possible, an empty
     * Set is returned. This requires that game is non-null.
     *
     * @param game the game object containing this Piece.
     * @return the set of all legal moves this piece can make.
     */
    public Set<Move> getLegalMoves(GameModel game) {
        Set<Move> moves = new HashSet<>();
        for (MovementRule movementRule : movementRules) {
            moves.addAll(movementRule.getMoves(coordinate, game));
        }
        return moves;
    }

    /**
     * Returns the color of this Piece.
     *
     * @return the color of this Piece.
     */
    public char getColor() {
        return color;
    }

    /**
     * Returns the coordinate of this Piece.
     *
     * @return the coordinate this piece is on.
     */
    public ChessCoordinate getCoordinate() {
        return coordinate;
    }
}
