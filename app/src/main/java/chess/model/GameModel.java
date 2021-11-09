package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static chess.model.pieces.Piece.BLACK_ROOK;
import static chess.model.pieces.Piece.WHITE_ROOK;

/**
 * This class represents the model for a standard chess game. This
 * class is responsible for game rules and managing all the parts of
 * the chess game.
 */
public class GameModel {

    /**
     * Char that shows that it is whites move.
     */
    private static final char WHITE = 'w';

    /**
     * Char that shows that it is blacks move.
     */
    private static final char BLACK = 'b';

    /**
     * State that we are still in the middle of a game.
     */
    private static final char IN_PROGRESS = 'p';

    /**
     * State that the game is over. And the player to move has lost.
     */
    private static final char LOSER = 'l';

    /**
     * State that the game is over, but ended in a draw.
     */
    private static final char DRAW = 'd';

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
     * The map that tracks the number of times each position has occurred.
     * This maps from hashCode -> number of times this hashCode has appeared.
     */
    private final Map<Integer, Integer> positionTracker;

    /**
     * The list of legal moves in this position.
     */
    private List<Move> legalMoves;

    /**
     * The move generator for this game.
     */
    private final MoveGenerator moveGenerator;

    /**
     * The tracker for which players turn it is to move.
     */
    private char turn;

    /**
     * The tracker for the current state of the game. This will be
     * IN_PROGRESS, LOSER, or DRAW.
     */
    private char gameState;

    /**
     * The current target for an En Passant capture. This is null
     * if no En Passant is possible.
     */
    private ChessCoordinate enPassantTarget;

    /**
     * The default constructor that creates a normal game.
     */
    public GameModel() {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
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
        this.positionTracker = new HashMap<>();
        this.gameState = IN_PROGRESS;
        this.moveGenerator = new MoveGenerator(this);

        addInitialState(whiteKingCastle, whiteQueenCastle, blackKingCastle, blackQueenCastle);

        this.legalMoves = this.moveGenerator.generateMoves();
        positionTracker.put(hashCode(), 1);

        checkRep();
    }

    public GameModel(String FEN) {
        String[] fenSections = FEN.split(" ");
        this.board = new BoardModel(fenSections[0]);
        this.turn = fenSections[1].charAt(0);
        this.enPassantTarget = !fenSections[3].equals("-") ? BoardModel.getChessCoordinate(fenSections[3].charAt(0),
                Integer.parseInt(fenSections[3].substring(1)) + 1) : null;
        this.moveHistory = new ArrayList<>(200);
        this.stateHistory = new ArrayList<>(200);
        this.positionTracker = new HashMap<>();
        this.gameState = IN_PROGRESS;
        this.moveGenerator = new MoveGenerator(this);

        boolean whiteKingCastle = fenSections[2].contains("K");
        boolean whiteQueenCastle = fenSections[2].contains("Q");
        boolean blackKingCastle = fenSections[2].contains("k");
        boolean blackQueenCastle = fenSections[2].contains("q");

        addInitialState(whiteKingCastle, whiteQueenCastle, blackKingCastle, blackQueenCastle);

        this.legalMoves = this.moveGenerator.generateMoves();

        positionTracker.put(hashCode(), 1);

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
        initZobristHash();
    }

    private void initZobristHash() {

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
    public boolean move(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate, Piece promoPiece) {
        boolean didMove = false;

        // Check that moves are both on screen.
        if (startCoordinate != null && endCoordinate != null) {
            Move currentMove = null;
            Piece movingPiece = board.getPieceOn(startCoordinate);
            if (movingPiece != null) {
                for (Move move : new MoveGenerator(this).generateMoves()) {
                    if (startCoordinate.equals(move.getStartingCoordinate())
                            && endCoordinate.equals(move.getEndingCoordinate())) {
                        if (!move.doesPromote() || move.getPromotedPiece() == promoPiece) {
                            currentMove = move;
                            break;
                        }
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

        if (gameState == IN_PROGRESS && move != null && move.getMovingPiece().getColor() == turn) {
            board.move(move);
            moveHistory.add(move);
            checkCastling(board.getWhiteKingCoord(), board.getBlackKingCoord());
            checkEnPassant(move);
            turn = (turn == WHITE) ? BLACK : WHITE;

            positionTracker.merge(hashCode(), 1, Integer::sum);

            didMove = true;
        }

        checkRep();
        return didMove;
    }

    private void checkGameOver() {
        if (positionTracker.containsKey(this) && positionTracker.get(this) >= 3) {
            gameState = DRAW;
        } else {
            if (legalMoves.size() == 0) {
                ChessCoordinate kingToMove = turn == WHITE ? board.getWhiteKingCoord() : board.getBlackKingCoord();
                if (moveGenerator.getOpponentAttackMap().isMarked(kingToMove.getOndDimIndex())) {
                    gameState = LOSER;
                } else {
                    gameState = DRAW;
                }
            } else {
                gameState = IN_PROGRESS;
            }
        }
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
        if (canKingSideCastle(WHITE)
                && !(board.getPieceOn(BoardModel.getChessCoordinate(7, 0)) == WHITE_ROOK
                && whiteKingCoord.equals(BoardModel.getChessCoordinate(4, 0)))) {
            state.flip(WHITE_KING_SIDE_CASTLE_MASK);
        }
        if (canKingSideCastle(BLACK)
                && !(board.getPieceOn(BoardModel.getChessCoordinate(7, 7)) == BLACK_ROOK
                && blackKingCoord.equals(BoardModel.getChessCoordinate(4, 7)))) {
            state.flip(BLACK_KING_SIDE_CASTLE_MASK);
        }
        if (canQueenSideCastle(WHITE)
                && !(board.getPieceOn(BoardModel.getChessCoordinate(0, 0)) == WHITE_ROOK
                && whiteKingCoord.equals(BoardModel.getChessCoordinate(4, 0)))) {
            state.flip(WHITE_QUEEN_SIDE_CASTLE_MASK);
        }
        if (canQueenSideCastle(BLACK)
                && !(board.getPieceOn(BoardModel.getChessCoordinate(0, 7)) == BLACK_ROOK
                && blackKingCoord.equals(BoardModel.getChessCoordinate(4, 7)))) {
            state.flip(BLACK_QUEEN_SIDE_CASTLE_MASK);
        }
        stateHistory.add(state);
    }

    public void undoMove(Move move) {
        checkRep();
        if (moveHistory.size() > 0 && moveHistory.get(moveHistory.size() - 1).equals(move)) {
            int hash = hashCode();
            positionTracker.merge(hash, -1, Integer::sum);
            if (positionTracker.get(hash) == 0) {
                positionTracker.remove(hash);
            }

            board.undoMove(move);
            checkEnPassant(moveHistory.size() >= 2 ? moveHistory.get(moveHistory.size() - 2) : null);
            stateHistory.remove(stateHistory.size() - 1);
            moveHistory.remove(moveHistory.size() - 1);
            turn = (turn == WHITE) ? BLACK : WHITE;

            gameState = IN_PROGRESS;
        }
        checkRep();
    }

    public boolean canKingSideCastle(char color) {
        FastMap currentState = stateHistory.get(stateHistory.size() - 1);
        return color == WHITE ? currentState.isMarked(0) : currentState.isMarked(2);
    }

    public boolean canQueenSideCastle(char color) {
        FastMap currentState = stateHistory.get(stateHistory.size() - 1);
        return color == WHITE ? currentState.isMarked(1) : currentState.isMarked(2);
    }

    public ChessCoordinate getEnPassantTarget() {
        return enPassantTarget;
    }

    public List<Move> getLegalMoves() {
        legalMoves = moveGenerator.generateMoves();
        checkGameOver();
        return legalMoves;
    }

    public Move getLastMove() {
        return moveHistory.size() == 0 ? null : moveHistory.get(moveHistory.size() - 1);
    }

    public char getTurn() {
        return turn;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        FastMap currentCastlingState = stateHistory.get(stateHistory.size() - 1);
        return Objects.hash(board, turn, enPassantTarget, currentCastlingState);
    }

    private void checkRep() {
        if (DEBUG_MODE) {
            if (board == null || moveHistory == null) {
                throw new RuntimeException("Representation is incorrect.");
            }
        }
    }

    public String getFEN() {
        StringBuilder builder = new StringBuilder();

        // The Board Info
        int numEmpty = 0;
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                ChessCoordinate coordinate = BoardModel.getChessCoordinate(file, rank);
                Piece piece = board.getPieceOn(coordinate);

                if (piece == null) {
                    numEmpty++;
                } else {
                    builder.append(numEmpty == 0 ? "" : numEmpty);
                    numEmpty = 0;
                    builder.append(piece.getStringRep());
                }
            }
            builder.append(numEmpty == 0 ? "" : numEmpty);
            numEmpty = 0;
            builder.append(rank == 0 ? "" : "/");
        }

        // The turn info
        builder.append(" ");
        builder.append(turn);

        // The Castling Info
        builder.append(" ");
        if (canKingSideCastle(WHITE) || canKingSideCastle(BLACK)
                || canQueenSideCastle(WHITE) || canQueenSideCastle(WHITE)) {
            builder.append(canKingSideCastle(WHITE) ? "K" : "");
            builder.append(canKingSideCastle(BLACK) ? "k" : "");
            builder.append(canQueenSideCastle(WHITE) ? "Q" : "");
            builder.append(canQueenSideCastle(BLACK) ? "q" : "");
        } else {
            builder.append("-");
        }

        // The EnPassant Info
        builder.append(" ");
        if (enPassantTarget == null) {
            builder.append("-");
        } else {
            builder.append(enPassantTarget);
        }

        // TODO: Half-Move info
        builder.append(" ");
        builder.append(0);

        // Fullmove Number
        builder.append(" ");
        builder.append(moveHistory.size() / 2 + 1);

        return builder.toString();
    }
}
