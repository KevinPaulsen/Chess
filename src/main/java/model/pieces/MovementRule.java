package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.BoardModel;
import main.java.model.GameModel;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all the information to describe movement of a ChessPiece.
 */
public class MovementRule {

    private final Direction direction;
    private final int maxDistance;
    private final char color;
    private final MoveMaker moveMaker;

    public MovementRule(Direction direction, int maxDistance, char color, MoveMaker moveMaker) {
        this.direction = direction;
        this.maxDistance = maxDistance;
        this.color = color;
        this.moveMaker = moveMaker;
    }

    public List<Move> getMoves(ChessCoordinate coordinate, GameModel gameModel) {
        List<Move> possibleMoves = new ArrayList<>();
        BoardModel boardModel = gameModel.getBoard();
        Piece movingPiece = boardModel.getPieceOn(coordinate);
        int distance = 1;

        for (ChessCoordinate endCoordinate = direction.next(coordinate);
             endCoordinate != null && distance <= maxDistance;
             endCoordinate = direction.next(endCoordinate), distance++) {

            Piece occupyingPiece = boardModel.getPieceOn(coordinate);
            if (occupyingPiece != null && occupyingPiece.color == color) {
                break;
            }

            possibleMoves.add(moveMaker.getMove(coordinate, endCoordinate, movingPiece, gameModel));
        }

        return possibleMoves;
    }
}
