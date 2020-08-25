package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.GameModel;
import main.java.model.moves.Move;

import java.util.ArrayList;
import java.util.Objects;

public abstract class Piece {

    protected final byte color;
    protected int timesMoved = 0;
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
     * Updates the required fields when a piece moves
     *
     * @param coordinate coordinate the piece will move to.
     */
    public void moveTo(ChessCoordinate coordinate) {
        this.coordinate = coordinate;
        timesMoved++;
    }

    public void moveBackTo(ChessCoordinate coordinate) {
        this.coordinate = coordinate;
        timesMoved--;
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

    public abstract double getValue();

    public abstract String getShortString();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return color == piece.color &&
                timesMoved == piece.timesMoved &&
                coordinate.equals(piece.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, timesMoved, coordinate);
    }
}
