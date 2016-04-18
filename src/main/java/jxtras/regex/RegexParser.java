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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


// This RegexParser class is  to the Regex package.
// It builds a tree of RegexNodes from a regular expression

// Implementation notes:
//
// It would be nice to get rid of the comment modes, since the
// ScanBlank() calls are just kind of duct-taped in.

final class RegexParser {
    RegexNode _stack;
    RegexNode _group;
    RegexNode _alternation;
    RegexNode _concatenation;
    RegexNode _unit;

    String _pattern;
    int _currentPos;
    Locale _culture; // TODO: not implemented yet

    int _autocap;
    int _capcount;
    int _captop;
    int _capsize;

    Map<Integer, Integer> _caps;
    Map<String, Integer> _capnames;

    int[] _capnumlist; //TODO: int32
    List<String> _capnamelist;

    int _options;
    List<Integer> _optionsStack;

    boolean _ignoreNextParen = false;

    static final long MaxValueDiv10 = Integer.MAX_VALUE / 10; //TODO: int32
    static final long MaxValueMod10 = Integer.MAX_VALUE % 10; //TODO: int32

    /*
     * This static call static finalructs a RegexTree from a regular expression
     * pattern string and an option string.
     *
     * The method creates, drives, and drops a parser instance.
     */
    static RegexTree parse(String pattern, int options) {
        final RegexParser parser = new RegexParser((options & RegexOptions.CultureInvariant) != 0 ? Locale.ROOT : Locale.getDefault());

        parser._options = options;
        parser.setPattern(pattern);
        parser.countCaptures();
        parser.reset(options);
        RegexNode root = parser.scanRegex();

        String[] capnamelist;
        if (parser._capnamelist == null) {
            capnamelist = null;
        } else {
            capnamelist = parser._capnamelist.toArray(new String[0]);
        }

        return new RegexTree(root, parser._caps, parser._capnumlist, parser._captop, parser._capnames, capnamelist, options);
    }

    /*
     * This static call static constructor a flat concatenation node given
     * a replacement pattern.
     */
    static RegexReplacement parseReplacement(String rep, Map<Integer, Integer> caps, int capsize, Map<String, Integer> capnames, int option) {
        RegexParser parser = new RegexParser((option & RegexOptions.CultureInvariant) != 0 ? Locale.ROOT : Locale.getDefault());

        parser._options = option;

        parser.noteCaptures(caps, capsize, capnames);
        parser.setPattern(rep);
        RegexNode root = parser.scanReplacement();

        return new RegexReplacement(rep, root, caps);
    }

    /*
     * Escapes all metacharacters (including |,(,),[,{,|,^,$,*,+,?,\, spaces and #)
     */
    static String escape(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (isMetachar(input.charAt(i))) {
                StringBuilder sb = new StringBuilder();
                char ch = input.charAt(i);
                int lastpos;

                sb.append(input.substring(0, i));
                do {
                    sb.append('\\');
                    switch (ch) {
                        case '\n':
                            ch = 'n';
                            break;
                        case '\r':
                            ch = 'r';
                            break;
                        case '\t':
                            ch = 't';
                            break;
                        case '\f':
                            ch = 'f';
                            break;
                    }
                    sb.append(ch);
                    i++;
                    lastpos = i;

                    while (i < input.length()) {
                        ch = input.charAt(i);
                        if (isMetachar(ch)) {
                            break;
                        }
                        i++;
                    }

                    sb.append(input.substring(lastpos, i));
                } while (i < input.length());

                return sb.toString();
            }
        }

        return input;
    }

    /*
     * Escapes all metacharacters (including (,),[,],{,},|,^,$,*,+,?,\, spaces and #)
     */
    static String unescape(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '\\') {
                StringBuilder sb = new StringBuilder();
                RegexParser parser = new RegexParser(Locale.ROOT);
                int lastpos;
                parser.setPattern(input);

                sb.append(input.substring(0, i));
                do {
                    i++;
                    parser.textto(i);
                    if (i < input.length()) {
                        sb.append(parser.scanCharEscape());
                    }
                    i = parser.textpos();
                    lastpos = i;
                    while (i < input.length() && input.charAt(i) != '\\') {
                        i++;
                    }
                    sb.append(input.substring(lastpos, i));
                } while (i < input.length());

                return sb.toString();
            }
        }

        return input;
    }

    /*
     * Private static finalructor.
     */
    private RegexParser(Locale culture) {
        _culture = culture;
        _optionsStack = new ArrayList<Integer>();
        _caps = new HashMap<Integer, Integer>();
    }

    /*
     * Drops a string into the pattern buffer.
     */
    void setPattern(String pattern) {
        if (pattern == null)
            pattern = "";//String.Empty;
        _pattern = pattern;
        _currentPos = 0;
    }

    /*
     * Resets parsing to the beginning of the pattern.
     */
    void reset(int topopts) {
        _currentPos = 0;
        _autocap = 1;
        _ignoreNextParen = false;

        if (_optionsStack.size() > 0) {
            // TODO:  _optionsStack.RemoveRange(0, _optionsStack.size() - 1);
            _optionsStack.subList(0, _optionsStack.size() - 1).clear();
        }

        _options = topopts;
        _stack = null;
    }

    /*
     * The main parsing function.
     */
    RegexNode scanRegex() {
        char ch = '@'; // nonspecial ch, means at beginning
        boolean isQuantifier = false;

        startGroup(new RegexNode(RegexNode.Capture, _options, 0, -1));

OUTER_SCAN:
        while (charsRight() > 0) {
            boolean wasPrevQuantifier = isQuantifier;
            isQuantifier = false;

            scanBlank();

            int startpos = textpos();

            // move past all of the normal characters.  We'll stop when we hit some kind of control character,
            // or if IgnorePatternWhiteSpace is on, we'll stop when we see some whitespace.
            if (useOptionX()) {
                while (charsRight() > 0 && (!isStopperX(ch = rightChar()) || ch == '{' && !isTrueQuantifier())) {
                    moveRight();
                }
            } else {
                while (charsRight() > 0 && (!isSpecial(ch = rightChar()) || ch == '{' && !isTrueQuantifier())) {
                    moveRight();
                }
            }

            int endpos = textpos();

            scanBlank();

            if (charsRight() == 0) {
                ch = '!'; // nonspecial, means at end
            } else if (isSpecial(ch = rightChar())) {
                isQuantifier = isQuantifier(ch);
                moveRight();
            } else {
                ch = ' '; // nonspecial, means at ordinary char
            }
            if (startpos < endpos) {
                int cchUnquantified = endpos - startpos - (isQuantifier ? 1 : 0);

                wasPrevQuantifier = false;

                if (cchUnquantified > 0) {
                    addConcatenate(startpos, cchUnquantified, false);
                }

                if (isQuantifier) {
                    addUnitOne(charAt(endpos - 1));
                }
            }

            switch (ch) {
                case '!':
                    break OUTER_SCAN; // TODO: break BreakOuterScan;

                case ' ':
                    continue OUTER_SCAN; // TODO: break ContinueOuterScan;

                case '[':
                    addUnitSet(scanCharClass(useOptionI()).toString());
                    break;

                case '(': {
                    RegexNode grouper;

                    pushOptions();

                    if (null == (grouper = scanGroupOpen())) {
                        popKeepOptions();
                    } else {
                        pushGroup();
                        startGroup(grouper);
                    }
                }
                continue;

                case '|':
                    addAlternate();
                    continue OUTER_SCAN; // TODO: break ContinueOuterScan;

                case ')':
                    if (emptyStack()) {
                        throw makeException(R.TooManyParents);
                    }

                    addGroup();
                    popGroup();
                    popOptions();

                    if (unit() == null) {
                        continue OUTER_SCAN; // TODO: break ContinueOuterScan;
                    }
                    break;

                case '\\':
                    addUnitNode(scanBackslash());
                    break;

                case '^':
                    addUnitType(useOptionM() ? RegexNode.Bol : RegexNode.Beginning);
                    break;

                case '$':
                    addUnitType(useOptionM() ? RegexNode.Eol : RegexNode.EndZ);
                    break;

                case '.':
                    if (useOptionS()) {
                        addUnitSet(RegexCharClass.AnyClass);
                    } else {
                        AddUnitNotone('\n');
                    }
                    break;

                case '{':
                case '*':
                case '+':
                case '?':
                    if (unit() == null) {
                        throw makeException(wasPrevQuantifier ? String.format(R.NestedQuantify, ch) : R.QuantifyAfterNothing);
                    }
                    moveLeft();
                    break;

                default: {
                    throw makeException(R.InternalError);
                }
            }

            scanBlank();

            if (charsRight() == 0 || !(isQuantifier = isTrueQuantifier())) {
                addConcatenate();
                continue OUTER_SCAN; // TODO: break ContinueOuterScan;
            }

            ch = moveRightGetChar();

            // Handle quantifiers
            while (unit() != null) {
                int min;
                int max;
                boolean lazy;

                switch (ch) {
                    case '*':
                        min = 0;
                        max = Integer.MAX_VALUE;
                        break;

                    case '?':
                        min = 0;
                        max = 1;
                        break;

                    case '+':
                        min = 1;
                        max = Integer.MAX_VALUE;
                        break;

                    case '{': {
                        startpos = textpos();
                        max = min = scanDecimal();
                        if (startpos < textpos()) {
                            if (charsRight() > 0 && rightChar() == ',') {
                                moveRight();
                                if (charsRight() == 0 || rightChar() == '}') {
                                    max = Integer.MAX_VALUE;
                                } else {
                                    max = scanDecimal();
                                }
                            }
                        }

                        if (startpos == textpos() || charsRight() == 0 || moveRightGetChar() != '}') {
                            addConcatenate();
                            textto(startpos - 1);
                            continue OUTER_SCAN; // TODO: break ContinueOuterScan;
                        }
                    }
                    break;

                    default: {
                        throw makeException(R.InternalError);
                    }
                }

                scanBlank();

                if (charsRight() == 0 || rightChar() != '?') {
                    lazy = false;
                } else {
                    moveRight();
                    lazy = true;
                }

                if (min > max) {
                    throw makeException(R.IllegalRange);
                }

                addConcatenate(lazy, min, max);
            }

            // ContinueOuterScan: comes here
        }

        // BreakOuterScan: comes here

        if (!emptyStack()) {
            throw makeException(R.NotEnoughParens);
        }

        addGroup();

        return unit();
    }

    /*
     * Simple parsing for replacement patterns
     */
    RegexNode scanReplacement() {
        int c;
        int startpos;

        _concatenation = new RegexNode(RegexNode.Concatenate, _options);

        for (; ;) {
            c = charsRight();
            if (c == 0) {
                break;
            }

            startpos = textpos();
            while (c > 0 && rightChar() != '$') {
                moveRight();
                c--;
            }

            addConcatenate(startpos, textpos() - startpos, true);

            if (c > 0) {
                if (moveRightGetChar() == '$') {
                    addUnitNode(scanDollar());
                }
                addConcatenate();
            }
        }

        return _concatenation;
    }

    /*
     * Scans contents of [] (not including []'s), and converts to a
     * RegexCharClass.
     */
    RegexCharClass scanCharClass(boolean caseInsensitive) {
        return scanCharClass(caseInsensitive, false);
    }

    /*
     * Scans contents of [] (not including []'s), and converts to a
     * RegexCharClass.
     */
    RegexCharClass scanCharClass(boolean caseInsensitive, boolean scanOnly) {
        char ch = '\0';
        char chPrev = '\0';
        boolean inRange = false;
        boolean firstChar = true;
        boolean closed = false;

        RegexCharClass cc;

        cc = scanOnly ? null : new RegexCharClass();

        if (charsRight() > 0 && rightChar() == '^') {
            moveRight();
            if (!scanOnly) {
                cc.setNegate(true);
            }
        }

        for (; charsRight() > 0; firstChar = false) {
            boolean fTranslatedChar = false;
            ch = moveRightGetChar();
            if (ch == ']') {
                if (!firstChar) {
                    closed = true;
                    break;
                }
            } else if (ch == '\\' && charsRight() > 0) {
                switch (ch = moveRightGetChar()) {
                    case 'D':
                    case 'd':
                        if (!scanOnly) {
                            if (inRange) {
                                throw makeException(String.format(R.BadClassInCharRange, ch));
                            }
                            cc.addDigit(useOptionE(), ch == 'D', _pattern);
                        }
                        continue;

                    case 'S':
                    case 's':
                        if (!scanOnly) {
                            if (inRange) {
                                throw makeException(String.format(R.BadClassInCharRange, ch));
                            }
                            cc.addSpace(useOptionE(), ch == 'S');
                        }
                        continue;

                    case 'W':
                    case 'w':
                        if (!scanOnly) {
                            if (inRange) {
                                throw makeException(String.format(R.BadClassInCharRange, ch));
                            }
                            cc.addWord(useOptionE(), ch == 'W');
                        }
                        continue;

                    case 'p':
                    case 'P':
                        if (!scanOnly) {
                            if (inRange) {
                                throw makeException(String.format(R.BadClassInCharRange, ch));
                            }
                            cc.addCategoryFromName(parseProperty(), (ch != 'p'), caseInsensitive, _pattern);
                        } else {
                            parseProperty();
                        }
                        continue;

                    case '-':
                        if (!scanOnly) {
                            cc.addRange(ch, ch);
                        }
                        continue;

                    default:
                        moveLeft();
                        ch = scanCharEscape(); // non-literal character
                        fTranslatedChar = true;
                        break;          // this break will only break out of the switch
                }
            } else if (ch == '[') {
                // This is code for Posix style properties - [:Ll:] or [:IsTibetan:].
                // It currently doesn't do anything other than skip the whole thing!
                if (charsRight() > 0 && rightChar() == ':' && !inRange) {
                    String name;
                    int savePos = textpos();

                    moveRight();
                    name = scanCapname();
                    if (charsRight() < 2 || moveRightGetChar() != ':' || moveRightGetChar() != ']') {
                        textto(savePos);
                    }
                    // else lookup name (nyi)
                }
            }


            if (inRange) {
                inRange = false;
                if (!scanOnly) {
                    if (ch == '[' && !fTranslatedChar && !firstChar) {
                        // We thought we were in a range, but we're actually starting a subtraction.
                        // In that case, we'll add chPrev to our char class, skip the opening [, and
                        // scan the new character class recursively.
                        cc.addChar(chPrev);
                        cc.addSubtraction(scanCharClass(caseInsensitive, false));

                        if (charsRight() > 0 && rightChar() != ']') {
                            throw makeException(R.SubtractionMustBeLast);
                        }
                    } else {
                        // a regular range, like a-z
                        if (chPrev > ch) {
                            throw makeException(R.ReversedCharRange);
                        }
                        cc.addRange(chPrev, ch);
                    }
                }
            } else if (charsRight() >= 2 && rightChar() == '-' && rightChar(1) != ']') {
                // this could be the start of a range
                chPrev = ch;
                inRange = true;
                moveRight();
            } else if (charsRight() >= 1 && ch == '-' && !fTranslatedChar && rightChar() == '[' && !firstChar) {
                // we aren't in a range, and now there is a subtraction.  Usually this happens
                // only when a subtraction follows a range, like [a-z-[b]]
                if (!scanOnly) {
                    moveRight(1);
                    cc.addSubtraction(scanCharClass(caseInsensitive, false));

                    if (charsRight() > 0 && rightChar() != ']') {
                        throw makeException(R.SubtractionMustBeLast);
                    }
                } else {
                    moveRight(1);
                    scanCharClass(caseInsensitive, true);
                }
            } else {
                if (!scanOnly) {
                    cc.addRange(ch, ch);
                }
            }
        }

        if (!closed) {
            throw makeException(R.UnterminatedBracket);
        }

        if (!scanOnly && caseInsensitive) {
            cc.addLowercase(_culture);
        }

        return cc;
    }

    /*
     * Scans chars following a '(' (not counting the '('), and returns
     * a RegexNode for the type of group scanned, or null if the group
     * simply changed options (?cimsx-cimsx) or was a comment (#...).
     */
    RegexNode scanGroupOpen() {
        char ch = '\0';
        int NodeType;
        char close = '>';


        // just return a RegexNode if we have:
        // 1. "(" followed by nothing
        // 2. "(x" where x != ?
        // 3. "(?)"
        if (charsRight() == 0 || rightChar() != '?' || (rightChar() == '?' && (charsRight() > 1 && rightChar(1) == ')'))) {
            if (useOptionN() || _ignoreNextParen) {
                _ignoreNextParen = false;
                return new RegexNode(RegexNode.Group, _options);
            } else {
                return new RegexNode(RegexNode.Capture, _options, _autocap++, -1);
            }
        }

        moveRight();

RECOGNIZE:
        for (; ;) {
            if (charsRight() == 0)
                break;

            switch (ch = moveRightGetChar()) {
                case ':':
                    NodeType = RegexNode.Group;
                    break;

                case '=':
                    _options &= ~(RegexOptions.RightToLeft);
                    NodeType = RegexNode.Require;
                    break;

                case '!':
                    _options &= ~(RegexOptions.RightToLeft);
                    NodeType = RegexNode.Prevent;
                    break;

                case '>':
                    NodeType = RegexNode.Greedy;
                    break;

                case '\'':
                    close = '\'';
                    // TODO:
                    // fallthrough
                    // goto case '<';

                case '<':
                    if (charsRight() == 0) {
                        break RECOGNIZE; // TODO: using break instead of goto
                    }

                    switch (ch = moveRightGetChar()) {
                        case '=':
                            if (close == '\'') {
                                break RECOGNIZE;
                            }

                            _options |= RegexOptions.RightToLeft;
                            NodeType = RegexNode.Require;
                            break;

                        case '!':
                            if (close == '\'') {
                                break RECOGNIZE; // TODO: using break instead of goto
                            }

                            _options |= RegexOptions.RightToLeft;
                            NodeType = RegexNode.Prevent;
                            break;

                        default:
                            moveLeft();
                            int capnum = -1;
                            int uncapnum = -1;
                            boolean proceed = false;

                            // grab part before -

                            if (ch >= '0' && ch <= '9') {
                                capnum = scanDecimal();

                                if (!isCaptureSlot(capnum)) {
                                    capnum = -1;
                                }

                                // check if we have bogus characters after the number
                                if (charsRight() > 0 && !(rightChar() == close || rightChar() == '-')) {
                                    throw makeException(R.InvalidGroupName);
                                }
                                if (capnum == 0) {
                                    throw makeException(R.CapnumNotZero);
                                }
                            } else if (RegexCharClass.isWordChar(ch)) {
                                String capname = scanCapname();

                                if (isCaptureName(capname)) {
                                    capnum = captureSlotFromName(capname);
                                }

                                // check if we have bogus character after the name
                                if (charsRight() > 0 && !(rightChar() == close || rightChar() == '-')) {
                                    throw makeException(R.InvalidGroupName);
                                }
                            } else if (ch == '-') {
                                proceed = true;
                            } else {
                                // bad group name - starts with something other than a word character and isn't a number
                                throw makeException(R.InvalidGroupName);
                            }

                            // grab part after - if any

                            if ((capnum != -1 || proceed == true) && charsRight() > 0 && rightChar() == '-') {
                                moveRight();
                                ch = rightChar();

                                if (ch >= '0' && ch <= '9') {
                                    uncapnum = scanDecimal();

                                    if (!isCaptureSlot(uncapnum)) {
                                        throw makeException(String.format(R.UndefinedBackref, uncapnum));
                                    }

                                    // check if we have bogus characters after the number
                                    if (charsRight() > 0 && rightChar() != close) {
                                        throw makeException(R.InvalidGroupName);
                                    }
                                } else if (RegexCharClass.isWordChar(ch)) {
                                    String uncapname = scanCapname();
                                    if (isCaptureName(uncapname)) {
                                        uncapnum = captureSlotFromName(uncapname);
                                    } else {
                                        throw makeException(String.format(R.UndefinedNameRef, uncapname));
                                    }

                                    // check if we have bogus character after the name
                                    if (charsRight() > 0 && rightChar() != close) {
                                        throw makeException(R.InvalidGroupName);
                                    }
                                } else {
                                    // bad group name - starts with something other than a word character and isn't a number
                                    throw makeException(R.InvalidGroupName);
                                }
                            }

                            // actually make the node

                            if ((capnum != -1 || uncapnum != -1) && charsRight() > 0 && moveRightGetChar() == close) {
                                return new RegexNode(RegexNode.Capture, _options, capnum, uncapnum);
                            }
                            break RECOGNIZE; // TODO: using break instead of goto
                    }
                    break;

                case '(':
                    // alternation static finalruct (?(...) | )

                    int parenPos = textpos();
                    if (charsRight() > 0) {
                        ch = rightChar();

                        // check if the alternation condition is a backref
                        if (ch >= '0' && ch <= '9') {
                            int capnum = scanDecimal();
                            if (charsRight() > 0 && moveRightGetChar() == ')') {
                                if (isCaptureSlot(capnum)) {
                                    return new RegexNode(RegexNode.Testref, _options, capnum);
                                } else {
                                    throw makeException(String.format(R.UndefinedReference, capnum));
                                }
                            } else {
                                throw makeException(String.format(R.MalformedReference, capnum));
                            }
                        } else if (RegexCharClass.isWordChar(ch)) {
                            String capname = scanCapname();
                            if (isCaptureName(capname) && charsRight() > 0 && moveRightGetChar() == ')') {
                                return new RegexNode(RegexNode.Testref, _options, captureSlotFromName(capname));
                            }
                        }
                    }

                    // not a backref
                    NodeType = RegexNode.Testgroup;
                    textto(parenPos - 1);       // jump to the start of the parentheses
                    _ignoreNextParen = true;    // but make sure we don't try to capture the insides

                    int charsRight = charsRight();
                    if (charsRight >= 3 && rightChar(1) == '?') {
                        char rightchar2 = rightChar(2);
                        // disallow comments in the condition
                        if (rightchar2 == '#') {
                            throw makeException(R.AlternationCantHaveComment);
                        }

                        // disallow named capture group (?<..>..) in the condition
                        if (rightchar2 == '\'') {
                            throw makeException(R.AlternationCantCapture);
                        } else {
                            if (charsRight >= 4 && (rightchar2 == '<' && rightChar(3) != '!' && rightChar(3) != '=')) {
                                throw makeException(R.AlternationCantCapture);
                            }
                        }
                    }
                    break;

                default:
                    moveLeft();

                    NodeType = RegexNode.Group;
                    scanOptions();
                    if (charsRight() == 0) {
                        break RECOGNIZE; // TODO: using break instead of goto
                    }

                    if ((ch = moveRightGetChar()) == ')') {
                        return null;
                    }

                    if (ch != ':') {
                        break RECOGNIZE; // TODO: using break instead of goto
                    }
                    break;
            }

            return new RegexNode(NodeType, _options);
        }

        // break Recognize comes here
        throw makeException(R.UnrecognizedGrouping);
    }

    /*
     * Scans whitespace or x-mode comments.
     */
    void scanBlank() {
        if (useOptionX()) {
            for (; ;) {
                while (charsRight() > 0 && isSpace(rightChar())) {
                    moveRight();
                }
                if (charsRight() == 0) {
                    break;
                }
                if (rightChar() == '#') {
                    while (charsRight() > 0 && rightChar() != '\n') {
                        moveRight();
                    }
                } else if (charsRight() >= 3 && rightChar(2) == '#' && rightChar(1) == '?' && rightChar() == '(') {
                    while (charsRight() > 0 && rightChar() != ')') {
                        moveRight();
                    }
                    if (charsRight() == 0) {
                        throw makeException(R.UnterminatedComment);
                    }
                    moveRight();
                } else
                    break;
            }
        } else {
            for (; ;) {
                if (charsRight() < 3 || rightChar(2) != '#' || rightChar(1) != '?' || rightChar() != '(') {
                    return;
                }
                while (charsRight() > 0 && rightChar() != ')') {
                    moveRight();
                }
                if (charsRight() == 0) {
                    throw makeException(R.UnterminatedComment);
                }
                moveRight();
            }
        }
    }

    /*
     * Scans chars following a '\' (not counting the '\'), and returns
     * a RegexNode for the type of atom scanned.
     */
    RegexNode scanBackslash() {
        char ch;
        RegexCharClass cc;

        if (charsRight() == 0) {
            throw makeException(R.IllegalEndEscape);
        }

        switch (ch = rightChar()) {
            case 'b':
            case 'B':
            case 'A':
            case 'G':
            case 'Z':
            case 'z':
                moveRight();
                return new RegexNode(typeFromCode(ch), _options);

            case 'w':
                moveRight();
                if (useOptionE()) {
                    return new RegexNode(RegexNode.Set, _options, RegexCharClass.ECMAWordClass);
                }
                return new RegexNode(RegexNode.Set, _options, RegexCharClass.WordClass);

            case 'W':
                moveRight();
                if (useOptionE()) {
                    return new RegexNode(RegexNode.Set, _options, RegexCharClass.NotECMAWordClass);
                }
                return new RegexNode(RegexNode.Set, _options, RegexCharClass.NotWordClass);

            case 's':
                moveRight();
                if (useOptionE()) {
                    return new RegexNode(RegexNode.Set, _options, RegexCharClass.ECMASpaceClass);
                }
                return new RegexNode(RegexNode.Set, _options, RegexCharClass.SpaceClass);

            case 'S':
                moveRight();
                if (useOptionE()) {
                    return new RegexNode(RegexNode.Set, _options, RegexCharClass.NotECMASpaceClass);
                }
                return new RegexNode(RegexNode.Set, _options, RegexCharClass.NotSpaceClass);

            case 'd':
                moveRight();
                if (useOptionE()) {
                    return new RegexNode(RegexNode.Set, _options, RegexCharClass.ECMADigitClass);
                }
                return new RegexNode(RegexNode.Set, _options, RegexCharClass.DigitClass);

            case 'D':
                moveRight();
                if (useOptionE()) {
                    return new RegexNode(RegexNode.Set, _options, RegexCharClass.NotECMADigitClass);
                }
                return new RegexNode(RegexNode.Set, _options, RegexCharClass.NotDigitClass);

            case 'p':
            case 'P':
                moveRight();
                cc = new RegexCharClass();
                cc.addCategoryFromName(parseProperty(), (ch != 'p'), useOptionI(), _pattern);
                if (useOptionI()) {
                    cc.addLowercase(_culture);
                }
                return new RegexNode(RegexNode.Set, _options, cc.toString());

            default:
                return scanBasicBackslash();
        }
    }

    /*
     * Scans \-style backreferences and character escapes
     */
    RegexNode scanBasicBackslash() {
        if (charsRight() == 0) {
            throw makeException(R.IllegalEndEscape);
        }

        char ch;
        boolean angled = false;
        char close = '\0';
        int backpos;

        backpos = textpos();
        ch = rightChar();

        // allow \k<foo> instead of \<foo>, which is now deprecated

        if (ch == 'k') {
            if (charsRight() >= 2) {
                moveRight();
                ch = moveRightGetChar();

                if (ch == '<' || ch == '\'') {
                    angled = true;
                    close = (ch == '\'') ? '\'' : '>';
                }
            }

            if (!angled || charsRight() <= 0) {
                throw makeException(R.MalformedNameRef);
            }

            ch = rightChar();
        }
        // Note angle without \g <
        else if ((ch == '<' || ch == '\'') && charsRight() > 1) {
            angled = true;
            close = (ch == '\'') ? '\'' : '>';

            moveRight();
            ch = rightChar();
        }

        // Try to parse backreference: \<1> or \<cap>
        if (angled && ch >= '0' && ch <= '9') {
            int capnum = scanDecimal();

            if (charsRight() > 0 && moveRightGetChar() == close) {
                if (isCaptureSlot(capnum)) {
                    return new RegexNode(RegexNode.Ref, _options, capnum);
                }
                else {
                    throw makeException(String.format(R.UndefinedBackref, capnum));
                }
            }
        }
        // Try to parse backreference or octal: \1
        else if (!angled && ch >= '1' && ch <= '9') {
            if (useOptionE()) {
                int capnum = -1;
                int newcapnum = ch - '0';
                int pos = textpos() - 1;
                while (newcapnum <= _captop) {
                    if (isCaptureSlot(newcapnum) && (_caps == null || _caps.get(newcapnum) < pos)) {
                        capnum = newcapnum;
                    }
                    moveRight();
                    if (charsRight() == 0 || (ch = rightChar()) < '0' || ch > '9') {
                        break;
                    }
                    newcapnum = newcapnum * 10 + (ch - '0');
                }
                if (capnum >= 0) {
                    return new RegexNode(RegexNode.Ref, _options, capnum);
                }
            } else {
                int capnum = scanDecimal();
                if (isCaptureSlot(capnum)) {
                    return new RegexNode(RegexNode.Ref, _options, capnum);
                } else if (capnum <= 9) {
                    throw makeException(String.format(R.UndefinedBackref, capnum));
                }
            }
        } else if (angled && RegexCharClass.isWordChar(ch)) {
            String capname = scanCapname();
            if (charsRight() > 0 && moveRightGetChar() == close) {
                if (isCaptureName(capname)) {
                    return new RegexNode(RegexNode.Ref, _options, captureSlotFromName(capname));
                } else {
                    throw makeException(String.format(R.UndefinedNameRef, capname));
                }
            }
        }

        // Not backreference: must be char code

        textto(backpos);
        ch = scanCharEscape();

        if (useOptionI())
            ch = Character.toLowerCase(ch /* , _culture */);

        return new RegexNode(RegexNode.One, _options, ch);
    }

    /*
     * Scans $ patterns recognized within replacment patterns
     */
    RegexNode scanDollar() {
        if (charsRight() == 0) {
            return new RegexNode(RegexNode.One, _options, '$');
        }

        char ch = rightChar();
        boolean angled;
        int backpos = textpos();
        int lastEndPos = backpos;

        // Note angle

        if (ch == '{' && charsRight() > 1) {
            angled = true;
            moveRight();
            ch = rightChar();
        } else {
            angled = false;
        }

        // Try to parse backreference: \1 or \{1} or \{cap}

        if (ch >= '0' && ch <= '9') {
            if (!angled && useOptionE()) {
                int capnum = -1;
                int newcapnum = ch - '0';
                moveRight();
                if (isCaptureSlot(newcapnum)) {
                    capnum = newcapnum;
                    lastEndPos = textpos();
                }

                while (charsRight() > 0 && (ch = rightChar()) >= '0' && ch <= '9') {
                    int digit = ch - '0';
                    if (newcapnum > (MaxValueDiv10) || (newcapnum == (MaxValueDiv10) && digit > (MaxValueMod10))) {
                        throw makeException(R.CaptureGroupOutOfRange);
                    }

                    newcapnum = newcapnum * 10 + digit;

                    moveRight();
                    if (isCaptureSlot(newcapnum)) {
                        capnum = newcapnum;
                        lastEndPos = textpos();
                    }
                }
                textto(lastEndPos);
                if (capnum >= 0) {
                    return new RegexNode(RegexNode.Ref, _options, capnum);
                }
            } else {
                int capnum = scanDecimal();
                if (!angled || charsRight() > 0 && moveRightGetChar() == '}') {
                    if (isCaptureSlot(capnum)) {
                        return new RegexNode(RegexNode.Ref, _options, capnum);
                    }
                }
            }
        } else if (angled && RegexCharClass.isWordChar(ch)) {
            String capname = scanCapname();

            if (charsRight() > 0 && moveRightGetChar() == '}') {
                if (isCaptureName(capname)) {
                    return new RegexNode(RegexNode.Ref, _options, captureSlotFromName(capname));
                }
            }
        } else if (!angled) {
            int capnum = 1;

            switch (ch) {
                case '$':
                    moveRight();
                    return new RegexNode(RegexNode.One, _options, '$');

                case '&':
                    capnum = 0;
                    break;

                case '`':
                    capnum = RegexReplacement.LeftPortion;
                    break;

                case '\'':
                    capnum = RegexReplacement.RightPortion;
                    break;

                case '+':
                    capnum = RegexReplacement.LastGroup;
                    break;

                case '_':
                    capnum = RegexReplacement.WholeString;
                    break;
            }

            if (capnum != 1) {
                moveRight();
                return new RegexNode(RegexNode.Ref, _options, capnum);
            }
        }

        // unrecognized $: literalize

        textto(backpos);
        return new RegexNode(RegexNode.One, _options, '$');
    }

    /*
     * Scans a capture name: consumes word chars
     */
    String scanCapname() {
        int startpos = textpos();

        while (charsRight() > 0) {
            if (!RegexCharClass.isWordChar(moveRightGetChar())) {
                moveLeft();
                break;
            }
        }

        // TODO: _pattern.substring(startpos, Textpos() - startpos);
        return _pattern.substring(startpos, textpos());
    }


    /*
     * Scans up to three octal digits (stops before exceeding 0377).
     */
    char scanOctal() {
        int d;
        int i;
        int c;

        // Consume octal chars only up to 3 digits and value 0377

        c = 3;

        if (c > charsRight())
            c = charsRight();

        // TODO: uint
        for (i = 0; c > 0 && (d = octalDigit(rightChar())) >= 0; c -= 1) {
            moveRight();
            i *= 8;
            i += d;
            if (useOptionE() && i >= 0x20) {
                break;
            }
        }

        // Octal codes only go up to 255.  Any larger and the behavior that Perl follows
        // is simply to truncate the high bits.
        i &= 0xFF;

        return (char) i;
    }

    /*
     * Scans any number of decimal digits (pegs value at 2^31-1 if too large)
     */
    int scanDecimal() {
        int i = 0;
        int d;

        // TODO: uint
        while (charsRight() > 0 && (d = decimalDigit(rightChar())) >= 0) {
            moveRight();

            if (i > (MaxValueDiv10) || (i == (MaxValueDiv10) && d > (MaxValueMod10))) {
                throw makeException(R.CaptureGroupOutOfRange);
            }
            i *= 10;
            i += d;
        }

        return i;
    }

    /*
     * Scans exactly c hex digits (c=2 for \xFF, c=4 for \uFFFF)
     */
    char scanHex(int c) {
        int i;
        int d;

        i = 0;

        if (charsRight() >= c) {
            for (; c > 0 && ((d = hexDigit(moveRightGetChar())) >= 0); c -= 1) {
                i *= 0x10;
                i += d;
            }
        }

        if (c > 0) {
            throw makeException(R.TooFewHex);
        }

        return (char) i;
    }

    static int octalDigit(char ch) {
        int d;

        d = ch - '0';
        if (0 <= d && d <= 7) // TODO: uint
            return d;

        return -1;
    }

    static int decimalDigit(char ch) {
        int d;

        d = ch - '0';
        if (0 <= d && d <= 9) // TODO: uint
            return d;

        return -1;
    }

    /*
     * Returns n <= 0xF for a hex digit.
     */
    static int hexDigit(char ch) {
        int d;

        d = ch - '0';
        if (0 <= d && d <= 9) // TODO: uint
            return d;

        d = ch - 'a';
        if (0 <= d && d <= 5) // TODO: uint
            return d + 0xa;

        d = ch - 'A';
        if (0 <= d && d <= 5) // TODO: uint
            return d + 0xa;

        return -1;
    }

    /*
     * Grabs and converts an ascii control character
     */
    char scanControl() {
        char ch;

        if (charsRight() <= 0) {
            throw makeException(R.MissingControl);
        }
        ch = moveRightGetChar();

        // \ca interpreted as \cA
        if (ch >= 'a' && ch <= 'z') {
            ch = (char) (ch - ('a' - 'A'));
        }

        if ((ch = (char) (ch - '@')) < ' ') {
            return ch;
        }

        throw makeException(R.UnrecognizedControl);
    }

    /*
     * Returns true for options allowed only at the top level
     */
    boolean IsOnlyTopOption(int option) {
        return (option == RegexOptions.RightToLeft
                //#if !SILVERLIGHT TODO:
                // || option == RegexOptions.Compiled
                //#endif
                || option == RegexOptions.CultureInvariant
                || option == RegexOptions.ECMAScript
        );
    }

    /*
     * Scans cimsx-cimsx option string, stops at the first unrecognized char.
     */
    void scanOptions() {
        char ch;
        boolean off;
        int option;

        for (off = false; charsRight() > 0; moveRight()) {
            ch = rightChar();

            if (ch == '-') {
                off = true;
            } else if (ch == '+') {
                off = false;
            } else {
                option = optionFromCode(ch);
                if (option == 0 || IsOnlyTopOption(option)) {
                    return;
                }
                if (off) {
                    _options &= ~option;
                } else {
                    _options |= option;
                }
            }
        }
    }

    /*
     * Scans \ code for escape codes that map to single unicode chars.
     */
    char scanCharEscape() {
        char ch;

        ch = moveRightGetChar();

        if (ch >= '0' && ch <= '7') {
            moveLeft();
            return scanOctal();
        }

        switch (ch) {
            case 'x':
                return scanHex(2);
            case 'u':
                return scanHex(4);
            case 'a':
                return '\u0007';
            case 'b':
                return '\b';
            case 'e':
                return '\u001B';
            case 'f':
                return '\f';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'v':
                return '\u000B';
            case 'c':
                return scanControl();
            default:
                if (!useOptionE() && RegexCharClass.isWordChar(ch)) {
                    throw makeException(String.format(R.UnrecognizedEscape, ch));
                }
                return ch;
        }
    }

    /*
     * Scans X for \p{X} or \P{X}
     */
    String parseProperty() {
        if (charsRight() < 3) {
            throw makeException(R.IncompleteSlashP);
        }
        char ch = moveRightGetChar();
        if (ch != '{') {
            throw makeException(R.MalformedSlashP);
        }

        int startpos = textpos();
        while (charsRight() > 0) {
            ch = moveRightGetChar();
            if (!(RegexCharClass.isWordChar(ch) || ch == '-')) {
                moveLeft();
                break;
            }
        }
        // TODO: _pattern.substring(startpos, Textpos() - startpos);
        String capname = _pattern.substring(startpos, textpos());

        if (charsRight() == 0 || moveRightGetChar() != '}')
            throw makeException(R.IncompleteSlashP);

        return capname;
    }

    /*
     * Returns ReNode type for zero-length assertions with a \ code.
     */
    int typeFromCode(char ch) {
        switch (ch) {
            case 'b':
                return useOptionE() ? RegexNode.ECMABoundary : RegexNode.Boundary;
            case 'B':
                return useOptionE() ? RegexNode.NonECMABoundary : RegexNode.Nonboundary;
            case 'A':
                return RegexNode.Beginning;
            case 'G':
                return RegexNode.Start;
            case 'Z':
                return RegexNode.EndZ;
            case 'z':
                return RegexNode.End;
            default:
                return RegexNode.Nothing;
        }
    }

    /*
     * Returns option bit from single-char (?cimsx) code.
     */
    static int optionFromCode(char ch) {
        // case-insensitive
        if (ch >= 'A' && ch <= 'Z') {
            ch += (char) ('a' - 'A');
        }

        switch (ch) {
            case 'i':
                return RegexOptions.IgnoreCase;
            case 'r':
                return RegexOptions.RightToLeft;
            case 'm':
                return RegexOptions.Multiline;
            case 'n':
                return RegexOptions.ExplicitCapture;
            case 's':
                return RegexOptions.Singleline;
            case 'x':
                return RegexOptions.IgnorePatternWhitespace;
            case 'd':
                return RegexOptions.Debug;
            case 'e':
                return RegexOptions.ECMAScript;
            default:
                return 0;
        }
    }

    /*
     * a prescanner for deducing the slots used for
     * captures by doing a partial tokenization of the pattern.
     */
    void countCaptures() {
        char ch;

        noteCaptureSlot(0, 0);

        _autocap = 1;

        while (charsRight() > 0) {
            int pos = textpos();
            ch = moveRightGetChar();
            switch (ch) {
                case '\\':
                    if (charsRight() > 0) {
                        moveRight();
                    }
                    break;

                case '#':
                    if (useOptionX()) {
                        moveLeft();
                        scanBlank();
                    }
                    break;

                case '[':
                    scanCharClass(false /* caseInsensitive */, true /* scanOnly */);
                    break;

                case ')':
                    if (!emptyOptionsStack()) {
                        popOptions();
                    }
                    break;

                case '(':
                    if (charsRight() >= 2 && rightChar(1) == '#' && rightChar() == '?') {
                        moveLeft();
                        scanBlank();
                    } else {
                        pushOptions();
                        if (charsRight() > 0 && rightChar() == '?') {
                            // we have (?...
                            moveRight();

                            if (charsRight() > 1 && (rightChar() == '<' || rightChar() == '\'')) {
                                // named group: (?<... or (?'...

                                moveRight();
                                ch = rightChar();

                                if (ch != '0' && RegexCharClass.isWordChar(ch)) {
                                    // TODO: this is same as .NET Corefx
                                    // if (_ignoreNextParen) throw makeException(R.AlternationCantCapture);
                                    if (ch >= '1' && ch <= '9') {
                                        noteCaptureSlot(scanDecimal(), pos);
                                    } else {
                                        noteCaptureName(scanCapname(), pos);
                                    }
                                }
                            } else {
                                // (?...

                                // get the options if it's an option static finalruct (?cimsx-cimsx...)
                                scanOptions();

                                if (charsRight() > 0) {
                                    if (rightChar() == ')') {
                                        // (?cimsx-cimsx)
                                        moveRight();
                                        popKeepOptions();
                                    } else if (rightChar() == '(') {
                                        // alternation static finalruct: (?(foo)yes|no)
                                        // ignore the next paren so we don't capture the condition
                                        _ignoreNextParen = true;

                                        // break from here so we don't reset _ignoreNextParen
                                        break;
                                    }
                                }
                            }
                        } else {
                            if (!useOptionN() && !_ignoreNextParen)
                                noteCaptureSlot(_autocap++, pos);
                        }
                    }

                    _ignoreNextParen = false;
                    break;
            }
        }

        assignNameSlots();
    }

    /*
     * Notes a used capture slot
     */
    void noteCaptureSlot(int i, int pos) {
        if (!_caps.containsKey(i)) {
            // the rhs of the hashtable isn't used in the parser

            _caps.put(i, pos);
            _capcount++;

            if (_captop <= i) {
                if (i == Integer.MAX_VALUE) // TODO: int32 -> int
                    _captop = i;
                else
                    _captop = i + 1;
            }
        }
    }

    /*
     * Notes a used capture slot
     */
    void noteCaptureName(String name, int pos) {
        if (_capnames == null) {
            _capnames = new HashMap<String, Integer>();
            _capnamelist = new ArrayList<String>();
        }

        if (!_capnames.containsKey(name)) {
            _capnames.put(name, pos);
            _capnamelist.add(name);
        }
    }

    /*
     * For when all the used captures are known: note them all at once
     */
    void noteCaptures(Map<Integer, Integer> caps, int capsize, Map<String, Integer> capnames) {
        _caps = caps;
        _capsize = capsize;
        _capnames = capnames;
    }

    /*
     * Assigns unused slot numbers to the capture names
     */
    void assignNameSlots() {
        if (_capnames != null) {
            for (int i = 0; i < _capnamelist.size(); i++) {
                while (isCaptureSlot(_autocap)) {
                    _autocap++;
                }
                String name = _capnamelist.get(i);
                int pos = (int) _capnames.get(name);
                _capnames.put(name, _autocap);
                noteCaptureSlot(_autocap, pos);

                _autocap++;
            }
        }

        // if the caps array has at least one gap, static finalruct the list of used slots
        if (_capcount < _captop) {
            _capnumlist = new int[_capcount];
            int i = 0;

            // TODO: for (IDictionaryEnumerator de = _caps.GetEnumerator(); de.MoveNext(); )
            //     _capnumlist[i++] = (int) de.Key;
            for (Integer key : _caps.keySet()) {
                _capnumlist[i++] = key;
            }

            // TODO: System.Array.Sort(_capnumlist, Comparer < Int32 >.Default);
            Arrays.sort(_capnumlist);
        }

        // merge capsnumlist into capnamelist
        if (_capnames != null || _capnumlist != null) {
            List<String> oldcapnamelist;
            int next;
            int k = 0;

            if (_capnames == null) {
                oldcapnamelist = null;
                _capnames = new HashMap<String, Integer>();
                _capnamelist = new ArrayList<String>();
                next = -1;
            } else {
                oldcapnamelist = _capnamelist;
                _capnamelist = new ArrayList<String>();
                next = _capnames.get(oldcapnamelist.get(0));
            }

            for (int i = 0; i < _capcount; i++) {
                int j = (_capnumlist == null) ? i : _capnumlist[i];

                if (next == j) {
                    _capnamelist.add(oldcapnamelist.get(k++));
                    next = (k == oldcapnamelist.size()) ? -1 : _capnames.get(oldcapnamelist.get(k));
                } else {
                    // TODO: Convert.ToString(j, _culture);
                    String str = Integer.toString(j);
                    _capnamelist.add(str);
                    _capnames.put(str, j);
                }
            }
        }
    }

    /*
     * Looks up the slot number for a given name
     */
    int captureSlotFromName(String capname) {
        return _capnames.get(capname);
    }

    /*
     * True if the capture slot was noted
     */
    boolean isCaptureSlot(int i) {
        if (_caps != null)
            return _caps.containsKey(i);

        return (i >= 0 && i < _capsize);
    }

    /*
     * Looks up the slot number for a given name
     */
    boolean isCaptureName(String capname) {
        if (_capnames == null)
            return false;

        return _capnames.containsKey(capname);
    }

    /*
     * True if N option disabling '(' autocapture is on.
     */
    boolean useOptionN() {
        return (_options & RegexOptions.ExplicitCapture) != 0;
    }

    /*
     * True if I option enabling case-insensitivity is on.
     */
    boolean useOptionI() {
        return (_options & RegexOptions.IgnoreCase) != 0;
    }

    /*
     * True if M option altering meaning of $ and ^ is on.
     */
    boolean useOptionM() {
        return (_options & RegexOptions.Multiline) != 0;
    }

    /*
     * True if S option altering meaning of . is on.
     */
    boolean useOptionS() {
        return (_options & RegexOptions.Singleline) != 0;
    }

    /*
     * True if X option enabling whitespace/comment mode is on.
     */
    boolean useOptionX() {
        return (_options & RegexOptions.IgnorePatternWhitespace) != 0;
    }

    /*
     * True if E option enabling ECMAScript behavior is on.
     */
    boolean useOptionE() {
        return (_options & RegexOptions.ECMAScript) != 0;
    }

    static final byte Q = 5;    // quantifier
    static final byte S = 4;    // ordinary stopper
    static final byte Z = 3;    // scanBlank stopper
    static final byte X = 2;    // whitespace
    static final byte E = 1;    // should be escaped

    /*
     * For categorizing ascii characters.
    */
    static final byte[] _category = new byte[]{
         // 0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F
            0, 0, 0, 0, 0, 0, 0, 0, 0, X, X, 0, X, X, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         //    !  "  #  $  %  &  '  (  )  *  +  ,  -  .  /  0  1  2  3  4  5  6  7  8  9  :  ;  <  =  >  ?
            X, 0, 0, Z, S, 0, 0, 0, S, S, Q, Q, 0, 0, S, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Q,
         // @  A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  S  T  U  V  W  X  Y  Z  [  \  ]  ^  _
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, S, S, 0, S, 0,
         // '  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q  r  s  t  u  v  w  x  y  z  {  |  }  ~
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Q, S, 0, 0, 0};

    /*
     * Returns true for those characters that terminate a string of ordinary chars.
     */
    static boolean isSpecial(char ch) {
        return ch <= '|' && _category[ch] >= S;
    }

    /*
     * Returns true for those characters that terminate a string of ordinary chars.
     */
    static boolean isStopperX(char ch) {
        return ch <= '|' && _category[ch] >= X;
    }

    /*
     * Returns true for those characters that begin a quantifier.
     */
    static boolean isQuantifier(char ch) {
        return ch <= '{' && _category[ch] >= Q;
    }

    boolean isTrueQuantifier() {
        int nChars = charsRight();
        if (nChars == 0) {
            return false;
        }

        int startpos = textpos();
        char ch = charAt(startpos);
        if (ch != '{') {
            return ch <= '{' && _category[ch] >= Q;
        }

        int pos = startpos;
        while (--nChars > 0 && (ch = charAt(++pos)) >= '0' && ch <= '9') ;

        if (nChars == 0 || pos - startpos == 1) {
            return false;
        }
        if (ch == '}') {
            return true;
        }
        if (ch != ',') {
            return false;
        }

        while (--nChars > 0 && (ch = charAt(++pos)) >= '0' && ch <= '9') ;

        return nChars > 0 && ch == '}';
    }

    /*
     * Returns true for whitespace.
     */
    static boolean isSpace(char ch) {
        return ch <= ' ' && _category[ch] == X;
    }

    /*
     * Returns true for chars that should be escaped.
     */
    static boolean isMetachar(char ch) {
        return ch <= '|' && _category[ch] >= E;
    }

    /*
     * Add a string to the last concatenate.
     */
    void addConcatenate(int pos, int cch, boolean isReplacement) {
        RegexNode node;

        if (cch == 0) {
            return;
        }

        if (cch > 1) {
            // TODO: _pattern.substring(pos, cch);
            String str = _pattern.substring(pos, pos + cch);

            if (useOptionI() && !isReplacement) {
                // We do the toLowerCase character by character for consistency.
                // With surrogate chars, doing a toLowerCase on the entire string could actually
                // change the surrogate pair. This is more correct linguistically, but since Regex
                // doesn't support surrogates, it's more important to be consistent.
                StringBuilder sb = new StringBuilder(str.length());
                for (int i = 0; i < str.length(); i++) {
                    sb.append(Character.toLowerCase(str.charAt(i) /*, _culture */));
                }
                str = sb.toString();
            }

            node = new RegexNode(RegexNode.Multi, _options, str);
        } else {
            char ch = _pattern.charAt(pos);

            if (useOptionI() && !isReplacement) {
                ch = Character.toLowerCase(ch /* , _culture */);
            }

            node = new RegexNode(RegexNode.One, _options, ch);
        }

        _concatenation.addChild(node);
    }

    /*
     * Push the parser state (in response to an open paren)
     */
    void pushGroup() {
        _group._next = _stack;
        _alternation._next = _group;
        _concatenation._next = _alternation;
        _stack = _concatenation;
    }

    /*
     * Remember the pushed state (in response to a ')')
     */
    void popGroup() {
        _concatenation = _stack;
        _alternation = _concatenation._next;
        _group = _alternation._next;
        _stack = _group._next;

        // The first () inside a Testgroup group goes directly to the group
        if (_group.type() == RegexNode.Testgroup && _group.childCount() == 0) {
            if (_unit == null)
                throw makeException(R.IllegalCondition);

            _group.addChild(_unit);
            _unit = null;
        }
    }

    /*
     * True if the group stack is empty.
     */
    boolean emptyStack() {
        return _stack == null;
    }

    /*
     * Start a new round for the parser state (in response to an open paren or string start)
     */
    void startGroup(RegexNode openGroup) {
        _group = openGroup;
        _alternation = new RegexNode(RegexNode.Alternate, _options);
        _concatenation = new RegexNode(RegexNode.Concatenate, _options);
    }

    /*
     * Finish the current concatenation (in response to a |)
     */
    void addAlternate() {
        // The | parts inside a Testgroup group go directly to the group
        if (_group.type() == RegexNode.Testgroup || _group.type() == RegexNode.Testref) {
            _group.addChild(_concatenation.reverseLeft());
        } else {
            _alternation.addChild(_concatenation.reverseLeft());
        }

        _concatenation = new RegexNode(RegexNode.Concatenate, _options);
    }

    /*
     * Finish the current quantifiable (when a quantifier is not found or is not possible)
     */
    void addConcatenate() {
        // The first (| inside a Testgroup group goes directly to the group
        _concatenation.addChild(_unit);
        _unit = null;
    }

    /*
     * Finish the current quantifiable (when a quantifier is found)
     */
    void addConcatenate(boolean lazy, int min, int max) {
        _concatenation.addChild(_unit.makeQuantifier(lazy, min, max));
        _unit = null;
    }

    /*
     * Returns the current unit
     */
    RegexNode unit() {
        return _unit;
    }

    /*
     * Sets the current unit to a single char node
     */
    void addUnitOne(char ch) {
        if (useOptionI())
            ch = Character.toLowerCase(ch /*, _culture */);

        _unit = new RegexNode(RegexNode.One, _options, ch);
    }

    /*
     * Sets the current unit to a single inverse-char node
     */
    void AddUnitNotone(char ch) {
        if (useOptionI())
            ch = Character.toLowerCase(ch /* , _culture */); //TODO:

        _unit = new RegexNode(RegexNode.Notone, _options, ch);
    }

    /*
     * Sets the current unit to a single set node
     */
    void addUnitSet(String cc) {
        _unit = new RegexNode(RegexNode.Set, _options, cc);
    }

    /*
     * Sets the current unit to a subtree
     */
    void addUnitNode(RegexNode node) {
        _unit = node;
    }

    /*
     * Sets the current unit to an assertion of the specified type
     */
    void addUnitType(int type) {
        _unit = new RegexNode(type, _options);
    }

    /*
     * Finish the current group (in response to a ')' or end)
     */
    void addGroup() {
        if (_group.type() == RegexNode.Testgroup || _group.type() == RegexNode.Testref) {
            _group.addChild(_concatenation.reverseLeft());

            if (_group.type() == RegexNode.Testref && _group.childCount() > 2 || _group.childCount() > 3)
                throw makeException(R.TooManyAlternates);
        } else {
            _alternation.addChild(_concatenation.reverseLeft());
            _group.addChild(_alternation);
        }

        _unit = _group;
    }

    /*
     * Saves options on a stack.
     */
    void pushOptions() {
        _optionsStack.add(_options);
    }

    /*
     * Recalls options from the stack.
     */
    void popOptions() {
        _options = _optionsStack.get(_optionsStack.size() - 1);
        _optionsStack.remove(_optionsStack.size() - 1);
    }

    /*
     * True if options stack is empty.
     */
    boolean emptyOptionsStack() {
        return (_optionsStack.size() == 0);
    }

    /*
     * Pops the option stack, but keeps the current options unchanged.
     */
    void popKeepOptions() {
        _optionsStack.remove(_optionsStack.size() - 1);
    }

    /*
     * Fills in an ArgumentException
     */
    IllegalArgumentException makeException(String message) {
        return new IllegalArgumentException(String.format(R.MakeException, _pattern, message));
    }

    /*
     * Returns the current parsing position.
     */
    int textpos() {
        return _currentPos;
    }

    /*
     * Zaps to a specific parsing position.
     */
    void textto(int pos) {
        _currentPos = pos;
    }

    /*
     * Returns the char at the right of the current parsing position and advances to the right.
     */
    char moveRightGetChar() {
        return _pattern.charAt(_currentPos++);
    }

    /*
     * Moves the current position to the right.
     */
    void moveRight() {
        moveRight(1);
    }

    void moveRight(int i) {
        _currentPos += i;
    }

    /*
     * Moves the current parsing position one to the left.
     */
    void moveLeft() {
        --_currentPos;
    }

    /*
     * Returns the char left of the current parsing position.
     */
    char charAt(int i) {
        return _pattern.charAt(i);
    }

    /*
     * Returns the char right of the current parsing position.
     */
    char rightChar() {
        return _pattern.charAt(_currentPos);
    }

    /*
     * Returns the char i chars right of the current parsing position.
     */
    char rightChar(int i) {
        return _pattern.charAt(_currentPos + i);
    }

    /*
     * Number of characters to the right of the current parsing position.
     */
    int charsRight() {
        return _pattern.length() - _currentPos;
    }
}