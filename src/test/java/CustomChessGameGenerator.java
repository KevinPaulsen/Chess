package test.java;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.GameModel;
import main.java.model.SquareModel;
import main.java.model.pieces.Bishop;
import main.java.model.pieces.King;
import main.java.model.pieces.Knight;
import main.java.model.pieces.Pawn;
import main.java.model.pieces.Piece;
import main.java.model.pieces.Queen;
import main.java.model.pieces.Rook;

public class CustomChessGameGenerator {

    public static final int WHITE_PAWN = 0;
    public static final int WHITE_ROOK = 1;
    public static final int WHITE_KNIGHT = 2;
    public static final int WHITE_BISHOP = 3;
    public static final int WHITE_QUEEN = 4;
    public static final int WHITE_KING = 5;
    public static final int BLACK_PAWN = 6;
    public static final int BLACK_ROOK = 7;
    public static final int BLACK_KNIGHT = 8;
    public static final int BLACK_BISHOP = 9;
    public static final int BLACK_QUEEN = 10;
    public static final int BLACK_KING = 11;

    public static GameModel makeGameModel(int [][] pieceBoard) {
        if (pieceBoard.length != 8 || pieceBoard[0].length != 8) {
            return null;
        }
        SquareModel[][] squares = new SquareModel[8][8];
        King whiteKing = null;
        King blackKing = null;

        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                Piece thisPiece = getPiece(pieceBoard[col][row], col, row);
                squares[col][row] = new SquareModel((col + row) % 2, thisPiece);
                if (thisPiece instanceof King) {
                    if (thisPiece.getColor() == 0) {
                        whiteKing = (King) thisPiece;
                    } else {
                        blackKing = (King) thisPiece;
                    }
                }
            }
        }
        return new GameModel(new BoardModel(squares, whiteKing, blackKing));
    }

    private static Piece getPiece(int pieceIdx, int col, int row) {
        switch (pieceIdx) {
            case WHITE_PAWN:
                return new Pawn((byte) 0, new ChessCoordinate(col, row));
            case WHITE_ROOK:
                return new Rook((byte) 0, new ChessCoordinate(col, row));
            case WHITE_KNIGHT:
                return new Knight((byte) 0, new ChessCoordinate(col, row));
            case WHITE_BISHOP:
                return new Bishop((byte) 0, new ChessCoordinate(col, row));
            case WHITE_QUEEN:
                return new Queen((byte) 0, new ChessCoordinate(col, row));
            case WHITE_KING:
                return new King((byte) 0, new ChessCoordinate(col, row));
            case BLACK_PAWN:
                return new Pawn((byte) 1, new ChessCoordinate(col, row));
            case BLACK_ROOK:
                return new Rook((byte) 1, new ChessCoordinate(col, row));
            case BLACK_KNIGHT:
                return new Knight((byte) 1, new ChessCoordinate(col, row));
            case BLACK_BISHOP:
                return new Bishop((byte) 1, new ChessCoordinate(col, row));
            case BLACK_QUEEN:
                return new Queen((byte) 1, new ChessCoordinate(col, row));
            case BLACK_KING:
                return new King((byte) 1, new ChessCoordinate(col, row));
        }
        return null;
    }


}
