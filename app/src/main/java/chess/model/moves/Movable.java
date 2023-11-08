package chess.model.moves;

import chess.ChessCoordinate;
import chess.model.BoardModel;
import chess.model.pieces.Piece;

public interface Movable {

    BoardModel.BoardState nextState(BoardModel.BoardState state);

    Piece getMovingPiece();

    ChessCoordinate getStartCoordinate();

    ChessCoordinate getEndCoordinate();
}
