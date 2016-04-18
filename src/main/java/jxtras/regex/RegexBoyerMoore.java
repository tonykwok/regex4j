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

import java.util.Locale;

// The RegexBoyerMoore object precomputes the Boyer-Moore
// tables for fast string scanning. These tables allow
// you to scan for the first occurrence of a string within
// a large body of text without examining every character.
// The performance of the heuristic depends on the actual
// string and the text being searched, but usually, the longer
// the string that is being searched for, the fewer characters
// need to be examined.
final class RegexBoyerMoore {
    int[] _positive;
    int[] _negativeASCII;
    int[][] _negativeUnicode;
    String _pattern;
    int _lowASCII;
    int _highASCII;
    boolean _rightToLeft;
    boolean _caseInsensitive;
    Locale _culture;

    /*
     * Constructs a Boyer-Moore state machine for searching for the string
     * pattern. The string must not be zero-length.
     */
    RegexBoyerMoore(String pattern, boolean caseInsensitive, boolean rightToLeft, Locale culture) {
        // Sorry,  you just can't use Boyer-Moore to find an empty pattern.
        // We're doing this for your own protection. (Really, for speed.)
        if (pattern.length() == 0) {
            throw new IllegalArgumentException("RegexBoyerMoore called with an empty string. This is bad for performance.");
        }

        int beforeFirst;
        int last;
        int bump;
        int examine;
        int scan;
        int match;
        char ch;

        // We do the ToLower character by character for consistency. With surrogate chars, doing
        // a toLowerCase on the entire string could actually change the surrogate pair.
        // This is more correct linguistically, but since Regex doesn't support surrogates, it's
        // more important to be consistent.
        if (caseInsensitive) {
            StringBuilder sb = new StringBuilder(pattern.length());
            for (int i = 0; i < pattern.length(); i++)
                sb.append(Character.toLowerCase(pattern.charAt(i) /*, culture */));
            pattern = sb.toString();
        }

        _pattern = pattern;
        _rightToLeft = rightToLeft;
        _caseInsensitive = caseInsensitive;
        _culture = culture;

        if (!rightToLeft) {
            beforeFirst = -1;
            last = pattern.length() - 1;
            bump = 1;
        } else {
            beforeFirst = pattern.length();
            last = 0;
            bump = -1;
        }

        /*
         * PART I - the good-suffix shift table
         *
         * compute the positive requirement:
         * if char "i" is the first one from the right that doesn't match,
         * then we know the matcher can advance by _positive[i].
         *
         * <STRIP>  This algorithm appears to be a simplified variant of the
         *          standard Boyer-Moore good suffix calculation.  It could
         *          be one of D.M. Sunday's variations, but I have not found which one.
         * </STRIP>
         */
        _positive = new int[pattern.length()];

        examine = last;
        ch = pattern.charAt(examine);
        _positive[examine] = bump;
        examine -= bump;

OuterloopBreak:
        for (; ; ) {
            // find an internal char (examine) that matches the tail
            for (; ; ) {
                if (examine == beforeFirst)
                    break OuterloopBreak; // TODO
                if (pattern.charAt(examine) == ch)
                    break;
                examine -= bump;
            }

            match = last;
            scan = examine;

            // find the length of the match
            for (; ; ) {
                if (scan == beforeFirst || pattern.charAt(match) != pattern.charAt(scan)) {
                    // at the end of the match, note the difference in _positive
                    // this is not the length of the match, but the distance from the match
                    // to the tail suffix.
                    if (_positive[match] == 0)
                        _positive[match] = match - scan;

                    // System.out.println("Set positive[" + match + "] to " + (match - scan));

                    break;
                }

                scan -= bump;
                match -= bump;
            }

            examine -= bump;
        }

// OuterloopBreak comes here:
        {
            match = last - bump;

            // scan for the chars for which there are no shifts that yield a different candidate

            /* <STRIP>
             *  The inside of the if statement used to say
             *  "_positive[match] = last - beforefirst;"
             *  I've changed it to the below code.  This
             *  is slightly less agressive in how much we skip, but at worst it
             *  should mean a little more work rather than skipping a potential
             *  match.
             * </STRIP>
             */
            while (match != beforeFirst) {
                if (_positive[match] == 0)
                    _positive[match] = bump;

                match -= bump;
            }

            //System.out.println("good suffix shift table:");
            //for (int i=0; i<_positive.length; i++)
            //    System.out.println("\t_positive[" + i + "] = " + _positive[i]);


            /*
             * PART II - the bad-character shift table
             *
             * compute the negative requirement:
             * if char "ch" is the reject character when testing position "i",
             * we can slide up by _negative[ch];
             * (_negative[ch] = str.Length - 1 - str.LastIndexOf(ch))
             *
             * the lookup table is divided into ASCII and Unicode portions;
             * only those parts of the Unicode 16-bit code set that actually
             * appear in the string are in the table. (Maximum size with
             * Unicode is 65K; ASCII only case is 512 bytes.)
             */

            _negativeASCII = new int[128];

            for (int i = 0; i < 128; i++)
                _negativeASCII[i] = last - beforeFirst;

            _lowASCII = 127;
            _highASCII = 0;

            for (examine = last; examine != beforeFirst; examine -= bump) {
                ch = pattern.charAt(examine);

                if (ch < 128) {
                    if (_lowASCII > ch)
                        _lowASCII = ch;

                    if (_highASCII < ch)
                        _highASCII = ch;

                    if (_negativeASCII[ch] == last - beforeFirst)
                        _negativeASCII[ch] = last - examine;
                } else {
                    int i = ch >> 8;
                    int j = ch & 0xFF;

                    if (_negativeUnicode == null) {
                        _negativeUnicode = new int[256][];
                    }

                    if (_negativeUnicode[i] == null) {
                        int[] newarray = new int[256];

                        for (int k = 0; k < 256; k++)
                            newarray[k] = last - beforeFirst;

                        if (i == 0) {
                            System.arraycopy(_negativeASCII, 0, newarray, 0, 128);
                            _negativeASCII = newarray;
                        }

                        _negativeUnicode[i] = newarray;
                    }

                    if (_negativeUnicode[i][j] == last - beforeFirst)
                        _negativeUnicode[i][j] = last - examine;
                }
            }
        }
    }

    private boolean matchPattern(String text, int index) {
        if (_caseInsensitive) {
            if (text.length() - index < _pattern.length()) {
                return false;
            }

            // TextInfo textinfo = _culture.TextInfo; // TODO:
            for (int i = 0; i < _pattern.length(); i++) {
                if (Character.toLowerCase(_pattern.charAt(i)) != _pattern.charAt(i)) {
                    throw new IllegalArgumentException("pattern should be converted to lower case in constructor!");
                }
                if (Character.toLowerCase(text.charAt(index + i)) != _pattern.charAt(i)) {
                    return false;
                }
            }
            return true;
        } else {
            // TODO: 0 == String.CompareOrdinal(_pattern, 0, text, index, _pattern.Length)
            return 0 == _pattern.compareTo(text.substring(index, index + _pattern.length()));
        }
    }

    /*
     * When a regex is anchored, we can do a quick IsMatch test instead of a Scan
     */
    boolean isMatch(String text, int index, int beglimit, int endlimit) {

        if (!_rightToLeft) {
            if (index < beglimit || endlimit - index < _pattern.length())
                return false;

            return matchPattern(text, index);
        } else {
            if (index > endlimit || index - beglimit < _pattern.length())
                return false;

            return matchPattern(text, index - _pattern.length());
        }
    }


    /*
     * Scan uses the Boyer-Moore algorithm to find the first occurrance
     * of the specified string within text, beginning at index, and
     * constrained within beglimit and endlimit.
     *
     * The direction and case-sensitivity of the match is determined
     * by the arguments to the RegexBoyerMoore constructor.
     */
    int scan(String text, int index, int beglimit, int endlimit) {
        int test;
        int test2;
        int match;
        int startmatch;
        int endmatch;
        int advance;
        int defadv;
        int bump;
        char chMatch;
        char chTest;
        int[] unicodeLookup;

        if (!_rightToLeft) {
            defadv = _pattern.length();
            startmatch = _pattern.length() - 1;
            endmatch = 0;
            test = index + defadv - 1;
            bump = 1;
        } else {
            defadv = -_pattern.length();
            startmatch = 0;
            endmatch = -defadv - 1;
            test = index + defadv;
            bump = -1;
        }

        chMatch = _pattern.charAt(startmatch);

        for (; ; ) {
            if (test >= endlimit || test < beglimit)
                return -1;

            chTest = text.charAt(test);

            if (_caseInsensitive)
                chTest = Character.toLowerCase(chTest); //TODO , _culture);

            if (chTest != chMatch) {
                if (chTest < 128)
                    advance = _negativeASCII[chTest];
                else if (null != _negativeUnicode && (null != (unicodeLookup = _negativeUnicode[chTest >> 8])))
                    advance = unicodeLookup[chTest & 0xFF];
                else
                    advance = defadv;

                test += advance;
            } else { // if (chTest == chMatch)
                test2 = test;
                match = startmatch;

                for (; ; ) {
                    if (match == endmatch)
                        return (_rightToLeft ? test2 + 1 : test2);

                    match -= bump;
                    test2 -= bump;

                    chTest = text.charAt(test2);

                    if (_caseInsensitive)
                        chTest = Character.toLowerCase(chTest/*, _culture */);

                    if (chTest != _pattern.charAt(match)) {
                        advance = _positive[match];
                        if ((chTest & 0xFF80) == 0)
                            test2 = (match - startmatch) + _negativeASCII[chTest];
                        else if (null != _negativeUnicode && (null != (unicodeLookup = _negativeUnicode[chTest >> 8])))
                            test2 = (match - startmatch) + unicodeLookup[chTest & 0xFF];
                        else {
                            test += advance;
                            break;
                        }

                        if (_rightToLeft ? test2 < advance : test2 > advance)
                            advance = test2;

                        test += advance;
                        break;
                    }
                }
            }
        }
    }

    /*
     * Used when dumping for debugging.
     */
    @Override
    public String toString() {
        return _pattern;
    }

    //#if DEBUG
    public String dump(String indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent + "BM Pattern: " + _pattern + "\n");
        sb.append(indent + "Positive: ");
        for (int i = 0; i < _positive.length; i++) {
            sb.append(_positive[i] + " ");
        }
        sb.append("\n");

        if (_negativeASCII != null) {
            sb.append(indent + "Negative table\n");
            for (int i = 0; i < _negativeASCII.length; i++) {
                if (_negativeASCII[i] != _pattern.length()) {
                    sb.append(indent + "  " + Regex.escape(String.valueOf((char) i)));
                }
            }
        }

        return sb.toString();
    }
//#endif
}