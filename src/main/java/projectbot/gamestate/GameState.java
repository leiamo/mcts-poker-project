package projectbot.gamestate;

import com.biotools.meerkat.Card;
import com.biotools.meerkat.GameInfo;
import com.biotools.meerkat.Hand;
import projectbot.deck.Dealer;
import projectbot.deck.Deck;
import projectbot.enums.ActionEnum;
import projectbot.enums.RoundEnum;
import projectbot.opponentModelling.ClassifierHandler;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GameState {

    public double betLimit;
    public Hand boardCards;
    public Card card1, card2;
    public int mySeat, enemySeat, buttonSeat;
    public boolean isEnemyTurn;
    public LinkedList<RowAction> actions;
    public double potSize;
    public RoundEnum round;
    public Dealer dealer;
    public Deck deck;
    public String enemyName;
    public Double botWinProb;
    public Double aggression;
    public Double enemyWinProb = 0.5;
    public ClassifierHandler classifier;

    public GameState(Hand boardCards, Card card1, Card card2, int mySeat, int enemySeat, int buttonSeat,
                     boolean isEnemyTurn, LinkedList<RowAction> actions, double potSize, RoundEnum round,
                     Dealer dealer, String enemyName, Double botWinProb, ClassifierHandler classifierHandler,
                     double aggression) {
        this.boardCards = boardCards;
        this.card1 = card1;
        this.card2 = card2;
        this.mySeat = mySeat;
        this.enemySeat = enemySeat;
        this.buttonSeat = buttonSeat;
        this.isEnemyTurn = isEnemyTurn;
        this.actions = actions;
        this.potSize = potSize;
        this.round = round;
        this.dealer = dealer;
        this.deck = dealer.getDeck();
        this.enemyName = enemyName;
        this.botWinProb = botWinProb;
        this.classifier = classifierHandler;
        this.betLimit = getBetLimit(round);
        this.aggression = aggression;
    }

    public double getBetLimit(RoundEnum round) {
        if (round == RoundEnum.TURN || round == RoundEnum.RIVER) {
            return 4;
        }
        return 2;
    }

    // This is initialised at the start of choosing the next action
    public GameState(GameInfo gi, Card c1, Card c2, String playerName, LinkedList<RowAction> rowActions, Double botWinProb, ClassifierHandler classifierHandler, double aggression) {
        // runs at the start of each prediction / get next action
        this.classifier = classifierHandler;
        this.boardCards = gi.getBoard();
        this.card1 = c1;
        this.card2 = c2;
        this.mySeat = gi.getPlayerSeat(playerName);
        if (this.mySeat == 0) {
            this.enemySeat = 1;
            this.enemyName = gi.getPlayerName(1);
        } else {
            this.enemySeat = 0;
            this.enemyName = gi.getPlayerName(0);
        }
        this.dealer = new Dealer(c1, c2);
        this.deck = dealer.getDeck();
        this.buttonSeat = gi.getButtonSeat();
        this.potSize = gi.getTotalPotSize();
        this.round = RoundEnum.convert(gi.getStage() + 1);
        this.betLimit = gi.getMinRaise();
        this.botWinProb = botWinProb;
        this.aggression = aggression;
        actions = rowActions;

        // This will always initialise as false:
        this.isEnemyTurn = actions.getLast().isEnemyTurn;
    }

    public void nextRound() {
        if (round != RoundEnum.RIVER)
            round = RoundEnum.convert(round.getValue() + 1);
    }

    public boolean isEnemyTurn() {
        return isEnemyTurn;
    }

    public Card getCard1() {
        return card1;
    }

    public Card getCard2() {
        return card2;
    }

    public int getButtonSeat() {
        return buttonSeat;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public String getEnemyName() {
        return enemyName;
    }

    public Double getBotWinProb() {
        return botWinProb;
    }

    public Hand getBoardCards() {
        return boardCards;
    }

    public RoundEnum getRound() {
        return round;
    }

    public LinkedList<RowAction> getActions() {
        return actions;
    }

    public int getMySeat() {
        return mySeat;
    }

    public int getEnemySeat() {
        return enemySeat;
    }

    public double getPotSize() {
        return potSize;
    }

    // The game ends if:
    // 1) Last Action is FOLD (one player folds)
    // 2) Round is RIVER and both players check
    // 3) Round is RIVER and last player calls the other's bet
    public boolean isGameOver() {
        if (actions.isEmpty()) return false;
        ActionEnum lastAction = actions.getLast().action;

        // 1)
        if (lastAction == ActionEnum.FOLD) return true;
        else if (actions.size() == 1) return false;

        ActionEnum lastLastAction = actions.get(actions.size() - 2).action;
        RoundEnum lastLastActionRound = actions.get(actions.size() - 2).round;

        // 2) and 3)
        if (round == RoundEnum.RIVER) {
            if (lastLastActionRound.equals(RoundEnum.RIVER)) {
                if ((lastAction == ActionEnum.CHECK && lastLastAction == ActionEnum.CHECK) || (lastAction == ActionEnum.CALL)) {
                    return true;
                }
            }
        }
        return false;
    }

    // The winner is either:
    // 1) The last player standing
    // 2) The player with the best hand (randomise opponent cards for now)
    public double getGameResult() throws Exception {
        if (isGameOver()) {
            // 1) If enemy folds, then bot wins (positive outcome). Else, enemy wins (negative outcome).
            if (!actions.isEmpty() && actions.getLast().action == ActionEnum.FOLD) {
                if (actions.getLast().isEnemyTurn)
                    return potSize;
                else
                    // Reduce negative reward when enemy wins so that bot is less motivated to bluff
                    return -(potSize / 2);
            }
            // 2)
            ActionEnum actionEnum;
            if (actions.getLast().isEnemyTurn) {
                actionEnum = actions.getLast().action;
            } else {
                actionEnum = actions.get(actions.size() - 2).action;
            }
            // Underestimate aggression as this has a lower mean value in general
            // Bot will be more likely to play sensibly rather than bluff against a bluffing bot
            enemyWinProb = classifier.getOpponentWinProb(enemyName, round, enemySeat, actionEnum, potSize, aggression - 0.1);

            // if bot wins (positive outcome)
            // Assume bot will win if their perceived winning probability is considerably greater than the opponent
            // Predictions are not likely to be perfect, so this caters for over-confidence in play
            if (botWinProb - 0.1 > (enemyWinProb + 0.3))
                return (potSize);

            // if enemy wins (negative outcome)
            return -(potSize);
        }
        return 0;
    }

    public boolean isEndOfRound() {
        ActionEnum lastAction = actions.getLast().action;
        if (lastAction == ActionEnum.FOLD)
            return true;
        else if (lastAction == ActionEnum.CALL)
            return true;
        else if (actions.size() == 1)
            return false;
        ActionEnum lastLastAction = actions.get(actions.size() - 2).action;
        return (lastAction == ActionEnum.CHECK && lastLastAction == ActionEnum.CHECK);
    }

    // Gets all possible game states
    public LinkedList<GameState> getAllPossibleGameStates() {
        LinkedList<GameState> allPossibleStates = new LinkedList<>();
        LinkedList<RowAction> tempActions = new LinkedList<>();

        // Clone actions temporarily
        for (RowAction ra : actions) {
            tempActions.add(ra);
        }
        ActionEnum possibleAction;
        double possibleBetSize = 0;
        boolean isEndOfRound = false;
        ActionEnum lastAction;

        if (actions.size() == 2) {
            lastAction = ActionEnum.INVALID;
        } else {
            lastAction = actions.getLast().action;
            isEndOfRound = isEndOfRound();
        }

        Hand tempBoardCards = new Hand();
        for (int i = 0; i < boardCards.size(); i++) {
            tempBoardCards.addCard(boardCards.getCard(i));
        }

        // ---------------------------------------------------
        // Terminal Nodes: game is over
        if (isGameOver()) return null;

        // ---------------------------------------------------
        // Chance Nodes: Updates board cards via random selection
        else if (isEndOfRound) {
            nextRound();
            tempBoardCards = dealer.dealRandom(round, tempBoardCards, dealer.deck);
        }

        // ---------------------------------------------------
        // Opponent/Decision Nodes: Find all possible game states resulting from each possible action

        // if previous action is a check (or if new round and check/call)
        // then possible response actions are: check or bet
        if (lastAction == ActionEnum.CHECK || lastAction == ActionEnum.CALL) {
            // Loop through 2 iterations to changes last action in action list to check/bet action, getting every possibility
            // Add check action choice
            // Add bet action choice
            for (int i = 0; i < 2; i++) {
                if (i == 0)
                    possibleAction = ActionEnum.CHECK;
                else {
                    possibleAction = ActionEnum.BET;
                    possibleBetSize = getBetLimit(round);
                }
                tempActions = new LinkedList<>();
                for (RowAction ra : actions) {
                    tempActions.add(ra);
                }
                tempActions.add(new RowAction(!isEnemyTurn, round, possibleAction, possibleBetSize, potSize + possibleBetSize));
                allPossibleStates.add(new GameState(tempBoardCards, card1, card2, mySeat, enemySeat, buttonSeat, !isEnemyTurn, tempActions, potSize + possibleBetSize, round, dealer, enemyName, botWinProb, classifier, aggression));
            }
            return allPossibleStates;
        }

        // else if this is first action (small blind) or previous action is bet/raise
        // then possible response actions are: fold or call or raise
        else if (lastAction == ActionEnum.INVALID || lastAction == ActionEnum.BET || lastAction == ActionEnum.RAISE) {

            for (int i = 0; i < 3; i++) {
                if (i == 0) {
                    possibleAction = ActionEnum.FOLD;
                    possibleBetSize = 0;
                } else if (i == 1) {
                    possibleAction = ActionEnum.CALL;
                    possibleBetSize = 1;
                    if (lastAction != ActionEnum.INVALID)
                        possibleBetSize = getBetLimit(round);
                } else {
                    double previousBetSize = 1;
                    if (lastAction != ActionEnum.INVALID)
                        previousBetSize = actions.getLast().betSize;
                    possibleAction = ActionEnum.RAISE;     // raise action (in response to a limit bet)
                    possibleBetSize = getBetLimit(round) + previousBetSize;
                    // Bet size must be their uncalled bet plus the new bet amount
                    // This considers re-raising with previous bet size being total amount they have put in pot this round
                }

                tempActions = new LinkedList<>();
                for (RowAction ra : actions) {
                    tempActions.add(ra);
                }

                tempActions.add(new RowAction(!isEnemyTurn, round, possibleAction, possibleBetSize, potSize + possibleBetSize));
                allPossibleStates.add(new GameState(tempBoardCards, card1, card2, mySeat, enemySeat, buttonSeat, !isEnemyTurn, tempActions, potSize + possibleBetSize, round, dealer, enemyName, botWinProb, classifier, aggression));
            }
        }

        return allPossibleStates;
    }

    // Play a predicted move (for opponent node) or random move (for decision node)
    public GameState playMove() throws Exception {
        // Get all possible states
        List<GameState> allPossibleStates = getAllPossibleGameStates();
        GameState sampleState = allPossibleStates.get(0);

        // If decision node (next is opponent node so make prediction)
        if (!actions.getLast().isEnemyTurn) {
            // Use classifier to predict next likely action
            // Estimate win probability - this should be tuned for best results
            // Challenge to estimate unknown winning probability
            String enemyMove = classifier.getLikelyAction(enemyName, sampleState.getRound(), enemySeat, potSize, aggression, (1 - botWinProb) + 0.3);

            // Return predicted action
            for (GameState gs : allPossibleStates) {
                if (gs.getActions().getLast().action == ActionEnum.BET && enemyMove.equals("br")) return gs;
                else if (gs.getActions().getLast().action == ActionEnum.RAISE && enemyMove.equals("br")) return gs;
                else if (gs.getActions().getLast().action == ActionEnum.CHECK && enemyMove.equals("ck")) return gs;
                else if (gs.getActions().getLast().action == ActionEnum.CALL && enemyMove.equals("ck")) return gs;
            }
        }

        // Otherwise, return random action for bot
        return getRandomChild(allPossibleStates);
    }

    // Get all possible states and then select random move
    public GameState playRandomMove() {
        List<GameState> allPossibleStates = getAllPossibleGameStates();
        return getRandomChild(allPossibleStates);
    }
    public GameState getRandomChild(List<GameState> allPossibleStates) {
        Random rnd = new Random();
        int randomChoice = rnd.nextInt(allPossibleStates.size());
        return allPossibleStates.get(randomChoice);
    }
}
