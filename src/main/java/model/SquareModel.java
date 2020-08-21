package main.java.model;

import main.java.model.pieces.Piece;

public class SquareModel {

    private final int color;
    private Piece piece;

    public SquareModel(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }
}
