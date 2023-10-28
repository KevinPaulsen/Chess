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

import java.util.List;

public class ChessView {

    private final Scene scene;
    private final StackPane root;
    private final BorderPane content;
    private final ChessBoardView boardView;
    private final ChessTurnView turnView;

    public ChessView(Piece[] pieceArray, MoveController controller, GameDataRetriever dataRetriever) {
        this.root = new StackPane();
        this.boardView = new ChessBoardView(pieceArray, controller, dataRetriever);
        this.turnView = new ChessTurnView(dataRetriever);
        this.scene = new Scene(root, 400, 400);

        Background background = new Background(new BackgroundFill(Color.rgb(70, 70, 70), CornerRadii.EMPTY, Insets.EMPTY));

        content = new BorderPane();
        content.setPadding(new Insets(10));

        // Set boardView as center
        content.setCenter(boardView.getRegion());

        // Set turnView at top
        turnView.prefWidthProperty().bind(content.widthProperty());
        BorderPane.setMargin(turnView, new Insets(0, 0, 10, 0));
        content.setTop(turnView);

        // Set the Background and add content
        root.setBackground(background);
        root.getChildren().add(content);

        // Make root always same size as screen
        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());

        // Bind center to be the correct size
        root.widthProperty().addListener((observable, oldValue, newValue) -> adjustSize());
        root.heightProperty().addListener((observable, oldValue, newValue) -> adjustSize());
    }

    public Scene getScene() {
        return scene;
    }

    public void displayMove(Movable move) {
        boardView.displayMove(move);
        turnView.updateTurn();
    }

    private void adjustSize() {
        double availableWidth = root.getWidth() - content.getPadding().getLeft() - content.getPadding().getRight();
        double availableHeight = root.getHeight() - content.getPadding().getTop() - content.getPadding().getBottom()
                - turnView.getHeight() - BorderPane.getMargin(turnView).getBottom() + BorderPane.getMargin(turnView).getTop();

        double size = Math.min(availableWidth, availableHeight);

        boardView.setMinSize(size, size);
        boardView.setMaxSize(size, size);
    }

    public double getMinimumWidth() {
        Insets padding = content.getPadding();
        return boardView.getMinimumWidth() + padding.getLeft() + padding.getRight();
    }

    public double getMinimumHeight() {
        Insets contentPadding = content.getPadding();
        Insets turnViewPadding = BorderPane.getMargin(turnView);

        return boardView.getMinimumHeight() + contentPadding.getTop() + contentPadding.getBottom()
                + turnView.getHeight() + turnViewPadding.getTop() + turnViewPadding.getBottom();
    }

    public interface MoveController {
        void makeMove(ChessCoordinate start, ChessCoordinate end);
    }

    public interface GameDataRetriever {
        List<ChessCoordinate> getReachableCoordinates(ChessCoordinate start);

        char getTurn();
    }
}
