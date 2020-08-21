package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.ArrayList;

public class Queen extends Piece {

    public Queen(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<ChessCoordinate> getPossibleMoves() {
        ArrayList<ChessCoordinate> possibleMoves = new ArrayList<>();

        // Loop through each direction the queen can go
        for (int colDirection = -1; colDirection <= 1; colDirection++) {
            for (int rowDirection = -1; rowDirection <= 1; rowDirection++) {
                // if both directions are 0, continue.
                if (colDirection == 0 && rowDirection == 0) {
                    continue;
                }

                // Make a new coordinate that the queen could go to, then check if the coordinate is in the board.
                ChessCoordinate possibleMove = new ChessCoordinate(coordinate.getColumn() + colDirection, coordinate.getRow() + rowDirection);
                while (possibleMove.isInBounds()) {
                    possibleMoves.add(possibleMove);
                    possibleMove = new ChessCoordinate(possibleMove.getColumn() + colDirection, possibleMove.getRow() + rowDirection);
                }
            }
        }

        return possibleMoves;
    }
}
