package chess.controller;

import chess.ChessCoordinate;
import chess.model.GameModel;
import chess.model.chessai.ChessAI;
import chess.model.chessai.PositionEvaluator;
import chess.model.moves.Movable;
import chess.model.pieces.Piece;
import chess.view.ChessView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static chess.model.GameModel.*;

/**
 * This class controls both the model and the view for the
 * Chess game.
 */
public class ChessController extends Application {

    private static final boolean AI_ON = true;

    private static final int MINIMUM_DEPTH = 1;
    private static final int SEARCH_TIME = 2_000;

    private GameModel gameModel;
    private ChessView view;
    private ChessAI chessAI;

    private ExecutorService aiExecutor;
    private ExecutorService finishGameExecutor;
    private CompletableFuture<Void> futureAIMove;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();

        gameModel = new GameModel();
        view = new ChessView(gameModel.getBoard().getPieceArray(), new ViewController(),
                new ChessView.GameDataRetriever() {
                    @Override
                    public List<ChessCoordinate> getReachableCoordinates(ChessCoordinate start) {
                        return gameModel.getLegalMoves().toList().stream()
                                .filter(move -> move.getStartCoordinate().equals(start))
                                .map(Movable::getEndCoordinate).toList();
                    }

                    @Override
                    public char getTurn() {
                        return gameModel.getTurn();
                    }

                    @Override
                    public boolean canMove(ChessCoordinate coordinate) {
                        Piece piece = gameModel.getBoard().getPieceOn(coordinate);
                        return piece != null && piece.getColor() == gameModel.getTurn();
                    }

                    @Override
                    public @Nullable Movable getLastMove() {
                        return gameModel.getLastMove();
                    }
                });
        chessAI = new ChessAI(new PositionEvaluator(gameModel), gameModel, true, true);
        aiExecutor = Executors.newSingleThreadExecutor();
        finishGameExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Chess Program");

        primaryStage.setScene(view.getScene());
        primaryStage.show();
        primaryStage.setAlwaysOnTop(true);
        primaryStage.requestFocus();

        final double titleBarHeight = primaryStage.getHeight() - view.getScene().getHeight();

        primaryStage.setMinHeight(view.getMinimumHeight() + titleBarHeight);
        primaryStage.setMinWidth(view.getMinimumWidth());
        view.adjustSize();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        aiExecutor.shutdownNow();
        finishGameExecutor.shutdownNow();
    }

    /**
     * Attempts to make a move given two coordinates.
     *
     * @param startCoordinate the starting coordinate
     * @param endCoordinate   the ending coordinate
     */
    private void makeMove(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        if (!gameModel.move(startCoordinate, endCoordinate, Piece.WHITE_QUEEN)) {
            return;
        }

        if (AI_ON) {
            makeAIMove();
        } else if (gameModel.getGameOverStatus() != IN_PROGRESS) {
            System.out.println("GAME OVER");
        }

        view.displayMove(gameModel.getLastMove());
    }

    private void undoMove() {
        gameModel.undoLastMove();
        view.setPosition(gameModel.getBoard().getPieceArray());
    }

    private void makeAIMove() {
        // If game is over, don't make the move
        if (gameModel.getGameOverStatus() != IN_PROGRESS) {
            return;
        }

        // If the future move is not done (previously computing) then return
        if (futureAIMove != null && !futureAIMove.isDone()) {
            return;
        }

        // Calculate the next best move, and make that move
        futureAIMove = CompletableFuture.runAsync(
                        () -> gameModel.move(chessAI.getBestMove(MINIMUM_DEPTH, SEARCH_TIME)),
                        aiExecutor)
                .thenRun(this::printAndUpdate).exceptionally((ex) -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void printAndUpdate() {
        Platform.runLater(() -> view.displayMove(gameModel.getLastMove()));
        if (gameModel.getGameOverStatus() != IN_PROGRESS) {
            System.out.printf("GAME OVER (%s)\n", switch (gameModel.getGameOverStatus()) {
                case DRAW -> "Draw";
                case LOSER -> gameModel.getTurn() == WHITE ? "White lost" : "Black Lost";
                default -> "Error";
            });
        }
    }

    private void letAIFinishGame() {
        if (gameModel.getGameOverStatus() == IN_PROGRESS) {
            CompletableFuture.runAsync(this::makeAIMove, finishGameExecutor).thenRunAsync(() -> {
                futureAIMove.join();
                letAIFinishGame();
            }, finishGameExecutor).exceptionally((ex) -> {
                ex.printStackTrace();
                return null;
            });
        }
    }

    private class ViewController implements ChessView.ViewControlable {
        @Override
        public void makeMove(ChessCoordinate start, ChessCoordinate end) {
            ChessController.this.makeMove(start, end);
        }

        @Override
        public void processCommand(String command) {
            String[] splitCommand = command.split("\\s+");

            if (splitCommand.length == 0) {
                return;
            }

            switch (splitCommand[0].toLowerCase()) {
                case "clear", "c" -> {
                    gameModel.setPosition("8/8/8/8/8/8/8/8 w - - 0 1");
                    view.setPosition(gameModel.getBoard().getPieceArray());
                }
                case "position", "pos", "p" -> {
                    if (splitCommand.length == 1) {
                        System.out.println("No FEN given");
                    }

                    String fen = String.join(" ",
                            Arrays.copyOfRange(splitCommand, 1, splitCommand.length));
                    gameModel.setPosition(fen);
                    view.setPosition(gameModel.getBoard().getPieceArray());
                }
                case "undo" -> undoMove();
                case "finish", "f" -> letAIFinishGame();
            }
        }
    }
}

