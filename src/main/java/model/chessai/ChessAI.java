package main.java.model.chessai;

import main.java.model.GameModel;
import main.java.model.moves.Move;

public abstract class ChessAI {

    protected static final Move NULL_MOVE = new Move();

    private Move bestMove = null;

    public abstract double getEvaluation(GameModel gameModel);

    public MoveEvaluation getBestMove(GameModel gameModel, int depth, int color) {
        boolean maximizingPlayer = color == 0;

        double evaluation = miniMax(gameModel, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, maximizingPlayer);
        return new MoveEvaluation(bestMove, evaluation);
    }

    private double miniMax(GameModel gameModel, int depth, double alpha, double beta, boolean maximizingPlayer) {
        // If done looking ahead, or game is over, return static evaluation
        if (depth == 0 || gameModel.isOver()) {
            return getEvaluation(gameModel);
        }

        if (maximizingPlayer) {
            double maxEvaluation = Integer.MIN_VALUE;
            Move bestMove = null;
            for (Move move : gameModel.getAllLegalMoves()) {
                gameModel.move(move);
                double evaluation = miniMax(gameModel, depth - 1, alpha, beta, false);
                gameModel.undoMove(move);

                if (evaluation > maxEvaluation) {
                    maxEvaluation = evaluation;
                    bestMove = move;
                }

                alpha = Double.max(alpha, evaluation);
                if (beta <= alpha) {
                    break;
                }
            }
            this.bestMove = bestMove;
            return maxEvaluation;
        } else {
            double minEvaluation = Integer.MAX_VALUE;
            Move bestMove = null;
            for (Move move : gameModel.getAllLegalMoves()) {
                gameModel.move(move);
                double evaluation = miniMax(gameModel, depth - 1, alpha, beta, true);
                gameModel.undoMove(move);

                if (evaluation < minEvaluation) {
                    minEvaluation = evaluation;
                    bestMove = move;
                }

                beta = Double.min(beta, evaluation);
                if (beta <= alpha) {
                    break;
                }
            }
            this.bestMove = bestMove;
            return minEvaluation;
        }
    }
}
