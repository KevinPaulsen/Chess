package chess.model.chessai;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.MoveGenerator;
import chess.model.pieces.Piece;

import java.util.Comparator;
import java.util.List;

import static chess.model.chessai.Constants.*;
import static chess.model.chessai.Evaluation.EXACT;

public class PositionEvaluator implements Evaluator {

    private static final int WIN_SCORE = 100_000;


    public PositionEvaluator(GameModel game) {
    }

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

        game.getLegalMoves();
        if (game.getGameOverStatus() == GameModel.LOSER) {
            if (game.getTurn() == GameModel.WHITE) {
                return new Evaluation(null, -10_000, GameModel.WHITE, 0, EXACT, null);
            } else {
                return new Evaluation(null, 10_000, GameModel.BLACK, 0, EXACT, null);
            }
        } else if (game.getGameOverStatus() == GameModel.DRAW) {
            return new Evaluation(null, 0, Evaluation.TIE, 0, EXACT, null);
        }

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                ChessCoordinate coordinate = ChessCoordinate.getChessCoordinate(file, rank);
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

    private static double readTable(Piece piece, ChessCoordinate coordinate) {
        int[] table = switch (piece) {
            case WHITE_PAWN, BLACK_PAWN -> PAWN_VALUE_MAP;
            case WHITE_KNIGHT, BLACK_KNIGHT -> KNIGHT_VALUE_MAP;
            case WHITE_BISHOP, BLACK_BISHOP -> BISHOP_VALUE_MAP;
            case WHITE_ROOK, BLACK_ROOK -> ROOK_VALUE_MAP;
            case WHITE_QUEEN, BLACK_QUEEN -> QUEEN_VALUE_MAP;
            case WHITE_KING, BLACK_KING -> KING_VALUE_MAP;
            default -> throw new IllegalStateException("Piece is not of expected type");
        };

        int index = piece.getColor() == 'w' ? coordinate.getOndDimIndex() :
                (8 * (7 - coordinate.getRank()) + coordinate.getFile());

        return table[index];
    }

    /**
     * Returns a list of all the legal moves in this position, and they are sorted
     * into this evaluators best guess from most-likely to be the best move, to least
     * likely.
     *
     * @param game     The game to get moves from.
     * @param hashMove A previous found best move, null if none exists.
     * @return the list of sorted legal moves.
     */
    @Override
    public List<Move> getSortedMoves(GameModel game, Move hashMove) {
        List<Move> legalMoves = game.getLegalMoves();
        legalMoves.sort(new MoveComparator(game));
        if (hashMove != null) {
            if (legalMoves.remove(hashMove)) {
                legalMoves.add(0, hashMove);
            } else {
                System.out.println("Hash Collision?");
            }
        }
        return legalMoves;
    }

    private record MoveComparator(GameModel game) implements Comparator<Move> {

        @Override
            public int compare(Move o1, Move o2) {
                return Integer.compare(evaluateMove(o1), evaluateMove(o2));
            }

        private int evaluateMove(Move move) {
                int score = 0;

                Piece movingPiece = move.getMovingPiece(game.getBoard());
                // If the move captures weight moves that capture with lower value pieces higher
                if (move.getInteractingPiece(game.getBoard()) != null && move.getInteractingPieceEnd() == null) {
                    score = CAPTURE_BIAS + CAPTURED_PIECE_VALUE_MULTIPLIER * (Evaluator.getValue(move.getInteractingPiece(game.getBoard()))
                            - Evaluator.getValue(movingPiece));
                }

                if (move.doesPromote()) {
                    switch (move.getPromotedPiece()) {
                        case WHITE_QUEEN, BLACK_QUEEN -> score += QUEEN_SCORE;
                        case WHITE_ROOK, BLACK_ROOK -> score += ROOK_SCORE;
                        case WHITE_BISHOP, BLACK_BISHOP -> score += BISHOP_SCORE;
                        case WHITE_KNIGHT, BLACK_KNIGHT -> score += KNIGHT_SCORE;
                    }
                }

                score += readTable(movingPiece, move.getEndingCoordinate())
                        - readTable(movingPiece, move.getStartingCoordinate());

                return score;
            }
        }
}
