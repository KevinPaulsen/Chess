package main.java.model.chessai;

import main.java.model.GameModel;
import main.java.model.pieces.Piece;

import java.util.ArrayList;

public class SimplePointComparisonAI extends ChessAI {

    @Override
    public double getEvaluation(GameModel gameModel) {
        double whiteScore = sumScore(gameModel.getBoardModel().getWhitePieces());
        double blackScore = sumScore(gameModel.getBoardModel().getBlackPieces());
        double score;

        double winIncentive = (gameModel.getWinner() == 0) ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        if (gameModel.isOver()) {
            score = winIncentive;
        } else {
            score = whiteScore - blackScore;
        }

        return Math.round(score);
    }

    private double sumScore(ArrayList<Piece> pieces) {
        int score = 0;
        for (Piece piece : pieces) {
            score += piece.getValue();
        }
        return score;
    }
}
