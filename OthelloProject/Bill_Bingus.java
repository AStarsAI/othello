import java.util.ArrayList;


public class Bill_Bingus implements IOthelloAI {
    private static final int MAX_DEPTH = 6;
    
    @Override
    public Position decideMove(GameState s) {
        return MiniMax(s, Integer.MIN_VALUE, Integer.MAX_VALUE, MAX_DEPTH);
    }

    public Position MiniMax(GameState s, int alpha, int beta, int depth) {
        Combo result = max_value(s, alpha, beta, depth);
        return result.p;
    }

    public Combo max_value(GameState s, int alpha, int beta, int depth) {
        if (s.isFinished()) {
            return new Combo(utility(s), null);
        }
        if (depth == 0) {
            return new Combo(evaluation(s), null);
        }
        if (s.legalMoves().isEmpty()) {
            return new Combo(evaluation(s), null); // Or handle "pass" move if supported
        }
        int v = Integer.MIN_VALUE;
        Position bestMove = s.legalMoves().isEmpty() ? null : s.legalMoves().get(0);

        
        for (Position a : s.legalMoves()) {

            GameState nextState = new GameState(s.getBoard(), s.getPlayerInTurn()); //copy game state

            boolean validMove = nextState.insertToken(a);
            // if (!validMove) continue; //skips invalid moves

            Combo result = min_value(nextState, alpha, beta, depth-1);
            if (result.util > v) {
                v = result.util;
                bestMove = a;
                alpha = Math.max(alpha, v);
            }
            if(v>=beta){
                return new Combo(v, bestMove);
            }
        }
        return new Combo(v, bestMove);
    }

    public Combo min_value(GameState s, int alpha, int beta, int depth) {
        if (s.isFinished()) {
            return new Combo(utility(s), null);
        }
        if (depth == 0) {
            return new Combo(evaluation(s), null);
        }
        int v = Integer.MAX_VALUE;
        Position bestMove = s.legalMoves().isEmpty() ? null : s.legalMoves().get(0);

        if (s.legalMoves().isEmpty()) {
            return new Combo(evaluation(s), null); // Or handle "pass" move if supported
        }
        for (Position a : s.legalMoves()) {
            if (a == null) continue; // Skip null moves

            GameState nextState = new GameState(s.getBoard(), s.getPlayerInTurn());

            boolean validMove = nextState.insertToken(a);
            if (!validMove) {
                System.out.println("Invalid move: " + a);
                continue;
            } 

            Combo result = max_value(nextState, alpha, beta, depth-1);
            if (result.util < v) {
                v = result.util;
                bestMove = a;
                beta = Math.min(beta, v);
            }
            if(v<=alpha){
                return new Combo(v, bestMove);
            }
        }
        return new Combo(v, bestMove);
    }


    //Calculate the evaluation of a given gameState
    public int evaluation(GameState s) {
        int size = s.getBoard().length;
        int[] tokenCounts = s.countTokens();
        
        int[][] board = s.getBoard(); //the current board
        int currentPlayer = s.getPlayerInTurn(); //get the current player (who has a turn)
        int opponent = (currentPlayer == 1) ? 2 : 1; //the opp 
        int eval = tokenCounts[currentPlayer-1] - tokenCounts[opponent-1]; //base eval on token diff
        
        int[][] corners = {
            {0,0}, {0,size-1}, {size-1,size-1}, {size-1,0}  //all the corner coords
        };

        

        for (int[] corner : corners) {
            int x = corner[0];
            int y = corner[1];
            if(board[x][y] == currentPlayer){ //if current player owns a corner
                eval += 100; //reward 
            } else if (board[x][y] == opponent){ //if opponent owns a corner
                eval -= 100; //penalty 
            } 
        }

        if(s.isFinished()){
            if(tokenCounts[currentPlayer-1] > tokenCounts[opponent-1]){
                return Integer.MAX_VALUE / 2; //big number to ensure win 
            }
            else if(tokenCounts[currentPlayer-1] < tokenCounts[opponent-1]){
                return Integer.MIN_VALUE / 2; //small number to avoid lo
            }
            return eval;
        }

        return eval;
    }

    //calcualte the utility to see if you win or lose the game
    public int utility(GameState s) {
        int currentPlayer = s.getPlayerInTurn(); //get the current player (who has a turn)
        int opponent = (currentPlayer == 1) ? 2 : 1; //the opp 

        int[] tokenCount = s.countTokens();
        if (tokenCount[currentPlayer-1] > tokenCount[opponent-1]) {
            return Integer.MAX_VALUE / 2; //big number to ensure win 
        } else if (tokenCount[opponent-1] > tokenCount[currentPlayer-1]) {
            return Integer.MIN_VALUE / 2; //small number to avoid lo
        }
        return 0;
    }
}


class Combo {
    public int util;
    public Position p;

    public Combo(int util,Position p){
        this.util = util;
        this.p = p;
    }

}