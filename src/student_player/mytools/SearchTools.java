package student_player.mytools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;

public class SearchTools {

	public static int getSomething(){
		return -1;
	}		

	/*
	 * Move ordering should order the moves from highest to lowest in estimated value for the Maximizing player (top node).
	 * By passing the global player_id, this allows for the sort to always sort in the correct order.
	 */
	public static ArrayList<BohnenspielMove> orderMoves(final BohnenspielBoardState bs, final int player_id){

		ArrayList<BohnenspielMove> sortedMoves = bs.getLegalMoves();
		Collections.sort(sortedMoves,new Comparator<BohnenspielMove>(){
			@Override
			public int compare(BohnenspielMove move0, BohnenspielMove move1) {
				BohnenspielBoardState state0 = (BohnenspielBoardState) bs.clone();
				BohnenspielBoardState state1 = (BohnenspielBoardState) bs.clone();

				state0.move(move0);
				state1.move(move1);
				//					int move0_val = eval(state0, player_id);
				//					int move1_val = eval(state1, player_id);

				return ((state0.getScore(player_id) - state0.getScore(1 - player_id)) -
						(state1.getScore(player_id) - state1.getScore(1 - player_id)));
			}
		});

		return sortedMoves;
	}



	/*
	 * Default evaluation function, weighting both the score differential and the difference in the
	 * number of pieces on both sides.
	 */
	public static int eval(BohnenspielBoardState bs, int player_id){
		int score_difference = bs.getScore(player_id) - bs.getScore(1 - player_id);
		int piece_difference = 0;
		int pits[][] = bs.getPits();
		for(int i : pits[player_id])
			piece_difference += i;
		for(int j : pits[1 - player_id])
			piece_difference -= j;

		return (int)(score_difference + piece_difference * 0.5);
	}



	/*
	 * Basic evaluation function, taking just the score differential
	 */
	public static int eval_basic(BohnenspielBoardState bs, int player_id){
		return bs.getScore(player_id) - bs.getScore(1 - player_id);
	}

	public static boolean game_over(BohnenspielBoardState bs){
		ArrayList<BohnenspielMove> moves = bs.getLegalMoves();
		if(moves.isEmpty()){
			return true;
		}
		return false;
	}

	
	/*
	 * Generate a lookup table of best moves, with search depth decreasing
	 */
	public static HashMap<String, String> generateMoveLookupTable(BohnenspielBoardState bs, int max_search_depth){
		HashMap<String, String> map =  new HashMap<String, String>();

		for(BohnenspielMove move0 : bs.getLegalMoves()){
			BohnenspielBoardState bs_temp0 = (BohnenspielBoardState) bs.clone();
			bs_temp0.move(move0);
			for(BohnenspielMove move1 : bs_temp0.getLegalMoves()){
				BohnenspielBoardState bs_temp1 = (BohnenspielBoardState) bs_temp0.clone();
				bs_temp1.move(move1);
				for(BohnenspielMove move2 : bs_temp1.getLegalMoves()){
					BohnenspielBoardState bs_temp2 = (BohnenspielBoardState) bs_temp1.clone();
					bs_temp2.move(move2);
					for(BohnenspielMove move3 : bs_temp2.getLegalMoves()){
						BohnenspielBoardState bs_temp3 = (BohnenspielBoardState) bs_temp2.clone();
						bs_temp3.move(move3);
						for(BohnenspielMove move4 : bs_temp3.getLegalMoves()){
							BohnenspielBoardState bs_temp4 = (BohnenspielBoardState) bs_temp3.clone();
							bs_temp4.move(move4);
							BohnenspielMove bestMove = AlphaBeta.alpha_beta(bs_temp4, max_search_depth - 5, 10000, false, false, false, 0, false, null, null, map);
							map.putIfAbsent(bs_temp4.toString(), bestMove.toTransportable());	
						}
						BohnenspielMove bestMove = AlphaBeta.alpha_beta(bs_temp3, max_search_depth - 4, 10000, false, false, false, 0, false, null, null, map);
						map.putIfAbsent(bs_temp3.toString(), bestMove.toTransportable());	
						System.out.println("Finished one iteration of 4th loop");
					}

					BohnenspielMove bestMove = AlphaBeta.alpha_beta(bs_temp2, max_search_depth - 3, 10000, false, false, false, 0, false, null, null, map);
					map.putIfAbsent(bs_temp2.toString(), bestMove.toTransportable());
					System.out.println("Finished one iteration of 3rd loop");

				}
				BohnenspielMove bestMove = AlphaBeta.alpha_beta(bs_temp1, max_search_depth - 2, 10000, false, false, false, 0, false, null, null, map);
				map.putIfAbsent(bs_temp1.toString(), bestMove.toTransportable());	
				System.out.println("Finished one iteration of 2nd loop");

			}
			BohnenspielMove bestMove = AlphaBeta.alpha_beta(bs_temp0, max_search_depth - 1, 10000, false, false, false, 0, false, null, null, map);
			map.putIfAbsent(bs_temp0.toString(), bestMove.toTransportable());	
			System.out.println("Finished one iteration of outer loop");

		}

		BohnenspielBoardState bs_temp = (BohnenspielBoardState) bs.clone();
		BohnenspielMove bestMove = AlphaBeta.alpha_beta(bs, max_search_depth, 10000, false, false, false, 0, false, null, null, map);
		map.putIfAbsent(bs_temp.toString(), bestMove.toTransportable());	
		return map;

	}

	public static void serializeMap(HashMap<String, String> map, String name){
		System.out.println("Map Size: " + map.size());

		try
		{
			FileOutputStream fos =
					new FileOutputStream(name);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(map);
			oos.close();
			fos.close();
		}catch(IOException ioe)
		{
			System.out.printf("Map Serialization Failed");

			ioe.printStackTrace();
		}
	}
}
