package main.java.tests;

import main.java.ChessCoordinate;
import main.java.model.pieces.Knight;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnightTest {

    @Test
    void getPossibleMoves() {
        Knight testKnight = new Knight((byte) 0, new ChessCoordinate(3, 3));

        assertEquals(8, testKnight.getPossibleMoves().size(), "Wrong number of possible moves.");
    }
}