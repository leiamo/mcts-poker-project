package game.cash;

import game.AbstractGameDescription;
import game.PublicGameInfo;
import game.PublicPlayerInfo;

public class DoylesRebuy implements RebuyStrategy {

    private AbstractGameDescription gameDescription;

    public DoylesRebuy(AbstractGameDescription gameDescription) {
        this.gameDescription = gameDescription;
    }

    @Override
    public void checkPlayerRebuy(PublicGameInfo gameInfo) {
        for (int seat = 0; seat < gameInfo.getNumSeats(); seat++) {
            PublicPlayerInfo player = gameInfo.getPlayer(seat);
            if (player != null) {
                player.setBankroll(gameDescription.getInitialBankRoll());
            }
            if (player != null && player.isSittingOut()) {
                player.setSittingOut(false);
            }
        }
    }
}
