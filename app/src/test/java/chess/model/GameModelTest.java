package chess.model;

import chess.model.pieces.Knight;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameModelTest {


    private int[][] expectedAttackers = {
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


    @Test
    public void move() {
    }

    @Test
    public void testMove() {
    }

    @Test
    public void undoMove() {
    }
}