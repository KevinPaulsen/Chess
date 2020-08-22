package main.java.model;

import main.java.ChessCoordinate;
import main.java.model.pieces.Piece;

import java.util.ArrayList;

public class GameModel {

    private final ArrayList<Move> moves;
    private final BoardModel boardModel;
    int turn = 0;

    public GameModel() {
        boardModel = new BoardModel();
        moves = new ArrayList<>();
    }


    public void move(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        Move legalMove = getLegalMove(startCoordinate, endCoordinate);
        if (legalMove != null) {
            moves.add(legalMove);
            boardModel.makeMove(legalMove);
            turn++;

            // Check if checkmate
        }
    }

    public BoardModel getBoardModel() {
        return boardModel;
    }

    public Move getLegalMove(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        Piece movingPiece = boardModel.getPieceOnSquare(startCoordinate);
        // Check that it is our turn
        if (movingPiece.getColor() != turn % 2) {
            return null;
        }

        ArrayList<Move> possibleMoves = movingPiece.getPossibleMoves(this);
        Move idealMove = null;
        for (int moveType = 0; moveType < 4; moveType ++) {
            Move attemptedMove = new Move(movingPiece, boardModel.getPieceOnSquare(endCoordinate), startCoordinate,
                    endCoordinate, moveType);
            if (possibleMoves.contains(attemptedMove)) {
                idealMove = attemptedMove;
                break;
            }
        }

        boardModel.makeMove(idealMove);
        if ((turn % 2 == 0) ? boardModel.getWhiteKing().isAttacked() : boardModel.getBlackKing().isAttacked()) {
            boardModel.undoMove(idealMove);
            return null;
        }
        boardModel.undoMove(idealMove);

        return idealMove;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }
}
