package main.java.model;

import main.java.ChessCoordinate;
import main.java.model.pieces.Piece;

import java.util.Objects;

public class Move {

    public static final int NORMAL_MOVE = 0;
    public static final int EN_PASSANT = 1;
    public static final int CASTLING_LEFT = 2;
    public static final int CASTLING_RIGHT = 3;

    private final Piece movedPiece;
    private final Piece capturedPiece;
    private final ChessCoordinate startingCoordinate;
    private final ChessCoordinate endingCoordinate;
    private final int typeOfMove; // 0-Normal 1-En Passant 2-Castling

    public Move(Piece movedPiece, Piece capturedPiece, ChessCoordinate startingCoordinate, ChessCoordinate endingCoordinate, int typeOfMove) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.startingCoordinate = startingCoordinate;
        this.endingCoordinate = endingCoordinate;
        this.typeOfMove = typeOfMove;
    }

    public boolean isLegal(BoardModel boardModel) {
        boolean isLegal = true;
        boardModel.makeMove(this);
        if ((movedPiece.getColor() == 0) ? boardModel.getWhiteKing().isAttacked() : boardModel.getBlackKing().isAttacked()) {
            isLegal = false;
        }
        boardModel.undoMove(this);
        return isLegal;
    }

    public int getTypeOfMove() {
        return typeOfMove;
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
        return typeOfMove == move.typeOfMove &&
                movedPiece.equals(move.movedPiece) &&
                startingCoordinate.equals(move.startingCoordinate) &&
                endingCoordinate.equals(move.endingCoordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movedPiece, startingCoordinate, endingCoordinate, typeOfMove);
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
