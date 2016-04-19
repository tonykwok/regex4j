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
 * This API supports the product infrastructure and is not intended to be used directly from your code.

 */

// TThe RegexRunner class is the base class for compiled regular expressions.

// Implementation notes:
//
// RegexRunner provides a common calling convention and a common
// runtime environment for the interpreter and the compiled code.
//
// It provides the driver code that call's the subclass's Go()
// method for either scanning or direct execution.
//
// It also maintains memory allocation for the backtracking stack,
// the grouping stack and the longjump crawlstack, and provides
// methods to push new subpattern match results into (or remove
// backtracked results from) the Match instance.
// @author  Tony Guo <tony.guo.peng@gmail.com>
public abstract class RegexRunner {
    protected int runtextbeg;          // beginning of text to search
    protected int runtextend;          // end of text to search
    protected int runtextstart;        // starting point for search

    protected String runtext;          // text to search
    protected int runtextpos;          // current position in text

    protected int[] runtrack;         // The backtracking stack.  Opcodes use this to store data regarding
    protected int runtrackpos;         // what they have matched and where to backtrack to.  Each "frame" on
    // the stack takes the form of [CodePosition Data1 Data2...], where
    // CodePosition is the position of the current opcode and
    // the data values are all optional.  The CodePosition can be negative, and
    // these values (also called "back2") are used by the BranchMark family of opcodes
    // to indicate whether they are backtracking after a successful or failed
    // match.
    // When we backtrack, we pop the CodePosition off the stack, set the current
    // instruction pointer to that code position, and mark the opcode
    // with a backtracking flag ("Back").  Each opcode then knows how to
    // handle its own data.

    protected int[] runstack;         // This stack is used to track text positions across different opcodes.
    protected int runstackpos;         // For example, in /(a*b)+/, the parentheses result in a SetMark/CaptureMark
    // pair. SetMark records the text position before we match a*b.  Then
    // CaptureMark uses that position to figure out where the capture starts.
    // Opcodes which push onto this stack are always paired with other opcodes
    // which will pop the value from it later.  A successful match should mean
    // that this stack is empty.

    protected int[] runcrawl;         // The crawl stack is used to keep track of captures.  Every time a group
    protected int runcrawlpos;         // has a capture, we push its group number onto the runcrawl stack.  In
    // the case of a balanced match, we push BOTH groups onto the stack.

    protected int runtrackcount;       // count of states that may do backtracking

    protected Match runmatch;          // result object
    protected Regex runregex;          // regex object

    //TODO: int32
    private int timeout;                      // timeout in millisecs (needed for actual)
    private boolean ignoreTimeout;
    private long timeoutOccursAt;


    // GPaperin: We have determined this value in a series of experiments where x86 retail
    // builds (ono-lab-optimised) were run on different pattern/input pairs. Larger values
    // of TimeoutCheckFrequency did not tend to increase performance; smaller values
    // of TimeoutCheckFrequency tended to slow down the execution.
    private static final int TimeoutCheckFrequency = 1000;
    private int timeoutChecksToSkip;

    protected RegexRunner() {
    }

    /*
     * Scans the string to find the first match. Uses the Match object
     * both to feed text in and as a place to store matches that come out.
     *
     * All the action is in the abstract Go() method defined by subclasses. Our
     * responsibility is to load up the class members (as done here) before
     * calling Go.
     *
     * <
     */
    protected Match scan(Regex regex, String text, int textbeg, int textend, int textstart, int
            prevlen, boolean quick) {
        return scan(regex, text, textbeg, textend, textstart, prevlen, quick, regex.matchTimeout());
    }

    protected Match scan(Regex regex, String text, int textbeg, int textend, int textstart, int
            prevlen, boolean quick, int timeout) {
        int bump;
        int stoppos;
        boolean initted = false;

        // We need to re-validate timeout here because Scan is historically protected and
        // thus there is a possibility it is called from user code:
        Regex.validateMatchTimeout(timeout);

        this.ignoreTimeout = (Regex.INFINITE_MATCH_TIMEOUT == timeout);
        this.timeout = this.ignoreTimeout ? Regex.INFINITE_MATCH_TIMEOUT : (int)(timeout + 0.5); // Round

        runregex = regex;
        runtext = text;
        runtextbeg = textbeg;
        runtextend = textend;
        runtextstart = textstart;

        bump = runregex.rightToLeft() ? -1 : 1;
        stoppos = runregex.rightToLeft() ? runtextbeg : runtextend;

        runtextpos = textstart;

        // If previous match was empty or failed, advance by one before matching

        if (prevlen == 0) {
            if (runtextpos == stoppos)
                return Match.EMPTY;

            runtextpos += bump;
        }

        startTimeoutWatch();

        for (; ;) {

            //#if DBG
            if (runregex.isDebugEnabled()) {
                System.out.println("");
                System.out.println("Search range: from " + runtextbeg + " to " + runtextend);
                System.out.println("Firstchar search starting at " + runtextpos + " stopping at " + stoppos);
            }
            //#endif

            if (findFirstChar()) {

                checkTimeout();

                if (!initted) {
                    initMatch();
                    initted = true;
                }

                //#if DBG
                if (runregex.isDebugEnabled()) {
                    System.out.println("Executing engine starting at " + runtextpos);
                    System.out.println("");
                }
                //#endif

                go();

                if (runmatch.matchCount[0] > 0) {
                    // <
                    return tidyMatch(quick);
                }

                // reset state for another go
                runtrackpos = runtrack.length;
                runstackpos = runstack.length;
                runcrawlpos = runcrawl.length;
            }

            // failure!

            if (runtextpos == stoppos) {
                tidyMatch(true);
                return Match.EMPTY;
            }

            // <

            // Bump by one and start again

            runtextpos += bump;
        }

        // We never get here
    }

    private void startTimeoutWatch() {
        if (ignoreTimeout)
            return;

        timeoutChecksToSkip = TimeoutCheckFrequency;

        // We are using Environment.TickCount and not Timewatch for performance reasons.
        // Environment.TickCount is an int that cycles. We intentionally let timeoutOccursAt
        // overflow it will still stay ahead of Environment.TickCount for comparisons made
        // in DoCheckTimeout():
        timeoutOccursAt = System.currentTimeMillis() /* Environment.TickCount */ + timeout;
    }

    protected void checkTimeout() {

        if (ignoreTimeout)
            return;

        if (--timeoutChecksToSkip != 0)
            return;

        timeoutChecksToSkip = TimeoutCheckFrequency;
        doCheckTimeout();
    }

    private void doCheckTimeout() {

        // Note that both, Environment.TickCount and timeoutOccursAt are ints and can overflow and become negative.
        // See the comment in StartTimeoutWatch().

        long currentMillis = System.currentTimeMillis(); //Environment.TickCount;

        if (currentMillis < timeoutOccursAt)
            return;


        if (0 > timeoutOccursAt && 0 < currentMillis)
            return;

        //#if DBG
        if (runregex.isDebugEnabled()) {
            System.out.println("");
            System.out.println("RegEx match timeout occurred!");
            System.out.println("Specified timeout(ms):   " + timeout);
            System.out.println("Timeout check frequency: " + TimeoutCheckFrequency);
            System.out.println("Search pattern:          " + runregex.pattern);
            System.out.println("Input:                   " + runtext);
            System.out.println("About to throw RegexMatchTimeoutException.");
        }
        //#endif

        throw new RegexMatchTimeoutException(runtext, runregex.pattern, timeout);
    }

    /*
     * The responsibility of Go() is to run the regular expression at
     * runtextpos and call Capture() on all the captured subexpressions,
     * then to leave runtextpos at the ending position. It should leave
     * runtextpos where it started if there was no match.
     */
    protected abstract void go();

    /*
     * The responsibility of FindFirstChar() is to advance runtextpos
     * until it is at the next position which is a candidate for the
     * beginning of a successful match.
     */
    protected abstract boolean findFirstChar();

    /*
     * InitTrackCount must initialize the runtrackcount field; this is
     * used to know how large the initial runtrack and runstack arrays
     * must be.
     */
    protected abstract void initTrackCount();

    /*
     * Initializes all the data members that are used by Go()
     */
    private void initMatch() {
        // Use a hashtable'ed Match object if the capture numbers are sparse

        if (runmatch == null) {
            if (runregex.caps != null)
                runmatch = new Match.MatchSparse(runregex, runregex.caps, runregex.capsize, runtext, runtextbeg, runtextend - runtextbeg, runtextstart);
            else
                runmatch = new Match(runregex, runregex.capsize, runtext, runtextbeg, runtextend - runtextbeg, runtextstart);
        } else {
            runmatch.reset(runregex, runtext, runtextbeg, runtextend, runtextstart);
        }

        // note we test runcrawl, because it is the last one to be allocated
        // If there is an alloc failure in the middle of the three allocations,
        // we may still return to reuse this instance, and we want to behave
        // as if the allocations didn't occur. (we used to test _trackcount != 0)

        if (runcrawl != null) {
            runtrackpos = runtrack.length;
            runstackpos = runstack.length;
            runcrawlpos = runcrawl.length;
            return;
        }

        initTrackCount();

        int tracksize = runtrackcount * 8;
        int stacksize = runtrackcount * 8;

        if (tracksize < 32)
            tracksize = 32;
        if (stacksize < 16)
            stacksize = 16;

        runtrack = new int[tracksize];
        runtrackpos = tracksize;

        runstack = new int[stacksize];
        runstackpos = stacksize;

        runcrawl = new int[32];
        runcrawlpos = 32;
    }

    /*
     * Put match in its canonical form before returning it.
     */
    private Match tidyMatch(boolean quick) {
        if (!quick) {
            Match match = runmatch;

            runmatch = null;

            match.tidy(runtextpos);
            return match;
        } else {
            // in quick mode, a successful match returns null, and
            // the allocated match object is left in the cache

            return null;
        }
    }

    /*
     * Called by the implemenation of Go() to increase the size of storage
     */
    protected void ensureStorage() {
        if (runstackpos < runtrackcount * 4)
            doubleStack();
        if (runtrackpos < runtrackcount * 4)
            doubleTrack();
    }

    /*
     * Called by the implemenation of Go() to decide whether the pos
     * at the specified index is a boundary or not. It's just not worth
     * emitting inline code for this logic.
     */
    protected boolean isBoundary(int index, int startpos, int endpos) {
        return (index > startpos && RegexCharClass.isWordChar(runtext.charAt(index - 1))) !=
                (index < endpos && RegexCharClass.isWordChar(runtext.charAt(index)));
    }

    protected boolean isECMABoundary(int index, int startpos, int endpos) {
        return (index > startpos && RegexCharClass.isECMAWordChar(runtext.charAt(index - 1))) !=
                (index < endpos && RegexCharClass.isECMAWordChar(runtext.charAt(index)));
    }

    protected static boolean charInSet(char ch, String set, String category) {
        String charClass = RegexCharClass.convertOldStringsToClass(set, category);
        return RegexCharClass.charInClass(ch, charClass);
    }

    protected static boolean charInClass(char ch, String charClass) {
        return RegexCharClass.charInClass(ch, charClass);
    }

    /*
     * Called by the implemenation of Go() to increase the size of the
     * backtracking stack.
     */
    protected void doubleTrack() {
        int[] newtrack;

        newtrack = new int[runtrack.length * 2];

        System.arraycopy(runtrack, 0, newtrack, runtrack.length, runtrack.length);
        runtrackpos += runtrack.length;
        runtrack = newtrack;
    }

    /*
     * Called by the implemenation of Go() to increase the size of the
     * grouping stack.
     */
    protected void doubleStack() {
        int[] newstack;

        newstack = new int[runstack.length * 2];

        System.arraycopy(runstack, 0, newstack, runstack.length, runstack.length); // TODO: API diff?
        runstackpos += runstack.length;
        runstack = newstack;
    }

    /*
     * Increases the size of the longjump unrolling stack.
     */
    protected void doubleCrawl() {
        int[] newcrawl;

        newcrawl = new int[runcrawl.length * 2];

        System.arraycopy(runcrawl, 0, newcrawl, runcrawl.length, runcrawl.length); // TODO: API diff?
        runcrawlpos += runcrawl.length;
        runcrawl = newcrawl;
    }

    /*
     * Save a number on the longjump unrolling stack
     */
    protected void crawl(int i) {
        if (runcrawlpos == 0)
            doubleCrawl();

        runcrawl[--runcrawlpos] = i;
    }

    /*
     * Remove a number from the longjump unrolling stack
     */
    protected int popcrawl() {
        return runcrawl[runcrawlpos++];
    }

    /*
     * Get the height of the stack
     */
    protected int crawlpos() {
        return runcrawl.length - runcrawlpos;
    }

    /*
     * Called by Go() to capture a subexpression. Note that the
     * capnum used here has already been mapped to a non-sparse
     * index (by the code generator RegexWriter).
     */
    protected void capture(int capnum, int start, int end) {
        if (end < start) {
            int T;

            T = end;
            end = start;
            start = T;
        }

        crawl(capnum);
        runmatch.addMatch(capnum, start, end - start);
    }

    /*
     * Called by Go() to capture a subexpression. Note that the
     * capnum used here has already been mapped to a non-sparse
     * index (by the code generator RegexWriter).
     */
    protected void transferCapture(int capnum, int uncapnum, int start, int end) {
        int start2;
        int end2;

        // these are the two intervals that are cancelling each other

        if (end < start) {
            int T;

            T = end;
            end = start;
            start = T;
        }

        start2 = matchIndex(uncapnum);
        end2 = start2 + matchLength(uncapnum);

        // The new capture gets the innermost defined interval

        if (start >= end2) {
            end = start;
            start = end2;
        } else if (end <= start2) {
            start = start2;
        } else {
            if (end > end2)
                end = end2;
            if (start2 > start)
                start = start2;
        }

        crawl(uncapnum);
        runmatch.balanceMatch(uncapnum);

        if (capnum != -1) {
            crawl(capnum);
            runmatch.addMatch(capnum, start, end - start);
        }
    }

    /*
     * Called by Go() to revert the last capture
     */
    protected void uncapture() {
        int capnum = popcrawl();
        runmatch.removeMatch(capnum);
    }

    /*
     * Call out to runmatch to get around visibility issues
     */
    protected boolean isMatched(int cap) {
        return runmatch.isMatched(cap);
    }

    /*
     * Call out to runmatch to get around visibility issues
     */
    protected int matchIndex(int cap) {
        return runmatch.matchIndex(cap);
    }

    /*
     * Call out to runmatch to get around visibility issues
     */
    protected int matchLength(int cap) {
        return runmatch.matchLength(cap);
    }

    /*
     * Dump the current state
     */
    void dumpState() {
        System.out.println("Text:  " + textposDescription());
        System.out.println("Track: " + stackDescription(runtrack, runtrackpos));
        System.out.println("Stack: " + stackDescription(runstack, runstackpos));
    }

    static String stackDescription(int[] A, int Index) {
        StringBuilder Sb = new StringBuilder();

        Sb.append(A.length - Index);
        Sb.append('/');
        Sb.append(A.length);

        if (Sb.length() < 8) {
            // TODO: Sb.append(' ', 8 - Sb.length());
            int repeatCount = 8 - Sb.length();
            while (repeatCount > 0) {
                Sb.append(' ');
                repeatCount--;
            }
        }

        Sb.append("(");

        for (int i = Index; i < A.length; i++) {
            if (i > Index)
                Sb.append(' ');
            Sb.append(A[i]);
        }

        Sb.append(')');

        return Sb.toString();
    }

    String textposDescription() {
        StringBuilder Sb = new StringBuilder();
        int remaining;

        Sb.append(runtextpos);

        if (Sb.length() < 8) {
            // TODO: Sb.append(' ', 8 - Sb.length());
            int repeatCount = 8 - Sb.length();
            while (repeatCount > 0) {
                Sb.append(' ');
                repeatCount--;
            }
        }

        if (runtextpos > runtextbeg)
            Sb.append(RegexCharClass.charDescription(runtext.charAt(runtextpos - 1)));
        else
            Sb.append('^');

        Sb.append('>');

        remaining = runtextend - runtextpos;

        for (int i = runtextpos; i < runtextend; i++) {
            Sb.append(RegexCharClass.charDescription(runtext.charAt(i)));
        }
        if (Sb.length() >= 64) {
            // TODO: Sb.Length = 61;
            Sb.setLength(61);
            Sb.append("...");
        } else {
            Sb.append('$');
        }

        return Sb.toString();
    }
}