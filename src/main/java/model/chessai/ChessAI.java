package main.java.model.chessai;

import main.java.Move;
import main.java.model.GameModel;
import main.java.model.pieces.Piece;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChessAI {

    private static final int DEPTH = 5;

    private final Map<Integer, Evaluation> positionToEval;
    private final Stack<GameModel> gameModels;
    private final Evaluator evaluator;
    private final Executor executor;

    public ChessAI(Evaluator evaluator) {
        this.evaluator = evaluator;
        this.positionToEval = new HashMap<>();
        this.gameModels = new Stack<>();
        this.executor = Executors.newFixedThreadPool(6);
    }

    public Move getBestMove(GameModel game) {

        Map<CompletableFuture<Evaluation>, Move> futureToMove = new HashMap<>();
        boolean maximizingPlayer = game.getTurn() == 'w';

        for (Move move : game.getLegalMoves(game.getTurn())) {
            GameModel gameClone = new GameModel(game);
            Move cloneMove = gameClone.cloneMove(move);
            gameClone.move(cloneMove);
            futureToMove.put(getFutureEvaluation(gameClone, maximizingPlayer), move);
        }

        CompletableFuture<Evaluation>[] futures = futureToMove.keySet().toArray(new CompletableFuture[0]);

        CompletableFuture<Evaluation> bestFuture = futures.length > 0 ? futures[0] : null;
        try {
            if (futures.length > 0) {
                bestFuture = CompletableFuture.allOf(futures).thenApply(v -> bestEvaluation(futures, maximizingPlayer)).get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return futureToMove.get(bestFuture);
    }

    private CompletableFuture<Evaluation> getFutureEvaluation(GameModel game, boolean maximizingPlayer) {
        return CompletableFuture.supplyAsync(() ->
                getBestEvaluation(game, maximizingPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE, DEPTH), executor);
    }

    private CompletableFuture<Evaluation> bestEvaluation(CompletableFuture<Evaluation>[] futureEvaluations, boolean maximizingPlayer) {
        CompletableFuture<Evaluation> bestFuture = futureEvaluations[0];
        Evaluation bestEvaluation = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;

        for (CompletableFuture<Evaluation> futureEvaluation : futureEvaluations) {
            try {
                Evaluation eval = futureEvaluation.get();
                if (eval.getEvaluation() > bestEvaluation.getEvaluation() && maximizingPlayer) {
                    bestEvaluation = eval;
                    bestFuture = futureEvaluation;
                } else if (eval.getEvaluation() < bestEvaluation.getEvaluation() && !maximizingPlayer) {
                    bestEvaluation = eval;
                    bestFuture = futureEvaluation;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bestFuture;
    }

    private Evaluation getBestEvaluation(GameModel game, boolean maximizingPlayer, double alpha, double beta, int depth) {

        int hashCode = game.hashCode();
        if (positionToEval.containsKey(hashCode) && positionToEval.get(hashCode).getDepth() >= depth) {
            return positionToEval.get(hashCode);
        }

        Set<Piece> movingPieces = maximizingPlayer ? Set.copyOf(game.getBoard().getWhitePieces())
                : Set.copyOf(game.getBoard().getBlackPieces());

        Evaluation bestEvaluation = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;

        for (Piece piece : movingPieces) {
            for (Move move : piece.getLegalMoves(game)) {
                game.move(move);
                Evaluation evaluation = miniMax(game, !maximizingPlayer, alpha, beta, depth - 1);
                evaluation = new Evaluation(move, evaluation.getEvaluation(), depth);
                game.undoMove(move);

                if (maximizingPlayer) {
                    bestEvaluation = Evaluation.max(bestEvaluation, evaluation);
                    alpha = Math.max(alpha, evaluation.getEvaluation());
                } else {
                    bestEvaluation = Evaluation.min(bestEvaluation, evaluation);
                    beta = Math.min(beta, evaluation.getEvaluation());
                }
                if (beta <= alpha) break;
            }
            if (beta <= alpha) break;
        }
        positionToEval.put(hashCode, bestEvaluation);
        return bestEvaluation;
    }


    private Evaluation miniMax(GameModel game, boolean maximizingPlayer, double alpha, double beta, int depth) {
        if (depth == 0 || game.isOver()) {
            return evaluator.evaluate(game);
        }

        Evaluation bestEvaluation;
        if (maximizingPlayer) {
            bestEvaluation = getBestEvaluation(game, true, alpha, beta, depth);
        } else {
            bestEvaluation = getBestEvaluation(game, false, alpha, beta, depth);
        }
        return bestEvaluation;
    }
}
