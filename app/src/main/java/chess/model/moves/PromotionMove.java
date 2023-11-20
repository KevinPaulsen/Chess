package chess.model.moves;

import chess.ChessCoordinate;
import chess.model.BoardModel;
import chess.model.Zobrist;
import chess.model.pieces.Piece;

import static chess.model.pieces.Piece.WHITE_PAWN;

public class PromotionMove implements Movable {

    private final Piece pawn;
    private final Piece promotedPiece;
    private final long start;
    private final long end;

    public PromotionMove(Piece pawn, Piece promotedPiece, long start, long end) {
        this.pawn = pawn;
        this.promotedPiece = promotedPiece;
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
        boolean isWhite = pawn == WHITE_PAWN;
        Piece capturedPiece = state.getPieceOn(end);

        long deltaHash = 0x0L;

        if (capturedPiece != null) { // This move captures a piece
            pieceMaps[capturedPiece.ordinal()] &= ~end;
            deltaHash = Zobrist.flipPiece(capturedPiece, Long.numberOfTrailingZeros(end),
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

        pieceMaps[pawn.ordinal()] ^= start;
        deltaHash = Zobrist.flipPiece(pawn, Long.numberOfTrailingZeros(start), deltaHash);
        pieceMaps[promotedPiece.ordinal()] ^= end;
        deltaHash = Zobrist.flipPiece(promotedPiece, Long.numberOfTrailingZeros(end), deltaHash);

        return new BoardModel.BoardState(pieceMaps, white, black, occupied, deltaHash);
    }

    @Override
    public Piece getMovingPiece() {
        return pawn;
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
        int result = pawn.hashCode();
        result = 31 * result + getPromotedPiece().hashCode();
        result = 31 * result + (int) (start ^ (start >>> 32));
        result = 31 * result + (int) (end ^ (end >>> 32));
        return result;
    }

    public Piece getPromotedPiece() {
        return promotedPiece;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PromotionMove that))
            return false;

        if (start != that.start)
            return false;
        if (end != that.end)
            return false;
        if (pawn != that.pawn)
            return false;
        return getPromotedPiece() == that.getPromotedPiece();
    }

    @Override
    public String toString() {
        return ChessCoordinate.getChessCoordinate(start).toString() +
                ChessCoordinate.getChessCoordinate(end).toString() + "=" +
                promotedPiece.getStringRep();
    }
}
