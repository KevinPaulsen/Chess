package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.GameModel;
import main.java.model.moves.Move;

import java.util.ArrayList;

public class Knight extends Piece {

    public Knight(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<Move> getPossibleMoves(GameModel gameModel) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        if (gameModel.getTurn() % 2 != color) {
            return possibleMoves;
        }

        // Loop from 2 columns left of coordinate to two columns right of coordinate
        for (int relativeCol = -2; relativeCol <= 2; relativeCol++) {
            // If relativeCol is 0, continue.
            if (relativeCol == 0) {
                continue;
            }

            // Make two possible coordinates, one up and one down on current column.
            for (int direction = -1; direction <= 1; direction += 2) {
                ChessCoordinate endingCoordinate = new ChessCoordinate(coordinate.getColumn() + relativeCol,
                        coordinate.getRow() + (direction * (3 - Math.abs(relativeCol))));
                Move possibleMove = new Move(this, gameModel.getBoardModel().getPieceOnSquare(endingCoordinate), coordinate, endingCoordinate);
                if (possibleMove.getEndingCoordinate().isInBounds()
                        && (possibleMove.getCapturedPiece() == null
                        || possibleMove.getCapturedPiece().getColor() != color)) {
                    if (possibleMove.isLegal(gameModel.getBoardModel())) {
                        possibleMoves.add(possibleMove);
                    }
                }
            }
        }

        return possibleMoves;
    }

    @Override
    public double getValue() {
        return 3;
    }

    @Override
    public String getShortString() {
        return "N";
    }

    @Override
    public String toString() {
        return "Knight{" +
                "color=" + color +
                ", timesMoved=" + timesMoved +
                ", coordinate=" + coordinate +
                '}';
    }
}
