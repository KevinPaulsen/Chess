package chess.view;

import chess.ChessCoordinate;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javax.annotation.Nullable;

public class ChessSquareView extends StackPane {

    public static final double MIN_SIZE = 30.0;
    private static final String LIGHT_COLOR = "-fx-background-color: rgb(236, 218, 185);";
    private static final String DARK_COLOR = "-fx-background-color: rgb(174, 138, 104);";
    private static final String SELECTED = "-fx-background-color: rgba(20, 85, 30, .5)";
    private static final String HIGHLIGHTED = "-fx-background-color: rgba(255,0,232,0.5)";
    private static final String MOVE_DESTINATION = "-fx-background-color: radial-gradient(" +
            "center 50% 50%, radius 75%, rgba(20, 85, 30, 0.5) 19%, rgba(0, 0, 0, 0) 20%);";
    private static final String MOVE_DESTINATION_CAPTURE =
            "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, " +
                    "rgba(0, 0, 0, 0) 56%, rgba(20, 85, 30, 0.3) 55%);";
    private static final String TRANSPARENT = "-fx-background-color: rgba(0, 0, 0, 0)";
    private static final String LAST_MOVE = "-fx-background-color: rgba(155, 199, 0, .41)";

    private final boolean isDark;
    private final ChessCoordinate coordinate;
    private final Pane colorFilter;

    public ChessSquareView(boolean isDark, DoubleBinding widthProperty,
                           DoubleBinding heightProperty, int file, int rank) {
        this.isDark = isDark;
        this.colorFilter = new Pane();
        this.setMinSize(MIN_SIZE, MIN_SIZE);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.prefWidthProperty().bind(widthProperty);
        this.prefHeightProperty().bind(heightProperty);

        this.coordinate = ChessCoordinate.getChessCoordinate(file, rank);

        if (isDark) {
            this.setStyle(DARK_COLOR);
        } else {
            this.setStyle(LIGHT_COLOR);
        }

        colorFilter.setStyle(TRANSPARENT);
        this.getChildren().add(colorFilter);
    }

    public void isLastMove() {
        colorFilter.setStyle(LAST_MOVE);
    }

    public void notLastMove() {
        colorFilter.setStyle(TRANSPARENT);
    }

    public void select() {
        colorFilter.setStyle(SELECTED);
    }

    public void unselect() {
        colorFilter.setStyle(TRANSPARENT);
    }

    public void addPiece(ChessPieceView movingPiece) {
        if (hasPiece()) {
            this.removePiece();
        }
        this.getChildren().add(movingPiece);
    }

    public boolean hasPiece() {
        return getChildren().stream().anyMatch(node -> node instanceof ChessPieceView);
    }

    public ChessPieceView removePiece() {
        if (hasPiece()) {
            ChessPieceView piece = getPieceView();
            getChildren().remove(piece);
            return piece;
        }
        return null;
    }

    public @Nullable ChessPieceView getPieceView() {
        return (ChessPieceView) getChildren().stream().filter(
                node -> node instanceof ChessPieceView).findAny().orElse(null);
    }

    public ChessCoordinate getCoordinate() {
        return coordinate;
    }

    public void setMoveDestination() {
        if (hasPiece()) {
            colorFilter.setStyle(MOVE_DESTINATION_CAPTURE);
        } else {
            colorFilter.setStyle(MOVE_DESTINATION);
        }
    }

    public void removeModeDestination() {
        colorFilter.setStyle(TRANSPARENT);
    }

    public void highlight() {
        colorFilter.setStyle(HIGHLIGHTED);
    }

    public void removeHighlight() {
        colorFilter.setStyle(TRANSPARENT);
    }
}
