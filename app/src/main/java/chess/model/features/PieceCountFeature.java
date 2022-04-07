package chess.model.features;

import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.pieces.Piece;

import static chess.model.GameModel.WHITE;
import static chess.model.pieces.Piece.EMPTY;

public class PieceCountFeature implements Feature {

    @Override
    public String featureString(GameModel game) {
        BoardModel board = game.getBoard();
        StringBuilder featureStringBuilder = new StringBuilder();

        byte[] pieceCounts = new byte[6];
        for (Piece piece : Piece.values()) {
            if (piece == EMPTY) continue;
            pieceCounts[piece.getUniqueIdx() % 6] += (piece.getColor() == WHITE ? 1 : -1)
                    * board.getLocations(piece).size();
        }

        for (byte pieceCount : pieceCounts) {
            featureStringBuilder.append(pieceCount);
            featureStringBuilder.append(",");
        }

        return featureStringBuilder.substring(0, featureStringBuilder.length() - 1);
    }
}
