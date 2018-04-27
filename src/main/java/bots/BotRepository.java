package bots;

import com.biotools.meerkat.Player;
import com.biotools.meerkat.util.Preferences;
import org.apache.log4j.Logger;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * A BotRepository is responsible for loading Meerkat-Bots that are defined in
 * the /data/bots directory of the program in *.pd-files.<br>
 * <br>
 * Currently not supported properties: PLAYER_JAR_FILE <br>
 */
public class BotRepository {
    // key = botname in canonical form AINAME/BOTNAME
    private Map<String, BotMetaData> botNameToBots = new HashMap<String, BotMetaData>();

    private static final Logger log = Logger.getLogger(BotRepository.class);

    public BotRepository() {
        findBots(new File("C:\\Users\\leila\\Desktop\\opentestbed-project\\opentestbed-master-1\\opentestbed-master\\data\\bots"));
    }

    /**
     * searches recursivly through the given directory to find *.pd files
     */
    private void findBots(File botDirectory) {
        File[] dirEntries = botDirectory.listFiles();
        for (File file : dirEntries) {
            if (file.isDirectory()) {
                findBots(file);
            } else if (file.getName().endsWith(".pd")) {
                readBotFromMeerkatFile(file);
            }
        }
    }

    private void readBotFromMeerkatFile(File meerkatPDFile) {
        
        System.out.println(meerkatPDFile);


        Preferences prefs = new Preferences(meerkatPDFile);

        String botClassName = prefs.getPreference("PLAYER_CLASS");

        if (botClassName.endsWith("PlugInOpponent")) {
            // PlugInOpponent is just a Decorator used in PA to allow for
            // special classLoading.
            // For now we expect all bots on the classpath
            botClassName = prefs.getPreference("BOT_PLAYER_CLASS");
        }


        String playerName = prefs.getPreference("PLAYER_NAME");
        String aiName = prefs.getPreference("AI_NAME");
        String fullName = aiName + "/" + playerName;
        boolean noLimitBot = prefs.getBoolean("NO_LIMIT", false);

        try {
            Class.forName(botClassName);
            botNameToBots.put(fullName, new BotMetaData(fullName, botClassName, noLimitBot, prefs));
        } catch (ClassNotFoundException e) {
            log.debug("Bot '" + botClassName + "' from '" + meerkatPDFile + "' could not be loaded");
        }
    }

    /**
     * @return names of all bots found
     */
    public Set<String> getBotNames() {
        return Collections.unmodifiableSet(botNameToBots.keySet());
    }

    /**
     * @param botName
     * @return BotMetaData of bot with given name (or null if not existent)
     */
    public BotMetaData getBotMetaData(String botName) {
        return botNameToBots.get(botName);
    }

    /**
     * creates a bot and initializes it.
     *
     * @param string
     * @return
     * @throws IllegalArgumentException if bot doesn't exist
     */

    public Player createBot(String botName) {
        BotMetaData botMetaData = botNameToBots.get(botName);
        if (botMetaData == null) {
            throw new IllegalArgumentException("Bot '" + botName + "' does not exist");
        }
        try {
            Class<?> botClass = Class.forName(botMetaData.getBotClassName());
            Player botInstance = (Player) botClass.newInstance();
            botInstance.init(botMetaData.getBotPreferences());
            return botInstance;
        } catch (Exception e) {
            throw new RuntimeException("Error creating bot '" + botName + "'", e);
        }

    }

    /**
     * add a bot manually without *.pd-file
     *
     * @param botMetaData
     */
    public void addBot(BotMetaData botMetaData) {
        botNameToBots.put(botMetaData.getBotName(), botMetaData);
    }
}
