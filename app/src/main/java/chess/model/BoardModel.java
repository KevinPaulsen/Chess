package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BoardModel {

    private static final ChessCoordinate[] CHESS_COORDINATES = createChessCoordinates();

    // The array that holds all the pieces. They are stored in the format [file][rank]
    private final Piece[] pieceArray;

    private final Set<ChessCoordinate> whitePieces;
    private final Set<ChessCoordinate> blackPieces;

    private final Set<ChessCoordinate> whiteQueens;
    private final Set<ChessCoordinate> blackQueens;
    private final Set<ChessCoordinate> whiteRooks;
    private final Set<ChessCoordinate> blackRooks;
    private final Set<ChessCoordinate> whiteBishops;
    private final Set<ChessCoordinate> blackBishops;
    private final Set<ChessCoordinate> whiteKnights;
    private final Set<ChessCoordinate> blackKnights;
    private final Set<ChessCoordinate> whitePawns;
    private final Set<ChessCoordinate> blackPawns;

    private ChessCoordinate whiteKingCoord;
    private ChessCoordinate blackKingCoord;


    public BoardModel(Piece[] pieceArray) {
        this.pieceArray = pieceArray;
        this.whitePieces = new HashSet<>();
        this.blackPieces = new HashSet<>();

        this.whiteQueens = new HashSet<>();
        this.blackQueens = new HashSet<>();
        this.whiteRooks = new HashSet<>();
        this.blackRooks = new HashSet<>();
        this.whiteBishops = new HashSet<>();
        this.blackBishops = new HashSet<>();
        this.whiteKnights = new HashSet<>();
        this.blackKnights = new HashSet<>();
        this.whitePawns = new HashSet<>();
        this.blackPawns = new HashSet<>();

        initPieces();
    }

    /**
     * Gets the chess coordinate at the given file and rank. If the requested
     * coordinate does not exist, null is returned.
     *
     * @param file the file of the chess coordinate.
     * @param rank the rank of the chess coordinate.
     * @return the coordinate with the requested file and rank.
     */
    public static ChessCoordinate getChessCoordinate(int file, int rank) {
        return ChessCoordinate.isInBounds(file, rank) ? CHESS_COORDINATES[rank * 8 + file] : null;
    }

    public static ChessCoordinate getChessCoordinate(int oneDimIndex) {
        return ChessCoordinate.isInBounds(oneDimIndex) ? CHESS_COORDINATES[oneDimIndex] : null;
    }

    /**
     * Create a 2D map of the chess coordinates.
     *
     * @return the 2D array of chess coordinates.
     */
    private static ChessCoordinate[] createChessCoordinates() {
        ChessCoordinate[] chessCoordinates = new ChessCoordinate[64];
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                chessCoordinates[rank * 8 + file] = new ChessCoordinate(file, rank);
            }
        }
        return chessCoordinates;
    }

    /**
     * Makes the given move. All pieces will be updated and moved
     * according the information in move. Move is expected to be
     * legal.
     *
     * @param move the move to make. Cannot be null.
     */
    public void move(Move move) {
        if (move != null) {
            if (getPieceOn(move.getEndingCoordinate()) != null
                    && (move.getInteractingPieceStart() == null
                    || !move.getInteractingPieceStart().equals(move.getEndingCoordinate()))) {
                throw new IllegalStateException("This move cannot exist");
            }

            movePiece(move.getInteractingPiece(), move.getInteractingPieceStart(), move.getInteractingPieceEnd());

            if (move.doesPromote()) {
                removePiece(move.getMovingPiece(), move.getStartingCoordinate());
                addPiece(move.getPromotedPiece(), move.getEndingCoordinate());
            } else {
                movePiece(move.getMovingPiece(), move.getStartingCoordinate(), move.getEndingCoordinate());
            }
        }
    }

    /**
     * Undoes the given move.
     *
     * @param move the move to undo.
     */
    public void undoMove(Move move) {
        if (move != null) {
            if (move.doesPromote()) {
                removePiece(move.getPromotedPiece(), move.getEndingCoordinate());
                addPiece(move.getMovingPiece(), move.getStartingCoordinate());
            } else {
                movePiece(move.getMovingPiece(), move.getEndingCoordinate(), move.getStartingCoordinate());
            }

            if (move.getInteractingPiece() != null) {
                if (move.getInteractingPieceEnd() == null) {
                    addPiece(move.getInteractingPiece(), move.getInteractingPieceStart());
                } else {
                    movePiece(move.getInteractingPiece(), move.getInteractingPieceEnd(),
                            move.getInteractingPieceStart());
                }
            }
        }
    }

    /**
     * Gets the piece on the given ChessCoordinate.
     *
     * @param coordinate the coordinate of the requested piece is at.
     * @return the piece on the given coordinate.
     */
    public Piece getPieceOn(ChessCoordinate coordinate) {
        return coordinate == null ? null : pieceArray[coordinate.getOndDimIndex()];
    }

    /**
     * Add piece to the board, and add the given moves to add to the piece.
     *
     * @param piece      the piece to add.
     * @param coordinate the coordinate to put the piece.
     */
    private void addPiece(Piece piece, ChessCoordinate coordinate) {
        if (piece != null && coordinate != null) {
            pieceArray[coordinate.getOndDimIndex()] = piece;

            addPieceToSet(coordinate);
            if (piece.getColor() == 'w') {
                if (!whitePieces.add(coordinate)) {
                    throw new RuntimeException("This piece already exists on the board.");
                }
            } else {
                if (!blackPieces.add(coordinate)) {
                    throw new RuntimeException("This piece already exists on the board.");
                }
            }
        }
    }

    /**
     * Removes the given piece from the board.
     *
     * @param piece the piece to remove.
     * @param coordinate the coordinate the piece is on.
     */
    private void removePiece(Piece piece, ChessCoordinate coordinate) {
        if (getPieceOn(coordinate) == null) {
            throw new IllegalStateException("Piece Data is out of sync.");
        }
        if (piece != null) {
            removePieceFromSet(coordinate);
            pieceArray[coordinate.getOndDimIndex()] = null;
            if (piece.getColor() == 'w') {
                if (!whitePieces.remove(coordinate)) {
                    throw new IllegalStateException("Attempted to remove piece that was not held.");
                }
            } else {
                if (!blackPieces.remove(coordinate)) {
                    throw new IllegalStateException("Attempted to remove piece that was not held.");
                }
            }
        }
    }

    /**
     * Adds the piece at the given coordinate from its respective set.
     *
     * @throws IllegalArgumentException if there is not a piece at the given coordinate
     * @param coordinate the coordinate of the piece to add.
     */
    private void addPieceToSet(ChessCoordinate coordinate) {
        Piece piece = getPieceOn(coordinate);
        if (piece == null) {
            throw new IllegalStateException("Piece data is out of sync.");
        }
        switch (piece) {
            case WHITE_QUEEN:
                whiteQueens.add(coordinate);
                break;
            case WHITE_ROOK:
                whiteRooks.add(coordinate);
                break;
            case WHITE_BISHOP:
                whiteBishops.add(coordinate);
                break;
            case WHITE_KNIGHT:
                whiteKnights.add(coordinate);
                break;
            case WHITE_PAWN:
                whitePawns.add(coordinate);
                break;
            case BLACK_QUEEN:
                blackQueens.add(coordinate);
                break;
            case BLACK_ROOK:
                blackRooks.add(coordinate);
                break;
            case BLACK_BISHOP:
                blackBishops.add(coordinate);
                break;
            case BLACK_KNIGHT:
                blackKnights.add(coordinate);
                break;
            case BLACK_PAWN:
                blackPawns.add(coordinate);
                break;
        }
    }

    /**
     * Removes the piece at the given coordinate from its respective set.
     *
     * @throws IllegalArgumentException if there is not a piece at the given coordinate
     * @param coordinate the coordinate of the piece to remove.
     */
    private void removePieceFromSet(ChessCoordinate coordinate) {
        Piece piece = getPieceOn(coordinate);
        if (piece == null) {
            throw new IllegalStateException("Piece data is out of sync.");
        }
        boolean didRemove;
        switch (piece) {
            case WHITE_QUEEN:
                didRemove = whiteQueens.remove(coordinate);
                break;
            case WHITE_ROOK:
                didRemove = whiteRooks.remove(coordinate);
                break;
            case WHITE_BISHOP:
                didRemove = whiteBishops.remove(coordinate);
                break;
            case WHITE_KNIGHT:
                didRemove = whiteKnights.remove(coordinate);
                break;
            case WHITE_PAWN:
                didRemove = whitePawns.remove(coordinate);
                break;
            case BLACK_QUEEN:
                didRemove = blackQueens.remove(coordinate);
                break;
            case BLACK_ROOK:
                didRemove = blackRooks.remove(coordinate);
                break;
            case BLACK_BISHOP:
                didRemove = blackBishops.remove(coordinate);
                break;
            case BLACK_KNIGHT:
                didRemove = blackKnights.remove(coordinate);
                break;
            case BLACK_PAWN:
                didRemove = blackPawns.remove(coordinate);
                break;
            default:
                throw new IllegalStateException("Attempting to Remove King from Board");
        }
        if (!didRemove) {
            throw new IllegalStateException("Piece was not in set when it should have been");
        }
    }

    /**
     * Moves the piece to the given coordinate, and adds the given number of moves
     * to the piece.
     *
     * @param piece         the piece to move.
     * @param startCoord the starting coordinate of the piece.
     * @param endCoord the ending coordinate of the piece.
     */
    public void movePiece(Piece piece, ChessCoordinate startCoord, ChessCoordinate endCoord) {
        if (piece != null) {
            if (endCoord == null) {
                removePiece(piece, startCoord);
            } else {
                updateSetMovement(startCoord, endCoord);
                pieceArray[startCoord.getOndDimIndex()] = null;
                pieceArray[endCoord.getOndDimIndex()] = piece;
            }
        }
    }

    private void updateSetMovement(ChessCoordinate startCoord, ChessCoordinate endCoord) {
        Piece piece = getPieceOn(startCoord);
        if (piece == null) {
            throw new IllegalStateException("Piece Data is out of sync.");
        }

        Set<ChessCoordinate> relevantSet;
        Set<ChessCoordinate> relevantColorSet;
        switch (piece) {
            case WHITE_QUEEN:
                relevantSet = whiteQueens;
                relevantColorSet = whitePieces;
                break;
            case WHITE_ROOK:
                relevantSet = whiteRooks;
                relevantColorSet = whitePieces;
                break;
            case WHITE_BISHOP:
                relevantSet = whiteBishops;
                relevantColorSet = whitePieces;
                break;
            case WHITE_KNIGHT:
                relevantSet = whiteKnights;
                relevantColorSet = whitePieces;
                break;
            case WHITE_PAWN:
                relevantSet = whitePawns;
                relevantColorSet = whitePieces;
                break;
            case WHITE_KING:
                whiteKingCoord = endCoord;
                whitePieces.remove(startCoord);
                whitePieces.add(endCoord);
                return;
            case BLACK_QUEEN:
                relevantSet = blackQueens;
                relevantColorSet = blackPieces;
                break;
            case BLACK_ROOK:
                relevantSet = blackRooks;
                relevantColorSet = blackPieces;
                break;
            case BLACK_BISHOP:
                relevantSet = blackBishops;
                relevantColorSet = blackPieces;
                break;
            case BLACK_KNIGHT:
                relevantSet = blackKnights;
                relevantColorSet = blackPieces;
                break;
            case BLACK_PAWN:
                relevantSet = blackPawns;
                relevantColorSet = blackPieces;
                break;
            case BLACK_KING:
                blackKingCoord = endCoord;
                blackPieces.remove(startCoord);
                blackPieces.add(endCoord);
                return;
            default:
                throw new IllegalStateException("Piece is not of expected type.");
        }

        if (!relevantSet.remove(startCoord) || !relevantSet.add(endCoord)
                || !relevantColorSet.remove(startCoord) || !relevantColorSet.add(endCoord)) {
            throw new IllegalStateException("Piece set data is out of sync");
        }
    }

    /**
     * Initialize the pieces, and local references to the relevant pieces.
     */
    private void initPieces() {
        for (int pieceIdx = 0; pieceIdx < pieceArray.length; pieceIdx++) {
            Piece piece = pieceArray[pieceIdx];
            ChessCoordinate coordinate = getChessCoordinate(pieceIdx);
            if (piece != null) {
                addPieceToSet(coordinate);

                if (piece.getColor() == 'w') {
                    if (!whitePieces.add(coordinate)) {
                        throw new IllegalStateException("Adding piece that already exists on board.");
                    }
                    if (piece == Piece.WHITE_KING) whiteKingCoord = coordinate;
                } else {
                    if (!blackPieces.add(coordinate)) {
                        throw new IllegalStateException("Adding piece that already exists on board.");
                    }
                    if (piece == Piece.BLACK_KING) blackKingCoord = coordinate;
                }
            }
        }
    }

    /**
     * @return the set of white pieces on this board.
     */
    public Set<ChessCoordinate> getWhitePieces() {
        return whitePieces;
    }

    /**
     * @return the set of black pieces on this board.
     */
    public Set<ChessCoordinate> getBlackPieces() {
        return blackPieces;
    }

    /**
     * @return the reference to the white king.
     */
    public ChessCoordinate getWhiteKingCoord() {
        return whiteKingCoord;
    }

    /**
     * @return the reference to the black king.
     */
    public ChessCoordinate getBlackKingCoord() {
        return blackKingCoord;
    }

    /**
     * @param color the color of the queen to get.
     * @return the set of queens of the requested color on this board.
     */
    public Set<ChessCoordinate> getQueens(char color) {
        if (color == 'w') {
            return whiteQueens;
        } else {
            return blackQueens;
        }
    }

    /**
     * @param color the color of the rooks to get.
     * @return the set of rooks of the requested color on this board.
     */
    public Set<ChessCoordinate> getRooks(char color) {
        if (color == 'w') {
            return whiteRooks;
        } else {
            return blackRooks;
        }
    }

    /**
     * @param color the color of the bishops to get.
     * @return the set of bishops of the requested color on this board.
     */
    public Set<ChessCoordinate> getBishops(char color) {
        if (color == 'w') {
            return whiteBishops;
        } else {
            return blackBishops;
        }
    }

    /**
     * @param color the color of the Knights to get.
     * @return the set of Knights of the requested color on this board.
     */
    public Set<ChessCoordinate> getKnights(char color) {
        if (color == 'w') {
            return whiteKnights;
        } else {
            return blackKnights;
        }
    }

    /**
     * @param color the color of the Pawns to get.
     * @return the set of Pawns of the requested color on this board.
     */
    public Set<ChessCoordinate> getPawns(char color) {
        if (color == 'w') {
            return whitePawns;
        } else {
            return blackPawns;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardModel)) return false;
        BoardModel that = (BoardModel) o;
        return Arrays.equals(pieceArray, that.pieceArray)
                && Objects.equals(whiteKingCoord, that.whiteKingCoord)
                && Objects.equals(blackKingCoord, that.blackKingCoord);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(whiteKingCoord, blackKingCoord);
        result = 31 * result + Arrays.hashCode(pieceArray);
        return result;
    }

    /**
     * TODO: Remove this function
     *
     * @return the 2D array of pieces.
     */
    public Piece[] getPieceArray() {
        return pieceArray.clone();
    }

    /**
     * Prints the board to the console. Used only for debug purposes.
     */
    public void printBoard() {
        for (Piece piece : pieceArray) {
            if (piece == null) {
                System.out.print("  ");
            } else {
                if (piece == Piece.WHITE_PAWN || piece == Piece.BLACK_PAWN) {
                    System.out.print("P ");
                } else {
                    System.out.print(piece + " ");
                }
            }
        }
        System.out.println();
    }

}
