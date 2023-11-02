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
    private final ChessInputView inputView;

    public ChessView(Piece[] pieceArray, ViewControlable controller,
                     GameDataRetriever dataRetriever) {
        this.root = new StackPane();
        this.boardView = new ChessBoardView(pieceArray, controller, dataRetriever);
        this.turnView = new ChessTurnView(dataRetriever);
        this.inputView = new ChessInputView(controller);
        this.scene = new Scene(root, 400, 400);

        content = new BorderPane();
        content.setPadding(new Insets(10));

        // Set boardView as center
        content.setCenter(boardView.getRegion());

        // Set turnView at top
        turnView.prefWidthProperty().bind(content.widthProperty());
        BorderPane.setMargin(turnView, new Insets(0, 0, 10, 0));
        content.setTop(turnView);

        // Set inputView at bottom
        inputView.prefWidthProperty().bind(content.widthProperty());
        BorderPane.setMargin(inputView, new Insets(10, 0, 0, 0));
        content.setBottom(inputView);

        // Set the Background and add content
        Background background = new Background(
                new BackgroundFill(Color.rgb(70, 70, 70), CornerRadii.EMPTY, Insets.EMPTY));
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

    public void adjustSize() {
        Insets boardPadding = content.getPadding();
        Insets turnPadding = BorderPane.getMargin(turnView);
        Insets inputPadding = BorderPane.getMargin(inputView);

        double availableWidth = root.getWidth() - boardPadding.getLeft() - boardPadding.getRight();

        double availableHeight =
                root.getHeight() - boardPadding.getTop() - boardPadding.getBottom() -
                        turnView.getHeight() - turnPadding.getBottom() - turnPadding.getTop() -
                        inputView.getHeight() - inputPadding.getBottom() - inputPadding.getTop();

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
        Insets inputViewPadding = BorderPane.getMargin(inputView);

        return boardView.getMinimumHeight() + contentPadding.getTop() + contentPadding.getBottom() +
                turnView.getHeight() + turnViewPadding.getTop() + turnViewPadding.getBottom() +
                inputView.getHeight() + inputViewPadding.getTop() + inputViewPadding.getBottom();
    }

    public void setPosition(Piece[] pieceArray) {
        boardView.setPieces(pieceArray);
    }

    public interface ViewControlable {
        void makeMove(ChessCoordinate start, ChessCoordinate end);

        void processCommand(String command);
    }

    public interface GameDataRetriever {
        List<ChessCoordinate> getReachableCoordinates(ChessCoordinate start);

        char getTurn();

        boolean canMove(ChessCoordinate coordinate);

        Movable getLastMove();
    }
}
