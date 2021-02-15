package main.java.model.chessai;

import main.java.Move;
import main.java.model.GameModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChessAI {

    private static final int DEPTH = 6;

    private final Map<Integer, Evaluation> positionToEval;
    private final Map<String, AlphaBeta> alphaBetaMap;
    private final Evaluator evaluator;
    private final Executor executor;

    private Evaluation evaluation;

    private long time;

    public ChessAI(Evaluator evaluator) {
        this.evaluator = evaluator;
        this.positionToEval = new HashMap<>();
        this.alphaBetaMap = new HashMap<>();
        this.executor = Executors.newFixedThreadPool(6);
    }

    public Move getBestMove(GameModel game) {
        long startTime = System.currentTimeMillis();

        ArrayList<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        boolean maximizingPlayer = game.getTurn() == 'w';
        evaluation = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;
        AlphaBeta alphaBeta = new AlphaBeta();
        alphaBetaMap.put("", alphaBeta);

        int count = 0;
        for (Move move : getOrderedMoves(game.getLegalMoves(game.getTurn()), maximizingPlayer)) {
            GameModel gameClone = new GameModel(game);
            Move cloneMove = gameClone.cloneMove(move);
            gameClone.move(cloneMove);
            completableFutures.add(getFutureEvaluation(gameClone, Integer.toString(999 - count), maximizingPlayer).thenAcceptAsync(evaluation -> {
                if (maximizingPlayer) {
                    this.evaluation = Evaluation.max(this.evaluation, new Evaluation(move, evaluation.getEvaluation(), evaluation.getDepth()));
                    alphaBeta.alphaMax(evaluation.getEvaluation());
                } else {
                    this.evaluation = Evaluation.min(this.evaluation, new Evaluation(move, evaluation.getEvaluation(), evaluation.getDepth()));
                    alphaBeta.betaMin(evaluation.getEvaluation());
                }
            }, executor));
            count++;
        }

        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
        time = System.currentTimeMillis() - startTime;

        System.out.println(time);
        positionToEval.put(game.hashCode(), evaluation);
        System.out.println("Eval: " + evaluation.getEvaluation());
        return evaluation.getMove();
    }

    /**
     * Returns a Future Evaluation. Calculates the best evaluation from a position asynchronously.
     *
     * @param game the game to to move in.
     * @param maximizingPlayer the best player
     * @return a future Evaluation
     */
    private CompletableFuture<Evaluation> getFutureEvaluation(GameModel game, String treeLocation, boolean maximizingPlayer) {
        return CompletableFuture.supplyAsync(() -> getBestEvaluation(game, maximizingPlayer, new AlphaBeta(), treeLocation, DEPTH), executor);
    }

    private Evaluation getBestEvaluation(GameModel game, boolean maximizingPlayer, AlphaBeta alphaBeta, String treeLocation, int depth) {

        int hashCode = game.hashCode();
        if (positionToEval.containsKey(hashCode) && positionToEval.get(hashCode).getDepth() >= depth) {
            return positionToEval.get(hashCode);
        }//*/

        Evaluation bestEvaluation = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;
        alphaBetaMap.put(treeLocation, alphaBeta);

        int count = 0;
        for (Move move : getOrderedMoves(game.getLegalMoves(maximizingPlayer ? 'w' : 'b'), maximizingPlayer)) {
            game.move(move);
            Evaluation evaluation = miniMax(game, !maximizingPlayer, new AlphaBeta(alphaBeta), treeLocation + (999 - count), depth - 1);
            evaluation = new Evaluation(move, evaluation.getEvaluation(), depth);
            game.undoMove(move);

            if (maximizingPlayer) {
                bestEvaluation = Evaluation.max(bestEvaluation, evaluation);
                alphaBeta.alphaMax(evaluation.getEvaluation());
            } else {
                bestEvaluation = Evaluation.min(bestEvaluation, evaluation);
                alphaBeta.betaMin(evaluation.getEvaluation());
            }
            alphaBeta.updateFromMap(alphaBetaMap, treeLocation, maximizingPlayer);
            if (alphaBeta.betaLessThanAlpha()) {
                break;
            }
            count++;
        }

        positionToEval.put(hashCode, bestEvaluation);
        return bestEvaluation;
    }

    private Evaluation miniMax(GameModel game, boolean maximizingPlayer, AlphaBeta alphaBeta, String treeLocation, int depth) {
        if (depth == 0 || game.isOver()) {
            return evaluator.evaluate(game);
        }

        Evaluation bestEvaluation;
        if (maximizingPlayer) {
            bestEvaluation = getBestEvaluation(game, true, alphaBeta, treeLocation, depth);
        } else {
            bestEvaluation = getBestEvaluation(game, false, alphaBeta, treeLocation, depth);
        }
        return bestEvaluation;
    }

    private List<Move> getOrderedMoves(List<Move> unorderedList, boolean maximizingPlayer) {
        unorderedList.sort((Move move1, Move move2) -> {
            int score;
            if (maximizingPlayer) {
                score = move1.valueScore() - move2.valueScore();
            } else {
                score = move2.valueScore() - move1.valueScore();
            }
            return score;
        });
        return unorderedList;
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

        private void updateFromMap(Map<String, AlphaBeta> alphaBetaMap, String location, boolean maximizingPlayer) {
            if (location.length() % 3 != 0) {
                throw new IllegalArgumentException("Location is not formatted properly");
            }

            for (int idx = 0; idx < location.length(); idx += 3) {
                AlphaBeta parentAlphaBeta = alphaBetaMap.get(location.substring(0, idx));
                if (parentAlphaBeta != null) {
                    alphaMax(parentAlphaBeta.alpha);
                    betaMin(parentAlphaBeta.beta);//*/
                }
            }
        }

        private boolean alphaMax(double eval) {
            if (alpha < eval) {
                alpha = eval;
                return true;
            }
            return false;
        }

        private boolean betaMin(double eval) {
            if (eval < beta) {
                beta = eval;
                return true;
            }
            return false;
        }
    }
}
