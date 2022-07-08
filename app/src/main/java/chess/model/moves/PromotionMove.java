package chess.model.moves;

import chess.ChessCoordinate;
import chess.model.BoardModel;
import chess.model.Zobrist;
import chess.model.pieces.Piece;

import static chess.model.pieces.Piece.*;

public class PromotionMove implements Movable {

    private final Piece pawn;
    private final Piece promotedPiece;
    private final long start;
    private final long end;

    public PromotionMove(Piece pawn, Piece promotedPiece, ChessCoordinate start, ChessCoordinate end) {
        this.pawn = pawn;
        this.promotedPiece = promotedPiece;
        this.start = start.getBitMask();
        this.end = end.getBitMask();
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
            deltaHash = Zobrist.flipPiece(capturedPiece, ChessCoordinate.getChessCoordinate(end), deltaHash);

            if (isWhite) black ^= end;
            else white ^= end;

            occupied ^= start;
        } else {
            occupied ^= moveMask;
        }

        if (isWhite) white ^= moveMask;
        else black ^= moveMask;

        pieceMaps[pawn.ordinal()] ^= start;
        deltaHash = Zobrist.flipPiece(pawn, ChessCoordinate.getChessCoordinate(start), deltaHash);
        pieceMaps[promotedPiece.ordinal()] ^= end;
        deltaHash = Zobrist.flipPiece(promotedPiece, ChessCoordinate.getChessCoordinate(end), deltaHash);

        return new BoardModel.BoardState(pieceMaps, white, black, occupied, deltaHash);
    }

    @Override
    public Piece getMovingPiece() {
        return pawn;
    }

    public Piece getPromotedPiece() {
        return promotedPiece;
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
    public String toString() {
        return ChessCoordinate.getChessCoordinate(start).toString()
                + ChessCoordinate.getChessCoordinate(end).toString() + "=" + promotedPiece.getStringRep();
    }
}
