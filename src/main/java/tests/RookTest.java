package main.java.tests;

import main.java.ChessCoordinate;
import main.java.model.pieces.Rook;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RookTest {

    @Test
    void getPossibleMoves() {
        Rook testRock = new Rook((byte) 0, new ChessCoordinate(2, 4));

        assertEquals(14, testRock.getPossibleMoves().size(), "Wrong number of possible moves.");
    }
}