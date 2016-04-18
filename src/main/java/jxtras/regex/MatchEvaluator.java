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
 * <p>You can use a {@code MatchEvaluator} delegate method to perform a custom verification or
 * manipulation operation for each match found by a replacement method such as
 * {@link Regex#replace(String, MatchEvaluator)}.</p>
 *
 * <p>For each matched string, the {@link Regex#replace(String, MatchEvaluator)} method calls the
 * {@code MatchEvaluator} delegate method with a {@link Match} object that represents the match.</p>
 *
 * <p>The delegate method performs whatever processing you prefer and returns a string that the
 * {@link Regex#replace(String, MatchEvaluator)} method substitutes for the matched string.</p>
 */
public interface MatchEvaluator {
    /**
     * <p>This callback method is called each time a regular expression match is found during a
     * {@link RegexReplacement#replace} method operation.</p>
     *
     * @param match The {@link Match} object that represents a single regular expression match
     *              during a {@link RegexReplacement#replace} method operation.
     * @return A string.
     */
    public abstract String evaluate(Match match);
}