package main.java.view;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public class ChessTurnView extends JPanel {

    private final JLabel label;

    public ChessTurnView(char turn) {
        super(new BorderLayout());
        this.setPreferredSize(new Dimension(0, 50));
        this.setBackground(Color.DARK_GRAY);
        label = new JLabel((turn == 'w') ? "White to move" : "Black to move", JLabel.CENTER);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Serif", Font.PLAIN, 30));
        this.add(label, BorderLayout.CENTER);
    }

    public void switchTurns() {
        if (label.getText().contains("W")) {
            label.setText("Black to move");
        } else {
            label.setText("White to move");
        }
    }
}
