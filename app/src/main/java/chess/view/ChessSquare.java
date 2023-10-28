package chess.view;

import chess.ChessCoordinate;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.layout.StackPane;

import javax.annotation.Nullable;

public class ChessSquare extends StackPane {

    private static final double MIN_SIZE = 30.0;
    private static final String LIGHT_COLOR = "-fx-background-color: rgb(240, 217, 181);";
    private static final String DARK_COLOR = "-fx-background-color: rgb(180, 136, 99);";

    private final ChessCoordinate coordinate;

    public ChessSquare(boolean isDark, DoubleBinding widthProperty, DoubleBinding heightProperty, int file, int rank) {
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

        setListeners();
    }

    public boolean hasPiece() {
        return !getChildren().isEmpty();
    }

    public @Nullable ChessPieceView getPieceView() {
        if (hasPiece()) {
            return (ChessPieceView) getChildren().get(0);
        } else {
            return null;
        }
    }

    private void setListeners() {
    }

    public void addPiece(ChessPieceView movingPiece) {
        if (!hasPiece()) {
            this.getChildren().add(movingPiece);
        }
    }

    public ChessPieceView removePiece() {
        if (hasPiece()) {
            return (ChessPieceView) this.getChildren().remove(0);
        }
        return null;
    }

    public ChessCoordinate getCoordinate() {
        return coordinate;
    }
}
