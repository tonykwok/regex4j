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

import java.io.Serializable;

/**
 * <p>>The presence of a {@code RegexMatchTimeoutException} exception generally indicates one of the
 * following conditions:</p>
 *
 * <ul>
 *     <li>The regular expression engine is backtracking excessively as it attempts to match the
 *     input text to the regular expression pattern.</li>
 *
 *     <li>The time-out interval has been set too low, especially given high machine load.</li>
 * </ul>
 *
 * <p>The way in which an exception handler handles an exception depends on the cause of the
 * exception:</p>
 *
 * <ul>
 *     <li>If the time-out results from excessive backtracking, your exception handler should
 *     abandon the attempt to match the input and inform the user that a time-out has occurred in
 *     the regular expression pattern-matching method. If possible, information about the regular
 *     expression pattern, which is available from the Pattern property, and the input that caused
 *     excessive backtracking, which is available from the Input property, should be logged so that
 *     the issue can be investigated and the regular expression pattern modified. Time-outs due to
 *     excessive backtracking are always reproducible.</li>
 *
 *     <li>If the time-out results from setting the time-out threshold too low, you can increase the
 *     time-out interval and retry the matching operation. The current time-out interval is
 *     available from the MatchTimeout property. When a {@code RegexMatchTimeoutException} exception
 *     is thrown, the regular expression engine maintains its state so that any future invocations
 *     return the same result, as if the exception did not occur. The recommended pattern is to wait
 *     for a brief, random time interval after the exception is thrown before calling the matching
 *     method again. This can be repeated several times. However, the number of repetitions should
 *     be small in case the time-out is caused by excessive backtracking.</li>
 * </ul>
 *
 * @author  Tony Guo <tony.guo.peng@gmail.com>
 */
public class RegexMatchTimeoutException extends RuntimeException implements Serializable {

    private String regexInput = null;

    private String regexPattern = null;

    private int matchTimeout = -1;

    /**
     * <p>Initializes a new instance of the {@code RegexMatchTimeoutException} class with
     * information about the regular expression pattern, the input text, and the time-out
     * interval.</p>
     *
     * <p>The exception that is thrown when the execution time of a regular expression
     * pattern-matching method exceeds its time-out interval.</p>
     *
     * @param regexInput   Matching timeout occurred during matching within the specified input.
     * @param regexPattern Matching timeout occurred during matching to the specified pattern.
     * @param matchTimeout Matching timeout occurred because matching took longer than the
     *                     specified timeout.
     */
    public RegexMatchTimeoutException(String regexInput, String regexPattern, int matchTimeout) {
        super(R.RegexMatchTimeoutException_Occurred);
        init(regexInput, regexPattern, matchTimeout);
    }

    private void init(String input, String pattern, int timeout) {
        regexInput = input;
        regexPattern = pattern;
        matchTimeout = timeout;
    }

    public String pattern() {
        return regexPattern;
    }

    public String input() {
        return regexInput;
    }

    public int matchTimeout() {
        return matchTimeout;
    }
}