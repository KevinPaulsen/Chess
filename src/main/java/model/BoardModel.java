package main.java.model;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.pieces.Piece;

public class BoardModel {

    // The array the hold all the pieces. They are stored in the format [file][rank]
    private final Piece[][] pieceArray;

    public BoardModel(Piece[][] pieceArray) {
        this.pieceArray = pieceArray;
    }

    public Piece[][] getPieceArray() {
        return pieceArray;
    }

    /**
     * Makes the given move. All pieces will be updated and moved
     * according the information in move. Move is expected to be
     * legal.
     *
     * @param move the move to make. Cannot be null.
     */
    public void move(Move move) {
        setPiece(move.getStartingCoordinate(), null);
        setPiece(move.getInteractingPieceStart(), null);
        setPiece(move.getEndingCoordinate(), move.getMovingPiece());
        setPiece(move.getInteractingPieceEnd(), move.getInteractingPiece());
    }

    private void setPiece(ChessCoordinate coordinate, Piece piece) {
        pieceArray[coordinate.getFile()][coordinate.getRank()] = piece;
    }
}
