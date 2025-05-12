package pt.ipbeja.app.model;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The fifteen puzzle model
 *
 * @author João Paulo Barros
 * @version 2025/05/12
 */
public class Model {
    public static final int N_LINES = 4;
    public static final int N_COLS = 4;
    public static final int EMPTY = 0;

    private final static Random RAND = new Random();
    private final static int[][] NEIGHBORS = {{-1, 0}, {0, -1}, {0, 1}, {1, 0}};

    private int board[][];
    private Position emptyPosition;

    private Deque<Move> moves;

    private Timer timer;
    private int timerValue;

    private View view;

    /**
     * Creates board in winning position
     */
    public Model(View view) {
        this.moves = new ArrayDeque<>();
        this.resetBoard();
        this.timer = new Timer();
        this.view = view;
    }

    /**
     * Creates a random mixed board starting from a winning position
     *
     * @param minIter        minimum number of iterations to mix board
     * @param additionalIter maximum number of additional iterations to mix board
     */
    public Model(View view, int minIter, int additionalIter) {
        this(view); // call default constructor Fifteen()
        this.mix(minIter, additionalIter);
        this.resetTimer();
        this.startTimer();
    }

    /**
     * Puts the board in the winning position (numbers in sequence)
     */
    private void resetBoard() {
        this.board = new int[Model.N_LINES][Model.N_COLS];
        int pieceNumber = 1;
        for (int line = 0; line < Model.N_LINES; line++) {
            for (int col = 0; col < Model.N_COLS; col++) {
                this.board[line][col] = pieceNumber++;
            }
        }
        this.board[Model.N_LINES - 1][Model.N_COLS - 1] = Model.EMPTY; // empty
        this.emptyPosition = new Position(Model.N_LINES - 1, Model.N_COLS - 1);
    }

    /**
     * @return fifteen board content in text form
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int line = 0; line < N_LINES; line++) {
            for (int col = 0; col < N_COLS; col++) {
                s.append(String.format("%2d ", this.board[line][col]));
            }
            s.setLength(s.length() - 2);
            s.append("\n");
        }
        return s.toString();
    }

    /**
     * get piece at given position
     *
     * @param position to get piece
     * @return the piece at position
     */
    public int pieceAt(Position position) {
        return this.board[position.line()][position.col()];
    }

    /**
     * get piece at given position
     *
     * @param position to get piece
     * @return the text for the piece at position
     */
    public String pieceTextAt(Position position) {
        int i = this.pieceAt(position);
        return i == EMPTY ? "empty" : (i + "");
    }

    /**
     * mixes the puzzle with random moves
     *
     * @param minMoves minimum of moves
     * @param maxMoves maximum of moves
     */
    public void mix(int minMoves, int maxMoves) {
        assert (minMoves <= maxMoves);
        int nMoves = minMoves + RAND.nextInt(maxMoves - minMoves + 1);

        for (int i = 0; i < nMoves; i++) {
            Position pieceToMove = this.randomlySelectNeighborOf(emptyPosition);
            Move m = new Move(pieceToMove, emptyPosition); // occupy empty space
            this.applyMove(m);
            this.moves.addFirst(m); // add at head (begin) of deque
        }
    }

    /**
     * Solve the puzzle using the stored positions
     */
    public void solve() {
        this.unmix(500);
    }

    /**
     * rewinds the puzzle with given moves and applies the reverse of each move
     *
     * @param sleepTime time between each move
     */
    public void unmix(int sleepTime) {
        Runnable task = () -> {
            Move m;
            while ((m = moves.poll()) != null) {
                Move mr = m.getReversed();
                applyMove(mr);
                Model.sleep(sleepTime);
                boolean winning = inWinningPositions();

                notifyViews(mr, winning, timerValue);

                if (winning) {
                    moves.clear();
                    break;
                }
            }
        };
        Thread threadToUnmix = new Thread(task);
        threadToUnmix.start();
    }

    public void pieceSelected(Position pos) {
        this.movePieceAt(pos);
    }


    /**
     * Tries to move a piece at position If moved notifies views
     *
     * @param position position of piece to move
     * @return true if moved, false otherwise
     */
    private void movePieceAt(Position position) {
        if (position.isInside()) {
            Position emptyPos = this.getEmptyInNeighborhood(position);
            if (emptyPos != null) {
                Move newMove = new Move(position, emptyPos);
                this.applyMove(newMove);
                this.moves.addFirst(newMove); // add at head (begin) of deque
                boolean winning = inWinningPositions();
                this.notifyViews(newMove, winning, timerValue);
                if (winning) {
                    timerValue = 0;
                    timer.cancel();
                }
            }
        }
    }

    /**
     * Notify observers using methods inherited from from class Observable
     *
     * @param move    the executed move
     * @param winning true if this is a winning position
     * @param tValue  current time count
     */
    private void notifyViews(Move move, Boolean winning, int tValue) {

        this.view.notifyView(move, winning, tValue);
    }

    /**
     * Gets last executed move
     *
     * @return the last move
     */
    public Move getLastMove() {
        return this.moves.getFirst();
    }

    /**
     * Checks if board as all pieces in winning positions
     *
     * @return true if winning positions, false otherwise
     */
    public boolean inWinningPositions() {
        int n = 1;
        final int TOTAL = N_LINES * N_COLS;
        for (int line = 0; line < N_LINES; line++) {
            for (int col = 0; col < N_COLS; col++) {
                if (this.board[line][col] != n && n < TOTAL) {
                    return false;
                }
                n++;
            }
        }
        return true;
    }

    /**
     * Applies move m to the board assert(move != null)
     *
     * @param move the move to apply
     * @return true
     */
    private void applyMove(Move move) {
        assert (move != null);
        this.swap(move);
    }

    private void swap(Move move) {
        assert (move.end().equals(emptyPosition));

        this.swap(move.begin(), move.end());
        this.emptyPosition = move.begin();
    }

    private void swap(Position pInit, Position pEnd) {
        int posXi = pInit.line();
        int posYi = pInit.col();
        int posXe = pEnd.line();
        int posYe = pEnd.col();
        this.swap(posXi, posYi, posXe, posYe);
    }

    private void swap(int posXi, int posYi, int posXe, int posYe) {
        int aux = this.board[posXe][posYe];
        this.board[posXe][posYe] = this.board[posXi][posYi];
        this.board[posXi][posYi] = aux;
    }

    /**
     * Randomly selects position that can be moved to the empty position
     *
     * @param empty the empty position
     * @return the selected neighbor position
     */
    private Position randomlySelectNeighborOf(Position empty) {
        int line = 0;
        int col = 0;
        do {
            int[] delta = NEIGHBORS[RAND.nextInt(NEIGHBORS.length)];
            line = empty.line() + delta[0];
            col = empty.col() + delta[1];
        } while (!Position.isInside(line, col));
        return new Position(line, col);
    }

    /**
     * Tries to return the empty position in the neighborhood of line col
     *
     * @param center position to find empty in its neighborhood
     * @return the empty position or null if non-existent in the neighborhood
     */
    private Position getEmptyInNeighborhood(Position center) {
        int lineDif = Math.abs(center.line() - emptyPosition.line());
        int colDif = Math.abs(center.col() - emptyPosition.col());
        if (lineDif == 0 && colDif == 1 || lineDif == 1 && colDif == 0) {
            return emptyPosition;
        }
        else {
            return null;
        }
    }

    /**
     * Wait the specified time in milliseconds
     *
     * @param sleepTime
     */
    public static void sleep(int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            // nothing to do
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((moves == null) ? 0 : moves.hashCode());
        result = prime * result + Arrays.deepHashCode(board);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Model other = (Model) obj;
        if (moves == null) {
            if (other.moves != null)
                return false;
        } else if (!moves.equals(other.moves))
            return false;
        if (!Arrays.deepEquals(board, other.board))
            return false;
        return true;
    }

    /**
     * Creates a new timer and sets the timer count to zero
     */
    public void resetTimer() {
        this.timerValue = -1;
        this.timer = new Timer();
    }

    /**
     * Starts timer
     */
    public void startTimer() {
        this.resetTimer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                timerValue++;
                notifyViews(null, false, timerValue);
            }
        };
        this.timer.schedule(timerTask, 0, 1000);
    }

    /**
     * Stops the current timer
     */
    public void stopTimer() {
        timer.cancel();
    }

    /**
     * Get current timer value
     *
     * @return time in seconds
     */
    public int getTimerValue() {
        return this.timerValue;
    }

}
