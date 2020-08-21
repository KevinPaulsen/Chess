package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.GameModel;
import main.java.model.Move;
import main.java.model.SquareModel;

import java.util.ArrayList;

public class Bishop extends LongMovingPiece {

    public Bishop(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<Move> getPossibleMoves(GameModel gameModel) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        // Loop through each direction (Down left or Up Right)
        for (int colDirection = -1; colDirection <= 1; colDirection += 2) {
            for (int rowDirection = -1; rowDirection <= 1; rowDirection += 2) {
                possibleMoves.addAll(getMovesAlongDirection(colDirection, rowDirection, gameModel.getBoardModel()));
            }
        }
        return possibleMoves;
    }
}
