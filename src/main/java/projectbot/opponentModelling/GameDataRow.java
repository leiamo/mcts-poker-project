package projectbot.opponentModelling;

public class GameDataRow {

    public String playerName, round, position, action, potSize, holeCard1, holeCard2, boardCards;

    public GameDataRow(String playerName, String round, String position,
                           String action, String potSize, String holeCard1, String holeCard2, String boardCards){
        this.playerName = playerName;
        this.round = round;
        this.position = position;
        this.action = action;
        this.potSize = potSize;
        this.holeCard1 = holeCard1;
        this.holeCard2 = holeCard2;
        this.boardCards = boardCards;
    }

    public String getAction() {
        return action;
    }

    public String getBoardCards() {
        return boardCards;
    }

    public String getHoleCard1() {
        return holeCard1;
    }

    public String getHoleCard2() {
        return holeCard2;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPosition() {
        return position;
    }

    public String getPotSize() {
        return potSize;
    }

    public String getRound() {
        return round;
    }

    public String printString(){
        return playerName + " " + round + " " + position + " " + action + " " + potSize + " " + holeCard1 + " " + holeCard2 + " " + boardCards;
    }

}