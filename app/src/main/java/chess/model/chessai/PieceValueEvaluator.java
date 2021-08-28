package chess.model.chessai;

import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.pieces.Bishop;
import chess.model.pieces.Knight;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import chess.model.pieces.Queen;
import chess.model.pieces.Rook;

public class PieceValueEvaluator implements Evaluator {

    private static final int PAWN_SCORE = 100;
    private static final int KNIGHT_SCORE = 300;
    private static final int BISHOP_SCORE = 300;
    private static final int ROOK_SCORE = 500;
    private static final int QUEEN_SCORE = 900;

    @Override
    public Evaluation evaluate(GameModel game) {
        int whiteScore = 0;
        int blackScore = 0;

        for (Piece piece : game.getBoard().getWhitePieces()) {
            whiteScore += getValue(piece);
        }
        for (Piece piece : game.getBoard().getBlackPieces()) {
            blackScore += getValue(piece);
        }
        return new Evaluation(whiteScore - blackScore, 0);
    }

    private int getValue(Piece piece) {
        int score = 0;

        if (piece instanceof Pawn) {
            score = PAWN_SCORE;
        } else if (piece instanceof Knight) {
            score = KNIGHT_SCORE;
        } else if (piece instanceof Bishop) {
            score = BISHOP_SCORE;
        } else if (piece instanceof Rook) {
            score = ROOK_SCORE;
        } else if (piece instanceof Queen) {
            score = QUEEN_SCORE;
        }


        return score;
    }
}
