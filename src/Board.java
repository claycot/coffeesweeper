package src;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import javax.swing.JButton;

// a board contains a 2D array of cells that may be uncovered or flagged
public class Board {
    private int height;
    private int width;
    private int nMines;

    private Cell[][] cells;
    private JButton[][] buttons;
    private int cellsUncovered;

    private State state = State.IN_PROGRESS;

    // relative offset for neighboring cells
    // used in forEachNeighbor
    private static final List<int[]> DIRECTIONS = List.of(
            new int[] { -1, -1 }, new int[] { -1, 0 }, new int[] { -1, 1 },
            new int[] { 0, -1 }, new int[] { 0, 1 },
            new int[] { 1, -1 }, new int[] { 1, 0 }, new int[] { 1, 1 });

    Board(int height, int width, int nMines, int firstRow, int firstCol) {
        this.height = height;
        this.width = width;
        this.nMines = nMines;
        this.cellsUncovered = 0;

        // generate a boolean array of mines
        boolean[][] mineMap = generateMines(firstRow, firstCol);

        // create cells using the mineMap from above
        this.cells = new Cell[this.height][this.width];
        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                this.cells[r][c] = new Cell(
                        mineMap[r][c],
                        this.countNeighborMines(mineMap, r, c));
            }
        }

        // hold refs to GUI buttons
        this.buttons = new JButton[this.height][this.width];
    }

    // print out the board for debug purposes
    public void drawBoard() {
        for (int r = 0; r < this.height; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < this.width; c++) {
                if (!this.getCell(r, c).getRevealed()) {
                    row.append(GameCharset.BLANK.getDisplayChar());
                } else if (this.getCell(r, c).getFlagged()) {
                    row.append(GameCharset.FLAG.getDisplayChar());
                } else if (this.getCell(r, c).getMine()) {
                    row.append(GameCharset.MINE.getDisplayChar());
                } else {
                    row.append(this.getCell(r, c).getNeighborMines());
                }
                row.append(' ');
            }
            System.out.println(row.toString());
        }
    }

    // perform the given action on all neighbors (8-directional adjacent)
    private void forEachNeighbor(int row, int col, BiConsumer<Integer, Integer> action) {
        for (int[] dir : DIRECTIONS) {
            int r = row + dir[0];
            int c = col + dir[1];

            if (isValidCell(r, c)) {
                action.accept(r, c);
            }
        }
    }

    // check bounds to validate cell
    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < this.height && col >= 0 && col < this.width;
    }

    // after uncovering a cell, check if the player has won, lost, or is in progress
    public void updateState(int r, int c) {
        // if the user uncoverd a mine, end the game
        if (this.getCell(r, c).getMine()) {
            this.state = State.LOST;
        }
        // if there are more cells to reveal than mines, game is still in progress
        else if ((this.height * this.width - this.cellsUncovered) > this.nMines) {
            this.state = State.IN_PROGRESS;
        }
        // if neither of those conditions is true, the game is won!
        else {
            this.state = State.WON;
        }
    }

    // return game state: WON, LOST, or IN_PROGRESS
    public State getState() {
        return this.state;
    }

    // generate a boolean map of mines
    private boolean[][] generateMines(int rowSafe, int colSafe) {
        boolean[][] mines = new boolean[this.height][this.width];

        // create random mines
        for (int m = 0; m < this.nMines; m++) {
            int r = (int) (Math.random() * height);
            int c = (int) (Math.random() * width);

            // if the mine already exists, or is in the first safe space, try again
            if (mines[r][c] == true || (r == rowSafe && c == colSafe)) {
                m--;
            } else {
                mines[r][c] = true;
            }
        }

        return mines;
    }

    // count how many mines border the cell
    private int countNeighborMines(boolean[][] mineMap, int row, int col) {
        AtomicInteger count = new AtomicInteger(0);

        // look in 8 neighboring cells for a mine
        this.forEachNeighbor(row, col, (r, c) -> {
            if (mineMap[r][c]) {
                count.incrementAndGet();
            }
        });

        return count.get();
    }

    // fetch a ref to a cell with given row, col coordinates
    public Cell getCell(int r, int c) {
        if (r < 0 || r >= this.height || c < 0 || c >= this.width) {
            throw new IllegalArgumentException("Attempted to fetch a cell that doesn't exist.");
        }

        return this.cells[r][c];
    }

    // assign a button to a row and col coordinate
    public void setButton(JButton button, int r, int c) {
        if (r < 0 || r >= this.height || c < 0 || c >= this.width) {
            throw new IllegalArgumentException("Attempted to set a button that doesn't exist.");
        }

        this.buttons[r][c] = button;
    }

    // update the button text to reveal what is in the underlying cell, and disable
    // it
    public void revealAndDisableButton(int r, int c) {
        if (r < 0 || r >= this.height || c < 0 || c >= this.width) {
            throw new IllegalArgumentException("Attempted to reveal a button that doesn't exist.");
        }

        JButton button = this.buttons[r][c];

        button.setText(Character.toString(this.getCell(r, c).getDisplayChar()));
        button.setEnabled(false);
    }

    // update the button text to reveal what is in the underlying cell
    public void revealButton(int r, int c) {
        if (r < 0 || r >= this.height || c < 0 || c >= this.width) {
            throw new IllegalArgumentException("Attempted to flag a button that doesn't exist.");
        }

        JButton button = this.buttons[r][c];

        button.setText(Character.toString(this.getCell(r, c).getDisplayChar()));
    }

    // left clicking on a cell will reveal it
    public void leftClick(int r, int c) {
        Cell cell = this.getCell(r, c);
        if (!cell.getRevealed()) {
            this.floodReveal(r, c);
        }
    }

    // right clicking on a cell with toggle the flag, or flood reveal if enough
    // neighboring cells are flagged
    public void rightClick(int r, int c) {
        Cell cell = this.getCell(r, c);
        // if the cell isn't revealed, flag it
        if (!cell.getRevealed()) {
            cell.Flag();
            this.revealButton(r, c);
        }
        // if the cell is revealed, and it's touching as many flags as its val,
        // reveal all touching cells
        else {
            // count flag neighbors
            AtomicInteger neighborFlags = new AtomicInteger(0);
            Queue<Integer> queueUncover = new LinkedList<>();

            // look in 8 neighboring cells for empty cells
            this.forEachNeighbor(r, c, (row, col) -> {
                // queue cell for uncovering if it's ready
                Cell neighbor = getCell(row, col);
                if (!neighbor.getRevealed()) {
                    // if the neighbor is flagged, count it
                    if (neighbor.getFlagged()) {
                        neighborFlags.incrementAndGet();
                    }
                    // if the neighbor isn't flagged, queue it
                    else {
                        queueUncover.add(row);
                        queueUncover.add(col);
                    }
                }
            });

            // if the cell is surrounded by as many flagged cells as mines, reveal them
            if (Character.getNumericValue(cell.getDisplayChar()) == neighborFlags.get()) {
                while (!queueUncover.isEmpty()) {
                    int rUncover = queueUncover.remove();
                    int cUncover = queueUncover.remove();

                    this.floodReveal(rUncover, cUncover);
                }
            }
        }
    }

    // reveal the cell at r, c
    // if the revealed cell is a 0 (empty), reveal all neighbors
    private void floodReveal(int r, int c) {
        Queue<Integer> queue = new LinkedList<>();
        Map<String, Boolean> checked = new HashMap<>();
        queue.add(r);
        queue.add(c);

        while (!queue.isEmpty()) {
            // get the head of the queue
            int rHead = queue.remove();
            int cHead = queue.remove();
            Cell cell = this.getCell(rHead, cHead);

            // stop if the cell is flagged or revealed
            if (cell.getFlagged() || cell.getRevealed()) {
                continue;
            }

            // stop if the cell has already been flood revealed
            if (checked.containsKey(String.format("%d,%d", rHead, cHead))) {
                continue;
            }
            // otherwise, hash and continue
            checked.put(String.format("%d,%d", rHead, cHead), true);

            // reveal the cell
            int revealed = cell.Reveal();
            this.cellsUncovered++;
            this.revealAndDisableButton(rHead, cHead);
            this.updateState(rHead, cHead);

            // if cell was empty, reveal its neighbors
            if (revealed == 0) {
                this.forEachNeighbor(rHead, cHead, (row, col) -> {
                    // enqueue revealed cell if it has not already been revealed
                    if (!this.getCell(row, col).getRevealed()) {
                        queue.add(row);
                        queue.add(col);
                    }
                });
            }
        }
    }
}
