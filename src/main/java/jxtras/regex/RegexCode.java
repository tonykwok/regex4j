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

import java.util.List;
import java.util.Map;

// This RegexCode class is internal to the regular expression package.
// It provides operator constants for use by the Builder and the Machine.

// Implementation notes:
//
// Regexps are built into RegexCodes, which contain an operation array,
// a string table, and some constants.
//
// Each operation is one of the codes below, followed by the integer
// operands specified for each op.
//
// Strings and sets are indices into a string table.

final class RegexCode {
    // the following primitive operations come directly from the parser

                                              // lef/back operands        description
    static final int Onerep = 0;              // lef,back char,min,max    a {n}
    static final int Notonerep = 1;           // lef,back char,min,max    .{n}
    static final int Setrep = 2;              // lef,back set,min,max     [\d]{n}

    static final int Oneloop = 3;             // lef,back char,min,max    a {,n}
    static final int Notoneloop = 4;          // lef,back char,min,max    .{,n}
    static final int Setloop = 5;             // lef,back set,min,max     [\d]{,n}

    static final int Onelazy = 6;             // lef,back char,min,max    a {,n}?
    static final int Notonelazy = 7;          // lef,back char,min,max    .{,n}?
    static final int Setlazy = 8;             // lef,back set,min,max     [\d]{,n}?

    static final int One = 9;                 // lef      char            a
    static final int Notone = 10;             // lef      char            [^a]
    static final int Set = 11;                // lef      set             [a-z\s]  \w \s \d

    static final int Multi = 12;              // lef      string          abcd
    static final int Ref = 13;                // lef      group           \#

    static final int Bol = 14;                //                          ^
    static final int Eol = 15;                //                          $
    static final int Boundary = 16;           //                          \b
    static final int Nonboundary = 17;        //                          \B
    static final int Beginning = 18;          //                          \A
    static final int Start = 19;              //                          \G
    static final int EndZ = 20;               //                          \Z
    static final int End = 21;                //                          \Z

    static final int Nothing = 22;            //                          Reject!

    // primitive control structures

    static final int Lazybranch = 23;         // back     jump            straight first
    static final int Branchmark = 24;         // back     jump            branch first for loop
    static final int Lazybranchmark = 25;     // back     jump            straight first for loop
    static final int Nullcount = 26;          // back     val             set counter, null mark
    static final int Setcount = 27;           // back     val             set counter, make mark
    static final int Branchcount = 28;        // back     jump,limit      branch++ if zero<=c<limit
    static final int Lazybranchcount = 29;    // back     jump,limit      same, but straight first
    static final int Nullmark = 30;           // back                     save position
    static final int Setmark = 31;            // back                     save position
    static final int Capturemark = 32;        // back     group           define group
    static final int Getmark = 33;            // back                     recall position
    static final int Setjump = 34;            // back                     save backtrack state
    static final int Backjump = 35;           //                          zap back to saved state
    static final int Forejump = 36;           //                          zap backtracking state
    static final int Testref = 37;            //                          backtrack if ref undefined
    static final int Goto = 38;               //          jump            just go

    static final int Prune = 39;              //                          prune it baby
    static final int Stop = 40;               //                          done!

    static final int ECMABoundary = 41;       //                          \b
    static final int NonECMABoundary = 42;    //                          \B

    // modifiers for alternate modes
    static final int Mask = 63;   // Mask to get unmodified ordinary operator
    static final int Rtl = 64;    // bit to indicate that we're reverse scanning.
    static final int Back = 128;  // bit to indicate that we're backtracking.
    static final int Back2 = 256; // bit to indicate that we're backtracking on a second branch.
    static final int Ci = 512;    // bit to indicate that we're case-insensitive.

    int[] _codes;                    // the code
    String[] _strings;               // the string/set table
    // not used!
    // internal int[] _sparseIndex;  // a list of the groups that are used
    int _trackcount;                 // how many instructions use backtracking
    Map<Integer, Integer> _caps;     // mapping of user group numbers -> impl group slots
    int _capsize;                    // number of impl group slots
    RegexPrefix _fcPrefix;           // the set of candidate first characters (may be null)
    RegexBoyerMoore _bmPrefix;       // the fixed prefix string as a Boyer-Moore machine (may be null)
    int _anchors;                    // the set of zero-length start anchors (RegexFCD.Bol, etc)
    boolean _rightToLeft;            // true if right to left

    // constructor
    RegexCode(int[] codes, List<String> stringlist, int trackcount, Map<Integer, Integer> caps, int capsize, RegexBoyerMoore bmPrefix, RegexPrefix fcPrefix, int anchors, boolean rightToLeft) {
        if (codes == null) {
            throw new IllegalArgumentException("codes cannot be null.");
        }

        if (stringlist == null) {
            throw new IllegalArgumentException("stringlist cannot be null.");
        }

        _codes = codes;
        // TODO: stringlist.CopyTo(0, _strings, 0, stringlist.size());
        _strings = stringlist.toArray(new String[0]);
        _trackcount = trackcount;
        _caps = caps;
        _capsize = capsize;
        _bmPrefix = bmPrefix;
        _fcPrefix = fcPrefix;
        _anchors = anchors;
        _rightToLeft = rightToLeft;
    }

    static boolean opcodeBacktracks(int op) {
        op &= Mask;

        switch (op) {
            case Oneloop:
            case Notoneloop:
            case Setloop:
            case Onelazy:
            case Notonelazy:
            case Setlazy:
            case Lazybranch:
            case Branchmark:
            case Lazybranchmark:
            case Nullcount:
            case Setcount:
            case Branchcount:
            case Lazybranchcount:
            case Setmark:
            case Capturemark:
            case Getmark:
            case Setjump:
            case Backjump:
            case Forejump:
            case Goto:
                return true;

            default:
                return false;
        }
    }

// #if DEBUG
    static int opcodeSize(int opcode) {
        opcode &= Mask;

        switch (opcode) {
            case Nothing:
            case Bol:
            case Eol:
            case Boundary:
            case Nonboundary:
            case ECMABoundary:
            case NonECMABoundary:
            case Beginning:
            case Start:
            case EndZ:
            case End:
            case Nullmark:
            case Setmark:
            case Getmark:
            case Setjump:
            case Backjump:
            case Forejump:
            case Stop:
                return 1;

            case One:
            case Notone:
            case Multi:
            case Ref:
            case Testref:
            case Goto:
            case Nullcount:
            case Setcount:
            case Lazybranch:
            case Branchmark:
            case Lazybranchmark:
            case Prune:
            case Set:
                return 2;

            case Capturemark:
            case Branchcount:
            case Lazybranchcount:
            case Onerep:
            case Notonerep:
            case Oneloop:
            case Notoneloop:
            case Onelazy:
            case Notonelazy:
            case Setlazy:
            case Setrep:
            case Setloop:
                return 3;

            default:
                throw new IllegalArgumentException(R.format(R.UnexpectedOpcode, opcode));
        }
    }

    private static final String[] CodeStr = new String[] {
        "Onerep", "Notonerep", "Setrep",
         "Oneloop", "Notoneloop", "Setloop",
         "Onelazy", "Notonelazy", "Setlazy",
         "One", "Notone", "Set",
         "Multi", "Ref",
         "Bol", "Eol", "Boundary", "Nonboundary", "Beginning", "Start", "EndZ", "End",
         "Nothing",
         "Lazybranch", "Branchmark", "Lazybranchmark",
         "Nullcount", "Setcount", "Branchcount", "Lazybranchcount",
         "Nullmark", "Setmark", "Capturemark", "Getmark",
         "Setjump", "Backjump", "Forejump", "Testref", "Goto",
         "Prune", "Stop",
// #if ECMA
         "ECMABoundary", "NonECMABoundary",
// #endif
    };

    static String operatorDescription(int Opcode) {
        boolean isCi = ((Opcode & Ci) != 0);
        boolean isRtl = ((Opcode & Rtl) != 0);
        boolean isBack = ((Opcode & Back) != 0);
        boolean isBack2 = ((Opcode & Back2) != 0);

        return CodeStr[Opcode & Mask] +
                (isCi ? "-Ci" : "") + (isRtl ? "-Rtl" : "") + (isBack ? "-Back" : "") + (isBack2 ? "-Back2" : "");
    }

    String opcodeDescription(int offset) {
        StringBuilder sb = new StringBuilder();
        int opcode = _codes[offset];

        // TODO: sb.appendFormat("{0:D6} ", offset);
        sb.append(String.format("%6d", offset));
        sb.append(opcodeBacktracks(opcode & Mask) ? '*' : ' ');
        sb.append(operatorDescription(opcode));
        sb.append('(');

        opcode &= Mask;

        switch (opcode) {
            case One:
            case Notone:
            case Onerep:
            case Notonerep:
            case Oneloop:
            case Notoneloop:
            case Onelazy:
            case Notonelazy:
                sb.append("Ch = ");
                sb.append(RegexCharClass.charDescription((char) _codes[offset + 1]));
                break;

            case Set:
            case Setrep:
            case Setloop:
            case Setlazy:
                sb.append("Set = ");
                sb.append(RegexCharClass.setDescription(_strings[_codes[offset + 1]]));
                break;

            case Multi:
                sb.append("String = ");
                sb.append(_strings[_codes[offset + 1]]);
                break;

            case Ref:
            case Testref:
                sb.append("Index = ");
                sb.append(_codes[offset + 1]);
                break;

            case Capturemark:
                sb.append("Index = ");
                sb.append(_codes[offset + 1]);
                if (_codes[offset + 2] != -1) {
                    sb.append(", Unindex = ");
                    sb.append(_codes[offset + 2]);
                }
                break;

            case Nullcount:
            case Setcount:
                sb.append("Value = ");
                sb.append(_codes[offset + 1]);
                break;

            case Goto:
            case Lazybranch:
            case Branchmark:
            case Lazybranchmark:
            case Branchcount:
            case Lazybranchcount:
                sb.append("Addr = ");
                sb.append(_codes[offset + 1]);
                break;
        }

        switch (opcode) {
            case Onerep:
            case Notonerep:
            case Oneloop:
            case Notoneloop:
            case Onelazy:
            case Notonelazy:
            case Setrep:
            case Setloop:
            case Setlazy:
                sb.append(", Rep = ");
                if (_codes[offset + 2] == Integer.MAX_VALUE) // TODO: int32
                    sb.append("inf");
                else
                    sb.append(_codes[offset + 2]);
                break;

            case Branchcount:
            case Lazybranchcount:
                sb.append(", Limit = ");
                if (_codes[offset + 2] == Integer.MAX_VALUE) // TODO: int32
                    sb.append("inf");
                else
                    sb.append(_codes[offset + 2]);
                break;
        }

        sb.append(")");

        return sb.toString();
    }

    void dump() {
        System.out.println("Direction:  " + (_rightToLeft ? "right-to-left" : "left-to-right"));
        System.out.println("Firstchars: " + (_fcPrefix == null ? "n/a" : RegexCharClass.setDescription(_fcPrefix.prefix())));
        System.out.println("Prefix:     " + (_bmPrefix == null ? "n/a" : Regex.escape(_bmPrefix.toString())));
        System.out.println("Anchors:    " + RegexFCD.anchorDescription(_anchors));
        System.out.println("");
        if (_bmPrefix != null) {
            System.out.println("BoyerMoore:");
            System.out.println(_bmPrefix.dump("    "));
        }
        for (int i = 0; i < _codes.length; ) {
            System.out.println(opcodeDescription(i));
            i += opcodeSize(_codes[i]);
        }

        System.out.println("");
    }
// #endif
}