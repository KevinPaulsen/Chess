package chess.model;

import chess.ChessCoordinate;
import chess.model.moves.Movable;
import chess.model.moves.PromotionMove;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final List<Movable> moveHistory;

    /**
     * The list of each state the board was in before each move. This List
     * should be 1 larger than moveHistory.
     * <p>
     * The first 4 bits are casting info, 1 means can castle, 0 means cannot.
     * The next bit is turn, 1 for white, 0 for black.
     * Bits 6-7 are game over bits. 00 is in progress, 01 loser, 10 draw.
     * bits 8-13 are the enPassant coordinate.
     */
    private final List<FastMap> stateHistory;

    /**
     * The map that tracks the number of times each position has occurred.
     * This maps from hashCode -> number of times this hashCode has appeared.
     */
    private final Map<Long, Integer> positionTracker;

    /**
     * The list of previous legal moves. This is here so the moves don't have
     * to be recalculated on move undo.
     */
    private final List<MoveList> previousLegalMoves;

    /**
     * The move generator for this game.
     */
    private final MoveGenerator moveGenerator;
    private final boolean threeFold;
    long ifTime = 0;
    long countIf = 0;
    long boardMoveTime = 0;
    long countBoardMove = 0;
    long moveHistoryAddTime = 0;
    long countMoveHistoryAdd = 0;
    long stateHistoryAddTime = 0;
    long countStateHistoryAdd = 0;
    long positionTrackerAddTime = 0;
    long countPositionTrackerAdd = 0;
    long generateMovesTime = 0;
    long countGenerateMove = 0;
    long previousMovesAddTime = 0;
    long countPreviousMoveAdd = 0;
    long checkGameOverTime = 0;
    long countCheckGameOver = 0;
    private long hashValue;

    /**
     * The default constructor that creates a normal game.
     */
    public GameModel() {
        this(true);
    }

    public GameModel(boolean threeFold) {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", threeFold);
    }

    /**
     * Creates a game from the given FEN string. Behavior is undefined
     * when the FEN string is not properly formatted.
     *
     * @param fen the FEN string for this game.
     */
    public GameModel(String fen, boolean threeFold) {
        // Instantiate each of the fields of this GameModel.
        this.board = new BoardModel();
        this.moveHistory = new ArrayList<>();
        this.stateHistory = new ArrayList<>();
        this.positionTracker = new HashMap<>();
        this.moveGenerator = new MoveGenerator(this);
        this.previousLegalMoves = new ArrayList<>();
        this.threeFold = threeFold;

        setPosition(fen);
    }

    public void setPosition(String fen) {
        // Split the FEN into each of its 6 sections.
        String[] fenSections = fen.split(" ");

        // Instantiate each of the fields of this GameModel.
        this.board.setPosition(fenSections[0]);
        this.moveHistory.clear();
        this.stateHistory.clear();
        this.positionTracker.clear();
        this.previousLegalMoves.clear();

        // Check for EnPassant target
        ChessCoordinate enPassantTarget = !fenSections[3].equals("-") ?
                ChessCoordinate.getChessCoordinate(fenSections[3].charAt(0),
                                                   Integer.parseInt(fenSections[3].substring(1))) :
                null;

        // Set the turn to the correct value
        char turn = fenSections[1].charAt(0);

        // Check for each of the castling rights
        boolean whiteKingCastle = fenSections[2].contains("K");
        boolean whiteQueenCastle = fenSections[2].contains("Q");
        boolean blackKingCastle = fenSections[2].contains("k");
        boolean blackQueenCastle = fenSections[2].contains("q");

        // Create and add the initial state
        addInitialState(whiteKingCastle, whiteQueenCastle, blackKingCastle, blackQueenCastle, turn,
                        enPassantTarget);

        // Initialize the deltaHash with the initial value
        this.hashValue = Zobrist.slowZobrist(this);

        // Generate the legal moves in this current position
        previousLegalMoves.add(this.moveGenerator.generateMoves());

        // Set the current position tracker to 1
        positionTracker.put(hashValue, 1);
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
     * Copy constructor for GameModel. All fields are copied,
     * including stateHistory, previousLegalMoves, MoveHistory,
     * and positionTracker.
     *
     * @param gameModel the GameModel to copy
     */
    public GameModel(GameModel gameModel) {
        this(gameModel.getFEN(), gameModel.threeFold);

        stateHistory.clear();
        for (FastMap fastMap : gameModel.stateHistory) {
            stateHistory.add(new FastMap(fastMap.getMap()));
        }

        previousLegalMoves.clear();
        for (MoveList moves : gameModel.previousLegalMoves) {
            previousLegalMoves.add(new MoveList(moves));
        }

        this.moveHistory.addAll(gameModel.moveHistory);
        this.positionTracker.putAll(gameModel.positionTracker);
    }

    public String getFEN() {
        return getFEN(board.getPieceArray(), getTurn(), canKingSideCastle(WHITE),
                      canQueenSideCastle(WHITE), canKingSideCastle(BLACK),
                      canQueenSideCastle(BLACK), getEnPassantTarget(), moveHistory.size());
    }

    /**
     * @return the FEN string of the current position
     */
    private static String getFEN(Piece[] board, char turn, boolean whiteKingCastle,
                                 boolean whiteQueenCastle, boolean blackKingCastle,
                                 boolean blackQueenCastle, ChessCoordinate enPassantTarget,
                                 int numMoves) {
        StringBuilder builder = new StringBuilder();

        // The Board Info
        int numEmpty = 0;
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                ChessCoordinate coordinate = ChessCoordinate.getChessCoordinate(file, rank);
                Piece piece = board[coordinate.getOndDimIndex()];

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
        if (whiteKingCastle || blackKingCastle || whiteQueenCastle || blackQueenCastle) {
            builder.append(whiteKingCastle ? "K" : "");
            builder.append(blackKingCastle ? "k" : "");
            builder.append(whiteQueenCastle ? "Q" : "");
            builder.append(blackQueenCastle ? "q" : "");
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

        // TODO: Half-NormalMove info
        builder.append(" ");
        builder.append(0);

        // Fullmove Number
        builder.append(" ");
        builder.append(numMoves / 2 + 1);

        return builder.toString();
    }

    /**
     * @return the turn to move.
     */
    public char getTurn() {
        return getGameState().isMarked(4) ? WHITE : BLACK;
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
     * @return the gameState.
     */
    public FastMap getGameState() {
        return !stateHistory.isEmpty() ? stateHistory.get(stateHistory.size() - 1) : null;
    }

    public GameModel(byte[] representation) {
        this(getFEN(representation));
    }

    public GameModel(String FEN) {
        this(FEN, true);
    }

    public static String getFEN(byte[] representation) {
        BigInteger rep = new BigInteger(representation);

        final int maxCoordSize = 6;
        final int maxTagSize = 4;
        final BigInteger coordinateMask = BigInteger.valueOf(0b111111);
        final BigInteger tagMask = BigInteger.valueOf(0b1111);

        char turn = rep.testBit(0) ? WHITE : BLACK;
        rep = rep.shiftRight(1);

        boolean blackQueenCastle = rep.testBit(0);
        rep = rep.shiftRight(1);
        boolean blackKingCastle = rep.testBit(0);
        rep = rep.shiftRight(1);
        boolean whiteQueenCastle = rep.testBit(0);
        rep = rep.shiftRight(1);
        boolean whiteKingCastle = rep.testBit(0);
        rep = rep.shiftRight(1);

        ChessCoordinate enPassantTarget = null;
        if (rep.testBit(0)) {
            rep = rep.shiftRight(1);
            enPassantTarget = ChessCoordinate.getChessCoordinate(
                    rep.and(coordinateMask).intValue());
            rep = rep.shiftRight(maxCoordSize);
        } else {
            rep = rep.shiftRight(1);
        }

        Piece[] board = new Piece[64];
        Piece[] pieces = Piece.values();
        for (int pieceIdx = pieces.length - 1; pieceIdx > 0; pieceIdx--) {
            Piece piece = pieces[pieceIdx];

            int numPieces = rep.and(tagMask).intValue();
            rep = rep.shiftRight(maxTagSize);

            for (int pieceNum = 0; pieceNum < numPieces; pieceNum++) {
                int coordinate = rep.and(coordinateMask).intValue();
                rep = rep.shiftRight(maxCoordSize);
                board[coordinate] = piece;
            }
        }

        return getFEN(board, turn, whiteKingCastle, whiteQueenCastle, blackKingCastle,
                      blackQueenCastle, enPassantTarget, 0);
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
    public boolean move(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate,
                        Piece promoPiece) {
        boolean didMove = false;

        // Check that moves are both on screen.
        if (startCoordinate != null && endCoordinate != null) {
            Movable currentMove = null;
            Piece movingPiece = board.getPieceOn(startCoordinate);
            if (movingPiece != null) {
                for (Movable move : new MoveGenerator(this).generateMoves()) {
                    if (startCoordinate.equals(move.getStartCoordinate()) && endCoordinate.equals(
                            move.getEndCoordinate())) {
                        if (!(move instanceof PromotionMove) ||
                                ((PromotionMove) move).getPromotedPiece() == promoPiece) {
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
    public boolean move(Movable move) {
        boolean didMove = false;

        /*
        long start;
        long end;

        start = System.nanoTime();
        hashValue = board.move(move);
        end = System.nanoTime();
        boardMoveTime += end - start;
        countBoardMove++;

        start = System.nanoTime();
        moveHistory.add(move);
        end = System.nanoTime();
        moveHistoryAddTime += end - start;
        countMoveHistoryAdd++;

        start = System.nanoTime();
        stateHistory.add(makeState());
        end = System.nanoTime();
        stateHistoryAddTime += end - start;
        countStateHistoryAdd++;

        start = System.nanoTime();
        positionTracker.merge(getZobristHash(), 1, Integer::sum);
        end = System.nanoTime();
        positionTrackerAddTime += end - start;
        countPositionTrackerAdd++;

        start = System.nanoTime();
        MoveList list = moveGenerator.generateMoves();
        end = System.nanoTime();
        generateMovesTime += end - start;
        countGenerateMove++;

        start = System.nanoTime();
        previousLegalMoves.add(list);
        end = System.nanoTime();
        previousMovesAddTime += end - start;
        countPreviousMoveAdd++;

        start = System.nanoTime();
        checkGameOver();
        end = System.nanoTime();
        checkGameOverTime += end - start;
        countCheckGameOver++;
         */

        hashValue = board.move(move);
        moveHistory.add(move);
        stateHistory.add(makeState());
        positionTracker.merge(getZobristHash(), 1, Integer::sum);
        previousLegalMoves.add(moveGenerator.generateMoves());
        checkGameOver();

        didMove = true;

        return didMove;
    }

    /**
     * Create and return the current state. This method also
     * updates deltaHash.
     */
    private FastMap makeState() {
        FastMap newState = new FastMap();
        newState.merge(getGameState());

        checkCastling(newState, board.getWhiteKingCoord(), board.getBlackKingCoord());
        Movable lastMove = getLastMove();
        checkEnPassant(newState, lastMove, board.isPawn(lastMove.getEndCoordinate()));
        newState.flip(WHITE_TO_MOVE_MASK);

        hashValue ^= Zobrist.getGameStateHash(newState);
        return newState;
    }

    /**
     * @return the deltaHash hash of the current position
     */
    public long getZobristHash() {
        return hashValue;
    }

    /**
     * Check if the game has ended, and update the bits in current
     * game state.
     */
    private void checkGameOver() {
        long hash = hashValue;
        MoveList legalMoves = getLegalMoves();
        FastMap currentState = getGameState();

        if (threeFold && positionTracker.containsKey(hash) && positionTracker.get(hash) >= 3) {
            // If this position has been reached 3 times, the game is a draw
            currentState.mergeMask(DRAW_MASK);
        } else if (legalMoves.isEmpty()) {
            // If this position has no legal moves, then the game is over
            ChessCoordinate kingToMove =
                    getTurn() == WHITE ? board.getWhiteKingCoord() : board.getBlackKingCoord();
            if ((moveGenerator.getOpponentAttackMap() & kingToMove.getBitMask()) != 0) {
                currentState.mergeMask(LOSER_MASK);
            } else {
                currentState.mergeMask(DRAW_MASK);
            }
        }
    }

    /**
     * Check if castling data needs to be updated
     *
     * @param state          the state to update
     * @param whiteKingCoord the coordinate of the white king
     * @param blackKingCoord the coordinate of the black king
     */
    private void checkCastling(FastMap state, ChessCoordinate whiteKingCoord,
                               ChessCoordinate blackKingCoord) {
        if (canKingSideCastle(WHITE) && !(board.getPieceOn(H1) == WHITE_ROOK &&
                whiteKingCoord.equals(E1))) {
            state.flip(WHITE_KING_SIDE_CASTLE_MASK);
        }
        if (canQueenSideCastle(WHITE) && !(board.getPieceOn(A1) == WHITE_ROOK &&
                whiteKingCoord.equals(E1))) {
            state.flip(WHITE_QUEEN_SIDE_CASTLE_MASK);
        }
        if (canKingSideCastle(BLACK) && !(board.getPieceOn(H8) == BLACK_ROOK &&
                blackKingCoord.equals(E8))) {
            state.flip(BLACK_KING_SIDE_CASTLE_MASK);
        }
        if (canQueenSideCastle(BLACK) && !(board.getPieceOn(A8) == BLACK_ROOK &&
                blackKingCoord.equals(E8))) {
            state.flip(BLACK_QUEEN_SIDE_CASTLE_MASK);
        }
    }

    /**
     * @return the last move made.
     */
    public Movable getLastMove() {
        return !moveHistory.isEmpty() ? moveHistory.get(moveHistory.size() - 1) : null;
    }

    /**
     * Check if an enPassant square needs to be set.
     *
     * @param state    the state to update
     * @param lastMove the last move made
     */
    private static void checkEnPassant(FastMap state, Movable lastMove, boolean isPawn) {
        if (lastMove == null) {
            throw new IllegalArgumentException("lastMove cannot be null");
        }
        ChessCoordinate enPassantTarget = null;
        if (isPawn && Math.abs(
                lastMove.getStartCoordinate().getRank() - lastMove.getEndCoordinate().getRank()) ==
                2) {
            int rank = lastMove.getStartCoordinate().getRank() == 1 ? 2 : 5;
            enPassantTarget = ChessCoordinate.getChessCoordinate(
                    lastMove.getEndCoordinate().getFile(), rank);
        }

        state.clearMask(EN_PASSANT_MASK);
        if (enPassantTarget != null) {
            state.mergeMask(((long) enPassantTarget.getOndDimIndex()) << 7);
        }
    }

    /**
     * @return the legal moves in the current position.
     */
    public MoveList getLegalMoves() {
        return previousLegalMoves.get(previousLegalMoves.size() - 1);
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

    /*public void printTimes() {
        System.out.printf("Average if Time:                      %dns\n", ifTime / countIf);
        System.out.printf("Average Board Move Time:              %dns\n",
                          boardMoveTime / countBoardMove);
        System.out.printf("Average Move History Add Time:        %dns\n",
                          moveHistoryAddTime / countMoveHistoryAdd);
        System.out.printf("Average State History Add Time:       %dns\n",
                          stateHistoryAddTime / countStateHistoryAdd);
        System.out.printf("Average Position Tracker Add if Time: %dns\n",
                          positionTrackerAddTime / countPositionTrackerAdd);
        System.out.printf("Average Generate Move Time:           %dns\n",
                          generateMovesTime / countGenerateMove);
        System.out.printf("Average Previous Move Add Time:       %dns\n",
                          previousMovesAddTime / countPreviousMoveAdd);
        System.out.printf("Average Check Game Over Time:         %dns\n",
                          checkGameOverTime / countCheckGameOver);
    }//*/

    /**
     * Undoes the given move. The state will be exactly the same
     * as if this move never happened. If no move has happened yet,
     * nothing happens.
     */
    public void undoLastMove() {
        if (!moveHistory.isEmpty()) {
            long hash = getZobristHash();

            // Decrement the position tracker of the current position by 1
            if (!positionTracker.containsKey(hash)) {
                System.out.println("oof");
            }
            positionTracker.merge(hash, -1, Integer::sum);
            if (positionTracker.get(hash) == 0) {
                positionTracker.remove(hash);
            }

            // remove the current move from move history.
            moveHistory.remove(moveHistory.size() - 1);

            // Remove the current state from stateHistory.
            stateHistory.remove(stateHistory.size() - 1);

            // Remove the current legal moves
            previousLegalMoves.remove(previousLegalMoves.size() - 1);

            // Update deltaHash
            hashValue = Zobrist.getGameStateHash(getGameState());

            // Undo the move on the board
            hashValue ^= board.undoMove();
        }
    }

    @Override
    public int hashCode() {
        return (int) getZobristWithTimesMoved();
    }

    /**
     * @return the zobristHash with times reached of the current position
     */
    public long getZobristWithTimesMoved() {
        return Zobrist.getHashValueWithTimesMoved(hashValue, getNumTimesReached());
    }

    /**
     * @return the number of times this position has been reached.
     */
    private int getNumTimesReached() {
        return positionTracker.get(getZobristHash());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GameModel gameModel))
            return false;

        if (!getBoard().equals(gameModel.getBoard()))
            return false;
        if (!moveHistory.equals(gameModel.moveHistory))
            return false;
        if (!stateHistory.equals(gameModel.stateHistory))
            return false;
        if (!positionTracker.equals(gameModel.positionTracker))
            return false;
        if (!previousLegalMoves.equals(gameModel.previousLegalMoves))
            return false;
        return moveGenerator.equals(gameModel.moveGenerator);
    }

    /**
     * @return the board model of this game.
     */
    public BoardModel getBoard() {
        return board;
    }

    /**
     * Gets a byte array representation of the board. It will be stored in the following format:
     *
     * <ul>
     *     <li>White King data</li>
     *     <li>White Queen data</li>
     *     <li>White Rook data</li>
     *     <li>White Bishop data</li>
     *     <li>White Knight data</li>
     *     <li>White Pawn data</li>
     *     <li>Black King data</li>
     *     <li>Black Queen data</li>
     *     <li>Black Rook data</li>
     *     <li>Black Bishop data</li>
     *     <li>Black Knight data</li>
     *     <li>Black Pawn data</li>
     *     <li>EnPassant Data</li>
     *     <li>Castling Data</li>
     *     <li>Turn to NormalMove Data</li>
     * </ul>
     * <p>
     * Note that piece data is stored in the following format:
     * <ul>
     *     <li>Lowest 4 bits say how many pieces</li>
     *     <li>Coordinate of one of the pieces is stored every 6 bits</li>
     * </ul>
     * <p>
     * Note that enPassant data is stored in the following format:
     * <ul>
     *     <li>Lowest bit states if there is an EnPassant Target</li>
     *     <li>Next 6 bits represent the coordinate of the EnPassantTarget</li>
     * </ul>
     * <p>
     * Note that enPassant data is stored in the following format:
     * <ul>
     *     <li>White king-side castling rights (1 for able 0 for unable)</li>
     *     <li>White queen-side castling rights (1 for able 0 for unable)</li>
     *     <li>Black king-side castling rights (1 for able 0 for unable)</li>
     *     <li>Black queen-side castling rights (1 for able 0 for unable)</li>
     * </ul>
     * <p>
     * Finally, the lest significant bit states the turn to move, 1 for white, 0 for black.
     *
     * @return byte representation of the current state
     */
    public byte[] getRep() {
        BigInteger result = new BigInteger("0");

        final int maxTagSize = 4;
        final int maxCoordSize = 6;

        // Add Piece data
        for (Piece piece : Piece.values()) {
            if (piece == EMPTY)
                continue;

            List<ChessCoordinate> pieceLocations = board.getLocations(piece);
            for (ChessCoordinate coord : pieceLocations) {
                result = result.shiftLeft(maxCoordSize).add(
                        BigInteger.valueOf(coord.getOndDimIndex()));
            }
            result = result.shiftLeft(maxTagSize).add(BigInteger.valueOf(pieceLocations.size()));
        }

        // Add EnPassant Data
        ChessCoordinate enPassantTarget = getEnPassantTarget();
        if (enPassantTarget != null) {
            result = result.shiftLeft(maxCoordSize).add(
                    BigInteger.valueOf(enPassantTarget.getOndDimIndex()));
            result = result.shiftLeft(1).add(BigInteger.ONE);
        } else {
            result = result.shiftLeft(1);
        }

        // Add castling data
        result = result.shiftLeft(1).add(BigInteger.valueOf(canKingSideCastle(WHITE) ? 1 : 0));
        result = result.shiftLeft(1).add(BigInteger.valueOf(canQueenSideCastle(WHITE) ? 1 : 0));
        result = result.shiftLeft(1).add(BigInteger.valueOf(canKingSideCastle(BLACK) ? 1 : 0));
        result = result.shiftLeft(1).add(BigInteger.valueOf(canQueenSideCastle(BLACK) ? 1 : 0));

        // Add turn data
        result = result.shiftLeft(1).add(BigInteger.valueOf(getTurn() == WHITE ? 1 : 0));

        return result.toByteArray();
    }

    public int moveNum() {
        return moveHistory.size();
    }

    public boolean hasEPTarget() {
        return getGameState().getMap() >> 7 != 0;
    }
}
