package chess.model.chessai;

import chess.Move;
import chess.model.GameModel;

public class ChessAI {

    private static final int DEPTH = 6;

    private final Evaluator evaluator;

    private final GameModel game;


    public ChessAI(Evaluator evaluator, GameModel game) {
        this.evaluator = evaluator;
        this.game = game;
    }

    public Move getBestMove() {
        boolean maximizingPlayer = game.getTurn() == 'w';

        Evaluation bestEval = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;
        for (Move move : evaluator.getSortedMoves(game)) {
            game.move(move);
            Evaluation moveEval = miniMax(game, new AlphaBeta(), DEPTH - 1);
            game.undoMove(move);

            if (maximizingPlayer) {
                bestEval = Evaluation.max(bestEval, new Evaluation(move, moveEval.getEvaluation(), DEPTH));
            } else {
                bestEval = Evaluation.min(bestEval, new Evaluation(move, moveEval.getEvaluation(), DEPTH));
            }
        }

        System.out.println(bestEval.getEvaluation());
        return bestEval.getMove();
    }

    private Evaluation miniMax(GameModel game, AlphaBeta alphaBeta, int depth) {
        if (depth == 0) {
            return evaluator.evaluate(game);
        }
        boolean maximizingPlayer = game.getTurn() == 'w';

        Evaluation bestEval = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;
        for (Move move : evaluator.getSortedMoves(game)) {
            game.move(move);
            Evaluation moveEval = miniMax(game, new AlphaBeta(alphaBeta), depth - 1);
            game.undoMove(move);

            if (maximizingPlayer) {
                bestEval = Evaluation.max(bestEval, new Evaluation(move, moveEval.getEvaluation(), depth));
                alphaBeta.alphaMax(bestEval.getEvaluation());
            } else {
                bestEval = Evaluation.min(bestEval, new Evaluation(move, moveEval.getEvaluation(), depth));
                alphaBeta.betaMin(bestEval.getEvaluation());
            }

            if (alphaBeta.betaLessThanAlpha()) {
                break;
            }
        }
        return bestEval;
    }

    private static class AlphaBeta {
        double alpha;
        double beta;

        private AlphaBeta() {
            this(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        private AlphaBeta(double alpha, double beta) {
            this.alpha = alpha;
            this.beta = beta;
        }

        private AlphaBeta(AlphaBeta alphaBeta) {
            alpha = alphaBeta.alpha;
            beta = alphaBeta.beta;
        }

        private boolean betaLessThanAlpha() {
            return beta <= alpha;
        }

        private void alphaMax(double eval) {
            if (alpha < eval) {
                alpha = eval;
            }
        }

        private void betaMin(double eval) {
            if (eval < beta) {
                beta = eval;
            }
        }
    }
}
