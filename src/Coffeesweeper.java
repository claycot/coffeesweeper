package src;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
        this.heightJSpinner = new JSpinner(new SpinnerNumberModel(10, 10, 100, 1));
        JLabel widthJLabel = new JLabel("Width: ");
        this.widthJSpinner = new JSpinner(new SpinnerNumberModel(10, 10, 100, 1));

        // maximum number of mines is dynamic: 1 fewer than the number of cells
        SpinnerNumberModel minesModel = new SpinnerNumberModel(10, 10, (int) heightJSpinner.getValue() * (int) widthJSpinner.getValue() - 1, 1);
        JLabel nMinesJLabel = new JLabel("# of Mines: ");
        this.nMinesJSpinner = new JSpinner(minesModel);

        // update the maximum number of mines based on the board dimensions
        ChangeListener listener = (ChangeEvent e) -> {
            int height = (int) heightJSpinner.getValue();
            int width = (int) widthJSpinner.getValue();
            int newMax = height * width - 1;

            minesModel.setMaximum(newMax);

            // if the value is greater than the max, set it to the max
            if ((int) minesModel.getValue() > newMax) {
                minesModel.setValue(newMax);
            }
        };

        heightJSpinner.addChangeListener(listener);
        widthJSpinner.addChangeListener(listener);

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

    // when new game is clicked, initialize a new game object
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