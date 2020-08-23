package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.GameModel;
import main.java.model.Move;

import java.util.ArrayList;

public class Rook extends LongMovingPiece {

    public Rook(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<Move> getPossibleMoves(GameModel gameModel) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        if (gameModel.getTurn() % 2 != color) {
            return possibleMoves;
        }

        possibleMoves.addAll(getMovesAlongDirection(-1, 0, gameModel.getBoardModel()));
        possibleMoves.addAll(getMovesAlongDirection(0, 1, gameModel.getBoardModel()));
        possibleMoves.addAll(getMovesAlongDirection(1, 0, gameModel.getBoardModel()));
        possibleMoves.addAll(getMovesAlongDirection(0, -1, gameModel.getBoardModel()));
        return possibleMoves;
    }

    @Override
    public double getValue() {
        return 5;
    }

    @Override
    public String getShortString() {
        return "R";
    }

    @Override
    public String toString() {
        return "Rook{" +
                "color=" + color +
                ", timesMoved=" + timesMoved +
                ", coordinate=" + coordinate +
                '}';
    }
}
