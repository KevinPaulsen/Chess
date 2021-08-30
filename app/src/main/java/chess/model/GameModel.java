package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
import chess.model.pieces.King;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents the model for a standard chess game. This
 * class is responsible for game rules and managing all the parts of
 * the chess game.
 */
public class GameModel {

    /**
     * Toggle for if check rep should be called.
     */
    private static final boolean DEBUG_MODE = false;

    /**
     * The model for the bard of the chess game.
     */
    private final BoardModel board;

    /**
     * The list of past moves that have occurred in this chess game.
     */
    private final List<Move> moveHistory;

    /**
     * The tracker for which players turn it is to move.
     */
    private char turn;

    /**
     * The current target for an En Passant capture. This is null
     * if no En Passant is possible.
     */
    private final ChessCoordinate enPassantTarget;

    /**
     * The flag for if white can castle king-side.
     */
    private final boolean whiteKingCastle;

    /**
     * The flag for if white can castle queen-side.
     */
    private final boolean whiteQueenCastle;

    /**
     * The flag for if black can castle king-side.
     */

    private final boolean blackKingCastle;
    /**
     * The flag for if black can castle queen-side.
     */
    private final boolean blackQueenCastle;

    /**
     * The default constructor that creates a normal game.
     */
    public GameModel() {
        this(ChessBoardFactory.createNormalBoard(), 'w', true,
                true, true, true, null);
    }

    /**
     * TODO: FIX THIS
     */
    @SuppressWarnings("all")
    public GameModel(GameModel gameModel) {
        this(null, 'w', false, false, false,
                false, null);
    }

    /**
     * Creates a Game model with all the needed information.
     *
     * @param board the board this game model follows.
     * @param turn the current turn.
     * @param whiteKingCastle weather white can castle King-side.
     * @param whiteQueenCastle weather white can castle Queen-side.
     * @param blackKingCastle weather black can castle King-side.
     * @param blackQueenCastle weather black can castle Queen-side.
     * @param enPassantTarget the target coordinate for En Passant.
     */
    public GameModel(BoardModel board, char turn, boolean whiteKingCastle, boolean whiteQueenCastle,
                     boolean blackKingCastle, boolean blackQueenCastle, ChessCoordinate enPassantTarget) {
        this.board = board;
        this.turn = turn;
        this.whiteKingCastle = whiteKingCastle;
        this.whiteQueenCastle = whiteQueenCastle;
        this.blackKingCastle = blackKingCastle;
        this.blackQueenCastle = blackQueenCastle;
        this.enPassantTarget = enPassantTarget;
        this.moveHistory = new ArrayList<>();

        checkRep();
    }

    /**
     * @return the board model of this game.
     */
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
        boolean didMove = false;

        // Check that moves are both on screen.
        if (startCoordinate != null && endCoordinate != null) {
            Move currentMove = null;
            Piece movingPiece = board.getPieceOn(startCoordinate);
            if (movingPiece != null) {
                for (Move move : new MoveGenerator(this).generateMoves()) {
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

        if (move != null && move.getMovingPiece().getColor() == turn) {
            board.move(move);
            moveHistory.add(move);
            turn = (turn == 'w') ? 'b' : 'w';
            didMove = true;
        }

        checkRep();
        return didMove;
    }

    public void undoMove(Move move) {
        checkRep();
        if (moveHistory.size() > 0 && moveHistory.get(moveHistory.size() - 1).equals(move)) {
            board.undoMove(move);
            moveHistory.remove(moveHistory.size() - 1);
            turn = (turn == 'w') ? 'b' : 'w';
        }
        checkRep();
    }

    public boolean canKingSideCastle(char color) {
        return color == 'w' ? whiteKingCastle : blackKingCastle;
    }

    public boolean canQueenSideCastle(char color) {
        return color == 'w' ? whiteQueenCastle : blackQueenCastle;
    }

    public ChessCoordinate getEnPassantTarget() {
        return enPassantTarget;
    }

    public List<Move> getLegalMoves() {
        return new MoveGenerator(this).generateMoves();
    }

    public Move cloneMove(Move move) {
        return new Move(move.getEndingCoordinate(), board.getPieceOn(move.getStartingCoordinate()),
                move.getInteractingPieceEnd(), board.getPieceOn(move.getInteractingPieceStart()),
                move.getPromotedPiece());
    }

    public Move getLastMove() {
        return moveHistory.size() == 0 ? null : moveHistory.get(moveHistory.size() - 1);
    }

    public char getTurn() {
        return turn;
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
