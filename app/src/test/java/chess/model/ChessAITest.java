package chess.model;

import chess.model.chessai.ChessAI;
import chess.model.chessai.PositionEvaluator;
import chess.model.moves.Movable;
import chess.model.moves.NormalMove;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static chess.ChessCoordinate.*;
import static chess.model.pieces.Piece.*;

public class ChessAITest {

    public static void main(String[] args) {
        GameModel testGame = new GameModel(
                "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        long startTime = System.currentTimeMillis();
        testAI.getBestMove(8);
        long endTime = System.currentTimeMillis();

        System.out.printf("Total time: %dms", endTime - startTime);
    }

    @Test
    public void testFindMateInOne1() {
        GameModel testGame = new GameModel(
                "r1bqkbnr/p1pp1ppp/1pn5/4p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 4");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        Movable expectedMove = new NormalMove(WHITE_QUEEN, F3.getBitMask(), F7.getBitMask());
        Movable actualMove = testAI.getBestMove(1);

        Assert.assertEquals("Mate in 1 was not found", expectedMove, actualMove);
    }

    @Test
    public void testFindMateInOne2() {
        GameModel testGame = new GameModel(
                "rnbqkbnr/pppp1ppp/4p3/8/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq - 0 2");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        Movable expectedMove = new NormalMove(BLACK_QUEEN, D8.getBitMask(), H4.getBitMask());
        Movable actualMove = testAI.getBestMove(1);

        Assert.assertEquals("Mate in 1 was not found", expectedMove, actualMove);
    }

    @Test
    public void testFindMateInTwo1() {
        GameModel testGame = new GameModel("5R2/8/8/8/1pN5/1pn5/k2K4/4R3 w - - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        List<Movable> expectedMoves = new ArrayList<>();
        expectedMoves.add(new NormalMove(WHITE_ROOK, F8.getBitMask(), A8.getBitMask()));
        expectedMoves.add(new NormalMove(BLACK_KNIGHT, C3.getBitMask(), A4.getBitMask()));
        expectedMoves.add(new NormalMove(WHITE_ROOK, A8.getBitMask(), A4.getBitMask()));

        for (Movable expectedMove : expectedMoves) {
            Movable actualMove = testAI.getBestMove(3);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }

    @Test
    public void testFindMateInTwo2() {
        GameModel testGame = new GameModel(
                "2bqkbn1/2pppp2/np2N3/r3P1p1/p2N2B1/5Q2/PPPPKPP1/RNB2r2 w KQkq - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        List<Movable> expectedMoves = new ArrayList<>();
        expectedMoves.add(new NormalMove(WHITE_QUEEN, F3.getBitMask(), F7.getBitMask()));
        expectedMoves.add(new NormalMove(BLACK_KING, E8.getBitMask(), F7.getBitMask()));
        expectedMoves.add(new NormalMove(WHITE_BISHOP, G4.getBitMask(), H5.getBitMask()));

        for (Movable expectedMove : expectedMoves) {
            Movable actualMove = testAI.getBestMove(3);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }

    @Test
    public void testFindMateInThree() {
        GameModel testGame = new GameModel("r5rk/5p1p/5R2/4B3/8/8/7P/7K w q - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        List<Movable> expectedMoves = new ArrayList<>();
        expectedMoves.add(new NormalMove(WHITE_ROOK, F6.getBitMask(), A6.getBitMask()));
        expectedMoves.add(new NormalMove(BLACK_PAWN, F7.getBitMask(), F6.getBitMask()));
        expectedMoves.add(new NormalMove(WHITE_BISHOP, E5.getBitMask(), F6.getBitMask()));
        expectedMoves.add(new NormalMove(BLACK_ROOK, G8.getBitMask(), G7.getBitMask()));
        expectedMoves.add(new NormalMove(WHITE_ROOK, A6.getBitMask(), A8.getBitMask()));

        for (Movable expectedMove : expectedMoves) {
            Movable actualMove = testAI.getBestMove(5);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }

    @Test
    public void testFindForcedDrawEasy() {
        GameModel testGame = new GameModel("K7/3rr3/8/8/8/8/R3R3/r4rk1 w - - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        List<Movable> expectedMoves = new ArrayList<>();
        expectedMoves.add(new NormalMove(WHITE_ROOK, E2.getBitMask(), G2.getBitMask()));

        expectedMoves.add(new NormalMove(BLACK_KING, G1.getBitMask(), H1.getBitMask()));
        expectedMoves.add(new NormalMove(WHITE_ROOK, G2.getBitMask(), H2.getBitMask()));
        expectedMoves.add(new NormalMove(BLACK_KING, H1.getBitMask(), G1.getBitMask()));
        expectedMoves.add(new NormalMove(WHITE_ROOK, H2.getBitMask(), G2.getBitMask()));

        expectedMoves.add(new NormalMove(BLACK_KING, G1.getBitMask(), H1.getBitMask()));
        expectedMoves.add(new NormalMove(WHITE_ROOK, G2.getBitMask(), H2.getBitMask()));
        expectedMoves.add(new NormalMove(BLACK_KING, H1.getBitMask(), G1.getBitMask()));
        expectedMoves.add(new NormalMove(WHITE_ROOK, H2.getBitMask(), G2.getBitMask()));

        int startDepth = 5;
        for (Movable expectedMove : expectedMoves) {
            Movable actualMove = testAI.getBestMove(startDepth);
            Assert.assertEquals("Forced draw in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }

        Assert.assertEquals("Game is not Over.", testGame.getGameOverStatus(), GameModel.DRAW);
    }

    @Test
    public void testFindForcedDrawIn4() {
        GameModel testGame = new GameModel(
                "7b/2q1p1PR/3r1pp1/6k1/3p4/3p1KPp/2n3pP/2r2bB1 w - - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame, true, true);

        while (testGame.getGameOverStatus() == GameModel.IN_PROGRESS) {
            Movable actualMove = testAI.getBestMove(8, 0);
            testGame.move(actualMove);
        }

        Assert.assertEquals("Game did not end in a draw", GameModel.DRAW,
                            testGame.getGameOverStatus());
        Assert.assertTrue("Used more moves than necessary", testGame.moveNum() <= 16);
    }
}
