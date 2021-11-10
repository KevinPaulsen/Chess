package chess.model.chessai;

import chess.Move;
import chess.model.GameModel;

public class Evaluation {

    public static final Evaluation WORST_EVALUATION = new Evaluation(Integer.MIN_VALUE, -1);
    public static final Evaluation BEST_EVALUATION = new Evaluation(Integer.MAX_VALUE, -1);

    private static final char NO_LOSER = 'n';

    private final Move move;
    private final double evaluation;
    private final char loser;
    private final int depth;

    public Evaluation(double evaluation, int depth) {
        this(null, evaluation, NO_LOSER, depth);
    }

    public Evaluation(Move move, Evaluation evaluation, int depth) {
        this(move, evaluation.evaluation, evaluation.loser, depth);
    }

    public Evaluation(Move move, double evaluation, char loser, int depth) {
        this.move = move;
        this.evaluation = evaluation;
        this.depth = depth;
        this.loser = loser;
    }

    public static Evaluation min(Evaluation evaluation1, Evaluation evaluation2) {
        if (evaluation1.loser == GameModel.WHITE) {
            if (evaluation2.loser == GameModel.WHITE) {
                if (evaluation1.depth < evaluation2.depth) {
                    return evaluation1;
                } else {
                    return evaluation2;
                }
            } else {
                return evaluation1;
            }
        } else if (evaluation2.loser == GameModel.WHITE) {
            return evaluation2;
        }

        if (evaluation1.evaluation <= evaluation2.evaluation) {
            return evaluation1;
        } else {
            return evaluation2;
        }
    }

    public static Evaluation max(Evaluation evaluation1, Evaluation evaluation2) {
        if (evaluation1.loser == GameModel.BLACK) {
            if (evaluation2.loser == GameModel.BLACK) {
                if (evaluation1.depth < evaluation2.depth) {
                    return evaluation1;
                } else {
                    return evaluation2;
                }
            } else {
                return evaluation1;
            }
        } else if (evaluation2.loser == GameModel.BLACK) {
            return evaluation2;
        }
        if (evaluation1.evaluation >= evaluation2.evaluation) {
            return evaluation1;
        } else {
            return evaluation2;
        }
    }

    public Move getMove() {
        return move;
    }

    public double getEvaluation() {
        return evaluation;
    }

    public int getDepth() {
        return depth;
    }
}
