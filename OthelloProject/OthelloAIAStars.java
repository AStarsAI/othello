import java.util.ArrayList;

public class OthelloAIAStars implements IOthelloAI {
    private static final int MAX_DEPTH = 9; // Maximum depth for Minimax search
    private int currentPlayer; // Tracks the current player
    private ArrayList<Long>  timeAverage = new ArrayList<>();
    
    public double average(ArrayList<Long> l) {
        
        long currentSum=0;

        for (long e : l){
            currentSum+=e;
        }
        double average = currentSum/(double) l.size();
        return average;
    }

    @Override
    public Position decideMove(GameState s) {
        return MiniMax(s, Integer.MIN_VALUE, Integer.MAX_VALUE, MAX_DEPTH);
    }

    /**
     * Initiates the Minimax algorithm with Alpha-Beta pruning.
     * 
     * @param s     The current game state.
     * @param alpha The Alpha value for pruning.
     * @param beta  The Beta value for pruning.
     * @param depth The remaining search depth.
     * @return The best move determined by the algorithm.
     */
    public Position MiniMax(GameState s, int alpha, int beta, int depth) {
        currentPlayer = s.getPlayerInTurn();
        long startTime = System.currentTimeMillis();
        Combo result = max_value(s, alpha, beta, depth);
        long endTime = System.currentTimeMillis();
        long delta = (endTime-startTime);
        timeAverage.add(delta);

        System.out.println("current average time per move (ms): " +average(timeAverage));

        return result.p;
    }

    /**
     * Computes the maximum utility value for the current player.
     * 
     * @param s     The current game state.
     * @param alpha The Alpha value for pruning.
     * @param beta  The Beta value for pruning.
     * @param depth The remaining search depth.
     * @return A Combo object containing the best move and its utility.
     */
    public Combo max_value(GameState s, int alpha, int beta, int depth) {
        if (s.isFinished()) {
            return new Combo(utility(s), null);
        }
        if (depth == 0 || s.legalMoves().isEmpty()) {
            return new Combo(evaluation(s), null);
        }

        int v = Integer.MIN_VALUE;
        Position bestMove = s.legalMoves().get(0);

        for (Position a : s.legalMoves()) {
            GameState nextState = new GameState(s.getBoard(), s.getPlayerInTurn());
            nextState.insertToken(a);

            Combo result = min_value(nextState, alpha, beta, depth - 1);
            if (result.util > v) {
                v = result.util;
                bestMove = a;
                alpha = Math.max(alpha, v);
            }
            if (v >= beta) {
                return new Combo(v, bestMove);
            }
        }
        return new Combo(v, bestMove);
    }

    /**
     * Computes the minimum utility value for the opponent.
     * 
     * @param s     The current game state.
     * @param alpha The Alpha value for pruning.
     * @param beta  The Beta value for pruning.
     * @param depth The remaining search depth.
     * @return A Combo object containing the best move and its utility.
     */
    public Combo min_value(GameState s, int alpha, int beta, int depth) {
        if (s.isFinished()) {
            return new Combo(utility(s), null);
        }
        if (depth == 0 || s.legalMoves().isEmpty()) {
            return new Combo(evaluation(s), null);
        }

        int v = Integer.MAX_VALUE;
        Position bestMove = s.legalMoves().get(0);

        for (Position a : s.legalMoves()) {
            if (a == null) continue;

            GameState nextState = new GameState(s.getBoard(), s.getPlayerInTurn());
            nextState.insertToken(a);

            Combo result = max_value(nextState, alpha, beta, depth - 1);
            if (result.util < v) {
                v = result.util;
                bestMove = a;
                beta = Math.min(beta, v);
            }
            if (v <= alpha) {
                return new Combo(v, bestMove);
            }
        }
        return new Combo(v, bestMove);
    }

    /**
     * Evaluates the current game state based on token difference and corner control.
     * 
     * @param s The current game state.
     * @return The heuristic evaluation score.
     */
    public int evaluation(GameState s) {
        int size = s.getBoard().length;
        int[] tokenCounts = s.countTokens();
        int[][] board = s.getBoard();
        int opponent = (currentPlayer == 1) ? 2 : 1;

        int eval = (tokenCounts[currentPlayer - 1] - tokenCounts[opponent - 1]) + 1;

        int[][] corners = {
            {0, 0}, {0, size - 1}, {size - 1, size - 1}, {size - 1, 0}
        };

        for (int[] corner : corners) {
            int x = corner[0], y = corner[1];
            if (board[x][y] == currentPlayer) {
                eval += 500; // Reward for owning a corner
            } else if (board[x][y] == opponent) {
                eval -= 500; // Penalty for opponent owning a corner
            }
        }

        return eval;
    }

    /**
     * Computes the utility value for a finished game state.
     * 
     * @param s The game state.
     * @return A high positive value for a win, a high negative value for a loss,
     *         and 0 for a draw.
     */
    public int utility(GameState s) {
        int opponent = (currentPlayer == 1) ? 2 : 1;
        int[] tokenCounts = s.countTokens();
        int roundPenalty = getCurrentRound(s) * 100; // Penalty for longer games

        if (tokenCounts[currentPlayer - 1] > tokenCounts[opponent - 1]) {
            return 10000 - roundPenalty; // Win reward
        } else if (tokenCounts[currentPlayer - 1] < tokenCounts[opponent - 1]) {
            return -10000 + roundPenalty; // Loss penalty
        }
        return 0;
    }

    /**
     * Retrieves the current round number based on the total number of placed tokens.
     * 
     * @param s The game state.
     * @return The current round number.
     */
    public int getCurrentRound(GameState s) {
        int[] tokenCounts = s.countTokens();
        return tokenCounts[0] + tokenCounts[1] - 4;
    }
}

/**
 * A helper class to store a move and its associated utility value.
 */
class Combo {
    public int util; // Utility value
    public Position p; // Corresponding move

    /**
     * Constructs a Combo object.
     * 
     * @param util The utility value.
     * @param p    The associated move.
     */
    public Combo(int util, Position p) {
        this.util = util;
        this.p = p;
    }
}
