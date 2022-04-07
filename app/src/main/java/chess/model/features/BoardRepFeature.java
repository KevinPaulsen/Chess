package chess.model.features;

import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.pieces.Piece;

import static chess.model.GameModel.BLACK;
import static chess.model.GameModel.WHITE;
import static chess.model.pieces.Piece.EMPTY;

public class BoardRepFeature implements Feature {

    private static final String EMPTY_REP =        ",0,0,0,0,0,0,0,0,0,0,0,0";
    private static final String WHITE_PAWN_REP =   ",1,0,0,0,0,0,0,0,0,0,0,0";
    private static final String WHITE_KNIGHT_REP = ",0,1,0,0,0,0,0,0,0,0,0,0";
    private static final String WHITE_BISHOP_REP = ",0,0,1,0,0,0,0,0,0,0,0,0";
    private static final String WHITE_ROOK_REP =   ",0,0,0,1,0,0,0,0,0,0,0,0";
    private static final String WHITE_QUEEN_REP =  ",0,0,0,0,1,0,0,0,0,0,0,0";
    private static final String WHITE_KING_REP =   ",0,0,0,0,0,1,0,0,0,0,0,0";
    private static final String BLACK_PAWN_REP =   ",0,0,0,0,0,0,1,0,0,0,0,0";
    private static final String BLACK_KNIGHT_REP = ",0,0,0,0,0,0,0,1,0,0,0,0";
    private static final String BLACK_BISHOP_REP = ",0,0,0,0,0,0,0,0,1,0,0,0";
    private static final String BLACK_ROOK_REP =   ",0,0,0,0,0,0,0,0,0,1,0,0";
    private static final String BLACK_QUEEN_REP =  ",0,0,0,0,0,0,0,0,0,0,1,0";
    private static final String BLACK_KING_REP =   ",0,0,0,0,0,0,0,0,0,0,0,1";

    @Override
    public String featureString(GameModel game) {
        BoardModel board = game.getBoard();
        StringBuilder featureStringBuilder = new StringBuilder();

        featureStringBuilder.append(game.getTurn() == WHITE ? 1 : 0);
        featureStringBuilder.append(',');
        featureStringBuilder.append(game.canKingSideCastle(WHITE) ? 1 : 0);
        featureStringBuilder.append(',');
        featureStringBuilder.append(game.canKingSideCastle(BLACK) ? 1 : 0);
        featureStringBuilder.append(',');
        featureStringBuilder.append(game.canQueenSideCastle(WHITE) ? 1 : 0);
        featureStringBuilder.append(',');
        featureStringBuilder.append(game.canQueenSideCastle(BLACK) ? 1 : 0);

        // Note we are not adding enPassant

        for (Piece piece : board.getPieceArray()) {
            featureStringBuilder.append(getPieceString(piece));
        }

        return featureStringBuilder.toString();
    }

    private String getPieceString(Piece piece) {
        if (piece == null) piece = EMPTY;
        return switch (piece) {
            case EMPTY -> EMPTY_REP;
            case WHITE_PAWN -> WHITE_PAWN_REP;
            case WHITE_KNIGHT -> WHITE_KNIGHT_REP;
            case WHITE_ROOK -> WHITE_ROOK_REP;
            case WHITE_BISHOP -> WHITE_BISHOP_REP;
            case WHITE_QUEEN -> WHITE_QUEEN_REP;
            case WHITE_KING -> WHITE_KING_REP;
            case BLACK_PAWN -> BLACK_PAWN_REP;
            case BLACK_KNIGHT -> BLACK_KNIGHT_REP;
            case BLACK_BISHOP -> BLACK_BISHOP_REP;
            case BLACK_ROOK -> BLACK_ROOK_REP;
            case BLACK_QUEEN -> BLACK_QUEEN_REP;
            case BLACK_KING -> BLACK_KING_REP;
        };
    }
}
