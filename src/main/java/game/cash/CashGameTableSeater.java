package game.cash;

import bots.BotRepository;
import game.AbstractGameDescription;
import game.PublicGameInfo;
import game.TableSeater;

/**
 * This class creates permutations of the seats to reduce variance in the simulations.
 */
public class CashGameTableSeater extends TableSeater {
    /**
     * if seats should be permuted. Currently only works for 2,3,4 and 6 seats
     * to be unbiased.
     */
    private boolean permuteSeats;

    /**
     * @param botRepository
     * @param permuteSeats  only works for even seat-counts
     */
    public CashGameTableSeater(BotRepository botRepository, boolean permuteSeats) {
        super(botRepository);
        this.permuteSeats = permuteSeats;
    }

    @Override
    public PublicGameInfo[] createTables(AbstractGameDescription gameDescription) {
        int[][] seatPermutations = createSeatPermutations(gameDescription);
        PublicGameInfo[] createdGameInfos = new PublicGameInfo[seatPermutations.length];

        for (int gamePermutation = 0; gamePermutation < seatPermutations.length; gamePermutation++) {
            PublicGameInfo publicGameInfo = new PublicGameInfo();
            createdGameInfos[gamePermutation] = publicGameInfo;

            publicGameInfo.setNumSeats(gameDescription.getNumSeats());

            for (int seat = 0; seat < gameDescription.getNumSeats(); seat++) {
                if (gameDescription.getBotNames()[seat] != null) {
                    int targetBot = seatPermutations[gamePermutation][seat];

                    seatPlayer(gameDescription, publicGameInfo, seat, targetBot);
                }
            }

        }

        return createdGameInfos;
    }

    private int[][] createSeatPermutations(AbstractGameDescription gameDescription) {
        int numSeats = gameDescription.getBotNames().length;

        if (!permuteSeats) {
            int[][] seatPermutations = new int[1][numSeats];
            for (int seat = 0; seat < numSeats; seat++) {
                seatPermutations[0][seat] = seat;
            }
            return seatPermutations;

        }

        // now for all the permutations
        if (numSeats == 2) {
            return new int[][]{{0, 1}, {1, 0}};
        }

        if (numSeats == 3) {
            return new int[][]{{0, 1, 2}, {1, 2, 0}, {2, 0, 1}, {2, 1, 0}, {0, 2, 1}, {1, 0, 2}};
        }

        //		if (numSeats == 4) {
        //			return new int[][] { { 0, 1, 2, 3 }, { 1, 3, 0, 2 }, { 2, 0, 3, 1 }, { 3, 2, 1, 0 }, { 1, 2, 3, 0 }, { 3, 0, 2, 1 }, { 0, 3, 1, 2 },
        //					{ 2, 1, 0, 3 }, { 2, 3, 0, 1 }, { 0, 2, 1, 3 }, { 3, 1, 2, 0 }, { 1, 0, 3, 2 }, { 3, 0, 1, 2 }, { 2, 1, 3, 0 }, { 1, 2, 0, 3 },
        //					{ 0, 3, 2, 1 } };
        //		}

        //		if (numSeats == 4) {
        //			return new int[][] { { 0, 1, 2, 3 }, { 1, 3, 0, 2 }, { 2, 0, 3, 1 }, { 3, 2, 1, 0 },
        //			{ 0, 3, 1, 2 }, { 3, 2, 0, 1 }, { 1, 0, 2, 3 }, { 2, 1, 3, 0 },
        //			{ 0, 2, 3, 1 }, { 2, 1, 0, 3 }, { 3, 0, 1, 2 }, { 1, 3, 2, 0 } };
        //		}

        if (numSeats == 4 || numSeats == 6) {
            // would work for 10 seats as well, but currently we support only up to
            // 9 seats

            // see http://pokerai.org/pf3/viewtopic.php?f=3&t=3272 for algorithm
            int permutationCount = permuteSeats ? numSeats : 1;

            int[][] seatPermutations = new int[permutationCount][numSeats];
            for (int permutation = 0; permutation < permutationCount; permutation++) {
                for (int seat = 0; seat < numSeats; seat++) {
                    seatPermutations[permutation][seat] = ((permutation + 1) * (seat + 1) % (numSeats + 1)) - 1;
                }
            }
            return seatPermutations;
        }

        throw new IllegalArgumentException("permutation currently only works with 2,3,4 or 6 seats");
    }

}
