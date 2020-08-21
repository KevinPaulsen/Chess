package main.java.tests;

import main.java.ChessCoordinate;
import main.java.model.pieces.King;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KingTest {

    @Test
    void getPossibleMoves() {
        King testKing = new King((byte) 0, new ChessCoordinate(3, 3));

        assertEquals(8, testKing.getPossibleMoves().size(), "Wrong number of possible moves.");
    }
}