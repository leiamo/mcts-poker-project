package ww10;

import game.*;
import game.cash.CashGameDescription;
import game.cash.CashGameTableSeater;
import game.deck.DeckFactory;
import game.deck.RandomDeck;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ww10.WW10Protocol.PrologBotDescription;
import ww10.gui.DataModel;
import bots.BotRepository;
import bots.prologbot.PrologBot;

import com.biotools.meerkat.GameObserver;

public class WetenschapsweekStarter {

    public static void main(String[] args) throws Exception {

        // number of games
        final int numGames = Integer.MAX_VALUE;
        final int nbPlayers = 6;

        // four Bots fight against each other
        // valid BotNames can be obtained from the botRepository
        String[] botNames = new String[nbPlayers];
        String[] ingameNames = new String[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            if (i == nbPlayers - 1) {
                botNames[i] = "DemoBot/SimpleBot.csv";
                ingameNames[i] = "SimpleBot.csv";
            } else {
                botNames[i] = "PrologBot/PrologBot";
                ingameNames[i] = "Player " + (i + 1);
            }
        }

        BotRepository botRepository = new BotRepository();
        TableSeater tableSeater = new CashGameTableSeater(botRepository, false);
        GameIDGenerator gameIDGenerator = new GameIDGenerator(System.nanoTime());
        HandHistoryWriter handHistoryWriter = new HandHistoryWriter();
        String simulationFileName = new SimpleDateFormat("yyMMdd-hhmm").format(new Date());
        handHistoryWriter.setWriter(new FileWriter("./data/" + simulationFileName + "-history.txt"));

        // in the future created via GUI, and persisted via XML to the ./data/games dir
        CashGameDescription cashGameDescription = new CashGameDescription();
        cashGameDescription.setSmallBlind(1);
        cashGameDescription.setBigBlind(2);
        cashGameDescription.setInitialBankRoll(200);
        cashGameDescription.setNumGames(numGames);

        cashGameDescription.setBotNames(botNames);
        cashGameDescription.setInGameNames(ingameNames);

        // start the game
        GameRunner runner = cashGameDescription.createGameRunner();
        final DataModel gui = new DataModel();
        runner.addBankrollObserver(gui);
        DeckFactory deckFactory = RandomDeck.createFactory();
        List<GameObserver> observers = new ArrayList<GameObserver>();
        //		observers.add(handHistoryWriter);
        observers.add(gui);
        final PublicGameInfo gameInfo = runner.asyncRunGame(deckFactory, tableSeater, gameIDGenerator, observers);

        PrologBotServer server = new PrologBotServer() {

            @Override
            protected void onNewBot(PrologBotDescription botDescription) {
                PrologBot bot = (PrologBot) (gameInfo.getPlayer(botDescription.getId()).getBot());
                try {
                    bot.writeBot(botDescription.getProlog());
                    gui.onSubmit(bot.getName(), truncate(botDescription.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Could not write prolog code.");
                    System.exit(-1);
                }
            }

            private String truncate(String name) {
                if (name.length() > 20)
                    return name.substring(0, 20);
                else
                    return name;
            }

        };

        (new Thread(server, "PrologBotServer")).start();

    }
}
