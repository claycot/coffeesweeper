package src;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class MouseListenerFactory {
    public static MouseAdapter createMouseAdapter(Consumer<State> callbackState, Board board, int row, int col) {
        return new MouseAdapter() {
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
                e.consume();
            }
        };
    }
}
