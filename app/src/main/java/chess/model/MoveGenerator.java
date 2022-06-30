package chess.model;

import chess.ChessCoordinate;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
import chess.model.pieces.Piece;
import chess.util.BitIterator;

import java.util.LinkedHashMap;
import java.util.Map;

import static chess.ChessCoordinate.*;
import static chess.model.GameModel.BLACK;
import static chess.model.GameModel.WHITE;
import static chess.model.pieces.Direction.LEFT;
import static chess.model.pieces.Direction.RIGHT;
import static chess.model.pieces.Directions.*;
import static chess.model.pieces.Piece.*;

/**
 * This class is responsible for generating moves.
 */
public class MoveGenerator {

    private static final int BITS_IN_BYTE = 8;

    private static final long ONES = 0xFFFFFFFFFFFFFFFFL;
    private static final long WHITE_KING_CASTLE_MASK  = 0x0000000000000070L;
    private static final long WHITE_QUEEN_CASTLE_MASK = 0x000000000000001EL;
    private static final long BLACK_KING_CASTLE_MASK  = 0x7000000000000000L;
    private static final long BLACK_QUEEN_CASTLE_MASK = 0x1E00000000000000L;
    private static final long WHITE_QUEEN_ATTACK_CASTLE_MASK = 0x000000000000001CL;
    private static final long BLACK_QUEEN_ATTACK_CASTLE_MASK = 0x1C00000000000000L;

    private static final long[] FILE_MASKS = {
            0x0101010101010101L,
            0x0202020202020202L,
            0x0404040404040404L,
            0x0808080808080808L,
            0x1010101010101010L,
            0x2020202020202020L,
            0x4040404040404040L,
            0x8080808080808080L,
    };

    private static final long[] ROW_MASKS = {
            0x00000000000000FFL,
            0x000000000000FF00L,
            0x0000000000FF0000L,
            0x00000000FF000000L,
            0x000000FF00000000L,
            0x0000FF0000000000L,
            0x00FF000000000000L,
            0xFF00000000000000L,
    };

    /**
     * Accessed by
     * coordinateToMask[posIdx][direction.ordinal]
     */
    private static final long[][] coordinateToMask = createDirectionMaskMap();

    private static final long[] KNIGHT_MOVE_MASKS = createKnightMoveMasks();

    private static final long[] KING_MOVE_MASKS = createKingMoveMasks();

    private final GameModel game;
    private final BoardModel board;
    private long checkRayMask;
    private long hvPinRayMap;
    private long d12PinRayMap;
    private long opponentAttackMap;
    private long epTarget;
    private MoveList moves;
    private boolean inCheck;
    private boolean inDoubleCheck;
    private Piece friendlyPawn;
    private Piece friendlyKnight;
    private Piece friendlyBishop;
    private Piece friendlyRook;
    private Piece friendlyQueen;
    private Piece attackingPawn;
    private Piece attackingKnight;
    private Piece attackingBishop;
    private Piece attackingRook;
    private Piece attackingQueen;
    private ChessCoordinate friendlyKingCoord;
    private ChessCoordinate attackingKingSquare;

    private char friendlyColor;
    private char attackingColor;

    public MoveGenerator(GameModel game) {
        this.game = game;
        this.board = game.getBoard();
    }

    private void resetState() {
        this.moves = new MoveList(board);
        this.inCheck = false;
        this.inDoubleCheck = false;

        this.checkRayMask = 0;
        this.hvPinRayMap = 0;
        this.d12PinRayMap = 0;
        this.opponentAttackMap = 0;
        this.epTarget = 0x0L;

        this.friendlyColor = game.getTurn();
        this.attackingColor = friendlyColor == WHITE ? BLACK : WHITE;

        if (friendlyColor == WHITE) {
            friendlyPawn = WHITE_PAWN;
            friendlyKnight = WHITE_KNIGHT;
            friendlyBishop = WHITE_BISHOP;
            friendlyRook = WHITE_ROOK;
            friendlyQueen = WHITE_QUEEN;

            attackingPawn = BLACK_PAWN;
            attackingKnight = BLACK_KNIGHT;
            attackingBishop = BLACK_BISHOP;
            attackingRook = BLACK_ROOK;
            attackingQueen = BLACK_QUEEN;

            friendlyKingCoord = board.getWhiteKingCoord();
            attackingKingSquare = board.getBlackKingCoord();
        } else {
            attackingPawn = WHITE_PAWN;
            attackingKnight = WHITE_KNIGHT;
            attackingBishop = WHITE_BISHOP;
            attackingRook = WHITE_ROOK;
            attackingQueen = WHITE_QUEEN;

            friendlyPawn = BLACK_PAWN;
            friendlyKnight = BLACK_KNIGHT;
            friendlyBishop = BLACK_BISHOP;
            friendlyRook = BLACK_ROOK;
            friendlyQueen = BLACK_QUEEN;

            friendlyKingCoord = board.getBlackKingCoord();
            attackingKingSquare = board.getWhiteKingCoord();
        }
    }

    public MoveList generateMoves() {
        resetState();
        calculateAttackData();

        generateKingMoves();

        if (!inDoubleCheck) {
            long queens = board.getPieceMap(friendlyQueen);
            long rooks = board.getPieceMap(friendlyRook);
            long bishops = board.getPieceMap(friendlyBishop);
            generateRookAndBishopMoves((queens | rooks) & ~d12PinRayMap, queens, hvPinRayMap, friendlyRook, STRAIGHT_COMPLEMENTS);
            generateRookAndBishopMoves((queens | bishops) & ~hvPinRayMap, queens, d12PinRayMap, friendlyBishop, DIAGONAL_COMPLEMENTS);
            generateKnightMoves();
            generatePawnMoves();
        }

        return moves;
    }

    private void calculateAttackData() {
        // Calculate the pin and check rays, and the sliding attack map
        calculateSlidingAttackMap();

        long friendlyKingMask = friendlyKingCoord.getBitMask();

        calculateKnightAttackData(friendlyKingMask);

        calculatePawnAttackData(friendlyKingMask);

        // Calculate King Attacks
        opponentAttackMap |= KING_MOVE_MASKS[attackingKingSquare.getOndDimIndex()];

        if (checkRayMask == 0) checkRayMask = ~checkRayMask;
    }

    private void calculateKnightAttackData(long friendlyKingMask) {
        BitIterator bitIterator = new BitIterator(board.getPieceMap(attackingKnight));
        while (bitIterator.hasNext()) {
            ChessCoordinate knightCoord = bitIterator.next();
            long attackMask = KNIGHT_MOVE_MASKS[knightCoord.getOndDimIndex()];
            opponentAttackMap |= attackMask;

            if ((attackMask & friendlyKingMask) != 0) {
                inDoubleCheck = inCheck;
                inCheck = true;
                checkRayMask |= knightCoord.getBitMask();
            }
        }
    }

    private void calculatePawnAttackData(long friendlyKingMask) {
        // Calculate Pawn Attacks
        long pawns = board.getPieceMap(attackingPawn);
        long pawnAttackSquares;
        if (friendlyColor == BLACK) {
            pawnAttackSquares = ((pawns & ~FILE_MASKS[0]) << 7) | ((pawns & ~FILE_MASKS[7]) << 9);
        } else {
            pawnAttackSquares = ((pawns & ~FILE_MASKS[0]) >>> 9) | ((pawns & ~FILE_MASKS[7]) >>> 7);
        }

        // Set Pawn Check data
        opponentAttackMap |= pawnAttackSquares;
        if ((pawnAttackSquares & friendlyKingMask) != 0) {
            inDoubleCheck = inCheck;
            inCheck = true;
            int kingFile = friendlyKingCoord.getFile();
            long attackingPieceBit = ROW_MASKS[friendlyKingCoord.getRank() + ((friendlyColor == WHITE) ? 1 : -1)]
                    & ((kingFile > 0 ? FILE_MASKS[friendlyKingCoord.getFile() - 1] : 0)
                    | (kingFile < 7 ? FILE_MASKS[friendlyKingCoord.getFile() + 1] : 0));
            checkRayMask |= pawns & attackingPieceBit;
        }

        // Set Pawn ep bit
        pawns = board.getPieceMap(friendlyPawn);
        if (game.hasEPTarget() && (epTarget = game.getEnPassantTarget().getBitMask()) != 0) {
            long eplPawn, eprPawn, epRank, epTargetPawn;
            if (friendlyColor == WHITE) {
                epRank = ROW_MASKS[4];
                eplPawn = pawns & (epTarget >>> 7);
                eprPawn = pawns & (epTarget >>> 9);
                epTargetPawn = epTarget >>> 8;
            } else {
                epRank = ROW_MASKS[3];
                eplPawn = pawns & (epTarget << 9);
                eprPawn = pawns & (epTarget << 7);
                epTargetPawn = epTarget << 8;
            }

            long rookAndQueen = board.getPieceMap(attackingRook) | board.getPieceMap(attackingQueen);
            if (!((epRank & friendlyKingMask) == 0 || (epRank & rookAndQueen) == 0 || (epRank & pawns) == 0)) {
                long upperMask = coordinateToMask[friendlyKingCoord.getOndDimIndex()][RIGHT.ordinal()];
                long lowerMask = coordinateToMask[friendlyKingCoord.getOndDimIndex()][LEFT.complement().ordinal()];
                if (eplPawn != 0) {
                    long afterEP = board.getOccupancyMap() ^ eplPawn ^ epTargetPawn;
                    long moveMask = getMoveMask(lowerMask, upperMask, afterEP);
                    if ((moveMask & rookAndQueen) != 0) epTarget = 0;
                }
                if (eprPawn != 0) {
                    long afterEP = board.getOccupancyMap() ^ eprPawn ^ epTargetPawn;
                    long moveMask = getMoveMask(lowerMask, upperMask, afterEP);
                    if ((moveMask & rookAndQueen) != 0) epTarget = 0;
                }
            }
        }
    }

    /**
     * Calculate and return the attack map of all the opponents sliding moves.
     */
    private void calculateSlidingAttackMap() {
        long slidingAttackMap = 0x0;
        long friendlyKingBit = friendlyKingCoord.getBitMask();
        long boardWithoutKing = board.getOccupancyMap() ^ friendlyKingBit;

        long queens = board.getPieceMap(attackingQueen);

        BitIterator bitIterator = new BitIterator(this.board.getPieceMap(attackingRook) | queens);
        slidingAttackMap |= getSlidingAttackMap(friendlyKingBit, boardWithoutKing, bitIterator, STRAIGHT_COMPLEMENTS);

        bitIterator = new BitIterator(this.board.getPieceMap(attackingBishop) | queens);
        slidingAttackMap |= getSlidingAttackMap(friendlyKingBit, boardWithoutKing, bitIterator, DIAGONAL_COMPLEMENTS);

        opponentAttackMap |= slidingAttackMap;
    }

    private long getSlidingAttackMap(long friendlyKingBit, long board, BitIterator bitIterator, Directions directions) {
        long slidingAttackMap = 0x0L;
        while (bitIterator.hasNext()) {
            ChessCoordinate attackingCoordinate = bitIterator.next();
            long ray = generateSlidingPieceMoves(directions, attackingCoordinate, board, PinCheckStatus.CHECK);
            slidingAttackMap |= ray;

            if ((ray & friendlyKingBit) == 0) {
                generateSlidingPieceMoves(directions, attackingCoordinate, board & ~ray, PinCheckStatus.PIN);
            }
        }
        return slidingAttackMap;
    }

    private void generateKingMoves() {
        Piece movingKing = friendlyColor == WHITE ? WHITE_KING : BLACK_KING;

        // Add moves for the regular king moves
        long kingMoveMask = KING_MOVE_MASKS[friendlyKingCoord.getOndDimIndex()]
                & ~(board.getOccupancyMap(friendlyColor) | opponentAttackMap);
        addMoves(movingKing, friendlyKingCoord, kingMoveMask, MoveList.Status.NORMAL);

        // Add castling moves
        kingMoveMask = 0x0L;
        long occupancy = (board.getOccupancyMap() ^ friendlyKingCoord.getBitMask());
        if (friendlyColor == WHITE) {
            if (game.canKingSideCastle(WHITE) && (occupancy & WHITE_KING_CASTLE_MASK) == 0 && (opponentAttackMap & WHITE_KING_CASTLE_MASK) == 0)
                kingMoveMask |= G1.getBitMask();
            if (game.canQueenSideCastle(WHITE) && (occupancy & WHITE_QUEEN_CASTLE_MASK) == 0 && (opponentAttackMap & WHITE_QUEEN_ATTACK_CASTLE_MASK) == 0)
                kingMoveMask |= C1.getBitMask();
        } else {
            if (game.canKingSideCastle(BLACK) && (occupancy & BLACK_KING_CASTLE_MASK) == 0 && ((opponentAttackMap & BLACK_KING_CASTLE_MASK) == 0))
                kingMoveMask |= G8.getBitMask();
            if (game.canQueenSideCastle(BLACK) && (occupancy & BLACK_QUEEN_CASTLE_MASK) == 0 && ((opponentAttackMap & BLACK_QUEEN_ATTACK_CASTLE_MASK) == 0))
                kingMoveMask |= C8.getBitMask();
        }

        addMoves(movingKing, friendlyKingCoord, kingMoveMask, MoveList.Status.CASTLING);//*/
    }

    private void generateRookAndBishopMoves(long slidingPieceMask, long queenMask, long pinMask, Piece friendlyPiece, Directions directions) {
        long pinnedPieces = slidingPieceMask & pinMask;
        long unpinnedPieces = slidingPieceMask & ~pinMask;

        BitIterator bitIterator = new BitIterator(pinnedPieces);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            long legalMoveMap = generateSlidingPieceMoves(directions, coordinate, board.getOccupancyMap(), PinCheckStatus.NONE)
                    & pinMask & checkRayMask & ~board.getOccupancyMap(friendlyColor);

            if ((coordinate.getBitMask() & queenMask) != 0) {
                addMoves(friendlyQueen, coordinate, legalMoveMap, MoveList.Status.NORMAL);
            } else {
                addMoves(friendlyPiece, coordinate, legalMoveMap, MoveList.Status.NORMAL);
            }
        }

        bitIterator = new BitIterator(unpinnedPieces);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            long legalMoveMap = generateSlidingPieceMoves(directions, coordinate, board.getOccupancyMap(), PinCheckStatus.NONE)
                    & checkRayMask & ~board.getOccupancyMap(friendlyColor);
            addMoves(friendlyPiece, coordinate, legalMoveMap, MoveList.Status.NORMAL);
        }
    }

    private long generateSlidingPieceMoves(Directions directions, ChessCoordinate coordinate, long board, PinCheckStatus status) {
        long moveMask = 0x0L;
        for (Direction direction : directions) {
            long upperMask = coordinateToMask[coordinate.getOndDimIndex()][direction.ordinal()];
            long lowerMask = coordinateToMask[coordinate.getOndDimIndex()][direction.complement().ordinal()];
            long ray = getMoveMask(lowerMask, upperMask, board);
            moveMask |= ray;

            switch (status) {
                case PIN -> {
                    long kingBit = friendlyKingCoord.getBitMask();
                    if ((kingBit & (upperMask | lowerMask)) != 0 && (kingBit & ray) != 0) {
                        long lower = coordinate.getBitMask();
                        long upper = kingBit;
                        if (Long.compareUnsigned(lower, upper) == 1) {
                            long temp = lower;
                            lower = upper;
                            upper = temp;
                        }
                        if (directions == DIAGONAL_COMPLEMENTS) {
                            d12PinRayMap |= getBitsBetweenInclusive(lower, upper, ray | coordinate.getBitMask());
                        } else {
                            hvPinRayMap |= getBitsBetweenInclusive(lower, upper, ray | coordinate.getBitMask());
                        }
                    }
                }
                case CHECK -> {
                    long kingBit = friendlyKingCoord.getBitMask();
                    if ((kingBit & ray) != 0) {
                        long lower = coordinate.getBitMask();
                        long upper = kingBit;
                        if (Long.compareUnsigned(lower, upper) == 1) {
                            long temp = lower;
                            lower = upper;
                            upper = temp;
                        }
                        checkRayMask |= getBitsBetweenInclusive(lower, upper, ray | coordinate.getBitMask());
                        inDoubleCheck = inCheck;
                        inCheck = true;
                    }
                }
            }
        }
        return moveMask;
    }

    private void addMoves(Piece piece, ChessCoordinate startingCoordinate, long moveMask, MoveList.Status status) {
        moves.add(piece, startingCoordinate, moveMask, status);
    }


    private long getMoveMask(long lowerMask, long upperMask, long board) {
        long lower = board & lowerMask;
        long upper = board & upperMask;

        return getBitsBetweenInclusive(board & lower, board & upper, upperMask | lowerMask);
    }

    private long getBitsBetweenInclusive(long lower, long upper, long mask) {
        long rightBits = Long.lowestOneBit(upper << 1) - 1;
        long leftBits = lower == 0 ? ONES : -Long.highestOneBit(lower);
        return rightBits & leftBits & mask;
    }

    private void generateKnightMoves() {
        long knights = board.getPieceMap(friendlyKnight) & ~(hvPinRayMap | d12PinRayMap);
        BitIterator bitIterator = new BitIterator(knights);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            long moveMask = KNIGHT_MOVE_MASKS[coordinate.getOndDimIndex()] & ~board.getOccupancyMap(friendlyColor) & checkRayMask;
            addMoves(friendlyKnight, coordinate, moveMask, MoveList.Status.NORMAL);
        }
    }

    private void generatePawnMoves() {

        long pawns = board.getPieceMap(friendlyPawn);
        long enemy = board.getOccupancyMap(attackingColor);
        long empty = ~board.getOccupancyMap();

        long pawnsLR = pawns & ~hvPinRayMap;
        long pawnsHV = pruneStraight(pawns & ~d12PinRayMap);
        long promotion = (ROW_MASKS[0] | ROW_MASKS[7]);
        long notPromotion = ~promotion & checkRayMask;
        promotion &= checkRayMask;

        long lPawns = pruneLeft(pawnsLR & ~FILE_MASKS[0]);
        long rPawns = pruneRight(pawnsLR & ~FILE_MASKS[7]);
        long fPawns;
        long pPawns;
        long eplTargetBit = 0;
        long eprTargetBit = 0;

        if (friendlyColor == WHITE) {
            lPawns = lPawns << 7;
            rPawns = rPawns << 9;
            fPawns = (pawnsHV << 8) & empty;
            pPawns = ((fPawns & ROW_MASKS[2]) << 8) & empty & checkRayMask;
        } else {
            lPawns = lPawns >>> 9;
            rPawns = rPawns >>> 7;
            fPawns = (pawnsHV >>> 8) & empty;
            pPawns = ((fPawns & ROW_MASKS[5]) >>> 8) & empty & checkRayMask;
        }

        if (epTarget != 0) {
            long shiftedCheckMask = friendlyColor == WHITE ? checkRayMask << 8 : checkRayMask >>> 8;
            eplTargetBit = lPawns & epTarget & shiftedCheckMask;
            eprTargetBit = rPawns & epTarget & shiftedCheckMask;
        }

        lPawns &= enemy;
        rPawns &= enemy;

        addMoves(friendlyPawn, null, lPawns & notPromotion, MoveList.Status.PAWN_TAKE_LEFT);
        addMoves(friendlyPawn, null, rPawns & notPromotion, MoveList.Status.PAWN_TAKE_RIGHT);
        addMoves(friendlyPawn, null, fPawns & notPromotion, MoveList.Status.PAWN_FORWARD);
        addMoves(friendlyPawn, null, pPawns, MoveList.Status.PAWN_PUSH);
        addMoves(friendlyPawn, null, lPawns & promotion, MoveList.Status.PAWN_PROMOTE_LEFT);
        addMoves(friendlyPawn, null, rPawns & promotion, MoveList.Status.PAWN_PROMOTE_RIGHT);
        addMoves(friendlyPawn, null, fPawns & promotion, MoveList.Status.PAWN_PROMOTE);
        addMoves(friendlyPawn, null, eplTargetBit, MoveList.Status.EN_PASSANT_LEFT);
        addMoves(friendlyPawn, null, eprTargetBit, MoveList.Status.EN_PASSANT_RIGHT);
    }

    private long pruneStraight(long mask) {
        long pinned = mask & hvPinRayMap;
        long unpinned = mask ^ pinned;

        if (friendlyColor == WHITE) pinned &= (hvPinRayMap & ~ROW_MASKS[0]) >>> 8;
        else pinned &= ((hvPinRayMap & ~FILE_MASKS[7]) << 8);

        return pinned | unpinned;
    }

    private long pruneRight(long mask) {
        long pinned = mask & d12PinRayMap;
        long unpinned = mask ^ pinned;

        if (friendlyColor == WHITE) pinned &= (d12PinRayMap & ~FILE_MASKS[0]) >>> 9;
        else pinned &= (d12PinRayMap & ~FILE_MASKS[0]) << 7;

        return pinned | unpinned;
    }

    private long pruneLeft(long mask) {
        long pinned = mask & d12PinRayMap;
        long unpinned = mask ^ pinned;

        if (friendlyColor == WHITE) pinned &= (d12PinRayMap & ~FILE_MASKS[7]) >>> 7;
        else pinned &= (d12PinRayMap & ~FILE_MASKS[7]) << 9;

        return pinned | unpinned;
    }

    public long getOpponentAttackMap() {
        return opponentAttackMap;
    }

    private static long[][] createDirectionMaskMap() {
        long[][] coordinateToMask = new long[Long.BYTES * BITS_IN_BYTE][ALL_DIRECTIONS.directions.size()];

        for (int coordIdx = 0; coordIdx < coordinateToMask.length; coordIdx++) {
            for (Direction direction : ALL_DIRECTIONS.directions) {
                long mask = 0x0;
                ChessCoordinate coordinate = direction.next(ChessCoordinate.getChessCoordinate(coordIdx));

                while (coordinate != null) {
                    mask |= coordinate.getBitMask();
                    coordinate = direction.next(coordinate);
                }

                if (Long.bitCount(mask) > 7) throw new IllegalStateException();
                coordinateToMask[coordIdx][direction.ordinal()] = mask;
            }
        }

        return coordinateToMask;
    }

    private static long[] createKnightMoveMasks() {
        long[] knightMoveMask = new long[64];

        for (int coordIdx = 0; coordIdx < knightMoveMask.length; coordIdx++) {
            ChessCoordinate coordinate = getChessCoordinate(coordIdx);
            long mask = 0x0;

            if (coordinate.getFile() > 0 && coordinate.getRank() > 1) mask |= (coordinate.getBitMask() >>> 17);
            if (coordinate.getFile() < 7 && coordinate.getRank() > 1) mask |= (coordinate.getBitMask() >>> 15);
            if (coordinate.getFile() > 1 && coordinate.getRank() > 0) mask |= (coordinate.getBitMask() >>> 10);
            if (coordinate.getFile() < 6 && coordinate.getRank() > 0) mask |= (coordinate.getBitMask() >>> 6);
            if (coordinate.getFile() > 1 && coordinate.getRank() < 7) mask |= (coordinate.getBitMask() << 6);
            if (coordinate.getFile() < 6 && coordinate.getRank() < 7) mask |= (coordinate.getBitMask() << 10);
            if (coordinate.getFile() > 0 && coordinate.getRank() < 6) mask |= (coordinate.getBitMask() << 15);
            if (coordinate.getFile() < 7 && coordinate.getRank() < 6) mask |= (coordinate.getBitMask() << 17);

            knightMoveMask[coordIdx] = mask;
        }

        return knightMoveMask;
    }

    private static long[] createKingMoveMasks() {
        long[] kingMoveMasks = new long[64];

        for (int coordIdx = 0; coordIdx < kingMoveMasks.length; coordIdx++) {
            ChessCoordinate coordinate = getChessCoordinate(coordIdx);
            long mask = 0x0;

            if (coordinate.getFile() > 0) mask |= (coordinate.getBitMask() >>> 1);
            if (coordinate.getFile() > 0 && coordinate.getRank() > 0) mask |= (coordinate.getBitMask()) >>> 9;
            if (coordinate.getFile() > 0 && coordinate.getRank() < 7) mask |= (coordinate.getBitMask()) << 7;
            if (coordinate.getFile() < 7) mask |= (coordinate.getBitMask() << 1);
            if (coordinate.getFile() < 7 && coordinate.getRank() > 0) mask |= (coordinate.getBitMask() >> 7);
            if (coordinate.getFile() < 7 && coordinate.getRank() < 7) mask |= (coordinate.getBitMask() << 9);
            if (coordinate.getRank() > 0) mask |= (coordinate.getBitMask() >>> 8);
            if (coordinate.getRank() < 7) mask |= (coordinate.getBitMask() << 8);

            kingMoveMasks[coordIdx] = mask;
        }

        return kingMoveMasks;
    }

    private enum PinCheckStatus {
        NONE,
        PIN,
        CHECK
    }
}
