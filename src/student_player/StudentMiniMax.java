package student_player;

import boardgame.Player;
import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;
import bohnenspiel.BohnenspielPlayer;
import student_player.mytools.AlphaBeta;
import student_player.mytools.MiniMax;

public class StudentMiniMax extends BohnenspielPlayer{

    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public StudentMiniMax() { super("260566105"); }

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class
bohnenspiel.RandomPlayer
     * for another example agent. */
    public BohnenspielMove chooseMove(BohnenspielBoardState board_state)
    {

        BohnenspielBoardState cloned_board_state = (BohnenspielBoardState) board_state.clone();


        BohnenspielMove move = MiniMax.minimax(cloned_board_state, player_id);

        return move;
    }
}