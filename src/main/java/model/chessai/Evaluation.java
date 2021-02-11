package main.java.model.chessai;

import main.java.Move;

public class Evaluation {

    public static final Evaluation WORST_EVALUATION = new Evaluation(Integer.MIN_VALUE, 0, -1);
    public static final Evaluation BEST_EVALUATION = new Evaluation(Integer.MAX_VALUE, 0, -1);

    private final Move move;
    private final double evaluation;
    private final int positionHash;
    private final int depth;

    public Evaluation(double evaluation, int positionHash, int depth) {
        this(null, evaluation, positionHash, depth);
    }

    public Evaluation(Move move, double evaluation, int positionHash, int depth) {
        this.move = move;
        this.evaluation = evaluation;
        this.positionHash = positionHash;
        this.depth = depth;
    }

    public static Evaluation min(Evaluation evaluation1, Evaluation evaluation2) {
        if (evaluation1.evaluation <= evaluation2.evaluation) {
            return evaluation1;
        } else {
            return evaluation2;
        }
    }

    public static Evaluation max(Evaluation evaluation1, Evaluation evaluation2) {
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

    public int getPositionHash() {
        return positionHash;
    }

    public int getDepth() {
        return depth;
    }
}
