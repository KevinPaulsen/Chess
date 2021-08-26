package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;
import chess.model.GameModel;

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
     * The color of this Piece
     */
    protected final char color;

    /**
     * The number of times this piece has moved, cannot be less than 0.
     */
    protected final int timesMoved;

    /**
     * The coordinate this piece is at
     */
    protected ChessCoordinate coordinate;

    /**
     * The set of moves this piece can make, this is ignoring checks.
     */
    protected final Set<Move> moves;

    /**
     * The set of coordinates this piece is attacking
     */
    protected final Set<ChessCoordinate> attackingCoords;

    /**
     * Constructs a new Piece with the given color and on the given coordinate.
     *
     * @param color the color of this Piece.
     * @param coordinate the coordinate of this Piece.
     */
    public Piece(char color, ChessCoordinate coordinate) {
        this.color = color;
        this.timesMoved = 0;
        this.coordinate = coordinate;
        moves = new HashSet<>();
        attackingCoords = new HashSet<>();
    }

    //public abstract void updateAttacking(GameModel gameModel);

    /**
     * Updates the set of all legal moves this piece can make.
     *
     * @param board the board this piece is on.
     * @param lastMove the last made move.
     */
    public abstract Set<Move> updateLegalMoves(BoardModel board, Move lastMove);

    /**
     * @return the color of this piece
     */
    public char getColor() {
        return color;
    }

    /**
     * @return the number of times this piece has moved
     */
    public int getTimesMoved() {
        return timesMoved;
    }

    /**
     * @return the coordinate this piece is on
     */
    public ChessCoordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piece)) return false;
        Piece piece = (Piece) o;
        return color == piece.color
                && timesMoved == piece.timesMoved
                && Objects.equals(coordinate, piece.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, timesMoved, coordinate);
    }

    /**
     * Returns a set of the coordinates in the given direction up to and including the first
     * obstructing piece.
     *
     * @param boardModel the board model this piece is in.
     * @param direction the direction to get coordinates in.
     * @return a set of the coordinates in the given direction.
     */
    protected Set<ChessCoordinate> getOpenCoordinatesInDirection(BoardModel boardModel, Direction direction) {
        Set<ChessCoordinate> openCoordinates = new HashSet<>();
        for (ChessCoordinate nextCoordinate = direction.next(coordinate);
             nextCoordinate != null; nextCoordinate = direction.next(nextCoordinate)) {
            openCoordinates.add(nextCoordinate);
            if (boardModel.getPieceOn(nextCoordinate) != null) {
                break;
            }
        }
        return openCoordinates;
    }

    /**
     * Generates and adds the move associated with the given coordinate.
     *
     * @param board the board this piece is on.
     * @param coordinate the coordinate this piece is on.
     */
    protected void addMove(BoardModel board, ChessCoordinate coordinate) {
        if (coordinate != null) {
            attackingCoords.add(coordinate);
            Piece endPiece = board.getPieceOn(coordinate);
            if (endPiece == null) {
                moves.add(new Move(coordinate, this));
            } else if (endPiece.color != color) {
                moves.add(new Move(coordinate, this, null, endPiece));
            }
        }
    }
}
