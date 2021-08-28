package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.King;
import chess.model.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for generating moves.
 */
public class MoveGenerator {

    private final List<Move> moves;
    private boolean inCheck;
    private boolean inDoubleCheck;
    private boolean pinsExistInPosition;

    public MoveGenerator(boolean inCheck, boolean inDoubleCheck, boolean pinsExistInPosition) {
        this.moves = new ArrayList<>();
        this.inCheck = inCheck;
        this.inDoubleCheck = inDoubleCheck;
        this.pinsExistInPosition = pinsExistInPosition;
    }

    public void generateMoves() {

    }

    private void generateKingMoves(GameModel game) {

        BoardModel board = game.getBoard();
        King movingKing = game.getTurn() == 'w' ? board.getWhiteKing() : board.getBlackKing();
        List<List<ChessCoordinate>> possibleEndCoordinates = movingKing.getFinalCoordinates();

        for (int endCoordIdx = 0; endCoordIdx < 8; endCoordIdx++) {
            for (ChessCoordinate endCoordinate : possibleEndCoordinates.get(endCoordIdx)) {
                Piece targetPiece = board.getPieceOn(endCoordinate);

                // Skip endCoordinates that are occupied by friendly pieces.
                if (targetPiece.getColor() == movingKing.getColor()) {
                    continue;
                }


            }
        }
    }

}
