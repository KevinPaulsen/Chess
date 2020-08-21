package main.java.model;

import main.java.ChessCoordinate;
import main.java.model.pieces.*;

public class GameModel {

    private final BoardModel boardModel;
    int turn = 0;

    public GameModel() {
        boardModel = new BoardModel();
    }


    public void move(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        boardModel.movePiece(startCoordinate, endCoordinate);
    }

    public BoardModel getBoardModel() {
        return boardModel;
    }

    public boolean canMakeMove(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        return boardModel.getPieceOnSquare(startCoordinate).getPossibleMoves().contains(endCoordinate);
    }
}
