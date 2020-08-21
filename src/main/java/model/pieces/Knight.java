package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.ArrayList;

public class Knight extends Piece {

    public Knight(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<ChessCoordinate> getPossibleMoves() {
        ArrayList<ChessCoordinate> possibleMoves = new ArrayList<>();

        // Loop from 2 columns left of coordinate to two columns right of coordinate
        for (int relativeCol = -2; relativeCol <= 2; relativeCol++) {
            // If relativeCol is 0, continue.
            if (relativeCol == 0) {
                continue;
            }

            // Make two possible coordinates, one up and one down on current column.
            ChessCoordinate possibleCoordinateUp = new ChessCoordinate(coordinate.getColumn() + relativeCol,
                    coordinate.getRow() + (3 - Math.abs(relativeCol)));
            ChessCoordinate possibleCoordinateDown = new ChessCoordinate(coordinate.getColumn() + relativeCol,
                    coordinate.getRow() - (3 - Math.abs(relativeCol)));
            // If possible coordinates are in bounds, add them to possibleMoves.
            if (possibleCoordinateUp.isInBounds()) {
                possibleMoves.add(possibleCoordinateUp);
            }
            if (possibleCoordinateDown.isInBounds()) {
                possibleMoves.add(possibleCoordinateDown);
            }
        }

        return possibleMoves;
    }
}
