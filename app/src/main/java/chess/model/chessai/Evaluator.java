package chess.model.chessai;

import chess.model.GameModel;

public interface Evaluator {

    Evaluation evaluate(GameModel game);

}
