package bots.smarterbot;

import java.util.LinkedList;

import com.biotools.meerkat.Action;
import com.biotools.meerkat.Card;
import com.biotools.meerkat.GameInfo;
import com.biotools.meerkat.HandEvaluator;
import com.biotools.meerkat.Holdem;
import com.biotools.meerkat.Player;
import com.biotools.meerkat.PlayerInfo;
import com.biotools.meerkat.util.Preferences;

/**
 * A model based/learning agent. Smarter Bot keeps track of data for each player. It uses this data to determine
 * the best course of action for the players that are currently in the hand. The main factor for determining if
 * Smarter Bot should bet or check is by calculating the pot odds. Another main statistic that Smarter Bot will track
 * is each player's VPIP (Voluntarily put money in pot.) This will help Smarter Bot determine the aggressiveness of
 * opponents and allow it to shift its play style accordingly.<p>
 * <p/>
 * <strong>Pre-Flop</strong> - Smarter Bot divides all the possible pre-flop hand combinations into 6 different
 * rankings. It determines which ranking to use for its particular hand via a dynamic look-up table. The hand
 * combinations are assigned rankings via a learning function. For each hand, the bot determines how well it played
 * that hand via a fitness function and updates the table accordingly. Smarter Bot actually has multiple independent
 * look-up tables depending on what play style it is using: aggressive, passive, and neutral. Each table learns
 * independently from the other ones and has its own associated fitness function.<p>
 * <p/>
 * <strong>Post-Flop</strong> - Smarter Bot uses the Hand Evaluator to determine the strength of its hand.
 *
 * @see <a href="https://wiki.csc.calpoly.edu/CPE-480-F10-13/wiki">Poker Face Project</a>
 */
public class SmarterBot implements Player {

    private int ourSeat; // our seat for the current hand
    private Card c1, c2; // our hole cards
    private GameInfo gi; // general game information
    private Preferences prefs; // the configuration options for this bot

    private boolean didRaise; // did we raise preflop?
    private bots.smarterbot.Action preflopLevel; // what strategy will we be using for this hand
    private LinkedList<PlayerStats> stats; //list of all players histories
    private History history;
    private double prePotSize; // size of the pot when we limped or called (counting amount we put in)
    private boolean limped; // did we successfully limp preflop
    private boolean called; // did we successfully call preflop
    private int timesLimped; // times successfully limped preflop with a limp category hand
    private int timesCalled; // times successfully called preflop with a call category hand
    private double profitLimp, profitCall, stackSize; // total percent of preflop pot won when limped/called successfully
    private int lastRaiser;
    private int cBetter;
    private int[][] bestHands;

    private enum stance {
        DEFENSIVE, AGRESSIVE, NEUTRAL
    }

    PlayMatrix neutralTable;
    PlayMatrix curTable;

    public SmarterBot() {
        neutralTable = new PlayMatrix();

        curTable = neutralTable;

        didRaise = false;
        limped = called = false;
        timesLimped = timesCalled = 0;
        prePotSize = stackSize = 0;
        lastRaiser = -1;
        cBetter = -1;
        preflopLevel = bots.smarterbot.Action.INVALID;
        stats = new LinkedList<PlayerStats>();
        history = new History();

        bestHands = new int[25][2];
        //66, A6, K8, Q8, J8, T8
        bestHands[0][0] = 12;
        bestHands[0][1] = 12;//AA
        bestHands[1][0] = 11;
        bestHands[1][1] = 11;//KK
        bestHands[2][0] = 12;
        bestHands[2][1] = 11;//AK
        bestHands[3][0] = 10;
        bestHands[3][1] = 10;//QQ
        bestHands[4][0] = 9;
        bestHands[4][1] = 9;//JJ
        bestHands[5][0] = 12;
        bestHands[5][1] = 10;//AQ
        bestHands[6][0] = 8;
        bestHands[6][1] = 8;//TT
        bestHands[7][0] = 12;
        bestHands[7][1] = 9;//AJ
        bestHands[8][0] = 7;
        bestHands[8][1] = 7;//99
        bestHands[9][0] = 11;
        bestHands[9][1] = 10;//KQ
        bestHands[10][0] = 6;
        bestHands[10][1] = 6;//88
        bestHands[11][0] = 12;
        bestHands[11][1] = 8;//AT
        bestHands[12][0] = 11;
        bestHands[12][1] = 9;//KJ
        bestHands[13][0] = 10;
        bestHands[13][1] = 9;//QJ
        bestHands[14][0] = 11;
        bestHands[14][1] = 8;//KT
        bestHands[15][0] = 12;
        bestHands[15][1] = 7;//A9
        bestHands[16][0] = 9;
        bestHands[16][1] = 8;//JT
        bestHands[17][0] = 10;
        bestHands[17][1] = 8;//QT
        bestHands[18][0] = 8;
        bestHands[18][1] = 7;//T9
        bestHands[19][0] = 11;
        bestHands[19][1] = 7;//K9
        bestHands[20][0] = 12;
        bestHands[20][1] = 6;//A8
        bestHands[21][0] = 5;
        bestHands[21][1] = 5;//77
        bestHands[22][0] = 10;
        bestHands[22][1] = 7;//Q9
        bestHands[23][0] = 9;
        bestHands[23][1] = 7;//J9
        bestHands[24][0] = 12;
        bestHands[24][1] = 5;//A7

    }

    /**
     * An event called to tell us our hole cards and seat number
     *
     * @param c1   your first hole card
     * @param c2   your second hole card
     * @param seat your seat number at the table
     */
    public void holeCards(Card c1, Card c2, int seat) {
        this.c1 = c1;
        this.c2 = c2;
        this.ourSeat = seat;
        System.out.println("\tEnemy's hand: [" + c1 + "-" + c2 + "]");
        preflopLevel = curTable.getFlopAction(c1, c2);
        this.didRaise = false;
        this.limped = this.called = false;
        this.prePotSize = this.stackSize = 0;
        this.lastRaiser = -1;
        this.cBetter = -1;
    }

    /**
     * Requests an Action from the player
     * Called when it is the Player's turn to act.
     */
    public Action getAction() {

        if (gi.isPreFlop()) {
            return preFlopAction();
        } else {
            return postFlopAction();
        }
    }

    /**
     * Get the current settings for this bot.
     */
    public Preferences getPreferences() {
        return prefs;
    }

    /**
     * Load the current settings for this bot.
     */
    public void init(Preferences playerPrefs) {
        this.prefs = playerPrefs;
    }

    /**
     * @return true if debug mode is on.
     */
    public boolean getDebug() {
        return prefs.getBooleanPreference("DEBUG", false);
    }

    /**
     * print a debug statement.
     */
    public void debug(String str) {
        if (getDebug()) {
            System.out.println(str);
        }
    }

    /**
     * print a debug statement with no end of line character
     */
    public void debugb(String str) {
        if (getDebug()) {
            System.out.print(str);
        }
    }

    /**
     * A new betting round has started.
     */
    public void stageEvent(int stage) {
    }

    /**
     * A showdown has occurred.
     *
     * @param pos the position of the player showing
     * @param c1  the first hole card shown
     * @param c2  the second hole card shown
     */
    public void showdownEvent(int seat, Card c1, Card c2) {
    }

    /**
     * A new game has been started.
     *
     * @param gi the game stat information
     */
    public void gameStartEvent(GameInfo gInfo) {
        this.gi = gInfo;
        if (gi.getNumPlayers() < 5) {
            neutralTable.setDefaultMatrix();
        } else {
            neutralTable.setNewAggressiveMatrix();
        }
    }

    /**
     * An event sent when all players are being dealt their hole cards
     */
    public void dealHoleCardsEvent() {
    }

    /**
     * An action has been observed.
     * In this method, we mine for data about players.
     * A call bet or raise increases vpmip (voluntarily put money in the pot)
     * A bet or raise also increases af (aggression factor)
     * I AM ASSUMING THAT THE ACTTION HAS NOT PROCESSED YET AND THAT BB COUNTS AS A BET. CHECK THIS
     * IF SO THEN I NEEDTO CHANGE TO ASSUME THE ACTION DIDNT PROCESS YET
     */
    public void actionEvent(int pos, Action act) {

        PlayerStats player = null;
        int i;
        boolean preflop = gi.isPreFlop();
        if (act.getType() < 0 || act.getType() > 4) {
            return; // not a type we care about, so dont track it
        }
        for (i = 0; i < stats.size(); i++) {
            if (stats.get(i).seat == pos) {
                player = stats.get(i);
                break;
            }
        }
        lastRaiser = pos;
        if (player == null || !player.name.equals(gi.getPlayerName(pos))) { // if new name, then new player
            if (player != null) {
                stats.remove(i);
            }
            player = new PlayerStats(pos, gi.getPlayerName(pos));
            stats.add(player);
        }
        if (act.isBetOrRaise()) {
            player.preRaise++;
        }
        if (!act.isFold()) {
            player.prePutMoney++;
        }
        player.preHands++;
        if (preflop) {
            if (gi.getNumRaises() == 0 && act.getToCall() > 0) {
                player.preLimpPos++;
                if (act.isCall()) {
                    player.preLimp++;
                }
            }
            if (gi.getNumRaises() == 1) {
                player.preRespondTo2++;
            }
            if (gi.getNumRaises() == 2) {
                player.preRespondTo3++;
            }
            if (gi.getNumRaises() == 3) {
                player.preRespondTo4++;
            }
            if (gi.getNumRaises() == 1 && act.isBetOrRaise()) {
                player.pre3Bet++;
            }
            if (gi.getNumRaises() == 2 && act.isBetOrRaise()) {
                player.pre4Bet++;
            }
            if (gi.getNumRaises() == 2 && act.isFold()) {
                player.preFoldTo3++;
            }
            if (gi.getNumRaises() == 3 && act.isFold()) {
                player.preFoldTo4++;
            }
            if (gi.getNumRaises() >= 1 && act.getToCall() == gi.getCurrentBetSize()) {
                player.preColdCallPos++;
                if (act.isCall()) {
                    player.preColdCall++;
                }
            }
            if (gi.getNumRaises() == 3 && act.isCall()) {
                player.preCall4++;
            }
        } else { // for post flop stats (none right now)
            if (gi.getStage() == Holdem.FLOP) { // is it the flop?
                if (pos == lastRaiser && gi.getNumRaises() == 0) { // no bets yet and the action is on last raiser
                    player.cBetPos++;
                    if (act.isBet()) {
                        player.cBet++;
                        this.cBetter = pos;
                    }
                } else if (pos == cBetter) { // else if cBetter was raised
                    player.cBetRaised++;
                    if (act.isFold()) {
                        player.foldToCBetRaise++;
                    }
                }
            }
        }
    }

    /**
     * The game info state has been updated
     * Called after an action event has been fully processed
     */
    public void gameStateChanged() {
    }

    /**
     * The hand is now over.
     */
    public void gameOverEvent() {

        double profit = gi.getBankRoll(ourSeat) - gi.getPlayer(ourSeat).getBankRollAtStartOfHand();
        bots.smarterbot.Action action = history.update(c1, c2, profit, preflopLevel.getValue(), curTable.getFlopAction(c1, c2).getValue());
        if (action.getValue() != Action.INVALID) {
            curTable.setFlopAction(c1, c2, action);
        }
        // now update limp / call % winnings
        if (this.called) {
            this.timesCalled++;
            this.profitCall += ((gi.getBankRoll(ourSeat) - this.stackSize) / this.prePotSize);
        }
        if (this.limped) {
            this.timesLimped++;
            this.profitLimp += ((gi.getBankRoll(ourSeat) - this.stackSize) / this.prePotSize);
        }
    }

    /**
     * A player at pos has won amount with the hand handName
     */
    public void winEvent(int pos, double amount, String handName) {
    }

    /**
     * Uses table to look up appropriate course of action for hole cards.
     */
    private Action preFlopAction() {
        double toCall = gi.getAmountToCall(ourSeat);

        //note the following action is our Action.class, not theirs
        bots.smarterbot.Action action = preflopLevel;

        if (action == bots.smarterbot.Action.FOLD) {
            if (toCall == 0) {
                return this.call(toCall);
            } else {
                return Action.foldAction(toCall);
            }
        } else if (action == bots.smarterbot.Action.LIMP) {
            if (toCall == 0) {
                this.limped = false;
                return this.call(toCall);
            } else if (gi.getNumRaises() != 0) { //does the BB count as a bet?
                this.limped = false;
                return Action.foldAction(toCall);
            } else { //no raises yet
                LinkedList<PlayerInfo> players = this.getPlayers();
                double noRaiseProb = 1; //probability of there being no raise if we call
                int player = -1; // variable representing seat of player in question
                PlayerStats stat; // our data on a certain player
                for (int i = 0; i < players.size(); i++) {
                    player = players.get(i).getSeat();
                    stat = this.find(stats, player);
                    if (stat.preHands > 0) {
                        noRaiseProb *= (1 - stat.af());
                    }
                }
                double percentExpected = 0.85; // how much of the present pot we expect to win if we limp
                if (this.timesLimped >= 5) {
                    percentExpected = this.profitLimp / (double) this.timesLimped;
                }
                if (noRaiseProb * percentExpected * (gi.getTotalPotSize() + toCall) >= toCall) {
                    Action act = this.call(toCall);
                    if (act == Action.callAction(toCall)) {
                        this.limped = true;
                        this.prePotSize = gi.getTotalPotSize() + toCall;
                        this.stackSize = gi.getPlayer(ourSeat).getBankRoll() - toCall;
                    }
                    return act;
                } else {
                    this.limped = false;
                    return Action.foldAction(toCall);
                }
            }
        } else if (action == bots.smarterbot.Action.CALL) {
            if (toCall == 0) { // we are BB, and no raise
                this.called = false;
                return Action.callAction(toCall);
            } else if (gi.getNumRaises() > 1) { // More than one raise, so fold. does the BB count as a bet?
                this.called = false;
                return Action.foldAction(toCall);
            } else if (gi.getNumRaises() == 0) { // no raises, so call
                this.called = false;
                return this.call(toCall);
            } else { //there is just one raise
                if (toCall <= gi.getBigBlindSize() * 4) { // will call a raise <= 4x BB if its likely noone else will raise
                    LinkedList<PlayerInfo> players = this.getPlayers();
                    double noRaiseProb = 1; //probability of there being no raise if we call
                    double foldTo3BetProb = 1; // probability of everyone folding if we 3bet
                    int player = -1; // variable representing seat of player in question
                    PlayerStats stat; // our data on a certain player
                    for (int i = 0; i < players.size(); i++) {
                        player = players.get(i).getSeat();
                        stat = this.find(stats, player);
                        if (stat.preRespondTo2 > 0) {
                            noRaiseProb *= (1 - stat.percent3Bet());
                        }
                        if (stat.preRespondTo3 > 0) {
                            foldTo3BetProb *= stat.foldTo3Bet();
                        } else if (stat.preHands > 0) {
                            foldTo3BetProb *= 1 - (stat.vpmip());
                        }
                    }
                    double percentExpected = 0.85;
                    if (this.timesCalled >= 5) {
                        percentExpected = this.profitCall / (double) this.timesCalled;
                    }
                    double amountGainedRaising = foldTo3BetProb * gi.getTotalPotSize() - (1 - foldTo3BetProb) * toCall;
                    double amountGainedCalling = ((gi.getTotalPotSize() + toCall) * percentExpected * noRaiseProb) - toCall;
                    if (amountGainedRaising > 0 && amountGainedRaising > amountGainedCalling) {
                        this.called = false;
                        return this.raise(toCall);
                    } else if (amountGainedCalling > 0) {
                        Action act = this.call(toCall);
                        if (act == Action.callAction(toCall)) {
                            this.called = true;
                            this.prePotSize = gi.getTotalPotSize();
                        }
                        return act;
                    }
                    this.called = false;
                    return Action.foldAction(toCall);
                } else { // fold if the raise is too big (like to 9$ for a 2$ BB
                    this.called = false;
                    return Action.foldAction(toCall);
                }
            }
        } else if (action == bots.smarterbot.Action.RAISE) {
            if (gi.getNumRaises() == 0) { //then raise!
                return this.raise(toCall);
            } else if (gi.getNumRaises() == 1) { //then call a raise
                if (toCall <= gi.getBigBlindSize() * 5) {
                    return this.call(toCall);
                } else {
                    return Action.foldAction(toCall);
                }
            } else if (gi.getNumRaises() >= 3) { // fold to 4 bets
                return Action.checkOrFoldAction(toCall);
            } else if (gi.getNumRaises() == 2 && this.didRaise) { // someone 3 bet our raise
                double handRange = 0;
                int i = 0;
                for (i = 0; i < 25 && handRange < find(this.stats, lastRaiser).percent3Bet(); i++) {
                    if (this.bestHands[i][0] == this.bestHands[i][1]) { //pocket pairs are 1 / 221
                        handRange += (1.0 / 221.0);
                    } else { // not a pocket pair is
                        handRange += (8.0 / 663.0);
                    }
                }
                double winChance = 0;
                for (int j = 0; j < i; j++) {
                    if (this.bestHands[j][0] == this.bestHands[j][1]) { //pocket pair is less likely
                        winChance += ((1.0 / 221.0) / handRange) * this.handMatcher(c1, c2, j);
                    } else { // not pair is more likely
                        winChance += ((8.0 / 663.0) / handRange) * this.handMatcher(c1, c2, j);
                    }
                }
                if (handRange < find(this.stats, lastRaiser).percent3Bet()) {
                    winChance += (find(this.stats, lastRaiser).percent3Bet() - handRange) * 0.75;
                }
                if (toCall <= (gi.getTotalPotSize() + toCall) * winChance) {
                    return this.call(toCall);
                }
            } else {
                return Action.checkOrFoldAction(toCall);
            }
        } else if (action == bots.smarterbot.Action.RERAISE) {
            if (gi.getNumRaises() <= 1) { //no raise or one raise so far
                this.didRaise = true;
                return this.raise(toCall);
            } else if (gi.getNumRaises() == 2) { // there is a 3 bet
                // code copied from raise for considering the call
                double handRange = 0;
                int i = 0;
                for (i = 0; i < 25 && handRange < find(this.stats, lastRaiser).percent3Bet(); i++) {
                    if (this.bestHands[i][0] == this.bestHands[i][1]) { //pocket pairs are 1 / 221
                        handRange += (1.0 / 221.0);
                    } else { // not a pocket pair is
                        handRange += (8.0 / 663.0);
                    }
                }
                double winChance = 0;
                for (int j = 0; j < i; j++) {
                    if (this.bestHands[j][0] == this.bestHands[j][1]) { //pocket pair is less likely
                        winChance += ((1.0 / 221.0) / handRange) * this.handMatcher(c1, c2, j);
                    } else { // not pair is more likely
                        winChance += ((8.0 / 663.0) / handRange) * this.handMatcher(c1, c2, j);
                    }
                }
                if (handRange < find(this.stats, lastRaiser).percent3Bet()) {
                    winChance += (find(this.stats, lastRaiser).percent3Bet() - handRange) * 0.85;
                }
                double profitCall = ((gi.getTotalPotSize() + toCall) * winChance) - toCall;
                LinkedList<PlayerInfo> players = this.getPlayers();
                double noRaiseProb = 1; //probability of there being no raise if we call
                double foldTo4BetProb = 1; // probability of everyone folding if we 4bet
                int player = -1; // variable representing seat of player in question
                PlayerStats stat; // our data on a certain player
                double call4Bet = 1; //prob of someone calling
                for (int k = 0; k < players.size(); k++) {
                    player = players.get(k).getSeat();
                    stat = this.find(stats, player);
                    if (stat.preRespondTo2 > 0) {
                        noRaiseProb *= (1 - stat.percent4Bet());
                    }
                    if (stat.preRespondTo4 > 0) {
                        foldTo4BetProb *= stat.foldTo4Bet();
                        call4Bet *= stat.call4Bet();
                    } else if (stat.preHands > 0) {
                        foldTo4BetProb *= 1 - (stat.vpmip());
                    }
                }
                call4Bet = 1 - call4Bet;
                //how much we gain bt raising == size of pot * chance of everyone folding - cost to raise
                double profitRaise = ((this.raise(toCall).getAmount() + gi.getTotalPotSize()) * foldTo4BetProb) - this.raise(toCall).getAmount();
                //also we gain the cost of a call + chance of win
                profitRaise = profitRaise + (winChance * call4Bet * ((this.raise(toCall).getAmount() * 2) + gi.getTotalPotSize()));
                if (profitRaise >= profitCall && profitRaise >= 0) {
                    return this.raise(toCall);
                } else if (profitCall >= 0) {
                    return this.call(toCall);
                } else {
                    return Action.checkOrFoldAction(toCall);
                }
            }
        } else if (action == bots.smarterbot.Action.PREMIUM) {
            return this.raise(toCall);
        }
        // should be unreachable
        return Action.checkOrFoldAction(toCall);

    }

    /**
     * Calls on hands with 50% > x <= 70% of winning
     * Raise/Bet on hands with x > 70% chance of winning
     * Goes all in if it knows it has the best hand
     * Fold all other hands
     */
    private Action postFlopAction() {
        double toCall = gi.getAmountToCall(ourSeat);
        int numPlayers = gi.getNumActivePlayers() - 1;
        double potSize = gi.getTotalPotSize();
        double handRank = HandEvaluator.handRank(c1, c2, gi.getBoard(), numPlayers);
        LinkedList<PlayerInfo> players = this.getPlayers();
        PlayerStats player;

        //first we consider a c bet before anything else
        if (numPlayers <= 3 && this.didRaise && gi.getNumRaises() == 0 && gi.getStage() == Holdem.FLOP) { // c bet
            return Action.betAction(potSize * 0.5 + (0.1 * numPlayers));
        }

        //what if someone has cBet? consider raising them off the hand
        if (gi.getNumRaises() == 1 && this.cBetter == this.lastRaiser && gi.getStage() == Holdem.FLOP) {
            player = find(stats, cBetter);
            boolean considerRaise = true;
            for (PlayerInfo listPlayer : players) {
                if (listPlayer.getAmountInPotThisRound() != 0 && listPlayer.getSeat() != this.cBetter) {
                    considerRaise = false;
                    break;
                }
            }
            if (considerRaise) {
                if (player.cBetRaised != 0) {
                    double raiseValue = player.foldToCBetRaise();
                    raiseValue = (raiseValue * (gi.getTotalPotSize() + this.raise(toCall).getAmount())) - this.raise(toCall).getAmount();
                    if (raiseValue > 0) {
                        return this.raise(toCall);
                    }
                }
            }
        }

        // now consider a bet  if no one has raised based on chance of them folding
        if (gi.getStage() == Holdem.PREFLOP && gi.getNumRaises() == 0) {
            double probFold = 1;
            for (PlayerInfo stat : players) {
                player = find(stats, stat.getSeat());
                if (player.preHands != 0) {
                    probFold *= (1 - player.vpmip());
                }
            }
            if ((probFold * (gi.getTotalPotSize() + this.raise(toCall).getAmount())) - this.raise(toCall).getAmount() > 0) {
                return this.raise(toCall);
            }
        }
        // now consider a call with a draw
        boolean flushDraw = false;
        if (c1.getSuit() == c2.getSuit()) {
            int suited = 0;
            for (int i = 0; i < gi.getBoard().size(); i++) {
                if (gi.getBoard().getCard(i).getSuit() == c1.getSuit()) {
                    suited++;
                }
            }
            if (suited == 2) {
                flushDraw = true;
            }
        }
        if (gi.getNumRaises() > 0 && flushDraw) {
            if ((9.0 / (double) (50 - gi.getBoard().size())) * ((gi.getTotalPotSize() + toCall) * 1.25) - toCall > 0) {
                return this.call(toCall);
            }
        }

        if (handRank >= 0.85) {
            return this.raise(toCall);
        } else if (handRank > 0.75) {
            return this.call(toCall);
        } else {
            return Action.checkOrFoldAction(toCall);
        }
    }

    private Action call(double toCall) { //Aaron's
        if (toCall >= gi.getPlayer(ourSeat).getBankRoll() / 3) {
            this.didRaise = true;
            return Action.raiseAction(toCall, gi.getPlayer(ourSeat).getBankRoll());
        } else {
            this.didRaise = false;
            return Action.callAction(toCall);
        }
    }

    private Action raise(double toCall) { //Aaron's
        return Action.raiseAction(toCall, gi.getMinRaise());
//
//        this.didRaise = true;
//        LinkedList<PlayerInfo> list = new LinkedList<PlayerInfo>();
//        double largestStack = 0;
//        list = getPlayers();
//        for (int i = 0; i < list.size(); i++) {
//            if (list.get(i).getBankRoll() > largestStack) {
//                largestStack = list.get(i).getBankRoll();
//            }
//        }
//        double raiseTo = 0;
//        if (gi.getStage() != Holdem.PREFLOP) {
//            if (gi.getNumRaises() == 0) {
//                raiseTo = gi.getTotalPotSize() * (0.5 + (gi.getNumActivePlayersNotAllIn() * 0.1));
//            } else {
//                if (gi.getTotalPotSize() * (0.5 + (gi.getNumActivePlayersNotAllIn() * 0.1)) > toCall * 2.5) {
//                    raiseTo = gi.getTotalPotSize() * (0.5 + (gi.getNumActivePlayersNotAllIn() * 0.1));
//                } else {
//                    raiseTo = toCall * 2.5;
//                }
//            }
//        } else if (gi.getNumRaises() == 0) { //preflop
//            raiseTo = gi.getBigBlindSize() * 4;
//        } else { // preflop with a raise already out
//            raiseTo = gi.getTotalPotSize() * 2;
//        }
//        if (raiseTo > gi.getPlayer(ourSeat).getBankRoll() / 2 || raiseTo > largestStack / 2) {
//            return Action.raiseAction(toCall, gi.getPlayer(ourSeat).getBankRoll());
//        } else {
//            return Action.raiseAction(toCall, raiseTo);
//        }
    }

    private LinkedList<PlayerInfo> getPlayers() {

        LinkedList<PlayerInfo> list = new LinkedList<PlayerInfo>();
        int player = this.ourSeat;
        player = gi.nextActivePlayer(player);
        while (player != this.ourSeat) {
            list.add(gi.getPlayer(player));
            player = gi.nextActivePlayer(player);
        }
        return list;
    }

    /**
     * @author nalemromandi
     *         Tracks all the stats about players.
     */
    private class PlayerStats {

        int seat; // the seat the player is sitting at
        String name; // the name of the player

        int preHands, preRaise, prePutMoney;
        int pre3Bet, pre4Bet;
        int preRespondTo2, preRespondTo3, preRespondTo4;
        int preColdCall, preColdCallPos;
        int preLimp, preLimpPos;
        int preFoldTo3, preFoldTo4;
        int preCall4;
        int cBet, cBetPos;
        int foldToCBetRaise, cBetRaised;

        public PlayerStats(int seat, String name) {

            this.seat = seat;
            this.name = name;

            preHands = preRaise = prePutMoney = 0;
            pre3Bet = pre4Bet = 0;
            preRespondTo2 = preRespondTo3 = preRespondTo4 = 0;
            preColdCall = preColdCallPos = 0;
            preLimp = preLimpPos = 0;
            preFoldTo3 = preFoldTo4 = 0;
            preCall4 = 0;
            cBet = cBetPos = 0;
            foldToCBetRaise = cBetRaised = 0;
        }

        public double vpmip() {
            if (preHands == 0) {
                return -1;
            }
            return (double) prePutMoney / (double) preHands;
        }

        public double af() {
            if (preHands == 0) {
                return -1;
            }
            return (double) preRaise / (double) preHands;
        }

        public double limp() {
            if (preLimpPos == 0) {
                return -1;
            }
            return (double) preLimp / (double) preLimpPos;
        }

        public double coldCall() {
            if (preColdCallPos == 0) {
                return -1;
            }
            return (double) preColdCall / (double) preColdCallPos;
        }

        public double percent3Bet() {
            if (preRespondTo2 == 0) {
                return -1;
            }
            return (double) pre3Bet / (double) preRespondTo2;
        }

        public double percent4Bet() {
            if (preRespondTo3 == 0) {
                return -1;
            }
            return (double) pre4Bet / (double) preRespondTo3;
        }

        public double foldTo3Bet() {
            if (preRespondTo3 == 0) {
                return -1;
            }
            return (double) preFoldTo3 / (double) preRespondTo3;
        }

        public double foldTo4Bet() {
            if (preRespondTo4 == 0) {
                return -1;
            }
            return (double) preFoldTo4 / (double) preRespondTo4;
        }

        public double call3Bet() {
            if (preRespondTo3 == 0) {
                return -1;
            }
            return 1.0 - foldTo3Bet() - percent4Bet();
        }

        public double call4Bet() {
            if (preRespondTo4 == 0) {
                return -1;
            }
            return (double) preCall4 / (double) preRespondTo4;
        }

        public double cBetChance() {
            if (cBetPos == 0) {
                return -1;
            }
            return (double) cBet / cBetPos;
        }

        public double foldToCBetRaise() {
            if (cBetRaised == 0) {
                return -1;
            }
            return (double) foldToCBetRaise / (double) cBetRaised;
        }
    }

    private PlayerStats find(LinkedList<PlayerStats> list, int seat) {

        PlayerStats player = null;
        int i;
        for (i = 0; i < list.size(); i++) {
            if (seat == list.get(i).seat) {
                player = list.get(i);
                break;
            }
        }
        if (player == null || !gi.getPlayerName(seat).equals(player.name)) {
            if (player != null) {
                list.remove(i);
            }
            player = new PlayerStats(seat, gi.getPlayerName(seat));
            list.add(player);
        }
        return player;
    }

    private double handMatcher(Card c1, Card c2, int index) {

        boolean usPair = c1.getRank() == c2.getRank();
        int rank1 = c1.getRank(), rank2 = c2.getRank();
        if (rank1 < rank2) {
            int temp = rank1;
            rank1 = rank2;
            rank2 = temp;
        }
        int them1 = this.bestHands[index][0], them2 = this.bestHands[index][1];
        if (them1 < them2) {
            int temp = them1;
            them1 = them2;
            them2 = temp;
        }
        boolean themPair = this.bestHands[index][0] == this.bestHands[index][1];

        if (usPair && themPair) {
            if (rank1 > them1) {
                return 0.8;
            } else if (rank1 < them1) {
                return 0.2;
            } else {
                return 0.5;
            }
        } else if (usPair) {
            if (rank1 > them1 && rank1 > them2) {
                return 0.85;
            } else if ((rank1 > them1 && rank1 < them2) || (rank1 < them1 && rank1 > them2)) {
                return 0.7;
            } else if (rank1 < them1 && rank1 < them2) {
                return 0.55;
            } else if ((rank1 == them1 && rank1 > them2) || (rank1 == them2 && rank1 > them1)) {
                return 0.9;
            } else if ((rank1 == them1 && rank1 < them2) || (rank1 == them2 && rank1 < them1)) {
                return 0.7;
            }
        } else if (themPair) {
            if (them1 > rank1 && them1 > rank2) {
                return 0.15;
            } else if ((them1 > rank1 && them1 < rank2) || (them1 < rank1 && them1 > rank2)) {
                return 0.3;
            } else if (them1 < rank1 && them1 < rank2) {
                return 0.45;
            } else if ((them1 == rank1 && them1 > rank2) || (them1 == rank2 && them1 > rank1)) {
                return 0.1;
            } else if ((them1 == rank1 && them1 < rank2) || (them1 == rank2 && them1 < rank1)) {
                return 0.3;
            }
        } else { // no one has a pair
            if (rank2 > them1) {
                return 0.65;
            } else if (them2 > rank1) {
                return 0.35;
            } else if (rank1 > them1 && them1 > rank2 && rank2 > them2) {
                return 0.65;
            } else if (them1 > rank1 && rank1 > them2 && them2 > rank2) {
                return 0.35;
            } else if (rank1 > them1 && them2 > rank2) {
                return 0.6;
            } else if (them1 > rank1 && rank2 > them2) {
                return 0.4;
            } else if (rank2 == them1) {
                return 0.75;
            } else if (them2 == rank1) {
                return 0.25;
            }
        }
        return 0.5; //should never happen
    }
}