package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.Move;

import java.util.ArrayList;

public abstract class LongMovingPiece extends Piece {

    LongMovingPiece(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    /**
     * Finds all legal moves in one direction. Directions
     * can be given by setting the row and col directions to
     * and integer between 1 and -1.
     *
     * @param colDirection the column offset when moving one space.
     * @param rowDirection the Row offset when moving one space.
     * @param board        the Model that contains the board data.
     * @return Arraylist of all moves along that path.
     */
    protected ArrayList<Move> getMovesAlongDirection(int colDirection, int rowDirection, BoardModel board) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

        // Make possible move in specified direction
        ChessCoordinate endingCoordinate = new ChessCoordinate(coordinate.getColumn() + colDirection,
                coordinate.getRow() + rowDirection);
        Move possibleMove = new Move(this, board.getPieceOnSquare(endingCoordinate), coordinate,
                endingCoordinate, Move.NORMAL_MOVE);

        // While the possible is still in bounds, keep looping.
        while (possibleMove.getEndingCoordinate().isInBounds()) {
            // If The desired ending coordinate has a piece either capture, or break.
            if (board.getPieceOnSquare(possibleMove.getEndingCoordinate()) != null) {
                if (possibleMove.getCapturedPiece().getColor() != color) {
                    if (possibleMove.isLegal(board)) {
                        possibleMoves.add(possibleMove);
                    }
                }
                break;
            }
            // Add move to list, then get next move along the specified direction.
            possibleMoves.add(possibleMove);
            endingCoordinate = new ChessCoordinate(possibleMove.getEndingCoordinate().getColumn() + colDirection,
                    possibleMove.getEndingCoordinate().getRow() + rowDirection);
            possibleMove = new Move(this, board.getPieceOnSquare(endingCoordinate), coordinate,
                    endingCoordinate, Move.NORMAL_MOVE);
        }
        return possibleMoves;
    }
}
