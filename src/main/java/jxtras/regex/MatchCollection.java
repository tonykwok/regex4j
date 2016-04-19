/*
 * Copyright (C) 2015 The JXTRAS Project Authors. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jxtras.regex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>The collection is immutable (read-only) and has no public constructor. The
 * {@link Regex#matches} method returns a {@code MatchCollection} object.</p>
 *
 * <p>The collection contains zero or more {@link Match} objects. If the match is successful, the
 * collection is populated with one {@link Match} object for each match found in the input string.
 * If the match is unsuccessful, the collection contains no {@link Match} objects, and its
 * {@link #count()} method returns zero.</p>
 *
 * <p>When applying a regular expression pattern to a particular input string, the regular
 * expression engine uses either of two techniques to build the MatchCollection object:</p>
 *
 * <ul>
 *     <li>Direct evaluation.</li>
 *     <p>The {@link MatchCollection} object is populated all at once, with all matches resulting
 *     from a particular call to the {@link Regex#matches} method. This technique is used when the
 *     collection's {@link #count()} method is accessed. It typically is the more expensive
 *     method of populating the collection and entails a greater performance hit.</p>
 *
 *     <li>Lazy evaluation</li>
 *     <p>The {@code MatchCollection} object is populated as needed on a match-by-match basis. It
 *     is equivalent to the regular expression engine calling the {@link Regex#match} method
 *     repeatedly and adding each match to the collection. This technique is used when the
 *     collection is accessed through its {@link #iterator()} method, or when it is accessed using
 *     the foreach statement ({@code for (Match m : MatchCollection)}).</p>
 * </ul>
 *
 * <p>To iterate through the members of the collection, you should use the collection iteration
 * construct ({@code for (Match m : MatchCollection)}) instead of retrieving the
 * {@link java.util.Iterator} that is returned by the {@link #iterator()} method.</p>
 *
 * @author  Tony Guo <tony.guo.peng@gmail.com>
 */
public class MatchCollection implements Iterable<Match> {
    private final Regex regex;
    private final List<Match> matches;
    private boolean done;
    private String input;
    private int beginning;
    private int length;
    private int startAt;
    private int previousLength;

    /*
     * Creates an instance of {@code MatchCollection} class which represents the set of  successful
     * matches found by iteratively applying a regular expression pattern to the input string.
     *
     * <p>The MatchCollection lists the successful matches that result when searching a string for a
     * regular expression.</p>
     *
     * <p>This collection returns a sequence of successful match results from
     * {@link Regex#matches()}. It stops when the first failure is encountered (it does not return
     * the failed match).</p>
     */
    MatchCollection(Regex regex, String input, int beginning, int length, int startAt) {
        if (startAt < 0 || startAt > input.length()) {
            throw new IndexOutOfBoundsException(
                    "string length = " + input.length() + "; index = " + startAt);
        }

        this.regex = regex;
        this.input = input;
        this.beginning = beginning;
        this.length = length;
        this.startAt = startAt;
        this.previousLength = -1;
        this.matches = new ArrayList<Match>();
        this.done = false;
    }

    /**
     * Gets the number of matches.
     */
    public int count() {
        ensureInitialized();
        return matches.size();
    }

    /**
     * Gets the <b>i</b><sup>th</sup> match in the collection.
     */
    public Match get(int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException("i");
        }

        Match match = getMatch(i);
        if (match == null) {
            throw new IndexOutOfBoundsException("i");
        }

        return match;
    }

    @Override
    public Iterator<Match> iterator() {
        return new MatchIterator(this);
    }

    private Match getMatch(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("i cannot be negative.");
        }

        if (matches.size() > i) {
            return matches.get(i);
        }

        if (done) {
            return null;
        }

        Match match;

        do {
            match = regex.run(false, previousLength, input, beginning, length, startAt);

            if (!match.success()) {
                done = true;
                return null;
            }

            matches.add(match);

            previousLength = match.length;
            startAt = match.textPosition;
        } while (matches.size() <= i);

        return match;
    }

    private void ensureInitialized() {
        if (!done) {
            getMatch(Integer.MAX_VALUE);
        }
    }

    private static final class MatchIterator implements Iterator<Match> {
        private final MatchCollection collection;
        private int index;

        MatchIterator(MatchCollection collection) {
            if (collection == null) {
                throw new IllegalArgumentException("collection cannot be null.");
            }

            this.collection = collection;
            this.index = -1;
        }

        /*
         * Advances to the next match.
         */
        @Override
        public boolean hasNext() {
            if (index == -2) {
                return false;
            }

            index++;
            Match match = collection.getMatch(index);
            if (match == null) {
                index = -2;
                return false;
            }

            return true;
        }

        /*
         * Gets the current match.
         */
        @Override
        public Match next() {
            if (index < 0) {
                throw new IndexOutOfBoundsException(
                        "match count = " + collection.count() + "; index = " + index);
            }

            return collection.getMatch(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                    "remove() is not supported by the MatchIterator.");
        }
    }
}