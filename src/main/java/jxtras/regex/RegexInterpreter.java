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

import java.util.Locale;

// This RegexInterpreter class is internal to the RegularExpression package.
// It executes a block of regular expression codes while consuming
// input.
// @author  Tony Guo <tony.guo.peng@gmail.com>
final class RegexInterpreter extends RegexRunner {
    int runoperator;
    int[] runcodes;
    int runcodepos;
    String[] runstrings;
    RegexCode runcode;
    RegexPrefix runfcPrefix;
    RegexBoyerMoore runbmPrefix;
    int runanchors;
    boolean runrtl;
    boolean runci;
    Locale runculture;

    RegexInterpreter(RegexCode code , Locale culture) {
        runcode = code;
        runcodes = code._codes;
        runstrings = code._strings;
        runfcPrefix = code._fcPrefix;
        runbmPrefix = code._bmPrefix;
        runanchors = code._anchors;
        runculture = culture;
    }

    @Override
    protected void initTrackCount() {
        runtrackcount = runcode._trackcount;
    }

    private void advance() {
        advance(0);
    }

    private void advance(int i) {
        runcodepos += (i + 1);
        setOperator(runcodes[runcodepos]);
    }

    private void goTo(int newpos) {
        // when branching backward, ensure storage
        if (newpos < runcodepos) {
            ensureStorage();
        }

        setOperator(runcodes[newpos]);
        runcodepos = newpos;
    }

    private void textto(int newpos) {
        runtextpos = newpos;
    }

    private void trackto(int newpos) {
        runtrackpos = runtrack.length - newpos;
    }

    private int textstart() {
        return runtextstart;
    }

    private int textpos() {
        return runtextpos;
    }

    // push onto the backtracking stack
    private int trackpos() {
        return runtrack.length - runtrackpos;
    }

    private void trackPush() {
        runtrack[--runtrackpos] = runcodepos;
    }

    private void trackPush(int I1) {
        runtrack[--runtrackpos] = I1;
        runtrack[--runtrackpos] = runcodepos;
    }

    private void trackPush(int I1, int I2) {
        runtrack[--runtrackpos] = I1;
        runtrack[--runtrackpos] = I2;
        runtrack[--runtrackpos] = runcodepos;
    }

    private void trackPush(int I1, int I2, int I3) {
        runtrack[--runtrackpos] = I1;
        runtrack[--runtrackpos] = I2;
        runtrack[--runtrackpos] = I3;
        runtrack[--runtrackpos] = runcodepos;
    }

    private void trackPush2(int I1) {
        runtrack[--runtrackpos] = I1;
        runtrack[--runtrackpos] = -runcodepos;
    }

    private void trackPush2(int I1, int I2) {
        runtrack[--runtrackpos] = I1;
        runtrack[--runtrackpos] = I2;
        runtrack[--runtrackpos] = -runcodepos;
    }

    private void backtrack() {
        int newpos = runtrack[runtrackpos++];

        if (runmatch.isDebugEnabled()) {
            if (newpos < 0) {
                System.out.println("       Backtracking (back2) to code position " + (-newpos));
            } else {
                System.out.println("       Backtracking to code position " + newpos);
            }
        }

        if (newpos < 0) {
            newpos = -newpos;
            setOperator(runcodes[newpos] | RegexCode.Back2);
        } else {
            setOperator(runcodes[newpos] | RegexCode.Back);
        }

        // When branching backward, ensure storage
        if (newpos < runcodepos) {
            ensureStorage();
        }

        runcodepos = newpos;
    }

    private void setOperator(int op) {
        runci = (0 != (op & RegexCode.Ci));
        runrtl = (0 != (op & RegexCode.Rtl));
        runoperator = op & ~(RegexCode.Rtl | RegexCode.Ci);
    }

    private void trackPop() {
        runtrackpos++;
    }

    // pop framesize items from the backtracking stack
    private void trackPop(int framesize) {
        runtrackpos += framesize;
    }

    // Technically we are actually peeking at items already popped.  So if you want to
    // get and pop the top item from the stack, you do
    // TrackPop();
    // TrackPeek();
    private int trackPeek() {
        return runtrack[runtrackpos - 1];
    }

    // get the ith element down on the backtracking stack
    private int trackPeek(int i) {
        return runtrack[runtrackpos - i - 1];
    }

    // Push onto the grouping stack
    private void stackPush(int I1) {
        runstack[--runstackpos] = I1;
    }

    private void stackPush(int I1, int I2) {
        runstack[--runstackpos] = I1;
        runstack[--runstackpos] = I2;
    }

    private void stackPop() {
        runstackpos++;
    }

    // pop framesize items from the grouping stack
    private void stackPop(int framesize) {
        runstackpos += framesize;
    }

    // Technically we are actually peeking at items already popped.  So if you want to
    // get and pop the top item from the stack, you do
    // StackPop();
    // StackPeek();
    private int stackPeek() {
        return runstack[runstackpos - 1];
    }

    // get the ith element down on the grouping stack
    private int stackPeek(int i) {
        return runstack[runstackpos - i - 1];
    }

    private int operator() {
        return runoperator;
    }

    private int operand(int i) {
        return runcodes[runcodepos + i + 1];
    }

    private int leftchars() {
        return runtextpos - runtextbeg;
    }

    private int rightchars() {
        return runtextend - runtextpos;
    }

    private int bump() {
        return runrtl ? -1 : 1;
    }

    private int forwardchars() {
        return runrtl ? runtextpos - runtextbeg : runtextend - runtextpos;
    }

    private char forwardcharnext() {
        char ch = (runrtl ? runtext.charAt(--runtextpos) : runtext.charAt(runtextpos++)); // TODO: using java charAt(i)

        return (runci ? Character.toLowerCase(ch /* , runculture */) : ch); // TODO: java does not support cultureInfo
    }

    private boolean stringmatch(String str) {
        int c;
        int pos;

        if (!runrtl) {
            if (runtextend - runtextpos < (c = str.length())) {
                return false;
            }
            pos = runtextpos + c;
        } else {
            if (runtextpos - runtextbeg < (c = str.length())) {
                return false;
            }
            pos = runtextpos;
        }

        if (!runci) {
            while (c != 0)
                // TODO: using java charAt(i)
                if (str.charAt(--c) != runtext.charAt(--pos))
                    return false;
        } else {
            while (c != 0)
                // TODO: java port does not support cultureInfo
                if (str.charAt(--c) != Character.toLowerCase(runtext.charAt(--pos))) //, runculture))
                    return false;
        }

        if (!runrtl) {
            pos += str.length();
        }

        runtextpos = pos;

        return true;
    }

    private boolean refmatch(int index, int len) {
        int c;
        int pos;
        int cmpos;

        if (!runrtl) {
            if (runtextend - runtextpos < len) {
                return false;
            }
            pos = runtextpos + len;
        } else {
            if (runtextpos - runtextbeg < len) {
                return false;
            }
            pos = runtextpos;
        }
        cmpos = index + len;

        c = len;

        if (!runci) {
            while (c-- != 0) {
                if (runtext.charAt(--cmpos) != runtext.charAt(--pos)) {
                    return false;
                }
            }
        } else {
            while (c-- != 0) {
                // TODO: java does not support cultureInfo
                if (Character.toLowerCase(runtext.charAt(--cmpos))
                        != Character.toLowerCase(runtext.charAt(--pos))) {
                    return false;
                }
            }
        }

        if (!runrtl) {
            pos += len;
        }

        runtextpos = pos;

        return true;
    }

    private void backwardnext() {
        runtextpos += runrtl ? 1 : -1;
    }

    private char charAt(int j) {
        return runtext.charAt(j);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!! This function must be kept synchronized with GenerateFindFirstChar !!!!
    // !!!! in RegexCompiler.cs                                                !!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @Override
    protected boolean findFirstChar() {
        int i;
        String set;

        if (0 != (runanchors & (RegexFCD.Beginning | RegexFCD.Start | RegexFCD.EndZ | RegexFCD.End))) {
            if (!runcode._rightToLeft) {
                if ((0 != (runanchors & RegexFCD.Beginning) && runtextpos > runtextbeg) ||
                        (0 != (runanchors & RegexFCD.Start) && runtextpos > runtextstart)) {
                    runtextpos = runtextend;
                    return false;
                }
                if (0 != (runanchors & RegexFCD.EndZ) && runtextpos < runtextend - 1) {
                    runtextpos = runtextend - 1;
                } else if (0 != (runanchors & RegexFCD.End) && runtextpos < runtextend) {
                    runtextpos = runtextend;
                }
            } else {
                if ((0 != (runanchors & RegexFCD.End) && runtextpos < runtextend) ||
                        (0 != (runanchors & RegexFCD.EndZ) && (runtextpos < runtextend - 1 ||
                                (runtextpos == runtextend - 1 && charAt(runtextpos) != '\n'))) ||
                        (0 != (runanchors & RegexFCD.Start) && runtextpos < runtextstart)) {
                    runtextpos = runtextbeg;
                    return false;
                }
                if (0 != (runanchors & RegexFCD.Beginning) && runtextpos > runtextbeg) {
                    runtextpos = runtextbeg;
                }
            }

            if (runbmPrefix != null) {
                return runbmPrefix.isMatch(runtext, runtextpos, runtextbeg, runtextend);
            }

            return true; // found a valid start or end anchor
        } else if (runbmPrefix != null) {
            runtextpos = runbmPrefix.scan(runtext, runtextpos, runtextbeg, runtextend);

            if (runtextpos == -1) {
                runtextpos = (runcode._rightToLeft ? runtextbeg : runtextend);
                return false;
            }

            return true;
        } else if (runfcPrefix == null) {
            return true;
        }

        runrtl = runcode._rightToLeft;
        runci = runfcPrefix.isCaseInsensitive();
        set = runfcPrefix.prefix();

        if (RegexCharClass.isSingleton(set)) {
            char ch = RegexCharClass.singletonChar(set);

            for (i = forwardchars(); i > 0; i--) {
                if (ch == forwardcharnext()) {
                    backwardnext();
                    return true;
                }
            }
        } else {
            for (i = forwardchars(); i > 0; i--) {
                if (RegexCharClass.charInClass(forwardcharnext(), set)) {
                    backwardnext();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void go() {
        goTo(0);

BACKWARD:
        for (; ;) {
            //#if DBG
            if (runmatch.isDebugEnabled()) {
                dumpState();
            }
            //#endif

            checkTimeout();

            switch (operator()) {
                case RegexCode.Stop: {
                    return;
                }

                case RegexCode.Nothing: {
                    break;
                }

                case RegexCode.Goto: {
                    goTo(operand(0));
                    continue;
                }

                case RegexCode.Testref: {
                    if (!isMatched(operand(0))) {
                        break;
                    }
                    advance(1);
                    continue;
                }

                case RegexCode.Lazybranch: {
                    trackPush(textpos());
                    advance(1);
                    continue;
                }

                case RegexCode.Lazybranch | RegexCode.Back: {
                    trackPop();
                    textto(trackPeek());
                    goTo(operand(0));
                    continue;
                }

                case RegexCode.Setmark: {
                    stackPush(textpos());
                    trackPush();
                    advance();
                    continue;
                }

                case RegexCode.Nullmark: {
                    stackPush(-1);
                    trackPush();
                    advance();
                    continue;
                }

                case RegexCode.Setmark | RegexCode.Back:
                case RegexCode.Nullmark | RegexCode.Back: {
                        stackPop();
                        break;
                }

                case RegexCode.Getmark: {
                    stackPop();
                    trackPush(stackPeek());
                    textto(stackPeek());
                    advance();
                    continue;
                }

                case RegexCode.Getmark | RegexCode.Back: {
                    trackPop();
                    stackPush(trackPeek());
                    break;
                }

                case RegexCode.Capturemark: {
                    if (operand(1) != -1 && !isMatched(operand(1))) {
                        break;
                    }
                    stackPop();
                    if (operand(1) != -1) {
                        transferCapture(operand(0), operand(1), stackPeek(), textpos());
                    } else {
                        capture(operand(0), stackPeek(), textpos());
                    }
                    trackPush(stackPeek());
                    advance(2);
                    continue;
                }

                case RegexCode.Capturemark | RegexCode.Back: {
                    trackPop();
                    stackPush(trackPeek());
                    uncapture();
                    if (operand(0) != -1 && operand(1) != -1) {
                        uncapture();
                    }
                    break;
                }

                case RegexCode.Branchmark: {
                    int matched;
                    stackPop();

                    matched = textpos() - stackPeek();

                    if (matched != 0) {                     // Nonempty match -> loop now
                        trackPush(stackPeek(), textpos());  // Save old mark, textpos
                        stackPush(textpos());               // Make new mark
                        goTo(operand(0));                   // Loop
                    } else {                                // Empty match -> straight now
                        trackPush2(stackPeek());            // Save old mark
                        advance(1);                         // Straight
                    }
                    continue;
                }

                case RegexCode.Branchmark | RegexCode.Back: {
                    trackPop(2);
                    stackPop();
                    textto(trackPeek(1));                       // Recall position
                    trackPush2(trackPeek());                    // Save old mark
                    advance(1);                                 // Straight
                    continue;
                }

                case RegexCode.Branchmark | RegexCode.Back2: {
                    trackPop();
                    stackPush(trackPeek());                     // Recall old mark
                    break;                                      // Backtrack
                }

                case RegexCode.Lazybranchmark: {
                    // We hit this the first time through a lazy loop and after each
                    // successful match of the inner expression.  It simply continues
                    // on and doesn't loop.
                    stackPop();

                    int oldMarkPos = stackPeek();

                    if (textpos() != oldMarkPos) {              // Nonempty match -> try to loop again by going to 'back' state
                        if (oldMarkPos != -1) {
                            trackPush(oldMarkPos, textpos());   // Save old mark, textpos
                        } else {
                            trackPush(textpos(), textpos());
                        }
                    } else {
                        // The inner expression found an empty match, so we'll go directly to 'back2' if we
                        // backtrack.  In this case, we need to push something on the stack, since back2 pops.
                        // However, in the case of ()+? or similar, this empty match may be legitimate, so push the text
                        // position associated with that empty match.
                        stackPush(oldMarkPos);

                        trackPush2(stackPeek());                // Save old mark
                    }
                    advance(1);
                    continue;
                }

                case RegexCode.Lazybranchmark | RegexCode.Back: {
                    // After the first time, Lazybranchmark | RegexCode.Back occurs
                    // with each iteration of the loop, and therefore with every attempted
                    // match of the inner expression.  We'll try to match the inner expression,
                    // then go back to Lazybranchmark if successful.  If the inner expression
                    // failes, we go to Lazybranchmark | RegexCode.Back2
                    int pos;

                    trackPop(2);
                    pos = trackPeek(1);
                    trackPush2(trackPeek());                // Save old mark
                    stackPush(pos);                         // Make new mark
                    textto(pos);                            // Recall position
                    goTo(operand(0));                       // Loop
                    continue;
                }

                case RegexCode.Lazybranchmark | RegexCode.Back2: {
                    // The lazy loop has failed.  We'll do a true backtrack and
                    // start over before the lazy loop.
                    stackPop();
                    trackPop();
                    stackPush(trackPeek());                      // Recall old mark
                    break;
                }

                case RegexCode.Setcount: {
                    stackPush(textpos(), operand(0));
                    trackPush();
                    advance(1);
                    continue;
                }

                case RegexCode.Nullcount: {
                    stackPush(-1, operand(0));
                    trackPush();
                    advance(1);
                    continue;
                }

                case RegexCode.Setcount | RegexCode.Back: {
                    stackPop(2);
                    break;
                }

                case RegexCode.Nullcount | RegexCode.Back: {
                    stackPop(2);
                    break;
                }

                case RegexCode.Branchcount: {
                    // StackPush:
                    //  0: Mark
                    //  1: Count
                    stackPop(2);
                    int mark = stackPeek();
                    int count = stackPeek(1);
                    int matched = textpos() - mark;

                    if (count >= operand(1) || (matched == 0 && count >= 0)) {                                   // Max loops or empty match -> straight now
                        trackPush2(mark, count);            // Save old mark, count
                        advance(2);                         // Straight
                    } else {                                // Nonempty match -> count+loop now
                        trackPush(mark);                    // remember mark
                        stackPush(textpos(), count + 1);    // Make new mark, incr count
                        goTo(operand(0));                   // Loop
                    }
                    continue;
                }

                case RegexCode.Branchcount | RegexCode.Back: {
                    // TrackPush:
                    //  0: Previous mark
                    // StackPush:
                    //  0: Mark (= current pos, discarded)
                    //  1: Count
                    trackPop();
                    stackPop(2);
                    if (stackPeek(1) > 0) {                         // Positive -> can go straight
                        textto(stackPeek());                        // Zap to mark
                        trackPush2(trackPeek(), stackPeek(1) - 1);  // Save old mark, old count
                        advance(2);                                 // Straight
                        continue;
                    }
                    stackPush(trackPeek(), stackPeek(1) - 1);       // recall old mark, old count
                    break;
                }

                case RegexCode.Branchcount | RegexCode.Back2: {
                    // TrackPush:
                    //  0: Previous mark
                    //  1: Previous count
                    trackPop(2);
                    stackPush(trackPeek(), trackPeek(1));           // Recall old mark, old count
                    break;                                          // Backtrack
                }

                case RegexCode.Lazybranchcount: {
                    // StackPush:
                    //  0: Mark
                    //  1: Count
                    stackPop(2);
                    int mark = stackPeek();
                    int count = stackPeek(1);

                    if (count < 0) {                        // Negative count -> loop now
                        trackPush2(mark);                   // Save old mark
                        stackPush(textpos(), count + 1);    // Make new mark, incr count
                        goTo(operand(0));                   // Loop
                    } else {                                // Nonneg count -> straight now
                        trackPush(mark, count, textpos());  // Save mark, count, position
                        advance(2);                         // Straight
                    }
                    continue;
                }

                case RegexCode.Lazybranchcount | RegexCode.Back: {
                    // TrackPush:
                    //  0: Mark
                    //  1: Count
                    //  2: Textpos
                    trackPop(3);
                    int mark = trackPeek();
                    int textpos = trackPeek(2);

                    if (trackPeek(1) < operand(1) && textpos != mark) { // Under limit and not empty match -> loop
                        textto(textpos);                                // Recall position
                        stackPush(textpos, trackPeek(1) + 1);           // Make new mark, incr count
                        trackPush2(mark);                               // Save old mark
                        goTo(operand(0));                               // Loop
                        continue;
                    } else {                                            // Max loops or empty match -> backtrack
                        stackPush(trackPeek(), trackPeek(1));           // Recall old mark, count
                        break;                                          // backtrack
                    }
                }

                case RegexCode.Lazybranchcount | RegexCode.Back2: {
                    // TrackPush:
                    //  0: Previous mark
                    // StackPush:
                    //  0: Mark (== current pos, discarded)
                    //  1: Count
                    trackPop();
                    stackPop(2);
                    stackPush(trackPeek(), stackPeek(1) - 1);   // Recall old mark, count
                    break;                                      // Backtrack
                }

                case RegexCode.Setjump: {
                    stackPush(trackpos(), crawlpos());
                    trackPush();
                    advance();
                    continue;
                }

                case RegexCode.Setjump | RegexCode.Back: {
                    stackPop(2);
                    break;
                }

                case RegexCode.Backjump: {
                    // StackPush:
                    //  0: Saved trackpos
                    //  1: Crawlpos
                    stackPop(2);
                    trackto(stackPeek());

                    while (crawlpos() != stackPeek(1)) {
                        uncapture();
                    }
                    break;
                }

                case RegexCode.Forejump: {
                    // StackPush:
                    //  0: Saved trackpos
                    //  1: Crawlpos
                    stackPop(2);
                    trackto(stackPeek());
                    trackPush(stackPeek(1));
                    advance();
                    continue;
                }

                case RegexCode.Forejump | RegexCode.Back: {
                    // TrackPush:
                    //  0: Crawlpos
                    trackPop();

                    while (crawlpos() != trackPeek()) {
                        uncapture();
                    }
                    break;
                }

                case RegexCode.Bol: {
                    if (leftchars() > 0 && charAt(textpos() - 1) != '\n') {
                        break;
                    }
                    advance();
                    continue;
                }

                case RegexCode.Eol: {
                    if (rightchars() > 0 && charAt(textpos()) != '\n') {
                        break;
                    }
                    advance();
                    continue;
                }

                case RegexCode.Boundary: {
                    if (!isBoundary(textpos(), runtextbeg, runtextend)) {
                        break;
                    }
                    advance();
                    continue;
                }

                case RegexCode.Nonboundary: {
                    if (isBoundary(textpos(), runtextbeg, runtextend)) {
                        break;
                    }
                    advance();
                    continue;
                }

                case RegexCode.ECMABoundary: {
                    if (!isECMABoundary(textpos(), runtextbeg, runtextend)) {
                        break;
                    }
                    advance();
                    continue;
                }

                case RegexCode.NonECMABoundary: {
                    if (isECMABoundary(textpos(), runtextbeg, runtextend)) {
                        break;
                    }
                    advance();
                    continue;
                }

                case RegexCode.Beginning: {
                    if (leftchars() > 0) {
                        break;
                    }
                    advance();
                    continue;
                }

                case RegexCode.Start: {
                    if (textpos() != textstart()) {
                        break;
                    }
                    advance();
                    continue;
                }

                case RegexCode.EndZ: {
                    if (rightchars() > 1 || rightchars() == 1 && charAt(textpos()) != '\n') {
                        break;
                    }
                    advance();
                    continue;
                }

                case RegexCode.End: {
                    if (rightchars() > 0) {
                        break;
                    }
                    advance();
                    continue;
                }

                case RegexCode.One: {
                    if (forwardchars() < 1 || forwardcharnext() != (char) operand(0)) {
                        break;
                    }

                    advance(1);
                    continue;
                }

                case RegexCode.Notone: {
                    if (forwardchars() < 1 || forwardcharnext() == (char) operand(0)) {
                        break;
                    }

                    advance(1);
                    continue;
                }

                case RegexCode.Set:
                    if (forwardchars() < 1 || !RegexCharClass.charInClass(forwardcharnext(),
                            runstrings[operand(0)]))
                        break;

                    advance(1);
                    continue;

                case RegexCode.Multi: {
                    if (!stringmatch(runstrings[operand(0)])) {
                        break;
                    }

                    advance(1);
                    continue;
                }

                case RegexCode.Ref: {
                    int capnum = operand(0);

                    if (isMatched(capnum)) {
                        if (!refmatch(matchIndex(capnum), matchLength(capnum))) {
                            break;
                        }
                    } else {
                        if ((runregex.options & RegexOptions.ECMAScript) == 0) {
                            break;
                        }
                    }

                    advance(1);
                    continue;
                }

                case RegexCode.Onerep: {
                    int c = operand(1);

                    if (forwardchars() < c) {
                        break;
                    }

                    char ch = (char) operand(0);

                    while (c-- > 0) {
                        if (forwardcharnext() != ch) {
                            // TODO: break BreakBackward;
                            break BACKWARD;
                        }
                    }

                    advance(2);
                    continue;
                }

                case RegexCode.Notonerep: {
                    int c = operand(1);

                    if (forwardchars() < c) {
                        break;
                    }

                    char ch = (char) operand(0);

                    while (c-- > 0) {
                        if (forwardcharnext() == ch) {
                            // TODO: break BreakBackward;
                            break BACKWARD;
                        }
                    }

                    advance(2);
                    continue;
                }

                case RegexCode.Setrep: {
                    int c = operand(1);

                    if (forwardchars() < c) {
                        break;
                    }

                    String set = runstrings[operand(0)];

                    while (c-- > 0) {
                        if (!RegexCharClass.charInClass(forwardcharnext(), set)) {
                            // TODO: break BreakBackward;
                            break BACKWARD;
                        }
                    }

                    advance(2);
                    continue;
                }

                case RegexCode.Oneloop: {
                    int c = operand(1);

                    if (c > forwardchars()) {
                        c = forwardchars();
                    }

                    char ch = (char) operand(0);
                    int i;

                    for (i = c; i > 0; i--) {
                        if (forwardcharnext() != ch) {
                            backwardnext();
                            break;
                        }
                    }

                    if (c > i) {
                        trackPush(c - i - 1, textpos() - bump());
                    }

                    advance(2);
                    continue;
                }

                case RegexCode.Notoneloop: {
                    int c = operand(1);

                    if (c > forwardchars()) {
                        c = forwardchars();
                    }

                    char ch = (char) operand(0);
                    int i;

                    for (i = c; i > 0; i--) {
                        if (forwardcharnext() == ch) {
                            backwardnext();
                            break;
                        }
                    }

                    if (c > i)
                        trackPush(c - i - 1, textpos() - bump());

                    advance(2);
                    continue;
                }

                case RegexCode.Setloop: {
                    int c = operand(1);

                    if (c > forwardchars()) {
                        c = forwardchars();
                    }

                    String set = runstrings[operand(0)];
                    int i;

                    for (i = c; i > 0; i--) {
                        if (!RegexCharClass.charInClass(forwardcharnext(), set)) {
                            backwardnext();
                            break;
                        }
                    }

                    if (c > i) {
                        trackPush(c - i - 1, textpos() - bump());
                    }

                    advance(2);
                    continue;
                }

                case RegexCode.Oneloop | RegexCode.Back:
                case RegexCode.Notoneloop | RegexCode.Back: {
                    trackPop(2);
                    int i = trackPeek();
                    int pos = trackPeek(1);

                    textto(pos);

                    if (i > 0) {
                        trackPush(i - 1, pos - bump());
                    }

                    advance(2);
                    continue;
                }

                case RegexCode.Setloop | RegexCode.Back: {
                    trackPop(2);
                    int i = trackPeek();
                    int pos = trackPeek(1);

                    textto(pos);

                    if (i > 0) {
                        trackPush(i - 1, pos - bump());
                    }

                    advance(2);
                    continue;
                }

                case RegexCode.Onelazy:
                case RegexCode.Notonelazy: {
                    int c = operand(1);

                    if (c > forwardchars()) {
                        c = forwardchars();
                    }

                    if (c > 0) {
                        trackPush(c - 1, textpos());
                    }

                    advance(2);
                    continue;
                }

                case RegexCode.Setlazy: {
                    int c = operand(1);

                    if (c > forwardchars()) {
                        c = forwardchars();
                    }

                    if (c > 0) {
                        trackPush(c - 1, textpos());
                    }

                    advance(2);
                    continue;
                }

                case RegexCode.Onelazy | RegexCode.Back: {
                    trackPop(2);
                    int pos = trackPeek(1);
                    textto(pos);

                    if (forwardcharnext() != (char) operand(0)) {
                        break;
                    }

                    int i = trackPeek();

                    if (i > 0) {
                        trackPush(i - 1, pos + bump());
                    }

                    advance(2);
                    continue;
                }

                case RegexCode.Notonelazy | RegexCode.Back: {
                    trackPop(2);
                    int pos = trackPeek(1);
                    textto(pos);

                    if (forwardcharnext() == (char) operand(0)) {
                        break;
                    }

                    int i = trackPeek();

                    if (i > 0) {
                        trackPush(i - 1, pos + bump());
                    }

                    advance(2);
                    continue;
                }

                case RegexCode.Setlazy | RegexCode.Back: {
                    trackPop(2);
                    int pos = trackPeek(1);
                    textto(pos);

                    if (!RegexCharClass.charInClass(forwardcharnext(), runstrings[operand(0)])) {
                        break;
                    }

                    int i = trackPeek();

                    if (i > 0) {
                        trackPush(i - 1, pos + bump());
                    }

                    advance(2);
                    continue;
                }

                default:
                    throw new IllegalArgumentException(R.UnimplementedState);
            }

            // "break Backward" comes here:
            backtrack();
        }
    }

    @Override
    void dumpState() {
        super.dumpState();
        System.out.println("       " + runcode.opcodeDescription(runcodepos) +
                ((runoperator & RegexCode.Back) != 0 ? " Back" : "") +
                ((runoperator & RegexCode.Back2) != 0 ? " Back2" : ""));
    }
}