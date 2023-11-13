package chess.model.chessai;


import chess.model.GameModel;
import chess.model.moves.Movable;

import java.util.List;

import static chess.model.GameModel.IN_PROGRESS;

public class Searcher {

    private static final int WORST_EVAL = Integer.MIN_VALUE + 1;
    private static final int BEST_EVAL = Integer.MAX_VALUE;

    private final GameModel game;
    private final Evaluator evaluator;
    private volatile boolean canceled;

    public Searcher(GameModel game, Evaluator evaluator) {
        this.game = game;
        this.evaluator = evaluator;
        this.canceled = false;
    }

    public Movable getBestMove(int depth) {
        int alpha = WORST_EVAL;
        Movable bestMove = null;

        List<Movable> sortedMoves = evaluator.getSortedMoves(game, null);
        for (Movable move : sortedMoves) {
            game.move(move);
            int eval = -search(depth - 1, WORST_EVAL, -alpha);
            game.undoLastMove();

            if (eval > alpha) {
                alpha = eval;
                bestMove = move;
            }
        }

        if (canceled) {
            return null;
        }

        System.out.printf("d%-2d=%6d | ", depth, alpha);
        return bestMove;
    }

    public void cancel() {
        canceled = true;
    }

    private int search(int depth, int alpha, int beta) {
        if (canceled) {
            return WORST_EVAL;
        } else if (depth == 0 || game.getGameOverStatus() != IN_PROGRESS) {
            return evaluator.evaluate(game);
        }

        List<Movable> sortedMoves = evaluator.getSortedMoves(game, null);

        for (Movable move : sortedMoves) {
            game.move(move);
            int eval = -search(depth - 1, -beta, -alpha);
            game.undoLastMove();

            if (eval >= beta) {
                return beta;
            } else if (eval > alpha) {
                alpha = eval;
            }
        }

        return alpha;
    }
}
