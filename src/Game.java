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
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                JButton button = new JButton(Character.toString(GameCharset.BLANK.getDisplayChar()));
                final int r = row, c = col;
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!Game.this.started) {
                            Game.this.started = true;
                            Game.this.startGame(r, c);
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

        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                JButton button = new JButton(Character.toString(GameCharset.BLANK.getDisplayChar()));
                this.board.setButton(button, row, col);
                button.addMouseListener(MouseListenerFactory.createMouseAdapter(this::useState, this.board, row, col));
                panel.add(button);
            }
        }

        this.startTime = System.nanoTime();
        this.board.leftClick(firstRow, firstCol);
        // check the state to ensure that it wasn't an instant win
        this.useState(this.board.getState());

        this.panel.revalidate();
        this.panel.repaint();
    }

    public void useState(State state) {
        // do nothing if in progress
        if (state == State.IN_PROGRESS) {
            return;
        }

        // otherwise...
        // reveal the game board
        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                this.board.getCell(row, col).Reveal();
                this.board.revealButton(row, col);
            }
        }

        // show result dialog
        JOptionPane.showMessageDialog(null, String.format("Game over! You %s in %.2f seconds!",
                state == State.WON ? "won" : "lost", (double) (System.nanoTime() - this.startTime) / 1_000_000_000.0));

        // close the game
        frame.dispose();
    }
}
