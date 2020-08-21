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
        }
    }

    public BoardModel getBoardModel() {
        return boardModel;
    }

    public Move getLegalMove(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        Piece movingPiece = boardModel.getPieceOnSquare(startCoordinate);
        if (movingPiece.getColor() != turn % 2) {
            return null;
        }

        ArrayList<Move> possibleMoves = movingPiece.getPossibleMoves(this);
        Move normalMove = new Move(movingPiece, startCoordinate, endCoordinate, Move.NORMAL_MOVE);
        Move enPassantMove = new Move(movingPiece, startCoordinate, endCoordinate, Move.EN_PASSANT);
        Move castlingLeftMove = new Move(movingPiece, startCoordinate, endCoordinate, Move.CASTLING_LEFT);
        Move castlingRightMove = new Move(movingPiece, startCoordinate, endCoordinate, Move.CASTLING_RIGHT);

        if (possibleMoves.contains(normalMove)) {
            return normalMove;
        } else if (possibleMoves.contains(enPassantMove)) {
            return enPassantMove;
        } else if (possibleMoves.contains(castlingLeftMove)) {
            return castlingLeftMove;
        } else if (possibleMoves.contains(castlingRightMove)) {
            return castlingRightMove;
        }
        return null;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }
}
