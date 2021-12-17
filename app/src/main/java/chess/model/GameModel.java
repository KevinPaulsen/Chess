package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;
import chess.util.BigFastMap;
import chess.util.FastMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static chess.ChessCoordinate.*;
import static chess.model.pieces.Piece.*;

/**
 * This class represents the model for a standard chess game. This
 * class is responsible for game rules and managing all the parts of
 * the chess game.
 */
public class GameModel {

    /**
     * Char that shows that it is whites move.
     */
    public static final char WHITE = 'w';

    /**
     * Char that shows that it is blacks move.
     */
    public static final char BLACK = 'b';

    /**
     * State that we are still in the middle of a game.
     */
    public static final char IN_PROGRESS = 'p';

    /**
     * State that the game is over. And the player to move has lost.
     */
    public static final char LOSER = 'l';

    /**
     * State that the game is over, but ended in a draw.
     */
    public static final char DRAW = 'd';

    /**
     * The bit mask that represents that white can king-side castle.
     */
    private static final long WHITE_KING_SIDE_CASTLE_MASK = 1L;

    /**
     * The bit mask that represents that white can king-side castle.
     */
    private static final long WHITE_QUEEN_SIDE_CASTLE_MASK = 1L << 1;

    /**
     * The bit mask that represents that white can king-side castle.
     */
    private static final long BLACK_KING_SIDE_CASTLE_MASK = 1L << 2;

    /**
     * The bit mask that represents that white can king-side castle.
     */
    private static final long BLACK_QUEEN_SIDE_CASTLE_MASK = 1L << 3;

    /**
     * The mask that represents it is whites move
     */
    private static final long WHITE_TO_MOVE_MASK = 1L << 4;

    /**
     * The mask that represents the game is over, and there is a loser
     */
    private static final long LOSER_MASK = 1L << 5;

    /**
     * The mask that represents teh game is over, and there is a draw
     */
    private static final long DRAW_MASK = 1L << 6;

    /**
     * The mask that represents the bits that store the info about EnPassant
     */
    private static final long EN_PASSANT_MASK = 0b111111L << 7;

    /**
     * The model for the bard of the chess game.
     */
    private final BoardModel board;

    /**
     * The list of past moves that have occurred in this chess game.
     */
    private final LinkedList<Move> moveHistory;

    /**
     * The list of each state the board was in before each move. This List
     * should be 1 larger than moveHistory.
     * <p>
     * The first 4 bits are casting info, 1 means can castle, 0 means cannot.
     * The next bit is turn, 1 for white, 0 for black.
     * Bits 6-7 are game over bits. 00 is in progress, 01 loser, 10 draw.
     * bits 8-13 are the enPassant coordinate.
     */
    private final LinkedList<FastMap> stateHistory;

    /**
     * The map that tracks the number of times each position has occurred.
     * This maps from hashCode -> number of times this hashCode has appeared.
     */
    private final Map<Long, Integer> positionTracker;

    /**
     * The list of previous legal moves. This is here so the moves don't have
     * to be recalculated on move undo.
     */
    private final LinkedList<List<Move>> previousLegalMoves;

    /**
     * The move generator for this game.
     */
    private final MoveGenerator moveGenerator;

    /**
     * The zobrist hash object for this game. This controls our hashing function.
     */
    private final Zobrist zobrist;

    /**
     * The default constructor that creates a normal game.
     */
    public GameModel() {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    /**
     * Copy constructor for GameModel. All fields are copied,
     * including stateHistory, previousLegalMoves, MoveHistory,
     * and positionTracker.
     *
     * @param gameModel the GameModel to copy
     */
    public GameModel(GameModel gameModel) {
        this(gameModel.getFEN());

        stateHistory.clear();
        for (FastMap fastMap : gameModel.stateHistory) {
            stateHistory.add(new FastMap(fastMap.getMap()));
        }

        previousLegalMoves.clear();
        for (List<Move> moves : gameModel.previousLegalMoves) {
            previousLegalMoves.add(new ArrayList<>(moves));
        }

        this.moveHistory.addAll(gameModel.moveHistory);
        this.positionTracker.putAll(gameModel.positionTracker);
    }

    /**
     * Creates a game from the given FEN string. Behavior is undefined
     * when the FEN string is not properly formatted.
     *
     * @param FEN the FEN string for this game.
     */
    public GameModel(String FEN) {
        // Split the FEN into each of its 6 sections.
        String[] fenSections = FEN.split(" ");

        // Instantiate each of the fields of this GameModel.
        this.zobrist = new Zobrist();
        this.board = new BoardModel(fenSections[0], this.zobrist);
        this.moveHistory = new LinkedList<>();
        this.stateHistory = new LinkedList<>();
        this.positionTracker = new HashMap<>();
        this.moveGenerator = new MoveGenerator(this);
        this.previousLegalMoves = new LinkedList<>();

        // Check for EnPassant target
        ChessCoordinate enPassantTarget = !fenSections[3].equals("-") ? ChessCoordinate
                .getChessCoordinate(fenSections[3].charAt(0), Integer.parseInt(fenSections[3].substring(1)))
                : null;

        // Set the turn to the correct value
        char turn = fenSections[1].charAt(0);

        // Check for each of the castling rights
        boolean whiteKingCastle = fenSections[2].contains("K");
        boolean whiteQueenCastle = fenSections[2].contains("Q");
        boolean blackKingCastle = fenSections[2].contains("k");
        boolean blackQueenCastle = fenSections[2].contains("q");

        // Create and add the initial state
        addInitialState(whiteKingCastle, whiteQueenCastle, blackKingCastle, blackQueenCastle, turn, enPassantTarget);

        // Initialize the zobrist with the initial value
        zobrist.slowZobrist(this);

        // Generate the legal moves in this current position
        previousLegalMoves.add(this.moveGenerator.generateMoves());

        // Set the current position tracker to 1
        positionTracker.put(zobrist.getHashValue(), 1);
    }

    private void addInitialState(boolean whiteKingCastle, boolean whiteQueenCastle,
                                 boolean blackKingCastle, boolean blackQueenCastle, char turn,
                                 ChessCoordinate enPassantTarget) {
        FastMap stateMap = new FastMap();

        // Merge the respective values for the castling masks
        stateMap.mergeMask(whiteKingCastle ? WHITE_KING_SIDE_CASTLE_MASK : 0);
        stateMap.mergeMask(whiteQueenCastle ? WHITE_QUEEN_SIDE_CASTLE_MASK : 0);
        stateMap.mergeMask(blackKingCastle ? BLACK_KING_SIDE_CASTLE_MASK : 0);
        stateMap.mergeMask(blackQueenCastle ? BLACK_QUEEN_SIDE_CASTLE_MASK : 0);

        // Set the turn bit
        stateMap.mergeMask(turn == WHITE ? WHITE_TO_MOVE_MASK : 0);

        // Set the enPassant target bits
        if (enPassantTarget != null) {
            stateMap.mergeMask(((long) enPassantTarget.getOndDimIndex()) << 7);
        }

        // Add the state to the current state
        stateHistory.add(stateMap);
    }

    /**
     * Attempts to make a move given two Coordinates. If the coordinates correspond
     * to a legal move, the move will be made. True is returned if the move was
     * successful, false otherwise.
     *
     * @param startCoordinate the starting coordinate of the moving Piece.
     * @param endCoordinate   the ending coordinate of the moving Piece.
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
            if (currentMove != null) {
                didMove = move(currentMove);
            }
        }
        return didMove;
    }

    /**
     * Attempts to make the given move.
     *
     * @param move a non-null move.
     * @return true if the move is successful, false otherwise.
     */
    public boolean move(Move move) {
        boolean didMove = false;

        if (getGameOverStatus() == IN_PROGRESS) {
            board.move(move);
            moveHistory.add(move);
            stateHistory.add(makeState());
            positionTracker.merge(getZobristHash(), 1, Integer::sum);
            previousLegalMoves.add(moveGenerator.generateMoves());
            checkGameOver();

            didMove = true;
        }

        return didMove;
    }

    /**
     * Undoes the given move. The state will be exactly the same
     * as if this move never happened. If no move has happened yet,
     * nothing happens.
     */
    public void undoMove() {
        Move move = getLastMove();
        if (moveHistory.size() > 0) {
            long hash = getZobristHash();

            // Decrement the position tracker of the current position by 1
            positionTracker.merge(hash, -1, Integer::sum);
            if (positionTracker.get(hash) == 0) {
                positionTracker.remove(hash);
            }

            // Undo the move on the board
            board.undoMove(move);

            // remove the current move from move history.
            moveHistory.pollLast();

            // Remove the current state from stateHistory.
            stateHistory.pollLast();

            // Remove the current legal moves
            previousLegalMoves.pollLast();

            // Update zobrist
            zobrist.updateGameData(getGameState());
        }
    }

    /**
     * Create and return the current state. This method also
     * updates zobrist.
     */
    private FastMap makeState() {
        FastMap currentState = new FastMap();
        currentState.merge(getGameState());

        checkCastling(currentState, board.getWhiteKingCoord(), board.getBlackKingCoord());
        checkEnPassant(currentState, getLastMove());
        currentState.flip(WHITE_TO_MOVE_MASK);

        zobrist.updateGameData(currentState);
        return currentState;
    }

    /**
     * Check if the game has ended, and update the bits in current
     * game state.
     */
    private void checkGameOver() {
        long hash = getZobristHash();
        List<Move> legalMoves = getLegalMoves();
        FastMap currentState = getGameState();

        if (positionTracker.containsKey(hash) && positionTracker.get(hash) >= 3) {
            // If this position has been reached 3 times, the game is a draw
            currentState.mergeMask(DRAW_MASK);
        } else if (legalMoves.size() == 0) {
            // If this position has no legal moves, then the game is over
            ChessCoordinate kingToMove = getTurn() == WHITE ? board.getWhiteKingCoord() : board.getBlackKingCoord();
            if (moveGenerator.getOpponentAttackMap().isMarked(kingToMove.getOndDimIndex())) {
                currentState.mergeMask(LOSER_MASK);
            } else {
                currentState.mergeMask(DRAW_MASK);
            }
        }
    }

    /**
     * Check if an enPassant square needs to be set.
     *
     * @param state the state to update
     * @param lastMove the last move made
     */
    private static void checkEnPassant(FastMap state, Move lastMove) {
        ChessCoordinate enPassantTarget = null;
        if (lastMove.getMovingPiece().isPawn()
                && Math.abs(lastMove.getStartingCoordinate().getRank() - lastMove.getEndingCoordinate().getRank()) == 2) {
            int rank = lastMove.getStartingCoordinate().getRank() == 1 ? 2 : 5;
            enPassantTarget = ChessCoordinate.getChessCoordinate(lastMove.getEndingCoordinate().getFile(), rank);
        }

        state.clearMask(EN_PASSANT_MASK);
        if (enPassantTarget != null) {
            state.mergeMask(((long) enPassantTarget.getOndDimIndex()) << 7);
        }
    }

    /**
     * Check if castling data needs to be updated
     * @param state the state to update
     * @param whiteKingCoord the coordinate of the white king
     * @param blackKingCoord the coordinate of the black king
     */
    private void checkCastling(FastMap state, ChessCoordinate whiteKingCoord, ChessCoordinate blackKingCoord) {
        if (canKingSideCastle(WHITE)
                && !(board.getPieceOn(H1) == WHITE_ROOK && whiteKingCoord.equals(E1))) {
            state.flip(WHITE_KING_SIDE_CASTLE_MASK);
        }
        if (canQueenSideCastle(WHITE)
                && !(board.getPieceOn(A1) == WHITE_ROOK && whiteKingCoord.equals(E1))) {
            state.flip(WHITE_QUEEN_SIDE_CASTLE_MASK);
        }
        if (canKingSideCastle(BLACK)
                && !(board.getPieceOn(H8) == BLACK_ROOK && blackKingCoord.equals(E8))) {
            state.flip(BLACK_KING_SIDE_CASTLE_MASK);
        }
        if (canQueenSideCastle(BLACK)
                && !(board.getPieceOn(A8) == BLACK_ROOK && blackKingCoord.equals(E8))) {
            state.flip(BLACK_QUEEN_SIDE_CASTLE_MASK);
        }
    }

    /**
     * @return the board model of this game.
     */
    public BoardModel getBoard() {
        return board;
    }

    /**
     * Return if the given color can castle kingside
     *
     * @param color the color to check
     * @return weather the given color can castle kingside
     */
    public boolean canKingSideCastle(char color) {
        FastMap currentState = getGameState();
        return color == WHITE ? currentState.isMarked(0) : currentState.isMarked(2);
    }

    /**
     * Return if the given color can castle queenside
     *
     * @param color the color to check
     * @return weather the given color can castle queenside
     */
    public boolean canQueenSideCastle(char color) {
        FastMap currentState = getGameState();
        return color == WHITE ? currentState.isMarked(1) : currentState.isMarked(3);
    }

    /**
     * Return
     *
     * @return the enPassantTarget in the current position
     */
    public ChessCoordinate getEnPassantTarget() {
        long stateRep = getGameState().getMap();
        stateRep = stateRep >> 7;

        return stateRep == 0 ? null : ChessCoordinate.getChessCoordinate((int) stateRep);
    }

    /**
     * @return the legal moves in the current position.
     */
    public List<Move> getLegalMoves() {
        return previousLegalMoves.get(previousLegalMoves.size() - 1);
    }

    /**
     * @return the last move made.
     */
    public Move getLastMove() {
        return moveHistory.peekLast();
    }

    /**
     * @return the turn to move.
     */
    public char getTurn() {
        return getGameState().isMarked(4) ? WHITE : BLACK;
    }

    /**
     * @return the gameState.
     */
    public FastMap getGameState() {
        return stateHistory.peekLast();
    }

    /**
     * @return the game over status of the game.
     */
    public char getGameOverStatus() {
        FastMap currentState = getGameState();
        if (currentState.isMarked(5)) {
            return LOSER;
        } else if (currentState.isMarked(6)) {
            return DRAW;
        }
        return IN_PROGRESS;
    }

    /**
     * @return the zobrist hash of the current position
     */
    public long getZobristHash() {
        return zobrist.getHashValue();
    }

    /**
     * @return the zobristHash with times reached of the current position
     */
    public long getZobristWithTimesMoved() {
        return zobrist.getHashValueWithTimesMoved(getNumTimesReached());
    }

    /**
     * @return the number of times this position has been reached.
     */
    private int getNumTimesReached() {
        return positionTracker.get(getZobristHash());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameModel)) return false;

        GameModel gameModel = (GameModel) o;

        if (!getBoard().equals(gameModel.getBoard())) return false;
        if (!moveHistory.equals(gameModel.moveHistory)) return false;
        if (!stateHistory.equals(gameModel.stateHistory)) return false;
        if (!positionTracker.equals(gameModel.positionTracker)) return false;
        if (!previousLegalMoves.equals(gameModel.previousLegalMoves)) return false;
        if (!moveGenerator.equals(gameModel.moveGenerator)) return false;
        return zobrist.equals(gameModel.zobrist);
    }

    @Override
    public int hashCode() {
        return (int) getZobristWithTimesMoved();
    }

    /**
     * @return the FEN string of the current position
     */
    public String getFEN() {
        StringBuilder builder = new StringBuilder();

        // The Board Info
        int numEmpty = 0;
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                ChessCoordinate coordinate = ChessCoordinate.getChessCoordinate(file, rank);
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
        builder.append(getTurn());

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
        if (getEnPassantTarget() == null) {
            builder.append("-");
        } else {
            builder.append(getEnPassantTarget());
        }

        // TODO: Half-Move info
        builder.append(" ");
        builder.append(0);

        // Fullmove Number
        builder.append(" ");
        builder.append(moveHistory.size() / 2 + 1);

        return builder.toString();
    }

    public BigFastMap getRep() {
        BigFastMap result = new BigFastMap(0, 840);

        int bitIdx = 0;
        for (int pieceIdx = 0; pieceIdx < 64; pieceIdx++) {
            Piece piece = board.getPieceOn(ChessCoordinate.getChessCoordinate(pieceIdx));
            int uid = piece == null ? EMPTY.getUniqueIdx() : piece.getUniqueIdx();
            result.flipBit(bitIdx + uid);
            bitIdx += 13;
        }
        if (canKingSideCastle(WHITE)) result.flipBit(bitIdx);
        bitIdx++;
        if (canQueenSideCastle(WHITE)) result.flipBit(bitIdx);
        bitIdx++;
        if (canKingSideCastle(BLACK)) result.flipBit(bitIdx);
        bitIdx++;
        if (canQueenSideCastle(BLACK)) result.flipBit(bitIdx);
        bitIdx++;

        if (getEnPassantTarget() != null) result.flipBit(bitIdx + getEnPassantTarget().getOndDimIndex());
        bitIdx += 64;
        if (getTurn() == WHITE) result.flipBit(bitIdx);

        return result;
    }
}
