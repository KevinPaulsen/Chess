package main.java.model.chessai;

import main.java.Move;
import main.java.model.GameModel;
import main.java.model.pieces.Piece;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class ChessAI {

    private static final int DEPTH = 5;

    private final Evaluator evaluator;

    public ChessAI(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public Move getBestMove(GameModel game) {
        boolean maximizingPlayer = game.getTurn() == 'w';

        Evaluation bestEvaluation = miniMax(game, maximizingPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE, DEPTH);
        Move bestMove = bestEvaluation.getMove();

        System.out.println(Math.round(bestEvaluation.getEvaluation()));
        System.out.println((bestEvaluation.getMove()));
        return bestMove;
    }

    private Evaluation getBestEvaluation(GameModel game, boolean maximizingPlayer, double alpha, double beta, int depth) {
        Set<Piece> movingPieces = maximizingPlayer ? Set.copyOf(game.getBoard().getWhitePieces())
                : Set.copyOf(game.getBoard().getBlackPieces());

        /*TreeSet<Piece> sortedSet = new TreeSet<>((Piece o1, Piece o2) -> {
            if (o1.getCoordinate().getFile() == o2.getCoordinate().getFile()) {
                return Integer.compare(o1.getCoordinate().getRank(), o2.getCoordinate().getRank());
            } else {
                return Integer.compare(o1.getCoordinate().getFile(), o2.getCoordinate().getFile());
            }
        });
        sortedSet.addAll(movingPieces);

        Comparator<Move> moveComparator = (Move o1, Move o2) -> {
            if (o1.getEndingCoordinate().getFile() == o2.getEndingCoordinate().getFile()) {
                return Integer.compare(o1.getEndingCoordinate().getRank(), o2.getEndingCoordinate().getRank());
            } else {
                return Integer.compare(o1.getEndingCoordinate().getFile(), o2.getEndingCoordinate().getFile());
            }
        };//*/

        Evaluation bestEvaluation = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;

        for (Piece piece : movingPieces) {
            //TreeSet<Move> moves = new TreeSet<>(moveComparator);
            //moves.addAll(piece.getLegalMoves(game));//*/
            for (Move move : piece.getLegalMoves(game)) {
                game.move(move);
                Evaluation evaluation = miniMax(game, !maximizingPlayer, alpha, beta, depth - 1);
                evaluation = new Evaluation(move, evaluation.getEvaluation(), game.hashCode(), depth);
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
