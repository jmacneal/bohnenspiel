package student_player.mytools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;
import student_player.mytools.SearchTools;

public class AlphaBeta {
	public static int TIME_LIMIT_MS = 700;
	public static int TIME_LIMIT_BUFF_MS = 5;
	public static int TIME_PER_MOVE_MS;
	public static int MAX_DEPTH = 9;
	public static int PLAYER_ID;
	public static boolean MEMOIZE;
	public static boolean REORDER;
	public static boolean SERIALIZE;
	public static int EVAL_FN;
	public static long START_TIME;
	
	public static Map<String, Integer> min_map; // Maps board positions to min-values 
	public static Map<String, Integer> max_map; // Maps board positions to max-values 
	public static Map<String, String> move_map; // Maps board positions to best moves


	/**
	 * 
	 * @param bs The board state at which a move must be selected for the current player
	 * @param max_depth Maxmimum recursion depth of pruned minimax algorithm
	 * @param time_limit_ms Limit on time for move
	 * @param memoization Set true to memoize values in a hashmap
	 * @param reordering Set true to search moves in order according to heuristic
	 * @param serialize Set true to serialize max- and min-value computation hashmaps
	 * @param minmap Pre-computed hashmap of min-values, set to null if undesired (or disable memoization)
	 * @param maxmap Pre-computed hashmap of max-values, set to null if undesired (or disable memoization)
	 * @param movemap re-computed hashmap of max-values, set to null if undesired
	 * @return The best move from the current position found given the computation depth and time restraints
	 * @throws IllegalArgumentException
	 */
	public static BohnenspielMove alpha_beta(BohnenspielBoardState bs, int max_depth, int time_limit_ms,
			boolean memoization, boolean reordering, boolean serialize, int eval_fn, boolean dynamic_depth,
			Map<String, Integer> minmap, Map<String, Integer> maxmap, Map<String, String> movemap) throws IllegalArgumentException{

		if(SearchTools.game_over(bs)){
			IllegalArgumentException e = new IllegalArgumentException("No legal moves available");
			throw e;
		}
		
		START_TIME = System.nanoTime();
		int moveCounter = 0;

		MAX_DEPTH = max_depth;
		PLAYER_ID = bs.getTurnPlayer();
		MEMOIZE = memoization;
		REORDER = reordering;
		SERIALIZE = serialize;
		TIME_LIMIT_MS = time_limit_ms;
		EVAL_FN = eval_fn;
		
		// Copy hashmaps if supplied, otherwise construct new ones
		if(minmap != null)
			min_map = minmap;
		else
			min_map = new HashMap<String, Integer>();

		if(maxmap != null)
			max_map = maxmap;
		else
			max_map = new HashMap<String, Integer>();
		if(movemap != null)
			move_map = movemap;
		else
			move_map = new HashMap<String, String>();
		
		
		// If move reordering enabled, order the current moves. Otherwise, use the default order.
		ArrayList<BohnenspielMove> moves;
		if(REORDER)
			moves = SearchTools.orderMoves(bs, PLAYER_ID);	
		else
			moves = bs.getLegalMoves();	
		
		if(dynamic_depth){
			MAX_DEPTH = (int) (MAX_DEPTH + Math.min(Math.pow(1.03, bs.getTurnNumber()) - 1, 2));
		}
		System.out.println("Running search with max depth " + MAX_DEPTH);

		Iterator<BohnenspielMove> move_iterator = moves.iterator();
		BohnenspielMove best_move = moves.get(0);
		int best_move_val = Integer.MIN_VALUE;

		// Check if the move exists in the pre-computed move hashmap, if so then use it
		if(move_map.containsKey(bs.toString())){
			String best_move_string = move_map.get(bs.toString());
			System.out.println("Looked up best move in move_map: " + best_move_string);
			return new BohnenspielMove(best_move_string);
		}
		
		TIME_PER_MOVE_MS = (TIME_LIMIT_MS - TIME_LIMIT_BUFF_MS) / moves.size();
		
		// If the move needs to be computed, use alpha-beta search on each possible move
		while(move_iterator.hasNext()){
			
			BohnenspielBoardState bs_temp = (BohnenspielBoardState) bs.clone();
			BohnenspielMove temp_move = move_iterator.next();
			bs_temp.move(temp_move);

			int temp_val = min_value(bs_temp, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
			if(temp_val > best_move_val){
				best_move = temp_move;
				best_move_val = temp_val;
			}
			// Since timing is important, dynamically reduce search depth if time is low
			moveCounter++;
			if((System.nanoTime() - START_TIME) / 1000000 > TIME_PER_MOVE_MS * moveCounter){
				MAX_DEPTH--;
				System.out.println("Reducing max depth to " + MAX_DEPTH + " due to time constraints");
			}
		}

		if(SERIALIZE){
			serializeMaps();
		}

		return best_move;
	}



	/*
	 * Use the mutually-recursive minimax algorithm to compute the most profitable move from the current
	 * position, with alpha-beta pruning and a provided depth limit.
	 */
	public static int max_value(BohnenspielBoardState bs, int alpha, int beta, int depth){
		// Check if the explored move results in game_over. If it's a loss, return the lowest possible
		// score. If it's a win, return the max score.
		if(SearchTools.game_over(bs)){
			if(bs.getWinner() == PLAYER_ID )
				return Integer.MAX_VALUE;
			else if(bs.getWinner() == BohnenspielBoardState.NOBODY || bs.getWinner() == BohnenspielBoardState.DRAW)
				return 0;
			else if(bs.getWinner() == (1 - PLAYER_ID))
				return Integer.MIN_VALUE;
			else if(bs.getWinner() == BohnenspielBoardState.CANCELLED0)
				return (PLAYER_ID == 1) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
			else if(bs.getWinner() == BohnenspielBoardState.CANCELLED1)
				return (PLAYER_ID == 0) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		}
		
		if(depth > MAX_DEPTH) {
			switch(EVAL_FN){
			case 0:
				return SearchTools.eval(bs, PLAYER_ID);
				
			case 1:
				return SearchTools.eval1(bs, PLAYER_ID);
			}
		}

		if(max_map.containsKey(bs.toString()) && MEMOIZE){
			return max_map.get(bs.toString());
		}

		ArrayList<BohnenspielMove> moves;
		if(REORDER)
			moves = SearchTools.orderMoves(bs, PLAYER_ID);	
		else
			moves = bs.getLegalMoves();	

		Iterator<BohnenspielMove> move_iterator = moves.iterator();		
		int max_move_val = Integer.MIN_VALUE;

		while(move_iterator.hasNext()){
			BohnenspielBoardState bs_temp = (BohnenspielBoardState) bs.clone();
			BohnenspielMove temp_move = move_iterator.next();
			bs_temp.move(temp_move);

			int temp_val = min_value(bs_temp, alpha, beta, depth+1);
			max_move_val = Math.max(max_move_val, temp_val);

			if(max_move_val >= beta){
				return max_move_val;
			}
			alpha = Math.max(alpha, max_move_val);
		}
		if (MEMOIZE)
			max_map.put(bs.toString(), max_move_val);
		return max_move_val;
	}

	/*
	 * Use the mutually-recursive minimax algorithm to compute the most profitable move from the current
	 * position, with alpha-beta pruning and a provided depth limit.
	 */
	public static int min_value(BohnenspielBoardState bs, int alpha, int beta, int depth){
		// Check if the explored move results in game_over. If it's a loss, return the lowest possible
		// score. If it's a win, return the max score.
		if(SearchTools.game_over(bs)){
			if(bs.getWinner() == PLAYER_ID )
				return Integer.MAX_VALUE;
			else if(bs.getWinner() == BohnenspielBoardState.NOBODY || bs.getWinner() == BohnenspielBoardState.DRAW)
				return 0;
			else if(bs.getWinner() == (1 - PLAYER_ID))
				return Integer.MIN_VALUE;
			else if(bs.getWinner() == BohnenspielBoardState.CANCELLED0)
				return (PLAYER_ID == 1) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
			else if(bs.getWinner() == BohnenspielBoardState.CANCELLED1)
				return (PLAYER_ID == 0) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		}
		if(depth > MAX_DEPTH){
			switch(EVAL_FN){
			case 0:
				return SearchTools.eval(bs, PLAYER_ID);
				
			case 1:
				return SearchTools.eval1(bs, PLAYER_ID);
			}
		}
		if(min_map.containsKey(bs.toString()) && MEMOIZE){
			return min_map.get(bs.toString());
		}

		ArrayList<BohnenspielMove> moves;
		if(REORDER)
			moves = SearchTools.orderMoves(bs, PLAYER_ID);	
		else
			moves = bs.getLegalMoves();	

		Iterator<BohnenspielMove> move_iterator = moves.iterator();		
		int min_move_val = Integer.MAX_VALUE;

		while(move_iterator.hasNext()){
			BohnenspielBoardState bs_temp = (BohnenspielBoardState) bs.clone();
			BohnenspielMove temp_move = move_iterator.next();
			bs_temp.move(temp_move);

			int temp_val = max_value(bs_temp, alpha, beta, depth+1);
			min_move_val = Math.min(min_move_val, temp_val);

			if(min_move_val <= alpha){
				return min_move_val;
			}
			beta = Math.min(beta, min_move_val);
		}

		if (MEMOIZE)
			min_map.put(bs.toString(), min_move_val);
		return min_move_val;
	}

	/*
	 * Output the min-value and max-value hashmaps to a serialized file
	 */
	private static void serializeMaps(){
		System.out.println("Map Size: " + max_map.size());

		try
		{
			FileOutputStream fos =
					new FileOutputStream("max_map.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(max_map);
			oos.close();
			fos.close();
			fos =	new FileOutputStream("min_map.ser");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(min_map);
			oos.close();
			fos.close();
			System.out.printf("Serialized HashMap data is saved");
		}catch(IOException ioe)
		{
			System.out.printf("Map Serialization Failed");

			ioe.printStackTrace();
		}
	}
}
