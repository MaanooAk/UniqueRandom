// UniqueRandom Copyright (c) 2017 UniqueRandom author list (see README.md)

package com.maanoo.urandom;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;


/**
 * An instance of this class is used to generate a stream of
 * pseudo-random non-repeating integers from 0 to a given limit.
 * <p>
 * The space complexity is O(1), The generated integers are not stored
 * in a array of other data structure. The next integer and only that
 * is generated every time the {@code next()} method is called.
 * <p>
 * A single {@link java.util.Random} object is used only at the constructor
 * in order to generate two random numbers. The given seed is passed directly
 * to it.
 * <p>
 * The only Thread-safe operation is the {@code nextIfHas()} method, a
 * synchronized combination of the {@code hasNext()} and {@code next()}
 * methods.
 *
 * @author MaanooAk
 * @version 0.4
 */
public class UniqueRandom implements Iterable<Integer> {

    private final int count;
    private final long seed;

    private final int prime;

    private final int offset_index;
    private final int offset_value;

    private int index;
    private int realIndex;

    private static final String BadCount = "count must be non-negative";

    /**
     * Creates a new unique random number generator.
     * <p>
     * The seed of the random number generator is set to a random number.
     *
     * @param count the number of elements
     * @throws IllegalArgumentException if if {@code count} is less than zero
     */
    public UniqueRandom(int count) {
        this(count, new Random().nextLong());
    }

    /**
     * Creates a new unique random number generator.
     *
     * @param count the number of elements
     * @param seed the seed of the random number generator
     * @throws IllegalArgumentException if if {@code count} is less than zero
     */
    public UniqueRandom(int count, long seed) {
        if (count < 0) throw new IllegalArgumentException(BadCount);

        this.count = count;
        this.seed = seed;

        prime = findPrime(count + 3);

        Random ra = new Random(seed);
        offset_index = count > 0 ? ra.nextInt(prime) : 0;
        offset_value = count > 0 ? ra.nextInt(count) : 0;

        index = 0;
        realIndex = 0;
    }

    /**
     * Checks if there are elements left.
     *
     * @return if there are elements left
     */
    public boolean hasNext() {
        // assert 0 <= realIndex && realIndex <= count;
        // assert 0 <= index && index <= prime;

        return realIndex < count;
    }

    /**
     * Calculates and returns the next element.
     *
     * @return the next element
     * @throws NoSuchElementException if there are no elements left
     */
    public int next() {
        if (!hasNext()) throw new NoSuchElementException();

        // apply index offset in order to create the local index
        // store in long in order to allow the power
        long lindex = (index + offset_index) % prime;

        int val;
        do {

            // after module the power can be contained in a int
            int powermod = (int)((lindex * lindex) % prime);

            if (lindex <= prime / 2) {
                val = powermod;
            } else {
                val = prime - powermod;
            }

            lindex = (lindex + 1) % prime;
            index += 1;

            // calculate next if its on the extras or on the skips
        } while(val >= count + 2 || val == 0 || val == 1);

        // apply value offset
        val = (val + 2 + offset_value) % count;

        realIndex += 1;
        return val;
    }

    /**
     * Thread-safe operation which returns the next element.
     * <p>
     * In case that the there isn't any next element, returns -1.
     *
     * @return the next element if exists, or -1
     */
    public synchronized int nextIfHas() {
        if (hasNext()) {
            return next();
        } else {
            return -1;
        }
    }

    /**
     * Returns an iterator over remaining elements.
     * <p>
     * The iter
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Integer> iterator() {
        return new UniqueRandomItr(realIndex);
    }

    private class UniqueRandomItr implements Iterator<Integer> {

        private int expectedRealIndex;

        public UniqueRandomItr(int realIndex) {
            expectedRealIndex = realIndex;
        }

        @Override
        public boolean hasNext() {
            return UniqueRandom.this.hasNext();
        }

        @Override
        public Integer next() {
            if (expectedRealIndex != UniqueRandom.this.realIndex)
                throw new ConcurrentModificationException();

            expectedRealIndex += 1;
            return UniqueRandom.this.next();
        }
    }

    // Getters

    public int getCount() {
        return count;
    }

    public long getSeed() {
        return seed;
    }

    public int getIndex() {
        return realIndex;
    }

    public int getNextLeft() {
        return count - realIndex;
    }

    // Internal

    private static int findPrime(int min) {
        // assert x >= 0;

        int prime = min;

        if (isEven(prime)) {
            prime += 1;
        }

        while (!(isSpecial(prime) && isPrime(prime))) {
            prime += 2; // skip even
        }

        return prime;
    }

    private static boolean isEven(int x) {
        // assert x >= 0;

        return x % 2 == 0;
    }

    private static boolean isPrime(int x) {
        // assert x >= 0;
        // assert x % 2 == 1;

        for (int i = 3; i * i <= x; i++) {
            if (x % i == 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSpecial(int x) {
        // assert x >= 0;

        return x % 4 == 3;
    }


}
