package game;

import com.biotools.meerkat.Action;
import com.biotools.meerkat.GameInfo;
import com.biotools.meerkat.Hand;
import com.biotools.meerkat.Player;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests the PublicGameInfo
 */
public class PublicGameInfoTest {

    /**
     * tests correct working of the {@link GameInfo#getNumToAct()} method.
     * We play a game to the turn and check for correct results<br>
     * - preflop: is complicated, because SB+BB count double (posting the blinds and calling/checking)
     * - flop: all check
     * - turn: a bet and a reraise - getNumToAct alternates
     */
    @Test
    public void testNumToAct() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setBlinds(0.01, 0.02);
        gameInfo.setNumSeats(4);
        gameInfo.setPlayer(0, PublicPlayerInfo.create("player1", 100, null));
        gameInfo.setPlayer(1, PublicPlayerInfo.create("player2", 100, null));
        gameInfo.setPlayer(2, PublicPlayerInfo.create("player3", 100, null));
        gameInfo.setPlayer(3, PublicPlayerInfo.create("player4", 100, null));
        gameInfo.newHand(0, 1, 2);

        // preflop
        // -------
        // as long as noone calls, we wait for everyone to the SB:
        // (player1: post SB, player2: post BB, player3:  ??, player0: ??, player(SB): ??)
        assertEquals(5, gameInfo.getNumToAct());
        gameInfo.update(Action.smallBlindAction(0.01), 1);
        assertEquals(4, gameInfo.getNumToAct());
        gameInfo.update(Action.bigBlindAction(0.02), 2);
        // pl-3: ??, pl-0: ??, pl-1 (SB): ??,
        assertEquals(3, gameInfo.getNumToAct());
        // now someone calls - this make the bigblind need to check
        gameInfo.update(Action.callAction(0.02), 3);
        // pl-0: ??, pl-1 (SB): ??, pl-2(BB) (check/raise??)
        assertEquals(3, gameInfo.getNumToAct());
        gameInfo.update(Action.callAction(0.02), 0);
        assertEquals(2, gameInfo.getNumToAct());
        gameInfo.update(Action.callAction(0.01), 1);
        gameInfo.update(Action.checkAction(), 2);
        assertEquals(0, gameInfo.getNumToAct());

        // flop
        // -------
        gameInfo.nextStage(new Hand("8s 8c 8h"));
        // 4 players, all check
        assertEquals(4, gameInfo.getNumToAct());
        gameInfo.update(Action.checkAction(), 1);
        gameInfo.update(Action.checkAction(), 2);
        assertEquals(2, gameInfo.getNumToAct());
        gameInfo.update(Action.checkAction(), 3);
        gameInfo.update(Action.checkAction(), 0);
        assertEquals(0, gameInfo.getNumToAct());

        // turn
        // -------
        gameInfo.nextStage(new Hand("8d"));
        // 4 players, 2 check, 1 raise, all other have to call
        assertEquals(4, gameInfo.getNumToAct());
        gameInfo.update(Action.checkAction(), 1);
        gameInfo.update(Action.checkAction(), 2);
        assertEquals(2, gameInfo.getNumToAct());
        // one bet, so remaining 3 have to call (or fold)
        gameInfo.update(Action.betAction(0.2), 3);
        assertEquals(3, gameInfo.getNumToAct());
        gameInfo.update(Action.callAction(0.02), 0);
        gameInfo.update(Action.foldAction(0.02), 1);
        assertEquals(1, gameInfo.getNumToAct());
        // reraise - one folded so two are left to call
        gameInfo.update(Action.raiseAction(0.02, 0.02), 2);
        assertEquals(2, gameInfo.getNumToAct());
        gameInfo.update(Action.callAction(0.02), 3);
        gameInfo.update(Action.callAction(0.02), 0);
        assertEquals(0, gameInfo.getNumToAct());
    }

    /**
     * Test for issue #19.
     * When one player is all-in and the other decides to raise,
     * getNumToAct should be 0 for this round.
     */
    @Test
    public void testNumToActAllInRaise() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setBlinds(0.01, 0.02);
        gameInfo.setNumSeats(3);
        gameInfo.setPlayer(0, PublicPlayerInfo.create("player1", 100, null));
        gameInfo.setPlayer(1, PublicPlayerInfo.create("player2", 100, null));
        gameInfo.setPlayer(2, PublicPlayerInfo.create("player3", 200, null));
        gameInfo.newHand(0, 1, 2);

        gameInfo.update(Action.smallBlindAction(0.01), 1);
        gameInfo.update(Action.bigBlindAction(0.02), 2);
        gameInfo.update(Action.foldAction(0.02), 0);
        gameInfo.update(Action.callAction(0.01), 1);

        // flop
        // -------
        gameInfo.nextStage(new Hand("8s 8c 8h"));
        assertEquals(2, gameInfo.getNumToAct());
        gameInfo.update(Action.betAction(99.98), 1); // all-in
        assertEquals(1, gameInfo.getNumToAct());
        gameInfo.update(Action.raiseAction(99.98, 50), 2);
        // strange raise, but the other one is all-in so noone to act anymore
        assertEquals(0, gameInfo.getNumToAct());
    }

    /**
     * Tests correct work of {@link GameInfo#getAmountToCall(int)}.<br>
     * AmountToCall is the difference of the current players bet compared to
     * the highest bet.<br>
     * In PokerAcademy it is also capped to
     */
    @Test
    public void testGetAmountToCall() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setBlinds(5, 10);
        gameInfo.setNumSeats(3);
        gameInfo.setPlayer(0, PublicPlayerInfo.create("player0", 100, null));
        gameInfo.setPlayer(1, PublicPlayerInfo.create("player1", 50, null));
        gameInfo.setPlayer(2, PublicPlayerInfo.create("player2", 100, null));
        gameInfo.newHand(0, 1, 2);

        gameInfo.update(Action.smallBlindAction(5), 1);
        gameInfo.update(Action.bigBlindAction(10), 2);
        assertEquals(10, gameInfo.getAmountToCall(0), 0.001);
        assertEquals(5, gameInfo.getAmountToCall(1), 0.001);
        assertEquals(0, gameInfo.getAmountToCall(2), 0.001);

        gameInfo.update(Action.raiseAction(10, 90), 0);
        assertEquals(0, gameInfo.getAmountToCall(0), 0.001);
        // player 1 posted SB, but is capped to a call of 50
        assertEquals(45, gameInfo.getAmountToCall(1), 0.001);
        // player 2 has 90 left and can call
        assertEquals(90, gameInfo.getAmountToCall(2), 0.001);
    }

    /**
     * Tests the building of SidePots. Here we create sort of a worst
     * case scenario:
     * player 2+3: small+bigblind 5/10
     * player 0 raise 100:
     * mainpot(player1)=5+10+100
     * player 1 call 50 (all-in):
     * mainpot(player1+2)=5+10+50+50, sidepot1(player1)=50
     * player 2 call 70 (5SB+70=all-in):
     * mainPot(player1+2+3)=50+50+50, sidepot1(player1+3)=25+25, sidepot2(player1)=25
     * player 3 call 10 (10BB+10=all-in):
     * mainPot(player1+2+3+4)=20+20+20+20, sidePot1=(player1+2+3)=30+30+30,sidepot2(player1+3)=25+25, sidepot3(player1)=25
     */
    @Test
    public void testPotHandling() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setBlinds(5, 10);
        gameInfo.setNumSeats(4);
        gameInfo.setPlayer(0, PublicPlayerInfo.create("player0", 200, null));
        gameInfo.setPlayer(1, PublicPlayerInfo.create("player1", 50, null));
        gameInfo.setPlayer(2, PublicPlayerInfo.create("player2", 75, null));
        gameInfo.setPlayer(3, PublicPlayerInfo.create("player3", 20, null));
        gameInfo.newHand(1, 2, 3);

        gameInfo.update(Action.smallBlindAction(5), 2);
        gameInfo.update(Action.bigBlindAction(10), 3);
        gameInfo.update(Action.raiseAction(10, 90), 0);
        assertEquals(115, gameInfo.getMainPotSize(), 0.001);
        // player 1, call 50 (all-in)
        // mainpot(player 2(SB),3(BB),0,1(caller)=5+10+50+50, sidepot1(player 0)=50
        gameInfo.update(Action.callAction(50), 1);

        assertEquals(5 + 10 + 50 + 50, gameInfo.getMainPotSize(), 0.001);
        assertEquals(50, gameInfo.getSidePotSize(0), 0.001);

        // player 2, call 70 (all-in)
        // mainPot(player 2(SB), 3(BB), 0,1)=50+10+50+50, sidepot1(player 0, 2)=25+25, sidepot2(player0)=25
        gameInfo.update(Action.callAction(70), 2);
        assertEquals(50 + 10 + 50 + 50, gameInfo.getMainPotSize(), 0.001);
        assertEquals(25 + 25, gameInfo.getSidePotSize(0), 0.001);
        assertEquals(25, gameInfo.getSidePotSize(1), 0.001);

        // player 3 call 10 (10BB+10=all-in):
        // mainPot(player 2(SB), 3(BB), 0,1)=20+20+20+20, sidepot1(player 2, 0, 1)=30+30+30, sidepot2(player0+2)=25+25, sidepot3(player0)=25
        gameInfo.update(Action.callAction(10), 3);
        assertEquals(20 + 20 + 20 + 20, gameInfo.getMainPotSize(), 0.001);
        assertEquals(30 + 30 + 30, gameInfo.getSidePotSize(0), 0.001);
        assertEquals(25 + 25, gameInfo.getSidePotSize(1), 0.001);
        assertEquals(25, gameInfo.getSidePotSize(2), 0.001);
    }

    /**
     * TestMinRaiseSizes for NoLimit:
     * always bigblind at the beginning of a stage, otherwise last raise-amount
     */
    @Test
    public void testMinRaiseSizeNoLimit() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setLimit(PublicGameInfo.NO_LIMIT);
        gameInfo.setBlinds(5, 10);
        gameInfo.setNumSeats(3);
        gameInfo.setPlayer(0, PublicPlayerInfo.create("player0", 200, null));
        gameInfo.setPlayer(1, PublicPlayerInfo.create("player1", 200, null));
        gameInfo.setPlayer(2, PublicPlayerInfo.create("player2", 200, null));
        gameInfo.newHand(0, 1, 2);

        gameInfo.update(Action.smallBlindAction(5), 1);
        gameInfo.update(Action.bigBlindAction(10), 2);
        assertEquals("minraise bigblind", 10, gameInfo.getMinRaise(), 0.001);
        gameInfo.update(Action.raiseAction(10, 50), 0);
        assertEquals("minraise 50", 50, gameInfo.getMinRaise(), 0.001);
        gameInfo.update(Action.foldAction(55), 1);
        gameInfo.update(Action.callAction(50), 2);

        gameInfo.nextStage(new Hand("7c 7s 7h"));
        assertEquals("minraise bigblind", 10, gameInfo.getMinRaise(), 0.001);
        gameInfo.update(Action.betAction(60), 2);
        assertEquals("minraise 60", 60, gameInfo.getMinRaise(), 0.001);
        gameInfo.update(Action.callAction(60), 0);
        gameInfo.nextStage(new Hand("7d"));
        assertEquals("minraise bigblind", 10, gameInfo.getMinRaise(), 0.001);
    }

    /**
     * TestMinRaiseSizes for Limit:
     * bigblind for preflop+flop, bigbet for turn and river
     */

    @Test
    public void testMinRaiseSizeLimit() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setLimit(PublicGameInfo.FIXED_LIMIT);
        gameInfo.setBlinds(5, 10);
        gameInfo.setNumSeats(3);
        gameInfo.setPlayer(0, PublicPlayerInfo.create("player0", 100, null));
        gameInfo.setPlayer(1, PublicPlayerInfo.create("player1", 100, null));
        gameInfo.setPlayer(2, PublicPlayerInfo.create("player2", 100, null));
        gameInfo.newHand(0, 1, 2);

        gameInfo.update(Action.smallBlindAction(5), 1);
        gameInfo.update(Action.bigBlindAction(10), 2);
        assertEquals("minraise bigblind", 10, gameInfo.getMinRaise(), 0.001);
        gameInfo.nextStage(new Hand("7c 7s 7h"));
        assertEquals("minraise bigblind", 10, gameInfo.getMinRaise(), 0.001);
        gameInfo.nextStage(new Hand("7d"));
        assertEquals("minraise bigbet", 20, gameInfo.getMinRaise(), 0.001);
    }

    /**
     * Test the values of {@link PublicGameInfo#getNumRaises()}.
     * According to issue #29, PokerAcademy also counts sb
     * and bets as 'numRaise'.
     * Also after each round numRaises starts at 0
     */
    @Test
    public void testNumRaises() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setLimit(PublicGameInfo.NO_LIMIT);
        gameInfo.setBlinds(5, 10);
        gameInfo.setNumSeats(3);
        gameInfo.setPlayer(0, PublicPlayerInfo.create("player0", 100, null));
        gameInfo.setPlayer(1, PublicPlayerInfo.create("player1", 100, null));
        gameInfo.setPlayer(2, PublicPlayerInfo.create("player2", 100, null));
        gameInfo.newHand(0, 1, 2);

        gameInfo.update(Action.smallBlindAction(5), 1);
        assertEquals("sb counts as raise", 1, gameInfo.getNumRaises());
        gameInfo.update(Action.bigBlindAction(10), 2);
        assertEquals("bb no extra raise", 1, gameInfo.getNumRaises());
        gameInfo.update(Action.callAction(10), 0);
        gameInfo.nextStage(new Hand("7c 7s 7h"));
        assertEquals("num raises = 0 after stage", 0, gameInfo.getNumRaises());
        gameInfo.update(Action.betAction(10), 1);
        assertEquals("bet is raise", 1, gameInfo.getNumRaises());
    }

    @Test
    public void testBigBlindPlayerDoesNotHaveEnoughToCallTheBlind() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setLimit(PublicGameInfo.NO_LIMIT);
        gameInfo.setBlinds(5, 10);
        gameInfo.setNumSeats(3);
        gameInfo.setPlayer(0, PublicPlayerInfo.create("player0", 20, null));
        gameInfo.setPlayer(1, PublicPlayerInfo.create("player1", 20, null)); // SB
        gameInfo.setPlayer(2, PublicPlayerInfo.create("player2", 2, null)); // BB
        gameInfo.newHand(0, 1, 2);

        gameInfo.update(Action.smallBlindAction(5), 1);

        gameInfo.update(Action.bigBlindAction(10), 2);
        assertThat("A new side pot is created", gameInfo.getPotManager().getNumPots(), is(2));
        assertThat("Main pot has all-in BB plus the same value from SB", gameInfo.getPotManager().getPot(0).getValue(), is(4.0));
        assertThat("Side pot has SB minus all-in BB", gameInfo.getPotManager().getPot(1).getValue(), is(3.0));

        assertThat("BB has no money left", gameInfo.getPlayer(2).getBankRoll(), is(0.0));
        assertThat("There is one all-in player (the BB)", gameInfo.getNumberOfAllInPlayers(), is(1));
        assertThat(gameInfo.getAmountToCall(0), is(5.0));
        assertThat(gameInfo.getCurrentBetSize(), is(10.0));

        gameInfo.update(Action.foldAction(10), 0);
        gameInfo.update(Action.foldAction(5), 1);

        gameInfo.payout();

        assertThat("BB received the uncalled bet from main pot", gameInfo.getPlayer(2).getBankRoll(), is(4.0));
        assertThat("SB received the uncalled bet from side pot", gameInfo.getPlayer(1).getBankRoll(), is(18.0));
    }


    @Test
    public void shouldRemovePlayerFromGame() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setLimit(PublicGameInfo.NO_LIMIT);
        gameInfo.setBlinds(5, 10);
        gameInfo.setNumSeats(3);
        gameInfo.setPlayer(0, PublicPlayerInfo.create("player0", 100, null));
        gameInfo.setPlayer(1, PublicPlayerInfo.create("player1", 100, null));

        Player bot2 = mock(Player.class);
        PublicPlayerInfo player2 = PublicPlayerInfo.create("player2", 100, bot2);
        player2.setSittingOut(true);
        gameInfo.setPlayer(2, player2);

        gameInfo.removePlayer(player2);

        assertThat(gameInfo.getNumActivePlayers(), is(2));
        assertThat(gameInfo.getPlayer(2), is(nullValue()));

        gameInfo.observersFireGameOverEvent();
        verify(bot2, never()).gameOverEvent();
    }

    @Test
    public void shouldOnlyRemovePlayerFromGameWhenHesSittingOut() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setLimit(PublicGameInfo.NO_LIMIT);
        gameInfo.setBlinds(5, 10);
        gameInfo.setNumSeats(1);
        PublicPlayerInfo player0 = PublicPlayerInfo.create("player0", 100, null);
        gameInfo.setPlayer(0, player0);

        try {
            gameInfo.removePlayer(player0);
            assertFalse("IllegalArgumentException not thrown!", false);
        } catch (IllegalArgumentException e) {
        }

        player0.setSittingOut(true);
        gameInfo.removePlayer(player0);
        assertThat(gameInfo.getNumActivePlayers(), is(0));
    }

    @Test
    public void shouldSeatANewPlayerInAnAvailableSeat() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setLimit(PublicGameInfo.NO_LIMIT);
        gameInfo.setBlinds(5, 10);
        gameInfo.setNumSeats(3);
        gameInfo.setPlayer(0, PublicPlayerInfo.create("player0", 100, null));
        gameInfo.setPlayer(1, PublicPlayerInfo.create("player1", 100, null));
        gameInfo.setPlayer(2, PublicPlayerInfo.create("player2", 100, null));
        gameInfo.getPlayer(2).setSittingOut(true);
        gameInfo.seatPlayer(PublicPlayerInfo.create("player3", 100, null));

        assertThat(gameInfo.getPlayer(2).getName(), is("player3"));
    }

    @Test
    public void shouldSetSittingOutToFalseWhenSeatingPlayer() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setLimit(PublicGameInfo.NO_LIMIT);
        gameInfo.setBlinds(5, 10);
        gameInfo.setNumSeats(3);
        PublicPlayerInfo player = PublicPlayerInfo.create("player", 100, null);
        player.setSittingOut(true);

        gameInfo.seatPlayer(player);

        assertThat(gameInfo.getPlayer(0).getName(), is("player"));
        assertThat(gameInfo.getPlayer(0).isSittingOut(), is(false));
    }


    @Test
    public void shouldSeatANewPlayerInANewPlaceIfNoneIsAvailable() {
        PublicGameInfo gameInfo = new PublicGameInfo();
        gameInfo.setLimit(PublicGameInfo.NO_LIMIT);
        gameInfo.setBlinds(5, 10);
        gameInfo.setNumSeats(3);
        gameInfo.setPlayer(0, PublicPlayerInfo.create("player0", 100, null));
        gameInfo.setPlayer(1, PublicPlayerInfo.create("player1", 100, null));
        gameInfo.setPlayer(2, PublicPlayerInfo.create("player2", 100, null));
        int seat = gameInfo.seatPlayer(PublicPlayerInfo.create("player3", 100, null));

        assertThat(seat, is(3));
        assertThat(gameInfo.getPlayer(3).getName(), is("player3"));
    }
}
