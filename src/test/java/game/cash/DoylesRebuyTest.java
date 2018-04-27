package game.cash;

import game.PublicGameInfo;
import game.PublicPlayerInfo;
import game.cash.CashGameDescription;
import game.cash.DoylesRebuy;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DoylesRebuyTest {

    @Test
    public void shouldResetAllStacks() {
        CashGameDescription cashGameDescription = mock(CashGameDescription.class);
        when(cashGameDescription.getInitialBankRoll()).thenReturn(2.00D);

        DoylesRebuy rebuy = new DoylesRebuy(cashGameDescription);

        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setNumSeats(3);

        PublicPlayerInfo player1 = new PublicPlayerInfo();
        player1.setBankroll(1.70D);
        gameInfo.setPlayer(0, player1);

        PublicPlayerInfo player2 = new PublicPlayerInfo();
        player2.setBankroll(2.30D);
        gameInfo.setPlayer(1, player2);

        PublicPlayerInfo player3 = new PublicPlayerInfo();
        player3.setBankroll(0.0D);
        player3.setSittingOut(true);
        gameInfo.setPlayer(2, player3);

        rebuy.checkPlayerRebuy(gameInfo);

        assertThat(player1.getBankRoll(), is(2.00D));
        assertThat(player2.getBankRoll(), is(2.00D));
        assertThat(player3.getBankRoll(), is(2.00D));
        assertThat(player3.isSittingOut(), is(false));
    }
}
