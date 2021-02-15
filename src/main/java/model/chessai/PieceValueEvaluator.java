package main.java.model.chessai;

import main.java.model.BoardModel;
import main.java.model.GameModel;
import main.java.model.pieces.Bishop;
import main.java.model.pieces.Knight;
import main.java.model.pieces.Pawn;
import main.java.model.pieces.Piece;
import main.java.model.pieces.Queen;
import main.java.model.pieces.Rook;

public class PieceValueEvaluator implements Evaluator {

    @Override
    public Evaluation evaluate(GameModel game) {
        if (game.isOver()) {
            double score = 0;
            switch (game.getWinner()) {
                case 'w':
                    score = 10_000;
                    break;
                case 'b':
                    score = -10_000;
                    break;
                case 's':
                    score = 0;
            }
            return new Evaluation(score, 0);
        }
        int whiteScore = 0;
        int blackScore = 0;

        for (BoardModel.PieceHolder piece : game.getBoard().getWhitePieces().values()) {
            whiteScore += getValue(piece.getPiece());
        }
        for (BoardModel.PieceHolder piece : game.getBoard().getBlackPieces().values()) {
            blackScore += getValue(piece.getPiece());
        }
        return new Evaluation(whiteScore - blackScore, 0);
    }

    private int getValue(Piece piece) {
        if (piece != null) {
            return piece.getValue();
        }
        return 0;
    }
}
