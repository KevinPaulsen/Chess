package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static chess.model.ChessBoardFactory.*;
import static chess.model.ChessBoardFactory.EMPTY;
import static chess.model.pieces.Piece.BLACK_ROOK;
import static chess.model.pieces.Piece.WHITE_ROOK;

/**
 * This class represents the model for a standard chess game. This
 * class is responsible for game rules and managing all the parts of
 * the chess game.
 */
public class GameModel {

    /**
     * The bit mask that represents that white can king-side castle.
     */
    private static final long WHITE_KING_SIDE_CASTLE_MASK = 1L;

    /**
     * The bit mask that represents that white can king-side castle.
     */
    private static final long WHITE_QUEEN_SIDE_CASTLE_MASK = 2L;

    /**
     * The bit mask that represents that white can king-side castle.
     */
    private static final long BLACK_KING_SIDE_CASTLE_MASK = 4L;

    /**
     * The bit mask that represents that white can king-side castle.
     */
    private static final long BLACK_QUEEN_SIDE_CASTLE_MASK = 8L;

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
     * The list of each state the board was in before each move. This List
     * should be 1 larger than moveHistory.
     */
    private final List<FastMap> stateHistory;

    /**
     * The tracker for which players turn it is to move.
     */
    private char turn;

    /**
     * The current target for an En Passant capture. This is null
     * if no En Passant is possible.
     */
    private ChessCoordinate enPassantTarget;

    private final static int[][] TEST_BOARD_PAWNS = {
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, W_KING, EMPTY, B_PAWN, EMPTY},
            {EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KING},
            {EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, EMPTY, B_PAWN, EMPTY},
            {EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, EMPTY},
            {EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, W_PAWN, EMPTY, EMPTY, B_QUEEN, EMPTY, EMPTY, EMPTY},
    };

    /**
     * The default constructor that creates a normal game.
     */
    public GameModel() {
        this(ChessBoardFactory.createNormalBoard(), 'w', true,
                true, true, true, null);
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
        this.enPassantTarget = enPassantTarget;
        this.moveHistory = new ArrayList<>(200);
        this.stateHistory = new ArrayList<>(200);

        addInitialState(whiteKingCastle, whiteQueenCastle, blackKingCastle, blackQueenCastle);

        checkRep();
    }

    private void addInitialState(boolean whiteKingCastle, boolean whiteQueenCastle,
                                 boolean blackKingCastle, boolean blackQueenCastle) {
        FastMap stateMap = new FastMap();
        stateMap.mergeMask(whiteKingCastle ? WHITE_KING_SIDE_CASTLE_MASK : 0);
        stateMap.mergeMask(whiteQueenCastle ? WHITE_QUEEN_SIDE_CASTLE_MASK : 0);
        stateMap.mergeMask(blackKingCastle ? BLACK_KING_SIDE_CASTLE_MASK : 0);
        stateMap.mergeMask(blackQueenCastle ? BLACK_QUEEN_SIDE_CASTLE_MASK : 0);
        stateHistory.add(stateMap);
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
            checkCastling(board.getWhiteKingCoord(), board.getBlackKingCoord());
            checkEnPassant(move);
            turn = (turn == 'w') ? 'b' : 'w';
            didMove = true;
        }

        checkRep();
        return didMove;
    }

    private void checkEnPassant(Move lastMove) {
        if (lastMove != null) {
            if ((lastMove.getMovingPiece() == Piece.WHITE_PAWN || lastMove.getMovingPiece() == Piece.BLACK_PAWN)
                    && Math.abs(lastMove.getStartingCoordinate().getRank() - lastMove.getEndingCoordinate().getRank()) == 2) {
                int rank = (int) (0.6 * (lastMove.getStartingCoordinate().getRank() - 3.5) + 3.5);
                enPassantTarget = BoardModel.getChessCoordinate(lastMove.getEndingCoordinate().getFile(), rank);
            } else {
                enPassantTarget = null;
            }
        } else {
            enPassantTarget = null;
        }
    }

    private void checkCastling(ChessCoordinate whiteKingCoord, ChessCoordinate blackKingCoord) {
        FastMap state = new FastMap();
        state.merge(stateHistory.get(stateHistory.size() - 1));
        if (canKingSideCastle('w')
                && !(board.getPieceOn(BoardModel.getChessCoordinate(7, 0)) == WHITE_ROOK
                && whiteKingCoord.equals(BoardModel.getChessCoordinate(4, 0)))) {
            state.flip(WHITE_KING_SIDE_CASTLE_MASK);
        }
        if (canKingSideCastle('b')
                && !(board.getPieceOn(BoardModel.getChessCoordinate(7, 7)) == BLACK_ROOK
                && blackKingCoord.equals(BoardModel.getChessCoordinate(4, 7)))) {
            state.flip(BLACK_KING_SIDE_CASTLE_MASK);
        }
        if (canQueenSideCastle('w')
                && !(board.getPieceOn(BoardModel.getChessCoordinate(0, 0)) == WHITE_ROOK
                && whiteKingCoord.equals(BoardModel.getChessCoordinate(4, 0)))) {
            state.flip(WHITE_QUEEN_SIDE_CASTLE_MASK);
        }
        if (canQueenSideCastle('b')
                && !(board.getPieceOn(BoardModel.getChessCoordinate(0, 7)) == BLACK_ROOK
                && blackKingCoord.equals(BoardModel.getChessCoordinate(4, 7)))) {
            state.flip(BLACK_QUEEN_SIDE_CASTLE_MASK);
        }
        stateHistory.add(state);
    }

    public void undoMove(Move move) {
        checkRep();
        if (moveHistory.size() > 0 && moveHistory.get(moveHistory.size() - 1).equals(move)) {
            board.undoMove(move);
            checkEnPassant(moveHistory.size() >= 2 ? moveHistory.get(moveHistory.size() - 2) : null);
            stateHistory.remove(stateHistory.size() - 1);
            moveHistory.remove(moveHistory.size() - 1);
            turn = (turn == 'w') ? 'b' : 'w';
        }
        checkRep();
    }

    public boolean canKingSideCastle(char color) {
        FastMap currentState = stateHistory.get(stateHistory.size() - 1);
        return color == 'w' ? currentState.isMarked(0) : currentState.isMarked(2);
    }

    public boolean canQueenSideCastle(char color) {
        FastMap currentState = stateHistory.get(stateHistory.size() - 1);
        return color == 'w' ? currentState.isMarked(1) : currentState.isMarked(3);
    }

    public ChessCoordinate getEnPassantTarget() {
        return enPassantTarget;
    }

    public List<Move> getLegalMoves() {
        return new MoveGenerator(this).generateMoves();
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
