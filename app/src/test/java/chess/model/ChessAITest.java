package chess.model;

import chess.Move;
import chess.model.chessai.ChessAI;
import chess.model.chessai.PositionEvaluator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static chess.ChessCoordinate.*;

public class ChessAITest {

    @Test
    public void testFindMateInOne1() {
        GameModel testGame = new GameModel("r1bqkbnr/p1pp1ppp/1pn5/4p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 4");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true,true);

        Move expectedMove = new Move(F3, F7, F7, null, testGame.getBoard());
        Move actualMove = testAI.getBestMove(1);

        Assert.assertEquals("Mate in 1 was not found", expectedMove, actualMove);
    }

    @Test
    public void testFindMateInOne2() {
        GameModel testGame = new GameModel("rnbqkbnr/pppp1ppp/4p3/8/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq - 0 2");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        Move expectedMove = new Move(D8, H4, testGame.getBoard());
        Move actualMove = testAI.getBestMove(1);

        Assert.assertEquals("Mate in 1 was not found", expectedMove, actualMove);
    }

    @Test
    public void testFindMateInTwo1() {
        GameModel testGame = new GameModel("5R2/8/8/8/1pN5/1pn5/k2K4/4R3 w - - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        List<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Move(F8, A8, testGame.getBoard()));
        expectedMoves.add(new Move(C3, A4, testGame.getBoard()));
        expectedMoves.add(new Move(A8, A4, A4, null, testGame.getBoard()));

        for (Move expectedMove : expectedMoves) {
            Move actualMove = testAI.getBestMove(3);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }

    @Test
    public void testFindMateInTwo2() {
        GameModel testGame = new GameModel("2bqkbn1/2pppp2/np2N3/r3P1p1/p2N2B1/5Q2/PPPPKPP1/RNB2r2 w KQkq - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        List<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Move(F3, F7, F7, null, testGame.getBoard()));
        expectedMoves.add(new Move(E8, F7, F7, null, testGame.getBoard()));
        expectedMoves.add(new Move(G4, H5, null, null, null));

        for (Move expectedMove : expectedMoves) {
            Move actualMove = testAI.getBestMove(3);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }

    @Test
    public void testFindMateInThree() {
        GameModel testGame = new GameModel("r5rk/5p1p/5R2/4B3/8/8/7P/7K w q - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        List<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Move(F6, A6, testGame.getBoard()));
        expectedMoves.add(new Move(F7, F6, testGame.getBoard()));
        expectedMoves.add(new Move(E5, F6, F6, null, testGame.getBoard()));
        expectedMoves.add(new Move(G8, G7, testGame.getBoard()));
        expectedMoves.add(new Move(A6, A8, A8, null, testGame.getBoard()));

        for (Move expectedMove : expectedMoves) {
            Move actualMove = testAI.getBestMove(5);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }

    @Test
    public void testFindForcedDrawEasy() {
        GameModel testGame = new GameModel("K7/3rr3/8/8/8/8/R3R3/r4rk1 w - - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        List<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Move(E2, G2, testGame.getBoard()));

        expectedMoves.add(new Move(G1, H1, testGame.getBoard()));
        expectedMoves.add(new Move(G2, H2, testGame.getBoard()));
        expectedMoves.add(new Move(H1, G1, testGame.getBoard()));
        expectedMoves.add(new Move(H2, G2, testGame.getBoard()));

        expectedMoves.add(new Move(G1, H1, testGame.getBoard()));
        expectedMoves.add(new Move(G2, H2, testGame.getBoard()));
        expectedMoves.add(new Move(H1, G1, testGame.getBoard()));
        expectedMoves.add(new Move(H2, G2, testGame.getBoard()));

        int startDepth = 5;
        for (Move expectedMove : expectedMoves) {
            Move actualMove = testAI.getBestMove(startDepth);
            Assert.assertEquals("Forced draw in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }

        Assert.assertEquals("Game is not Over.", testGame.getGameOverStatus(), GameModel.DRAW);
    }


    @Test
    public void testFindForcedDrawIn4() {
        GameModel testGame = new GameModel("7b/2q1p1PR/3r1pp1/6k1/3p4/3p1KPp/2n3pP/2r2bB1 w - - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        while (testGame.getGameOverStatus() == GameModel.IN_PROGRESS) {
            Move actualMove = testAI.getBestMove(8, 0);
            testGame.move(actualMove);
        }

        Assert.assertEquals("Game did not end in a draw", GameModel.DRAW, testGame.getGameOverStatus());
        Assert.assertTrue("Used more moves than necessary", testGame.moveNum() <= 16);
    }

    public static void main(String[] args) {
        GameModel testGame = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        long startTime = System.currentTimeMillis();
        testAI.getBestMove(8);
        long endTime = System.currentTimeMillis();

        System.out.printf("Total time: %dms", endTime - startTime);
    }
}
