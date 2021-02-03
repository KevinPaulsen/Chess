package main.java;

import main.java.model.pieces.Piece;

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
}
