package chess.model.chessai;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.pieces.Piece;

import java.util.Comparator;
import java.util.List;

import static chess.model.chessai.Constants.*;
import static chess.model.pieces.Piece.BLACK_PAWN;
import static chess.model.pieces.Piece.WHITE_PAWN;

public class PieceValueEvaluator implements Evaluator {

    @Override
    public Evaluation evaluate(GameModel game) {
        int whiteScore = 0;
        int blackScore = 0;

        for (ChessCoordinate coordinate : game.getBoard().getWhitePieces()) {
            whiteScore += getValue(game.getBoard().getPieceOn(coordinate));
        }
        for (ChessCoordinate coordinate : game.getBoard().getBlackPieces()) {
            blackScore += getValue(game.getBoard().getPieceOn(coordinate));
        }
        return new Evaluation(whiteScore - blackScore, 0);
    }

    @Override
    public List<Move> getSortedMoves(GameModel game) {
        List<Move> result = game.getLegalMoves();
        result.sort(getMoveComparator(game.getBoard()));
        return result;
    }

    private Comparator<Move> getMoveComparator(BoardModel board) {
        return (move1, move2) -> Float.compare(getMoveVal(board, move1), getMoveVal(board, move2));
    }

    private float getMoveVal(BoardModel board, Move move) {
        float score = 1.0f;

        if (move.getInteractingPiece() != null) {
            if (move.getInteractingPieceEnd() == null) {
                score *= Math.abs(PieceValueEvaluator.getValue(move.getMovingPiece())
                        - PieceValueEvaluator.getValue(move.getInteractingPiece())) / 100f + 1f;
            }
            score *= 1.5;
        }//*/

        if (!(move.getMovingPiece() == WHITE_PAWN || move.getMovingPiece() == BLACK_PAWN)
                && attackedByPawn(board, move.getEndingCoordinate(), move.getMovingPiece().getColor())) {
            score *= 0.5;
        }

        return score;
    }

    private boolean attackedByPawn(BoardModel board, ChessCoordinate coordinate, char color) {
        List<List<ChessCoordinate>> finalPawnCoords = color == 'w' ?
                WHITE_PAWN.getReachableCoordinateMapFrom(coordinate) :
                BLACK_PAWN.getReachableCoordinateMapFrom(coordinate);

        boolean attackedByPawn = false;
        for (int i = 1; i < 3; i++) {
            for (ChessCoordinate searchCoord : finalPawnCoords.get(i)) {
                if (board.getPieceOn(searchCoord) == WHITE_PAWN
                        || board.getPieceOn(searchCoord) == BLACK_PAWN) {
                    attackedByPawn = true;
                    break;
                }
            }
        }

        return attackedByPawn;
    }

    public static int getValue(Piece piece) {
        int score = 0;

        switch (piece) {
            case WHITE_PAWN:
            case BLACK_PAWN:
                score = PAWN_SCORE;
                break;
            case WHITE_KNIGHT:
            case BLACK_KNIGHT:
                score = KNIGHT_SCORE;
                break;
            case WHITE_BISHOP:
            case BLACK_BISHOP:
                score = BISHOP_SCORE;
                break;
            case WHITE_ROOK:
            case BLACK_ROOK:
                score = ROOK_SCORE;
                break;
            case WHITE_QUEEN:
            case BLACK_QUEEN:
                score = QUEEN_SCORE;
                break;
        }

        return score;
    }
}
