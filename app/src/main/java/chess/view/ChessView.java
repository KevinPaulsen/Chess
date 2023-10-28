package chess.view;

import chess.ChessCoordinate;
import chess.model.moves.Movable;
import chess.model.pieces.Piece;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class ChessView {

    private final Scene scene;
    private final StackPane root;
    private final ChessBoardView boardView;
    private final BorderPane content;

    public ChessView(Piece[] pieceArray, MoveController controller) {
        this.root = new StackPane();
        this.boardView = new ChessBoardView(pieceArray, controller);

        Background background = new Background(new BackgroundFill(Color.rgb(70, 70, 70), CornerRadii.EMPTY, Insets.EMPTY));

        this.content = new BorderPane();
        this.content.setPadding(new Insets(10));
        this.content.setCenter(boardView.getRegion());

        this.root.setBackground(background);
        this.root.getChildren().add(content);

        this.scene = new Scene(root, 400, 400);

        // Make root always same size as screen
        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());

        root.widthProperty().addListener((observable, oldValue, newValue) -> adjustSize());
        root.heightProperty().addListener((observable, oldValue, newValue) -> adjustSize());
    }

    public Scene getScene() {
        return scene;
    }

    public BorderPane getBoarderPane() {
        return content;
    }

    public void displayMove(Movable move) {
        boardView.displayMove(move);
    }

    public interface MoveController {
        Movable makeMove(ChessCoordinate start, ChessCoordinate end);
    }

    private void adjustSize() {
        double availableWidth = root.getWidth() - content.getPadding().getLeft() - content.getPadding().getRight();
        double availableHeight = root.getHeight() - content.getPadding().getTop() - content.getPadding().getBottom();

        double size = Math.min(availableWidth, availableHeight);

        boardView.setMinSize(size, size);
        boardView.setMaxSize(size, size);
    }
}
