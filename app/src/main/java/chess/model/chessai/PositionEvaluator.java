package chess.model.chessai;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.pieces.Piece;

import java.util.List;

import static chess.model.chessai.Constants.*;

public class PositionEvaluator implements Evaluator {

    /**
     * Evaluates the given game and returns an evaluation of this position. The
     * Evaluation will have a depth of 0, and the move will be null.
     *
     * @param game the game to evaluate.
     * @return the evaluation of this game.
     */
    @Override
    public Evaluation evaluate(GameModel game) {
        int whiteScore = 0;
        int blackScore = 0;

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                ChessCoordinate coordinate = BoardModel.getChessCoordinate(file, rank);
                Piece piece = game.getBoard().getPieceOn(coordinate);

                if (piece != null) {
                    double value = Evaluator.getValue(piece);
                    value += readTable(piece, coordinate);

                    if (piece.getColor() == 'w') {
                        whiteScore += value;
                    } else {
                        blackScore += value;
                    }
                }
            }
        }
        return new Evaluation(whiteScore - blackScore, 0);
    }

    private double readTable(Piece piece, ChessCoordinate coordinate) {
        int[] table;

        switch (piece) {
            case WHITE_PAWN:
            case BLACK_PAWN:
                table = PAWN_VALUE_MAP;
                break;
            case WHITE_KNIGHT:
            case BLACK_KNIGHT:
                table = KNIGHT_VALUE_MAP;
                break;
            case WHITE_BISHOP:
            case BLACK_BISHOP:
                table = BISHOP_VALUE_MAP;
                break;
            case WHITE_ROOK:
            case BLACK_ROOK:
                table = ROOK_VALUE_MAP;
                break;
            case WHITE_QUEEN:
            case BLACK_QUEEN:
                table = QUEEN_VALUE_MAP;
                break;
            case WHITE_KING:
            case BLACK_KING:
                table = KING_VALUE_MAP;
                break;
            default:
                throw new IllegalStateException("Piece is not of expected type");
        }

        int index = piece.getColor() == 'w' ? coordinate.getOndDimIndex() :
                (8 * (7 - coordinate.getRank()) + coordinate.getFile());

        return table[index];
    }

    /**
     * Returns a list of all the legal moves in this position, and they are sorted
     * into this evaluators best guess from most-likely to be the best move, to least
     * likely.
     *
     * @param game The game to get moves from.
     * @return the list of sorted legal moves.
     */
    @Override
    public List<Move> getSortedMoves(GameModel game) {
        return game.getLegalMoves();
    }
}
