package src;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

public class MouseListenerFactory {
    public static MouseListener createMouseListener(Consumer<Integer> callbackState, Board board, int row, int col) {
        return new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                // left click
                if (e.getButton() == MouseEvent.BUTTON1) {
                    board.leftClick(row, col);
                }
                // right click
                if (e.getButton() == MouseEvent.BUTTON3) {
                    board.rightClick(row, col);
                }
                
                // either way, check game state
                callbackState.accept(board.getState());
            }
            public void mousePressed(MouseEvent e) {
            }
            public void mouseReleased(MouseEvent e) {
            }
            public void mouseEntered(MouseEvent e) {
            }
            public void mouseExited(MouseEvent e) {
            }
        };
    }
}
