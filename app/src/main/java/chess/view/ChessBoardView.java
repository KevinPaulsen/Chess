package chess.view;

import chess.ChessCoordinate;
import chess.model.moves.CastlingMove;
import chess.model.moves.EnPassantMove;
import chess.model.moves.Movable;
import chess.model.moves.PromotionMove;
import chess.model.pieces.Piece;
import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import javax.annotation.Nullable;
import java.util.Objects;

public class ChessBoardView extends Region {
    private static final int numRows = 8;
    private static final int numCols = 8;

    private final GridPane board;
    private final ChessView.MoveController controller;
    private ChessPieceView movingPiece = null;

    public ChessBoardView(Piece[] pieceArray, ChessView.MoveController controller,
                          ChessView.GameDataRetriever dataRetriever) {
        this.board = createBoard();
        this.controller = controller;

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if (pieceArray[row * 8 + col] != null) {
                    StackPane square =
                            (StackPane) board.getChildren().get((numRows - row - 1) * 8 + col);
                    ImageView piece =
                            new ChessPieceView(pieceArray[row * 8 + col], square.widthProperty());
                    square.getChildren().add(piece);
                }
            }
        }

        board.prefHeightProperty().bind(this.heightProperty());
        board.prefWidthProperty().bind(this.widthProperty());
        this.getChildren().add(board);
    }

    public Region getRegion() {
        return this;
    }

    private GridPane createBoard() {
        GridPane gridPane = new GridPane();

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                ChessSquare square = new ChessSquare((row + col) % 2 == 0,
                        gridPane.widthProperty().divide(numCols),
                        gridPane.heightProperty().divide(numRows), col, 7 - row);

                makeSquareDraggable(square);

                gridPane.add(square, col, row);
            }
        }
        return gridPane;
    }

    private void makeSquareDraggable(ChessSquare square) {
        square.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && square.hasPiece()) {
                Objects.requireNonNull(square.getPieceView()).opacityProperty().set(0.5);

                movingPiece = new ChessPieceView(square.getPieceView().getPiece(),
                        square.widthProperty());
                Bounds bounds = board.localToScene(board.getBoundsInLocal());
                movingPiece.relocate(
                        event.getSceneX() - movingPiece.getFitWidth() / 2 - bounds.getMinX(),
                        event.getSceneY() - movingPiece.getFitWidth() / 2 - bounds.getMinY());
                this.getChildren().add(movingPiece);
            }

            event.consume();
        });

        square.setOnMouseReleased(event -> {
            ChessPieceView piece = square.getPieceView();

            if (piece == null) {
                return;
            }

            square.getPieceView().opacityProperty().set(1);
            this.getChildren().remove(movingPiece);
            movingPiece = null;

            if (event.getButton() != MouseButton.PRIMARY) {
                event.consume();
                return;
            }

            ChessSquare targetSquare = getSquareAt(event.getSceneX(), event.getSceneY());

            if (targetSquare != null) {
                controller.makeMove(square.getCoordinate(), targetSquare.getCoordinate());
            }

            event.consume();
        });

        square.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && movingPiece != null) {
                Bounds bounds = board.localToScene(board.getBoundsInLocal());
                movingPiece.relocate(
                        event.getSceneX() - movingPiece.getFitWidth() / 2 - bounds.getMinX(),
                        event.getSceneY() - movingPiece.getFitWidth() / 2 - bounds.getMinY());
            }
            event.consume();
        });
    }

    public void displayMove(Movable move) {
        if (move == null) {
            return;
        }

        // If castling, move the rook
        if (move instanceof CastlingMove castlingMove) {
            ChessPieceView rook = getSquareAt(castlingMove.getRookStart()).removePiece();
            getSquareAt(castlingMove.getRookEnd()).addPiece(rook);
        }

        // If EnPassant, remove the captured pawn
        if (move instanceof EnPassantMove enPassantMove) {
            getSquareAt(enPassantMove.getCaptureCoordinate()).removePiece();
        }

        // Capture any piece on final square
        getSquareAt(move.getEndCoordinate()).removePiece();

        // Move Piece from start to End
        ChessPieceView movingPiece = getSquareAt(move.getStartCoordinate()).removePiece();
        getSquareAt(move.getEndCoordinate()).addPiece(movingPiece);

        if (move instanceof PromotionMove promotionMove) {
            Objects.requireNonNull(getSquareAt(move.getEndCoordinate()).getPieceView())
                    .promote(promotionMove.getPromotedPiece());
        }
    }

    private ChessSquare getSquareAt(ChessCoordinate coordinate) {
        return (ChessSquare) board.getChildren()
                .get((7 - coordinate.getRank()) * numRows + coordinate.getFile());
    }

    private @Nullable ChessSquare getSquareAt(double sceneX, double sceneY) {
        Bounds bounds = board.localToScene(board.getBoundsInLocal());

        if (bounds.contains(sceneX, sceneY)) {
            double localX = sceneX - bounds.getMinX();
            double localY = sceneY - bounds.getMinY();

            int row = (int) (localX / (bounds.getWidth() / numCols));
            int col = (int) (localY / (bounds.getHeight() / numRows));

            return (ChessSquare) board.getChildren().get(numCols * col + row);
        } else {
            return null;
        }
    }

    public double getMinimumWidth() {
        return numCols * ChessSquare.MIN_SIZE;
    }

    public double getMinimumHeight() {
        return numRows * ChessSquare.MIN_SIZE;
    }
}
