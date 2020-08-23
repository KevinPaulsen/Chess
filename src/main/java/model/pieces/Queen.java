package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.GameModel;
import main.java.model.Move;

import java.util.ArrayList;

public class Queen extends LongMovingPiece {

    public Queen(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<Move> getPossibleMoves(GameModel gameModel) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        // Loop through each direction the queen can go
        for (int colDirection = -1; colDirection <= 1; colDirection++) {
            for (int rowDirection = -1; rowDirection <= 1; rowDirection++) {
                // if both directions are 0, continue.
                if (colDirection == 0 && rowDirection == 0) {
                    continue;
                }
                possibleMoves.addAll(getMovesAlongDirection(colDirection, rowDirection, gameModel.getBoardModel()));
            }
        }
        return possibleMoves;
    }

    @Override
    public String getShortString() {
        return "Q";
    }

    @Override
    public String toString() {
        return "Queen{" +
                "color=" + color +
                ", timesMoved=" + timesMoved +
                ", coordinate=" + coordinate +
                '}';
    }
}
