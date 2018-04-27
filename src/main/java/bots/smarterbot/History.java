package bots.smarterbot;

import com.biotools.meerkat.Card;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class History {

    private static final String POKER_HISTORY_FILE = "./data/PokerHistory.dat";

    private class HandHistory {

        public HandHistory() {
            count = new int[6];
            profit = new double[6];
            for (int i = 0; i < 6; i++) {
                count[i] = 0;
                profit[i] = 0;
            }
        }

        private int count[];
        private double profit[];
    }

    private HandHistory history[][];

    public History() {
        history = new HandHistory[13][13];
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                history[i][j] = new HandHistory();
            }
        }
        readFile(); // attempt to read data from an existing file. On failure, do nothing
    }

    /**
     * Returns an action that should replace the old action in the playmatrix
     *
     * @param c1     is the first card in players hand
     * @param c2     is the second card in players hand
     * @param profit is how much gain was made in the hand
     * @param rank   is what preflop level the hand was played at
     */
    public Action update(Card c1, Card c2, double profit, int rank, int curRank) {

        double thisProfit = 0, upProfit = 0, downProfit = 0;
        boolean upValid = false, downValid = false;
        HandHistory history;
        if ((c1.getSuit() == c2.getSuit()) == (c1.getRank() <= c2.getRank())) {
            history = this.history[c1.getRank()][c2.getRank()];
        } else {
            history = this.history[c2.getRank()][c1.getRank()];
        }
        history.count[rank]++;
        history.profit[rank] += profit;

        //begin code for recommending a hand level
        if (history.count[curRank] >= 4) { //we have at least 4 tests done at current hypothesized level
            thisProfit = history.profit[curRank] / history.count[curRank];
            if (curRank < 5 && history.count[curRank + 1] >= 3) {
                upProfit = history.profit[curRank + 1] / history.count[curRank + 1];
                upValid = true;
            }
            if (curRank > 0 && history.count[curRank - 1] >= 3) {
                downProfit = history.profit[curRank - 1] / history.count[curRank - 1];
                downValid = true;
            }
            if (upValid && upProfit < thisProfit) {
                upValid = false;
            }
            if (downValid && downProfit < thisProfit) {
                downValid = false;
            }
            if (upValid && downValid) {
                if (upProfit >= downProfit && history.count[curRank + 1] >= history.count[curRank - 1]) {
                    return Action.convert(curRank + 1);
                } else if (upProfit <= downProfit && history.count[curRank + 1] <= history.count[curRank - 1]) {
                    return Action.convert(curRank - 1);
                } else { // simple weighting for confidence interval (I fail at statistics basically)
                    upProfit -= Math.abs(upProfit / (history.count[curRank + 1] * 4));
                    downProfit -= Math.abs(downProfit / (history.count[curRank - 1] * 4));
                    if (upProfit >= downProfit) {
                        return Action.convert(curRank + 1);
                    } else {
                        return Action.convert(curRank - 1);
                    }
                }
            } else if (upValid) {
                return Action.convert(curRank + 1);
            } else if (downValid) {
                return Action.convert(curRank - 1);
            }
        }
        return Action.INVALID;
    }

    public Action getNeeded(Card c1, Card c2, Action levelAction) {

        int order1, order2;
        int level = levelAction.getValue();
        if ((c1.getSuit() == c2.getSuit()) == (c1.getRank() <= c2.getRank())) {
            order1 = c1.getRank();
            order2 = c2.getRank();
        } else {
            order1 = c2.getRank();
            order2 = c1.getRank();
        }
        if (history[order1][order2].count[level] < 4) {
            if ((level > 0 && history[order1][order2].count[level - 1] > 3) || (level < 5 && history[order1][order2].count[level + 1] > 3)) {
                return levelAction;
            }
            return Action.INVALID;
        }
        if (level < 5) {
            if (history[order1][order2].count[level + 1] < 3) {
                return levelAction.oneUp();
            }
        }
        if (level > 0) {
            if (history[order1][order2].count[level - 1] < 3) {
                return levelAction.oneDown();
            }
        }
        return Action.INVALID;
    }

    public boolean writeFile() {

        FileWriter writer;
        try {
            writer = new FileWriter(POKER_HISTORY_FILE, false);
        } catch (IOException e) {
            return false;
        }
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                for (int k = 0; k < 6; k++) {
                    try {
                        writer.write(history[i][j].count[k] + " " + history[i][j].profit[k] + " ");
                    } catch (IOException error) {
                        try {
                            writer.close();
                            return false;
                        } catch (IOException e) {
                            return false;
                        }
                    }
                }
            }
        }
        try {
            writer.close();
        } catch (IOException e) {
        }
        return true;
    }

    public boolean readFile() { // overrides present data with data from file

        Scanner writer;
        HandHistory history[][] = new HandHistory[13][13];
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                history[i][j] = new HandHistory();
            }
        }

        try {
            writer = new Scanner(new File(POKER_HISTORY_FILE));
        } catch (IOException e) {
            return false;
        }
        writer.useDelimiter(" ");
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                for (int k = 0; k < 6; k++) {
                    if (!writer.hasNextInt()) {
                        return false;
                    }
                    history[i][j].count[k] = writer.nextInt();
                    if (!writer.hasNextDouble()) {
                        return false;
                    }
                    history[i][j].profit[k] = writer.nextDouble();
                }
            }
        }
        this.history = history;
        return true;
    }
}
