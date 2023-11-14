package chess.model.chessai;

import chess.model.moves.Movable;

public class Evaluation {

    public static final Evaluation WORST_EVALUATION = new Evaluation(Integer.MIN_VALUE + 1, -1);
    public static final Evaluation MAX_EVALUATION = new Evaluation(Integer.MAX_VALUE, -1);

    private final Movable move;
    private final Evaluation child;
    private final int score;
    private final int depth;

    public Evaluation(int score, int depth) {
        this(null, null, score, depth);
    }

    public Evaluation(Movable move, Evaluation child, int score, int depth) {
        this.move = move;
        this.child = child;
        this.score = score;
        this.depth = depth;
    }

    public Evaluation(Movable move, int score) {
        this(move, null, score, -1);
    }

    public Movable getMove() {
        return move;
    }

    public Evaluation getChild() {
        return child;
    }

    public int getScore() {
        return score;
    }

    public int getDepth() {
        return depth;
    }
}
