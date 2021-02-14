package main.java.model;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.pieces.King;
import main.java.model.pieces.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class GameModel {

    private static final boolean DEBUG_MODE = false;

    private final BoardModel board;
    private final List<Move> moveHistory;

    private char turn;
    private boolean isOver;
    private char winner;

    public GameModel() {
        this.board = ChessBoardFactory.createNormalBoard();
        this.turn = 'w';
        this.moveHistory = new ArrayList<>();
        checkRep();
    }

    public GameModel(GameModel gameModel) {
        this.board = new BoardModel(gameModel.getBoard());
        this.moveHistory = new ArrayList<>(gameModel.moveHistory);
        this.turn = gameModel.getTurn();
        checkRep();
    }

    public GameModel(BoardModel board) {
        this.board = board;
        this.turn = 'w';
        this.moveHistory = new ArrayList<>();
        checkRep();
    }

    public BoardModel getBoard() {
        return board;
    }

    /**
     * Attempts to make a move given two Coordinates. If the coordinates correspond
     * to a legal move, the move will be made. True is returned if the move was
     * successful, false otherwise.
     *
     * @param startCoordinate the starting coordinate of the moving Piece.
     * @param endCoordinate the ending coordinate of the moving Piece.
     * @return weather or not the move was successful.
     */
    public boolean move(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        checkRep();
        boolean didMove = false;

        // Check that moves are both on screen.
        if (startCoordinate != null && endCoordinate != null) {
            Move currentMove = null;
            Piece movingPiece = board.getPieceOn(startCoordinate);
            if (movingPiece != null) {
                for (Move move : movingPiece.getLegalMoves(this)) {
                    if (startCoordinate.equals(move.getStartingCoordinate()) && endCoordinate.equals(move.getEndingCoordinate())) {
                        currentMove = move;
                        break;
                    }
                }
            }
            didMove = move(currentMove);
        }

        checkRep();
        return didMove;
    }

    /**
     * Attempts to make the given move. If the move is legal, true is returned,
     * otherwise, false is returned.
     *
     * @param move a non-null move.
     * @return true if the move is successful, false otherwise.
     */
    public boolean move(Move move) {
        checkRep();
        boolean didMove = false;

        if (move != null && move.getMovingPiece().getLegalMoves(this).contains(move) && move.getMovingPiece().getColor() == turn) {
            board.move(move);
            moveHistory.add(move);
            turn = (turn == 'w') ? 'b' : 'w';
            checkGameOver(turn == 'w');
            didMove = true;
        }

        checkRep();
        return didMove;
    }

    private void checkGameOver(final boolean isWhitesMove) {
        King relevantKing = isWhitesMove ? board.getWhiteKing() : board.getBlackKing();
        AtomicBoolean foundMove = new AtomicBoolean(false);
        Set<Piece> relevantPieces = isWhitesMove ? board.getWhitePieces() : board.getBlackPieces();
        for (Piece piece : relevantPieces.stream().collect(Collectors.toUnmodifiableSet())) {
            if (!piece.getLegalMoves(this).isEmpty()) {
                foundMove.set(true);
                break;
            }
        }//*/
        if (foundMove.get()) {
            isOver = false;
            winner = 'N';
        } else {
            isOver = true;
            if (relevantKing.isAttacked(relevantKing.getCoordinate(), board)) {
                winner = isWhitesMove ? 'b' : 'w';
            } else {
                winner = 's';
            }
        }
    }

    public void undoMove(Move move) {
        checkRep();
        if (moveHistory.size() > 0 && moveHistory.get(moveHistory.size() - 1).equals(move)) {
            board.undoMove(move);
            moveHistory.remove(moveHistory.size() - 1);
            turn = (turn == 'w') ? 'b' : 'w';
            isOver = false;
            winner = 'N';
        }
        checkRep();
    }

    public Move getLastMove() {
        return moveHistory.size() == 0 ? null : moveHistory.get(moveHistory.size() - 1);
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public char getTurn() {
        return turn;
    }

    public boolean isOver() {
        return isOver;
    }

    public char getWinner() {
        return winner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameModel)) return false;
        GameModel gameModel = (GameModel) o;
        return turn == gameModel.turn && board.equals(gameModel.board) && moveHistory.equals(gameModel.moveHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, moveHistory, turn);
    }

    private void checkRep() {
        if (DEBUG_MODE) {
            if (board == null || moveHistory == null) {
                throw new RuntimeException("Representation is incorrect.");
            }
        }
    }
}
