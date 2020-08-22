package main.java.model;

import main.java.model.pieces.Piece;

public class SquareModel {

    private final int realColor;
    private int color;
    private Piece piece;

    public SquareModel(int color) {
        this.realColor = color;
        this.color = color;
    }

    public SquareModel(int color, Piece piece) {
        this.realColor = color;
        this.color = color;
        this.piece = piece;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void resetColor() {
        this.color = realColor;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }
}
