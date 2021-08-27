package chess.model;

import chess.Move;
import chess.model.pieces.Knight;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import org.junit.Test;

import java.util.List;

import static chess.model.ChessBoardFactory.*;
import static org.junit.Assert.*;

public class GameModelTest {


    private final int[][] expectedAttackers = {
            {0, 1, 2, 0, 0, 2, 1, 0},
            {1, 1, 2, 0, 0, 2, 1, 1},
            {1, 1, 3, 0, 0, 3, 1, 1},
            {1, 4, 2, 0, 0, 2, 4, 1},
            {1, 4, 2, 0, 0, 2, 4, 1},
            {1, 1, 3, 0, 0, 3, 1, 1},
            {1, 1, 2, 0, 0, 2, 1, 1},
            {0, 1, 2, 0, 0, 2, 1, 0},
    };
    @Test
    public void constructorTest() {
        GameModel gameModel = new GameModel();
        Square[][] squares = gameModel.getBoard().getPieceArray();

        // Test that each piece is attacking right number of squares
        for (int file = 0; file < squares.length; file++) {
            for (int row = 0; row < squares.length; row++) {
                int numAttackers = squares[file][row].numAttackers('w') + squares[file][row].numAttackers('b');
                assertEquals("Square " + BoardModel.getChessCoordinate(file, row) + " has wrong number of attackers", expectedAttackers[file][row], numAttackers);
            }
        }

        for (Square[] file: gameModel.getBoard().getPieceArray()) {
            for (Square square : file) {
                Piece piece = square.getPiece();
                if (piece != null) {
                    if (piece instanceof Pawn) {
                        assertEquals("Pawn on " + piece.getCoordinate() + "has wrong number of moves", 2, piece.updateLegalMoves(gameModel.getBoard(), gameModel.getLastMove()).size());
                    } else if (piece instanceof Knight) {
                        assertEquals("Knight on " + piece.getCoordinate() + "has wrong number of moves", 2, piece.updateLegalMoves(gameModel.getBoard(), gameModel.getLastMove()).size());
                    } else {
                        assertEquals("Piece on " + piece.getCoordinate() + "has wrong number of moves", 0, piece.updateLegalMoves(gameModel.getBoard(), gameModel.getLastMove()).size());
                    }
                }
            }
        }
    }

    private final static int[][] TEST_BOARD = {
            {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK},
            {EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KNIGHT},
            {W_BISHOP, W_PAWN, W_KNIGHT, EMPTY, EMPTY, EMPTY, B_PAWN, B_BISHOP},
            {W_QUEEN, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_QUEEN},
            {W_KING, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KING},
            {W_BISHOP, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_BISHOP},
            {W_KNIGHT, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KNIGHT},
            {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK},
    };



    @Test
    public void moveAndUndo() {
        GameModel game = new GameModel();

        game.move(BoardModel.getChessCoordinate(1, 0), BoardModel.getChessCoordinate(2, 2));
        game.move(BoardModel.getChessCoordinate(4, 6), BoardModel.getChessCoordinate(4, 5));
        game.move(BoardModel.getChessCoordinate(2, 2), BoardModel.getChessCoordinate(3, 4));

        long start = System.currentTimeMillis();

        System.out.println("Positions: " + countNumPositions1(game, 1));

        long end = System.currentTimeMillis();
        System.out.println("" + (end - start) + " ms");
    }
    //position fen rnbqkbnr/pppp1ppp/4p3/3N4/8/8/PPPPPPPP/R1BQKBNR b KQkq - 1 1

    public int countNumPositions1(GameModel game, int depth) {
        System.out.println();
        int sum = 0;
        for (Move move : List.copyOf(game.getLegalMoves(game.getTurn()))) {
            game.move(move);
            int num = countNumPositions(game, depth - 1);
            System.out.println(move.toString() + ": " + num);
            sum += num;
            game.undoMove(move);
        }

        int test = 9_329;

        System.out.println();
        return sum;

    }

    private int countNumPositions(GameModel game, int depth) {
        if (depth <= 0) {
            return 1;
        }
        int sum = 0;
        for (Move move : List.copyOf(game.getLegalMoves(game.getTurn()))) {
            game.move(move);
            sum += countNumPositions(game, depth - 1);
            game.undoMove(move);
        }
        return sum;
    }
}