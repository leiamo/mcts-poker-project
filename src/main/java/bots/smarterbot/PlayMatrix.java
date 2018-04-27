package bots.smarterbot;

import com.biotools.meerkat.Card;


public class PlayMatrix {

    private Action[][] matrix;

    public PlayMatrix() {
        matrix = new Action[13][13];
    }

    /*Note that ascending order card represents suited. Not ascending order is offsuit*/
    public void setDefaultMatrix() {

        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                matrix[i][j] = Action.FOLD;
                matrix[j][i] = Action.FOLD;
            }
        }

        matrix[8][10] = Action.FOLD;
        matrix[10][8] = Action.FOLD;
        matrix[8][11] = Action.FOLD;
        matrix[11][8] = Action.FOLD;

        matrix[0][0] = Action.LIMP; //pocket 2s - 6s
        matrix[1][1] = Action.LIMP;
        matrix[2][2] = Action.LIMP;
        matrix[3][3] = Action.LIMP;
        matrix[4][4] = Action.LIMP;

        matrix[8][9] = Action.LIMP; //10J suited
        matrix[9][10] = Action.LIMP;
        matrix[9][11] = Action.LIMP;

        matrix[9][8] = Action.LIMP; //J10 off suit
        matrix[10][9] = Action.LIMP;
        matrix[11][9] = Action.LIMP;

        matrix[5][5] = Action.CALL; //pocket 7s-9s
        matrix[6][6] = Action.CALL;
        matrix[7][7] = Action.CALL;

        matrix[8][12] = Action.CALL;
        matrix[9][12] = Action.CALL;
        matrix[10][11] = Action.CALL;

        matrix[12][8] = Action.CALL;
        matrix[12][9] = Action.CALL;
        matrix[11][10] = Action.CALL;

        matrix[8][8] = Action.RAISE; //10s, Js
        matrix[9][9] = Action.RAISE;

        matrix[10][12] = Action.RAISE; //AQ suited
        matrix[11][12] = Action.RAISE; //AK suited

        matrix[12][10] = Action.RAISE; //AQ
        matrix[12][11] = Action.RAISE; //AK

        matrix[10][10] = Action.RERAISE; //QQ
        matrix[11][11] = Action.RERAISE; //KK

        matrix[12][12] = Action.PREMIUM; //AA
    }

    /*Note that ascending order card represents suited. Not ascending order is offsuit*/
    public void setAggressiveMatrix() {
      
      /* Initialize to LIMP, since this table has lots of limps */
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                matrix[i][j] = Action.LIMP;
                matrix[j][i] = Action.LIMP;
            }
        }

        matrix[0][0] = Action.CALL;      //pocket 2s - 7s
        matrix[1][1] = Action.CALL;
        matrix[2][2] = Action.CALL;
        matrix[3][3] = Action.CALL;
        matrix[4][4] = Action.CALL;
        matrix[5][5] = Action.CALL;

        matrix[6][6] = Action.RAISE;     //pocket 8s - 9s
        matrix[7][7] = Action.RAISE;

        matrix[8][8] = Action.RERAISE;   //pocket 10s - Qs
        matrix[9][9] = Action.RERAISE;
        matrix[10][10] = Action.RERAISE;

        matrix[11][11] = Action.PREMIUM; //pocket Ks - As
        matrix[12][12] = Action.PREMIUM;

        matrix[1][0] = Action.FOLD;      //fold 2 + (K - 3) unsuited
        matrix[2][0] = Action.FOLD;
        matrix[3][0] = Action.FOLD;
        matrix[4][0] = Action.FOLD;
        matrix[5][0] = Action.FOLD;
        matrix[6][0] = Action.FOLD;
        matrix[7][0] = Action.FOLD;
        matrix[8][0] = Action.FOLD;
        matrix[9][0] = Action.FOLD;
        matrix[10][0] = Action.FOLD;
        matrix[11][0] = Action.FOLD;

        matrix[4][1] = Action.FOLD;      //fold 3 + (6 - K) unsuited
        matrix[5][1] = Action.FOLD;
        matrix[6][1] = Action.FOLD;
        matrix[7][1] = Action.FOLD;
        matrix[8][1] = Action.FOLD;
        matrix[9][1] = Action.FOLD;
        matrix[10][1] = Action.FOLD;
        matrix[11][1] = Action.FOLD;

        matrix[5][2] = Action.FOLD;      //fold 4 + (7 - K) unsuited
        matrix[6][2] = Action.FOLD;
        matrix[7][2] = Action.FOLD;
        matrix[8][2] = Action.FOLD;
        matrix[9][2] = Action.FOLD;
        matrix[10][2] = Action.FOLD;
        matrix[11][2] = Action.FOLD;

        matrix[6][3] = Action.FOLD;      //fold 5 + (8 - K) unsuited
        matrix[7][3] = Action.FOLD;
        matrix[8][3] = Action.FOLD;
        matrix[9][3] = Action.FOLD;
        matrix[10][3] = Action.FOLD;
        matrix[11][3] = Action.FOLD;

        matrix[7][4] = Action.FOLD;      //fold 6 + (9 - K) unsuited
        matrix[8][4] = Action.FOLD;
        matrix[9][4] = Action.FOLD;
        matrix[10][4] = Action.FOLD;
        matrix[11][4] = Action.FOLD;

        matrix[8][5] = Action.FOLD;      //fold 7 + (10 - K) unsuited
        matrix[9][5] = Action.FOLD;
        matrix[10][5] = Action.FOLD;
        matrix[11][5] = Action.FOLD;
      
      /* Calls */
        matrix[1][2] = Action.CALL;      // 2 3 suited
        matrix[2][3] = Action.CALL;      // 4 5 suited
        matrix[2][4] = Action.CALL;      // 4 6 suited
        matrix[2][5] = Action.CALL;      // 4 7 suited
        matrix[3][6] = Action.CALL;      // 5 8 suited
        matrix[4][7] = Action.CALL;      // 6 9 suited
        matrix[5][8] = Action.CALL;      // 7 T suited
        matrix[5][9] = Action.CALL;      // 7 J suited
        matrix[6][10] = Action.CALL;     // 8 Q suited
        matrix[6][11] = Action.CALL;     // 8 K suited
        matrix[7][11] = Action.CALL;     // 9 K suited
        matrix[7][12] = Action.CALL;     // 9 A suited
        matrix[7][6] = Action.CALL;      // 9 8 unsuited
        matrix[9][7] = Action.CALL;      // J 9 unsuited
      
      /* Raises */
        matrix[3][4] = Action.RAISE;     // 5 6 suited
        matrix[3][5] = Action.RAISE;     // 5 7 suited
        matrix[4][5] = Action.RAISE;     // 6 7 suited
        matrix[4][6] = Action.RAISE;     // 6 8 suited
        matrix[5][6] = Action.RAISE;     // 7 8 suited
        matrix[5][7] = Action.RAISE;     // 7 9 suited
        matrix[6][7] = Action.RAISE;     // 8 9 suited
        matrix[6][8] = Action.RAISE;     // 8 T suited
        matrix[6][9] = Action.RAISE;     // 8 J suited
        matrix[7][9] = Action.RAISE;     // 9 J suited
        matrix[7][10] = Action.RAISE;    // 9 Q suited
        matrix[8][10] = Action.RAISE;    // T Q suited
        matrix[8][11] = Action.RAISE;    // T K suited
        matrix[9][11] = Action.RAISE;    // J K suited
        matrix[9][12] = Action.RAISE;    // J A suited
        matrix[8][7] = Action.RAISE;     // T 9 unsuited
        matrix[9][8] = Action.RAISE;     // J T unsuited
        matrix[10][8] = Action.RAISE;    // Q T unsuited
        matrix[11][8] = Action.RAISE;    // K T unsuited
        matrix[12][8] = Action.RAISE;    // A T unsuited
        matrix[10][9] = Action.RAISE;    // Q J unsuited
        matrix[11][9] = Action.RAISE;    // K J unsuited
        matrix[12][9] = Action.RAISE;    // A J unsuited
      
      /* Re-Raises */
        matrix[7][8] = Action.RERAISE;   // 9 T suited
        matrix[8][9] = Action.RERAISE;   // T J suited
        matrix[9][10] = Action.RERAISE;  // J Q suited
        matrix[10][11] = Action.RERAISE; // Q K suited
        matrix[9][12] = Action.RERAISE;  // J A suited
        matrix[10][12] = Action.RERAISE; // Q A suited
        matrix[11][10] = Action.RERAISE; // K Q unsuited
        matrix[12][10] = Action.RERAISE; // A Q unsuited
        matrix[12][11] = Action.RERAISE; // A K unsuited
      
      /* Premium */
        matrix[11][12] = Action.PREMIUM; // A K suited

    }

    public void setNewAggressiveMatrix() {
      /* Initialize to LIMP, since this table has lots of limps */
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                matrix[i][j] = Action.FOLD;
                matrix[j][i] = Action.FOLD;
            }
        }

        matrix[0][0] = Action.RAISE;     //pocket 2s - 7s
        matrix[1][1] = Action.RAISE;
        matrix[2][2] = Action.RAISE;
        matrix[3][3] = Action.RAISE;
        matrix[4][4] = Action.RAISE;
        matrix[5][5] = Action.RAISE;

        matrix[6][6] = Action.RERAISE;   //pocket 8s - Qs
        matrix[7][7] = Action.RERAISE;
        matrix[8][8] = Action.RERAISE;
        matrix[9][9] = Action.RERAISE;
        matrix[10][10] = Action.RERAISE;

        matrix[11][11] = Action.PREMIUM; //pocket Ks - As
        matrix[12][12] = Action.PREMIUM;
      
      /* Limps */
        matrix[0][1] = Action.LIMP;      // 2 4 suited
        matrix[0][2] = Action.LIMP;      // 2 4 suited
        matrix[1][3] = Action.LIMP;      // 3 5 suited
        matrix[1][4] = Action.LIMP;      // 3 6 suited
        matrix[2][5] = Action.LIMP;      // 4 7 suited
        matrix[3][5] = Action.LIMP;      // 5 7 suited
        matrix[3][6] = Action.LIMP;      // 5 8 suited
        matrix[4][7] = Action.LIMP;      // 6 9 suited
        matrix[4][12] = Action.LIMP;     // 6 A suited
        matrix[5][12] = Action.LIMP;     // 7 A suited
        matrix[6][12] = Action.LIMP;     // 8 A suited
        matrix[5][8] = Action.LIMP;      // 7 T suited
        matrix[6][9] = Action.LIMP;      // 8 J suited
        matrix[8][6] = Action.LIMP;      // T 8 unsuited
        matrix[9][6] = Action.LIMP;      // J 8 unsuited
        matrix[10][6] = Action.LIMP;     // Q 8 unsuited
        matrix[11][6] = Action.LIMP;     // K 8 unsuited
        matrix[10][7] = Action.LIMP;     // Q 9 unsuited
        matrix[11][7] = Action.LIMP;     // K 9 unsuited
        matrix[12][0] = Action.LIMP;     // A 2 unsuited
        matrix[12][1] = Action.LIMP;     // A 3 unsuited
        matrix[12][2] = Action.LIMP;     // A 4 unsuited
        matrix[12][3] = Action.LIMP;     // A 5 unsuited
      
      
      /* Calls */
        matrix[0][12] = Action.CALL;      // 2 A suited
        matrix[2][12] = Action.CALL;      // 3 A suited
        matrix[3][12] = Action.CALL;      // 4 A suited
        matrix[4][12] = Action.CALL;      // 5 A suited

        matrix[1][2] = Action.CALL;      // 2 3 suited
        matrix[2][3] = Action.CALL;      // 4 5 suited
        matrix[2][4] = Action.CALL;      // 4 6 suited
        matrix[3][4] = Action.CALL;      // 5 6 suited
        matrix[4][5] = Action.CALL;      // 6 7 suited
        matrix[5][6] = Action.CALL;      // 7 8 suited
        matrix[5][9] = Action.CALL;      // 7 J suited
        matrix[6][7] = Action.CALL;      // 8 9 suited
        matrix[6][10] = Action.CALL;     // 8 Q suited
        matrix[6][11] = Action.CALL;     // 8 K suited
        matrix[7][10] = Action.CALL;     // 9 Q suited
        matrix[7][11] = Action.CALL;     // 9 K suited
        matrix[7][12] = Action.CALL;     // 9 A suited
        matrix[7][6] = Action.CALL;      // 9 8 unsuited
        matrix[9][7] = Action.CALL;      // J 9 unsuited
        matrix[12][6] = Action.CALL;      // A 8 unsuited
        matrix[12][7] = Action.CALL;      // A 9 unsuited
      
      /* Raises */
        matrix[3][5] = Action.RAISE;     // 5 7 suited
        matrix[4][6] = Action.RAISE;     // 6 8 suited
        matrix[5][7] = Action.RAISE;     // 7 9 suited
        matrix[6][8] = Action.RAISE;     // 8 T suited
        matrix[7][8] = Action.RAISE;     // 9 T suited
        matrix[7][9] = Action.RAISE;     // 9 J suited
        matrix[8][10] = Action.RAISE;    // T Q suited
        matrix[8][11] = Action.RAISE;    // T K suited
        matrix[9][11] = Action.RAISE;    // J K suited
        matrix[9][12] = Action.RAISE;    // J A suited
        matrix[8][7] = Action.RAISE;     // T 9 unsuited
        matrix[9][8] = Action.RAISE;     // J T unsuited
        matrix[10][8] = Action.RAISE;    // Q T unsuited
        matrix[11][8] = Action.RAISE;    // K T unsuited
        matrix[12][8] = Action.RAISE;    // A T unsuited
        matrix[10][9] = Action.RAISE;    // Q J unsuited
        matrix[11][9] = Action.RAISE;    // K J unsuited
        matrix[12][9] = Action.RAISE;    // A J unsuited
      
      /* Re-Raises */
        matrix[8][9] = Action.RERAISE;   // T J suited
        matrix[9][10] = Action.RERAISE;  // J Q suited
        matrix[9][11] = Action.RERAISE;  // J K suited
        matrix[9][11] = Action.RERAISE;  // A Q unsuited
        matrix[9][12] = Action.RERAISE;  // J A suited
        matrix[11][10] = Action.RERAISE; // K Q unsuited
        matrix[12][10] = Action.RERAISE; // A Q unsuited
      
      /* Premium */
        matrix[11][12] = Action.PREMIUM; // K A suited
        matrix[12][11] = Action.PREMIUM; // A K unsuited
        matrix[10][12] = Action.PREMIUM; // Q A suited
    }

    public Action getFlopAction(Card c1, Card c2) {
        if (c1.getRank() <= c2.getRank()) {
            if (c1.getSuit() == c2.getSuit()) {
                return matrix[c1.getRank()][c2.getRank()];
            } else {
                return matrix[c2.getRank()][c1.getRank()];
            }
        } else {
            if (c1.getSuit() == c2.getSuit()) {
                return matrix[c2.getRank()][c1.getRank()];
            } else {
                return matrix[c1.getRank()][c2.getRank()];
            }
        }
    }

    public void setFlopAction(Card c1, Card c2, Action action) {
        if (c1.getRank() <= c2.getRank()) {
            if (c1.getSuit() == c2.getSuit()) {
                matrix[c1.getRank()][c2.getRank()] = action;
            } else {
                matrix[c2.getRank()][c1.getRank()] = action;
            }
        } else {
            if (c1.getSuit() == c2.getSuit()) {
                matrix[c2.getRank()][c1.getRank()] = action;
            } else {
                matrix[c1.getRank()][c2.getRank()] = action;
            }
        }
    }
}
