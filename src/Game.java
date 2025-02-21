package src;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Game {
    private final JFrame frame;
    private final JPanel panel;

    private final int height;
    private final int width;
    private final int nMines;

    private Board board;
    private boolean started = false;
    private long startTime;

    // default values: 10x10 game with 10 mines
    Game() {
        this(10, 10, 10);
    }

    Game(int height, int width, int nMines) {
        this.frame = new JFrame();
        this.panel = new JPanel();

        this.height = height;
        this.width = width;
        this.nMines = nMines;

        this.panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        this.panel.setLayout(new GridLayout(height, width));

        // init dummy buttons because the game doesn't exist until one is clicked
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                JButton button = new JButton(Character.toString(Charset.BLANK.getDisplayChar()));
                final int row = r, col = c;
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!Game.this.started) {
                            Game.this.started = true;
                            startGame(row, col);
                        }
                    }
                });
                panel.add(button);
            }
        }

        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setTitle(String.format("Game %dx%d", height, width));
        frame.pack();
        frame.setVisible(true);

    }

    private void startGame(int firstRow, int firstCol) {
        this.board = new Board(this.height, this.width, this.nMines, firstRow, firstCol);
        this.panel.removeAll();

        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                JButton button = new JButton(Character.toString(Charset.BLANK.getDisplayChar()));
                this.board.setButton(button, r, c);
                button.addMouseListener(MouseListenerFactory.createMouseListener(this::useState, this.board, r, c));
                panel.add(button);
            }
        }

        this.startTime = System.nanoTime();
        this.board.leftClick(firstRow, firstCol);

        this.panel.revalidate();
        this.panel.repaint();
    }

    public void useState(int state) {
        // in progress
        if (state == 0) {
            return;
        }

        // reveal the game board
        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                this.board.getButton(r, c).setText(Character.toString(this.board.getCell(r, c).getDisplayChar()));
                this.board.getButton(r, c).setEnabled(false);
            }
        }


        // show result dialog
        JOptionPane.showMessageDialog(null, String.format("Game over! You %s in %.2f seconds!",
                state == 1 ? "won" : "lost", (double) (System.nanoTime() - this.startTime) / 1_000_000_000.0));

        // close the game
        frame.dispose();
    }
}
