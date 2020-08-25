package main.java.model.moves;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.pieces.Piece;

import java.util.Objects;

public class Move {

    protected final Piece movedPiece;
    protected final Piece capturedPiece;
    protected final ChessCoordinate startingCoordinate;
    protected final ChessCoordinate endingCoordinate;

    public Move() {
        movedPiece = null;
        capturedPiece = null;
        startingCoordinate = null;
        endingCoordinate = null;
    }

    public Move(Piece movedPiece, Piece capturedPiece, ChessCoordinate startingCoordinate, ChessCoordinate endingCoordinate) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.startingCoordinate = startingCoordinate;
        this.endingCoordinate = endingCoordinate;
    }

    public boolean makeMove(BoardModel boardModel) {
        if (movedPiece == null || startingCoordinate == null || endingCoordinate == null) {
            return false;
        }
        // If move captures a piece, remove the piece.
        if (capturedPiece != null) {
            boardModel.getBoard()[capturedPiece.getCoordinate().getColumn()][capturedPiece.getCoordinate().getRow()]
                    .setPiece(null);
            (movedPiece.getColor() == 1 ? boardModel.getWhitePieces() : boardModel.getBlackPieces())
                    .remove(capturedPiece);
        }

        // Move the piece on the board
        movedPiece.moveTo(endingCoordinate);
        boardModel.getBoard()[endingCoordinate.getColumn()][endingCoordinate.getRow()].setPiece(movedPiece);
        boardModel.getBoard()[startingCoordinate.getColumn()][startingCoordinate.getRow()].setPiece(null);

        boardModel.getWhiteKing().updateAttacked(boardModel, this);
        boardModel.getBlackKing().updateAttacked(boardModel, this);

        return true;
    }

    public boolean undoMove(BoardModel boardModel) {
        if (movedPiece == null || startingCoordinate == null || endingCoordinate == null) {
            return false;
        }

        // Move piece to starting pos
        movedPiece.moveBackTo(startingCoordinate);
        boardModel.getBoard()[startingCoordinate.getColumn()][startingCoordinate.getRow()].setPiece(movedPiece);
        boardModel.getBoard()[endingCoordinate.getColumn()][endingCoordinate.getRow()].setPiece(null);

        if (capturedPiece != null) {
            boardModel.getBoard()[capturedPiece.getCoordinate().getColumn()][capturedPiece.getCoordinate().getRow()]
                    .setPiece(capturedPiece);
            (movedPiece.getColor() == 1 ? boardModel.getWhitePieces() : boardModel.getBlackPieces())
                    .add(capturedPiece);
        }

        boardModel.getWhiteKing().updateAttacked(boardModel, this);
        boardModel.getBlackKing().updateAttacked(boardModel, this);

        return true;
    }

    public boolean isLegal(BoardModel boardModel) {
        boolean isLegal = true;
        makeMove(boardModel);
        assert movedPiece != null;
        if ((movedPiece.getColor() == 0) ? boardModel.getWhiteKing().isAttacked() : boardModel.getBlackKing().isAttacked()) {
            isLegal = false;
        }
        undoMove(boardModel);
        return isLegal;
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    public ChessCoordinate getStartingCoordinate() {
        return startingCoordinate;
    }

    public ChessCoordinate getEndingCoordinate() {
        return endingCoordinate;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return movedPiece.equals(move.movedPiece) &&
                startingCoordinate.equals(move.startingCoordinate) &&
                endingCoordinate.equals(move.endingCoordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movedPiece, startingCoordinate, endingCoordinate);
    }

    @Override
    public String toString() {
        if (capturedPiece == null) {
            return movedPiece.getShortString() + endingCoordinate.toString();
        } else {
            return movedPiece.getShortString() + "x" + endingCoordinate.toString() + " (" + capturedPiece.getShortString() + ")";
        }
    }
}
