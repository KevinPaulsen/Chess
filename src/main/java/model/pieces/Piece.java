package main.java.model.pieces;

import main.java.ChessCoordinate;

public abstract class Piece {

    private final char color;

    Piece(char color, ChessCoordinate coordinate) {
        this.color = color;
    }

    public char getColor() {
        return color;
    }
}
