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

import jxtras.regex.support.R;

import java.util.Map;
import java.util.Set;

/**
 * <p>The Match object is immutable and has no public constructor.</p>
 *
 * <p>An instance of the {@code Match} class is returned by the {@link Regex#match()} method and
 * represents the first pattern match in a string. Subsequent matches are represented by
 * {@code Match} objects returned by the {@link #nextMatch()} method.</p>
 *
 * <p>In addition, a {@link MatchCollection} object that consists of zero, one, or more Match
 * objects is returned by the {@link Regex#matches()} method.</p>
 *
 * <p>If the {@link Regex#matches()} method fails to match a regular expression pattern in an input
 * string, it returns an empty {@link MatchCollection} object. You can then use a foreach construct
 * ({@code for (Match m : MatchCollection)}) to iterate the collection.</p>
 *
 * <p>If the {@link Regex#match()} method fails to match the regular expression pattern, it
 * returns a {@code Match} object that is equal to {@link #EMPTY}. You can use the
 * {@link #success()} method to determine whether the match was successful.</p>
 *
 * <p>The following example provides an illustration.</P>
 *
 * <pre><code>
 *     // Search for a pattern that is not found in the input string.
 *     String pattern = "dog";
 *     String input = "The cat saw the other cats playing in the back yard.";
 *     Match match = Regex.match(input, pattern);
 *     if (match.success())
 *         // Report position as a one-based integer.
 *         System.out.println(String.format("'%s' was found at position %d in '%s'.",
 *                 match.value(), match.index() + 1, input));
 *     else
 *         System.out.println(String.format("The pattern '%s' was not found in '%s'.",
 *                 pattern, input));
 * </code></pre>
 *
 * <p>If a pattern match is successful, the {@link #value()} method contains the matched substring,
 * the {@link #index()} method indicates the zero-based starting position of the matched substring
 * in the input string, and the {@link #length()} method indicates the length of matched substring
 * in the input string.</p>
 *
 * <p>Because a single match can involve multiple capturing groups, {@code Match} has a
 * {@link #groups()} method that returns the {@link GroupCollection}. The {@code Match} instance
 * itself is equivalent to the first object in the collection, at {@link Match#groups()#getMatch
 * (0)}, which represents the entire match. You can access the captured groups in a match in the
 * following ways:<p>
 *
 * <ul><li>You can iterate the members of the {@link GroupCollection} object by using a foreach
 * construct({@code for (Group g : GroupCollection)}).</li>
 * <p/>
 * <li>You can use the {@link GroupCollection#get(int)} method to retrieve groups by the number of
 * the capturing group. Note that you can determine which numbered groups are present in a regular
 * expression by calling the instance {@link Regex#getGroupNumbers()} method.</li>
 * <p/>
 * <li>You can use the {@link GroupCollection#get(String)} method to retrieve groups by the name of
 * the capturing group. Note that you can determine which named groups are present in a regular
 * expression by calling the instance {@link Regex#getGroupNames()} method.</li></ul>
 *
 * @author Tony Guo <tony.guo.peng@gmail.com>
 * @since 1.0
 */
public class Match extends Group {
    // The empty match object.
    static Match EMPTY = new Match(null, 1, "", 0, 0, 0);
    GroupCollection rgc;

    // input to the match
    Regex regex;
    int textBegin;
    int textPosition;
    int textEnd;
    int textStart;

    // output from the match
    int[][] matches;
    int[] matchCount;
    boolean balancing;        // whether we've done any balancing with this match.  If we
                              // have done balancing, we'll need to do extra work in Tidy().

    /*
     * <p>Create an instance of {@code Match} class which represents the results from a single
     * regular expression match.</p>
     *
     * <p>{@code Match} is the result class for a regex search. It returns the location, length, and
     * substring for the entire match as well as every captured group.</p>
     *
     * <p>{@code Match} is also used during the search to keep track of each capture for each group.
     * This is done using the "matches" array.</p>
     *
     * <p>{@code matches[x]} represents an array of the captures for group {@code x}. This array
     * consists of start and length pairs, and may have empty entries at the end.</p>
     *
     * <p>{@code matchCount[x]} stores how many captures a group has. Note that
     * {@code matchCount[x]*2} is the length of all the valid values in matches.
     * {@code matchCount[x]*2-2} is the Start of the last capture, and
     * {@code matchCount[x]*2-1} is the length of the last capture.</p>
     *
     * <p>For example, if group 2 has one capture starting at position 4 with length 6,</p>
     * <pre><code>
     *     matchCount[2] == 1
     *     matches[2][0] == 4
     *     matches[2][1] == 6
     * </code></pre>
     *
     * <p>Values in the {@code matches} array can also be negative. This happens when using the
     * balanced match construct, "{@code (?<start-end>...)}". When the "end" group matches, a
     * capture is added for both the "start" and "end" groups. The capture added for "start"
     * receives the negative values, and these values point to the next capture to be balanced.
     * They do NOT point to the capture that "end" just balanced out. The negative values are
     * indices into the {@code matches} array transformed by the formula {@code -3-x}.
     * This formula also untransforms.</p>
     */
    Match(Regex regex, int captureCount, String text, int beginPosition, int length,
          int startPosition) {
        super(text, new int[2], 0);
        this.regex = regex;
        this.matchCount = new int[captureCount];

        this.matches = new int[captureCount][];
        this.matches[0] = captures;
        this.textBegin = beginPosition;
        this.textEnd = beginPosition + length;
        this.textStart = startPosition;
        this.balancing = false;

        // TODO: Do we need to throw an exception here?
        // This is only called internally, so we'll use an Assert instead.
        if (textBegin < 0 || textStart < textBegin || textEnd < textStart || text.length() < textEnd) {
            throw new IllegalArgumentException("The parameters are out of range.");
        }
    }

    void reset(Regex regex, String text, int textBegin, int textEnd, int textStart) {
        this.regex = regex;
        this.text = text;
        this.textBegin = textBegin;
        this.textEnd = textEnd;
        this.textStart = textStart;

        for (int i = 0; i < this.matchCount.length; i++) {
            this.matchCount[i] = 0;
        }

        this.balancing = false;
    }

    /**
     * Gets a collection of groups matched by the regular expression.
     */
    public GroupCollection groups() {
        if (rgc == null) {
            rgc = new GroupCollection(this, null);
        }

        return rgc;
    }

    /**
     * Gets the next match.
     *
     * @return A new Match with the results for the next match, starting
     * at the position at which the last match ended (at the character beyond the last
     * matched character).
     */
    public Match nextMatch() {
        if (regex == null) {
            return this;
        }

        return regex.run(false, length, text, textBegin, textEnd - textBegin, textPosition);
    }

    /**
     * Gets the result string (using the replacement pattern).
     *
     * <p>For example, if the replacement pattern is {@code ?$1$2?}, {@code result()} returns
     * the concatenation of {@code Group(1).toString()} and {@code Group(2).toString()}.</p>
     *
     * @return The expansion of the passed replacement pattern.
     */
    public String result(String replacement) {
        if (replacement == null) {
            throw new IllegalArgumentException("replacement cannot be null.");
        }

        if (regex == null) {
            throw new IllegalStateException(R.NoResultOnFailed);
        }

        RegexReplacement repl = regex.replref.get();
        if ((repl == null) || !repl.pattern().equals(replacement)) {
            repl = RegexParser.parseReplacement(
                    replacement, regex.caps, regex.capsize, regex.capnames, regex.options);
            regex.replref.cache(repl);
        }

        return repl.replacement(this);
    }

    /*
     * Used by the replacement code.
     */
    String groupToStringImpl(int groupNumber) {
        int c = matchCount[groupNumber];
        if (c == 0) {
            return "";
        }

        int[] m = matches[groupNumber];
        // TODO: text.substring(m[(c - 1) * 2], m[(c * 2) - 1]);
        return text.substring(m[(c - 1) * 2], m[(c - 1) * 2] + m[(c * 2) - 1]);
    }

    /*
     * Used by the replacement code.
     */
    String lastGroupToStringImpl() {
        return groupToStringImpl(matchCount.length - 1);
    }

    /*
     * Adds a capture to the group specified by "cap".
     */
    void addMatch(int cap, int start, int length) {
        if (matches[cap] == null) {
            matches[cap] = new int[2];
        }

        int captureCount = matchCount[cap];

        if (captureCount * 2 + 2 > matches[cap].length) {
            int[] oldMatches = matches[cap];
            int[] newMatches = new int[captureCount * 8];
            for (int i = 0; i < captureCount * 2; i++) {
                newMatches[i] = oldMatches[i];
            }
            matches[cap] = newMatches;
        }

        matches[cap][captureCount * 2] = start;
        matches[cap][captureCount * 2 + 1] = length;
        matchCount[cap] = captureCount + 1;
    }

    /*
     * Adds a capture to balance the specified group.
     *
     * <p>This is used by the balanced match construct. (?<foo-foo2>...)
     * If there were no such thing as backtracking, this would be as simple as calling
     * {@link #removeMatch(int)}. However, since we have backtracking, we need to keep track of
     * everything.</p>
     */
    void balanceMatch(int cap) {
        balancing = true;

        // we'll look at the last capture first
        int captureCount = matchCount[cap];
        int target = captureCount * 2 - 2;

        // first see if it is negative, and therefore is a reference to the next available
        // capture group for balancing.  If it is, we'll reset target to point to that capture.
        if (matches[cap][target] < 0) {
            target = -3 - matches[cap][target];
        }

        // move back to the previous capture
        target -= 2;

        // if the previous capture is a reference, just copy that reference to the end.
        // Otherwise, point to it.
        if (target >= 0 && (matches[cap][target] < 0)) {
            addMatch(cap, matches[cap][target], matches[cap][target + 1]);
        } else {
            addMatch(cap, -3 - target, -4 - target /* == -3 - (target + 1) */);
        }
    }

    /*
     * Removes a group match by capnum.
     */
    void removeMatch(int cap) {
        matchCount[cap]--;
    }

    /*
     * Tells if a group was matched by capnum.
     */
    boolean isMatched(int cap) {
        return cap < matchCount.length && matchCount[cap] > 0 && matches[cap][matchCount[cap] * 2 - 1] != (-3 + 1);
    }

    /*
     * Gets the index of the last specified matched group by capnum.
     */
    int matchIndex(int cap) {
        int i = matches[cap][matchCount[cap] * 2 - 2];
        if (i >= 0) {
            return i;
        }

        return matches[cap][-3 - i];
    }

    /*
     * Gets the length of the last specified matched group by capnum.
     */
    int matchLength(int cap) {
        int i = matches[cap][matchCount[cap] * 2 - 1];
        if (i >= 0) {
            return i;
        }

        return matches[cap][-3 - i];
    }

    /*
     * Tidy the match so that it can be used as an immutable result.
     */
    void tidy(int position) {
        int[] interval = matches[0];
        index = interval[0];
        length = interval[1];
        textPosition = position;
        captureCount = matchCount[0];

        if (balancing) {
            // The idea here is that we want to compact all of our unbalanced captures.  To do that we
            // use j basically as a count of how many unbalanced captures we have at any given time
            // (really j is an index, but j/2 is the count).  First we skip past all of the real captures
            // until we find a balance captures.  Then we check each subsequent entry.  If it's a balance
            // capture (it's negative), we decrement j.  If it's a real capture, we increment j and copy
            // it down to the last free position.
            for (int cap = 0; cap < matchCount.length; cap++) {
                int limit = matchCount[cap] * 2;
                int[] matchArray = matches[cap];

                int i, j;
                for (i = 0; i < limit; i++) {
                    if (matchArray[i] < 0) {
                        break;
                    }
                }

                for (j = i; i < limit; i++) {
                    if (matchArray[i] < 0) {
                        // skip negative values
                        j--;
                    } else {
                        // but if we find something positive (an actual capture), copy it back to the last
                        // unbalanced position.
                        if (i != j) {
                            matchArray[j] = matchArray[i];
                        }
                        j++;
                    }
                }

                matchCount[cap] = j / 2;
            }

            balancing = false;
        }
    }

    boolean isDebugEnabled() {
        return regex != null && regex.isDebugEnabled();
    }

    void dump() {
        for (int i = 0; i < matchCount.length; i++) {
            System.out.println("Capture(" + i + "):");
            for (int j = 0; j < matchCount[i]; j++) {
                String str = "";
                if (matches[i][j * 2] >= 0) {
                    // TODO: text.substring(matches[i][j * 2], matches[i][j * 2 + 1]);
                    str = text.substring(
                            matches[i][j * 2], // start index (inclusive)
                            matches[i][j * 2] + matches[i][j * 2 + 1] // end index (exclusive)
                    );
                }
                System.out.println(" (" + matches[i][j * 2] + "," + matches[i][j * 2 + 1] + ") " + str);
            }
        }
    }

    /*
     * MatchSparse is for handling the case where slots are
     * sparsely arranged (e.g., if somebody says use slot 100000)
     */
    static final class MatchSparse extends Match {
        Map<Integer, Integer> captureMap;

        MatchSparse(Regex regex, Map<Integer, Integer> captureMap, int captureCount, String text, int beginPosition, int length, int startPosition) {
            super(regex, captureCount, text, beginPosition, length, startPosition);
            this.captureMap = captureMap;
        }

        @Override
        public GroupCollection groups() {
            if (rgc == null) {
                rgc = new GroupCollection(this, captureMap);
            }

            return rgc;
        }

        @Override
        void dump() {
            if (captureMap != null) {
                Set<Map.Entry<Integer, Integer>> entrySet = captureMap.entrySet();
                for (Map.Entry<Integer, Integer> entry : entrySet) {
                    System.out.println("Slot " + entry.getKey() + " -> " + entry.getValue());
                }
            }

            super.dump();
        }
    }
}