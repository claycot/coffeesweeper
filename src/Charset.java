package src;

enum Charset {
    MINE('*'),
    FLAG('⚑'),
    BLANK(' ');

    private char displayChar;

    Charset(char displayChar) {
        this.displayChar = displayChar;
    }

    public char getDisplayChar() {
        return this.displayChar;
    }
}
