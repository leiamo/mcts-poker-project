package com.biotools.util;

import java.security.SecureRandom;

/**
 * Simple implementation for the missing com.biotools.util.Randomizer, so bots that use com.biotools.meerkat.HandEvaluator
 * can run with the Test Bed.
 * <p/>
 * You are better off writing your own HandEvaluator though, as this is a week random number generator....
 *
 * @see com.biotools.meerkat.HandEvaluator
 */
public class Randomizer extends SecureRandom {

    public static Randomizer getRandomizer() {
        return new Randomizer();
    }

}
