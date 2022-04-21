package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import static chess.model.GameModel.WHITE;
import static chess.model.pieces.Piece.*;

public class BoardModel {

    // The array that holds all the pieces. They are stored in the format [file][rank]
    private final Piece[] pieces;
    private final FastMap[] pieceMaps;
    private final FastMap[] whiteMaps;
    private final FastMap[] blackMaps;

    private final FastMap black;
    private final FastMap white;
    private final FastMap occupied;

    private final Deque<Piece> takenPieces;

    private final Zobrist zobrist;

    public BoardModel(String FEN, Zobrist zobrist) {

        pieces = new Piece[64];

        pieceMaps = new FastMap[]{
                new FastMap(),
                new FastMap(),
                new FastMap(),
                new FastMap(),
                new FastMap(),
                new FastMap(),
                new FastMap(),
                new FastMap(),
                new FastMap(),
                new FastMap(),
                new FastMap(),
                new FastMap(),
        };
        whiteMaps = new FastMap[]{
                pieceMaps[6],
                pieceMaps[1],
                pieceMaps[2],
                pieceMaps[3],
                pieceMaps[4],
                pieceMaps[5]
        };
        blackMaps = new FastMap[]{
                pieceMaps[0],
                pieceMaps[7],
                pieceMaps[8],
                pieceMaps[9],
                pieceMaps[10],
                pieceMaps[11],
        };

        takenPieces = new ArrayDeque<>();


        black = new FastMap();
        white = new FastMap();
        occupied = new FastMap();


        this.zobrist = zobrist;
        int pieceIdx = 63;
        for (char c : FEN.toCharArray()) {
            if (c == '/') {
                continue;
            }

            if (49 <= c && c <= 56) {
                pieceIdx -= c - 48;
                continue;
            }

            byte squareIdx = (byte) (pieceIdx + (7 - 2 * (pieceIdx % 8)));
            Piece piece = switch (c) {
                case 'K' -> WHITE_KING;
                case 'Q' -> WHITE_QUEEN;
                case 'R' -> WHITE_ROOK;
                case 'B' -> WHITE_BISHOP;
                case 'N' -> WHITE_KNIGHT;
                case 'P' -> WHITE_PAWN;
                case 'k' -> BLACK_KING;
                case 'q' -> BLACK_QUEEN;
                case 'r' -> BLACK_ROOK;
                case 'b' -> BLACK_BISHOP;
                case 'n' -> BLACK_KNIGHT;
                case 'p' -> BLACK_PAWN;
                default -> throw new IllegalStateException("Unexpected value: " + c);
            };

            pieceMaps[piece.getUniqueIdx() % 12].mark(squareIdx);
            pieces[squareIdx] = piece;

            if (c < 'a') {
                white.mark(squareIdx);
            } else {
                black.mark(squareIdx);
            }
            occupied.mark(squareIdx);

            pieceIdx--;
        }
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
            movePiece(getPieceOn(move.getInteractingPieceStart()), move.getInteractingPieceStart(), move.getInteractingPieceEnd());

            if (move.doesPromote()) {
                removePiece(getPieceOn(move.getStartingCoordinate()), move.getStartingCoordinate(), true);
                addPiece(move.getPromotedPiece(), move.getEndingCoordinate());
            } else {
                movePiece(getPieceOn(move.getStartingCoordinate()), move.getStartingCoordinate(), move.getEndingCoordinate());
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
                // TODO test logic
                removePiece(move.getPromotedPiece(), move.getEndingCoordinate(), false);
                addPiece(move.getStartingCoordinate());
            } else {
                movePiece(getPieceOn(move.getEndingCoordinate()), move.getEndingCoordinate(), move.getStartingCoordinate());
            }

            if (move.getInteractingPieceStart() != null) {
                if (move.getInteractingPieceEnd() == null) {
                    addPiece(move.getInteractingPieceStart());
                } else {
                    movePiece(getPieceOn(move.getInteractingPieceEnd()), move.getInteractingPieceEnd(),
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
        return coordinate == null ? null : pieces[coordinate.getOndDimIndex()];
    }

    /**
     * Add piece to the board, and add the given moves to add to the piece.
     *
     * @param piece      the piece to add.
     * @param coordinate the coordinate to put the piece.
     */
    private void addPiece(Piece piece, ChessCoordinate coordinate) {
        if (piece != null && coordinate != null) {
            byte oneDimIdx = (byte) coordinate.getOndDimIndex();

            pieceMaps[piece.getUniqueIdx() % 12].mark(oneDimIdx);
            pieces[oneDimIdx] = piece;

            occupied.mark(oneDimIdx);

            if (piece.getColor() == WHITE) white.mark(oneDimIdx);
            else black.mark(oneDimIdx);

            zobrist.addPiece(piece, coordinate);
        }
    }

    private void addPiece(ChessCoordinate coordinate) {
        if (takenPieces.size() == 0) throw new IllegalStateException();

        addPiece(takenPieces.pop(), coordinate);
    }

    /**
     * Removes the given piece from the board.
     *
     * @param piece      the piece to remove.
     * @param coordinate the coordinate the piece is on.
     */
    private void removePiece(Piece piece, ChessCoordinate coordinate, boolean take) {
        if (piece != null) {
            byte oneDimIdx = (byte) coordinate.getOndDimIndex();

            pieceMaps[piece.getUniqueIdx() % 12].unmark(oneDimIdx);
            pieces[oneDimIdx] = null;

            occupied.unmark(oneDimIdx);

            if (piece.getColor() == WHITE) white.unmark(oneDimIdx);
            else black.unmark(oneDimIdx);

            if (take) takenPieces.push(piece);

            zobrist.removePiece(piece, coordinate);
        }
    }

    /**
     * Moves the piece to the given coordinate, and adds the given number of moves
     * to the piece.
     *
     * @param piece      the piece to move.
     * @param startCoord the starting coordinate of the piece.
     * @param endCoord   the ending coordinate of the piece.
     */
    public void movePiece(Piece piece, ChessCoordinate startCoord, ChessCoordinate endCoord) {
        if (piece != null) {
            removePiece(piece, startCoord, true);

            if (endCoord != null) {
                addPiece(endCoord);
            }
        }
    }

    public List<ChessCoordinate> getLocations(Piece piece) {
        FastMap map = pieceMaps[piece.getUniqueIdx() % 12];

        Collection<Byte> markedIndices = map.markedIndices();
        List<ChessCoordinate> locations = new ArrayList<>(8);

        for (byte coordIdx : markedIndices) {
            locations.add(ChessCoordinate.getChessCoordinate(coordIdx));
        }

        return locations;
    }

    /**
     * @return the reference to the white king.
     */
    public ChessCoordinate getWhiteKingCoord() {
        return ChessCoordinate.getChessCoordinate(pieceMaps[WHITE_KING.getUniqueIdx() % 12].getLowestSet());
    }

    /**
     * @return the reference to the black king.
     */
    public ChessCoordinate getBlackKingCoord() {
        return ChessCoordinate.getChessCoordinate(pieceMaps[BLACK_KING.getUniqueIdx() % 12].getLowestSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardModel that)) return false;

        return Arrays.equals(pieceMaps, that.pieceMaps);
    }

    @Override
    public int hashCode() {
        return (int) zobrist.getHashValue();
    }

    /**
     * TODO: Remove this function
     *
     * @return the 2D array of pieces.
     */
    public Piece[] getPieceArray() {
        Piece[] pieceArray = new Piece[64];

        for (int idx = 0; idx < pieceArray.length; idx++) {
            pieceArray[idx] = getPieceOn(ChessCoordinate.getChessCoordinate(idx));
        }

        return pieceArray;
    }

    public boolean isOccupiedByColor(ChessCoordinate potentialCoordinate, char color) {
        if (color == WHITE) {
            return white.isMarked(potentialCoordinate.getOndDimIndex());
        } else {
            return black.isMarked(potentialCoordinate.getOndDimIndex());
        }
    }

    public boolean isKing(ChessCoordinate coordinate) {
        return pieceMaps[WHITE_KING.getUniqueIdx() % 12].isMarked(coordinate.getOndDimIndex())
                || pieceMaps[BLACK_KING.getUniqueIdx() % 12].isMarked(coordinate.getOndDimIndex());
    }

    public boolean isQueen(ChessCoordinate coordinate) {
        return pieceMaps[WHITE_QUEEN.getUniqueIdx() % 12].isMarked(coordinate.getOndDimIndex())
                || pieceMaps[BLACK_QUEEN.getUniqueIdx() % 12].isMarked(coordinate.getOndDimIndex());
    }

    public boolean isBishop(ChessCoordinate coordinate) {
        return pieceMaps[WHITE_BISHOP.getUniqueIdx() % 12].isMarked(coordinate.getOndDimIndex())
                || pieceMaps[BLACK_BISHOP.getUniqueIdx() % 12].isMarked(coordinate.getOndDimIndex());
    }

    public boolean isRook(ChessCoordinate coordinate) {
        return pieceMaps[WHITE_ROOK.getUniqueIdx() % 12].isMarked(coordinate.getOndDimIndex())
                || pieceMaps[BLACK_ROOK.getUniqueIdx() % 12].isMarked(coordinate.getOndDimIndex());
    }

    public boolean isPawn(ChessCoordinate coordinate) {
        return pieceMaps[WHITE_PAWN.getUniqueIdx() % 12].isMarked(coordinate.getOndDimIndex())
                || pieceMaps[BLACK_PAWN.getUniqueIdx() % 12].isMarked(coordinate.getOndDimIndex());
    }

    public boolean isOccupied(ChessCoordinate coordinate) {
        return occupied.isMarked(coordinate.getOndDimIndex());
    }
}
