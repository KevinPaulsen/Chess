package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.ArrayList;

public class Rook extends Piece {

    public Rook(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<ChessCoordinate> getPossibleMoves() {
        ArrayList<ChessCoordinate> possibleMoves = new ArrayList<>();

        // Loop through each direction (Left or Right)
        for (int colDirection = -1; colDirection <= 1; colDirection += 2) {
            // Create a possibleMove, then check if it is in bounds. repeat until out of bounds.
            ChessCoordinate possibleMove = new ChessCoordinate(coordinate.getColumn() + colDirection, coordinate.getRow());
            while (possibleMove.isInBounds()) {
                possibleMoves.add(possibleMove);
                possibleMove = new ChessCoordinate(possibleMove.getColumn() + colDirection, possibleMove.getRow());
            }
        }
        // Loop through each direction (Left or Right)
        for (int rowDirection = -1; rowDirection <= 1; rowDirection += 2) {
            ChessCoordinate possibleMove = new ChessCoordinate(coordinate.getColumn(), coordinate.getRow() + rowDirection);
            // Create a possibleMove, then check if it is in bounds. repeat until out of bounds.
            while (possibleMove.isInBounds()) {
                possibleMoves.add(possibleMove);
                possibleMove = new ChessCoordinate(possibleMove.getColumn(), possibleMove.getRow() + rowDirection);
            }
        }
        return possibleMoves;
    }
}
