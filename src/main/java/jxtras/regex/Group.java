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

/**
 * <p>A capturing group can capture zero, one, or more strings in a single match because of
 * quantifiers. All the substrings matched by a single capturing group are available from the
 * {@link Group#captures()} method. Information about the last substring captured can be accessed
 * directly from the {@link #value()} and {@link #index()} method. (That is, the {@code Group}
 * instance is equivalent to the last item of the collection returned by the {@link #captures()}
 * method, which reflects the last capture made by the capturing group.)</p>
 *
 * @author  Tony Guo <tony.guo.peng@gmail.com>
 */
public class Group extends Capture {
    // The empty group object.
    static final Group EMPTY = new Group("", new int[0], 0);

    int[] captures;
    int captureCount;

    /*
     * The collection of all captures for this group.
     */
    CaptureCollection rcc;

    /*
     * Creates a {@code Group} instance which represents the results from a single capturing group.
     */
    Group(String text, int[] captures, int captureCount) {
        super(
                text,
                captureCount == 0 ? 0 : captures[(captureCount - 1) * 2],
                captureCount == 0 ? 0 : captures[(captureCount * 2) - 1]
        );
        this.captures = captures;
        this.captureCount = captureCount;
    }

    /**
     * Indicates whether the match is successful.
     *
     * @return {@code true} if the match was successful.
     */
    public boolean success() {
        return captureCount != 0;
    }

    /**
     * <p>Gets a collection of all the captures matched by the capturing group, in
     * innermost-leftmost-first order (or innermost-rightmost-first order if the regular expression
     * is modified with the {@link RegexOptions#RightToLeft} option).</p>
     *
     * <p>The collection may have zero or more items.</p>
     */
    public CaptureCollection captures() {
        if (rcc == null) {
            rcc = new CaptureCollection(this);
        }

        return rcc;
    }
}