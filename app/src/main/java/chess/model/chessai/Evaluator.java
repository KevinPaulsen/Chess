package chess.model.chessai;

import chess.Move;
import chess.model.GameModel;

import java.util.List;

public interface Evaluator {

    Evaluation evaluate(GameModel game);

    List<Move> getSortedMoves(GameModel game);
}
