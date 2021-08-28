package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class is an abstract representation of a Piece. Any piece can implement
 * this class. A piece contains information about how the piece moves, and its
 * color, and where it is on the board.
 */
public abstract class Piece {

    private static int PIECE_COUNT = 0;

    /**
     * The color of this Piece
     */
    protected final char color;

    /**
     * The number of times this piece has moved, cannot be less than 0.
     */
    protected int timesMoved;

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

    protected final int uid;

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
        this.uid = PIECE_COUNT;
        PIECE_COUNT++;
        moves = new HashSet<>();
        attackingCoords = new HashSet<>();
    }

    protected Piece(Pawn pawn) {
        this.color = pawn.color;
        this.timesMoved = pawn.timesMoved;
        this.coordinate = pawn.coordinate;
        this.uid = pawn.uid;
        moves = pawn.moves;
        attackingCoords = pawn.attackingCoords;
    }

    /**
     * Updates the set of all legal moves this piece can make.
     *
     * @param board the board this piece is on.
     * @param lastMove the last made move.
     * @return the set of moves this piece can make.
     */
    public abstract Set<Move> updateLegalMoves(BoardModel board, Move lastMove);

    /**
     * @return all the moves this piece can make
     */
    public Set<Move> getMoves() {
        return moves;
    }

    public void moveTo(ChessCoordinate coordinate, int movesToAdd) {
        this.coordinate = coordinate;
        this.timesMoved += movesToAdd;
    }

    /**
     * @return the color of this piece
     */
    public char getColor() {
        return color;
    }

    /**
     * @return the coordinate this piece is on
     */
    public ChessCoordinate getCoordinate() {
        return coordinate;
    }

    /**
     * Remove this piece from the given board.
     *
     * @param board the board this piece is to be removed from.
     */
    public void removeFrom(BoardModel board) {
        clearAttacking(board);
        coordinate = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piece)) return false;
        Piece piece = (Piece) o;
        return uid == piece.uid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
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
     * Generates and adds the move associated with the given coordinate. It
     * also adds the coordinate to the square and adds this piece as an attacker.
     *
     * @param board the board this piece is on.
     * @param coordinate the coordinate this piece is on.
     */
    protected void addMove(BoardModel board, ChessCoordinate coordinate) {
        if (coordinate != null) {
            attackingCoords.add(coordinate);
            board.getSquare(coordinate).addAttacker(this);

            Piece endPiece = board.getPieceOn(coordinate);
            if (endPiece == null) {
                moves.add(new Move(coordinate, this));
            } else if (endPiece.color != color) {
                moves.add(new Move(coordinate, this, null, endPiece));
            }
        }
    }

    /**
     * Clears the set of attacking coordinates, and removes this piece from
     * the square attacking list.
     *
     * @param board the board this piece is on.
     */
    protected void clearAttacking(BoardModel board) {
        attackingCoords.forEach(coordinate -> board.getSquare(coordinate).removeAttacker(this));
        attackingCoords.clear();
    }

    /**
     * @return the opposite color of this piece.
     */
    protected char oppositeColor() {
        return color == 'w' ? 'b' : 'w';
    }
}
