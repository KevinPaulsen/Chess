package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.GameModel;
import main.java.model.Move;

import java.util.ArrayList;

public class Knight extends Piece {

    public Knight(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<Move> getPossibleMoves(GameModel gameModel) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        // Loop from 2 columns left of coordinate to two columns right of coordinate
        for (int relativeCol = -2; relativeCol <= 2; relativeCol++) {
            // If relativeCol is 0, continue.
            if (relativeCol == 0) {
                continue;
            }

            // Make two possible coordinates, one up and one down on current column.
            Move possibleMoveUp = new Move(this, coordinate,
                    new ChessCoordinate(coordinate.getColumn() + relativeCol,
                            coordinate.getRow() + (3 - Math.abs(relativeCol))), Move.NORMAL_MOVE);
            Move possibleMoveDown = new Move(this, coordinate,
                    new ChessCoordinate(coordinate.getColumn() + relativeCol,
                            coordinate.getRow() - (3 - Math.abs(relativeCol))), Move.NORMAL_MOVE);
            // If possible coordinates are in bounds, add them to possibleMoves.
            if (possibleMoveUp.getEndingCoordinate().isInBounds()
                    && (gameModel.getBoardModel().getPieceOnSquare(possibleMoveUp.getEndingCoordinate()) == null ||
                    gameModel.getBoardModel().getPieceOnSquare(possibleMoveUp.getEndingCoordinate()).getColor() != color)) {
                possibleMoves.add(possibleMoveUp);
            }
            if (possibleMoveDown.getEndingCoordinate().isInBounds()
                    && (gameModel.getBoardModel().getPieceOnSquare(possibleMoveDown.getEndingCoordinate()) == null ||
                    gameModel.getBoardModel().getPieceOnSquare(possibleMoveDown.getEndingCoordinate()).getColor() != color)) {
                possibleMoves.add(possibleMoveDown);
            }
        }

        return possibleMoves;
    }

    @Override
    public String toString() {
        return "Knight";
    }
}
