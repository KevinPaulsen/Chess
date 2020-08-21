package main.java.tests;

import main.java.ChessCoordinate;
import main.java.model.pieces.Queen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueenTest {

    @Test
    void getPossibleMoves() {
        Queen testQueen = new Queen((byte) 0, new ChessCoordinate(3, 3));

        assertEquals(27, testQueen.getPossibleMoves().size(), "Wrong number of possible moves.");
    }
}