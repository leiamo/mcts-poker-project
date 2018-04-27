package game.tournament;

import game.AbstractGameDescription;
import game.GameRunner;
import game.tournament.blinds.BlindsStructure;

/**
 * GameDescription for Tournaments<br>
 */
public class TournamentGameDescription extends AbstractGameDescription {
    /**
     * number of games to run in a simulation
     */
    private BlindsStructure blindsStructure;
    private int handsPerLevel;

    public TournamentGameDescription(int handsPerLevel, int initialBankRoll) {
        super();
        this.handsPerLevel = handsPerLevel;
        super.setInitialBankRoll(initialBankRoll);
    }

    public GameRunner createGameRunner() {
        blindsStructure = new BlindsStructure(getNumSeats(), handsPerLevel, getInitialBankRoll());
        return new TournamentGameRunner(this);
    }

    public void setInitialBankRoll(double initialBankRoll) {
        super.setInitialBankRoll(initialBankRoll);
        blindsStructure = new BlindsStructure(getNumSeats(), handsPerLevel, getInitialBankRoll());
    }

    public BlindsStructure getBlindsStructure() {
        return blindsStructure;
    }

    public int getHandsPerLevel() {
        return handsPerLevel;
    }

    public void setHandsPerLevel(int handsPerLevel) {
        this.handsPerLevel = handsPerLevel;
        blindsStructure = new BlindsStructure(getNumSeats(), handsPerLevel, getInitialBankRoll());
    }

}
