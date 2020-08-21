package test.java;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.GameModel;
import main.java.model.SquareModel;
import main.java.model.pieces.Pawn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PawnTest {

    @Test
    void getPossibleMoves() {
        Pawn testPawn = new Pawn((byte) 0, new ChessCoordinate(0 ,1));

        assertEquals(2, testPawn.getPossibleMoves(new GameModel()).size(), "Wrong number of possible moves.");
    }
}