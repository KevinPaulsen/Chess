package main.java.model.pieces;

public abstract class Piece {

    private final char color;

    Piece(char color) {
        this.color = color;
    }

    public char getColor() {
        return color;
    }
}
