package student_player.mytools;

import java.util.HashMap;
import java.util.Iterator;

import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;

public class Precompute_Moves {
	static int MAX_DEPTH = 11;
	static boolean MEMOIZATION = true;
	static boolean REORDERING = false;
	static boolean SERIALIZE = true;
	public static void main(String[] args){
		BohnenspielBoardState bs = new BohnenspielBoardState();
//		System.out.println("Num moves: " + bs.getLegalMoves().size());
//		System.out.println(bs.toString());

//	    AlphaBeta.alpha_beta(bs, MAX_DEPTH, 100000, MEMOIZATION, REORDERING, SERIALIZE, null, null);
//	    MiniMax.minimax(bs, 0, MAX_DEPTH, 20000, MEMOIZATION, REORDERING, SERIALIZE, null, null);
		HashMap<String, String> map = SearchTools.generateMoveLookupTable(bs, MAX_DEPTH);
		SearchTools.serializeMap(map, "move_map.ser");
		System.out.println("map size: " + map.size());
//		Iterator<String> vals = map.values().iterator();
//		Iterator<String> keys = map.keySet().iterator();	
//		for(int i=0; i<10; i++)
//			System.out.println(keys.next() + " -> " + vals.next());

//		System.out.println(map.get(bs.toString()));

	}
}
