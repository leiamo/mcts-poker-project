package game;

import game.deck.Deck;

import com.biotools.meerkat.Action;
import com.biotools.meerkat.Hand;

public class Dealer {
    private PublicGameInfo gameInfo;

    protected int buttonSeat = 0;
    protected int smallBlindSeat = 1;
    protected int bigBlindSeat = 2;

    private Deck deck;

    public Dealer(Deck deck, PublicGameInfo gameInfo) {
        this.deck = deck;
        this.gameInfo = gameInfo;
        buttonSeat = gameInfo.nextActivePlayer(gameInfo.getNumSeats() - 1);

        // NOTE: very important change made to testbed code! This is not mine, but I noticed a bug...
        // We are doing heads-up, so small blind is now the button seat, not the person sat next to the button seat.
        // Old code:
        /*smallBlindSeat = gameInfo.nextActivePlayer(buttonSeat);
        bigBlindSeat = gameInfo.nextActivePlayer(smallBlindSeat);*/
        if (gameInfo.getNumPlayers() == 2) {
            bigBlindSeat = gameInfo.nextActivePlayer(buttonSeat);
            smallBlindSeat = gameInfo.nextActivePlayer(bigBlindSeat);
        }
        else {
            smallBlindSeat = gameInfo.nextActivePlayer(buttonSeat);
            bigBlindSeat = gameInfo.nextActivePlayer(smallBlindSeat);
        }

    }

    public void playHand() {
        deck.nextGame();

        gameInfo.newHand(buttonSeat, smallBlindSeat, bigBlindSeat);
        gameInfo.update(Action.smallBlindAction(gameInfo.getSmallBlindSize()), smallBlindSeat);
        gameInfo.update(Action.bigBlindAction(gameInfo.getBigBlindSize()), bigBlindSeat);

        gameInfo.observersFireDealHoleCardsEvent();

        // now we deal holecards - unlike in the realgame always in the
        // same order - nobody would recognize ;)
        for (int seat = 0; seat < gameInfo.getNumSeats(); seat++) {
            PublicPlayerInfo player = gameInfo.getPlayer(seat);
            if (player != null && player.isActive()) {
                Hand holeCardsForPlayer = deck.getPlayerCards(seat);
                player.getBot().holeCards(holeCardsForPlayer.getFirstCard(), holeCardsForPlayer.getSecondCard(), seat);
                player.setCards(holeCardsForPlayer);
                gameInfo.observersFireDealHoleCardsEvent(holeCardsForPlayer.getFirstCard(), holeCardsForPlayer.getSecondCard(), seat);
            }
        }

        playStage();

        // flop
        // if there are at least two players still in the game, we proceed
        // the players could be all in already, but this is handled in
        // playStage();
        if (gameInfo.getNumActivePlayers() > 1) {
            Hand flop = new Hand();
            flop.addCard(deck.getCommunityCard(0));
            flop.addCard(deck.getCommunityCard(1));
            flop.addCard(deck.getCommunityCard(2));
            gameInfo.nextStage(flop);
            playStage();
        }

        // turn
        if (gameInfo.getNumActivePlayers() > 1) {
            Hand turn = new Hand();
            turn.addCard(deck.getCommunityCard(3));
            gameInfo.nextStage(turn);
            playStage();
        }

        // river
        if (gameInfo.getNumActivePlayers() > 1) {
            Hand river = new Hand();
            river.addCard(deck.getCommunityCard(4));
            gameInfo.nextStage(river);
            playStage();
        }
        gameInfo.payout();

        makeSitOut();
    }

    /**
     * sets all players to 'sittingOut' who have a bankroll of 0
     */
    private void makeSitOut() {
        for (int seat = 0; seat < gameInfo.getNumSeats(); seat++) {
            PublicPlayerInfo player = gameInfo.getPlayer(seat);
            if (player != null && player.getBankRoll() <= 0.001) {
                player.setSittingOut(true);
            }

        }
    }

    /**
     * asks all players for their actions until noone it to
     * act anymore
     */
    private void playStage() {
        while (gameInfo.getNumToAct() > 0) {
            int currentPlayerSeat = gameInfo.getCurrentPlayerSeat();
            PublicPlayerInfo currentPlayer = gameInfo.getPlayer(currentPlayerSeat);
            gameInfo.update(currentPlayer.getBot().getAction(), currentPlayerSeat);
        }
    }

    /**
     * moves the button one seat clockwise
     */
    public void moveButton() {
        // we don't just increment the button-seat and take the next active seats as small/bigblind.
        // this is because in this round the big-blind could have been busted and is not active anymore:<br>
        // UTG BB(busted) SB D
        // If we just move the dealer, UTG would become SB and never had to pay a Big Blind which is wrong

        buttonSeat = nextActivePlayer(buttonSeat);
        smallBlindSeat = nextActivePlayer(buttonSeat);
        bigBlindSeat = nextActivePlayer(smallBlindSeat);
    }

    private int nextActivePlayer(int seat) {
        // don't call 'gameInfo.isActive()' because this would jump over sitting-out players.
        // this is not correct for tourneys
        // with this implementation sitting-out players in cash-games also have to pay their blinds - if they
        // don't want to, they have to stand up
        do {
            seat = (seat + 1) % gameInfo.getNumSeats();
        } while (gameInfo.getPlayer(seat) == null || gameInfo.getPlayer(seat).getBankRoll() < 0.001);

        return seat;
    }
}
