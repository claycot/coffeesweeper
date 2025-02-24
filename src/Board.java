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
        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                this.cells[row][col] = new Cell(
                        mineMap[row][col],
                        this.countNeighborMines(mineMap, row, col));
            }
        }

        // hold refs to GUI buttons
        this.buttons = new JButton[this.height][this.width];
    }

    // print out the board for debug purposes
    public void drawBoard() {
        for (int row = 0; row < this.height; row++) {
            StringBuilder outRow = new StringBuilder();
            for (int col = 0; col < this.width; col++) {
                if (!this.getCell(row, col).getRevealed()) {
                    outRow.append(GameCharset.BLANK.getDisplayChar());
                } else if (this.getCell(row, col).getFlagged()) {
                    outRow.append(GameCharset.FLAG.getDisplayChar());
                } else if (this.getCell(row, col).getMine()) {
                    outRow.append(GameCharset.MINE.getDisplayChar());
                } else {
                    outRow.append(this.getCell(row, col).getNeighborMines());
                }
                outRow.append(' ');
            }
            System.out.println(outRow.toString());
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
    public void updateState(int row, int col) {
        // if the user uncoverd a mine, end the game
        if (this.getCell(row, col).getMine()) {
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
            int row = (int) (Math.random() * height);
            int col = (int) (Math.random() * width);

            // if the mine already exists, or is in the first safe space, try again
            if (mines[row][col] == true || (row == rowSafe && col == colSafe)) {
                m--;
            } else {
                mines[row][col] = true;
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
    public Cell getCell(int row, int col) {
        if (!this.isValidCell(row, col)) {
            throw new IllegalArgumentException("Attempted to fetch a cell that doesn't exist.");
        }

        return this.cells[row][col];
    }

    // assign a button to a row and col coordinate
    public void setButton(JButton button, int row, int col) {
        if (!this.isValidCell(row, col)) {
            throw new IllegalArgumentException("Attempted to set a button that doesn't exist.");
        }

        this.buttons[row][col] = button;
    }

    // update the button text to reveal what is in the underlying cell, and disable
    // it
    public void revealAndDisableButton(int row, int col) {
        if (!this.isValidCell(row, col)) {
            throw new IllegalArgumentException("Attempted to reveal a button that doesn't exist.");
        }

        JButton button = this.buttons[row][col];

        button.setText(Character.toString(this.getCell(row, col).getDisplayChar()));
        button.setEnabled(false);
    }

    // update the button text to reveal what is in the underlying cell
    public void revealButton(int row, int col) {
        if (!this.isValidCell(row, col)) {
            throw new IllegalArgumentException("Attempted to flag a button that doesn't exist.");
        }

        JButton button = this.buttons[row][col];

        button.setText(Character.toString(this.getCell(row, col).getDisplayChar()));
    }

    // left clicking on a cell will reveal it
    public void leftClick(int row, int col) {
        Cell cell = this.getCell(row, col);
        if (!cell.getRevealed()) {
            this.floodReveal(row, col);
        }
    }

    // right clicking on a cell with toggle the flag, or flood reveal if enough
    // neighboring cells are flagged
    public void rightClick(int row, int col) {
        Cell cell = this.getCell(row, col);
        // if the cell isn't revealed, flag it
        if (!cell.getRevealed()) {
            cell.Flag();
            this.revealButton(row, col);
        }
        // if the cell is revealed, and it's touching as many flags as its val,
        // reveal all touching cells
        else {
            // count flag neighbors
            AtomicInteger neighborFlags = new AtomicInteger(0);
            Queue<Integer> queueUncover = new LinkedList<>();

            // look in 8 neighboring cells for empty cells
            this.forEachNeighbor(row, col, (r, c) -> {
                // queue cell for uncovering if it's ready
                Cell neighbor = getCell(r, c);
                if (!neighbor.getRevealed()) {
                    // if the neighbor is flagged, count it
                    if (neighbor.getFlagged()) {
                        neighborFlags.incrementAndGet();
                    }
                    // if the neighbor isn't flagged, queue it
                    else {
                        queueUncover.add(r);
                        queueUncover.add(c);
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

    // reveal the cell at row, col
    // if the revealed cell is a 0 (empty), reveal all neighbors
    private void floodReveal(int row, int col) {
        Queue<Integer> queue = new LinkedList<>();
        Map<String, Boolean> checked = new HashMap<>();
        queue.add(row);
        queue.add(col);

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
                this.forEachNeighbor(rHead, cHead, (r, c) -> {
                    // enqueue revealed cell if it has not already been revealed
                    if (!this.getCell(r, c).getRevealed()) {
                        queue.add(r);
                        queue.add(c);
                    }
                });
            }
        }
    }
}
