package chess.model.features;

import chess.model.GameModel;

public interface Feature {
    String featureString(GameModel game);
}
