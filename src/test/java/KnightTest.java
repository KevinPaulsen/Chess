package test.java;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.GameModel;
import main.java.model.SquareModel;
import main.java.model.pieces.Knight;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnightTest {

    @Test
    void getPossibleMoves() {
        Knight testKnight = new Knight((byte) 0, new ChessCoordinate(3, 3));

        assertEquals(6, testKnight.getPossibleMoves(new GameModel()).size(), "Wrong number of possible moves.");
    }
}