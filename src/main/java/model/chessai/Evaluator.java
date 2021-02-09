package main.java.model.chessai;

import main.java.model.GameModel;

public interface Evaluator {

    Evaluation evaluate(GameModel game);

}
