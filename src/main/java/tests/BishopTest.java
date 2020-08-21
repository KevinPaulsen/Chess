package main.java.tests;

import main.java.ChessCoordinate;
import main.java.model.pieces.Bishop;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BishopTest {

    @Test
    void getPossibleMoves() {
        Bishop testBishop = new Bishop((byte) 0, new ChessCoordinate(3, 3));

        assertEquals(13, testBishop.getPossibleMoves().size(), "Wrong number of possible moves");
    }
}