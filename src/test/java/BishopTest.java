package test.java;

import main.java.ChessCoordinate;
import main.java.model.GameModel;
import main.java.model.pieces.Bishop;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BishopTest {

    @Test
    void getPossibleMoves() {
        Bishop testBishop = new Bishop((byte) 0, new ChessCoordinate(3, 3));

        assertEquals(8, testBishop.getPossibleMoves(new GameModel()).size(), "Wrong number of possible moves");
    }
}