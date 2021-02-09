package main.java.model.chessai;

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
        int whiteScore = 0;
        int blackScore = 0;

        for (Piece piece : game.getBoard().getWhitePieces()) {
            whiteScore += getValue(piece);
        }
        for (Piece piece : game.getBoard().getBlackPieces()) {
            blackScore += getValue(piece);
        }
        return new Evaluation(whiteScore - blackScore, game.hashCode(), 0);
    }

    private int getValue(Piece piece) {
        int value = 0;

        if (piece instanceof Pawn) {
            value = 1;
        } else if (piece instanceof Knight) {
            value = 3;
        } else if (piece instanceof Bishop) {
            value = 3;
        } else if (piece instanceof Rook) {
            value = 5;
        } else if (piece instanceof Queen) {
            value = 9;
        }

        return value;
    }
}
