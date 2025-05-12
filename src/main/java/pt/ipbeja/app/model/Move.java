package pt.ipbeja.app.model;

/**
 * Data for each piece movement Stores initial and final position
 *
 * @author João Paulo Barros
 * @version 2014/05/18 - 2016/04/03 - 2024/05/28
 */
public record Move(Position begin, Position end) {

    /**
     * Created move only if begin and end are inside the board
     *
     * @param begin
     * @param end
     * @return new Move object if begin and end are inside, null otherwise
     */
    public static Move createMove(Position begin, Position end)
    {
        return begin.isInside() && end.isInside() ? new Move(begin, end) : null;
    }

    /**
     * Get the reversed move
     *
     * @return reverse move
     */
    public Move getReversed()
    {
        return new Move(this.end, this.begin);
    }

    @Override
    public String toString()
    {
        return "Move [begin=" + begin() + ", end=" + end() + "]";
    }

}

