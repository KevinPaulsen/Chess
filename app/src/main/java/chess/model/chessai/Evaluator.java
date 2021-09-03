package chess.model.chessai;

import chess.Move;
import chess.model.GameModel;

import java.util.List;

public interface Evaluator {

    /**
     * Evaluates the given game and returns an evaluation of this position. The
     * Evaluation will have a depth of 0, and the move will be null.
     *
     * @param game the game to evaluate.
     * @return the evaluation of this game.
     */
    Evaluation evaluate(GameModel game);

    /**
     * Returns a list of all the legal moves in this position, and they are sorted
     * into this evaluators best guess from most-likely to be the best move, to least
     * likely.
     *
     * @param game The game to get moves from.
     * @return the list of sorted legal moves.
     */
    List<Move> getSortedMoves(GameModel game);
}
