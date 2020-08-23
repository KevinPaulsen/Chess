package main.java.model;

import main.java.ChessCoordinate;
import main.java.model.pieces.Piece;

import java.util.ArrayList;

public class GameModel {

    private final BoardModel boardModel;
    private final ArrayList<Move> moves;
    int turn = 0;
    boolean isOver = false;
    int winner = -1;

    public GameModel() {
        boardModel = new BoardModel();
        moves = new ArrayList<>();
    }

    public GameModel(BoardModel boardModel) {
        this.boardModel = boardModel;
        moves = new ArrayList<>();
    }


    public boolean move(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        if (isOver) {
            return false;
        }
        Move legalMove = getLegalMove(startCoordinate, endCoordinate);
        if (legalMove != null) {
            moves.add(legalMove);
            boardModel.makeMove(legalMove);
            turn++;

            // Check if checkmate
            if (getAllLegalMoves().size() == 0) {
                isOver = true;
                winner = (turn % 2 == 1) ? 0 : 1;
            }
            return true;
        }
        return false;
    }

    public void move(Move move) {
        if (!isOver && move != null && move.isLegal(boardModel)) {
            moves.add(move);
            boardModel.makeMove(move);
            turn++;

            if (getAllLegalMoves().size() == 0) {
                isOver = true;
                winner = (turn % 2 == 1) ? 0 : 1;
            }
        }
    }

    public void undoMove(Move move) {
        if (move != null) {
            moves.remove(move);
            boardModel.undoMove(move);
            turn--;

            isOver = false;
        }
    }

    public BoardModel getBoardModel() {
        return boardModel;
    }

    /**
     * Takes in two coordinates, one indicating which square the moving
     * piece starts on, and the other where that piece ends on. This
     * Method then attempts to find a legal move that piece can make to
     * land on that square. This method is in charge of making sure the
     * move follows all of the rules of the game.
     *
     * @param startCoordinate thee coordinate the moving piece starts on.
     * @param endCoordinate the coordinate the moving piece ends on.
     * @return a move object that contains all relevant information on the legal move.
     */
    public Move getLegalMove(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        Piece movingPiece = boardModel.getPieceOnSquare(startCoordinate);

        for (Move move : movingPiece.getPossibleMoves(this)) {
            if (move.getEndingCoordinate().equals(endCoordinate)) {
                return move;
            }
        }
        return null;
    }

    public ArrayList<Move> getAllLegalMoves() {
        ArrayList<Move> moves = new ArrayList<>();

        for (Piece piece : boardModel.getWhitePieces()) {
            moves.addAll(piece.getPossibleMoves(this));
        }
        for (Piece piece : boardModel.getBlackPieces()) {
            moves.addAll(piece.getPossibleMoves(this));
        }
        return moves;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }

    public int getTurn() {
        return turn;
    }

    public boolean isOver() {
        return isOver;
    }

    public int getWinner() {
        return winner;
    }
}
