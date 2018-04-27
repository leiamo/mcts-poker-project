package projectbot.opponentModelling;

public class TrainingDataRow {

    public String playerName, round, position, action, potSize, winProb, aggression;

    public TrainingDataRow(String playerName, String round, String position,
                           String action, String potSize, String winProb,
                           String aggression){
        this.playerName = playerName;
        this.round = round;
        this.position = position;
        this.action = action;
        this.potSize = potSize;
        this.winProb = winProb;
        this.aggression = aggression;
    }

    public String printString(){
        return playerName + "," + round + "," + position + "," + action + "," + potSize + "," + winProb + "," + aggression;
    }

}
