package bots.demobots;

import com.biotools.meerkat.*;
import com.biotools.meerkat.util.Preferences;

/**
 * @author oursland
 *         <p/>
 *         See <a href="http://oursland.net/projects/pabots">Oursland</a>
 */
public class FlockBot implements Player {

    private Card card1 = null;
    private Card card2 = null;
    private int seat = -1;
    private GameInfo game = null;

    /**
     * Run once to set up this bot.
     */
    public void init(Preferences prefs) {
    }

    /**
     * Receive hole cards
     */
    public void holeCards(Card card1, Card card2, int seat) {
        this.card1 = card1;
        this.card2 = card2;
        this.seat = seat;
    }

    public Action getAction() {
        Action rc = null;
        if (game.isPreFlop()) {
            // preflop: always call
            rc = alwaysCall();
        } else if (game.isFlop()) {
            // flop: always call
            rc = alwaysCall();
        } else if (game.isTurn()) {
            // turn: always call
            rc = alwaysCall();
        } else if (game.isRiver()) {
            // river: play based on hand eval % and # of players
            int players = game.getNumActivePlayers();
            double strength = HandEvaluator.handRank(card1, card2, game.getBoard(), players - 1);
            double target = 1.0 - 1.0 / players;
            double toCall = game.getAmountToCall(seat);
            if (toCall > 0) {
                if (strength > target) {
                    rc = Action.callAction(toCall);
                } else {
                    rc = Action.foldAction(game);
                }
            } else {
                if (strength > target) {
                    rc = Action.raiseAction(game, game.getTotalPotSize() / 2);
                } else {
                    rc = Action.checkAction();
                }
            }
        }
        return rc;
    }

    private Action alwaysCall() {
        Action rc;
        double toCall = game.getAmountToCall(seat);
        if (toCall > 0) {
            rc = Action.callAction(toCall);
        } else {
            rc = Action.checkAction();
        }
        return rc;
    }

    public void actionEvent(int arg0, Action arg1) {
        // TODO Auto-generated method stub

    }

    public void stageEvent(int arg0) {
        // TODO Auto-generated method stub

    }

    public void showdownEvent(int arg0, Card arg1, Card arg2) {
        // TODO Auto-generated method stub

    }

    public void gameStartEvent(GameInfo game) {
        this.game = game;
    }

    public void dealHoleCardsEvent() {
        // TODO Auto-generated method stub

    }

    public void gameOverEvent() {
        // TODO Auto-generated method stub

    }

    public void winEvent(int arg0, double arg1, String arg2) {
        // TODO Auto-generated method stub

    }

    public void gameStateChanged() {
        // TODO Auto-generated method stub

    }
}