package main.java.view;

import main.java.ChessCoordinate;
import main.java.controller.ChessController;
import main.java.model.SquareModel;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

public class BoardView extends JFrame {

    public static final int BORDER_SIZE = 5;

    private final ChessController controller;
    private final JPanel piecePanel;
    private final JPanel squaresPanel;

    public BoardView(ChessController controller) {
        super("Chess Board");
        this.controller = controller;
        piecePanel = new JPanel();
        squaresPanel = new JPanel();

        //Setup Board
        JPanel topLevelPanel = new JPanel();
        topLevelPanel.setLayout(new OverlayLayout(topLevelPanel));
        initializeSquaresWithPieces(topLevelPanel);
        this.add(topLevelPanel, BorderLayout.CENTER);

        initialize();
    }

    /**
     * Setup the JFrame that displays the Chess Board.
     */
    private void initialize() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);
        this.pack();
    }

    /**
     * Create and add two JPanels. One for the
     * Background/squares, and one for the pieces.
     *
     * @param panel This is the panel the other two panels will be added to.
     */
    private void initializeSquaresWithPieces(JPanel panel) {
        // Create Squares panel
        squaresPanel.setBackground(Color.black);
        squaresPanel.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        squaresPanel.setLayout(new GridLayout(8, 8));

        // Create Pieces panel
        piecePanel.setOpaque(false);
        piecePanel.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        piecePanel.setLayout(new GridLayout(8, 8));

        // Add each square
        SquareModel[][] board = controller.getBoard();
        for (int row = 7; row >= 0; row--) {
            for (int col = 0; col < 8; col++) {
                // Create 'squareView' or the panel for the square
                JPanel squareView = createSquareView(board[col][row]);
                // Create 'pieceView' or the label that displays the piece.
                PieceView pieceView = createPieceView(board[col][row]);

                // Add each component to their respective panels.
                squaresPanel.add(squareView);
                piecePanel.add(pieceView);
            }
        }
        // Add each panel to panel
        panel.add(piecePanel);
        panel.add(squaresPanel);
    }

    private PieceView createPieceView(SquareModel squareModel) {
        PieceView pieceView = new PieceView(squareModel.getPiece());
        pieceView.addMouseListener(controller);
        pieceView.addMouseMotionListener(controller);
        return pieceView;
    }

    private JPanel createSquareView(SquareModel squareModel) {
        JPanel squareView = new JPanel();
        squareView.setBackground(squareModel.getColor() == 0 ? Color.darkGray : Color.lightGray);
        switch (squareModel.getColor()) {
            case 0:
                squareView.setBackground(Color.darkGray);
                break;
            case 1:
                squareView.setBackground(Color.lightGray);
                break;
            case 2:
                squareView.setBackground(Color.red);
                break;
        }
        return squareView;
    }

    public void updateScreen() {
        updatePieces();
        this.repaint();
        this.revalidate();
    }

    private void updatePieces() {
        piecePanel.removeAll();
        squaresPanel.removeAll();

        for (int row = 7; row >= 0; row--) {
            for (int col = 0; col < 8; col++) {
                // Create 'squareView' or the panel for the square
                JPanel squareView = createSquareView(controller.getBoard()[col][row]);
                // Create 'pieceView' or the label that displays the piece.
                PieceView pieceView = createPieceView(controller.getBoard()[col][row]);

                squaresPanel.add(squareView);
                piecePanel.add(pieceView);
            }
        }
    }

    public ChessCoordinate getSquareFromPixel(int x, int y) {
        int squareSideLength = getSquareSideLength();
        int topBarSize = getTopBarSize();

        int column = (x - BORDER_SIZE) / squareSideLength;
        int row = 7 - ((y - BORDER_SIZE - topBarSize) / squareSideLength);

        if (column < 0 || 7 < column || row < 0 || 7 < row) {
            System.out.println("There's a problem:\nColumn: " + column + "\tRow: " + row);
            System.out.println("There's a problem:\nX: " + x + "\tY: " + y);
        }
        return new ChessCoordinate(column, row);
    }

    public int getSquareSideLength() {
        return (this.getWidth() - (2 * BORDER_SIZE)) / 8;
    }

    public int getTopBarSize() {
        return this.getHeight() - (2 * BORDER_SIZE) - (this.getWidth() - (2 * BORDER_SIZE));
    }

    public void drawPoint(int x, int y) {
        this.getGraphics().setColor(Color.red);
        this.getGraphics().fillOval(x, y, 4, 4);
    }
}

