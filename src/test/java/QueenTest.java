package test.java;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.GameModel;
import main.java.model.SquareModel;
import main.java.model.pieces.Queen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueenTest {

    @Test
    void getPossibleMoves() {
        Queen testQueen = new Queen((byte) 0, new ChessCoordinate(3, 3));

        assertEquals(19, testQueen.getPossibleMoves(new GameModel()).size(), "Wrong number of possible moves.");
    }
}