package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.ArrayList;

public class Bishop extends Piece {

    public Bishop(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<ChessCoordinate> getPossibleMoves() {
        ArrayList<ChessCoordinate> possibleMoves = new ArrayList<>();

        int squaresTraveled = 1;
        int column = coordinate.getColumn();
        int row = coordinate.getRow();

        // Loop while column and row +- squaresTraveled are both between 0 and 7 inclusive
        while (0 <= column - squaresTraveled || column + squaresTraveled <= 7 || 0 <= row - squaresTraveled || row + squaresTraveled <= 7) {
            if (0 <= column - squaresTraveled) {
                if (0 <= row - squaresTraveled) {
                    // Check if Down and Left is on board, if yes, add it to possibleMoves
                    possibleMoves.add(new ChessCoordinate(column - squaresTraveled, row - squaresTraveled));
                }
                if (row + squaresTraveled <= 7) {
                    // Check if Up and Left is on board, if yes, add it to possibleMoves
                    possibleMoves.add(new ChessCoordinate(column - squaresTraveled, row + squaresTraveled));
                }
            }
            if (column + squaresTraveled <= 7) {
                if (0 <= row - squaresTraveled) {
                    // Check if Down and Right is on board, if yes, add it to possibleMoves
                    possibleMoves.add(new ChessCoordinate(column + squaresTraveled, row - squaresTraveled));
                }
                if (row + squaresTraveled <= 7) {
                    // Check if Up and Right is on board, if yes, add it to possibleMoves
                    possibleMoves.add(new ChessCoordinate(column + squaresTraveled, row + squaresTraveled));
                }
            }
            squaresTraveled += 1;
        }
        return possibleMoves;
    }
}
