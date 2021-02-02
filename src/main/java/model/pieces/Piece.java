package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.Move;

import java.util.Set;

public abstract class Piece {

    protected static final MoveMaker STANDARD_MOVE_MAKER = (startCoordinate, endCoordinate, movingPiece, game) ->
            new Move(endCoordinate, movingPiece, null, game.getBoard().getPieceOn(endCoordinate));

    protected Set<MovementRule> movementRules;
    protected ChessCoordinate coordinate;
    protected final char color;
    protected boolean hasMoved = false;

    public Piece(ChessCoordinate coordinate, char color) {
        this(Set.of(), coordinate, color);
    }

    public Piece(Set<MovementRule> movementRules, ChessCoordinate coordinate, char color) {
        this.movementRules = movementRules;
        this.coordinate = coordinate;
        this.color = color;
    }

    public char getColor() {
        return color;
    }

    public ChessCoordinate getCoordinate() {
        return coordinate;
    }

    public abstract boolean canMoveTo(ChessCoordinate endCoordinate, Piece[][] pieceArray);
}
