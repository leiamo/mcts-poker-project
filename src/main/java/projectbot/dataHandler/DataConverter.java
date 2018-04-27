package projectbot.dataHandler;

import com.biotools.meerkat.Card;
import com.biotools.meerkat.Hand;
import projectbot.calculators.WinProbCalculator;
import projectbot.enums.ActionEnum;
import projectbot.enums.RoundEnum;
import projectbot.opponentModelling.AggressionDataRow;
import projectbot.opponentModelling.AggressionFileHandler;
import projectbot.opponentModelling.GameDataRow;
import projectbot.opponentModelling.TrainingDataRow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;

public class DataConverter {

    public AggressionFileHandler aggressionFile;
    public DecimalFormat df4 = new DecimalFormat("#.####");
    public String homeDir;

    // Constructor initialises aggression file so no need to keep opening and closing the file
    public DataConverter(String homeDir) throws IOException {
        aggressionFile = new AggressionFileHandler(homeDir + "data\\aggression.csv");

    }

    // Converts real-time game data for an individual game (preflop to showdown)
    // Converts this to training data that opponent models can train/learn from
    public LinkedList<TrainingDataRow> convertToTrainingData(LinkedList<GameDataRow> inputData) throws IOException {
        WinProbCalculator winProbCalculator = new WinProbCalculator(homeDir);
        LinkedList<TrainingDataRow> outputData = new LinkedList<>();

        // Initialise empty variables
        AggressionDataRow aggDataRow1 = new AggressionDataRow();
        AggressionDataRow aggDataRow2 = new AggressionDataRow();
        double aggression1 = 0.5;
        double aggression2 = 0.5;
        Card[] cards = new Card[]{};
        Card[] cards1 = new Card[]{};
        Card[] cards2 = new Card[]{};

        // Need to calculate win probability and aggression based on player and board cards
        double winProb = 0.5;
        double aggression = 0.5;

        int count = 0;
        boolean nextGame = true;

        String lastRound = "River";

        for (GameDataRow entry : inputData) {
            if (count < inputData.size()-20) {
                // if this is first entry in game, initialise aggression and cards data
                if (entry.getRound().equals("Preflop") && lastRound.equals("River")) {

                    // Sample entry for game as some info (name and cards) are same throughout this game
                    GameDataRow sampleEntry1 = inputData.get(count);
                    GameDataRow sampleEntry2 = inputData.get(count + 1);

                    // Get opponent aggression which is also considered as the same throughout the game
                    aggDataRow1 = aggressionFile.getAggressionData(sampleEntry1.playerName);
                    aggDataRow2 = aggressionFile.getAggressionData(sampleEntry2.playerName);

                    nextGame = false;
                    int subcounter = count;
                    // First loop to get player aggression by updating all actions made
                    while (nextGame == false) {
                        GameDataRow newRow = inputData.get(subcounter);
                        GameDataRow nextRow = inputData.get(subcounter+1);
                        // if this belongs to a different game, reset boolean variable
                        if (newRow.round.equals("River") && nextRow.round.equals("Preflop")) {
                            nextGame = true;
                        }
                        // otherwise, update aggression data
                        else {
                            if (newRow.getPlayerName().equals(aggDataRow1.playerName)) {
                                ActionEnum action = ActionEnum.convertToAction(newRow.action);
                                aggDataRow1.update(action);
                            } else if (newRow.getPlayerName().equals(aggDataRow2.playerName)) {
                                ActionEnum action = ActionEnum.convertToAction(newRow.action);
                                aggDataRow2.update(action);
                            }
                        }
                        subcounter++;
                    }
                    // reset boolean variable and update aggression file with this specific game info
                    aggression1 = aggDataRow1.recalculateAggression();
                    aggression2 = aggDataRow2.recalculateAggression();
                    aggressionFile.updateFile(aggDataRow1);
                    aggressionFile.updateFile(aggDataRow2);

                    // Gets opponent cards for this individual game
                    cards1 = new Card[]{convertCard(sampleEntry1.getHoleCard1()), convertCard(sampleEntry1.getHoleCard2())};
                    cards2 = new Card[]{convertCard(sampleEntry2.getHoleCard1()), convertCard(sampleEntry2.getHoleCard2())};
                }

                // Calculate opponent win probability based on opponent cards and current board
                if (entry.getPlayerName().equals(aggDataRow1.playerName)) {
                    cards = cards1;
                    aggression = aggression1;
                } else if (entry.getPlayerName().equals(aggDataRow2.playerName)) {
                    cards = cards2;
                    aggression = aggression2;
                }
                winProb = winProbCalculator.calcWinProb(cards, convertBoard(entry.getBoardCards()));

                // Convert actions to "br" or "ck"
                String action = formatAction(entry.getAction());

                // Convert to training data and add to output set
                TrainingDataRow outputDataRow = new TrainingDataRow(
                        entry.getPlayerName(),
                        entry.getRound(),
                        entry.getPosition(),
                        action,
                        entry.getPotSize(),
                        df4.format(winProb),
                        df4.format(aggression)
                );

                outputData.add(outputDataRow);
                count++;
                System.out.println("Converted " + count + " entries");
                lastRound = entry.getRound();
            }
        }
        return outputData;
    }


    // Return "ck" or "br" as training data format
    private String formatAction(String action) {
        if (action.equals("b") || action.equals("r")) return "br";
        return "ck";
    }

    private Card convertCard(String holeCard) {
        return new Card(holeCard);
    }

    private Hand convertBoard(String board) {
        String[] boardCards = board.split(" ");
        Hand hand = new Hand();
        if (board == "") return hand;
        for (int i = 0; i < boardCards.length; i++) {
            hand.addCard(convertCard(boardCards[i]));
        }
        return hand;
    }

}
