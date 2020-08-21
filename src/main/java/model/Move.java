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
    private final ChessCoordinate startingCoordinate;
    private final ChessCoordinate endingCoordinate;
    private final int typeOfMove; // 0-Normal 1-En Passant 2-Castling

    public Move(Piece movedPiece, ChessCoordinate startingCoordinate, ChessCoordinate endingCoordinate, int typeOfMove) {
        this.movedPiece = movedPiece;
        this.startingCoordinate = startingCoordinate;
        this.endingCoordinate = endingCoordinate;
        this.typeOfMove = typeOfMove;
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
        return movedPiece.toString() + " to " + endingCoordinate.toString();
    }
}
