package game.tournament.blinds;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BlindsStructure {
    private static final Logger log = Logger.getLogger(BlindsStructure.class);

    private List<Level> blindsLevels = new ArrayList<Level>();
    private int totalPlayers;
    private int handsPerLevel;

    public BlindsStructure(int totalPlayers, int handsPerLevel, double initialChipAmount) {
        this.totalPlayers = totalPlayers;
        this.handsPerLevel = handsPerLevel;
        createBlindsLevels(initialChipAmount);
    }

    private void createBlindsLevels(double initialChipAmount) {
        double bigBlind = Math.round(initialChipAmount / 50);
        double smallBlind = Math.round(bigBlind / 2);
        double maxBigBlind = initialChipAmount * 2 * ((totalPlayers / 10) + 1);
        while (bigBlind < maxBigBlind) {
            blindsLevels.add(new Level(smallBlind, bigBlind));
            log.trace(blindsLevels.size() + ": " + smallBlind + "/" + bigBlind);
            smallBlind = smallBlind * 2;
            bigBlind = bigBlind * 2;
        }
    }

    public Level getCurrentLevel(int handNumber) {
        int level = (int) (((double) handNumber / handsPerLevel));
        return getBlindsLevel(level);
    }

    public Level getBlindsLevel(int blindsLevel) {
        if (blindsLevel >= blindsLevels.size()) {
            log.trace("blindsLevel " + blindsLevel + " requested, but we only have " + blindsLevels.size() + " levels. Returning last one.");
            return blindsLevels.get(blindsLevels.size() - 1);
        }
        return blindsLevels.get(blindsLevel);
    }

    public int getNumLevels() {
        return blindsLevels.size();
    }

}
