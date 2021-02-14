package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.GameModel;

import java.util.HashSet;
import java.util.Objects;
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
    protected static final MoveMaker STANDARD_MOVE_MAKER = (start, end, game, code) -> {
        Piece capturedPiece = game.getBoard().getPieceOn(end);
        Piece movingPiece = game.getBoard().getPieceOn(start);
        if (capturedPiece == null || capturedPiece.color != movingPiece.color) {
            return new Move(end, movingPiece, null, capturedPiece);
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
    protected int timesMoved;
    private static int identifier = 0;
    private final int uniqueIdentifier;

    /**
     * Constructs a piece with no movement rules, and is on the given
     * coordinate, and is of the color specified.
     *
     * @param coordinate the coordinate this piece is on.
     * @param color      the color of this piece.
     */
    public Piece(char color, ChessCoordinate coordinate) {
        this.movementRules = Set.of();
        this.coordinate = coordinate;
        this.color = color;
        timesMoved = 0;
        identifier++;
        uniqueIdentifier = identifier;
    }

    public Piece(Piece piece) {
        this.movementRules = Set.of();
        this.coordinate = piece.coordinate;
        this.color = piece.color;
        this.timesMoved = 0;
        this.uniqueIdentifier = piece.uniqueIdentifier;
    }

    /**
     * Moves the piece to given coordinate, the given coordinate does
     * not need to follow this pieces movement rules.
     *
     * @param coordinate the coordinate this piece is moving to.
     */
    public void moveTo(ChessCoordinate coordinate, int movesToAdd) {
        this.coordinate = coordinate;
        if (-1 <= movesToAdd && movesToAdd <= 1) {
            timesMoved += movesToAdd;
        }
    }

    public static Piece clone(Piece piece) {
        if (piece instanceof Pawn) {
            return new Pawn(piece);
        } else if (piece instanceof Knight) {
            return new Knight(piece);
        } else if (piece instanceof Bishop) {
            return new Bishop(piece);
        } else if (piece instanceof Rook) {
            return new Rook(piece);
        } else if (piece instanceof Queen) {
            return new Queen(piece);
        } else if (piece instanceof King) {
            return new King(piece);
        } else {
            return null;
        }
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

    public boolean hasMoved() {
        return timesMoved != 0;
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

    public int getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piece)) return false;
        Piece piece = (Piece) o;
        return color == piece.color && uniqueIdentifier == piece.uniqueIdentifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, uniqueIdentifier);
    }
}
