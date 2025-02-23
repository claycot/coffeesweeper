package src;

enum GameCharset {
    MINE('⛯'),
    FLAG('⚑'),
    BLANK(' ');

    private final char displayChar;

    GameCharset(char displayChar) {
        this.displayChar = displayChar;
    }

    public char getDisplayChar() {
        return this.displayChar;
    }
}
