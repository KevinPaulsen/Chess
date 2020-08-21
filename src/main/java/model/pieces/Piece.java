package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.GameModel;
import main.java.model.Move;

import java.util.ArrayList;
import java.util.Objects;

public abstract class Piece {

    protected final byte color;
    protected boolean hasMoved = false;
    protected ChessCoordinate coordinate;

    Piece(byte color, ChessCoordinate coordinate) {
        this.color = color;
        this.coordinate = coordinate;
    }

    /**
     * Returns Arraylist of possible moves assuming no other
     * pieces are in the way.
     *
     * @return List of possible Moves.
     */
    public abstract ArrayList<Move> getPossibleMoves(GameModel gameModel);

    /**
     * Updates the required field when a piece moves
     *
     * @param coordinate coordinate the piece will move to.
     */
    public void moveTo(ChessCoordinate coordinate) {
        this.coordinate = coordinate;
        hasMoved = true;
    }

    public byte getColor() {
        return color;
    }

    public ChessCoordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(ChessCoordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return color == piece.color &&
                hasMoved == piece.hasMoved &&
                coordinate.equals(piece.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, hasMoved, coordinate);
    }
}
