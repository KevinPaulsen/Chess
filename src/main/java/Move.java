package main.java;

import main.java.model.BoardModel;
import main.java.model.pieces.Pawn;
import main.java.model.pieces.Piece;

import java.util.Objects;

public class Move {

    private final ChessCoordinate startingCoordinate;
    private final ChessCoordinate endingCoordinate;
    private final Piece movingPiece;
    private final ChessCoordinate interactingPieceStart;
    private final ChessCoordinate interactingPieceEnd;
    private final Piece interactingPiece;
    private final Piece promotedPiece;

    public Move(ChessCoordinate endingCoordinate, Piece movingPiece,
                ChessCoordinate interactingPieceEnd, Piece interactingPiece) {
        this(endingCoordinate, movingPiece, interactingPieceEnd, interactingPiece, null);
    }

    public Move(ChessCoordinate endingCoordinate, Piece movingPiece,
                ChessCoordinate interactingPieceEnd, Piece interactingPiece, Piece promotedPiece) {
        this.startingCoordinate = movingPiece.getCoordinate();
        this.endingCoordinate = endingCoordinate;
        this.movingPiece = movingPiece;
        this.interactingPieceStart = interactingPiece == null ? null : interactingPiece.getCoordinate();
        this.interactingPieceEnd = interactingPieceEnd;
        this.interactingPiece = interactingPiece;
        this.promotedPiece = promotedPiece;
    }

    public Move(Move move) {
        this.startingCoordinate = move.startingCoordinate;
        this.endingCoordinate = move.endingCoordinate;
        this.movingPiece = Piece.clone(move.movingPiece);
        this.interactingPieceStart = move.interactingPieceStart;
        this.interactingPieceEnd = move.interactingPieceEnd;
        this.interactingPiece = Piece.clone(move.interactingPiece);
        this.promotedPiece = Piece.clone(move.promotedPiece);
    }

    public Move clone() {
        return new Move(this);
    }

    public boolean doesPromote() {
        return promotedPiece != null;
    }

    public ChessCoordinate getStartingCoordinate() {
        return startingCoordinate;
    }

    public ChessCoordinate getEndingCoordinate() {
        return endingCoordinate;
    }

    public Piece getMovingPiece() {
        return movingPiece;
    }

    public ChessCoordinate getInteractingPieceStart() {
        return interactingPieceStart;
    }

    public ChessCoordinate getInteractingPieceEnd() {
        return interactingPieceEnd;
    }

    public Piece getInteractingPiece() {
        return interactingPiece;
    }

    public Piece getPromotedPiece() {
        return promotedPiece;
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();

        if (interactingPiece != null && interactingPieceEnd != null) {
            // Castle
            if (endingCoordinate.getFile() == 6) {
                result.append("0-0");
            } else {
                result.append("0-0-0");
            }
        } else {
            // Pawn
            if (movingPiece instanceof Pawn) {
                if (interactingPiece != null) {
                    result.append(startingCoordinate.getCharFile());
                }
            }

            result.append(movingPiece.toString());
            if (interactingPiece != null) {
                result.append("x");
            }
            result.append(endingCoordinate.toString());
        }

        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return Objects.equals(startingCoordinate, move.startingCoordinate)
                && Objects.equals(endingCoordinate, move.endingCoordinate)
                && Objects.equals(movingPiece, move.movingPiece)
                && Objects.equals(interactingPieceStart, move.interactingPieceStart)
                && Objects.equals(interactingPieceEnd, move.interactingPieceEnd)
                && Objects.equals(interactingPiece, move.interactingPiece)
                && Objects.equals(promotedPiece, move.promotedPiece);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startingCoordinate, endingCoordinate, movingPiece, interactingPieceStart, interactingPieceEnd, interactingPiece, promotedPiece);
    }
}
