package chess.view;

import javafx.scene.control.Label;

import static chess.model.GameModel.WHITE;

public class ChessTurnView extends Label {

    private static final String WHITE_TO_MOVE = "White to Move";
    private static final String BLACk_TO_MOVE = "Black to Move";
    private static final String CENTER_STYLE = "-fx-alignment: center;";
    private static final String BOLD_STYLE = "-fx-font-weight: bold;";
    private static final String FONT_SIZE_STYLE = "-fx-font-size: 24px;";
    private static final String TEXT_COLOR_STYLE = "-fx-text-fill: lightgray;";

    private final ChessView.GameDataRetriever retriever;

    public ChessTurnView(ChessView.GameDataRetriever retriever) {
        this.retriever = retriever;
        setStyle(CENTER_STYLE + BOLD_STYLE + FONT_SIZE_STYLE + TEXT_COLOR_STYLE);

        updateTurn();
    }

    public void updateTurn() {
        if (retriever.getTurn() == WHITE) {
            setText(WHITE_TO_MOVE);
        } else {
            setText(BLACk_TO_MOVE);
        }
    }
}
