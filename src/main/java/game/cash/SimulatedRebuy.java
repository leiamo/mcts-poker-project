package game.cash;

import game.PublicGameInfo;
import game.PublicPlayerInfo;

public class SimulatedRebuy implements RebuyStrategy {

    private final double rebuyThreshold;
    private final double rebuyAmount;

    public SimulatedRebuy(double rebuyThreshold, double rebuyAmount) {
        this.rebuyThreshold = rebuyThreshold;
        this.rebuyAmount = rebuyAmount;
    }

    @Override
    public void checkPlayerRebuy(PublicGameInfo gameInfo) {
        for (int seat = 0; seat < gameInfo.getNumSeats(); seat++) {
            PublicPlayerInfo player = gameInfo.getPlayer(seat);
            if (player != null && player.getBankRoll() <= rebuyThreshold) {
                player.setBankroll(rebuyAmount);
                player.setSittingOut(false);
            }
        }
    }

}
