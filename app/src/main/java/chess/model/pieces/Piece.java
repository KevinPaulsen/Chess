package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.Square;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * This class is an abstract representation of a Piece. Any piece can implement
 * this class. A piece contains information about how the piece moves, and its
 * color, and where it is on the board.
 */
public abstract class Piece {

    /**
     * A standard move in which the piece moves to the square, and captures
     * the piece currently occupying that square.
     */
    protected static final MoveMaker STANDARD_MOVE_MAKER = (start, end, game, code) -> {
        Piece capturedPiece = game.getBoard().getPieceOn(end);
        Piece movingPiece = game.getBoard().getPieceOn(start);
        Move move = null;
        if (capturedPiece == null || capturedPiece.color != movingPiece.color) {
            move = new Move(end, movingPiece, null, capturedPiece);
        }
        movingPiece.addAttacking(game.getBoard().getSquare(end));
        return move;
    };
    private static int identifier = 0;
    protected static final int LONG_MOVING_MAX = 8;

    private final Set<Square> attackingSquares;
    protected final Set<Move> sudoLegalMoves;
    protected final Set<MovementRule> movementRules;

    // The color of this piece
    protected final char color;
    // The coordinate this piece is currently on.
    protected ChessCoordinate coordinate;
    // Weather or not this piece has moved.
    protected int timesMoved;
    // The identifier for this piece
    private final int uniqueIdentifier;

    /**
     * Constructs a piece with no movement rules, and is on the given
     * coordinate, and is of the color specified.
     *
     * @param coordinate the coordinate this piece is on.
     * @param color      the color of this piece.
     */
    public Piece(char color, ChessCoordinate coordinate) {
        this.movementRules = new HashSet<>();
        this.attackingSquares = new HashSet<>();
        this.sudoLegalMoves = new HashSet<>();
        this.coordinate = coordinate;
        this.color = color;
        this.timesMoved = 0;
        this.uniqueIdentifier = identifier;
        identifier++;
    }

    public Piece(Piece piece) {
        this.movementRules = new HashSet<>();
        this.attackingSquares = new HashSet<>();
        this.sudoLegalMoves = new HashSet<>();
        this.coordinate = piece.coordinate;
        this.color = piece.color;
        this.timesMoved = 0;
        this.uniqueIdentifier = piece.uniqueIdentifier;
    }

    /**
     * Moves the piece to given coordinate, the given coordinate does
     * not need to follow this pieces movement rules.
     *
     * @param coordinate the coordinate this piece is moving to.
     */
    public void moveTo(Set<Square> squaresToUpdate, ChessCoordinate coordinate, int movesToAdd) {
        this.coordinate = coordinate;
        if (-1 <= movesToAdd && movesToAdd <= 1) {
            timesMoved += movesToAdd;
        }

        removeAttackingSquares(squaresToUpdate);
    }

    private void removeAttackingSquares(Set<Square> squaresToUpdate) {
        Iterator<Square> squareIterator = attackingSquares.iterator();
        while (squareIterator.hasNext()) {
            Square square = squareIterator.next();
            if (square.getPiece() != null) {
                squaresToUpdate.add(square);
            }
            squareIterator.remove();
            square.removeAttacker(this);
        }
    }

    public static Piece clone(Piece piece) {
        if (piece instanceof Pawn) {
            return new Pawn(piece);
        } else if (piece instanceof Knight) {
            return new Knight(piece);
        } else if (piece instanceof Bishop) {
            return new Bishop(piece);
        } else if (piece instanceof Rook) {
            return new Rook(piece);
        } else if (piece instanceof Queen) {
            return new Queen(piece);
        } else if (piece instanceof King) {
            return new King(piece);
        } else {
            return null;
        }
    }

    /**
     * Returns the set of all possible moves this piece can move to. This
     * Method takes into account position of other pieces, and moves according
     * to the MovementRules of this Piece. If no moves are possible, an empty
     * Set is returned. This requires that game is non-null.
     *
     * @param game the game object containing this Piece.
     * @return the set of all legal moves this piece can make.
     */
    public Set<Move> getLegalMoves(GameModel game) {
        Set<Move> moves = new HashSet<>();
        King myKing = color == 'w' ? game.getBoard().getWhiteKing() : game.getBoard().getBlackKing();

        if (game.getBoard().kingInCheck(color)) {
            moves.addAll(getCheckMoves(game, myKing));
        } else {
            moves.addAll(getNormalMoves(game, myKing.coordinate));
        }
        return moves;
    }

    private Set<Move> getCheckMoves(GameModel gameModel, King king) {
        Set<Move> moves = new HashSet<>();

        int numAttackers = gameModel.getBoard().getSquare(king.coordinate).numAttackers(color);
        if (numAttackers == 1) {
            Piece attacker = gameModel.getBoard().getSquare(king.coordinate).getAttacker(color);
            // Get Set of block/Capture squares
            Set<Square> targetSquares = getSquaresBetween(king.coordinate, attacker.coordinate, gameModel.getBoard());
            for (Square square : targetSquares) {
                if (square.isAttacking(this)) {
                    // If Set of block/capture squares contains add the move
                    moves.add(makeMove(gameModel, square.getCoordinate()));
                }
            }
        } else if (this instanceof King) {
            for (MovementRule rule : movementRules) {
                moves.addAll(rule.getMoves(coordinate, gameModel));
            }
        }

        return moves;
    }

    private Set<Move> getNormalMoves(GameModel gameModel, ChessCoordinate kingCoordinate) {
        Set<Move> moves = new HashSet<>();

        if (!isPinned(kingCoordinate, gameModel.getBoard())) {
            for (MovementRule rule : movementRules) {
                moves.addAll(rule.getMoves(coordinate, gameModel));
            }
        }

        return moves;
    }

    private boolean isPinned(ChessCoordinate kingCoordinate, BoardModel boardModel) {
        if (attackingSquares.contains(boardModel.getSquare(kingCoordinate))) {
            Direction directionToKing = Direction.getNormalDirectionTo(this.coordinate, kingCoordinate);
            if (directionToKing != null) {
                Piece piece = getPieceInDirection(boardModel, directionToKing.getOpposite());
                return piece instanceof Queen
                        || (directionToKing.isAxial() && piece instanceof Rook)
                        || (directionToKing.isDiagonal() && piece instanceof Bishop);
            }
        }
        return false;
    }

    private Piece getPieceInDirection(BoardModel boardModel, Direction direction) {
        Piece piece = null;
        for (ChessCoordinate coordinate = direction.next(this.coordinate);
             coordinate != null; coordinate = direction.next(coordinate)) {
            Piece pieceOnSquare = boardModel.getPieceOn(coordinate);
            if (pieceOnSquare != null) {
                piece = pieceOnSquare;
                break;
            }
        }
        return piece;
    }

    /**
     * If able to move, creates the move to the given coordinate. Assumes that
     * no piece blocks, and isn't pinned.
     *
     * @param gameModel the game this move occurs in
     * @param coordinate the ending coordinate
     * @return the move that moves from this.coordinate to coordinate
     */
    protected abstract Move makeMove(GameModel gameModel, ChessCoordinate coordinate);

    private Set<Square> getSquaresBetween(ChessCoordinate start, ChessCoordinate end, BoardModel board) {
        Set<Square> coordinates = new HashSet<>();
        Direction direction = Direction.getNormalDirectionTo(start, end);
        if (direction != null) {
            for (ChessCoordinate searchCoord = direction.next(start);
                 searchCoord != end;
                 searchCoord = direction.next(searchCoord)) {
                coordinates.add(board.getSquare(searchCoord));
            }
        }
        return coordinates;
    }

    /**
     * Calculates sudoLegalMoves and updates all attacking squares
     *
     * @param game the this piece is in.
     */
    public void updateAttacking(GameModel game) {
        sudoLegalMoves.clear();
        for (MovementRule movementRule : movementRules) {
            for (Move move : movementRule.getMoves(coordinate, game)) {
                if (move != null) {
                    sudoLegalMoves.add(move);
                }
            }
        }
    }

    public boolean hasMoved() {
        return timesMoved != 0;
    }

    /**
     * Returns the color of this Piece.
     *
     * @return the color of this Piece.
     */
    public char getColor() {
        return color;
    }

    /**
     * Returns the coordinate of this Piece.
     *
     * @return the coordinate this piece is on.
     */
    public ChessCoordinate getCoordinate() {
        return coordinate;
    }

    public int getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public Set<Move> getSudoLegalMoves() {
        return sudoLegalMoves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piece)) return false;
        Piece piece = (Piece) o;
        return color == piece.color && uniqueIdentifier == piece.uniqueIdentifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, uniqueIdentifier);
    }

    public void addAttacking(Square square) {
        square.addAttacker(this);
        attackingSquares.add(square);
    }

    public void removeAttacking(Square square) {
        square.removeAttacker(this);
        attackingSquares.remove(square);
    }
}
