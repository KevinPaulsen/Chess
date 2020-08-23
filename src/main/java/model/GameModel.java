package main.java.model;

import main.java.ChessCoordinate;
import main.java.model.pieces.Piece;

import java.util.ArrayList;

public class GameModel {

    private final BoardModel boardModel;
    private final ArrayList<Move> moves;
    int turn = 0;
    boolean isOver = false;

    public GameModel() {
        boardModel = new BoardModel();
        moves = new ArrayList<>();
    }

    public GameModel(BoardModel boardModel) {
        this.boardModel = boardModel;
        moves = new ArrayList<>();
    }


    public void move(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        if (isOver) {
            return;
        }
        Move legalMove = getLegalMove(startCoordinate, endCoordinate);
        if (legalMove != null) {
            moves.add(legalMove);
            boardModel.makeMove(legalMove);
            turn++;

            // Check if checkmate
            if (getAllLegalMoves().size() == 0) {
                isOver = true;
            }
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

        // Check that it is our turn
        if (movingPiece.getColor() != turn % 2) {
            return null;
        }

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
}
