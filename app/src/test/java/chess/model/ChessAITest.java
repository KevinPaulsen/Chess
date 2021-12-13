package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.chessai.ChessAI;
import chess.model.chessai.PositionEvaluator;
import chess.model.pieces.Piece;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static chess.model.pieces.Piece.*;
import static org.junit.Assert.*;

public class ChessAITest {

    @Test
    public void testFindMateInOne1() {
        GameModel testGame = new GameModel("r1bqkbnr/p1pp1ppp/1pn5/4p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 4");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        ChessCoordinate startingCoordinate = BoardModel.getChessCoordinate('f', 3);
        ChessCoordinate endingCoordinate = BoardModel.getChessCoordinate('f', 7);

        Move expectedMove = new Move(startingCoordinate, endingCoordinate, WHITE_QUEEN, endingCoordinate,
                null, BLACK_PAWN);
        Move actualMove = testAI.getBestMove(false, 1);

        Assert.assertEquals("Mate in 1 was not found", expectedMove, actualMove);
    }

    @Test
    public void testFindMateInOne2() {
        GameModel testGame = new GameModel("rnbqkbnr/pppp1ppp/4p3/8/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq - 0 2");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        ChessCoordinate startingCoordinate = BoardModel.getChessCoordinate('d', 8);
        ChessCoordinate endingCoordinate = BoardModel.getChessCoordinate('h', 4);

        Move expectedMove = new Move(startingCoordinate, endingCoordinate, BLACK_QUEEN);
        Move actualMove = testAI.getBestMove(false, 1);

        Assert.assertEquals("Mate in 1 was not found", expectedMove, actualMove);
    }

    @Test
    public void testFindMateInTwo1() {
        GameModel testGame = new GameModel("5R2/8/8/8/1pN5/1pn5/k2K4/4R3 w - - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        List<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Move(BoardModel.getChessCoordinate('f', 8), BoardModel.getChessCoordinate('a', 8), WHITE_ROOK));
        expectedMoves.add(new Move(BoardModel.getChessCoordinate('c', 3), BoardModel.getChessCoordinate('a', 4), BLACK_KNIGHT));
        expectedMoves.add(new Move(BoardModel.getChessCoordinate('a', 8), BoardModel.getChessCoordinate('a', 4), WHITE_ROOK, BoardModel.getChessCoordinate('a', 4), null, BLACK_KNIGHT));

        for (Move expectedMove : expectedMoves) {
            Move actualMove = testAI.getBestMove(false, 3);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }

    @Test
    public void testFindMateInTwo2() {
        GameModel testGame = new GameModel("2bqkbn1/2pppp2/np2N3/r3P1p1/p2N2B1/5Q2/PPPPKPP1/RNB2r2 w KQkq - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        List<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Move(BoardModel.getChessCoordinate('f', 3), BoardModel.getChessCoordinate('f', 7), WHITE_QUEEN, BoardModel.getChessCoordinate('f', 7), null, BLACK_PAWN));
        expectedMoves.add(new Move(BoardModel.getChessCoordinate('e', 8), BoardModel.getChessCoordinate('f', 7), BLACK_KING, BoardModel.getChessCoordinate('f', 7), null, WHITE_QUEEN));
        expectedMoves.add(new Move(BoardModel.getChessCoordinate('g', 4), BoardModel.getChessCoordinate('h', 5), WHITE_BISHOP, null, null, null));

        for (Move expectedMove : expectedMoves) {
            Move actualMove = testAI.getBestMove(false, 3);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }

    @Test
    public void testFindMateInThree() {
        GameModel testGame = new GameModel("r5rk/5p1p/5R2/4B3/8/8/7P/7K w q - 0 1");
        ChessAI testAI = new ChessAI(new PositionEvaluator(testGame), testGame);

        List<Move> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Move(BoardModel.getChessCoordinate('f', 6), BoardModel.getChessCoordinate('a', 6), WHITE_ROOK, null, null, null));
        expectedMoves.add(new Move(BoardModel.getChessCoordinate('f', 7), BoardModel.getChessCoordinate('f', 6), BLACK_PAWN, null, null, null));
        expectedMoves.add(new Move(BoardModel.getChessCoordinate('e', 5), BoardModel.getChessCoordinate('f', 6), WHITE_BISHOP, BoardModel.getChessCoordinate('f', 6), null, BLACK_PAWN));
        expectedMoves.add(new Move(BoardModel.getChessCoordinate('g', 8), BoardModel.getChessCoordinate('g', 7), BLACK_ROOK, null, null, null));
        expectedMoves.add(new Move(BoardModel.getChessCoordinate('a', 6), BoardModel.getChessCoordinate('a', 8), WHITE_ROOK, BoardModel.getChessCoordinate('a', 8), null, BLACK_ROOK));

        for (Move expectedMove : expectedMoves) {
            Move actualMove = testAI.getBestMove(false, 5);
            Assert.assertEquals("Mate in 3 was not found.", expectedMove, actualMove);
            testGame.move(actualMove);
        }
    }
}
