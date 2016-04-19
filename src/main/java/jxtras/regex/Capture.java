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
 * <p>A {@code Capture} object is immutable and has no public constructor.</p>
 *
 * <p>Instances of {@code Capture} class are returned through the {@link CaptureCollection}
 * object, which is returned by the {@link Match#captures()} and {@link Group#captures()}
 * methods.</p>
 *
 * <p>However, the {@link Match#captures()} method provides information about the same match as the
 * {@link Match} object.</p>
 *
 * <ul>
 *     <li>If you do not apply a to a capturing group, the {@link Group#captures()} method returns
 *     a {@link CaptureCollection} with a single {@code Capture} object that provides information
 *     about the same capture as the {@link Group} object.</li>
 *
 *     <li>If you do apply a quantifier to a capturing group, the{@link Group#index()},
 *     {@link Group#length()}, and {@link Group#value()} methods provide information only about the
 *     last captured group, whereas the {@code Capture} objects in the {@link CaptureCollection}
 *     provide information about all subexpression captures.</li>
 * </ul>
 *
 * @author  Tony Guo <tony.guo.peng@gmail.com>
 */
public class Capture {
    /*
     * The position in the original string where the first character of the
     * captured substring is found.
     */
    int index;

    /*
     * The length of the captured substring.
     */
    int length;

    /*
     * The original string.
     */
    String text;

    /*
     * Creates a {@code Capture} instance with specified location/length pair
     * that indicates the location of a regular expression match.
     */
    Capture(String text, int index, int length) {
        this.text = text;
        this.index = index;
        this.length = length;
    }

    /**
     * Gets the position in the original string where the first character of
     * captured substring was found.
     */
    public int index() {
        return index;
    }

    /**
     * Gets the length of the captured substring.
     */
    public int length() {
        return length;
    }

    /**
     * Gets the substring that was matched.
     */
    public String value() {
        // TODO: text.substring(index, length);
        return text.substring(index, index + length);
    }

    @Override
    public String toString() {
        return value();
    }

    /*
     * Gets the original string.
     */
    String getOriginalString() {
        return text;
    }

    /*
     * Gets the substring to the left of the capture.
     */
    String getLeftSubstring() {
        // TODO: text.substring(0, index);
        return text.substring(0, index);
    }

    /*
     * Gets the substring to the right of the capture.
     */
    String getRightSubstring() {
        // TODO:  text.substring(index + length, text.length() - index - length);
        return text.substring(index + length, text.length());
    }

    String description() {
        return String.format("(I = %d, L = %d): %s", index, length, text);
    }
}