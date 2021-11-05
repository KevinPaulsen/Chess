package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Bishop;
import chess.model.pieces.BlackPawn;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
import chess.model.pieces.King;
import chess.model.pieces.Knight;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import chess.model.pieces.Queen;
import chess.model.pieces.Rook;
import chess.model.pieces.WhitePawn;
import chess.util.FastMap;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for generating moves.
 */
public class MoveGenerator {

    private final GameModel game;
    private final List<Move> moves;
    private final FastMap checkRayMap;
    private final FastMap pinRayMap;
    private final FastMap opponentAttackMap;
    private boolean inCheck;
    private boolean inDoubleCheck;
    private boolean pinsExistInPosition;

    public MoveGenerator(GameModel game) {
        this.game = game;
        this.moves = new ArrayList<>();
        this.inCheck = false;
        this.inDoubleCheck = false;
        this.pinsExistInPosition = false;

        this.checkRayMap = new FastMap();
        this.pinRayMap = new FastMap();
        this.opponentAttackMap = new FastMap();
    }

    /**
     * Updates the given sliding piece's attack map. Adds all the squares this piece
     * can move to to the given map. This will go though the firendly king, and
     * continue the ray past it.
     *
     * @param board          the board the piece is on.
     * @param attackingPiece the attacking piece to update.
     * @param coordinate     the coordinate this piece is on
     * @param map            the map to mark.
     */
    private static void updateSlidingAttackPiece(BoardModel board, Piece attackingPiece,
                                                 ChessCoordinate coordinate, FastMap map) {
        for (List<ChessCoordinate> potentialRay : attackingPiece.getFinalCoordinates(coordinate)) {
            for (ChessCoordinate potentialCoordinate : potentialRay) {
                Piece targetPiece = board.getPieceOn(potentialCoordinate);
                map.mergeMask(potentialCoordinate.getBitMask());

                if (targetPiece != null && !(targetPiece instanceof King
                        && targetPiece.getColor() != attackingPiece.getColor())) {
                    break;
                }
            }
        }
    }

    private static boolean areAligned(ChessCoordinate coordinate1, ChessCoordinate coordinate2,
                                      ChessCoordinate coordinate3) {
        float product1 = (coordinate1.getRank() - coordinate2.getRank()) * (coordinate1.getFile() - coordinate3.getFile());
        float product2 = (coordinate1.getFile() - coordinate2.getFile()) * (coordinate1.getRank() - coordinate3.getRank());
        return product1 == product2;
    }

    private static List<List<ChessCoordinate>> getReachableCoords(
            List<List<ChessCoordinate>>[][] reachableCoordinatesMap, ChessCoordinate coordinate) {
        return reachableCoordinatesMap[coordinate.getFile()][coordinate.getRank()];
    }

    public List<Move> generateMoves() {
        calculateAttackData();

        generateKingMoves();

        if (inDoubleCheck) {
            return moves;
        }

        generateSlidingMoves();
        generateKnightMoves();
        generatePawnMoves();

        return moves;
    }

    private void calculateAttackData() {
        opponentAttackMap.merge(calculateSlidingAttackMap(game));

        BoardModel board = game.getBoard();
        char turn = game.getTurn();
        char attackingColor = game.getTurn() == 'w' ? 'b' : 'w';
        ChessCoordinate kingCoord = turn == 'w' ? board.getWhiteKingCoord() : board.getBlackKingCoord();

        // Calculate the pin and check rays
        if (board.getRooks(attackingColor).size() > 0 || board.getQueens(attackingColor).size() > 0) {
            calculatePinsAndCheckRays(board, getReachableCoords(Rook.REACHABLE_COORDINATES_MAP, kingCoord),
                    false, turn);
        }
        if (board.getBishops(attackingColor).size() > 0 || board.getQueens(attackingColor).size() > 0) {
            calculatePinsAndCheckRays(board, getReachableCoords(Bishop.REACHABLE_COORDINATES_MAP, kingCoord),
                    true, turn);
        }

        // Calculate Knight Attacks
        for (ChessCoordinate knightCoord : board.getKnights(attackingColor)) {
            Piece knight = board.getPieceOn(knightCoord);
            for (List<ChessCoordinate> ray : knight.getFinalCoordinates(knightCoord)) {
                for (ChessCoordinate targetCoord : ray) {
                    opponentAttackMap.mergeMask(targetCoord.getBitMask());
                    if (targetCoord.equals(kingCoord)) {
                        inDoubleCheck = inCheck;
                        inCheck = true;
                        checkRayMap.mergeMask(knightCoord.getBitMask());
                    }
                }
            }
        }

        // Calculate Pawn Attacks
        for (ChessCoordinate pawnCoord : board.getPawns(attackingColor)) {
            Piece pawn = board.getPieceOn(pawnCoord);
            for (int rayIdx = 1; rayIdx <= 2; rayIdx++) {
                for (ChessCoordinate targetCoord : pawn.getFinalCoordinates(pawnCoord).get(rayIdx)) {
                    opponentAttackMap.mergeMask(targetCoord.getBitMask());
                    if (targetCoord.equals(kingCoord)) {
                        inDoubleCheck = inCheck;
                        inCheck = true;
                        checkRayMap.mergeMask(pawnCoord.getBitMask());
                    }
                }
            }
        }

        ChessCoordinate opponentKingCoord = turn == 'w' ? board.getBlackKingCoord() : board.getWhiteKingCoord();
        Piece opponentKing = board.getPieceOn(opponentKingCoord);

        // Calculate King Attacks
        for (List<ChessCoordinate> ray : opponentKing.getFinalCoordinates(opponentKingCoord)) {
            for (ChessCoordinate coordinate : ray) {
                opponentAttackMap.mergeMask(coordinate.getBitMask());
            }
        }
    }

    private void calculatePinsAndCheckRays(BoardModel board, List<List<ChessCoordinate>> raysToCheck,
                                           boolean isDiagonal, char turn) {
        for (List<ChessCoordinate> ray : raysToCheck) {
            if (inDoubleCheck) {
                break;
            }

            boolean friendlyRay = false;
            FastMap rayMap = new FastMap();

            for (ChessCoordinate coordinate : ray) {
                Piece targetPiece = board.getPieceOn(coordinate);
                rayMap.mergeMask(coordinate.getBitMask());

                if (targetPiece != null) { // There is a piece on this square
                    if (targetPiece.getColor() == turn) { // This piece is our color
                        if (!friendlyRay) { // This is the first friendly piece we found
                            friendlyRay = true;
                        } else { // this is the second friendly piece we found.
                            break;
                        }
                    } else { // Piece is an enemy piece.
                        if (targetPiece instanceof Queen
                                || (isDiagonal && targetPiece instanceof Bishop)
                                || (!isDiagonal && targetPiece instanceof Rook)) {
                            if (friendlyRay) {
                                pinsExistInPosition = true;
                                pinRayMap.merge(rayMap);
                            } else {
                                checkRayMap.merge(rayMap);
                                inDoubleCheck = inCheck;
                                inCheck = true;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Calculate and return the attack map of all the opponents sliding moves.
     *
     * @param game the relevant game.
     * @return the map of all the squares attacked by the sliding pieces.
     */
    private FastMap calculateSlidingAttackMap(GameModel game) {
        FastMap slidingAttackMap = new FastMap();
        char attackingColor = game.getTurn() == 'w' ? 'b' : 'w';

        for (ChessCoordinate queenCoord : game.getBoard().getQueens(attackingColor)) {
            Piece queen = game.getBoard().getPieceOn(queenCoord);
            updateSlidingAttackPiece(game.getBoard(), queen, queenCoord, slidingAttackMap);
        }

        for (ChessCoordinate rookCoordinate : game.getBoard().getRooks(attackingColor)) {
            Piece rook = game.getBoard().getPieceOn(rookCoordinate);
            updateSlidingAttackPiece(game.getBoard(), rook, rookCoordinate, slidingAttackMap);
        }
        for (ChessCoordinate bishopCoordinate : game.getBoard().getBishops(attackingColor)) {
            Piece bishop = game.getBoard().getPieceOn(bishopCoordinate);
            updateSlidingAttackPiece(game.getBoard(), bishop, bishopCoordinate, slidingAttackMap);
        }

        return slidingAttackMap;
    }

    private void generateKingMoves() {
        BoardModel board = game.getBoard();
        ChessCoordinate movingKingCoord = game.getTurn() == 'w' ? board.getWhiteKingCoord() : board.getBlackKingCoord();
        Piece movingKing = board.getPieceOn(movingKingCoord);
        ChessCoordinate friendlyKingCoord = game.getTurn() == 'w' ?
                board.getWhiteKingCoord() : board.getBlackKingCoord();
        List<List<ChessCoordinate>> possibleEndCoordinates = movingKing.getFinalCoordinates(movingKingCoord);

        // Add moves for the regular king moves
        for (int endCoordIdx = 0; endCoordIdx < 8; endCoordIdx++) {
            for (ChessCoordinate targetCoord : possibleEndCoordinates.get(endCoordIdx)) {
                Piece targetPiece = board.getPieceOn(targetCoord);

                if (opponentAttackMap.isMarked(targetCoord.getOndDimIndex())) {
                    continue;
                }

                if (targetPiece != null) {
                    // Skip endCoordinates that are occupied by friendly pieces.
                    if (targetPiece.getColor() == movingKing.getColor()) {
                        continue;
                    }

                    moves.add(new Move(movingKingCoord, targetCoord, movingKing,
                            targetCoord, null, targetPiece));
                } else {
                    moves.add(new Move(movingKingCoord, targetCoord, movingKing));
                }
            }
        }

        // Add castling moves
        for (int endCoordIdx = 8; endCoordIdx <= 9; endCoordIdx++) {
            for (ChessCoordinate targetCoord : possibleEndCoordinates.get(endCoordIdx)) {
                Piece targetPiece = board.getPieceOn(targetCoord);

                if (targetPiece != null) {
                    continue;
                }

                boolean canCastle = true;
                if (targetCoord.getFile() == 6) {
                    if (game.canKingSideCastle(movingKing.getColor())) {
                        ChessCoordinate searchCoord = friendlyKingCoord;
                        for (int numChecked = 0; numChecked < 3;
                             numChecked++, searchCoord = Directions.RIGHT.next(searchCoord)) {
                            if (numChecked > 0 && board.getPieceOn(searchCoord) != null
                                    || opponentAttackMap.isMarked(searchCoord.getOndDimIndex())) {
                                canCastle = false;
                                break;
                            }
                        }
                        if (canCastle) {
                            ChessCoordinate rookStart = BoardModel.getChessCoordinate(7, targetCoord.getRank());
                            Piece piece = board.getPieceOn(rookStart);
                            ChessCoordinate endInteracting = BoardModel.getChessCoordinate(5, targetCoord.getRank());
                            moves.add(new Move(movingKingCoord, targetCoord, movingKing, rookStart, endInteracting, piece));
                        }
                    }
                } else {
                    if (game.canQueenSideCastle(movingKing.getColor())) {
                        ChessCoordinate searchCoord = friendlyKingCoord;
                        for (int numChecked = 0; numChecked < 3;
                             numChecked++, searchCoord = Directions.LEFT.next(searchCoord)) {
                            if (board.getPieceOn(searchCoord) != null
                                    || opponentAttackMap.isMarked(searchCoord.getOndDimIndex())) {
                                canCastle = false;
                                break;
                            }
                        }
                        if (canCastle) {
                            ChessCoordinate rookStart = BoardModel.getChessCoordinate(0, targetCoord.getRank());
                            Piece piece = board.getPieceOn(rookStart);
                            ChessCoordinate endInteracting = BoardModel.getChessCoordinate(3, targetCoord.getRank());
                            moves.add(new Move(movingKingCoord, targetCoord, movingKing, rookStart, endInteracting, piece));
                        }
                    }
                }
            }
        }
    }

    private void generateSlidingMoves() {
        for (ChessCoordinate coordinate : game.getBoard().getQueens(game.getTurn())) {
            Piece piece = game.getBoard().getPieceOn(coordinate);
            generateSlidingPieceMoves(piece, coordinate);
        }
        for (ChessCoordinate coordinate : game.getBoard().getRooks(game.getTurn())) {
            Piece piece = game.getBoard().getPieceOn(coordinate);
            generateSlidingPieceMoves(piece, coordinate);
        }
        for (ChessCoordinate coordinate : game.getBoard().getBishops(game.getTurn())) {
            Piece piece = game.getBoard().getPieceOn(coordinate);
            generateSlidingPieceMoves(piece, coordinate);
        }
    }

    private void generateSlidingPieceMoves(Piece piece, ChessCoordinate coordinate) {
        BoardModel board = game.getBoard();
        ChessCoordinate friendlyKingCoord = game.getTurn() == 'w' ?
                board.getWhiteKingCoord() : board.getBlackKingCoord();
        boolean isPinned = isPinned(coordinate);

        // If pinned and in check, this piece can't move.
        if (!inCheck || !isPinned) {
            for (List<ChessCoordinate> ray : piece.getFinalCoordinates(coordinate)) {
                // If we are pinned, then we can only move along pin.
                if ((ray.size() != 0 && (!isPinned || areAligned(coordinate, ray.get(0), friendlyKingCoord)))) {
                    for (ChessCoordinate targetCoord : ray) {
                        Piece targetPiece = board.getPieceOn(targetCoord);
                        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
                            break;
                        }
                        if (!inCheck || checkRayMap.isMarked(targetCoord.getOndDimIndex())) {
                            moves.add(new Move(coordinate, targetCoord, piece, targetCoord, null, targetPiece));
                        }
                        if (targetPiece != null) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private void generateKnightMoves() {
        BoardModel board = game.getBoard();
        for (ChessCoordinate coordinate : board.getKnights(game.getTurn())) {
            if (isPinned(coordinate)) {
                continue;
            }
            Piece knight = board.getPieceOn(coordinate);

            for (List<ChessCoordinate> ray : knight.getFinalCoordinates(coordinate)) {;
                for (ChessCoordinate targetCoordinate : ray) {
                    Piece targetPiece = board.getPieceOn(targetCoordinate);
                    if ((targetPiece == null || targetPiece.getColor() != knight.getColor())
                            && (!inCheck || checkRayMap.isMarked(targetCoordinate.getOndDimIndex()))) {
                        moves.add(new Move(coordinate, targetCoordinate, knight, targetCoordinate,
                                null, targetPiece));
                    }
                }
            }
        }
    }

    private void generatePawnMoves() {
        BoardModel board = game.getBoard();
        ChessCoordinate friendlyKingCoord = game.getTurn() == 'w' ?
                board.getWhiteKingCoord() : board.getBlackKingCoord();

        for (ChessCoordinate coordinate : board.getPawns(game.getTurn())) {
            Piece pawn = board.getPieceOn(coordinate);
            List<List<ChessCoordinate>> finalCoordinates = pawn.getFinalCoordinates(coordinate);
            boolean isPinned = isPinned(coordinate);

            for (ChessCoordinate targetCoord : finalCoordinates.get(0)) {
                if (!isPinned || areAligned(coordinate, friendlyKingCoord, targetCoord)) {
                    Piece targetPiece = board.getPieceOn(targetCoord);
                    if (targetPiece == null) {
                        if (!inCheck || checkRayMap.isMarked(targetCoord.getOndDimIndex())) {
                            if ((pawn instanceof WhitePawn && targetCoord.getRank() == 7)
                                    || (pawn instanceof BlackPawn && targetCoord.getRank() == 0)) {
                                // Promotion square
                                moves.add(new Move(coordinate, targetCoord, pawn, new Queen((Pawn) pawn)));
                                moves.add(new Move(coordinate, targetCoord, pawn, new Rook((Pawn) pawn)));
                                moves.add(new Move(coordinate, targetCoord, pawn, new Bishop((Pawn) pawn)));
                                moves.add(new Move(coordinate, targetCoord, pawn, new Knight((Pawn) pawn)));
                            } else {
                                moves.add(new Move(coordinate, targetCoord, pawn));
                            }
                        }
                    } else {
                        break;
                    }
                }
            }

            for (ChessCoordinate captureRight : finalCoordinates.get(1)) {
                addAttackingPawnMoves(coordinate, isPinned, captureRight, Directions.RIGHT);
            }

            for (ChessCoordinate captureLeft : finalCoordinates.get(2)) {
                addAttackingPawnMoves(coordinate, isPinned, captureLeft, Directions.LEFT);
            }
        }
    }

    private void addAttackingPawnMoves(ChessCoordinate coordinate, boolean isPinned, ChessCoordinate captureCoord, Direction left) {
        BoardModel board = game.getBoard();
        Pawn pawn = (Pawn) board.getPieceOn(coordinate);
        ChessCoordinate friendlyKingCoord = game.getTurn() == 'w' ?
                board.getWhiteKingCoord() : board.getBlackKingCoord();
        if (isPinned && !areAligned(coordinate, friendlyKingCoord, captureCoord)) {
            return;
        }

        Piece targetPiece = board.getPieceOn(captureCoord);
        if (targetPiece == null) {
            ChessCoordinate enPassantTarget = game.getEnPassantTarget();
            if (enPassantTarget != null && enPassantTarget.equals(captureCoord)) {
                ChessCoordinate targetCoord = left.next(coordinate);
                targetPiece = board.getPieceOn(targetCoord);
                if (!inCheck || checkRayMap.isMarked(captureCoord.getOndDimIndex())
                        || checkRayMap.isMarked(targetCoord.getOndDimIndex())) {
                    if (targetPiece != null && !areAligned(coordinate, friendlyKingCoord, targetCoord)
                            || !longRangeAttacker(coordinate, targetPiece)) {
                        moves.add(new Move(coordinate, captureCoord, pawn, targetCoord,
                                null, targetPiece));
                    }
                }
                // TODO: Deal with enPassant discovery
            }
        } else {
            if (!inCheck || checkRayMap.isMarked(captureCoord.getOndDimIndex())) {
                if (targetPiece.getColor() != pawn.getColor()) {
                    if ((pawn instanceof WhitePawn && captureCoord.getRank() == 7)
                            || (pawn instanceof BlackPawn && captureCoord.getRank() == 0)) {
                        moves.add(new Move(coordinate, captureCoord, pawn, captureCoord,
                                null, targetPiece, new Queen(pawn)));
                        moves.add(new Move(coordinate, captureCoord, pawn, captureCoord,
                                null, targetPiece, new Rook(pawn)));
                        moves.add(new Move(coordinate, captureCoord, pawn, captureCoord,
                                null, targetPiece, new Bishop(pawn)));
                        moves.add(new Move(coordinate, captureCoord, pawn, captureCoord,
                                null, targetPiece, new Knight(pawn)));
                    } else {
                        moves.add(new Move(coordinate, captureCoord, pawn, captureCoord,
                                null, targetPiece));
                    }
                }
            }
        }
    }

    private boolean longRangeAttacker(ChessCoordinate coordinate, Piece capturedPiece) {
        BoardModel board = game.getBoard();
        Piece pawn = board.getPieceOn(coordinate);
        ChessCoordinate friendlyKingCoord = game.getTurn() == 'w' ?
                board.getWhiteKingCoord() : board.getBlackKingCoord();

        int xDiff = coordinate.getFile() - friendlyKingCoord.getFile();
        Direction kingToPawn = xDiff > 0 ? Directions.RIGHT : Directions.LEFT;
        boolean longRangeAttackerExists = false;

        for (ChessCoordinate searchCoord = kingToPawn.next(friendlyKingCoord);
             searchCoord != null;
             searchCoord = kingToPawn.next(searchCoord)) {
            Piece targetPiece = board.getPieceOn(searchCoord);

            if (targetPiece == null || targetPiece == pawn || targetPiece == capturedPiece) {
                continue;
            }

            if (targetPiece.getColor() != game.getTurn()
                    && targetPiece instanceof Queen || targetPiece instanceof Rook) {
                longRangeAttackerExists = true;
            }
            break;
        }

        return longRangeAttackerExists;
    }

    private boolean isPinned(ChessCoordinate coordinate) {
        return pinsExistInPosition && pinRayMap.isMarked(coordinate.getOndDimIndex());
    }
}
