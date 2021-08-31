package chess.model.chessai;

import chess.Move;
import chess.model.GameModel;

public class ChessAI {

    private static final int DEPTH = 7;

    private final Evaluator evaluator;


    public ChessAI(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public Move getBestMove(GameModel game) {
        boolean maximizingPlayer = game.getTurn() == 'w';

        Evaluation bestEval = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;
        for (Move move : game.getLegalMoves()) {
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
        for (Move move : game.getLegalMoves()) {
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

    private Evaluation searchAllCaptures(GameModel game, AlphaBeta alphaBeta) {

        boolean maximizingPlayer = game.getTurn() == 'w';
        Evaluation bestEval = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;
        for (Move move : game.getLegalMoves()) {
            if (move.getInteractingPiece() != null && move.getInteractingPieceEnd() == null) {
                game.move(move);
                Evaluation eval = searchAllCaptures(game, new AlphaBeta(alphaBeta));
                game.undoMove(move);

                if (maximizingPlayer) {
                    bestEval = Evaluation.max(bestEval, eval);
                    alphaBeta.alphaMax(bestEval.getEvaluation());
                } else {
                    bestEval = Evaluation.min(bestEval, eval);
                    alphaBeta.betaMin(bestEval.getEvaluation());
                }

                if (alphaBeta.betaLessThanAlpha()) {
                    break;
                }
            }
        }

        if (bestEval == Evaluation.BEST_EVALUATION || bestEval == Evaluation.WORST_EVALUATION) {
            return evaluator.evaluate(game);
        }

        return new Evaluation(maximizingPlayer ? alphaBeta.alpha : alphaBeta.beta, 0);
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
