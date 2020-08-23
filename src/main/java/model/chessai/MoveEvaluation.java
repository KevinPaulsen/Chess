package main.java.model.chessai;

import main.java.model.Move;

public class MoveEvaluation {

    private Move move;
    private final double evaluation;

    public MoveEvaluation(Move move, double evaluation) {
        this.move = move;
        this.evaluation = evaluation;
    }

    public static MoveEvaluation min(MoveEvaluation evaluation1, MoveEvaluation evaluation2, Move move) {
        if (evaluation1.getEvaluation() > evaluation2.getEvaluation()) {
            evaluation2.setMove(move);
            return evaluation2;
        } else {
            return evaluation1;
        }
    }

    public static MoveEvaluation max(MoveEvaluation evaluation1, MoveEvaluation evaluation2, Move move) {
        if (evaluation1.getEvaluation() < evaluation2.getEvaluation()) {
            evaluation2.setMove(move);
            return evaluation2;
        } else {
            return evaluation1;
        }
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public Move getMove() {
        return move;
    }

    public double getEvaluation() {
        return evaluation;
    }
}
