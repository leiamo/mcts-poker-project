package projectbot;

import com.biotools.meerkat.*;
import com.biotools.meerkat.util.Preferences;
import projectbot.backpropagation.AdvancedBackpropagation;
import projectbot.backpropagation.IBackpropagation;
import projectbot.calculators.WinProbCalculator;
import projectbot.dataHandler.DataConverter;
import projectbot.enums.ActionEnum;
import projectbot.enums.RoundEnum;
import projectbot.expansion.FullExpansion;
import projectbot.expansion.IExpansion;
import projectbot.gamestate.RowAction;
import projectbot.main.MCTS;
import projectbot.opponentModelling.*;
import projectbot.selection.ISelection;
import projectbot.selection.SmoothUCTSelection;
import projectbot.simulation.ISimulation;
import projectbot.simulation.PredictiveSimulation;
import java.io.IOException;
import java.util.LinkedList;

public class Abot implements Player {
    private int ourSeat; // our seat for the current hand
    private Card c1, c2; // our hole cards
    private GameInfo gi; // general game information
    private Preferences prefs; // the configuration options for this bot
    private LinkedList<RowAction> actionsMade;
    private RoundEnum round;
    private Double winProb;
    public WinProbCalculator winProbCalculator;
    public ClassifierHandler classifierHandler;
    public String opponentName;
    public AggressionFileHandler aggressionFile;
    public MLFileHandler trainingDataFile;
    public AggressionDataRow enemyAggressionDataRow;
    public DataConverter dataConverter;
    public ISelection selection;
    public IExpansion expansion;
    public ISimulation simulation;
    public IBackpropagation backpropagation;
    public int numIterations;
    boolean startOfGame;


    // TODO Set home directory string to local store location
    public String homeDir = "C:\\Users\\leila\\Desktop\\opentestbed-project\\opentestbed-master-1\\opentestbed-master" +
            "\\src\\main\\java\\projectbot\\";

    // Bot is initialised at the start of each set of games
    public Abot() throws Exception {
        initStrategiesForMCTS();
        startOfGame = true;
        dataConverter = new DataConverter(homeDir);
        winProbCalculator = new WinProbCalculator(homeDir);
        aggressionFile = new AggressionFileHandler(homeDir + "data\\aggression.csv");
        trainingDataFile = new MLFileHandler(homeDir + "data\\onlineData.csv");
    }

    // TODO Update selection, expansion, simulation, back-propagation strategies
    public void initStrategiesForMCTS() {
        numIterations = 100000;

        // SELECTION:
        // Choose RandomSelection(), UCTSelection(17), SmoothUCTSelection(0.1, 0.9, 0.00005, 17)
        selection = new SmoothUCTSelection(0.1, 0.9, 0.00005, 17);

        // EXPANSION:
        // Choose FullExpansion()
        expansion = new FullExpansion();

        // SIMULATION:
        // Choose RandomSimulation(), PredictiveSimulation()
        simulation = new PredictiveSimulation();

        // BACK-PROPAGATION:
        // Choose SimpleBackpropagation(), AdvancedBackpropagation()
        backpropagation = new AdvancedBackpropagation();
    }

    /**
     * /**
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
        System.out.println("\tAbot's Hand: [" + c1.toString() + "-" + c2.toString() + "]");
    }

    /**
     * Requests an Action from the player
     * Called when it is the Player's turn to act.
     */
    public Action getAction() {
        // Calculate winning probability for bot
        if (winProb == null) {
            winProb = winProbCalculator.calcWinProb(new Card[]{c1, c2}, gi.getBoard());
        }
        // Initialise MCTS Algorithm
        MCTS algorithm = new MCTS(numIterations, selection, expansion, simulation, backpropagation);

        // Calculate aggression frequency of opponent
        double aggression = enemyAggressionDataRow.recalculateAggression();

        // Choose action
        Action action;
        try {
            action = algorithm.chooseNextMove(gi, c1, c2, actionsMade, winProb, classifierHandler, aggression);
        } catch (Exception e) {
            action = Action.checkAction();
            e.printStackTrace();
        }
        // Validation action
        Action validAction = validateAction(action);
        System.out.println("\tAbot's move: " + validAction.toString());
        return validAction;
    }

    private Action validateAction(Action action) {
        ActionEnum prevAction = actionsMade.get(actionsMade.size() - 1).action;
        ActionEnum chosenAction = ActionEnum.convertMeerkatStringToAction(action.toString());

        // If previous action is bet/raise: bot can only raise, call, or fold
        if (prevAction == ActionEnum.BET || prevAction == ActionEnum.RAISE) {
            if (chosenAction == ActionEnum.BET || chosenAction == ActionEnum.RAISE)
                return Action.raiseAction(gi, gi.getMinRaise());
            else if (chosenAction == ActionEnum.CHECK || chosenAction == ActionEnum.CALL)
                return Action.callAction(gi);
            else if (chosenAction == ActionEnum.FOLD)
                return Action.foldAction(gi);
        }
        // If previous action is call/check: bot can only bet, or check
        else {
            if (chosenAction == ActionEnum.BET || chosenAction == ActionEnum.RAISE)
                return Action.betAction(gi.getMinRaise());
            else if (chosenAction == ActionEnum.CHECK || chosenAction == ActionEnum.CALL || chosenAction == ActionEnum.FOLD)
                return Action.checkAction();
        }
        return action;
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
     * A new betting round has started.
     */
    public void stageEvent(int stage) {
        String board = "Board: ";
        if (stage > 0) {
            board += gi.getBoard().getCard(1).toString();
            board += " ";
            board += gi.getBoard().getCard(2).toString();
            board += " ";
            board += gi.getBoard().getCard(3).toString();
            board += " ";
        }
        if (stage > 1) {
            board += gi.getBoard().getCard(4).toString();
            board += " ";
        }
        if (stage > 2) {
            board += gi.getBoard().getCard(5).toString();
        }
        String sround = "";
        switch (stage) {
            case 0:
                sround = "PREFLOP";
                break;
            case 1:
                sround = "FLOP";
                break;
            case 2:
                sround = "TURN";
                break;
            case 3:
                sround = "RIVER";
                break;
        }
        double pot = gi.getTotalPotSize();
        if (pot == 0) pot = 3.0;
        System.out.println("\nROUND " + sround);
        System.out.print("\tPot: " + pot);
        System.out.println("\t" + board.toString());

        round = RoundEnum.convert(stage + 1);
        if (c1 != null && c2 != null) {
            winProb = winProbCalculator.calcWinProb(new Card[]{c1, c2}, gi.getBoard());
        }
    }

    /**
     * A showdown has occurred.
     * //@param pos the position of the player showing
     *
     * @param c1 the first hole card shown
     * @param c2 the second hole card shown
     */
    public void showdownEvent(int seat, Card c1, Card c2) {
        if (seat != gi.getPlayerSeat("Abot")) {
            int count = 0;
            RoundEnum oldRound = RoundEnum.PREFLOP;
            Hand fullBoard = gi.getBoard();
            Hand newBoard = new Hand();
            double aggression = enemyAggressionDataRow.recalculateAggression();

            // Ignore first two blind actions as this does not represent enemy behaviour
            actionsMade.removeFirst();
            actionsMade.removeFirst();

            // Calculate winProb pre-flop
            double enemyWinProb = winProbCalculator.calcWinProb(new Card[]{c1, c2}, newBoard);

            // Iterate through actions during this game
            // calculate win probability for each round with enemy cards
            // (could not calculate this during the game as this information is not available)
            while (count < actionsMade.size()) {
                RowAction action = actionsMade.get(count);
                count++;
                // Only record enemy actions as this is only valuable learning data
                if (action.isEnemyTurn) {
                    RoundEnum newRound = action.round;
                    // if new round, then update board cards and recalculate win probability for enemy
                    if (oldRound != newRound) {
                        switch (newRound) {
                            case FLOP:
                                newBoard.addCard(fullBoard.getCard(0));
                                newBoard.addCard(fullBoard.getCard(1));
                                newBoard.addCard(fullBoard.getCard(2));
                                break;
                            case TURN:
                                newBoard.addCard(3);
                                break;
                            case RIVER:
                                newBoard.addCard(4);
                                break;
                        }
                        // recalculate win probability
                        enemyWinProb = winProbCalculator.calcWinProb(new Card[]{c1, c2}, newBoard);
                    }
                    // update old round
                    oldRound = action.round;

                    // add training data to file
                    TrainingDataRow newRow = new TrainingDataRow(
                            opponentName,
                            newRound.convertToMLString(),
                            Integer.toString(seat),
                            action.action.convertToMLString(),
                            Double.toString(action.currentPotSize),
                            Double.toString(enemyWinProb),
                            Double.toString(aggression)
                    );
                    try {
                        // update data and update classifier by instance
                        trainingDataFile.updateDataAndClassifiers(newRow, classifierHandler);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                // rebuild opponent model
                classifierHandler.updateFileWithOnlineData(trainingDataFile.dataSet);
            } catch (Exception e) {
                System.out.println("Updating trainingDataNew.csv.csv was unsuccessful!");
                e.printStackTrace();
            }

        }
    }

    /**
     * A new game has been started.
     * //@param gi the game stat information
     */
    public void gameStartEvent(GameInfo gInfo) {
        this.gi = gInfo;

        if (startOfGame) {
            if (gi.getPlayerName(0).trim().equals("Abot")) {
                opponentName = gi.getPlayerName(1);
            } else {
                opponentName = gi.getPlayerName(0);
            }

            try {
                classifierHandler = new ClassifierHandler(homeDir, "lr", opponentName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // get aggression data for player (this is initialised if new opponent)
            enemyAggressionDataRow = aggressionFile.getAggressionData(opponentName);
            startOfGame = false;
        }

        actionsMade = new LinkedList<>();
        System.out.println();
        System.out.println();
        System.out.println("---------------- NEW GAME ----------------");
    }

    /**
     * An event sent when all players are being dealt their hole cards
     */
    public void dealHoleCardsEvent() {
    }

    /**
     * An action has been observed.
     */
    public void actionEvent(int pos, Action act) {
        ActionEnum action = ActionEnum.INVALID;
        if (act.isBet()) action = ActionEnum.BET;
        else if (act.isRaise()) action = ActionEnum.RAISE;
        else if (act.isFold()) action = ActionEnum.FOLD;
        else if (act.isCall()) action = ActionEnum.CALL;
        else if (act.isCheck()) action = ActionEnum.CHECK;
        else if (act.isSmallBlind()) action = ActionEnum.BET;
        else if (act.isBigBlind()) action = ActionEnum.RAISE;
        RowAction ra = new RowAction((pos != gi.getPlayerSeat("Abot")), round, action, act.getAmount(), gi.getTotalPotSize());
        actionsMade.add(ra);

        if (pos != gi.getPlayerSeat("Abot")) {
            System.out.println("\tEnemy " + act.toString2());
        }
        // if this is not bot's action, then update enemy aggression info
        if (pos != gi.getPlayerSeat("Abot") && !act.isBlind()) {
            enemyAggressionDataRow.update(action);
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
        try {
            aggressionFile.updateFile(enemyAggressionDataRow);
        } catch (IOException e) {
            System.out.println("Updating aggression.csv was unsuccessful!");
            e.printStackTrace();
        }
    }

    /**
     * A player at pos has won amount with the hand handName
     */
    public void winEvent(int pos, double amount, String handName) {
        String s = pos != gi.getPlayerSeat("Abot") ? "Enemy" : "Abot";
        if (handName == "") handName = "as last player standing";
        System.out.println(s + " wins " + amount + " with " + handName + ".");
        System.out.println();
        System.out.println("---------------- END GAME ----------------");
        System.out.println();

    }
}
