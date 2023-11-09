package chess.model;

import chess.ChessCoordinate;
import chess.model.pieces.Direction;
import chess.model.pieces.Directions;
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
    private static final MagicData MAGIC_COORD;
    private static final long WHITE_KING_CASTLE_MASK = 0x0000000000000070L;
    private static final long WHITE_QUEEN_CASTLE_MASK = 0x000000000000001EL;
    private static final long BLACK_KING_CASTLE_MASK = 0x7000000000000000L;
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
    private static final long[] KNIGHT_MOVE_MASKS = createKnightMoveMasks();
    private static final long[] KING_MOVE_MASKS = createKingMoveMasks();
    private static final MagicData[] ROOK_MAGICS = {
            new MagicData(0xa8002c000108020L, 12),
            new MagicData(0x6c00049b0002001L, 11),
            new MagicData(0x100200010090040L, 11),
            new MagicData(0x2480041000800801L, 11),
            new MagicData(0x280028004000800L, 11),
            new MagicData(0x900410008040022L, 11),
            new MagicData(0x280020001001080L, 11),
            new MagicData(0x2880002041000080L, 12),
            new MagicData(0xa000800080400034L, 11),
            new MagicData(0x4808020004000L, 10),
            new MagicData(0x2290802004801000L, 10),
            new MagicData(0x411000d00100020L, 10),
            new MagicData(0x402800800040080L, 10),
            new MagicData(0xb000401004208L, 10),
            new MagicData(0x2409000100040200L, 10),
            new MagicData(0x1002100004082L, 11),
            new MagicData(0x22878001e24000L, 11),
            new MagicData(0x1090810021004010L, 10),
            new MagicData(0x801030040200012L, 10),
            new MagicData(0x500808008001000L, 10),
            new MagicData(0xa08018014000880L, 10),
            new MagicData(0x8000808004000200L, 10),
            new MagicData(0x201008080010200L, 10),
            new MagicData(0x801020000441091L, 11),
            new MagicData(0x800080204005L, 11),
            new MagicData(0x1040200040100048L, 10),
            new MagicData(0x120200402082L, 10),
            new MagicData(0xd14880480100080L, 10),
            new MagicData(0x12040280080080L, 10),
            new MagicData(0x100040080020080L, 10),
            new MagicData(0x9020010080800200L, 10),
            new MagicData(0x813241200148449L, 11),
            new MagicData(0x491604001800080L, 11),
            new MagicData(0x100401000402001L, 10),
            new MagicData(0x4820010021001040L, 10),
            new MagicData(0x400402202000812L, 10),
            new MagicData(0x209009005000802L, 10),
            new MagicData(0x810800601800400L, 10),
            new MagicData(0x4301083214000150L, 10),
            new MagicData(0x204026458e001401L, 11),
            new MagicData(0x40204000808000L, 11),
            new MagicData(0x8001008040010020L, 10),
            new MagicData(0x8410820820420010L, 10),
            new MagicData(0x1003001000090020L, 10),
            new MagicData(0x804040008008080L, 10),
            new MagicData(0x12000810020004L, 10),
            new MagicData(0x1000100200040208L, 10),
            new MagicData(0x430000a044020001L, 11),
            new MagicData(0x280009023410300L, 11),
            new MagicData(0xe0100040002240L, 10),
            new MagicData(0x200100401700L, 10),
            new MagicData(0x2244100408008080L, 10),
            new MagicData(0x8000400801980L, 10),
            new MagicData(0x2000810040200L, 10),
            new MagicData(0x8010100228810400L, 10),
            new MagicData(0x2000009044210200L, 11),
            new MagicData(0x4080008040102101L, 12),
            new MagicData(0x40002080411d01L, 11),
            new MagicData(0x2005524060000901L, 11),
            new MagicData(0x502001008400422L, 11),
            new MagicData(0x489a000810200402L, 11),
            new MagicData(0x1004400080a13L, 11),
            new MagicData(0x4000011008020084L, 11),
            new MagicData(0x26002114058042L, 12),
    };
    private static final MagicData[] BISHOP_MAGICS = {
            new MagicData(0x89a1121896040240L, 6),
            new MagicData(0x2004844802002010L, 5),
            new MagicData(0x2068080051921000L, 5),
            new MagicData(0x62880a0220200808L, 5),
            new MagicData(0x4042004000000L, 5),
            new MagicData(0x100822020200011L, 5),
            new MagicData(0xc00444222012000aL, 5),
            new MagicData(0x28808801216001L, 6),
            new MagicData(0x400492088408100L, 5),
            new MagicData(0x201c401040c0084L, 5),
            new MagicData(0x840800910a0010L, 5),
            new MagicData(0x82080240060L, 5),
            new MagicData(0x2000840504006000L, 5),
            new MagicData(0x30010c4108405004L, 5),
            new MagicData(0x1008005410080802L, 5),
            new MagicData(0x8144042209100900L, 5),
            new MagicData(0x208081020014400L, 5),
            new MagicData(0x4800201208ca00L, 5),
            new MagicData(0xf18140408012008L, 7),
            new MagicData(0x1004002802102001L, 7),
            new MagicData(0x841000820080811L, 7),
            new MagicData(0x40200200a42008L, 7),
            new MagicData(0x800054042000L, 5),
            new MagicData(0x88010400410c9000L, 5),
            new MagicData(0x520040470104290L, 5),
            new MagicData(0x1004040051500081L, 5),
            new MagicData(0x2002081833080021L, 7),
            new MagicData(0x400c00c010142L, 9),
            new MagicData(0x941408200c002000L, 9),
            new MagicData(0x658810000806011L, 7),
            new MagicData(0x188071040440a00L, 5),
            new MagicData(0x4800404002011c00L, 5),
            new MagicData(0x104442040404200L, 5),
            new MagicData(0x511080202091021L, 5),
            new MagicData(0x4022401120400L, 7),
            new MagicData(0x80c0040400080120L, 9),
            new MagicData(0x8040010040820802L, 9),
            new MagicData(0x480810700020090L, 7),
            new MagicData(0x102008e00040242L, 5),
            new MagicData(0x809005202050100L, 5),
            new MagicData(0x8002024220104080L, 5),
            new MagicData(0x431008804142000L, 5),
            new MagicData(0x19001802081400L, 7),
            new MagicData(0x200014208040080L, 7),
            new MagicData(0x3308082008200100L, 7),
            new MagicData(0x41010500040c020L, 7),
            new MagicData(0x4012020c04210308L, 5),
            new MagicData(0x208220a202004080L, 5),
            new MagicData(0x111040120082000L, 5),
            new MagicData(0x6803040141280a00L, 5),
            new MagicData(0x2101004202410000L, 5),
            new MagicData(0x8200000041108022L, 5),
            new MagicData(0x21082088000L, 5),
            new MagicData(0x2410204010040L, 5),
            new MagicData(0x40100400809000L, 5),
            new MagicData(0x822088220820214L, 5),
            new MagicData(0x40808090012004L, 6),
            new MagicData(0x910224040218c9L, 5),
            new MagicData(0x402814422015008L, 5),
            new MagicData(0x90014004842410L, 5),
            new MagicData(0x1000042304105L, 5),
            new MagicData(0x10008830412a00L, 5),
            new MagicData(0x2520081090008908L, 5),
            new MagicData(0x40102000a0a60140L, 6),
    };

    static {
        MAGIC_COORD = new MagicData(444285591126707838L, 6);
        SLIDING_ATTACK_MASKS = createDirectionMaskMap();
        BITS_BETWEEN_MAP = createBitsBetweenMap();
        ROOK_MOVE_MASKS = createRookMasks();
        BISHOP_MOVE_MASKS = createBishopMasks();
        ROOK_TABLE = createBlockerToMoveMap(STRAIGHT_COMPLEMENTS, ROOK_MOVE_MASKS, ROOK_MAGICS,
                                            4096);
        BISHOP_TABLE = createBlockerToMoveMap(DIAGONAL_COMPLEMENTS, BISHOP_MOVE_MASKS,
                                              BISHOP_MAGICS, 1048);
    }

    private final GameModel game;
    private final BoardModel board;
    private final PieceGroup whitePieceGroup;
    private final PieceGroup blackPieceGroup;
    private long checkRayMask;
    private long hvPinRayMap;
    private long d12PinRayMap;
    private long opponentAttackMap;
    private long epTarget;
    private boolean inCheck;
    private boolean inDoubleCheck;
    private MoveList moves;
    private PieceGroup friendlyPieceGroup;
    private PieceGroup attackingPieceGroup;

    public MoveGenerator(GameModel game) {
        this.game = game;
        this.board = game.getBoard();
        this.whitePieceGroup = new PieceGroup(WHITE);
        this.blackPieceGroup = new PieceGroup(BLACK);
    }

    /**
     * Create map to quickly access the ray between any two coordinates. This is accessed by
     * chess [firstCoordIndex][secondCoordIndex].
     *
     * @return the map mapping every pair of coordinates to the ray between them
     */
    private static long[][] createBitsBetweenMap() {
        long[][] bitBetweenMap =
                new long[ChessCoordinate.values().length][ChessCoordinate.values().length];

        for (int firstSquare = 0; firstSquare < bitBetweenMap.length; firstSquare++) {
            for (int secondSquare = 0; secondSquare < bitBetweenMap.length; secondSquare++) {
                long bitsBetween = getBitsBetweenInclusiveSLOW(firstSquare, secondSquare);

                int firstIndex = MAGIC_COORD.getIndex(1L << firstSquare);
                int secondIndex = MAGIC_COORD.getIndex(1L << secondSquare);

                bitBetweenMap[firstIndex][secondIndex] = bitsBetween;
            }
        }

        return bitBetweenMap;
    }

    /**
     * Get the ray that marks the bits between any two coordinates. This method is not optimized
     * and should only be used to initialize the map. If two coordinates are not horizontally or
     * vertically aligned, then 0 is returned.
     *
     * @param first  the index of the first coordinate (0-63 is expected range)
     * @param second the index of the second coordinate (0-63 is expected range)
     * @return the bit mask of the ray between the two indices
     */
    private static long getBitsBetweenInclusiveSLOW(int first, int second) {
        if (first > second) {
            int temp = first;
            first = second;
            second = temp;
        }

        ChessCoordinate firstCoord = ChessCoordinate.getChessCoordinate(first);
        ChessCoordinate secondCoord = ChessCoordinate.getChessCoordinate(second);

        if (second == first)
            return firstCoord.getBitMask();

        long result = 0;
        if (Directions.areDiagonallyAligned(firstCoord, secondCoord)) {
            Direction direction = firstCoord.getFile() < secondCoord.getFile() ? UP_RIGHT : UP_LEFT;

            do {
                result |= firstCoord.getBitMask();
                firstCoord = direction.next(firstCoord);
            } while (Long.compareUnsigned(result, secondCoord.getBitMask()) < 0);
        } else if (Directions.areStraightlyAligned(firstCoord, secondCoord)) {
            result = ((secondCoord.getBitMask() << 1) - 1) ^ (firstCoord.getBitMask() - 1);

            if (firstCoord.getRank() < secondCoord.getRank()) {
                result &= FILE_MASKS[firstCoord.getFile()];
            }
        }

        return result;
    }

    /**
     * Create blocker masks for rooks on every coordinate. These masks are accessed by a coordinate
     * index (0-63). These masks to not extend to the edge of the board (because use does not
     * require them too, see wiki on magic bit boards for more info).
     *
     * @return the array of rook masks
     */
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

    /**
     * Create blocker masks for bishops on every coordinate. These masks are accessed by a
     * coordinate index (0-63). These masks to not extend to the edge of the board (because use
     * does not require them too, see wiki on magic bit boards for more info).
     *
     * @return the array of bishop masks
     */
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

    /**
     * Creates the map which maps the pair (Coordinate index, blocker array) -> (movement mask).
     * This is used so that we can access the squares which can be moved to in O(1) time.
     *
     * @param directions the directions that can be moved, these should be complements (i.e. LEFT
     *                   and RIGHT should not both be on here.
     * @param moveMasks  the array of move masks that correspond with what is moving
     * @param magics     the array of MagicData that is used for indexing
     * @param maxSize    the maximum size of the array
     * @return the blocker to movement map
     */
    private static long[][] createBlockerToMoveMap(Direction[] directions, long[] moveMasks,
                                                   MagicData[] magics, int maxSize) {
        long[][] moveTable = new long[ChessCoordinate.values().length][maxSize];

        // For all squares
        for (ChessCoordinate coordinate : ChessCoordinate.values()) {
            int square = coordinate.getOndDimIndex();

            // For all possible blockers for this square
            for (int blockerIndex = 0; blockerIndex < magics[square].numCombinations();
                 blockerIndex++) {
                long blockers = getBlockersFromIndex(blockerIndex, moveMasks[square]);

                moveTable[square][magics[square].getIndex(blockers)] = getAttacksSlow(coordinate,
                                                                                      blockers,
                                                                                      directions);
            }
        }
        return moveTable;
    }

    /**
     * Gets the block mask from a given index. Each block mask has some combination of 1s and 0s,
     * this indexes all combinations and makes looping over all of them easy.
     *
     * @param index the index of the blocker you want
     * @param mask  the mask of the bits to index
     * @return the blockers
     */
    private static long getBlockersFromIndex(int index, long mask) {
        long blockers = 0L;
        int bits = Long.bitCount(mask);
        for (int i = 0; i < bits; i++) {
            int bitPos = Long.numberOfTrailingZeros(mask);
            mask = mask ^ Long.lowestOneBit(mask);

            if ((index & (1 << i)) != 0) {
                blockers |= 1L << bitPos;
            }
        }
        return blockers;
    }

    /**
     * Gets all the squares that can be moved to in the given directionComplements, and according
     * to the blockers that are in place. This is slow and should only be used in initialization.
     *
     * @param coordinate           the coordinate that is the piece is moving from
     * @param blockers             the blockers that are in place
     * @param directionComplements the array of direction complements (LEFT and RIGHT should not
     *                             be together)
     * @return the squares that can be seen in the given direction complements (if there is a
     * piece in the way, then the square that piece is on will be the final square in that
     * direction.
     */
    private static long getAttacksSlow(ChessCoordinate coordinate, long blockers,
                                       Direction[] directionComplements) {
        long attacks = 0L;
        for (Direction direction : directionComplements) {
            long upperMask = SLIDING_ATTACK_MASKS[coordinate.getOndDimIndex()][direction.ordinal()];
            long lowerMask =
                    SLIDING_ATTACK_MASKS[coordinate.getOndDimIndex()][direction.complement()
                            .ordinal()];

            attacks |= getMoveMask(lowerMask, upperMask, blockers);
        }
        return attacks;
    }

    /**
     * Gets the move ray between the highest bit in the lower mask, and the lowest bit in the
     * upper mask.
     *
     * @param lowerMask the lower mask
     * @param upperMask the upper mask
     * @param blockers  the board with blockers in the way
     * @return the ray which can be moved to
     */
    private static long getMoveMask(long lowerMask, long upperMask, long blockers) {
        long lower = blockers & lowerMask;
        long upper = blockers & upperMask;

        long result;

        if ((lower | upper) == 0) {
            // No blockers in either direction
            result = lowerMask | upperMask;
        } else if (lower == 0) {
            // Upper has bits, but lower has no blockers
            long upperBit = Long.lowestOneBit(upper);
            long lowerBit = Long.lowestOneBit(upperMask);

            result = getBitsBetweenInclusive(lowerBit, upperBit) | lowerMask;
        } else if (upper == 0) {
            // Lower has bits, but upper has no blockers
            long lowerBit = Long.highestOneBit(lower);
            long upperBit = Long.highestOneBit(lowerMask);

            result = getBitsBetweenInclusive(lowerBit, upperBit) | upperMask;
        } else {
            // Normal case: both have blockers
            long lowerBit = Long.highestOneBit(lower);
            long upperBit = Long.lowestOneBit(upper);

            result = getBitsBetweenInclusive(lowerBit, upperBit) & (lowerMask | upperMask);
        }

        return result;
    }

    /**
     * Gets the ray of the bits between bitOne and bitTwo. bitOne and bitTwo are meant to contain
     * one bit. If either has more bits, then the result is not defined.
     *
     * @param bitOne bit mask of the first coordinate
     * @param bitTwo bit mask of the second coordinate
     * @return the bits between bitOne and bitTwo, 0 if no ray exists.
     */
    private static long getBitsBetweenInclusive(long bitOne, long bitTwo) {
        // 6 +  1
        int firstIndex = (int) ((MAGIC_COORD.magicNumber * bitOne) >>> MAGIC_COORD.shiftBits);

        // 6 + 1
        int secondIndex = (int) ((MAGIC_COORD.magicNumber * bitTwo) >>> MAGIC_COORD.shiftBits);

        // ~100
        return BITS_BETWEEN_MAP[firstIndex][secondIndex];
    }

    /**
     * Create a map that contains the ray from any given coordinate to the edge in every direction.
     * The given ray will be accessed by [coordinateIndex][Direction.ordinal()].
     *
     * @return the mapping from (coordinateIndex, direction) -> (mask of set bits in that direction)
     */
    private static long[][] createDirectionMaskMap() {
        long[][] coordinateToMask = new long[Long.BYTES * BITS_IN_BYTE][ALL_DIRECTIONS.length];

        for (int coordIdx = 0; coordIdx < coordinateToMask.length; coordIdx++) {
            for (Direction direction : ALL_DIRECTIONS) {
                long mask = 0x0;
                ChessCoordinate coordinate = direction.next(
                        ChessCoordinate.getChessCoordinate(coordIdx));

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

    /**
     * Create the move masks for a knight on any given square. This will be accessed by
     * [coordinateIndex].
     *
     * @return the mapping from (coordinateIndex) -> (knight move mask)
     */
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

    /**
     * Create the move masks for a king on any given square. This will be accessed by
     * [coordinateIndex].
     *
     * @return the mapping from (coordinateIndex) -> (king move mask)
     */
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

    public MoveList generateMoves() {
        resetState();

        if (friendlyPieceGroup.kingCoord != null) {
            calculateAttackData();
            generateKingMoves();
        }

        if (!inDoubleCheck) {
            long queens = board.getPieceMap(friendlyPieceGroup.queen);
            long rooks = board.getPieceMap(friendlyPieceGroup.rook);
            long bishops = board.getPieceMap(friendlyPieceGroup.bishop);
            generateRookAndBishopMoves((queens | rooks) & ~d12PinRayMap, queens, hvPinRayMap,
                                       friendlyPieceGroup.rook, ROOK_TABLE, ROOK_MOVE_MASKS,
                                       ROOK_MAGICS);
            generateRookAndBishopMoves((queens | bishops) & ~hvPinRayMap, queens, d12PinRayMap,
                                       friendlyPieceGroup.bishop, BISHOP_TABLE, BISHOP_MOVE_MASKS,
                                       BISHOP_MAGICS);
            generateKnightMoves();
            generatePawnMoves();
        }

        return moves;
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

        if (game.getTurn() == WHITE) {
            friendlyPieceGroup = whitePieceGroup;
            attackingPieceGroup = blackPieceGroup;

            friendlyPieceGroup.kingCoord = board.getWhiteKingCoord();
            attackingPieceGroup.kingCoord = board.getBlackKingCoord();
        } else {
            friendlyPieceGroup = blackPieceGroup;
            attackingPieceGroup = whitePieceGroup;

            friendlyPieceGroup.kingCoord = board.getBlackKingCoord();
            attackingPieceGroup.kingCoord = board.getWhiteKingCoord();
        }
    }

    private void calculateAttackData() {
        // Calculate the pin and check rays, and the sliding attack map
        calculateSlidingAttackMap();

        calculatePinRays();

        long friendlyKingMask = friendlyPieceGroup.kingCoord.getBitMask();

        calculateKnightAttackData(friendlyKingMask);

        calculatePawnAttackData(friendlyKingMask);

        // Calculate King Attacks
        opponentAttackMap |= KING_MOVE_MASKS[attackingPieceGroup.kingCoord.getOndDimIndex()];

        if (checkRayMask == 0)
            checkRayMask = ~checkRayMask;
    }

    private void generateKingMoves() {
        // Add moves for the regular king moves
        long kingMoveMask = KING_MOVE_MASKS[friendlyPieceGroup.kingCoord.getOndDimIndex()] &
                ~(board.getOccupancyMap(friendlyPieceGroup.color) | opponentAttackMap);
        addMoves(friendlyPieceGroup.king, friendlyPieceGroup.kingCoord.getBitMask(), kingMoveMask,
                 MoveList.Status.NORMAL);

        // Add castling moves
        kingMoveMask = 0x0L;
        long occupancy = (board.getOccupancyMap() ^ friendlyPieceGroup.kingCoord.getBitMask());
        if (friendlyPieceGroup.color == WHITE) {
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

        addMoves(friendlyPieceGroup.king, friendlyPieceGroup.kingCoord.getBitMask(), kingMoveMask,
                 MoveList.Status.CASTLING);
    }

    private void generateRookAndBishopMoves(long slidingPieceMask, long queenMask, long pinMask,
                                            Piece friendlyPiece, long[][] table, long[] moveMasks,
                                            MagicData[] magics) {
        long pinnedPieces = slidingPieceMask & pinMask;
        long unpinnedPieces = slidingPieceMask & ~pinMask;

        BitIterator bitIterator = new BitIterator(pinnedPieces);
        while (bitIterator.hasNext()) {
            int square = bitIterator.next();

            long legalMoveMap = pinMask & checkRayMask & ~board.getOccupancyMap(
                    friendlyPieceGroup.color);
            legalMoveMap &= table[square][magics[square].getIndex(
                    board.getOccupancyMap() & moveMasks[square])];

            long squareMask = ChessCoordinate.getBitMask(square);
            if ((squareMask & queenMask) != 0) {
                addMoves(friendlyPieceGroup.queen, squareMask, legalMoveMap,
                         MoveList.Status.NORMAL);
            } else {
                addMoves(friendlyPiece, squareMask, legalMoveMap, MoveList.Status.NORMAL);
            }
        }

        bitIterator = new BitIterator(unpinnedPieces);
        while (bitIterator.hasNext()) {
            int square = bitIterator.next();

            long legalMoveMap = checkRayMask & ~board.getOccupancyMap(friendlyPieceGroup.color);
            legalMoveMap &= table[square][magics[square].getIndex(
                    board.getOccupancyMap() & moveMasks[square])];

            long squareMask = ChessCoordinate.getBitMask(square);
            if ((squareMask & queenMask) != 0) {
                addMoves(friendlyPieceGroup.queen, squareMask, legalMoveMap,
                         MoveList.Status.NORMAL);
            } else {
                addMoves(friendlyPiece, squareMask, legalMoveMap, MoveList.Status.NORMAL);
            }
        }
    }

    private void generateKnightMoves() {
        long knights = board.getPieceMap(friendlyPieceGroup.knight) & ~(hvPinRayMap | d12PinRayMap);
        BitIterator bitIterator = new BitIterator(knights);
        while (bitIterator.hasNext()) {
            int square = bitIterator.next();
            long moveMask = KNIGHT_MOVE_MASKS[square] & ~board.getOccupancyMap(
                    friendlyPieceGroup.color) & checkRayMask;
            addMoves(friendlyPieceGroup.knight, ChessCoordinate.getBitMask(square), moveMask,
                     MoveList.Status.NORMAL);
        }
    }

    private void generatePawnMoves() {

        long pawns = board.getPieceMap(friendlyPieceGroup.pawn);
        long enemy = board.getOccupancyMap(attackingPieceGroup.color);
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

        if (friendlyPieceGroup.color == WHITE) {
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
            long shiftedCheckMask =
                    friendlyPieceGroup.color == WHITE ? checkRayMask << 8 : checkRayMask >>> 8;
            eplTargetBit = lPawns & epTarget & shiftedCheckMask;
            eprTargetBit = rPawns & epTarget & shiftedCheckMask;
        }

        lPawns &= enemy;
        rPawns &= enemy;

        addMoves(friendlyPieceGroup.pawn, 0, lPawns & notPromotion, MoveList.Status.PAWN_TAKE_LEFT);
        addMoves(friendlyPieceGroup.pawn, 0, rPawns & notPromotion,
                 MoveList.Status.PAWN_TAKE_RIGHT);
        addMoves(friendlyPieceGroup.pawn, 0, fPawns & notPromotion, MoveList.Status.PAWN_FORWARD);
        addMoves(friendlyPieceGroup.pawn, 0, pPawns, MoveList.Status.PAWN_PUSH);
        addMoves(friendlyPieceGroup.pawn, 0, lPawns & promotion, MoveList.Status.PAWN_PROMOTE_LEFT);
        addMoves(friendlyPieceGroup.pawn, 0, rPawns & promotion,
                 MoveList.Status.PAWN_PROMOTE_RIGHT);
        addMoves(friendlyPieceGroup.pawn, 0, fPawns & promotion, MoveList.Status.PAWN_PROMOTE);
        addMoves(friendlyPieceGroup.pawn, 0, eplTargetBit, MoveList.Status.EN_PASSANT_LEFT);
        addMoves(friendlyPieceGroup.pawn, 0, eprTargetBit, MoveList.Status.EN_PASSANT_RIGHT);
    }

    /**
     * Calculate and return the attack map of all the opponents sliding moves.
     */
    private void calculateSlidingAttackMap() {
        long friendlyKingBit = friendlyPieceGroup.kingCoord.getBitMask();
        long boardWithoutKing = board.getOccupancyMap() ^ friendlyKingBit;

        long queens = board.getPieceMap(attackingPieceGroup.queen);

        BitIterator bitIterator = new BitIterator(
                board.getPieceMap(attackingPieceGroup.rook) | queens);
        opponentAttackMap |= getAttackMap(bitIterator, boardWithoutKing, friendlyKingBit,
                                          ROOK_TABLE, ROOK_MAGICS, ROOK_MOVE_MASKS);

        bitIterator = new BitIterator(board.getPieceMap(attackingPieceGroup.bishop) | queens);
        opponentAttackMap |= getAttackMap(bitIterator, boardWithoutKing, friendlyKingBit,
                                          BISHOP_TABLE, BISHOP_MAGICS, BISHOP_MOVE_MASKS);
    }

    private void calculatePinRays() {
        int friendlyKingIndex = friendlyPieceGroup.kingCoord.getOndDimIndex();
        hvPinRayMap = getPinRays(board.getOccupancyMap(), ROOK_MOVE_MASKS[friendlyKingIndex],
                                 board.getPieceMap(attackingPieceGroup.queen) |
                                         board.getPieceMap(attackingPieceGroup.rook),
                                 friendlyPieceGroup.kingCoord.getOndDimIndex(), ROOK_TABLE,
                                 ROOK_MAGICS);
        d12PinRayMap = getPinRays(board.getOccupancyMap(), BISHOP_MOVE_MASKS[friendlyKingIndex],
                                  board.getPieceMap(attackingPieceGroup.queen) |
                                          board.getPieceMap(attackingPieceGroup.bishop),
                                  friendlyPieceGroup.kingCoord.getOndDimIndex(), BISHOP_TABLE,
                                  BISHOP_MAGICS);
    }

    private void calculateKnightAttackData(long friendlyKingMask) {
        BitIterator bitIterator = new BitIterator(board.getPieceMap(attackingPieceGroup.knight));
        while (bitIterator.hasNext()) {
            int knightSquare = bitIterator.next();
            long attackMask = KNIGHT_MOVE_MASKS[knightSquare];
            opponentAttackMap |= attackMask;

            if ((attackMask & friendlyKingMask) != 0) {
                inDoubleCheck = inCheck;
                inCheck = true;
                checkRayMask |= ChessCoordinate.getBitMask(knightSquare);
            }
        }
    }

    private void calculatePawnAttackData(long friendlyKingMask) {
        // Calculate Pawn Attacks
        long pawns = board.getPieceMap(attackingPieceGroup.pawn);
        long pawnAttackSquares;
        if (friendlyPieceGroup.color == BLACK) {
            pawnAttackSquares = ((pawns & ~FILE_MASKS[0]) << 7) | ((pawns & ~FILE_MASKS[7]) << 9);
        } else {
            pawnAttackSquares = ((pawns & ~FILE_MASKS[0]) >>> 9) | ((pawns & ~FILE_MASKS[7]) >>> 7);
        }

        // Set Pawn Check data
        opponentAttackMap |= pawnAttackSquares;
        if ((pawnAttackSquares & friendlyKingMask) != 0) {
            inDoubleCheck = inCheck;
            inCheck = true;
            int kingFile = friendlyPieceGroup.kingCoord.getFile();
            long attackingPieceBit = ROW_MASKS[friendlyPieceGroup.kingCoord.getRank() +
                    ((friendlyPieceGroup.color == WHITE) ? 1 : -1)] &
                    ((kingFile > 0 ? FILE_MASKS[friendlyPieceGroup.kingCoord.getFile() - 1] : 0) |
                            (kingFile < 7 ? FILE_MASKS[friendlyPieceGroup.kingCoord.getFile() + 1] :
                                    0));
            checkRayMask |= pawns & attackingPieceBit;
        }

        // Set Pawn ep bit
        pawns = board.getPieceMap(friendlyPieceGroup.pawn);
        if (game.hasEPTarget() && (epTarget = game.getEnPassantTarget().getBitMask()) != 0) {
            long eplPawn, eprPawn, epRank, epTargetPawn;
            if (friendlyPieceGroup.color == WHITE) {
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

            long rookAndQueen = board.getPieceMap(attackingPieceGroup.rook) | board.getPieceMap(
                    attackingPieceGroup.queen);
            if (!((epRank & friendlyKingMask) == 0 || (epRank & rookAndQueen) == 0 ||
                    (epRank & pawns) == 0)) {
                long upperMask =
                        SLIDING_ATTACK_MASKS[friendlyPieceGroup.kingCoord.getOndDimIndex()][RIGHT.ordinal()];
                long lowerMask =
                        SLIDING_ATTACK_MASKS[friendlyPieceGroup.kingCoord.getOndDimIndex()][LEFT.ordinal()];
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

    private void addMoves(Piece piece, long startingCoordinate, long moveMask,
                          MoveList.Status status) {
        if (moveMask != 0)
            moves.add(piece, startingCoordinate, moveMask, status);
    }

    private long pruneStraight(long mask) {
        long pinned = mask & hvPinRayMap;
        long unpinned = mask ^ pinned;

        if (friendlyPieceGroup.color == WHITE)
            pinned &= (hvPinRayMap & ~ROW_MASKS[0]) >>> 8;
        else
            pinned &= ((hvPinRayMap & ~FILE_MASKS[7]) << 8);

        return pinned | unpinned;
    }

    private long pruneLeft(long mask) {
        long pinned = mask & d12PinRayMap;
        long unpinned = mask ^ pinned;

        if (friendlyPieceGroup.color == WHITE)
            pinned &= (d12PinRayMap & ~FILE_MASKS[7]) >>> 7;
        else
            pinned &= (d12PinRayMap & ~FILE_MASKS[7]) << 9;

        return pinned | unpinned;
    }

    private long pruneRight(long mask) {
        long pinned = mask & d12PinRayMap;
        long unpinned = mask ^ pinned;

        if (friendlyPieceGroup.color == WHITE)
            pinned &= (d12PinRayMap & ~FILE_MASKS[0]) >>> 9;
        else
            pinned &= (d12PinRayMap & ~FILE_MASKS[0]) << 7;

        return pinned | unpinned;
    }

    private long getAttackMap(BitIterator pieces, long boardWithoutKing, long friendlyKing,
                              long[][] moveTable, MagicData[] magicTable, long[] moveMaskTable) {
        long attackingSquares = 0;
        while (pieces.hasNext()) {
            int pieceSquare = pieces.next();

            // Get all squares piece is attacking
            long bishopAttackingSquares = moveTable[pieceSquare][magicTable[pieceSquare].getIndex(
                    boardWithoutKing & moveMaskTable[pieceSquare])];
            attackingSquares |= bishopAttackingSquares;

            // If there is a check, mark it
            if ((bishopAttackingSquares & friendlyKing) != 0) {
                inDoubleCheck = inCheck;
                inCheck = true;
                checkRayMask |= BITS_BETWEEN_MAP[MAGIC_COORD.getIndex(
                        ChessCoordinate.getBitMask(pieceSquare))][MAGIC_COORD.getIndex(
                        friendlyKing)];
            }
        }
        return attackingSquares;
    }

    private static long getPinRays(long board, long moveMask, long slidingPieces,
                                   int friendlyKingIndex, long[][] moveTable,
                                   MagicData[] magicTable) {
        long pinRays = 0L;

        board &= ~moveTable[friendlyKingIndex][magicTable[friendlyKingIndex].getIndex(
                board & moveMask)];

        long xRaySquares = moveTable[friendlyKingIndex][magicTable[friendlyKingIndex].getIndex(
                board & moveMask)] & board;

        BitIterator piningPieceIterator = new BitIterator(slidingPieces & xRaySquares);
        while (piningPieceIterator.hasNext()) {
            int piningPieceCoord = piningPieceIterator.next();

            pinRays |= BITS_BETWEEN_MAP[MAGIC_COORD.getIndex(
                    ChessCoordinate.getBitMask(piningPieceCoord))][MAGIC_COORD.getIndex(
                    1L << friendlyKingIndex)];
        }

        return pinRays;
    }

    public long getOpponentAttackMap() {
        return opponentAttackMap;
    }

    private static class PieceGroup {
        private final char color;
        private final Piece pawn;
        private final Piece knight;
        private final Piece bishop;
        private final Piece rook;
        private final Piece queen;
        private final Piece king;
        private ChessCoordinate kingCoord;

        public PieceGroup(char color) {
            this.color = color;
            if (color == WHITE) {
                this.pawn = WHITE_PAWN;
                this.knight = WHITE_KNIGHT;
                this.bishop = WHITE_BISHOP;
                this.rook = WHITE_ROOK;
                this.queen = WHITE_QUEEN;
                this.king = WHITE_KING;
            } else {
                this.pawn = BLACK_PAWN;
                this.knight = BLACK_KNIGHT;
                this.bishop = BLACK_BISHOP;
                this.rook = BLACK_ROOK;
                this.queen = BLACK_QUEEN;
                this.king = BLACK_KING;
            }
        }
    }

    private static class MagicData {

        private final long magicNumber;
        private final int indexBits;
        private final int shiftBits;

        public MagicData(long magicNumber, int indexBits) {
            this.magicNumber = magicNumber;
            this.indexBits = indexBits;
            this.shiftBits = 64 - indexBits;
        }

        private int numCombinations() {
            return 1 << indexBits;
        }

        private int getIndex(long blockers) {
            return (int) ((blockers * magicNumber) >>> shiftBits);
        }
    }
}
