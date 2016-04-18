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

/**
 * <p>The {@code CaptureCollection} object is immutable (read-only) and has no public constructor.
 * The {@code CaptureCollection} object contains one or more {@link Capture} objects.</p>
 *
 * <p>Instances of the {@code CaptureCollection} class are returned by the following methods:</p>
 *
 * <ul>
 *     <li>The {@link Group#captures()} method. Each member of the collection represents a
 *     substring captured by a capturing group. If a quantifier is not applied to a capturing
 *     group, the {@code CaptureCollection} includes a single {@link Capture} object that represents
 *     the same captured substring as the {@link Group} object. If a quantifier is applied to a
 *     capturing group, the {@code CaptureCollection} includes one {@link Capture} object for each
 *     captured substring, and the {@link Group} object provides information only about the last
 *     captured substring.</li>
 *
 *     <li>The {@link Match#captures()} method. In this case, the collection consists of a single
 *     {@link Capture} object that provides information about the match as a whole. That is, the
 *     {@code CaptureCollection} object provides the same information as the Match object.</li>
 * </ul>
 *
 * <p>To iterate through the members of the collection, you should use the collection iteration
 * construct ({@code for (Capture g : CaptureCollection)}) instead of retrieving the
 * {@link java.util.Iterator} that is returned by the {@link #iterator()} method.</p>
 */
public class CaptureCollection implements Iterable<Capture> {
    /*
     * The group that these captures belonging to.
     */
    private final Group group;

    /*
     * The number of captures for the group.
     */
    private final int captureCount;

    /*
     * The set of captures in the group.
     */
    private Capture[] captures;

    /*
     * Creates a {@code CaptureCollection} instance which represents the set of captures made
     * by a single capturing group.
     *
     * @see Group#captures()
     */
    CaptureCollection(Group group) {
        this.group = group;
        this.captureCount = group.captureCount;
    }

    /**
     * Gets number of captures for the group.
     *
     * @return The number of captures.
     */
    public int count() {
        return captureCount;
    }

    /**
     * Gets the <b>i<b/><sup>th<sup/> capture in the group.
     *
     * @return A specific capture, by index, in this collection.
     */
    public Capture get(int index) {
        return getCapture(index);
    }

    @Override
    public Iterator<Capture> iterator() {
        return new CaptureIterator(this);
    }

    /*
     * Gets the capture at given index in the group.
     */
    Capture getCapture(int index) {
        if (index == captureCount - 1 && index >= 0) {
            return group;
        }

        if (index >= captureCount || index < 0) {
            throw new IndexOutOfBoundsException(
                    "capture count = " + captureCount + "; index = " + index);
        }

        // first time a capture is accessed, compute them all.
        if (captures == null) {
            captures = new Capture[captureCount];
            for (int i = 0; i < captureCount - 1; i++) {
                captures[i] = new Capture(
                        group.text,
                        group.captures[i * 2],
                        group.captures[i * 2 + 1]
                );
            }
        }

        return captures[index];
    }

    private static final class CaptureIterator implements Iterator<Capture> {
        private final CaptureCollection collection;
        private int index;

        private CaptureIterator(CaptureCollection collection) {
            if (collection == null) {
                throw new IllegalArgumentException("collection cannot be null.");
            }

            this.collection = collection;
            this.index = -1;
        }

        /*
         * Advances to the next capture.
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
         * Gets the current capture.
         *
         * @return The current capture.
         * @throws ArrayIndexOutOfBoundsException if {@code index} is < 0 or >= count.
         */
        @Override
        public Capture next() {
            if (index < 0 || index >= collection.count()) {
                throw new IndexOutOfBoundsException(
                        "capture count = " + collection.count() + "; index = " + index);
            }

            return collection.get(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                    "remove() is not supported by the CaptureIterator.");
        }
    }
}