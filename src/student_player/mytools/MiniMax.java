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

public class MiniMax {
	public static int TIME_LIMIT_MS = 700;
	public static int TIME_LIMIT_BUFF_MS = 20;
	public static int TIME_PER_MOVE_MS;
	public static int MAX_DEPTH = 6;
	public static int PLAYER_ID = 0;
	public static boolean MEMOIZE;
	public static boolean REORDER;
	public static boolean SERIALIZE;
	
	public static Map<String, Integer> min_map = new HashMap<String, Integer>() ;
	public static Map<String, Integer> max_map = new HashMap<String, Integer>() ;

	
	public static BohnenspielMove minimax(BohnenspielBoardState bs, int pid, int max_depth, int time_limit_ms,
			boolean memoization, boolean reordering, boolean serialize,
			Map<String, Integer> minmap, Map<String, Integer> maxmap) throws IllegalArgumentException{
		
		long startTime = System.nanoTime();
		int moveCounter = 0;
		if(SearchTools.game_over(bs)){
			IllegalArgumentException e = new IllegalArgumentException("No legal moves available");
			throw e;
		}		
		MAX_DEPTH = max_depth;
		PLAYER_ID = pid;
		MEMOIZE = memoization;
		SERIALIZE = serialize;
		REORDER = reordering;
		TIME_LIMIT_MS = time_limit_ms;

		if(minmap != null)
			min_map = minmap;
		else
			min_map = new HashMap<String, Integer>();
		
		if(maxmap != null)
			max_map = maxmap;
		else
			max_map = new HashMap<String, Integer>();
		ArrayList<BohnenspielMove> moves = bs.getLegalMoves();	
		Iterator<BohnenspielMove> move_iterator = moves.iterator();
		
		BohnenspielMove best_move = moves.get(0);
		int best_move_val = Integer.MIN_VALUE;
		
		if(MEMOIZE){
			for(BohnenspielMove move : moves){
				BohnenspielBoardState bs_temp = (BohnenspielBoardState) bs.clone();
				bs_temp.move(move);
				if(max_map.containsKey(bs_temp.toString())){
					System.out.println("Looked up value in max_map: " + max_map.get(bs_temp.toString()));
					if(SERIALIZE)
						serializeMaps();
					return move;
				}
			}
		}
		
		TIME_PER_MOVE_MS = (TIME_LIMIT_MS - TIME_LIMIT_BUFF_MS) / moves.size();

		while(move_iterator.hasNext()){
			BohnenspielBoardState bs_temp = (BohnenspielBoardState) bs.clone();
			BohnenspielMove temp_move = move_iterator.next();
			bs_temp.move(temp_move);
			int temp_val = min_value(bs_temp, 0);
			if(temp_val > best_move_val){
				best_move = temp_move;
				best_move_val = temp_val;
			}
			moveCounter++;
			if((System.nanoTime() - startTime) / 1000000 > TIME_PER_MOVE_MS * moveCounter){
				MAX_DEPTH--;
				System.out.println("Reducing max depth to " + MAX_DEPTH + " due to time constraints");
			}
		}
		
		
		if(SERIALIZE){
			serializeMaps();
		}
		
		if(MEMOIZE){
			BohnenspielBoardState bs_temp = (BohnenspielBoardState) bs.clone();
			bs_temp.move(best_move);
			if(max_map.containsKey(bs_temp.toString()))
				System.out.println("Lookde up value in max_map: " + max_map.get(bs_temp.toString()));
		}
		return best_move;
	}


	public static int max_value(BohnenspielBoardState bs, int depth){
		if(SearchTools.game_over(bs) || depth > MAX_DEPTH){
			return SearchTools.eval(bs, PLAYER_ID);
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
			int temp_val = min_value(bs_temp, depth+1);
			if(temp_val > max_move_val){
				max_move_val = temp_val;
			}
		}
//		if (MEMOIZE)
//			max_map.put(bs.toString(), max_move_val);
		return max_move_val;
	}
	
	public static int min_value(BohnenspielBoardState bs, int depth){
		if(SearchTools.game_over(bs) || depth > MAX_DEPTH){
			return SearchTools.eval(bs, PLAYER_ID);
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
			int temp_val = max_value(bs_temp, depth+1);
			if(temp_val < min_move_val){
				min_move_val = temp_val;
			}
		}
//		if (MEMOIZE)
//			min_map.put(bs.toString(), min_move_val);
		return min_move_val;
	}
	
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
