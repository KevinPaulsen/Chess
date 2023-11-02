package chess.view;

import javafx.scene.control.TextField;

public class ChessInputView extends TextField {

    private static final String TEXT_COLOR_STYLE = "-fx-text-fill: lightgray;";
    private static final String BACKGROUND_COLOR = "-fx-prompt-text-fill: lightgray;";
    private static final String PROMPT_COLOR_STYLE = "-fx-background-color: grey;";
    private static final String FONT_SIZE_STYLE = "-fx-font-size: 16px;";

    public ChessInputView(ChessView.ViewControlable controlable) {
        this.setPromptText("Enter your command...");
        this.setStyle(TEXT_COLOR_STYLE + BACKGROUND_COLOR + PROMPT_COLOR_STYLE + FONT_SIZE_STYLE);

        // Event handler for the Enter key press
        this.setOnAction(event -> {
            String userInput = getText();

            if (!userInput.isEmpty()) {
                controlable.processCommand(userInput);
                this.clear();
            }
        });
    }
}
