package game;

import bots.BotRepository;
import com.biotools.meerkat.Player;

/**
 * A TableSeater puts bots on a table.
 */
public abstract class TableSeater {

    private BotRepository botRepository;

    protected TableSeater(BotRepository botRepository) {
        this.botRepository = botRepository;
    }

    /**
     * creates Tables (a.k.a {@link PublicGameInfo}s with the players/bots according to
     * a gameDescription. <br>
     * {@link PublicPlayerInfo}s will get their initial bankroll, name and bot assigned
     *
     * @param gameDescription
     * @return an array of PublicGameInfos with the bots seated around the table.
     */
    public abstract PublicGameInfo[] createTables(AbstractGameDescription gameDescription);

    protected void seatPlayer(AbstractGameDescription gameDescription, PublicGameInfo gameInfo, int seat, int targetPlayer) {
        Player bot = botRepository.createBot(gameDescription.getBotNames()[targetPlayer]);
        if (bot instanceof NamedPlayer) {
            ((NamedPlayer) bot).setIngameName(gameDescription.getInGameNames()[targetPlayer]);
        }
        PublicPlayerInfo playerInfo = new PublicPlayerInfo();
        playerInfo.setBankroll(gameDescription.getInitialBankRoll());
        playerInfo.setBot(bot);
        playerInfo.setName(gameDescription.getInGameNames()[targetPlayer]);
        gameInfo.setPlayer(seat, playerInfo);
    }
}
