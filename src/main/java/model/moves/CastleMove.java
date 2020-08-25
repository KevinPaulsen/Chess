package main.java.model.moves;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.pieces.Piece;
import main.java.model.pieces.Rook;

public class CastleMove extends Move {

    public CastleMove(Piece movedPiece, Piece capturedPiece, ChessCoordinate startingCoordinate,
                      ChessCoordinate endingCoordinate) {
        super(movedPiece, capturedPiece, startingCoordinate, endingCoordinate);
    }

    @Override
    public boolean makeMove(BoardModel boardModel) {
        if (!super.makeMove(boardModel)) {
            return false;
        }

        int rookStartingColumn = (endingCoordinate.getColumn() == 2) ? 0 : 7;
        int rookEndingColumn = (endingCoordinate.getColumn() == 2) ? 3 : 5;
        moveRook(boardModel, rookStartingColumn, rookEndingColumn, startingCoordinate.getRow());

        return true;
    }

    @Override
    public boolean undoMove(BoardModel boardModel) {
        if (!super.undoMove(boardModel)) {
            return false;
        }

        int rookStartingColumn = (endingCoordinate.getColumn() == 2) ? 0 : 7;
        int rookEndingColumn = (endingCoordinate.getColumn() == 2) ? 3 : 5;
        moveRook(boardModel, rookEndingColumn, rookStartingColumn, startingCoordinate.getRow());

        return true;
    }

    private void moveRook(BoardModel boardModel, int startCol, int endCol, int row) {
        Rook rook = (Rook) boardModel.getBoard()[startCol][row].getPiece();
        rook.setCoordinate(new ChessCoordinate(endCol, row));
        boardModel.getBoard()[endCol][row].setPiece(rook);
        boardModel.getBoard()[startCol][row].setPiece(null);
    }
}
