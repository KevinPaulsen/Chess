package main.java.model;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.pieces.King;
import main.java.model.pieces.Piece;

public class BoardModel {

    // The array the hold all the pieces. They are stored in the format [file][rank]
    private final Piece[][] pieceArray;

    private King whiteKing;
    private King blackKing;

    private static final ChessCoordinate[][] chessCoordinates = createChessCoordinates();

    public BoardModel(Piece[][] pieceArray) {
        this.pieceArray = pieceArray;
        initKings();
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
        if (move != null) {
            setPiece(move.getStartingCoordinate(), null);
            setPiece(move.getInteractingPieceStart(), null);
            setPiece(move.getEndingCoordinate(), move.getMovingPiece());
            setPiece(move.getInteractingPieceEnd(), move.getInteractingPiece());
            move.getMovingPiece().moveTo(move.getEndingCoordinate());
            if (move.getInteractingPiece() != null) {
                move.getInteractingPiece().moveTo(move.getInteractingPieceEnd());
            }
        }
    }

    public void undoMove(Move move) {
        if (move != null) {
            setPiece(move.getEndingCoordinate(), null);
            setPiece(move.getInteractingPieceEnd(), null);
            setPiece(move.getStartingCoordinate(), move.getMovingPiece());
            setPiece(move.getInteractingPieceStart(), move.getInteractingPiece());
            move.getMovingPiece().moveBackTo(move.getStartingCoordinate());
            if (move.getInteractingPiece() != null) {
                move.getInteractingPiece().moveBackTo(move.getInteractingPieceStart());
            }
        }
    }

    public boolean kingInCheck(char color) {
        if (color == 'w') {
            return whiteKing.isAttacked(whiteKing.getCoordinate(), this);
        } else {
            return blackKing.isAttacked(blackKing.getCoordinate(), this);
        }
    }

    public Piece getPieceOn(ChessCoordinate coordinate) {
        return coordinate != null ? pieceArray[coordinate.getFile()][coordinate.getRank()] : null;
    }

    private void setPiece(ChessCoordinate coordinate, Piece piece) {
        if (coordinate != null) {
            pieceArray[coordinate.getFile()][coordinate.getRank()] = piece;
        }
    }

    public static ChessCoordinate getChessCoordinate(int file, int rank) {
        return ChessCoordinate.isInBounds(file, rank) ? chessCoordinates[file][rank] : null;
    }

    private static ChessCoordinate[][] createChessCoordinates() {
        ChessCoordinate[][] chessCoordinates = new ChessCoordinate[8][8];
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                chessCoordinates[file][rank] = new ChessCoordinate(file, rank);
            }
        }
        return chessCoordinates;
    }

    private void initKings() {
        for (Piece[] file : pieceArray) {
            for (Piece piece : file) {
                if (piece instanceof King) {
                    if (piece.getColor() == 'w') {
                        whiteKing = (King) piece;
                    } else {
                        blackKing = (King) piece;
                    }
                }
            }
        }
    }
}
