package test.java;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.GameModel;
import main.java.model.SquareModel;
import main.java.model.pieces.King;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KingTest {

    @Test
    void getPossibleMoves() {
        King testKing = new King((byte) 0, new ChessCoordinate(4, 0));

        assertEquals(0, testKing.getPossibleMoves(new GameModel()).size(), "Wrong number of possible moves.");
    }
}