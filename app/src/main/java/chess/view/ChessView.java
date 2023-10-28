package chess.view;

import chess.ChessCoordinate;
import chess.model.moves.Movable;
import chess.model.pieces.Piece;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class ChessView extends StackPane {

    private final ChessBoardView boardView;
    private final BorderPane content;

    public ChessView(Piece[] pieceArray, MoveController controller) {
        this.boardView = new ChessBoardView(pieceArray, controller);

        Background background = new Background(new BackgroundFill(Color.rgb(70, 70, 70)
                , CornerRadii.EMPTY, Insets.EMPTY));

        this.content = new BorderPane();
        this.content.setPadding(new Insets(10));
        this.content.setCenter(boardView.getRegion());

        setBackground(background);
        this.getChildren().add(content);
    }

    public Parent getRoot() {
        return this;
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
}
