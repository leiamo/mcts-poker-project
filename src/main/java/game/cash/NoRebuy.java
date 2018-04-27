package game.cash;

import game.PublicGameInfo;

public class NoRebuy implements RebuyStrategy {
    @Override
    public void checkPlayerRebuy(PublicGameInfo gameInfo) {
        // NOP
    }
}
