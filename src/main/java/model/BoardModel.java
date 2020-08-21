package main.java.model;

import main.java.ChessCoordinate;
import main.java.model.pieces.Bishop;
import main.java.model.pieces.King;
import main.java.model.pieces.Knight;
import main.java.model.pieces.Pawn;
import main.java.model.pieces.Piece;
import main.java.model.pieces.Queen;
import main.java.model.pieces.Rook;

public class BoardModel {

    private static final int width = 8;
    private static final int height = 8;

    private final SquareModel[][] board = new SquareModel[width][height];

    public BoardModel() {
        for (int width = 0; width < BoardModel.width; width++) {
            for (int height = 0; height < BoardModel.height; height++) {
                board[width][height] = new SquareModel((width + height) % 2);
            }
        }
        initializeBoard();
    }

    public void makeMove(Move move) {
        Piece movingPiece = move.getMovedPiece();
        movingPiece.moveTo(move.getEndingCoordinate());

        board[move.getEndingCoordinate().getColumn()][move.getEndingCoordinate().getRow()].setPiece(movingPiece);
        board[move.getStartingCoordinate().getColumn()][move.getStartingCoordinate().getRow()].setPiece(null);
        if (move.getTypeOfMove() == Move.EN_PASSANT) {
            board[move.getEndingCoordinate().getColumn()][move.getStartingCoordinate().getRow()].setPiece(null);
        } else if (move.getTypeOfMove() == Move.CASTLING_LEFT) {
            Rook leftRook = (Rook) board[0][move.getStartingCoordinate().getRow()].getPiece();
            makeMove(new Move(leftRook, leftRook.getCoordinate(),
                    new ChessCoordinate(3, leftRook.getCoordinate().getRow()), Move.NORMAL_MOVE));
        } else if (move.getTypeOfMove() == Move.CASTLING_RIGHT) {
            Rook rightRook = (Rook) board[7][move.getStartingCoordinate().getRow()].getPiece();
            makeMove(new Move(rightRook, rightRook.getCoordinate(),
                    new ChessCoordinate(5, rightRook.getCoordinate().getRow()), Move.NORMAL_MOVE));
        }
    }

    public void setPieceOnSquare(Piece piece) {
        board[piece.getCoordinate().getColumn()][piece.getCoordinate().getRow()].setPiece(piece);
    }

    public Piece getPieceOnSquare(ChessCoordinate coordinate) {
        return board[coordinate.getColumn()][coordinate.getRow()].getPiece();
    }

    /**
     * Set up board with the starting positions of each piece.
     */
    private void initializeBoard() {
        //Pawns
        for (int column = 0; column < BoardModel.getWidth(); column++) {
            setPieceOnSquare(new Pawn((byte) 0, new ChessCoordinate(column, 1)));
            setPieceOnSquare(new Pawn((byte) 1, new ChessCoordinate(column, 6)));
        }

        // Rooks
        setPieceOnSquare(new Rook((byte) 0, new ChessCoordinate(0, 0)));
        setPieceOnSquare(new Rook((byte) 0, new ChessCoordinate(7, 0)));
        setPieceOnSquare(new Rook((byte) 1, new ChessCoordinate(0, 7)));
        setPieceOnSquare(new Rook((byte) 1, new ChessCoordinate(7, 7)));

        // Knights
        setPieceOnSquare(new Knight((byte) 0, new ChessCoordinate(1, 0)));
        setPieceOnSquare(new Knight((byte) 0, new ChessCoordinate(6, 0)));
        setPieceOnSquare(new Knight((byte) 1, new ChessCoordinate(1, 7)));
        setPieceOnSquare(new Knight((byte) 1, new ChessCoordinate(6, 7)));

        // Bishops
        setPieceOnSquare(new Bishop((byte) 0, new ChessCoordinate(2, 0)));
        setPieceOnSquare(new Bishop((byte) 0, new ChessCoordinate(5, 0)));
        setPieceOnSquare(new Bishop((byte) 1, new ChessCoordinate(2, 7)));
        setPieceOnSquare(new Bishop((byte) 1, new ChessCoordinate(5, 7)));

        // Queens
        setPieceOnSquare(new Queen((byte) 0, new ChessCoordinate(3, 0)));
        setPieceOnSquare(new Queen((byte) 1, new ChessCoordinate(3, 7)));

        // Kings
        setPieceOnSquare(new King((byte) 0, new ChessCoordinate(4, 0)));
        setPieceOnSquare(new King((byte) 1, new ChessCoordinate(4, 7)));
    }

    public int getColorOnSquare(int column, int row) {
        return board[column][row].getColor();
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public SquareModel[][] getBoard() {
        return board;
    }
}
