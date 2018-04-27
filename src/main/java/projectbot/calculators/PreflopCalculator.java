package projectbot.calculators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

// Accesses a file of pre-calculated preflop data to retrieve winning probabilities for preflop hands

public class PreflopCalculator {
    // There is no need to differentiate between different suits as these are not ranked
    HashMap<String, Double> suitedProbabilities;
    HashMap<String, Double> offsuitedProbabilities;

    // Read preflop data at initialisation
    public PreflopCalculator(String homeDir) {
        String fileLocation = homeDir + "data\\PreflopWinProbs.txt";
        suitedProbabilities = new HashMap<>();
        offsuitedProbabilities = new HashMap<>();
        int count = 0;
        String line, cards, winProb;
        String row[];
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileLocation));
            while ((line = br.readLine()) != null) {
                if (count != 0) {
                    row = line.split("\t");
                    cards = row[1].trim();
                    winProb = row[3].trim();
                    if (cards.contains("s")) {
                        suitedProbabilities.put(cards.substring(0,2), Double.parseDouble(winProb)/100);
                    }
                    else if (cards.contains("o")) {
                        offsuitedProbabilities.put(cards.substring(0,2), Double.parseDouble(winProb)/100);
                    }
                }
                count++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Return winning probability from preflop data
    public Double getWinningProbabilityPreflop(String[] holeCards){
        ArrayList<Character> rankings = new ArrayList<>(Arrays.asList('2','3','4','5','6','7','8','9','T','J','Q','K','A'));
        char card1 = holeCards[0].charAt(0);
        char card2 = holeCards[1].charAt(0);

        String cards;
        if (rankings.indexOf(card1) > rankings.indexOf(card2)) {
            cards = Character.toString(card1) + Character.toString(card2);
        }
        else {
            cards = Character.toString(card2) + Character.toString(card1);
        }

        boolean suited = (holeCards[0].charAt(1)==(holeCards[1].charAt(1)));
        if (suited) return suitedProbabilities.get(cards);
        else return offsuitedProbabilities.get(cards);
    }
}