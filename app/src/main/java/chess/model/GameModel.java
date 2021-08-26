package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.King;
import chess.model.pieces.Piece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class GameModel {

    private static final boolean DEBUG_MODE = false;

    private final BoardModel board;
    private final List<Move> moveHistory;

    private char turn;
    private boolean isOver;
    private char winner;

    public GameModel() {
        this.board = ChessBoardFactory.createNormalBoard();
        this.turn = 'w';
        this.moveHistory = new ArrayList<>();
        initPieces();
        checkRep();
    }

    public GameModel(GameModel gameModel) {
        this.board = new BoardModel(gameModel.getBoard());
        this.moveHistory = new ArrayList<>(gameModel.moveHistory);
        //moveHistory.replaceAll(Move::new);
        this.turn = gameModel.getTurn();
        this.isOver = gameModel.isOver;
        this.winner = gameModel.winner;
        initPieces();
        checkRep();
    }

    public GameModel(BoardModel board) {
        this.board = board;
        this.turn = 'w';
        this.moveHistory = new ArrayList<>();
        initPieces();
        checkRep();
    }

    private void initPieces() {
        for (BoardModel.PieceHolder pieceHolder : board.getBlackPieces().values()) {
            Piece piece = pieceHolder.getPiece();
            if (piece != null) {
                // FIXME: updateAttacking dependency
                //piece.updateAttacking(this);
            }
        }
        for (BoardModel.PieceHolder pieceHolder : board.getWhitePieces().values()) {
            Piece piece = pieceHolder.getPiece();
            if (piece != null) {
                // FIXME: UID dependency
                //piece.updateAttacking(this);
            }
        }
    }

    public BoardModel getBoard() {
        return board;
    }

    /**
     * Attempts to make a move given two Coordinates. If the coordinates correspond
     * to a legal move, the move will be made. True is returned if the move was
     * successful, false otherwise.
     *
     * @param startCoordinate the starting coordinate of the moving Piece.
     * @param endCoordinate the ending coordinate of the moving Piece.
     * @return weather or not the move was successful.
     */
    public boolean move(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        boolean didMove = false;

        // Check that moves are both on screen.
        if (startCoordinate != null && endCoordinate != null) {
            Move currentMove = null;
            Piece movingPiece = board.getPieceOn(startCoordinate);
            if (movingPiece != null) {
                for (Move move : movingPiece.updateLegalMoves(board, getLastMove())) {
                    if (startCoordinate.equals(move.getStartingCoordinate()) && endCoordinate.equals(move.getEndingCoordinate())) {
                        currentMove = move;
                        break;
                    }
                }
            }
            didMove = move(currentMove);
        }

        checkRep();
        return didMove;
    }

    /**
     * Attempts to make the given move. If the move is legal, true is returned,
     * otherwise, false is returned.
     *
     * @param move a non-null move.
     * @return true if the move is successful, false otherwise.
     */
    public boolean move(Move move) {
        checkRep();
        boolean didMove = false;

        if (move != null && move.getMovingPiece().getColor() == turn) {
            board.move(move);
            updateAfterMove(move);
            moveHistory.add(move);
            turn = (turn == 'w') ? 'b' : 'w';
            //checkGameOver(turn == 'w');
            didMove = true;
        }

        checkRep();
        return didMove;
    }

    private void updateAfterMove(Move move) {
        board.getSquare(move.getEndingCoordinate()).update(this);
        board.getSquare(move.getStartingCoordinate()).update(this);

        updatePossiblePawn(move.getStartingCoordinate());
        updatePossiblePawn(move.getEndingCoordinate());

        if (move.getInteractingPiece() != null && !move.getInteractingPieceStart().equals(move.getEndingCoordinate())) {
            board.getSquare(move.getInteractingPieceStart()).update(this);
            board.getSquare(move.getInteractingPieceEnd()).update(this);
            updatePossiblePawn(move.getStartingCoordinate());
            updatePossiblePawn(move.getEndingCoordinate());
        }
    }

    private void updatePossiblePawn(ChessCoordinate coordinate) {
        Piece piece1 = board.getPieceOn(BoardModel.getChessCoordinate(coordinate.getFile(), coordinate.getRank() + 2));
        Piece piece2 = board.getPieceOn(BoardModel.getChessCoordinate(coordinate.getFile(), coordinate.getRank() + 1));
        Piece piece3 = board.getPieceOn(BoardModel.getChessCoordinate(coordinate.getFile(), coordinate.getRank() - 1));
        Piece piece4 = board.getPieceOn(BoardModel.getChessCoordinate(coordinate.getFile(), coordinate.getRank() - 2));

        // FIXME: What is this?
        /*if (piece1 instanceof Pawn && piece1.getColor() == 'b') {
            piece1.updateAttacking(this);
        } else if (piece2 instanceof Pawn && piece2.getColor() == 'b') {
            piece2.updateAttacking(this);
        } else if (piece3 instanceof Pawn && piece3.getColor() == 'w') {
            piece3.updateAttacking(this);
        } else if (piece4 instanceof Pawn && piece4.getColor() == 'w') {
            piece4.updateAttacking(this);
        }//*/
    }

    private void checkGameOver(final boolean isWhitesMove) {
        King relevantKing = isWhitesMove ? board.getWhiteKing() : board.getBlackKing();
        boolean foundMove = false;
        Collection<BoardModel.PieceHolder> relevantPieces = isWhitesMove ? board.getWhitePieces().values() : board.getBlackPieces().values();
        for (BoardModel.PieceHolder piece : relevantPieces) {
            if (piece.getPiece() != null && !piece.getPiece().updateLegalMoves(board, getLastMove()).isEmpty()) {
                foundMove = true;
                break;
            }
        }//*/
        if (foundMove) {
            isOver = false;
            winner = 'N';
        } else {
            isOver = true;
            if (board.getSquare(relevantKing.getCoordinate()).numAttackers(isWhitesMove ? 'b' : 'w') != 0) {
                winner = isWhitesMove ? 'b' : 'w';
            } else {
                winner = 's';
            }
        }
    }

    public void undoMove(Move move) {
        checkRep();
        if (moveHistory.size() > 0 && moveHistory.get(moveHistory.size() - 1).equals(move)) {
            board.undoMove(move);
            moveHistory.remove(moveHistory.size() - 1);
            turn = (turn == 'w') ? 'b' : 'w';
            isOver = false;
            winner = 'N';
        }
        checkRep();
    }

    private void updateSquares() {
        for (Square square : board.getSquaresToUpdate()) {
            square.update(this);
        }
    }

    public List<Move> getLegalMoves(char color) {
        List<Move> result = new ArrayList<>(40);

        Collection<BoardModel.PieceHolder> relevantPieces = color == 'w' ? board.getWhitePieces().values() : board.getBlackPieces().values();

        for (BoardModel.PieceHolder piece: relevantPieces) {
            if (piece.getPiece() != null) {
                result.addAll(piece.getPiece().updateLegalMoves(board, getLastMove()));
            }
        }

        return result;
    }

    public Move cloneMove(Move move) {
        return new Move(move.getEndingCoordinate(), board.getPieceOn(move.getStartingCoordinate()),
                move.getInteractingPieceEnd(), board.getPieceOn(move.getInteractingPieceStart()),
                move.getPromotedPiece());
    }

    public Move getLastMove() {
        return moveHistory.size() == 0 ? null : moveHistory.get(moveHistory.size() - 1);
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public char getTurn() {
        return turn;
    }

    public boolean isOver() {
        return isOver;
    }

    public char getWinner() {
        return winner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameModel)) return false;
        GameModel gameModel = (GameModel) o;
        return turn == gameModel.turn && board.equals(gameModel.board) && moveHistory.equals(gameModel.moveHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, moveHistory, turn);
    }

    private void checkRep() {
        if (DEBUG_MODE) {
            if (board == null || moveHistory == null) {
                throw new RuntimeException("Representation is incorrect.");
            }
        }
    }
}
