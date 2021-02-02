package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.GameModel;

public interface MoveMaker {

    Move getMove(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate, Piece movingPiece,
                 GameModel model);
}
