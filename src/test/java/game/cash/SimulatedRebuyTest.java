package game.cash;

import game.PublicGameInfo;
import game.PublicPlayerInfo;
import game.cash.CashGameDescription;
import game.cash.SimulatedRebuy;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimulatedRebuyTest {

    CashGameDescription cashGameDescription;
    SimulatedRebuy rebuy;
    PublicPlayerInfo[] players = new PublicPlayerInfo[3];
    PublicGameInfo gameInfo;

    @Before
    public void setup() {
        cashGameDescription = mock(CashGameDescription.class);
        when(cashGameDescription.getInitialBankRoll()).thenReturn(2.00D);

        gameInfo = new PublicGameInfo();
        gameInfo.setNumSeats(players.length);

        for (int i = 0; i < players.length; i++) {
            players[i] = new PublicPlayerInfo();
            gameInfo.setPlayer(i, players[i]);
        }
    }

    @Test
    public void shouldOnlyResetPlayersWithNoMoneyLeft() {

        rebuy = new SimulatedRebuy(0, 2.00D);
        players[0].setBankroll(1.70D);
        players[1].setBankroll(2.30D);
        players[2].setBankroll(0.0D);
        players[2].setSittingOut(true);

        rebuy.checkPlayerRebuy(gameInfo);

        assertThat(players[0].getBankRoll(), is(1.70D));
        assertThat(players[1].getBankRoll(), is(2.30D));
        assertThat(players[2].getBankRoll(), is(2.00D));
        assertThat(players[2].isSittingOut(), is(false));
    }

}
