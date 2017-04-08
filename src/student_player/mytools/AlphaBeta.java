package student_player.mytools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;

public class AlphaBeta {
	public static int MAX_DEPTH = 9;
	public static int player_id = 0;

	public static Map<Integer, Integer> min_map = new HashMap<Integer, Integer>() ;
	public static Map<Integer, Integer> max_map = new HashMap<Integer, Integer>() ;

	
	public static BohnenspielMove alpha_beta(BohnenspielBoardState bs, int pid) throws IllegalArgumentException{
		if(game_over(bs)){
			IllegalArgumentException e = new IllegalArgumentException("No legal moves available");
			throw e;
		}
		player_id = pid;
		ArrayList<BohnenspielMove> moves = bs.getLegalMoves();	
		Iterator<BohnenspielMove> move_iterator = moves.iterator();
		BohnenspielMove best_move = moves.get(0);
		int best_move_val = Integer.MIN_VALUE;
		
		while(move_iterator.hasNext()){
			BohnenspielBoardState bs_temp = (BohnenspielBoardState) bs.clone();
			BohnenspielMove temp_move = move_iterator.next();
			bs_temp.move(temp_move);
			
			int temp_val = min_value(bs_temp, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
			if(temp_val > best_move_val){
				best_move = temp_move;
				best_move_val = temp_val;
			}
		}
		
		return best_move;
	}


	public static int max_value(BohnenspielBoardState bs, int alpha, int beta, int depth){
		if(game_over(bs) || depth > MAX_DEPTH){
			return eval(bs);
		}

		
		ArrayList<BohnenspielMove> moves = bs.getLegalMoves();	
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

		return max_move_val;
	}
	
	public static int min_value(BohnenspielBoardState bs, int alpha, int beta,int depth){
		if(game_over(bs) || depth > MAX_DEPTH){

			return eval(bs);
		}
		
		ArrayList<BohnenspielMove> moves = bs.getLegalMoves();	
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

		return min_move_val;
	}

	
	public static int eval(BohnenspielBoardState bs){
		return bs.getScore(player_id) - bs.getScore(1 - player_id);
	}
	
	public static boolean game_over(BohnenspielBoardState bs){
		ArrayList<BohnenspielMove> moves = bs.getLegalMoves();
		if(moves.isEmpty()){
			return true;
		}
		return false;
	}
}
