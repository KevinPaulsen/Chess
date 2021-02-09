package main.java.model.chessai;

import main.java.Move;
import main.java.model.GameModel;
import main.java.model.pieces.Piece;

import java.util.ConcurrentModificationException;
import java.util.Set;

public class ChessAI {

    private static final int DEPTH = 2;

    private final Evaluator evaluator;

    public ChessAI(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public Move getBestMove(GameModel game) {
        Move bestMove = null;

        boolean maximizingPlayer = game.getTurn() == 'w';
        Set<Piece> movingPieces = maximizingPlayer ?
                game.getBoard().getWhitePieces() : game.getBoard().getBlackPieces();
        Evaluation bestEvaluation = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;

        for (Move move : Set.copyOf(game.getLegalMoves())) {
            if (move.getMovingPiece().getColor() == (maximizingPlayer ? 'w' : 'b')) {
                game.move(move);
                if (maximizingPlayer) {
                    Evaluation evaluation = miniMax(game, true, DEPTH);
                    if (bestEvaluation.getEvaluation() < evaluation.getEvaluation()) {
                        bestEvaluation = evaluation;
                        bestMove = move;
                    }
                } else {
                    Evaluation evaluation = miniMax(game, false, DEPTH);
                    if (bestEvaluation.getEvaluation() > evaluation.getEvaluation()) {
                        bestEvaluation = evaluation;
                        bestMove = move;
                    }
                    game.undoMove(move);
                }
            }
        }

        return bestMove;
    }

    private Evaluation miniMax(GameModel game, boolean maximizingPlayer, int depth) {
        if (depth == 0) {
            return evaluator.evaluate(game);
        }

        if (maximizingPlayer) {
            Evaluation maxEval = Evaluation.WORST_EVALUATION;
            for (Move move : Set.copyOf(game.getLegalMoves()))  {
                if (move.getMovingPiece().getColor() == 'w') {
                    game.move(move);
                    Evaluation eval = miniMax(game, false, depth - 1);
                    game.undoMove(move);
                    maxEval = Evaluation.max(maxEval, eval);
                }
            }
            return maxEval;
        } else {
            Evaluation minEval = Evaluation.BEST_EVALUATION;
            for (Move move : Set.copyOf(game.getLegalMoves())) {
                if (move.getMovingPiece().getColor() == 'b') {
                    game.move(move);
                    Evaluation eval = miniMax(game, true, depth - 1);
                    game.undoMove(move);
                    minEval = Evaluation.min(minEval, eval);
                }
            }
            return minEval;
        }
    }
}
