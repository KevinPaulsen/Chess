package test.java;

import main.java.model.GameModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KingTest {

    private final int[][] normalBoard = {
            {1, 0, -1, -1, -1, -1, 6, 7},
            {2, 0, -1, -1, -1, -1, 6, 8},
            {3, 0, -1, -1, -1, -1, 6, 9},
            {5, 0, -1, -1, -1, -1, 6, 11},
            {4, 0, -1, -1, -1, -1, 6, 10},
            {3, 0, -1, -1, -1, -1, 6, 9},
            {2, 0, -1, -1, -1, -1, 6, 8},
            {1, 0, -1, -1, -1, -1, 6, 7},
    };

    private final int[][] kingAttackerBoard = {
            {-1, -1, -1, -1, -1, -1, -1, -1,},
            {-1, -1, -1, -1, -1, -1, -1, -1,},
            {-1, -1, -1, -1, -1, -1, -1, -1,},
            {-1, -1, -1, -1,  5, -1, -1, -1,},
            {-1, -1,  7, -1, -1, -1,  9, -1,},
            {-1, -1, -1, -1,  8, -1, -1, -1,},
            {-1, -1, -1, -1, -1, -1, -1, -1,},
            {-1, -1, -1, -1, -1, 10, -1, 11,},
    };

    @Test
    void getPossibleMoves() {
        GameModel normalGame = CustomChessGameGenerator.makeGameModel(normalBoard);
        GameModel kingAttackerGame = CustomChessGameGenerator.makeGameModel(kingAttackerBoard);

        assert normalGame != null;
        assertEquals(0, normalGame.getBoardModel().getWhiteKing().getPossibleMoves(normalGame).size());
        assert kingAttackerGame != null;
        assertEquals(1, kingAttackerGame.getBoardModel().getWhiteKing().getPossibleMoves(kingAttackerGame).size());
    }
}