package projectbot.calculators;

import com.biotools.meerkat.Card;
import com.biotools.meerkat.Deck;
import com.biotools.meerkat.Hand;
import game.HandEvalImpl;

import java.text.DecimalFormat;

// Winning Probability Calculator programmed from pseudo-code algorithms published by Billings et al.
// Calculates Win Probability from Hand Strength and positive/negative Hand Potentials
// See http://poker.cs.ualberta.ca/publications/billings.phd.pdf p45-49

public class WinProbCalculator {

    static HandEvalImpl handEvaluator = new HandEvalImpl();
    static DecimalFormat df4 = new DecimalFormat("#.####");
    static PreflopCalculator preflopCalculator;

    public WinProbCalculator(String homeDir){
        preflopCalculator = new PreflopCalculator(homeDir);
    }

    // WIN_PROB = HS × (1 − NPot) + (1 − HS) × PPot
    public double calcWinProb(Card[] holeCards, Hand boardCards) {
        try {
            if (boardCards.size()==0) {
                String[] strCards = new String[]{holeCards[0].toString(), holeCards[1].toString()};
                return Double.parseDouble(df4.format(preflopCalculator.getWinningProbabilityPreflop(strCards)));
            }
            double HS = HandStrength(holeCards, boardCards);
            double[] HP = HandPotential(holeCards, boardCards);
            double PPot = HP[0];
            double NPot = HP[1];
            double winProb = (HS * (1 - NPot) + (1 - HS) * PPot);
            return Double.parseDouble(df4.format(winProb));
        } catch (Exception e) {
            e.printStackTrace();
            return 0.5;
        }
    }

    public Deck extractCards(Deck deck, Card[] cards) {
        for (Card c : cards) {
            if (c != null && deck.inDeck(c))
                deck.extractCard(c);
        }
        return deck;
    }

    public Hand formHand(Hand board, Card[] cards) {
        Hand hand = new Hand();
        hand.addHand(board);
        for (Card c : cards) {
            hand.addCard(c);
        }
        return hand;
    }

    public double HandStrength(Card[] holeCards, Hand board) {
        double ahead = 0;
        double tied = 0;
        double behind = 0;

        // check these card conversions...
        Card c1 = holeCards[0];
        Card c2 = holeCards[1];

        // Get card ranking
        double rank = handEvaluator.rankHand(formHand(board, new Card[]{c1, c2}));

        Deck deck2, deck1 = new Deck();
        extractCards(deck1, new Card[]{c1, c2});
        deck1.extractHand(board);

        double oppRank;
        Card c1Opp, c2Opp;
        /* Consider all two-card combinations of the remaining cards. */
        while (deck1.cardsLeft() > 0) {
            c1Opp = deck1.extractRandomCard();
            deck2 = new Deck();
            deck2.extractHand(board);
            extractCards(deck2, new Card[]{c1Opp, c1, c2});

            while (deck2.cardsLeft() > 0) {
                c2Opp = deck2.extractRandomCard();
                oppRank = handEvaluator.rankHand(formHand(board, new Card[]{c1Opp, c2Opp}));
                if (rank > oppRank) ahead += 1;
                else if (rank == oppRank) tied += 1;
                else behind += 1;
            }
        }
        return ((ahead + tied) / (ahead + tied + behind));
    }

    public double[] HandPotential(Card[] holeCards, Hand board) {
        /* Hand Potential array, each index represents ahead, tied, and behind. */
        double[][] HP = new double[3][3];  /* initialize to 0 */
        double[] HPTotal = new double[3]; /* initialize to 0 */

        Card c1 = holeCards[0];
        Card c2 = holeCards[1];

        if (board.size() == 5) return new double[]{0, 0};

        double rank = handEvaluator.rankHand(formHand(board, new Card[]{c1, c2}));
        /* Consider all two-card combinations of the remaining cards for the opponent. */
        Deck deck2, deck1 = new Deck();
        deck1.extractHand(board);
        extractCards(deck1, new Card[]{c1, c2});
        double oppRank, best, oppBest;
        int ahead = 0;
        int tied = 1;
        int behind = 2;
        int index;

        Card c1Opp, c2Opp;
        // for each pair of opponent card combo possibilities
        while (deck1.cardsLeft() > 0) {
            c1Opp = deck1.extractRandomCard();
            deck2 = new Deck();
            deck2.extractHand(board);
            extractCards(deck2, new Card[]{c1Opp, c1, c2});

            while (deck2.cardsLeft() > 0) {
                c2Opp = deck2.extractRandomCard();
                oppRank = handEvaluator.rankHand(formHand(board, new Card[]{c1Opp, c2Opp}));

                if (rank > oppRank) index = ahead;
                else if (rank == oppRank) index = tied;
                else index = behind;
                // update HP total for whether starts off behind/tied/ahead
                HPTotal[index] += 1;

                Card[] extractedCards = new Card[9];
                extractedCards[0] = c1Opp;
                extractedCards[1] = c2Opp;
                extractedCards[2] = c1;
                extractedCards[3] = c2;

                Deck deck7, deck6, deck5, deck4, deck3 = new Deck();
                Card bCard1, bCard2, bCard3, bCard4, bCard5;
                deck3.extractHand(board);
                extractCards(deck3, extractedCards);
                while (deck3.cardsLeft() > 0) {
                    bCard1 = deck3.extractRandomCard();
                    extractedCards[4] = bCard1;
                    if (board.size() == 4) {
                        // Formulate board
                        board.addCard(bCard1);
                        // Get best ranks
                        best = handEvaluator.rankHand(formHand(board, new Card[]{c1, c2}));
                        oppBest = handEvaluator.rankHand(formHand(board, new Card[]{c1Opp, c2Opp}));
                        if (best > oppBest) HP[index][ahead] += 1;
                        else if (best == oppBest) HP[index][tied] += 1;
                        else /* < */ HP[index][behind] += 1;
                        board.removeCard();
                    } else {
                        deck4 = new Deck();
                        deck4.extractHand(board);
                        extractCards(deck4, extractedCards);
                        while (deck4.cardsLeft() > 0) {
                            bCard2 = deck4.extractRandomCard();
                            extractedCards[5] = bCard2;
                            // if just generating river and turn cards
                            if (board.size() == 3) {
                                // Formulate board
                                board.addCard(bCard1);
                                board.addCard(bCard2);
                                // Get best ranks
                                best = handEvaluator.rankHand(formHand(board, new Card[]{c1, c2}));
                                oppBest = handEvaluator.rankHand(formHand(board, new Card[]{c1Opp, c2Opp}));
                                if (best > oppBest) HP[index][ahead] += 1;
                                else if (best == oppBest) HP[index][tied] += 1;
                                else /* < */ HP[index][behind] += 1;
                                board.removeCard();
                                board.removeCard();
                            }

                            // else if generating flop, turn and river cards (board is empty initially)
                            else {
                                // New deck to get all possible
                                deck5 = new Deck();
                                deck5.extractHand(board);
                                extractCards(deck5, extractedCards);
                                while (deck5.cardsLeft() > 0) {
                                    bCard3 = deck5.extractRandomCard();
                                    extractedCards[6] = bCard3;

                                    // New deck to get all possible
                                    deck6 = new Deck();
                                    deck6.extractHand(board);
                                    extractCards(deck6, extractedCards);
                                    while (deck6.cardsLeft() > 0) {
                                        bCard4 = deck6.extractRandomCard();
                                        extractedCards[7] = bCard4;

                                        // New deck to get all possible
                                        deck7 = new Deck();
                                        deck7.extractHand(board);
                                        extractCards(deck7, extractedCards);
                                        while (deck7.cardsLeft() > 0) {
                                            bCard5 = deck7.extractRandomCard();
                                            extractedCards[8] = bCard5;

                                            // Formulate board
                                            board.addCard(bCard1);
                                            board.addCard(bCard2);
                                            board.addCard(bCard3);
                                            board.addCard(bCard4);
                                            board.addCard(bCard5);

                                            // Get best ranks
                                            best = handEvaluator.rankHand(formHand(board, new Card[]{c1, c2}));
                                            oppBest = handEvaluator.rankHand(formHand(board, new Card[]{c1Opp, c2Opp}));
                                            if (best > oppBest) HP[index][ahead] += 1;
                                            else if (best == oppBest) HP[index][tied] += 1;
                                            else /* < */ HP[index][behind] += 1;

                                            board.removeCard();
                                            board.removeCard();
                                            board.removeCard();
                                            board.removeCard();
                                            board.removeCard();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        double numTimes = (HP[behind][ahead] + HP[behind][tied] + HP[tied][ahead]);
        double total = HP[behind][ahead] + HP[behind][tied] + HP[behind][behind] + HP[tied][ahead] + HP[tied][tied] + HP[tied][behind];
        double p = 0.0;
        if (numTimes > 0 && total > 0)
            p = numTimes / total;

        double numTimes1 = HP[ahead][behind] + HP[tied][behind] + HP[ahead][tied];
        double total1 = HP[ahead][behind] + HP[ahead][tied] + HP[ahead][ahead] + HP[tied][behind] + HP[tied][tied] + HP[tied][ahead];
        double n = 0.0;
        if (numTimes1 > 0 && total1 > 0)
            n = numTimes1 / total1;

        return new double[]{p, n};
    }
}

