package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.chessai.ChessAI;
import chess.model.chessai.PositionEvaluator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static chess.ChessCoordinate.*;
import static chess.model.pieces.Piece.*;

public class ChessAITest {

    @Test
    public void testFindMateInOne1() {
        GameModel testGame = new GameModel("r1bqkbnr/p1pp1ppp/1pn5/4p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 4");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        Move expectedMove = new Move(F3, F7, WHITE_QUEEN, F7,
                null, BLACK_PAWN);
        Move actualMove = testAI.getBestMove(1);

        Assert.assertEquals("Mate in 1 was not found", expectedMove, actualMove);
    }

    @Test
    public void testFindMateInOne2() {
        GameModel testGame = new GameModel("rnbqkbnr/pppp1ppp/4p3/8/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq - 0 2");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        Move expectedMove = new Move(D8, H4, BLACK_QUEEN);
        Move actualMove = testAI.getBestMove(1);

        Assert.assertEquals("Mate in 1 was not found", expectedMove, actualMove);
    }

    @Test
    public void testFindMateInTwo1() {
        GameModel testGame = new GameModel("5R2/8/8/8/1pN5/1pn5/k2K4/4R3 w - - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        List<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Move(F8, A8, WHITE_ROOK));
        expectedMoves.add(new Move(C3, A4, BLACK_KNIGHT));
        expectedMoves.add(new Move(A8, A4, WHITE_ROOK, A4, null, BLACK_KNIGHT));

        for (Move expectedMove : expectedMoves) {
            Move actualMove = testAI.getBestMove(3);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }

    @Test
    public void testFindMateInTwo2() {
        GameModel testGame = new GameModel("2bqkbn1/2pppp2/np2N3/r3P1p1/p2N2B1/5Q2/PPPPKPP1/RNB2r2 w KQkq - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        List<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Move(F3, F7, WHITE_QUEEN, F7, null, BLACK_PAWN));
        expectedMoves.add(new Move(E8, F7, BLACK_KING, F7, null, WHITE_QUEEN));
        expectedMoves.add(new Move(G4, H5, WHITE_BISHOP, null, null, null));

        for (Move expectedMove : expectedMoves) {
            Move actualMove = testAI.getBestMove(3);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }

    @Test
    public void testFindMateInThree() {
        GameModel testGame = new GameModel("r5rk/5p1p/5R2/4B3/8/8/7P/7K w q - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        List<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Move(F6, A6, WHITE_ROOK, null, null, null));
        expectedMoves.add(new Move(F7, F6, BLACK_PAWN, null, null, null));
        expectedMoves.add(new Move(E5, F6, WHITE_BISHOP, F6, null, BLACK_PAWN));
        expectedMoves.add(new Move(G8, G7, BLACK_ROOK, null, null, null));
        expectedMoves.add(new Move(A6, A8, WHITE_ROOK, A8, null, BLACK_ROOK));

        for (Move expectedMove : expectedMoves) {
            Move actualMove = testAI.getBestMove(5);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }

    @Test
    public void testFindForcedDrawEasy() {
        GameModel testGame = new GameModel("K7/3rr3/8/8/8/8/R3R3/r4rk1 w - - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        List<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Move(E2, G2, WHITE_ROOK));

        expectedMoves.add(new Move(G1, H1, BLACK_KING));
        expectedMoves.add(new Move(G2, H2, WHITE_ROOK));
        expectedMoves.add(new Move(H1, G1, BLACK_KING));
        expectedMoves.add(new Move(H2, G2, WHITE_ROOK));

        expectedMoves.add(new Move(G1, H1, BLACK_KING));
        expectedMoves.add(new Move(G2, H2, WHITE_ROOK));
        expectedMoves.add(new Move(H1, G1, BLACK_KING));
        expectedMoves.add(new Move(H2, G2, WHITE_ROOK));

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
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        while (testGame.getGameOverStatus() == GameModel.IN_PROGRESS) {
            Move actualMove = testAI.getBestMove(true, 1, 1_000);
            testGame.move(actualMove);
        }

        Assert.assertEquals("Game did not end in a draw", GameModel.DRAW, testGame.getGameOverStatus());
    }
}
