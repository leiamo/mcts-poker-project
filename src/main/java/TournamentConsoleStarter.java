import bots.BotRepository;
import game.GameIDGenerator;
import game.GameRunner;
import game.HandHistoryWriter;
import game.TableSeater;
import game.deck.DeckFactory;
import game.deck.SerializedDeck;
import game.tournament.TournamentGameDescription;
import game.tournament.TournamentRank;
import game.tournament.TournmentTableSeater;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This class starts a simulation on the console and writes a handhistory-file
 * to the ./data directory
 */
public class TournamentConsoleStarter {

    private static final int NUM_COMPETITORS = 50;

    public static void main(String[] args) throws Exception {

        List<String> botNames = new ArrayList<String>();

        while (botNames.size() < NUM_COMPETITORS) {
            botNames.add("DemoBot/SimpleBot.csv");
            botNames.add("SmarterBot/SmarterBot");
            botNames.add("DemoBot/AlwaysCallBot");
            botNames.add("Oursland/ChumpBot");
            botNames.add("Oursland/FlockBot");
        }

        BotRepository botRepository = new BotRepository();
        TableSeater tableSeater = new TournmentTableSeater(botRepository);
        GameIDGenerator gameIDGenerator = new GameIDGenerator(System.nanoTime());

        HandHistoryWriter handHistoryWriter = new HandHistoryWriter();
        String simulationFileName = new SimpleDateFormat("yyMMdd-HHmm").format(new Date());
        handHistoryWriter.setWriter(new FileWriter("./data/" + simulationFileName + "-history.txt"));

        TournamentRank rank = new TournamentRank();

        TournamentGameDescription tournament = new TournamentGameDescription(100, 1000);

        tournament.setBotNames(botNames.toArray(new String[botNames.size()]));

        // start the game
        GameRunner runner = tournament.createGameRunner();
        DeckFactory deckFactory = SerializedDeck.createFactory("./data/decks/deck-100000.deck");
        runner.runGame(deckFactory, tableSeater, gameIDGenerator, Arrays.asList(handHistoryWriter, rank));

        System.out.println("\n\n==============================================");
        System.out.println("Final Rank:\n");
        for (int pos = 1; pos <= botNames.size(); pos++) {
            System.out.println("" + pos + ": " + rank.getRankedPlayers().get(botNames.size() - pos).getName());
        }

    }
}
