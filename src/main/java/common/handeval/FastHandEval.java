package common.handeval;

import com.biotools.meerkat.Card;
import com.biotools.meerkat.Hand;
import com.biotools.meerkat.HandEval;
import com.biotools.meerkat.HandEvaluator;
import common.handeval.klaatu.PartialStageFastEval;

import java.io.Console;

public class FastHandEval implements HandEval {

    static {
        HandEvaluator.setHandEval(new FastHandEval());
    }

    @Override
    public int rankHand(Hand hand) {
        // TODO Use just the indexes
        Card c1 = hand.getFirstCard();
        Card c2 = hand.getSecondCard();
        int c1Index = PartialStageFastEval.encode(c1.getRank(), c1.getSuit());
        int c2Index = PartialStageFastEval.encode(c2.getRank(), c2.getSuit());

        int[] boardIndexes = new int[hand.size() - 2];

        for (int i = 2; i < hand.size(); i++) {
            Card card = hand.getCard(i + 1);
            boardIndexes[i] = PartialStageFastEval.encode(card.getRank(), card.getSuit());
        }

        return eval(boardIndexes, c1Index, c2Index);
    }

    @Override
    public int rankHand7(Hand hand) {
        return rankHand(hand);
    }

    @Override
    public int rankHand6(Hand hand) {
        return rankHand(hand);
    }

    @Override
    public int rankHand5(Hand hand) {
        return rankHand(hand);
    }

    private int eval(int[] boardIndexes, int c1Index, int c2Index) {
        if (boardIndexes.length == 5) {
            return PartialStageFastEval.eval7(boardIndexes[0], boardIndexes[1], boardIndexes[2], boardIndexes[3], boardIndexes[4], c1Index, c2Index);
        } else if (boardIndexes.length == 4) {
            return PartialStageFastEval.eval6(boardIndexes[0], boardIndexes[1], boardIndexes[2], boardIndexes[3], c1Index, c2Index);
        } else {
            return PartialStageFastEval.eval5(boardIndexes[0], boardIndexes[1], boardIndexes[2], c1Index, c2Index);
        }
    }

}
