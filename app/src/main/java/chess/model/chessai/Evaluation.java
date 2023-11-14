package chess.model.chessai;

import chess.model.GameModel;
import chess.model.moves.Movable;

import static chess.model.GameModel.WHITE;

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

    public int getScore() {
        return score;
    }

    public int getDepth() {
        return depth;
    }

    public void print(GameModel game) {
        int evaluation = score * (move.getMovingPiece().getColor() == WHITE ? 1 : -1);
        System.out.printf("%2d ply = %-5d | %-5s", depth, evaluation, game.getMoveString(move));

        game.move(move);

        Evaluation current = child;
        while (current != null && current.getMove() != null) {
            System.out.printf(" -> %-6s", game.getMoveString(current.getMove()));
            game.move(current.getMove());
            current = current.getChild();
        }

        System.out.println();
    }

    public Movable getMove() {
        return move;
    }

    public Evaluation getChild() {
        return child;
    }
}
