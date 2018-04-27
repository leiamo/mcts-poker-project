package projectbot.gamestate;

import projectbot.enums.ActionEnum;
import projectbot.enums.RoundEnum;

public class RowAction {
    public double currentPotSize;
    public boolean isEnemyTurn;
    public double betSize;

    public RoundEnum round;
    // 1 = Pre-flop, 2 = Flop, 3 = Turn, 4 = River

    public ActionEnum action;
    // 1 = Fold, 2 = Check, 3 = Call, 4 = Bet, 5 = Raise

    public RowAction(boolean isEnemyTurn, RoundEnum round, ActionEnum action, double betSize, double currentPotSize){
        this.isEnemyTurn = isEnemyTurn;
        this.round = round;
        this.action = action;
        this.betSize = betSize;
        this.currentPotSize = currentPotSize;
    }

}
