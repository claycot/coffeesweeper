package src;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Coffeesweeper implements ActionListener {
    private JFrame frame;
    private JPanel panel;

    private JSpinner heightJSpinner;
    private JSpinner widthJSpinner;
    private JSpinner nMinesJSpinner;

    public Coffeesweeper() {
        frame = new JFrame();
        panel = new JPanel();

        JLabel heightJLabel = new JLabel("Height: ");
        heightJSpinner = new JSpinner(new SpinnerNumberModel(10, 10, 100, 1));
        JLabel widthJLabel = new JLabel("Width: ");
        widthJSpinner = new JSpinner(new SpinnerNumberModel(10, 10, 100, 1));
        JLabel nMinesJLabel = new JLabel("# of Mines: ");
        nMinesJSpinner = new JSpinner(new SpinnerNumberModel(10, 10, 9999, 1));

        JButton button = new JButton("New Game");
        button.addActionListener(this);

        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        panel.setLayout(new GridLayout(4, 2));
        // row 1 : height
        panel.add(heightJLabel);
        panel.add(heightJSpinner);
        // row 2 : width
        panel.add(widthJLabel);
        panel.add(widthJSpinner);
        // row 3 : # of mines
        panel.add(nMinesJLabel);
        panel.add(nMinesJSpinner);
        // row 4 : new game
        panel.add(button);

        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Coffeesweeper");
        frame.pack();
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        new Game(
                (int) heightJSpinner.getValue(),
                (int) widthJSpinner.getValue(),
                (int) nMinesJSpinner.getValue());
    }

    public static void main(String[] args) {
        new Coffeesweeper();
    }
}