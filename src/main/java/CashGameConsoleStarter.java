import game.*;
import game.cash.CashGameDescription;
import game.cash.CashGameTableSeater;
import game.deck.DeckFactory;
import game.deck.RandomDeck;
import game.deck.SerializedDeck;
import game.stats.BankrollGraphUI;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import bots.BotRepository;

import static java.lang.System.exit;

/**
 * This class starts a simulation on the console and writes a handhistory-file
 * to the ./data directory
 */

public class CashGameConsoleStarter {
    // TODO Initialise value for local home directory
    public static String homeDir = "C:\\Users\\leila\\Desktop\\opentestbed-project\\opentestbed-master-1\\opentestbed-master\\"
            + "projectbot\\";

    public static void main(String[] args) throws Exception {

        // TODO Set number of games
        int numGames = 25;
        // TODO Initialise bankroll
        int bankroll = 100;

        // TODO Select enemy for bot to play against
        String enemyName = "DemoBot/SimpleBot";
//        String enemyName = "DemoBot/AlwaysCallBot";
//        String enemyName = "Oursland/ChumpBot";
//        String enemyName = "Oursland/FlockBot";
//        String enemyName = "SmarterBot/SmarterBot";
//        String enemyName = "InteractiveBot/InteractiveBot";
//        String enemyName = "AggressiveBot/AggressiveBot";
//        String enemyName = "PassiveBot/PassiveBot";

        // permute seats to reduce variance
        boolean permuteSeats = true;

        String[] botNames = new String[]{"ABot/ABot", enemyName};

        BotRepository botRepository = new BotRepository();
        TableSeater tableSeater = new CashGameTableSeater(botRepository, permuteSeats);
        GameIDGenerator gameIDGenerator = new GameIDGenerator(System.nanoTime());
        HandHistoryWriter handHistoryWriter = new HandHistoryWriter();
        String simulationFileName = new SimpleDateFormat("yyMMdd-HHmm").format(new Date());
        handHistoryWriter.setWriter(new FileWriter(homeDir.replace("projectbot", "data\\" + simulationFileName + "-history.txt")));

        // in the future created via GUI, and persisted via XML to the ./data/games dir
        CashGameDescription cashGameDescription = new CashGameDescription();

        // Constants used for limit poker; these should not be changed
        cashGameDescription.setSmallBlind(1);
        cashGameDescription.setBigBlind(2);
        cashGameDescription.setNolimit(false);

        cashGameDescription.setInitialBankRoll(bankroll);
        cashGameDescription.setNumGames(numGames);
        cashGameDescription.setBotNames(botNames);
        enemyName = enemyName.split("/")[1];

        cashGameDescription.setInGameNames(new String[] { "Abot", enemyName});

        // start the game
        GameRunner runner = cashGameDescription.createGameRunner();
        BankrollGraphUI bankrollgraphUI = new BankrollGraphUI();
        runner.addBankrollObserver(bankrollgraphUI);

        // random deck for demo
        // DeckFactory deckFactory = RandomDeck.createFactory();

        // serialised deck for bot experiments
        DeckFactory deckFactory = SerializedDeck.createFactory(homeDir.replace("projectbot", "data\\decks\\deck-50000b.deck"));

        runner.runGame(deckFactory, tableSeater, gameIDGenerator, Arrays.asList(handHistoryWriter));
        bankrollgraphUI.createGraph(simulationFileName);
    }
}
