package main.java.model.chessai;

public class Evaluation {

    public static final Evaluation WORST_EVALUATION = new Evaluation(Double.MIN_VALUE, 0, -1);
    public static final Evaluation BEST_EVALUATION = new Evaluation(Double.MAX_VALUE, 0, -1);

    private final double evaluation;
    private final int positionHash;
    private final int depth;

    public Evaluation(double evaluation, int positionHash, int depth) {
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
