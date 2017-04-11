package student_player;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import boardgame.Player;
import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;
import bohnenspiel.BohnenspielPlayer;
import student_player.mytools.AlphaBeta;
import student_player.mytools.MiniMax;

public class StudentMiniMax extends BohnenspielPlayer{

    public StudentMiniMax() { super("MiniMax"); }
    
	static HashMap<String, Integer> max_map = null;
	static HashMap<String, Integer> min_map = null;
	static HashMap<String, String> move_map = null;

    public BohnenspielMove chooseMove(BohnenspielBoardState board_state)
    {

//		long startTime = System.nanoTime();
		int timeout = boardgame.Server.DEFAULT_TIMEOUT;
		
        BohnenspielBoardState cloned_board_state = (BohnenspielBoardState) board_state.clone();

		if(board_state.getTurnNumber() == 0){
			timeout = boardgame.Server.FIRST_MOVE_TIMEOUT;
		}
//        BohnenspielMove move = MiniMax.minimax(cloned_board_state, 0, 6, false, false, false, null, null);
		BohnenspielMove move = AlphaBeta.alpha_beta(cloned_board_state, 10, timeout, false, false, false, 0, false, null, null, null);

//		long endTime = System.nanoTime();
//		System.out.println("Move computation time: " + (endTime - startTime)/1000000 + " ms");
		
        return move;
    }
}