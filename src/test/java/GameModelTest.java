package test.java;

import main.java.ChessCoordinate;
import main.java.model.GameModel;
import main.java.model.moves.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameModelTest {

    private final int[][] kingAttackerBoard = {
            {-1, -1, -1, -1, -1, -1, -1, -1,},
            {-1, -1, -1, -1, -1, -1, -1, -1,},
            {-1, -1, -1, -1, -1, -1, -1, -1,},
            {-1, -1, -1, -1,  5, -1, -1, -1,},
            {-1,  1,  7, -1, -1, -1,  9, -1,},
            {-1, -1, -1, -1,  8, -1, -1, -1,},
            {-1, -1, -1,  3, -1, -1, -1, -1,},
            {-1, -1, -1, -1, -1, 10, -1, 11,},
    };
    private final int[][] kingCastleTest = {
            { 1, -1, -1, -1, -1, -1, -1,  7},
            {-1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1},
            { 5, -1, -1, -1, -1, -1, -1, 11},
            {-1, -1, -1, -1, -1, -1, -1, -1},
            {-1,  5, -1, -1, -1, -1, -1, -1},
            { 1, -1, -1, -1, -1, -1, -1,  7}
    };

    @Test
    void getLegalMove() {
        GameModel kingAttackerGame = CustomChessGameGenerator.makeGameModel(kingAttackerBoard);

        assert kingAttackerGame != null;
        ChessCoordinate kingStartCoord = new ChessCoordinate(3, 4);
        ChessCoordinate kingEndCoord = new ChessCoordinate(2, 3);
        assertEquals(new Move(kingAttackerGame.getBoardModel().getWhiteKing(), null, kingStartCoord,
                kingEndCoord), kingAttackerGame.getLegalMove(kingStartCoord, kingEndCoord));

        ChessCoordinate rookStartCoord = new ChessCoordinate(4, 1);
        ChessCoordinate rookEndCoord1 = new ChessCoordinate(4, 3);
        ChessCoordinate rookEndCoord2 = new ChessCoordinate(4, 2);

        assertNull(kingAttackerGame.getLegalMove(rookStartCoord, rookEndCoord1));
        assertEquals(new Move(kingAttackerGame.getBoardModel().getPieceOnSquare(rookStartCoord),
                kingAttackerGame.getBoardModel().getPieceOnSquare(rookEndCoord2), rookStartCoord, rookEndCoord2),
                kingAttackerGame.getLegalMove(rookStartCoord, rookEndCoord2));


        GameModel castleTest = CustomChessGameGenerator.makeGameModel(kingCastleTest);
        assert castleTest != null;

        ChessCoordinate kingStart = new ChessCoordinate(4, 0);
        ChessCoordinate kingEnd = new ChessCoordinate(6, 0);
        assertEquals(new Move(castleTest.getBoardModel().getPieceOnSquare(kingStart), null, kingStart,
                kingEnd), castleTest.getLegalMove(kingStart, kingEnd));
    }

    @Test
    void move() {
    }
}