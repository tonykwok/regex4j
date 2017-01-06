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

import java.util.Iterator;
import java.util.Map;

/**
 * <p>The {@code GroupCollection} class is a zero-based collection class that consists of one or
 * more {@link Group} objects that provide information about captured groups in a regular expression
 * match.</p>
 *
 * <p>The collection is immutable (read-only) and has no public constructor.</p>
 *
 * <p>A {@code GroupCollection} object is returned by the {@link Match#groups()} method.</p>
 *
 * <p>The collection contains one or more {@link Group} objects. If the match is successful, the
 * first element in the collection contains the {@link Group} object that corresponds to the entire
 * match. Each subsequent element represents a captured group, if the regular expression includes
 * capturing groups. Matches from numbered (unnamed) capturing groups appear in numeric order before
 * matches from named capturing groups. If the match is unsuccessful, the collection contains a
 * single {@link Group} object whose {@link Group#success()} method returns {@code false} and whose
 * {@link Group#value()} method equals an empty string ({@code ""}).</p>
 *
 * <p>To iterate through the members of the collection, you should use the collection iteration
 * construct ({@code for (Group g : GroupCollection)}) instead of retrieving the
 * {@link java.util.Iterator} that is returned by the {@link #iterator()} method.</p>
 *
 * <p>Note that you can retrieve an array that contains the numbers and names of all capturing
 * groups by calling the {@link Regex#getGroupNumbers()} and {@link Regex#getGroupNames()} methods,
 * respectively. Both are instance methods and require that you instantiate a {@link Regex} object
 * that represents the regular expression to be matched.</p>
 *
 * @author Tony Guo <tony.guo.peng@gmail.com>
 * @since 1.0
 */
public class GroupCollection implements Iterable<Group> {
    /*
     * The match that these groups belonging to.
     */
    private final Match match;

    /*
     * Cache of group objects fed to the user.
     */
    private final Map<Integer, Integer> captureMap;

    /*
     * The set of groups in the match.
     */
    private Group[] groups;

    /*
     * <p>The {@code GroupCollection} object represents a sequence of capture substrings.</p>
     *
     * <p>It is used to return the set of captures done by a single capturing group.</p>
     */
    GroupCollection(Match match, Map<Integer, Integer> captureMap) {
        this.match = match;
        this.captureMap = captureMap;
    }

    /**
     * Gets the count of groups.
     */
    public int count() {
        return match.matchCount.length;
    }

    /**
     * Gets the <b>i<b/><sup>th<sup/> group in the collection.
     */
    public Group get(int groupNumber) {
        return getGroup(groupNumber);
    }

    /**
     * Gets the group in the collection according to the specified group name.
     */
    public Group get(String groupName) {
        if (match.regex == null) {
            return Group.EMPTY;
        }

        return getGroup(this.match.regex.groupNumberFromName(groupName));
    }

    @Override
    public Iterator<Group> iterator() {
        return new GroupIterator(this);
    }

    Group getGroup(int groupNumber) {
        if (this.captureMap != null) {
            Integer groupNumberImpl = captureMap.get(groupNumber);
            if (groupNumberImpl != null) {
                return getGroupImpl(groupNumberImpl);
            }
        } else {
            if (groupNumber < match.matchCount.length && groupNumber >= 0) {
                return getGroupImpl(groupNumber);
            }
        }

        return Group.EMPTY;
    }

    /*
     * Caches the group objects.
     */
    Group getGroupImpl(int groupNumber) {
        if (groupNumber == 0) {
            return match;
        }

        // Constructs all the Group objects the first time getGroup(...) is called.
        if (groups == null) {
            groups = new Group[match.matchCount.length - 1];
            for (int i = 0; i < groups.length; i++) {
                groups[i] = new Group(match.text, match.matches[i + 1], match.matchCount[i + 1]);
            }
        }

        return groups[groupNumber - 1];
    }

    private static final class GroupIterator implements Iterator<Group> {
        private final GroupCollection collection;
        private int index = -1;

        private GroupIterator(GroupCollection collection) {
            if (collection == null) {
                throw new IllegalArgumentException("collection cannot be null.");
            }

            this.collection = collection;
            this.index = -1;
        }

        /*
         * Advances to the next group.
         */
        @Override
        public boolean hasNext() {
            final int size = collection.count();

            if (index >= size) {
                return false;
            }

            index++;

            return index < size;
        }

        /*
         * Gets the current group.
         *
         * @return The current capture.
         * @throws ArrayIndexOutOfBoundsException if {@code index} is < 0 or >= count.
         */
        @Override
        public Group next() {
            if (index < 0 || index >= collection.count()) {
                throw new IndexOutOfBoundsException(
                        "group count = " + collection.count() + "; index = " + index);
            }

            return collection.get(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                    "remove() is not supported by the GroupIterator.");
        }
    }
}