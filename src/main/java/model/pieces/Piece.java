package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.ArrayList;

public abstract class Piece {

    private final byte color;
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
    public abstract ArrayList<ChessCoordinate> getPossibleMoves();

    public byte getColor() {
        return color;
    }

    public ChessCoordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(ChessCoordinate coordinate) {
        this.coordinate = coordinate;
    }
}
