package chess.model;

import chess.model.pieces.Piece;

import static chess.model.pieces.Piece.*;

/**
 * This public class is capable of creating new and custom ChessBoards.
 * Given an array of integers corresponding to the pieces, a new ChessBoard
 * Can be made from it.
 */
public class ChessBoardFactory {

    public static final int EMPTY = -1;
    public static final int W_PAWN = 0;
    public static final int B_PAWN = 1;
    public static final int W_ROOK = 2;
    public static final int B_ROOK = 3;
    public static final int W_KNIGHT = 4;
    public static final int B_KNIGHT = 5;
    public static final int W_BISHOP = 6;
    public static final int B_BISHOP = 7;
    public static final int W_QUEEN = 8;
    public static final int B_QUEEN = 9;
    public static final int W_KING = 10;
    public static final int B_KING = 11;

    // Normal chess board
    private static final int[][] normalBoard = {
            {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK},
            {W_KNIGHT, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KNIGHT},
            {W_BISHOP, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_BISHOP},
            {W_QUEEN, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_QUEEN},
            {W_KING, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KING},
            {W_BISHOP, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_BISHOP},
            {W_KNIGHT, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KNIGHT},
            {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK},
    };

    public static final int[][] TEST_BOARD = {
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {W_KING, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, B_KING},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY}
    };

    /**
     * Creates a standard chess board with all the pieces in their
     * original positions.
     *
     * @return the ChessBoard with all pieces in their starting positions.
     */
    public static BoardModel createNormalBoard() {
        return createChessBoard(normalBoard);
    }

    /**
     * Creates a new ChessBoard with a given integer array. The board is
     * created by files. The position at [0][0] corresponds to A1, and the
     * position at [0][7] corresponds to A8 etc..
     *
     * @throws IllegalArgumentException if 'board' is not 8x8.
     * @param board any non-null integer array that is 8x8.
     * @return a ChessBoard from the given 'board'
     */
    public static BoardModel createChessBoard(int[][] board) {
        if (board.length != 8 || board[0].length != 8) {
            throw new IllegalArgumentException("Board must be 8x8.");
        }

        return new BoardModel(createSquareArray(board));
    }

    /**
     * creates a square array from the given integer array.
     *
     * @param board any non-null integer array that is 8x8.
     * @return a Square array from the given 'board'
     */
    private static Piece[] createSquareArray(int[][] board) {
        Piece[] pieceArray = new Piece[64];
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                pieceArray[rank * 8 + file] = createPiece(board[file][rank]);
            }
        }
        return pieceArray;
    }

    /**
     * Creates a new piece from the given 'pieceInt'
     *
     * @param pieceInt the number representation of the pieces
     * @return the piece corresponding to the piece, if no piece exists, null is returned.
     */
    private static Piece createPiece(int pieceInt) {
        Piece piece = null;
        switch (pieceInt) {
            case W_PAWN:
                piece = WHITE_PAWN;
                break;
            case W_ROOK:
                piece = WHITE_ROOK;
                break;
            case W_KNIGHT:
                piece = WHITE_KNIGHT;
                break;
            case W_BISHOP:
                piece = WHITE_BISHOP;
                break;
            case W_KING:
                piece = WHITE_KING;
                break;
            case W_QUEEN:
                piece = WHITE_QUEEN;
                break;
            case B_PAWN:
                piece = BLACK_PAWN;
                break;
            case B_ROOK:
                piece = BLACK_ROOK;
                break;
            case B_KNIGHT:
                piece = BLACK_KNIGHT;
                break;
            case B_BISHOP:
                piece = BLACK_BISHOP;
                break;
            case B_QUEEN:
                piece = BLACK_QUEEN;
                break;
            case B_KING:
                piece = BLACK_KING;
        }
        return piece;
    }
}
