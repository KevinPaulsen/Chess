package main.java.model;

import main.java.ChessCoordinate;
import main.java.model.pieces.Piece;

import java.util.ArrayList;

public class GameModel {

    private final BoardModel boardModel;
    private final ArrayList<Move> moves;
    int turn = 0;

    public GameModel() {
        boardModel = new BoardModel();
        moves = new ArrayList<>();
    }

    public GameModel(BoardModel boardModel) {
        this.boardModel = boardModel;
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

        // Find what kind of move it is (normal or special)
        ArrayList<Move> possibleMoves = movingPiece.getPossibleMoves(this);
        Move idealMove = null;
        for (int moveType = 0; moveType < 4; moveType ++) {
            Move attemptedMove = (moveType != Move.EN_PASSANT) ?
                    new Move(movingPiece, boardModel.getPieceOnSquare(endCoordinate), startCoordinate, endCoordinate,
                            moveType) : new Move(movingPiece, boardModel.getPieceOnSquare(
                                    new ChessCoordinate(endCoordinate.getColumn(), endCoordinate.getRow()
                                            + (turn == 0 ? 1 : -1))), startCoordinate, endCoordinate, moveType);
            if (possibleMoves.contains(attemptedMove)) {
                idealMove = attemptedMove;
                break;
            }
        }

        // Attempt to make the move, then check if the king is being attacked at the end of the move.
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
