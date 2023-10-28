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
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

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
    private static final int SEARCH_TIME = 1_000;

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
        view = new ChessView(gameModel.getBoard().getPieceArray(), this::makeMove);
        chessAI = new ChessAI(new PositionEvaluator(gameModel), gameModel, true, true);
        aiExecutor = Executors.newSingleThreadExecutor();
        finishGameExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Chess Program");

        primaryStage.setScene(view.getScene());
        primaryStage.requestFocus();
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
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
    private Movable makeMove(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        if (gameModel.move(startCoordinate, endCoordinate, Piece.WHITE_QUEEN)) {

            if (AI_ON) {
                makeAIMove();
            } else {
                if (gameModel.getGameOverStatus() != IN_PROGRESS) {
                    System.out.println("GAME OVER");
                }
            }

            return gameModel.getLastMove();
        }

        return null;
    }

    private void undoMove() {
        gameModel.undoLastMove();
    }

    private void makeAIMove() {
        if (futureAIMove == null || futureAIMove.isDone()) {
            futureAIMove = CompletableFuture.runAsync(() -> gameModel.move(chessAI.getBestMove(MINIMUM_DEPTH, SEARCH_TIME)), aiExecutor).thenRun(this::printAndUpdate).exceptionally((ex) -> {
                ex.printStackTrace();
                return null;
            });
        }
    }

    private void printAndUpdate() {
        Platform.runLater(() -> {
            view.displayMove(gameModel.getLastMove());
        });
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
}

