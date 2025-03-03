package src;

// a cell is the unit that either contains a mine or a number of neighboring mines
public class Cell {
    // has a mine inside of it true/false
    private final boolean mine;
    // number of mines touching in the 8 immediate neighbors
    private final int neighborMines;

    // has been revealed by user
    private boolean revealed = false;
    // has been flagged by user
    private boolean flagged = false;

    // create a cell with or without a mine, and a number of neighboring mines
    public Cell(boolean mine, int neighborMines) {
        this.mine = mine;
        this.neighborMines = neighborMines;
    }

    // reveal the cell and return the value within (-1 if a mine)
    public int Reveal() {
        this.revealed = true;
        this.flagged = false;

        if (this.mine) {
            return -1;
        }
        else {
            return neighborMines;
        }
    }

    // flag a cell (if it's not already revealed)
    public void Flag() {
        if (!this.revealed) {
            this.flagged = !this.flagged;
        }
    }

    // return the GameCharset value for the cell
    public char getDisplayChar() {
        char displayChar;
        if (this.getFlagged()) {
            displayChar = GameCharset.FLAG.getDisplayChar();
        }
        else if (!this.getRevealed()) {
            displayChar = GameCharset.BLANK.getDisplayChar();
        }
        else if (this.getMine()) {
            displayChar = GameCharset.MINE.getDisplayChar();
        }
        else if (this.getNeighborMines() == 0) {
            displayChar = GameCharset.BLANK.getDisplayChar();
        }
        else {
            displayChar = (char) ('0' + this.getNeighborMines());
        }

        return displayChar;
    }

    public boolean getMine() {
        return this.mine;
    }

    public int getNeighborMines() {
        return this.neighborMines;
    }

    public boolean getRevealed() {
        return this.revealed;
    }
    
    public boolean getFlagged() {
        return this.flagged;
    }    

}
