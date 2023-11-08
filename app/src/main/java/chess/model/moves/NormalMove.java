package chess.model.moves;

import chess.ChessCoordinate;
import chess.model.BoardModel;
import chess.model.Zobrist;
import chess.model.pieces.Piece;

import static chess.model.GameModel.WHITE;

/**
 * This class contains all the information needed to make a move.
 */
public class NormalMove implements Movable {

    private final Piece moving;
    private final long start;
    private final long end;

    /**
     * Creates a move that does not capture or do anything special.
     *
     * @param moving the piece that is moving
     * @param start  the coordinate the moving piece starts on.
     * @param end    the ending coordinate.
     */
    public NormalMove(Piece moving, long start, long end) {
        this.moving = moving;
        this.start = start;
        this.end = end;
    }

    @Override
    public BoardModel.BoardState nextState(BoardModel.BoardState state) {
        long[] pieceMaps = state.pieceMaps().clone();
        long occupied = state.occupied();
        long white = state.white();
        long black = state.black();
        long moveMask = start | end;
        boolean isWhite = moving.getColor() == WHITE;
        Piece capturedPiece = state.getPieceOn(end);

        long deltaHash = 0x0L;

        if (capturedPiece != null) { // This move captures a piece

            pieceMaps[capturedPiece.ordinal()] &= ~end;
            deltaHash = Zobrist.flipPiece(capturedPiece, ChessCoordinate.getChessCoordinate(end),
                                          deltaHash);

            if (isWhite)
                black ^= end;
            else
                white ^= end;

            occupied ^= start;
        } else {
            occupied ^= moveMask;
        }

        if (isWhite)
            white ^= moveMask;
        else
            black ^= moveMask;

        pieceMaps[moving.ordinal()] ^= moveMask;
        deltaHash = Zobrist.flipPiece(moving, ChessCoordinate.getChessCoordinate(start), deltaHash);
        deltaHash = Zobrist.flipPiece(moving, ChessCoordinate.getChessCoordinate(end), deltaHash);

        return new BoardModel.BoardState(pieceMaps, white, black, occupied, deltaHash);
    }

    @Override
    public Piece getMovingPiece() {
        return moving;
    }

    @Override
    public ChessCoordinate getStartCoordinate() {
        return ChessCoordinate.getChessCoordinate(start);
    }

    @Override
    public ChessCoordinate getEndCoordinate() {
        return ChessCoordinate.getChessCoordinate(end);
    }

    @Override
    public int hashCode() {
        int result = moving.hashCode();
        result = 31 * result + (int) (start ^ (start >>> 32));
        result = 31 * result + (int) (end ^ (end >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NormalMove move))
            return false;

        if (start != move.start)
            return false;
        if (end != move.end)
            return false;
        return moving == move.moving;
    }

    @Override
    public String toString() {
        return ChessCoordinate.getChessCoordinate(start).toString() +
                ChessCoordinate.getChessCoordinate(end).toString();
    }
}
