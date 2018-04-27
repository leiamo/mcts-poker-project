package game.tournament;

import com.biotools.meerkat.Action;
import com.biotools.meerkat.Card;
import com.biotools.meerkat.GameInfo;
import com.biotools.meerkat.PlayerInfo;
import game.ExtendedGameObserver;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class TournamentRank implements ExtendedGameObserver {
    private static final Logger log = Logger.getLogger(TournamentRank.class);

    private List<PlayerInfo> rank = new ArrayList<PlayerInfo>();

    public List<PlayerInfo> getRankedPlayers() {
        return rank;
    }

    @Override
    public void gameOverEvent(GameInfo gameInfo) {
        if (gameInfo.getNumPlayers() == 2) {
            PlayerInfo first = null;
            PlayerInfo second = null;
            for (int seat = 0; seat < gameInfo.getNumSeats(); seat++) {
                PlayerInfo player = gameInfo.getPlayer(seat);
                if (player != null) {
                    if (player.getBankRoll() <= 0.001) {
                        second = player;
                    } else {
                        first = player;
                    }
                }
            }
            if (second != null) {
                rank.add(second);
                log.debug("Player " + second.getName() + " eliminated - " + rank.size());
                rank.add(first);
                log.debug("And the winner is: " + first.getName() + "!!");
            }
        } else {
            for (int seat = 0; seat < gameInfo.getNumSeats(); seat++) {
                PlayerInfo player = gameInfo.getPlayer(seat);
                if (player != null && player.getBankRoll() <= 0.001) {
                    rank.add(player);
                    log.debug("Player " + player.getName() + " eliminated - " + rank.size());
                }
            }
        }
    }

    @Override
    public void gameStartEvent(GameInfo gameinfo) {
    }

    @Override
    public void actionEvent(int i, Action action) {
    }

    @Override
    public void stageEvent(int i) {
    }

    @Override
    public void showdownEvent(int i, Card card, Card card1) {
    }

    @Override
    public void dealHoleCardsEvent() {
    }

    @Override
    public void winEvent(int i, double d, String s) {
    }

    @Override
    public void gameStateChanged() {
    }

    @Override
    public void beforeActionEvent(int pos, Action action) {
    }

    @Override
    public void gameOverEvent() {
    }
}
