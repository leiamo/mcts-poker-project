package projectbot.opponentModelling;

import projectbot.enums.ActionEnum;
import java.text.DecimalFormat;

public class AggressionDataRow {
    public String playerName;
    public int numBets, numCalls, numChecks, numFolds, numRaises, totalMoves;
    public double aggression;
    public DecimalFormat df4 = new DecimalFormat("#.####");

    public AggressionDataRow(String playerName, int bets, int calls, int checks, int folds, int raises) {
        this.playerName = playerName;
        this.numBets = bets;
        this.numCalls = calls;
        this.numChecks = checks;
        this.numFolds = folds;
        this.numRaises = raises;
        recalculateAggression();
    }

    public AggressionDataRow() { }

    public void update(ActionEnum action) {
        switch (action) {
            case FOLD:
                incrementFolds(); break;
            case CALL:
                incrementCalls(); break;
            case BET:
                incrementBets(); break;
            case RAISE:
                incrementRaises(); break;
            case CHECK:
                incrementChecks(); break;
        }
    }

    public double recalculateAggression() {
        this.totalMoves = numBets + numCalls + numChecks + numFolds + numRaises;
        this.aggression = 0.5;
        if (totalMoves >= 10) {
            // Aggression Frequency = (Total Bets + Total Raises) / (Total Bets + Total Raises + Total Calls + Total Folds) * 100
            this.aggression = ((double) numRaises + numBets) / ((double)totalMoves);
        }
        aggression = Double.parseDouble(df4.format(aggression));
        return aggression;
    }

    private void incrementBets() {
        numBets++;
        totalMoves++;
    }

    private void incrementCalls() {
        numCalls++;
        totalMoves++;
    }

    private void incrementChecks() {
        numChecks++;
        totalMoves++;
    }

    private void incrementFolds() {
        numFolds++;
        totalMoves++;
    }

    private void incrementRaises() {
        numRaises++;
        totalMoves++;
    }

}
