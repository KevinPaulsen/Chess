package chess.model;

import chess.ChessCoordinate;
import chess.model.pieces.Direction;
import chess.model.pieces.Piece;
import chess.util.BitIterator;

import static chess.ChessCoordinate.*;
import static chess.model.GameModel.BLACK;
import static chess.model.GameModel.WHITE;
import static chess.model.pieces.Direction.*;
import static chess.model.pieces.Directions.*;
import static chess.model.pieces.Piece.*;

/**
 * This class is responsible for generating moves.
 */
public class MoveGenerator {

    /**
     * Accessed by
     * coordinateToMask[posIdx][direction.ordinal]
     */
    protected static final long[][] SLIDING_ATTACK_MASKS;
    private static final int BITS_IN_BYTE = 8;
    private static final long[] ROOK_MOVE_MASKS;
    private static final long[] BISHOP_MOVE_MASKS;
    private static final long[][] ROOK_TABLE;
    private static final long[][] BISHOP_TABLE;
    private static final long[][] BITS_BETWEEN_MAP;
    private static final long EDGE_SQUARES = 0xFF818181818181FFL;
    private static final long ONES = 0xFFFFFFFFFFFFFFFFL;
    private static final long WHITE_KING_CASTLE_MASK = 0x0000000000000070L;
    private static final long WHITE_QUEEN_CASTLE_MASK = 0x000000000000001EL;
    private static final long BLACK_KING_CASTLE_MASK = 0x7000000000000000L;
    private static final long BLACK_QUEEN_CASTLE_MASK = 0x1E00000000000000L;
    private static final long WHITE_QUEEN_ATTACK_CASTLE_MASK = 0x000000000000001CL;
    private static final long BLACK_QUEEN_ATTACK_CASTLE_MASK = 0x1C00000000000000L;
    private static final long[] FILE_MASKS = {
            0x0101010101010101L, 0x0202020202020202L, 0x0404040404040404L, 0x0808080808080808L,
            0x1010101010101010L, 0x2020202020202020L, 0x4040404040404040L, 0x8080808080808080L,
    };
    private static final long[] ROW_MASKS = {
            0x00000000000000FFL, 0x000000000000FF00L, 0x0000000000FF0000L, 0x00000000FF000000L,
            0x000000FF00000000L, 0x0000FF0000000000L, 0x00FF000000000000L, 0xFF00000000000000L,
    };
    private static final long[] KNIGHT_MOVE_MASKS = createKnightMoveMasks();
    private static final long[] KING_MOVE_MASKS = createKingMoveMasks();
    private static final MagicData[] ROOK_MAGICS = {
            new MagicData(0xa8002c000108020L, 12), new MagicData(0x6c00049b0002001L, 11),
            new MagicData(0x100200010090040L, 11), new MagicData(0x2480041000800801L, 11),
            new MagicData(0x280028004000800L, 11), new MagicData(0x900410008040022L, 11),
            new MagicData(0x280020001001080L, 11), new MagicData(0x2880002041000080L, 12),
            new MagicData(0xa000800080400034L, 11), new MagicData(0x4808020004000L, 10),
            new MagicData(0x2290802004801000L, 10), new MagicData(0x411000d00100020L, 10),
            new MagicData(0x402800800040080L, 10), new MagicData(0xb000401004208L, 10),
            new MagicData(0x2409000100040200L, 10), new MagicData(0x1002100004082L, 11),
            new MagicData(0x22878001e24000L, 11), new MagicData(0x1090810021004010L, 10),
            new MagicData(0x801030040200012L, 10), new MagicData(0x500808008001000L, 10),
            new MagicData(0xa08018014000880L, 10), new MagicData(0x8000808004000200L, 10),
            new MagicData(0x201008080010200L, 10), new MagicData(0x801020000441091L, 11),
            new MagicData(0x800080204005L, 11), new MagicData(0x1040200040100048L, 10),
            new MagicData(0x120200402082L, 10), new MagicData(0xd14880480100080L, 10),
            new MagicData(0x12040280080080L, 10), new MagicData(0x100040080020080L, 10),
            new MagicData(0x9020010080800200L, 10), new MagicData(0x813241200148449L, 11),
            new MagicData(0x491604001800080L, 11), new MagicData(0x100401000402001L, 10),
            new MagicData(0x4820010021001040L, 10), new MagicData(0x400402202000812L, 10),
            new MagicData(0x209009005000802L, 10), new MagicData(0x810800601800400L, 10),
            new MagicData(0x4301083214000150L, 10), new MagicData(0x204026458e001401L, 11),
            new MagicData(0x40204000808000L, 11), new MagicData(0x8001008040010020L, 10),
            new MagicData(0x8410820820420010L, 10), new MagicData(0x1003001000090020L, 10),
            new MagicData(0x804040008008080L, 10), new MagicData(0x12000810020004L, 10),
            new MagicData(0x1000100200040208L, 10), new MagicData(0x430000a044020001L, 11),
            new MagicData(0x280009023410300L, 11), new MagicData(0xe0100040002240L, 10),
            new MagicData(0x200100401700L, 10), new MagicData(0x2244100408008080L, 10),
            new MagicData(0x8000400801980L, 10), new MagicData(0x2000810040200L, 10),
            new MagicData(0x8010100228810400L, 10), new MagicData(0x2000009044210200L, 11),
            new MagicData(0x4080008040102101L, 12), new MagicData(0x40002080411d01L, 11),
            new MagicData(0x2005524060000901L, 11), new MagicData(0x502001008400422L, 11),
            new MagicData(0x489a000810200402L, 11), new MagicData(0x1004400080a13L, 11),
            new MagicData(0x4000011008020084L, 11), new MagicData(0x26002114058042L, 12),
    };
    private static final MagicData[] BISHOP_MAGICS = {
            new MagicData(0x89a1121896040240L, 6), new MagicData(0x2004844802002010L, 5),
            new MagicData(0x2068080051921000L, 5), new MagicData(0x62880a0220200808L, 5),
            new MagicData(0x4042004000000L, 5), new MagicData(0x100822020200011L, 5),
            new MagicData(0xc00444222012000aL, 5), new MagicData(0x28808801216001L, 6),
            new MagicData(0x400492088408100L, 5), new MagicData(0x201c401040c0084L, 5),
            new MagicData(0x840800910a0010L, 5), new MagicData(0x82080240060L, 5),
            new MagicData(0x2000840504006000L, 5), new MagicData(0x30010c4108405004L, 5),
            new MagicData(0x1008005410080802L, 5), new MagicData(0x8144042209100900L, 5),
            new MagicData(0x208081020014400L, 5), new MagicData(0x4800201208ca00L, 5),
            new MagicData(0xf18140408012008L, 7), new MagicData(0x1004002802102001L, 7),
            new MagicData(0x841000820080811L, 7), new MagicData(0x40200200a42008L, 7),
            new MagicData(0x800054042000L, 5), new MagicData(0x88010400410c9000L, 5),
            new MagicData(0x520040470104290L, 5), new MagicData(0x1004040051500081L, 5),
            new MagicData(0x2002081833080021L, 7), new MagicData(0x400c00c010142L, 9),
            new MagicData(0x941408200c002000L, 9), new MagicData(0x658810000806011L, 7),
            new MagicData(0x188071040440a00L, 5), new MagicData(0x4800404002011c00L, 5),
            new MagicData(0x104442040404200L, 5), new MagicData(0x511080202091021L, 5),
            new MagicData(0x4022401120400L, 7), new MagicData(0x80c0040400080120L, 9),
            new MagicData(0x8040010040820802L, 9), new MagicData(0x480810700020090L, 7),
            new MagicData(0x102008e00040242L, 5), new MagicData(0x809005202050100L, 5),
            new MagicData(0x8002024220104080L, 5), new MagicData(0x431008804142000L, 5),
            new MagicData(0x19001802081400L, 7), new MagicData(0x200014208040080L, 7),
            new MagicData(0x3308082008200100L, 7), new MagicData(0x41010500040c020L, 7),
            new MagicData(0x4012020c04210308L, 5), new MagicData(0x208220a202004080L, 5),
            new MagicData(0x111040120082000L, 5), new MagicData(0x6803040141280a00L, 5),
            new MagicData(0x2101004202410000L, 5), new MagicData(0x8200000041108022L, 5),
            new MagicData(0x21082088000L, 5), new MagicData(0x2410204010040L, 5),
            new MagicData(0x40100400809000L, 5), new MagicData(0x822088220820214L, 5),
            new MagicData(0x40808090012004L, 6), new MagicData(0x910224040218c9L, 5),
            new MagicData(0x402814422015008L, 5), new MagicData(0x90014004842410L, 5),
            new MagicData(0x1000042304105L, 5), new MagicData(0x10008830412a00L, 5),
            new MagicData(0x2520081090008908L, 5), new MagicData(0x40102000a0a60140L, 6),
    };

    static {
        SLIDING_ATTACK_MASKS = createDirectionMaskMap();
        ROOK_MOVE_MASKS = createRookMasks();
        BISHOP_MOVE_MASKS = createBishopMasks();
        ROOK_TABLE = createTable(STRAIGHT_COMPLEMENTS, ROOK_MOVE_MASKS, ROOK_MAGICS, 4096);
        BISHOP_TABLE = createTable(DIAGONAL_COMPLEMENTS, BISHOP_MOVE_MASKS, BISHOP_MAGICS, 1048);

        BITS_BETWEEN_MAP = createBitsBetweenMap();
    }

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

    private static long[][] createBitsBetweenMap() {
        long[][] bitBetweenMap =
                new long[ChessCoordinate.values().length][ChessCoordinate.values().length];

        for (int firstSquare = 0; firstSquare < bitBetweenMap.length; firstSquare++) {
            for (int secondSquare = firstSquare; secondSquare < bitBetweenMap.length;
                 secondSquare++) {
                /*long bitsBetween = getBitsBetweenInclusiveSLOW(firstSquare, secondSquare);

                bitBetweenMap[firstSquare][secondSquare] = bitsBetween;
                bitBetweenMap[secondSquare][firstSquare] = bitsBetween;//*/
            }
        }

        return bitBetweenMap;
    }

    private static long[] createRookMasks() {
        long[] rookMasks = new long[ChessCoordinate.values().length];
        for (ChessCoordinate coordinate : ChessCoordinate.values()) {
            int square = coordinate.getOndDimIndex();
            rookMasks[square] = SLIDING_ATTACK_MASKS[square][UP.ordinal()] & ~ROW_MASKS[7] |
                    SLIDING_ATTACK_MASKS[square][DOWN.ordinal()] & ~ROW_MASKS[0] |
                    SLIDING_ATTACK_MASKS[square][LEFT.ordinal()] & ~FILE_MASKS[0] |
                    SLIDING_ATTACK_MASKS[square][RIGHT.ordinal()] & ~FILE_MASKS[7];
        }
        return rookMasks;
    }

    private static long[] createBishopMasks() {
        long[] moveMasks = new long[ChessCoordinate.values().length];

        for (ChessCoordinate coordinate : ChessCoordinate.values()) {
            int square = coordinate.getOndDimIndex();
            long[] SLIDING_DIRECTIONS = SLIDING_ATTACK_MASKS[square];
            for (Direction direction : DIAGONALS) {
                moveMasks[square] |= SLIDING_DIRECTIONS[direction.ordinal()] & ~EDGE_SQUARES;
            }
        }

        return moveMasks;
    }

    private static long[][] createTable(Direction[] directions, long[] moveMasks,
                                        MagicData[] magics, int maxSize) {
        long[][] moveTable = new long[ChessCoordinate.values().length][maxSize];

        // For all squares
        for (ChessCoordinate coordinate : ChessCoordinate.values()) {
            int square = coordinate.getOndDimIndex();

            // For all possible blockers for this square
            for (int blockerIndex = 0; blockerIndex < magics[square].numCombinations();
                 blockerIndex++) {
                long blockers = getBlockersFromIndex(blockerIndex, moveMasks[square]);

                moveTable[square][magics[square].getIndex(blockers)] =
                        getAttacksSlow(coordinate, blockers, directions);
            }
        }
        return moveTable;
    }

    private static long getBlockersFromIndex(int index, long mask) {
        long blockers = 0L;
        int bits = Long.bitCount(mask);
        for (int i = 0; i < bits; i++) {
            int bitPos = lowestSetBit(mask);
            mask = mask ^ Long.lowestOneBit(mask);

            if ((index & (1 << i)) != 0) {
                blockers |= 1L << bitPos;
            }
        }
        return blockers;
    }

    private static long getAttacksSlow(ChessCoordinate coordinate, long blockers,
                                       Direction[] directionComplements) {
        long bishopAttacks = 0L;
        for (Direction direction : directionComplements) {
            long upperMask = SLIDING_ATTACK_MASKS[coordinate.getOndDimIndex()][direction.ordinal()];
            long lowerMask =
                    SLIDING_ATTACK_MASKS[coordinate.getOndDimIndex()][direction.complement()
                            .ordinal()];

            bishopAttacks |= getMoveMask(lowerMask, upperMask, blockers);
        }
        return bishopAttacks;
    }

    private static int lowestSetBit(long mask) {
        return Long.numberOfTrailingZeros(mask);
    }

    private static long getMoveMask(long lowerMask, long upperMask, long board) {
        long lower = board & lowerMask;
        long upper = board & upperMask;

        return getBitsBetweenInclusiveSLOW(lower, upper, upperMask | lowerMask);
    }

    private static long getBitsBetweenInclusiveSLOW(long lower, long upper, long mask) {
        long rightBits = Long.lowestOneBit(upper << 1) - 1;
        long leftBits;
        if (lower == 0) {
            leftBits = ONES;
        } else {
            leftBits = -Long.highestOneBit(lower);
        }
        return rightBits & leftBits & mask;
    }

    private static long getBitsBetween(long lower, long upper) {
        return BITS_BETWEEN_MAP[ChessCoordinate.getChessCoordinate(lower)
                .getOndDimIndex()][ChessCoordinate.getChessCoordinate(upper).getOndDimIndex()];
    }

    private static long[][] createDirectionMaskMap() {
        long[][] coordinateToMask = new long[Long.BYTES * BITS_IN_BYTE][ALL_DIRECTIONS.length];

        for (int coordIdx = 0; coordIdx < coordinateToMask.length; coordIdx++) {
            for (Direction direction : ALL_DIRECTIONS) {
                long mask = 0x0;
                ChessCoordinate coordinate =
                        direction.next(ChessCoordinate.getChessCoordinate(coordIdx));

                while (coordinate != null) {
                    mask |= coordinate.getBitMask();
                    coordinate = direction.next(coordinate);
                }

                if (Long.bitCount(mask) > 7)
                    throw new IllegalStateException();
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

            if (coordinate.getFile() > 0 && coordinate.getRank() > 1)
                mask |= (coordinate.getBitMask() >>> 17);
            if (coordinate.getFile() < 7 && coordinate.getRank() > 1)
                mask |= (coordinate.getBitMask() >>> 15);
            if (coordinate.getFile() > 1 && coordinate.getRank() > 0)
                mask |= (coordinate.getBitMask() >>> 10);
            if (coordinate.getFile() < 6 && coordinate.getRank() > 0)
                mask |= (coordinate.getBitMask() >>> 6);
            if (coordinate.getFile() > 1 && coordinate.getRank() < 7)
                mask |= (coordinate.getBitMask() << 6);
            if (coordinate.getFile() < 6 && coordinate.getRank() < 7)
                mask |= (coordinate.getBitMask() << 10);
            if (coordinate.getFile() > 0 && coordinate.getRank() < 6)
                mask |= (coordinate.getBitMask() << 15);
            if (coordinate.getFile() < 7 && coordinate.getRank() < 6)
                mask |= (coordinate.getBitMask() << 17);

            knightMoveMask[coordIdx] = mask;
        }

        return knightMoveMask;
    }

    private static long[] createKingMoveMasks() {
        long[] kingMoveMasks = new long[64];

        for (int coordIdx = 0; coordIdx < kingMoveMasks.length; coordIdx++) {
            ChessCoordinate coordinate = getChessCoordinate(coordIdx);
            long mask = 0x0;

            if (coordinate.getFile() > 0)
                mask |= (coordinate.getBitMask() >>> 1);
            if (coordinate.getFile() > 0 && coordinate.getRank() > 0)
                mask |= (coordinate.getBitMask()) >>> 9;
            if (coordinate.getFile() > 0 && coordinate.getRank() < 7)
                mask |= (coordinate.getBitMask()) << 7;
            if (coordinate.getFile() < 7)
                mask |= (coordinate.getBitMask() << 1);
            if (coordinate.getFile() < 7 && coordinate.getRank() > 0)
                mask |= (coordinate.getBitMask() >> 7);
            if (coordinate.getFile() < 7 && coordinate.getRank() < 7)
                mask |= (coordinate.getBitMask() << 9);
            if (coordinate.getRank() > 0)
                mask |= (coordinate.getBitMask() >>> 8);
            if (coordinate.getRank() < 7)
                mask |= (coordinate.getBitMask() << 8);

            kingMoveMasks[coordIdx] = mask;
        }

        return kingMoveMasks;
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

        if (friendlyKingCoord != null) {
            calculateAttackData();
            generateKingMoves();
        }

        if (!inDoubleCheck) {
            long queens = board.getPieceMap(friendlyQueen);
            long rooks = board.getPieceMap(friendlyRook);
            long bishops = board.getPieceMap(friendlyBishop);
            generateRookAndBishopMoves((queens | rooks) & ~d12PinRayMap, queens, hvPinRayMap,
                    friendlyRook, STRAIGHT_COMPLEMENTS);
            generateRookAndBishopMoves((queens | bishops) & ~hvPinRayMap, queens, d12PinRayMap,
                    friendlyBishop, DIAGONAL_COMPLEMENTS);
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

        if (checkRayMask == 0)
            checkRayMask = ~checkRayMask;
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
            long attackingPieceBit =
                    ROW_MASKS[friendlyKingCoord.getRank() + ((friendlyColor == WHITE) ? 1 : -1)] &
                            ((kingFile > 0 ? FILE_MASKS[friendlyKingCoord.getFile() - 1] : 0) |
                                    (kingFile < 7 ? FILE_MASKS[friendlyKingCoord.getFile() + 1] :
                                            0));
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

            long rookAndQueen =
                    board.getPieceMap(attackingRook) | board.getPieceMap(attackingQueen);
            if (!((epRank & friendlyKingMask) == 0 || (epRank & rookAndQueen) == 0 ||
                    (epRank & pawns) == 0)) {
                long upperMask =
                        SLIDING_ATTACK_MASKS[friendlyKingCoord.getOndDimIndex()][RIGHT.ordinal()];
                long lowerMask =
                        SLIDING_ATTACK_MASKS[friendlyKingCoord.getOndDimIndex()][LEFT.ordinal()];
                if (eplPawn != 0) {
                    long afterEP = board.getOccupancyMap() ^ eplPawn ^ epTargetPawn;
                    long moveMask = getMoveMask(lowerMask, upperMask, afterEP);
                    if ((moveMask & rookAndQueen) != 0)
                        epTarget = 0;
                }
                if (eprPawn != 0) {
                    long afterEP = board.getOccupancyMap() ^ eprPawn ^ epTargetPawn;
                    long moveMask = getMoveMask(lowerMask, upperMask, afterEP);
                    if ((moveMask & rookAndQueen) != 0)
                        epTarget = 0;
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

        BitIterator bitIterator = new BitIterator(board.getPieceMap(attackingRook) | queens);
        slidingAttackMap |= getSlidingAttackMap(friendlyKingBit, boardWithoutKing, bitIterator,
                STRAIGHT_COMPLEMENTS);
        //getRookAttackMap(bitIterator, boardWithoutKing);

        bitIterator = new BitIterator(board.getPieceMap(attackingBishop) | queens);
        slidingAttackMap |= getSlidingAttackMap(friendlyKingBit, boardWithoutKing, bitIterator,
                DIAGONAL_COMPLEMENTS);

        opponentAttackMap |= slidingAttackMap;
    }

    private void getRookAttackMap(BitIterator rooksAndQueens, long boardWithoutKing,
                                  long friendlyKing) {
        while (rooksAndQueens.hasNext()) {
            ChessCoordinate rook = rooksAndQueens.next();
            int square = rook.getOndDimIndex();

            // Get all squares piece is attacking
            long attackingSquares = ROOK_TABLE[square][ROOK_MAGICS[square].getIndex(
                    boardWithoutKing & ROOK_MOVE_MASKS[square])];

            if ((attackingSquares & friendlyKing) == 0) {
                // If there is no check, look for pins
                boardWithoutKing &= ~attackingSquares;

                long xRaySquares = ROOK_TABLE[square][ROOK_MAGICS[square].getIndex(
                        boardWithoutKing & ROOK_MOVE_MASKS[square])];

                if ((xRaySquares & friendlyKing) != 0) {
                    // TODO make constant time
                    long lower = square;
                    long upper = friendlyKing;
                    if (Long.compareUnsigned(lower, upper) == 1) {
                        long temp = lower;
                        lower = upper;
                        upper = temp;
                    }
                    //hvPinRayMap |= getBitsBetweenInclusiveSLOW(lower, upper, );
                }
            }
        }
    }

    private long getSlidingAttackMap(long friendlyKingBit, long board, BitIterator bitIterator,
                                     Direction[] directions) {
        long slidingAttackMap = 0x0L;
        while (bitIterator.hasNext()) {
            ChessCoordinate attackingCoordinate = bitIterator.next();
            long ray = generateSlidingPieceMoves(directions, attackingCoordinate, board,
                    PinCheckStatus.CHECK);
            slidingAttackMap |= ray;

            if ((ray & friendlyKingBit) == 0) {
                generateSlidingPieceMoves(directions, attackingCoordinate, board & ~ray,
                        PinCheckStatus.PIN);
            }
        }
        return slidingAttackMap;
    }

    private void generateKingMoves() {
        Piece movingKing = friendlyColor == WHITE ? WHITE_KING : BLACK_KING;

        // Add moves for the regular king moves
        long kingMoveMask = KING_MOVE_MASKS[friendlyKingCoord.getOndDimIndex()] &
                ~(board.getOccupancyMap(friendlyColor) | opponentAttackMap);
        addMoves(movingKing, friendlyKingCoord, kingMoveMask, MoveList.Status.NORMAL);

        // Add castling moves
        kingMoveMask = 0x0L;
        long occupancy = (board.getOccupancyMap() ^ friendlyKingCoord.getBitMask());
        if (friendlyColor == WHITE) {
            if (game.canKingSideCastle(WHITE) && (occupancy & WHITE_KING_CASTLE_MASK) == 0 &&
                    (opponentAttackMap & WHITE_KING_CASTLE_MASK) == 0)
                kingMoveMask |= G1.getBitMask();
            if (game.canQueenSideCastle(WHITE) && (occupancy & WHITE_QUEEN_CASTLE_MASK) == 0 &&
                    (opponentAttackMap & WHITE_QUEEN_ATTACK_CASTLE_MASK) == 0)
                kingMoveMask |= C1.getBitMask();
        } else {
            if (game.canKingSideCastle(BLACK) && (occupancy & BLACK_KING_CASTLE_MASK) == 0 &&
                    ((opponentAttackMap & BLACK_KING_CASTLE_MASK) == 0))
                kingMoveMask |= G8.getBitMask();
            if (game.canQueenSideCastle(BLACK) && (occupancy & BLACK_QUEEN_CASTLE_MASK) == 0 &&
                    ((opponentAttackMap & BLACK_QUEEN_ATTACK_CASTLE_MASK) == 0))
                kingMoveMask |= C8.getBitMask();
        }

        addMoves(movingKing, friendlyKingCoord, kingMoveMask, MoveList.Status.CASTLING);//*/
    }

    private void generateRookAndBishopMoves(long slidingPieceMask, long queenMask, long pinMask,
                                            Piece friendlyPiece, Direction[] directions) {
        long pinnedPieces = slidingPieceMask & pinMask;
        long unpinnedPieces = slidingPieceMask & ~pinMask;

        BitIterator bitIterator = new BitIterator(pinnedPieces);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();

            long legalMoveMap = pinMask & checkRayMask & ~board.getOccupancyMap(friendlyColor);
            int square = coordinate.getOndDimIndex();
            if (directions == STRAIGHT_COMPLEMENTS) {
                legalMoveMap &= ROOK_TABLE[square][ROOK_MAGICS[square].getIndex(
                        board.getOccupancyMap() & ROOK_MOVE_MASKS[square])];
            } else {
                legalMoveMap &= BISHOP_TABLE[square][BISHOP_MAGICS[square].getIndex(
                        board.getOccupancyMap() & BISHOP_MOVE_MASKS[square])];
            }

            if ((coordinate.getBitMask() & queenMask) != 0) {
                addMoves(friendlyQueen, coordinate, legalMoveMap, MoveList.Status.NORMAL);
            } else {
                addMoves(friendlyPiece, coordinate, legalMoveMap, MoveList.Status.NORMAL);
            }
        }

        bitIterator = new BitIterator(unpinnedPieces);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();

            long legalMoveMap = checkRayMask & ~board.getOccupancyMap(friendlyColor);
            int square = coordinate.getOndDimIndex();
            if (directions == STRAIGHT_COMPLEMENTS) {
                legalMoveMap &= ROOK_TABLE[square][ROOK_MAGICS[square].getIndex(
                        board.getOccupancyMap() & ROOK_MOVE_MASKS[square])];
            } else {
                legalMoveMap &= BISHOP_TABLE[square][BISHOP_MAGICS[square].getIndex(
                        board.getOccupancyMap() & BISHOP_MOVE_MASKS[square])];
            }

            if ((coordinate.getBitMask() & queenMask) != 0) {
                addMoves(friendlyQueen, coordinate, legalMoveMap, MoveList.Status.NORMAL);
            } else {
                addMoves(friendlyPiece, coordinate, legalMoveMap, MoveList.Status.NORMAL);
            }
        }
    }

    private long generateSlidingPieceMoves(Direction[] directions, ChessCoordinate coordinate,
                                           long board, PinCheckStatus status) {
        long moveMask = 0x0L;
        for (int index = 0; index < directions.length; index++) {
            Direction direction = directions[index];
            long upperMask = SLIDING_ATTACK_MASKS[coordinate.getOndDimIndex()][direction.ordinal()];
            long lowerMask =
                    SLIDING_ATTACK_MASKS[coordinate.getOndDimIndex()][direction.complement()
                            .ordinal()];
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
                            d12PinRayMap |= getBitsBetweenInclusiveSLOW(lower, upper,
                                    ray | coordinate.getBitMask());
                        } else {
                            hvPinRayMap |= getBitsBetweenInclusiveSLOW(lower, upper,
                                    ray | coordinate.getBitMask());
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
                        checkRayMask |= getBitsBetweenInclusiveSLOW(lower, upper,
                                ray | coordinate.getBitMask());
                        inDoubleCheck = inCheck;
                        inCheck = true;
                    }
                }
            }
        }
        return moveMask;
    }

    private void addMoves(Piece piece, ChessCoordinate startingCoordinate, long moveMask,
                          MoveList.Status status) {
        if (moveMask == 0)
            return;
        moves.add(piece, startingCoordinate, moveMask, status);
    }

    private void generateKnightMoves() {
        long knights = board.getPieceMap(friendlyKnight) & ~(hvPinRayMap | d12PinRayMap);
        BitIterator bitIterator = new BitIterator(knights);
        while (bitIterator.hasNext()) {
            ChessCoordinate coordinate = bitIterator.next();
            long moveMask = KNIGHT_MOVE_MASKS[coordinate.getOndDimIndex()] &
                    ~board.getOccupancyMap(friendlyColor) & checkRayMask;
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

        if (friendlyColor == WHITE)
            pinned &= (hvPinRayMap & ~ROW_MASKS[0]) >>> 8;
        else
            pinned &= ((hvPinRayMap & ~FILE_MASKS[7]) << 8);

        return pinned | unpinned;
    }

    private long pruneRight(long mask) {
        long pinned = mask & d12PinRayMap;
        long unpinned = mask ^ pinned;

        if (friendlyColor == WHITE)
            pinned &= (d12PinRayMap & ~FILE_MASKS[0]) >>> 9;
        else
            pinned &= (d12PinRayMap & ~FILE_MASKS[0]) << 7;

        return pinned | unpinned;
    }

    private long pruneLeft(long mask) {
        long pinned = mask & d12PinRayMap;
        long unpinned = mask ^ pinned;

        if (friendlyColor == WHITE)
            pinned &= (d12PinRayMap & ~FILE_MASKS[7]) >>> 7;
        else
            pinned &= (d12PinRayMap & ~FILE_MASKS[7]) << 9;

        return pinned | unpinned;
    }

    public long getOpponentAttackMap() {
        return opponentAttackMap;
    }

    private enum PinCheckStatus {
        NONE, PIN, CHECK
    }

    private record MagicData(long magicNumber, int indexBits) {

        private int numCombinations() {
            return 1 << indexBits;
        }

        private int getIndex(long blockers) {
            return (int) ((blockers * magicNumber) >>> (64 - indexBits));
        }
    }
}
