package chess.view;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is capable of creating a JPanel that shows a
 * ChessBoard.
 */
public class ChessBoardView extends JLayeredPane {

    // The panel that contains the squares (background)
    private final JPanel squaresPanel;
    // The panel that contains the pieces.
    private final JPanel piecesPanel;

    private final List<ChessCoordinate> markedEnds;

    /**
     * Creates a new ChessBoardView from the given board,
     * MouseListener and MouseMotionListener.
     *
     * @param board          the board to display
     * @param mouseListener  the mouseListener for the PieceViews to use.
     * @param motionListener the MouseMotionListener for the PieceViews to use.
     */
    public ChessBoardView(Piece[] board, MouseListener mouseListener,
                          MouseMotionListener motionListener) {
        this.setLayout(new OverlayLayout(this));
        this.markedEnds = new ArrayList<>();
        squaresPanel = new JPanel(new GridLayout(8, 8));
        piecesPanel = new JPanel(new GridLayout(8, 8));
        initBoardView(board, mouseListener, motionListener);
    }

    /**
     * Returns the ZOrder of the piece on the given ChessCoordinate.
     *
     * @param coordinate the chessCoordinate to get the ZOrder of
     * @return the ZOrder of the piece on the given coordinate
     */
    private static int getZOrder(ChessCoordinate coordinate) {
        return (7 - coordinate.getRank()) * 8 + coordinate.getFile();
    }

    /**
     * Creates a new Square from the given rank and file.
     *
     * @param rank the rank of this SquareView.
     * @param file the file of this SquareView.
     * @return the SquareView corresponding to the given rank and file.
     */
    private static JPanel makeSquare(int rank, int file) {
        JPanel panel = new JPanel();
        float[] darkHSB = Color.RGBtoHSB(180, 136, 99, null);
        float[] lightHSB = Color.RGBtoHSB(240, 217, 181, null);
        panel.setBackground((rank + file) % 2 == 0 ? Color.getHSBColor(darkHSB[0], darkHSB[1], darkHSB[2]) :
                Color.getHSBColor(lightHSB[0], lightHSB[1], lightHSB[2]));
        return panel;
    }

    /**
     * Creates a new Square from the given Piece, MouseListener
     * and MouseMotionListener.
     *
     * @param piece          the piece on this square.
     * @param mouseListener  the MouseListener for this pieceView to use.
     * @param motionListener the MouseMotionListener for this pieceView to use.
     * @return the PieceView representation of this Piece.
     */
    private static Component makePieceView(Piece piece, MouseListener mouseListener,
                                                MouseMotionListener motionListener) {
        ChessPieceView pieceView = new ChessPieceView(piece);
        pieceView.addMouseListener(mouseListener);
        pieceView.addMouseMotionListener(motionListener);
        return pieceView;
    }

    public void animateMove(Move move) {
        Component start = piecesPanel.getComponent(getZOrder(move.getStartingCoordinate()));
        Component end = piecesPanel.getComponent(getZOrder(move.getEndingCoordinate()));
        new Animate(start, start.getLocation(), end.getLocation(), () -> showMove(move)).start();
    }

    /**
     * Updates the BoardView from the given move.
     *
     * @param move the last move made.
     */
    public void showMove(Move move) {
        if (move != null) {
            if (move.getInteractingPieceStart() != null) {
                if (move.getInteractingPieceEnd() == null) {
                    // Capture
                    ((ChessPieceView) piecesPanel.getComponent(getZOrder(move.getInteractingPieceStart()))).capture();
                } else {
                    // Castle
                    swap(move.getInteractingPieceStart(), move.getInteractingPieceEnd());
                }
            }
            if (move.doesPromote()) {
                ((ChessPieceView) piecesPanel.getComponent(getZOrder(move.getStartingCoordinate()))).capture();
                ((ChessPieceView) piecesPanel.getComponent(getZOrder(move.getEndingCoordinate()))).setImage(move.getPromotedPiece());
            } else {
                swap(move.getStartingCoordinate(), move.getEndingCoordinate());
            }
        }
        piecesPanel.revalidate();
        piecesPanel.repaint();
    }

    public void markEnds(List<ChessCoordinate> endCoordinates) {
        for (ChessCoordinate coordinate : endCoordinates) {
            Component component = squaresPanel.getComponent(getZOrder(coordinate));

            float[] darkHSB = Color.RGBtoHSB(182, 45, 61, null);
            float[] lightHSB = Color.RGBtoHSB(198, 60, 68, null);
            component.setBackground((coordinate.getFile() + coordinate.getRank()) % 2 == 0 ?
                    Color.getHSBColor(darkHSB[0], darkHSB[1], darkHSB[2]) :
                    Color.getHSBColor(lightHSB[0], lightHSB[1], lightHSB[2]));

            markedEnds.add(coordinate);
        }
    }

    public void unmarkEnds() {
        for (ChessCoordinate coordinate : markedEnds) {
            Component component = squaresPanel.getComponent(getZOrder(coordinate));

            float[] darkHSB = Color.RGBtoHSB(180, 136, 99, null);
            float[] lightHSB = Color.RGBtoHSB(240, 217, 181, null);
            component.setBackground((coordinate.getFile() + coordinate.getRank()) % 2 == 0 ?
                    Color.getHSBColor(darkHSB[0], darkHSB[1], darkHSB[2]) :
                    Color.getHSBColor(lightHSB[0], lightHSB[1], lightHSB[2]));
        }
        markedEnds.clear();
    }

    private void swap(ChessCoordinate coordinate1, ChessCoordinate coordinate2) {
        int zOrder1 = getZOrder(coordinate1);
        int zOrder2 = getZOrder(coordinate2);

        if (zOrder1 > zOrder2) {
            int temp = zOrder1;
            zOrder1 = zOrder2;
            zOrder2 = temp;
        }

        Component first = piecesPanel.getComponent(zOrder1);
        Component second = piecesPanel.getComponent(zOrder2);

        piecesPanel.remove(first);
        piecesPanel.remove(second);

        piecesPanel.add(second, zOrder1);
        piecesPanel.add(first, zOrder2);
    }

    /**
     * Initializes this board View by setting up the piecesPanel and
     * the squaresPanel.
     *
     * @param board          the board to display
     * @param mouseListener  the MouseListener for the PieceViews to use
     * @param motionListener the MouseMotionListener for the PieceViews to use
     */
    private void initBoardView(Piece[] board, MouseListener mouseListener,
                               MouseMotionListener motionListener) {
        piecesPanel.setOpaque(false);
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                Piece piece = board[rank * 8 + file];
                piecesPanel.add(makePieceView(piece, mouseListener, motionListener), BorderLayout.CENTER);
                squaresPanel.add(makeSquare(rank, file), BorderLayout.CENTER);
            }
        }
        this.add(piecesPanel, DRAG_LAYER);
        this.add(squaresPanel, DEFAULT_LAYER);
    }

    public void slowUpdateBoard(Piece[] board, MouseListener mouseListener, MouseMotionListener motionListener) {
        piecesPanel.removeAll();
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                Piece piece = board[rank * 8 + file];
                Component pieceView = makePieceView(piece, mouseListener, motionListener);
                piecesPanel.add(pieceView);
            }
        }
    }

    private static class Animate {
        private static final int RUN_TIME = 100;

        private final Component component;
        private final Point start;
        private final Point end;
        private final Runnable afterEnd;

        private long startTime;

        public Animate(Component component, Point start, Point end, Runnable afterEnd) {
            this.component = component;
            this.start = start;
            this.end = end;
            this.afterEnd = afterEnd;
            this.startTime = 0;
        }

        public void start() {
            Timer timer = new Timer(40, e -> {
                long duration = System.currentTimeMillis() - startTime;
                double progress = (double) duration / RUN_TIME;

                if (progress > 1f) {
                    ((Timer) e.getSource()).stop();
                    afterEnd.run();
                } else {
                    Point target = calculateProgress(start, end, progress);
                    component.setLocation(target);
                }
            });

            timer.setRepeats(true);
            timer.setCoalesce(true);
            timer.setInitialDelay(0);
            startTime = System.currentTimeMillis();
            timer.start();
        }

        public static Point calculateProgress(Point startPoint, Point targetPoint, double progress) {
            Point point = new Point();

            if (startPoint != null && targetPoint != null) {

                point.x = calculateProgress(startPoint.x, targetPoint.x, progress);
                point.y = calculateProgress(startPoint.y, targetPoint.y, progress);

            }
            return point;
        }

        public static int calculateProgress(int startValue, int endValue, double fraction) {
            int value;
            int distance = endValue - startValue;
            value = (int) Math.round((double) distance * fraction);
            value += startValue;

            return value;
        }
    }
}
