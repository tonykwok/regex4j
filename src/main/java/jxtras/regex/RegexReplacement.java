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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// The RegexReplacement class represents a substitution string for
// use when using regexs to search/replace, etc. It's logically
// a sequence intermixed (1) static finalant strings and (2) group numbers.
final class RegexReplacement {
    /*
     * Since RegexReplacement shares the same parser as Regex,
     * the static finalructor takes a RegexNode which is a concatenation
     * of static finalant strings and backreferences.
     */
    RegexReplacement(String rep, RegexNode concat, Map<Integer, Integer> _caps) {
        StringBuilder sb;
        List<String> strings;
        List<Integer> rules;
        int slot;

        _rep = rep;

        if (concat.type() != RegexNode.Concatenate) {
            throw new IllegalArgumentException(R.ReplacementError);
        }

        sb = new StringBuilder();
        strings = new ArrayList<String>();
        rules = new ArrayList<Integer>();

        for (int i = 0; i < concat.childCount(); i++) {
            RegexNode child = concat.childAt(i);

            switch (child.type()) {
                case RegexNode.Multi:
                    sb.append(child._str);
                    break;
                case RegexNode.One:
                    sb.append(child._ch);
                    break;
                case RegexNode.Ref:
                    if (sb.length() > 0) {
                        rules.add(strings.size());
                        strings.add(sb.toString());
                        // TODO: sb.Length = 0;
                        sb.setLength(0);
                    }
                    slot = child._m;

                    if (_caps != null && slot >= 0)
                        slot = _caps.get(slot);

                    rules.add(-Specials - 1 - slot);
                    break;
                default:
                    throw new IllegalArgumentException("type" + R.ReplacementError);
            }
        }

        if (sb.length() > 0) {
            rules.add(strings.size());
            strings.add(sb.toString());
        }

        _strings = strings;
        _rules = rules;
    }

    String _rep;
    List<String> _strings;          // table of string static finalants
    List<Integer> _rules;            // negative -> group #, positive -> string #

    // static final ints for special insertion patterns

    static final int Specials = 4;
    static final int LeftPortion = -1;
    static final int RightPortion = -2;
    static final int LastGroup = -3;
    static final int WholeString = -4;

    /*
     * Given a Match, emits into the StringBuilder the evaluated
     * substitution pattern.
     */
    private void ReplacementImpl(StringBuilder sb, Match match) {
        for (int i = 0; i < _rules.size(); i++) {
            int r = _rules.get(i);
            if (r >= 0)   // string lookup
                sb.append(_strings.get(r));
            else if (r < -Specials) // group lookup
                sb.append(match.groupToStringImpl(-Specials - 1 - r));
            else {
                switch (-Specials - 1 - r) { // special insertion patterns
                    case LeftPortion:
                        sb.append(match.getLeftSubstring());
                        break;
                    case RightPortion:
                        sb.append(match.getRightSubstring());
                        break;
                    case LastGroup:
                        sb.append(match.lastGroupToStringImpl());
                        break;
                    case WholeString:
                        sb.append(match.getOriginalString());
                        break;
                }
            }
        }
    }

    /*
     * Given a Match, emits into the List<String> the evaluated
     * Right-to-Left substitution pattern.
     */
    private void ReplacementImplRTL(List<String> al, Match match) {
        for (int i = _rules.size() - 1; i >= 0; i--) {
            int r = _rules.get(i);
            if (r >= 0)  // string lookup
                al.add(_strings.get(r));
            else if (r < -Specials) // group lookup
                al.add(match.groupToStringImpl(-Specials - 1 - r));
            else {
                switch (-Specials - 1 - r) { // special insertion patterns
                    case LeftPortion:
                        al.add(match.getLeftSubstring());
                        break;
                    case RightPortion:
                        al.add(match.getRightSubstring());
                        break;
                    case LastGroup:
                        al.add(match.lastGroupToStringImpl());
                        break;
                    case WholeString:
                        al.add(match.getOriginalString());
                        break;
                }
            }
        }
    }

    /*
     * The original pattern string
     */
    String pattern() {
        return _rep;
    }

    /*
     * Returns the replacement result for a single match
     */
    String replacement(Match match) {
        StringBuilder sb = new StringBuilder();

        ReplacementImpl(sb, match);

        return sb.toString();
    }

        /*
         * Three very similar algorithms appear below: replace (pattern),
         * replace (evaluator), and split.
         */


    /*
     * Replaces all ocurrances of the regex in the string with the
     * replacement pattern.
     *
     * Note that the special case of no matches is handled on its own:
     * with no matches, the input string is returned unchanged.
     * The right-to-left case is split out because StringBuilder
     * doesn't handle right-to-left string building directly very well.
     */
    String replace(Regex regex, String input, int count, int startat) {
        Match match;

        if (count < -1) {
            throw new IllegalArgumentException("count" + R.CountTooSmall);
        }
        if (startat < 0 || startat > input.length()) {
            throw new IllegalArgumentException("startat" + R.BeginIndexNotNegative);
        }

        if (count == 0)
            return input;

        match = regex.match(input, startat);
        if (!match.success()) {
            return input;
        } else {
            StringBuilder sb;

            if (!regex.rightToLeft()) {
                sb = new StringBuilder();
                int prevat = 0;

                do {
                    if (match.index() != prevat)
                        sb.append(input, prevat, match.index() - prevat);

                    prevat = match.index() + match.length();
                    ReplacementImpl(sb, match);
                    if (--count == 0)
                        break;

                    match = match.nextMatch();
                } while (match.success());

                if (prevat < input.length())
                    sb.append(input, prevat, input.length() - prevat);
            } else {
                List<String> al = new ArrayList<String>();
                int prevat = input.length();

                do {
                    if (match.index() + match.length() != prevat)
                        // TODO: input.substring(match.index() + match.length(), prevat - match.index() - match.length())
                        al.add(input.substring(match.index() + match.length(), prevat));
                    prevat = match.index();
                    ReplacementImplRTL(al, match);
                    if (--count == 0)
                        break;

                    match = match.nextMatch();
                } while (match.success());

                sb = new StringBuilder();

                if (prevat > 0)
                    sb.append(input, 0, prevat);

                for (int i = al.size() - 1; i >= 0; i--) {
                    sb.append(al.get(i));
                }
            }

            return sb.toString();
        }
    }

    /*
     * Replaces all ocurrances of the regex in the string with the
     * replacement evaluator.
     *
     * Note that the special case of no matches is handled on its own:
     * with no matches, the input string is returned unchanged.
     * The right-to-left case is split out because StringBuilder
     * doesn't handle right-to-left string building directly very well.
     */
    static String replace(MatchEvaluator evaluator, Regex regex, String input, int count, int startat) {
        Match match;

        if (evaluator == null) {
            throw new IllegalArgumentException("evaluator cannot be null.");
        }
        if (count < -1) {
            throw new IllegalArgumentException("count" + R.CountTooSmall);
        }
        if (startat < 0 || startat > input.length()) {
            throw new IllegalArgumentException("startat" + R.BeginIndexNotNegative);
        }

        if (count == 0)
            return input;

        match = regex.match(input, startat);

        if (!match.success()) {
            return input;
        } else {
            StringBuilder sb;

            if (!regex.rightToLeft()) {
                sb = new StringBuilder();
                int prevat = 0;

                do {
                    if (match.index() != prevat)
                        sb.append(input, prevat, match.index() - prevat);

                    prevat = match.index() + match.length();

                    sb.append(evaluator.evaluate(match));

                    if (--count == 0)
                        break;

                    match = match.nextMatch();
                } while (match.success());

                if (prevat < input.length())
                    sb.append(input, prevat, input.length() - prevat);
            } else {
                List<String> al = new ArrayList<String>();
                int prevat = input.length();

                do {
                    if (match.index() + match.length() != prevat)
                        // TODO: input.substring(match.index() + match.length(), prevat - match.index() - match.length())
                        al.add(input.substring(match.index() + match.length(), prevat));

                    prevat = match.index();

                    al.add(evaluator.evaluate(match));

                    if (--count == 0)
                        break;

                    match = match.nextMatch();
                } while (match.success());

                sb = new StringBuilder();

                if (prevat > 0)
                    sb.append(input, 0, prevat);

                for (int i = al.size() - 1; i >= 0; i--) {
                    sb.append(al.get(i));
                }
            }

            return sb.toString();
        }
    }

    /*
     * Does a split. In the right-to-left case we reorder the
     * array to be forwards.
     */
    static String[] split(Regex regex, String input, int count, int startat) {
        Match match;
        String[] result;

        if (count < 0) {
            throw new IllegalArgumentException("count" + R.CountTooSmall);
        }
        if (startat < 0 || startat > input.length()) {
            throw new IllegalArgumentException("startat" + R.BeginIndexNotNegative);
        }

        if (count == 1) {
            result = new String[1];
            result[0] = input;
            return result;
        }

        count -= 1;

        match = regex.match(input, startat);

        if (!match.success()) {
            result = new String[1];
            result[0] = input;
            return result;
        } else {
            List<String> al = new ArrayList<String>();

            if (!regex.rightToLeft()) {
                int prevat = 0;

                for (; ;) {
                    // TODO: input.substring(prevat, match.index() - prevat)
                    al.add(input.substring(prevat, match.index()));

                    prevat = match.index() + match.length();

                    // add all matched capture groups to the list.
                    for (int i = 1; i < match.groups().count(); i++) {
                        if (match.isMatched(i))
                            al.add(match.groups().get(i).value());
                    }

                    if (--count == 0)
                        break;

                    match = match.nextMatch();

                    if (!match.success())
                        break;
                }
                // TODO: input.substring(prevat, input.length() - prevat)
                al.add(input.substring(prevat));
            } else {
                int prevat = input.length();

                for (; ;) {
                    // TODO: input.substring(match.index() + match.length(), prevat - match.index() - match.length())
                    al.add(input.substring(match.index() + match.length(), prevat));

                    prevat = match.index();

                    // add all matched capture groups to the list.
                    for (int i = 1; i < match.groups().count(); i++) {
                        if (match.isMatched(i))
                            al.add(match.groups().get(i).toString());
                    }

                    if (--count == 0)
                        break;

                    match = match.nextMatch();

                    if (!match.success())
                        break;
                }
                // TODO: input.substring(0, prevat)
                al.add(input.substring(0, prevat));

                // TODO: al.Reverse(0, al.Count);
                Collections.reverse(al);
            }

            return al.toArray(new String[0]);
        }
    }
}
