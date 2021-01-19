package main.java.model;

import main.java.ChessCoordinate;
import main.java.model.pieces.Piece;

public class SquareModel {

    private final ChessCoordinate coordinate;
    private Piece piece;

    public SquareModel(ChessCoordinate coordinate, Piece piece) {
        this.coordinate = coordinate;
        this.piece = piece;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }
}
