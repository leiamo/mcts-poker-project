package projectbot.deck;

import com.biotools.meerkat.Card;

import java.util.LinkedList;
import java.util.Random;

public class Deck {

    public LinkedList<Card> cards;
    public Deck() {
        // initialise cards in deck
        cards = new LinkedList<Card>();
        for (int i = 0; i < 52; i++) {
            cards.add(new Card(i));
        }
    }

    public Card selectRandomCard(){
        Random rand = new Random();
        int randomNumber = rand.nextInt(cards.size()+1);    // generates random number between 1 and 52 (max deck size)
        if (randomNumber!=0)
            randomNumber-=1;

        return cards.remove(randomNumber);
    }

    public void removeCardFromDeck(Card card){
        cards.remove(card);
    }

    public void putCardBackInDeck(Card card){
        cards.add(card);

    }
}
