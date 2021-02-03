package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.GameModel;

/**
 * this interface is capable of creating a move given a startCoordinate,
 * endCoordinate, movingPiece and GameModel.
 */
public interface MoveMaker {

    /**
     * Creates a move with a startCoordinate, endCoordinate, movingPiece,
     * and Game model. If no move is possible that meets the required
     * information, then null is returned.
     *
     * @param startCoordinate the starting coordinate of the moving Piece
     * @param endCoordinate the ending coordinate of the moving Piece
     * @param model the gameModel containing the moving Piece
     * @param promotionCode the code to promote to {@see Pawn}
     * @return a Move that makes the requested move
     */
    Move getMove(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate, GameModel model, int promotionCode);
}
