package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;
import chess.util.BigFastMap;
import chess.util.FastMap;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private static final long WHITE_QUEEN_SIDE_CASTLE_MASK = 2L;

    /**
     * The bit mask that represents that white can king-side castle.
     */
    private static final long BLACK_KING_SIDE_CASTLE_MASK = 4L;

    /**
     * The bit mask that represents that white can king-side castle.
     */
    private static final long BLACK_QUEEN_SIDE_CASTLE_MASK = 8L;

    private static final long WHITE_TO_MOVE_MASK = 16L;

    private static final long LOSER_MASK = 32L;

    private static final long DRAW_MASK = 64L;

    private static final long EN_PASSANT_MASK = 0b111111L << 7;

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
     *
     * The first 4 bits are casting info, 1 means can castle, 0 means cannot.
     * The next bit is turn, 1 for white, 0 for black.
     * Bits 6-7 are game over bits. 00 is in progress, 01 loser, 10 draw.
     * bits 8-13 are the enPassant coordinate.
     */
    private final List<FastMap> stateHistory;

    private FastMap currentState;

    /**
     * The map that tracks the number of times each position has occurred.
     * This maps from hashCode -> number of times this hashCode has appeared.
     */
    private final Map<Long, Integer> positionTracker;

    private final List<List<Move>> previousLegalMoves;

    /**
     * The move generator for this game.
     */
    private final MoveGenerator moveGenerator;

    private final Zobrist zobrist;

    /**
     * The default constructor that creates a normal game.
     */
    public GameModel() {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public GameModel(GameModel gameModel) {
        this.zobrist = new Zobrist(gameModel.zobrist);
        this.board = new BoardModel(gameModel.board, zobrist);
        this.moveHistory = new ArrayList<>(gameModel.moveHistory);
        this.stateHistory = new ArrayList<>();
        for(FastMap fastMap : gameModel.stateHistory)
            stateHistory.add(new FastMap(fastMap.getMap()));
        this.currentState = stateHistory.get(stateHistory.size() - 1);
        this.positionTracker = new HashMap<>(gameModel.positionTracker);
        this.moveGenerator = new MoveGenerator(this);
        this.previousLegalMoves = new ArrayList<>();
        for (List<Move> moves : gameModel.previousLegalMoves) {
            previousLegalMoves.add(new ArrayList<>(moves));
        }
    }

    public GameModel(String FEN) {
        String[] fenSections = FEN.split(" ");

        this.zobrist = new Zobrist();
        this.board = new BoardModel(fenSections[0], this.zobrist);
        this.moveHistory = new ArrayList<>(200);
        this.stateHistory = new ArrayList<>(200);
        this.positionTracker = new HashMap<>();
        this.moveGenerator = new MoveGenerator(this);
        this.previousLegalMoves = new ArrayList<>();

        ChessCoordinate enPassantTarget = !fenSections[3].equals("-") ? ChessCoordinate
                .getChessCoordinate(fenSections[3].charAt(0), Integer.parseInt(fenSections[3].substring(1)))
                : null;
        char turn = fenSections[1].charAt(0);
        boolean whiteKingCastle = fenSections[2].contains("K");
        boolean whiteQueenCastle = fenSections[2].contains("Q");
        boolean blackKingCastle = fenSections[2].contains("k");
        boolean blackQueenCastle = fenSections[2].contains("q");

        addInitialState(whiteKingCastle, whiteQueenCastle, blackKingCastle, blackQueenCastle, turn, enPassantTarget);
        this.currentState = stateHistory.get(stateHistory.size() - 1);
        zobrist.slowZobrist(this);

        previousLegalMoves.add(this.moveGenerator.generateMoves());

        positionTracker.put(zobrist.getHashValue(), 1);
    }

    private void addInitialState(boolean whiteKingCastle, boolean whiteQueenCastle,
                                 boolean blackKingCastle, boolean blackQueenCastle, char turn,
                                 ChessCoordinate enPassantTarget) {
        FastMap stateMap = new FastMap();
        stateMap.mergeMask(whiteKingCastle ? WHITE_KING_SIDE_CASTLE_MASK : 0);
        stateMap.mergeMask(whiteQueenCastle ? WHITE_QUEEN_SIDE_CASTLE_MASK : 0);
        stateMap.mergeMask(blackKingCastle ? BLACK_KING_SIDE_CASTLE_MASK : 0);
        stateMap.mergeMask(blackQueenCastle ? BLACK_QUEEN_SIDE_CASTLE_MASK : 0);
        stateMap.mergeMask(turn == WHITE ? WHITE_TO_MOVE_MASK : 0);
        if (enPassantTarget != null) {
            stateMap.mergeMask(((long) enPassantTarget.getOndDimIndex()) << 7);
        }

        this.currentState = stateMap;
        stateHistory.add(currentState);
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
        boolean didMove = false;

        if (getGameOverStatus() == IN_PROGRESS && move != null && move.getMovingPiece().getColor() == getTurn()) {
            board.move(move);
            moveHistory.add(move);
            makeState(board.getWhiteKingCoord(), board.getBlackKingCoord(), move);
            positionTracker.merge(zobrist.getHashValue(), 1, Integer::sum);

            didMove = true;
        }

        return didMove;
    }

    private void makeState(ChessCoordinate whiteKingCoord, ChessCoordinate blackKingCoord, Move lastMove) {
        FastMap currentState = new FastMap();
        currentState.merge(getGameState());

        checkCastling(currentState, whiteKingCoord, blackKingCoord);
        checkEnPassant(currentState, lastMove);
        currentState.flip(WHITE_TO_MOVE_MASK);

        zobrist.updateGameData(currentState);

        this.currentState = currentState;
        stateHistory.add(currentState);
        checkGameOver();
    }

    private void checkGameOver() {
        long hash = zobrist.getHashValue();
        List<Move> legalMoves = moveGenerator.generateMoves();
        FastMap gameState = getGameState();
        if (positionTracker.containsKey(hash) && positionTracker.get(hash) >= 3) {
            gameState.mergeMask(DRAW_MASK);
        } else {
            if (legalMoves.size() == 0) {
                ChessCoordinate kingToMove = getTurn() == WHITE ? board.getWhiteKingCoord() : board.getBlackKingCoord();
                if (moveGenerator.getOpponentAttackMap().isMarked(kingToMove.getOndDimIndex())) {
                    gameState.mergeMask(LOSER_MASK);
                } else {
                    gameState.mergeMask(DRAW_MASK);
                }
            }
        }
        previousLegalMoves.add(legalMoves);
    }

    private void checkEnPassant(FastMap state, Move lastMove) {
        ChessCoordinate enPassantTarget = null;
        if (lastMove != null
                && (lastMove.getMovingPiece() == Piece.WHITE_PAWN
                || lastMove.getMovingPiece() == Piece.BLACK_PAWN)
                && Math.abs(lastMove.getStartingCoordinate().getRank()
                - lastMove.getEndingCoordinate().getRank()) == 2) {

            int rank = (int) (0.6 * (lastMove.getStartingCoordinate().getRank() - 3.5) + 3.5);
            enPassantTarget = ChessCoordinate.getChessCoordinate(lastMove.getEndingCoordinate().getFile(), rank);
        }

        state.clearMask(EN_PASSANT_MASK);
        if (enPassantTarget != null) {
            state.mergeMask(((long) enPassantTarget.getOndDimIndex()) << 7);
        }
    }

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

    public void undoMove(Move move) {
        if (moveHistory.size() > 0 && moveHistory.get(moveHistory.size() - 1).equals(move)) {
            long hash = zobrist.getHashValue();
            positionTracker.merge(hash, -1, Integer::sum);
            if (positionTracker.get(hash) == 0) {
                positionTracker.remove(hash);
            }

            board.undoMove(move);
            moveHistory.remove(moveHistory.size() - 1);

            stateHistory.remove(stateHistory.size() - 1);
            currentState = stateHistory.get(stateHistory.size() - 1);
            zobrist.updateGameData(currentState);
            previousLegalMoves.remove(previousLegalMoves.size() - 1);
        }
    }

    public boolean canKingSideCastle(char color) {
        return color == WHITE ? currentState.isMarked(0) : currentState.isMarked(2);
    }

    public boolean canQueenSideCastle(char color) {
        return color == WHITE ? currentState.isMarked(1) : currentState.isMarked(3);
    }

    public ChessCoordinate getEnPassantTarget() {
        long stateRep = getGameState().getMap();
        stateRep = stateRep >> 7;

        return stateRep == 0 ? null : ChessCoordinate.getChessCoordinate((int) stateRep);
    }

    public List<Move> getLegalMoves() {
        return previousLegalMoves.get(previousLegalMoves.size() - 1);
    }

    public Move getLastMove() {
        return moveHistory.size() == 0 ? null : moveHistory.get(moveHistory.size() - 1);
    }

    public char getTurn() {
        FastMap currentState = getGameState();
        return currentState.isMarked(4) ? WHITE : BLACK;
    }

    public FastMap getGameState() {
        return currentState;
    }

    public char getGameOverStatus() {
        FastMap currentState = getGameState();
        if (currentState.isMarked(5)) {
            return LOSER;
        } else if (currentState.isMarked(6)) {
            return DRAW;
        }
        return IN_PROGRESS;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        FastMap currentState = getGameState();
        return Objects.hash(board, currentState);
    }

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

    public long getZobristHash() {
        return zobrist.getHashValue();
    }

    public long getZobristWithTimesMoved() {
        return zobrist.getHashValueWithTimesMoved(getNumTimesReached());
    }

    private int getNumTimesReached() {
        return positionTracker.get(getZobristHash());
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
