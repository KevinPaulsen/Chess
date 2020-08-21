package test.java;

import main.java.ChessCoordinate;
import main.java.model.GameModel;
import main.java.model.pieces.Rook;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RookTest {

    @Test
    void getPossibleMoves() {
        Rook testRock = new Rook((byte) 0, new ChessCoordinate(2, 4));

        assertEquals(11, testRock.getPossibleMoves(new GameModel()).size(), "Wrong number of possible moves.");
    }
}