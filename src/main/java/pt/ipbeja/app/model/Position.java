package pt.ipbeja.app.model;

public record Position(int line, int col) {

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "(" + line() + ", " + col() + ")";
    }

    /**
     * Checks if position is inside the board
     * @return true if inside, false otherwise
     */
    public boolean isInside()
    {
        return Position.isInside(this.line(), this.col());
    }

    /**
     * Checks if line col are inside tha board
     * @param line line
     * @param col column
     * @return true if inside, false otherwise
     */
    public static boolean isInside(int line, int col) {
        return 0 <= line && line < Model.N_LINES &&
                0 <= col && col < Model.N_COLS;
    }
}
