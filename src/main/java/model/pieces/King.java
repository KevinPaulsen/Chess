package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.ArrayList;

public class King extends Piece {

    public King(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<ChessCoordinate> getPossibleMoves() {
        ArrayList<ChessCoordinate> possibleMoves = new ArrayList<>();

        for (int relativeRow = - 1; relativeRow <= 1; relativeRow++) {
            for (int relativeCol = -1; relativeCol <= 1; relativeCol++) {
                if (relativeRow == 0 && relativeCol == 0) {
                    continue;
                }
                boolean isInRowBound = 0 <= coordinate.getRow() + relativeRow && coordinate.getRow() + relativeRow <= 7;
                boolean isInColBound = 0 <= coordinate.getColumn() + relativeCol && coordinate.getColumn() + relativeCol <= 7;
                if (isInRowBound && isInColBound) {
                    possibleMoves.add(new ChessCoordinate(coordinate.getColumn() + relativeCol, coordinate.getRow() + relativeRow));
                }
            }
        }

        return possibleMoves;
    }
}
