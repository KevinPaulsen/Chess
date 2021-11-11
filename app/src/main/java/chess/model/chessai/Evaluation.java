package chess.model.chessai;

import chess.Move;

import static chess.model.GameModel.BLACK;
import static chess.model.GameModel.WHITE;

public class Evaluation {

    public static final Evaluation WORST_EVALUATION = new Evaluation(Integer.MIN_VALUE, -1);
    public static final Evaluation BEST_EVALUATION = new Evaluation(Integer.MAX_VALUE, -1);

    public static final char NO_LOSER = 'n';

    private final Move move;
    private final int evaluation;
    private final char loser;
    private final int depth;

    public Evaluation(int evaluation, int depth) {
        this(null, evaluation, NO_LOSER, depth);
    }

    public Evaluation(Move move, Evaluation evaluation, int depth) {
        this(move, evaluation.evaluation, evaluation.loser, depth);
    }

    public Evaluation(Move move, int evaluation, char loser, int depth) {
        this.move = move;
        this.evaluation = evaluation;
        this.depth = depth;
        this.loser = loser;
    }

    public static Evaluation min(Evaluation evaluation1, Evaluation evaluation2) {
        if (evaluation1 == BEST_EVALUATION) {
            return evaluation2;
        } else if (evaluation2 == BEST_EVALUATION) {
            return evaluation1;
        } else if (evaluation1.loser == WHITE) {
            if (evaluation2.loser == WHITE) {
                return evaluation1.depth < evaluation2.depth ? evaluation1 : evaluation2;
            } else {
                return evaluation1;
            }
        } else if (evaluation1.loser == BLACK) {
            if (evaluation2.loser == BLACK) {
                return evaluation1.depth < evaluation2.depth ? evaluation2 : evaluation1;
            } else {
                return evaluation2;
            }
        } else if (evaluation2.loser == WHITE) {
            return evaluation2;
        } else if (evaluation2.loser == BLACK) {
            return evaluation1;
        } else {
            return evaluation1.evaluation <= evaluation2.evaluation ? evaluation1 : evaluation2;
        }
    }

    public static Evaluation max(Evaluation evaluation1, Evaluation evaluation2) {
        if (evaluation1 == WORST_EVALUATION) {
            return evaluation2;
        } else if (evaluation2 == WORST_EVALUATION) {
            return evaluation1;
        } else if (evaluation1.loser == WHITE) {
            if (evaluation2.loser == WHITE) {
                return evaluation1.depth < evaluation2.loser ? evaluation2 : evaluation1;
            } else {
                return evaluation2;
            }
        } else if (evaluation1.loser == BLACK) {
            if (evaluation2.loser == BLACK) {
                return evaluation1.depth < evaluation2.loser ? evaluation1 : evaluation2;
            } else {
                return evaluation2;
            }
        } else if (evaluation2.loser == WHITE) {
            return evaluation1;
        } else if (evaluation2.loser == BLACK) {
            return evaluation2;
        } else {
            return evaluation1.evaluation >= evaluation2.evaluation ? evaluation1 : evaluation2;
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

    public char getLoser() {
        return loser;
    }

    @Override
    public String toString() {
        return "{" +
                String.format("%-6s ", move.toString()) +
                String.format("%4d ", evaluation) +
                String.format("%2d", depth) +
                "}";
    }
}
