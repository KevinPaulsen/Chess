package chess.view;

import chess.ChessCoordinate;
import chess.model.moves.CastlingMove;
import chess.model.moves.EnPassantMove;
import chess.model.moves.Movable;
import chess.model.moves.PromotionMove;
import chess.model.pieces.Piece;
import chess.util.BitIterator;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ChessBoardView extends Region {
    private static final int numRows = 8;
    private static final int numCols = 8;

    private final GridPane board;
    private final ChessView.ViewControlable controller;
    private final ChessView.GameDataRetriever retriever;
    private final Set<ChessSquareView> moveDestinations;
    private final Set<ChessSquareView> lastMoved;
    private final Set<ChessSquareView> highlighted;
    private DragData dragData;

    public ChessBoardView(Piece[] pieceArray, ChessView.ViewControlable controller,
                          ChessView.GameDataRetriever dataRetriever) {
        this.board = createBoard();
        this.controller = controller;
        this.retriever = dataRetriever;

        this.moveDestinations = new HashSet<>();
        this.lastMoved = new HashSet<>();
        this.highlighted = new HashSet<>();

        setPieces(pieceArray);

        this.board.prefHeightProperty().bind(this.heightProperty());
        this.board.prefWidthProperty().bind(this.widthProperty());
        this.getChildren().add(board);
    }

    /**
     * Initialize the board by creating pieces, and making sure they are properly resizeable. Also
     * make sure each square is intractable.
     *
     * @return the created board
     */
    private GridPane createBoard() {
        GridPane gridPane = new GridPane();

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                ChessSquareView square = new ChessSquareView((row + col) % 2 == 0,
                        gridPane.widthProperty().divide(numCols),
                        gridPane.heightProperty().divide(numRows), col, 7 - row);

                makeSquareDraggable(square);

                gridPane.add(square, col, row);
            }
        }
        return gridPane;
    }

    /**
     * Add all the Pieces to the correct squares.
     *
     * @param pieceArray the array of pieces from the Model. piece array should always be
     *                   {@link #numRows} x {@link #numCols} long.
     */
    public void setPieces(Piece[] pieceArray) {
        if (pieceArray.length != numCols * numRows) {
            return;
        }

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                ChessSquareView square =
                        (ChessSquareView) board.getChildren().get((numRows - row - 1) * 8 + col);
                Piece piece = pieceArray[row * 8 + col];

                if (piece == null) {
                    square.removePiece();
                } else {
                    square.addPiece(new ChessPieceView(piece, square.widthProperty()));
                }
            }
        }

        Movable move = retriever.getLastMove();
        if (move != null) {
            clearAndUpdateLastMoved(getSquareAt(move.getStartCoordinate()),
                    getSquareAt(move.getEndCoordinate()));
        } else {
            clearAndUpdateLastMoved();
        }
    }

    /**
     * Add the logic to make sure that the square is draggable.
     *
     * @param square the square to make draggable
     */
    private void makeSquareDraggable(ChessSquareView square) {
        square.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                startDrag(square, event.getSceneX(), event.getSceneY());
            }

            event.consume();
        });

        square.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                endDrag(square, event.getSceneX(), event.getSceneY());
            }

            event.consume();
        });

        square.setOnMouseDragged(event -> {
            updateDragged(event.getSceneX(), event.getSceneY());
            event.consume();
        });
    }

    private synchronized void clearAndUpdateLastMoved(ChessSquareView... squares) {
        lastMoved.forEach(ChessSquareView::notLastMove);
        lastMoved.clear();
        Collections.addAll(lastMoved, squares);
        lastMoved.forEach(ChessSquareView::isLastMove);
    }

    private ChessSquareView getSquareAt(ChessCoordinate coordinate) {
        return (ChessSquareView) board.getChildren()
                .get((7 - coordinate.getRank()) * numRows + coordinate.getFile());
    }

    private synchronized void startDrag(ChessSquareView square, double sceneX, double sceneY) {
        // Make sure drag is possible
        if (!retriever.canMove(square.getCoordinate())) {
            return;
        }

        // If DragData is not null, then drag is already happening
        if (dragData != null) {
            return;
        }

        // Initialize DragData and move piece to cursor
        dragData = new DragData(square);
        Bounds bounds = board.localToScene(board.getBoundsInLocal());
        dragData.movePiece(sceneX, sceneY, bounds.getMinX(), bounds.getMinY());
        this.getChildren().add(dragData.draggedPiece);

        moveDestinations.addAll(retriever.getReachableCoordinates(square.getCoordinate()).stream()
                .map(this::getSquareAt).toList());
        for (ChessSquareView destinationSquare : moveDestinations) {
            destinationSquare.setMoveDestination();
        }
    }

    private synchronized void endDrag(ChessSquareView square, double sceneX, double sceneY) {
        // If Drag not happening, return
        if (dragData == null) {
            return;
        }

        // Make piece not transparent
        dragData.originalPiece.makeNotGhost();
        dragData.selected.unselect();

        // Remove the dragged piece
        this.getChildren().remove(dragData.draggedPiece);

        // remove move destinations
        for (ChessSquareView moveDestination : moveDestinations) {
            moveDestination.removeModeDestination();
        }
        moveDestinations.clear();

        // Attempt to make a move
        ChessSquareView targetSquare = getSquareAt(sceneX, sceneY);
        if (targetSquare != null) {
            controller.makeMove(square.getCoordinate(), targetSquare.getCoordinate());
        }

        // Clear drag data
        dragData = null;
    }

    private synchronized void updateDragged(double sceneX, double sceneY) {
        if (dragData == null) {
            return;
        }

        Bounds bounds = board.localToScene(board.getBoundsInLocal());
        dragData.movePiece(sceneX, sceneY, bounds.getMinX(), bounds.getMinY());
    }

    private @Nullable ChessSquareView getSquareAt(double sceneX, double sceneY) {
        Bounds bounds = board.localToScene(board.getBoundsInLocal());

        if (bounds.contains(sceneX, sceneY)) {
            double localX = sceneX - bounds.getMinX();
            double localY = sceneY - bounds.getMinY();

            int row = (int) (localX / (bounds.getWidth() / numCols));
            int col = (int) (localY / (bounds.getHeight() / numRows));

            return (ChessSquareView) board.getChildren().get(numCols * col + row);
        } else {
            return null;
        }
    }

    public Region getRegion() {
        return this;
    }

    public void displayMove(Movable move) {
        if (move == null) {
            return;
        }

        ChessSquareView start = getSquareAt(move.getStartCoordinate());
        ChessSquareView end = getSquareAt(move.getEndCoordinate());

        clearAndUpdateLastMoved(start, end);
        clearHighlighted();

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

    private void clearHighlighted() {
        synchronized (highlighted) {
            for (ChessSquareView square : highlighted) {
                square.removeHighlight();
            }
            highlighted.clear();
        }
    }

    public double getMinimumWidth() {
        return numCols * ChessSquareView.MIN_SIZE;
    }

    public double getMinimumHeight() {
        return numRows * ChessSquareView.MIN_SIZE;
    }

    public void displayBits(long bitBoard) {
        clearHighlighted();
        BitIterator bitIterator = new BitIterator(bitBoard);

        while (bitIterator.hasNext()) {
            highlightSquares(ChessCoordinate.getChessCoordinate(bitIterator.next()));
        }
    }

    public void highlightSquares(ChessCoordinate... coordinates) {
        synchronized (highlighted) {
            for (ChessCoordinate coordinate : coordinates) {
                getSquareAt(coordinate).highlight();
                highlighted.add(getSquareAt(coordinate));
            }
        }
    }

    private record DragData(ChessPieceView originalPiece, ChessPieceView draggedPiece,
                            ChessSquareView selected) {

        private DragData(ChessSquareView selected) {
            this(selected.getPieceView(), new ChessPieceView(selected.getPieceView().getPiece(),
                    selected.widthProperty()), selected);
        }

        private DragData {
            originalPiece.makeGhost();
            selected.select();
        }

        public void movePiece(double sceneX, double sceneY, double minX, double minY) {
            draggedPiece.relocate(sceneX - draggedPiece.getFitWidth() / 2 - minX,
                    sceneY - draggedPiece.getFitWidth() / 2 - minY);
        }
    }
}
