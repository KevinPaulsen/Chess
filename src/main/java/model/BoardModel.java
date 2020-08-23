package main.java.model;

import main.java.ChessCoordinate;
import main.java.model.pieces.King;
import main.java.model.pieces.Piece;
import main.java.model.pieces.Rook;

import java.util.ArrayList;

public class BoardModel {

    private static final int width = 8;
    private static final int height = 8;

    private SquareModel[][] board = new SquareModel[width][height];
    private final ArrayList<Piece> whitePieces = new ArrayList<>();
    private final ArrayList<Piece> blackPieces = new ArrayList<>();
    private King whiteKing;
    private King blackKing;

    public BoardModel() {
        for (int width = 0; width < BoardModel.width; width++) {
            for (int height = 0; height < BoardModel.height; height++) {
                board[width][height] = new SquareModel((width + height) % 2);
            }
        }
    }

    public BoardModel(SquareModel[][] startingPosition, King whiteKing, King blackKing) {
        this.board = startingPosition;
        this.whiteKing = whiteKing;
        this.blackKing = blackKing;
        initializePieceArrays();
    }

    public void makeMove(Move move) {
        if (move == null || move.isIncomplete()) {
            return;
        }
        Piece movingPiece = move.getMovedPiece();
        movingPiece.moveTo(move.getEndingCoordinate());

        if (move.getCapturedPiece() != null) {
            (movingPiece.getColor() == 1 ? whitePieces : blackPieces).remove(move.getCapturedPiece());
        }
        board[move.getEndingCoordinate().getColumn()][move.getEndingCoordinate().getRow()].setPiece(movingPiece);
        board[move.getStartingCoordinate().getColumn()][move.getStartingCoordinate().getRow()].setPiece(null);

        if (move.getTypeOfMove() == Move.EN_PASSANT) {
            board[move.getEndingCoordinate().getColumn()][move.getStartingCoordinate().getRow()].setPiece(null);
        } else if (move.getTypeOfMove() == Move.CASTLING_LEFT) {
            Rook leftRook = (Rook) board[0][move.getStartingCoordinate().getRow()].getPiece();
            makeMove(new Move(leftRook, null, leftRook.getCoordinate(),
                    new ChessCoordinate(3, leftRook.getCoordinate().getRow()), Move.NORMAL_MOVE));
        } else if (move.getTypeOfMove() == Move.CASTLING_RIGHT) {
            Rook rightRook = (Rook) board[7][move.getStartingCoordinate().getRow()].getPiece();
            makeMove(new Move(rightRook, null, rightRook.getCoordinate(),
                    new ChessCoordinate(5, rightRook.getCoordinate().getRow()), Move.NORMAL_MOVE));
        }
        whiteKing.updateAttacked(this);
        blackKing.updateAttacked(this);
        updateKingColorSquare(move, whiteKing);
        updateKingColorSquare(move, blackKing);
    }

    public void undoMove(Move move) {
        if (move == null) {
            return;
        }
        Piece movedPiece = move.getMovedPiece();
        movedPiece.moveBackTo(move.getStartingCoordinate());

        if (move.getCapturedPiece() != null) {
            (movedPiece.getColor() == 1 ? whitePieces : blackPieces).add(move.getCapturedPiece());
        }
        board[move.getStartingCoordinate().getColumn()][move.getStartingCoordinate().getRow()].setPiece(movedPiece);

        if (move.getTypeOfMove() == Move.NORMAL_MOVE) {
            board[move.getEndingCoordinate().getColumn()][move.getEndingCoordinate().getRow()].setPiece(move.getCapturedPiece());
        } else if (move.getTypeOfMove() == Move.EN_PASSANT) {
            board[move.getEndingCoordinate().getColumn()][move.getEndingCoordinate().getRow()].setPiece(null);
            board[move.getEndingCoordinate().getColumn()][move.getStartingCoordinate().getRow()].setPiece(move.getCapturedPiece());
        } else if (move.getTypeOfMove() == Move.CASTLING_LEFT) {
            board[move.getEndingCoordinate().getColumn()][move.getEndingCoordinate().getRow()].setPiece(move.getCapturedPiece());
            Rook leftRook = (Rook) board[3][move.getStartingCoordinate().getRow()].getPiece();
            undoMove(new Move(leftRook, null, leftRook.getCoordinate(),
                    new ChessCoordinate(0, leftRook.getCoordinate().getRow()), Move.NORMAL_MOVE));
        } else if (move.getTypeOfMove() == Move.CASTLING_RIGHT) {
            board[move.getEndingCoordinate().getColumn()][move.getEndingCoordinate().getRow()].setPiece(move.getCapturedPiece());
            Rook rightRook = (Rook) board[5][move.getStartingCoordinate().getRow()].getPiece();
            undoMove(new Move(rightRook, null,
                    new ChessCoordinate(7, rightRook.getCoordinate().getRow()), rightRook.getCoordinate(),
                    Move.NORMAL_MOVE));
        }
        whiteKing.updateAttacked(this);
        blackKing.updateAttacked(this);
        updateKingColorSquare(move, whiteKing);
        updateKingColorSquare(move, blackKing);
    }

    private void updateKingColorSquare(Move move, King blackKing) {
        if (blackKing.isAttacked()) {
            board[blackKing.getCoordinate().getColumn()][blackKing.getCoordinate().getRow()].setColor(2);
        } else {
            board[blackKing.getCoordinate().getColumn()][blackKing.getCoordinate().getRow()].resetColor();
            board[move.getStartingCoordinate().getColumn()][move.getStartingCoordinate().getRow()].resetColor();
            board[move.getEndingCoordinate().getColumn()][move.getEndingCoordinate().getRow()].resetColor();
        }
    }

    public Piece getPieceOnSquare(ChessCoordinate coordinate) {
        if (!coordinate.isInBounds()) {
            return null;
        }
        return board[coordinate.getColumn()][coordinate.getRow()].getPiece();
    }

    private void initializePieceArrays() {
        for (SquareModel[] column : board) {
            for (SquareModel square : column) {
                if (square.getPiece() != null) {
                    if (square.getPiece().getColor() == 0) {
                        whitePieces.add(square.getPiece());
                    } else {
                        blackPieces.add(square.getPiece());
                    }
                }
            }
        }
    }

    public SquareModel[][] getBoard() {
        return board;
    }

    public King getWhiteKing() {
        return whiteKing;
    }

    public King getBlackKing() {
        return blackKing;
    }

    public ArrayList<Piece> getWhitePieces() {
        return whitePieces;
    }

    public ArrayList<Piece> getBlackPieces() {
        return blackPieces;
    }
}
