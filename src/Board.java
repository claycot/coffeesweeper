package src;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.swing.JButton;

// a board contains a 2D array of cells that may be uncovered or flagged
public class Board {
    int height;
    int width;
    int nMines;
    Cell[][] cells;
    JButton[][] buttons;

    // relative offset for neighboring cells
    private static final int[][] dirs = {
            { -1, -1 },
            { -1, 0 },
            { -1, 1 },
            { 0, 1 },
            { 1, 1 },
            { 1, 0 },
            { 1, -1 },
            { 0, -1 },
    };

    Board(int height, int width, int nMines, int firstRow, int firstCol) {
        this.height = height;
        this.width = width;
        this.nMines = nMines;

        // generate a boolean array of mines
        boolean[][] mineMap = generateMines(firstRow, firstCol);

        // create cells using the mineMap from above
        this.cells = new Cell[this.height][this.width];
        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                this.cells[r][c] = new Cell(
                        mineMap[r][c],
                        countNeighborMines(mineMap, r, c));
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
                    row.append(Charset.BLANK.getDisplayChar());
                } else if (this.getCell(r, c).getFlagged()) {
                    row.append(Charset.FLAG.getDisplayChar());
                } else if (this.getCell(r, c).getMine()) {
                    row.append(Charset.MINE.getDisplayChar());
                } else {
                    row.append(this.getCell(r, c).getNeighborMines());
                }
                row.append(' ');
            }
            System.out.println(row.toString());
        }
    }

    // check if the player has won, lost, or is in progress
    public int getState() {
        int tilesRemaining = 0;

        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                if (!this.getCell(r, c).getRevealed()) {
                    tilesRemaining++;
                    // if the cell is revealed and there is a mine, the game is lost
                } else if (this.getCell(r, c).getMine()) {
                    // 2 - lost
                    return 2;
                }
            }
        }

        // if there are more tiles to reveal than mines, game is still in progress
        if (tilesRemaining > nMines) {
            // 0 - in progress
            return 0;
        }

        // if neither of those conditions is true, the game is won!
        // 1 - won
        return 1;
    }

    private boolean[][] generateMines(int rowSafe, int colSafe) {
        boolean[][] mines = new boolean[this.height][this.width];

        // create random mines
        for (int m = 0; m < this.nMines; m++) {
            int r = (int) (Math.random() * height);
            int c = (int) (Math.random() * width);

            // if the mine already exists, try again
            if (mines[r][c] == true || (r == rowSafe && c == colSafe)) {
                m--;
            } else {
                mines[r][c] = true;
            }
        }

        return mines;
    }

    private int countNeighborMines(boolean[][] mineMap, int row, int col) {
        int count = 0;

        // look in 8 neighboring cells for a mine
        for (int[] dir : Board.dirs) {
            int r = row + dir[0];
            int c = col + dir[1];

            // bounds check
            if (r < 0 || r >= this.height || c < 0 || c >= this.width) {
                continue;
            }

            if (mineMap[r][c]) {
                count++;
            }
        }

        return count;
    }

    public Cell getCell(int r, int c) {
        if (r < 0 || r >= this.height || c < 0 || c >= this.width) {
            throw new IllegalArgumentException("Attempted to fetch a cell that doesn't exist.");
        }

        return this.cells[r][c];
    }

    public void setButton(JButton button, int r, int c) {
        if (r < 0 || r >= this.height || c < 0 || c >= this.width) {
            throw new IllegalArgumentException("Attempted to set a button that doesn't exist.");
        }

        this.buttons[r][c] = button;
    }

    public JButton getButton(int r, int c) {
        if (r < 0 || r >= this.height || c < 0 || c >= this.width) {
            throw new IllegalArgumentException("Attempted to fetch a button that doesn't exist.");
        }

        return this.buttons[r][c];
    }

    public void leftClick(int r, int c) {
        Cell cell = getCell(r, c);
        if (!cell.getRevealed()) {
            // reveal the cell
            int cellVal = cell.Reveal();

            // if the game ends after that action, don't bother doing anything else
            int state = getState();
            if (state != 0) {
                return;
            }

            // if the value was 0, queue and uncover all adjacent cells
            if (cellVal == 0) {
                recursiveUncover(r, c);
            }
        }

        this.getButton(r, c).setText(Character.toString(this.getCell(r, c).getDisplayChar()));
        this.getButton(r, c).setEnabled(false);
    }

    public void rightClick(int r, int c) {
        Cell cell = getCell(r, c);
        // if the cell isn't revealed, flag it
        if (!cell.getRevealed()) {
            cell.Flag();
        }
        // if the cell is revealed, and it's touching as many flags as its val, reveal
        // all touching cells
        else {
            // count flag neighbors
            int neighborFlags = 0;
            Queue<Integer> queueUncover = new LinkedList<>();

            // look in 8 neighboring cells for empty cells
            for (int[] dir : Board.dirs) {
                int row = r + dir[0];
                int col = c + dir[1];

                // bounds check
                if (row < 0 || row >= this.height || col < 0 || col >= this.width) {
                    continue;
                }

                // queue cell for uncovering if it's ready
                Cell neighbor = getCell(row, col);
                if (!neighbor.getRevealed()) {
                    // if the neighbor is flagged, count it
                    if (neighbor.getFlagged()) {
                        neighborFlags++;
                    }
                    // if the neighbor isn't flagged, queue it
                    else {
                        queueUncover.add(row);
                        queueUncover.add(col);
                    }
                }
            }

            if (Character.getNumericValue(cell.getDisplayChar()) == neighborFlags) {
                while (!queueUncover.isEmpty()) {
                    int rUncover = queueUncover.remove();
                    int cUncover = queueUncover.remove();

                    int revealed = this.getCell(rUncover, cUncover).Reveal();

                    if (revealed == 0) {
                        recursiveUncover(rUncover, cUncover);
                    }
                    this.getButton(rUncover, cUncover)
                            .setText(Character.toString(this.getCell(rUncover, cUncover).getDisplayChar()));
                    this.getButton(rUncover, cUncover).setEnabled(false);
                }
            }
        }
        this.getButton(r, c).setText(Character.toString(this.getCell(r, c).getDisplayChar()));
    }

    private void recursiveUncover(int r, int c) {
        Queue<Integer> queue = new LinkedList<>();
        Map<String, Boolean> checked = new HashMap<>();
        queue.add(r);
        queue.add(c);

        while (!queue.isEmpty()) {

            // get the head of the queue
            int rHead = queue.remove();
            int cHead = queue.remove();
            if (checked.containsKey(String.format("%d,%d", rHead, cHead))) {
                continue;
            }
            checked.put(String.format("%d,%d", rHead, cHead), true);

            // look in 8 neighboring cells for empty cells
            for (int[] dir : Board.dirs) {
                int row = rHead + dir[0];
                int col = cHead + dir[1];

                // bounds check
                if (row < 0 || row >= this.height || col < 0 || col >= this.width) {
                    continue;
                }

                // reveal adjacent cells
                int revealed = this.getCell(row, col).Reveal();
                this.getButton(row, col).setText(Character.toString(this.getCell(row, col).getDisplayChar()));
                this.getButton(row, col).setEnabled(false);

                // enqueue revealed cell if it was also blank
                if (revealed == 0) {
                    queue.add(row);
                    queue.add(col);
                }
            }
        }
    }
}
