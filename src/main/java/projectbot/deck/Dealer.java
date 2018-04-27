package projectbot.deck;

import com.biotools.meerkat.Card;
import com.biotools.meerkat.Hand;
import projectbot.deck.Deck;
import projectbot.enums.ActionEnum;
import projectbot.enums.RoundEnum;

import java.util.LinkedList;

import static projectbot.enums.RoundEnum.FLOP;
import static projectbot.enums.RoundEnum.RIVER;
import static projectbot.enums.RoundEnum.TURN;

public class Dealer {

    public LinkedList<Card> existingCards;
    public Deck deck;

    public Dealer(Card c1, Card c2){
        deck = new Deck();
        deck.removeCardFromDeck(c1);
        deck.removeCardFromDeck(c2);
    }

    public Deck getDeck() {
        return deck;
    }

    // Substituting chance node idea; just deals random cards for now
    public Hand dealRandom(RoundEnum round, Hand boardCards, Deck deck) {

        // Flop: deal 3 cards out of 50*49*48 possibilities
        if (round == FLOP) {
            Card c1 = deck.selectRandomCard();
            Card c2 = deck.selectRandomCard();
            Card c3 = deck.selectRandomCard();
            boardCards.addCard(c1);
            boardCards.addCard(c2);
            boardCards.addCard(c3);
            deck.putCardBackInDeck(c1);
            deck.putCardBackInDeck(c2);
            deck.putCardBackInDeck(c3);
        }

        // Turn: deal 1 card out of 46/47 possibilities
        if (round == TURN || round == RIVER) {
            Card c = deck.selectRandomCard();
            boardCards.addCard(c);
            deck.putCardBackInDeck(c);
        }
        return boardCards;

    }
}
