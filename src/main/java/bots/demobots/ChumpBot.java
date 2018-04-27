package bots.demobots;

import com.biotools.meerkat.*;
import com.biotools.meerkat.util.Preferences;

import java.util.Random;

/**
 * @author oursland
 *         <p/>
 *         See <a href="http://oursland.net/projects/pabots">Oursland</a>
 */
public class ChumpBot implements Player {
    //	private Preferences prefs = new Preferences();
    private GameInfo gameInfo = null;
    private Card c1 = new Card();
    private Card c2 = new Card();
    private int seat;
    private int stageCount = 0;

    private double basicRaise = 0.0;
    private HandEvaluator he = new HandEvaluator();
    private Random random = new Random();
    private double fear = 0.0;

    private static final double[] BRAVERY = {0.60, 0.75, 0.82, 0.88, 0.9, 0.92, 0.98, 0.995, 1.0};
    private static final double[] BOLDNESS = {1.0, 1.2, 1.3, 1.35, 1.4, 1.45, 1.5, 1.7, 2.0};
    private double bravery = 0.9;
    private double boldness = 1.4;

    public void holeCards(Card c1, Card c2, int seat) {
        this.c1 = c1;
        this.c2 = c2;
        this.seat = seat;
    }

    public Action getAction() {
        double callAmount = gameInfo.getAmountToCall(seat);
        Action rc = Action.callAction(callAmount);
        switch (gameInfo.getStage()) {
            case Holdem.PREFLOP: {
                rc = preFlopAction(stageCount, rc);
            }
            break;
            case Holdem.FLOP: {
                rc = postFlopAction(0.5, 0.8, stageCount, rc);
            }
            break;
            case Holdem.TURN: {
                rc = postFlopAction(0.2, 0.8, stageCount, rc);
            }
            break;
            case Holdem.RIVER: {
                rc = postFlopAction(0.1, 0.8, stageCount, rc);
            }
            break;
            default: {
                throw new Error();
            }
        }
        stageCount++;
        return rc;
    }

    public void init(Preferences prefs) {
        // TODO Initialize your player from the given preferences file.
//		this.prefs = prefs;
        this.basicRaise = 0.0;
        setPlayStyle();
    }

    private void setPlayStyle() {
        this.bravery = BRAVERY[random.nextInt(BRAVERY.length)];
        this.boldness = BOLDNESS[random.nextInt(BOLDNESS.length)];
    }

    public void gameStartEvent(GameInfo gameInfo) {
        // The hand is starting
        this.gameInfo = gameInfo;
        // initialize betting structure based on the starting chip amount
        if (this.basicRaise == 0.0) {
            this.basicRaise = gameInfo.getBankRoll(gameInfo.getCurrentPlayerSeat()) / 40;
        }
        this.fear = 0.0;
    }

    private Action postFlopAction(double strength, double bluff, int stageCount, Action defaultAction) {
        double toCall = gameInfo.getAmountToCall(seat);
        updateFear(toCall);
        strength = Math.pow(strength, Math.pow(bravery, fear + stageCount * 5));
        bluff = Math.pow(bluff, Math.pow(boldness, fear + stageCount * 5));
        double handRank = HandEvaluator.handRank(c1, c2, gameInfo.getBoard());
        double bluffChoice = random.nextDouble();
        if (stageCount < 2) {
            if (handRank > 0.95 + 0.2 * strength) {
                return safeRaise(toCall, 20);
            } else if (handRank > 0.9 + 0.2 * strength) {
                return safeRaise(toCall, 10);
            } else if (handRank > 0.8 + 0.2 * strength) {
                return safeRaise(toCall, 5);
            } else if (handRank > 0.6 + 0.4 * strength) {
                return safeRaise(toCall, 4);
            } else if (handRank > 0.4 + 0.6 * strength) {
                return safeRaise(toCall, 3);
            } else if (handRank > 0.2 + 0.8 * strength) {
                return safeRaise(toCall, 2);
            } else if (handRank > strength) {
                return safeRaise(toCall, 1);
            } else if (bluffChoice < Math.pow(bluff, 12)) {
                return Action.raiseAction(toCall, 5 * basicRaise);
            } else if (bluffChoice < Math.pow(bluff, 10)) {
                return Action.raiseAction(toCall, 4 * basicRaise);
            } else if (bluffChoice < Math.pow(bluff, 8)) {
                return Action.raiseAction(toCall, 3 * basicRaise);
            } else if (bluffChoice < Math.pow(bluff, 6)) {
                return Action.raiseAction(toCall, 2 * basicRaise);
            } else if (bluffChoice < Math.pow(bluff, 4)) {
                return Action.raiseAction(toCall, 1 * basicRaise);
            } else if (toCall > 0.0 && bluffChoice > Math.pow(bluff, 1)) {
                return Action.foldAction(toCall);
            }
        }
        return defaultAction;
    }

    private Action safeRaise(double toCall, double mult) {
        int mySeat = gameInfo.getCurrentPlayerSeat();
        boolean canRaise = gameInfo.canRaise(mySeat);
        double minRaise = gameInfo.getMinRaise();

        mult -= mult * random.nextGaussian() / 4;
        if (mult < 0) {
            mult = 0;
        }
        double raise = mult * basicRaise - toCall;
        if (raise > 0.0 && raise > minRaise && canRaise) {
            raise = Math.min(raise, gameInfo.getBankRoll(mySeat));
            return Action.raiseAction(toCall, raise);
        } else {
            return Action.callAction(toCall);
        }
    }

    private void updateFear(double toCall) {
        double tightenUp = toCall / basicRaise;
        while (tightenUp > 3) {
            fear += 1;
            tightenUp /= 1.5;
        }
    }

    public Action preFlopAction(int stageCount, Action defaultAction) {
        double toCall = gameInfo.getAmountToCall(seat);
        updateFear(toCall);
        if (stageCount < 2) {
            boolean fold = true;
            fold &= c1.getRank() < Card.TEN;
            fold &= c1.getRank() < Card.TEN;
            fold &= isHoleStraight();
            fold &= isHoleFlush();
            fold &= toCall > 0.0;
            if (fold) {
                return Action.foldAction(0.0);
            }
            int raiseCount = 0;
            if (c1.getRank() == c2.getRank()) {
                raiseCount += Math.ceil(Math.sqrt(c1.getRank()));
            }
            if (isHoleStraight() && isHoleFlush()) {
                raiseCount += 1;
            }
            if (raiseCount > 0) {
                return Action.raiseAction(toCall, raiseCount * basicRaise);
            }
        }
        return defaultAction;
    }

    private boolean isHoleFlush() {
        return c1.getSuit() != c2.getSuit();
    }

    private boolean isHoleStraight() {
        return Math.abs(c1.getRank() - c2.getRank()) > 3;
    }

    public void actionEvent(int pos, Action action) {
        // TODO A player can override this method to recieve events for each action made by a player.
    }

    public void winEvent(int pos, double amount, String handName) {
        // TODO A player at pos has won amount with the hand handName
        if (pos != seat && random.nextDouble() < 0.1) {
            setPlayStyle();
        }
    }

    public void stageEvent(int stage) {
        // A new stage (betting round) has begun.
        stageCount = 0;
    }

    public void showdownEvent(int pos, Card c1, Card c2) {
        // Player pos has shown two cards.
    }

    public void gameOverEvent() {
        // The hand is now over.
    }

    public void gameStateChanged() {
        // The game info state has been updated  Called after an action event has been fully processed
    }

    public void dealHoleCardsEvent() {
        // TODO Auto-generated method stub

    }
}
