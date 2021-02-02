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

    // The direction of this movement rule
    private final Direction direction;
    // The maximum distance the piece can move in this direction
    private final int maxDistance;
    // The moveMaker that creates the move
    private final MoveMaker moveMaker;

    /**
     * Creates a MovementRule with the given direction, maxDistance and moveMaker.
     *
     * @param direction the direction of this movement rule.
     * @param maxDistance the max distance allowed in the given direction.
     * @param moveMaker the MoveMaker to create the move.
     */
    public MovementRule(Direction direction, int maxDistance, MoveMaker moveMaker) {
        this.direction = direction;
        this.maxDistance = maxDistance;
        this.moveMaker = moveMaker;
    }

    /**
     * Calculates and returns the number of moves that satisfy this MovementRule.
     * If no moves are possible, an empty list is returned.
     *
     * @param coordinate the starting coordinate to move from
     * @param gameModel the GameModel that we are moving on.
     * @return the list of moves that satisfy this movement rule.
     */
    public List<Move> getMoves(ChessCoordinate coordinate, GameModel gameModel) {
        List<Move> moves = new ArrayList<>();
        int distance = 1;

        // Loop until end coordinate is out of bounds or distance > max distance.
        for (ChessCoordinate endCoordinate = direction.next(coordinate);
             endCoordinate != null && distance <= maxDistance;
             endCoordinate = direction.next(endCoordinate), distance++) {

            // Create the move
            Move move = moveMaker.getMove(coordinate, endCoordinate,
                    gameModel.getBoard().getPieceOn(coordinate), gameModel);
            // If the move is non-null, add it to moves.
            if (move != null) {
                moves.add(move);
            }

            // If there is a piece in the way, break.
            if (gameModel.getBoard().getPieceOn(endCoordinate) != null) {
                break;
            }
        }

        return moves;
    }
}
