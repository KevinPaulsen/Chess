package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.ArrayList;

public class Pawn extends Piece {

    public Pawn(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<ChessCoordinate> getPossibleMoves() {
        ArrayList<ChessCoordinate> possibleMoves = new ArrayList<>();

        ChessCoordinate possibleCoordinate1 = new ChessCoordinate(coordinate.getColumn(), coordinate.getRow() + 1);
        ChessCoordinate possibleCoordinate2 = new ChessCoordinate(coordinate.getColumn(), coordinate.getRow() + 2);

        if (possibleCoordinate1.isInBounds()) {
            possibleMoves.add(possibleCoordinate1);
        }
        if (possibleCoordinate2.isInBounds()) {
            possibleMoves.add(possibleCoordinate2);
        }

        return possibleMoves;
    }
}
