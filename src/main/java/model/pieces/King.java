package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.GameModel;
import main.java.model.Move;

import java.util.ArrayList;

public class King extends Piece {

    public King(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<Move> getPossibleMoves(GameModel gameModel) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        for (int relativeRow = -1; relativeRow <= 1; relativeRow++) {
            for (int relativeCol = -1; relativeCol <= 1; relativeCol++) {
                if (relativeRow == 0 && relativeCol == 0) {
                    continue;
                }
                Move possibleMove = new Move(this, coordinate,
                        new ChessCoordinate(coordinate.getColumn() + relativeCol,
                                coordinate.getRow() + relativeRow), Move.NORMAL_MOVE);
                if (possibleMove.getEndingCoordinate().isInBounds()
                        && (gameModel.getBoardModel().getPieceOnSquare(possibleMove.getEndingCoordinate()) == null
                        || gameModel.getBoardModel().getPieceOnSquare(possibleMove.getEndingCoordinate()).getColor() != color)) {
                    possibleMoves.add(possibleMove);
                }
            }
        }
        // Check if can Castle
        if (!hasMoved) {
            BoardModel model = gameModel.getBoardModel();
            // Check Left
            if (model.getPieceOnSquare(new ChessCoordinate(0, coordinate.getRow())) != null
                    && !model.getPieceOnSquare(new ChessCoordinate(0, coordinate.getRow())).hasMoved
                    && model.getPieceOnSquare(new ChessCoordinate(1, coordinate.getRow())) == null
                    && model.getPieceOnSquare(new ChessCoordinate(2, coordinate.getRow())) == null
                    && model.getPieceOnSquare(new ChessCoordinate(3, coordinate.getRow())) == null) {
                possibleMoves.add(new Move(this, coordinate, new ChessCoordinate(2,
                        coordinate.getRow()), Move.CASTLING_LEFT));
            }
            // Check Right
            if (model.getPieceOnSquare(new ChessCoordinate(7, coordinate.getRow())) != null
                    && !model.getPieceOnSquare(new ChessCoordinate(7, coordinate.getRow())).hasMoved
                    && model.getPieceOnSquare(new ChessCoordinate(6, coordinate.getRow())) == null
                    && model.getPieceOnSquare(new ChessCoordinate(5, coordinate.getRow())) == null) {
                possibleMoves.add(new Move(this, coordinate, new ChessCoordinate(6,
                        coordinate.getRow()), Move.CASTLING_RIGHT));
            }
        }

        return possibleMoves;
    }

    @Override
    public String toString() {
        return "King";
    }
}
