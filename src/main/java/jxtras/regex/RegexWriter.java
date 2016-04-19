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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// This RegexWriter class is to the Regex package.
// It builds a block of regular expression codes (RegexCode)
// from a RegexTree parse tree.

// Implementation notes:
//
// This step is as simple as walking the tree and emitting
// sequences of codes.
// @author  Tony Guo <tony.guo.peng@gmail.com>
final class RegexWriter {
    private int[] _intStack;
    private int _depth;
    private int[] _emitted;
    private int _curpos;
    private final HashMap<String, Integer> _Stringhash;
    private final List<String> _Stringtable;
    // not used! int         _Stringcount;
    private boolean _counting;
    private int _count;
    private int _trackcount;
    private Map<Integer, Integer> _caps;

    private static final int BeforeChild = 64;
    private static final int AfterChild = 128;

    /*
     * This is the only function that should be called from outside.
     * It takes a RegexTree and creates a corresponding RegexCode.
     */
    static RegexCode write(RegexTree tree) {
        RegexWriter writer = new RegexWriter();
        RegexCode code = writer.regexCodeFromRegexTree(tree);

        // #if DEBUG
        if (tree.isDebugEnabled()) {
            tree.dump();
            code.dump();
        }
        // #endif

        return code;
    }

    /*
     * private constructor; can't be created outside
     */
    private RegexWriter() {
        _intStack = new int[32];
        _emitted = new int[32];
        _Stringhash = new HashMap<String, Integer>();
        _Stringtable = new ArrayList<String>();
    }

    /*
     * To avoid recursion, we use a simple integer stack.
     * This is the push.
     */
    private void pushInt(int I) {
        if (_depth >= _intStack.length) {
            int[] expanded = new int[_depth * 2];

            // TODO: arrayCopy
            System.arraycopy(_intStack, 0, expanded, 0, _depth);

            _intStack = expanded;
        }

        _intStack[_depth++] = I;
    }

    /*
     * True if the stack is empty.
     */
    private boolean emptyStack() {
        return _depth == 0;
    }

    /*
     * This is the pop.
     */
    private int popInt() {
        return _intStack[--_depth];
    }

    /*
     * Returns the current position in the emitted code.
     */
    private int curPos() {
        return _curpos;
    }

    /*
     * Fixes up a jump instruction at the specified offset
     * so that it jumps to the specified jumpDest.
     */
    private void patchJump(int Offset, int jumpDest) {
        _emitted[Offset + 1] = jumpDest;
    }

    /*
     * Emits a zero-argument operation. Note that the emit
     * functions all run in two modes: they can emit code, or
     * they can just count the size of the code.
     */
    private void emit(int op) {
        if (_counting) {
            _count += 1;
            if (RegexCode.opcodeBacktracks(op))
                _trackcount += 1;
            return;
        }
        _emitted[_curpos++] = op;
    }

    /*
     * Emits a one-argument operation.
     */
    private void emit(int op, int opd1) {
        if (_counting) {
            _count += 2;
            if (RegexCode.opcodeBacktracks(op))
                _trackcount += 1;
            return;
        }
        _emitted[_curpos++] = op;
        _emitted[_curpos++] = opd1;
    }

    /*
     * Emits a two-argument operation.
     */
    private void emit(int op, int opd1, int opd2) {
        if (_counting) {
            _count += 3;
            if (RegexCode.opcodeBacktracks(op))
                _trackcount += 1;
            return;
        }
        _emitted[_curpos++] = op;
        _emitted[_curpos++] = opd1;
        _emitted[_curpos++] = opd2;
    }

    /*
     * Returns an index in the String table for a String;
     * uses a hashtable to eliminate duplicates.
     */
    private int stringCode(String str) {
        if (_counting)
            return 0;

        if (str == null)
            str = "";

        Integer i = _Stringhash.get(str);
        if (i != null) {
            return i;
        }

        i = _Stringtable.size();
        _Stringhash.put(str, i);
        _Stringtable.add(str);

        return i;
    }

    /*
     * When generating code on a regex that uses a sparse set
     * of capture slots, we hash them to a dense set of indices
     * for an array of capture slots. Instead of doing the hash
     * at match time, it's done at compile time, here.
     */
    private int mapCapnum(int capnum) {
        if (capnum == -1)
            return -1;

        if (_caps != null)
            return _caps.get(capnum);
        else
            return capnum;
    }

    /*
     * The top level RegexCode generator. It does a depth-first walk
     * through the tree and calls EmitFragment to emits code before
     * and after each child of an interior node, and at each leaf.
     *
     * It runs two passes, first to count the size of the generated
     * code, and second to generate the code.
     *
     * We should time it against the alternative, which is
     * to just generate the code and grow the array as we go.
     */
    private RegexCode regexCodeFromRegexTree(RegexTree tree) {
        RegexNode curNode;
        int curChild;
        int capsize;
        RegexPrefix fcPrefix;
        RegexPrefix prefix;
        int anchors;
        RegexBoyerMoore bmPrefix;
        boolean rtl;

        // construct sparse capnum mapping if some numbers are unused

        if (tree._capnumlist == null || tree._captop == tree._capnumlist.length) {
            capsize = tree._captop;
            _caps = null;
        } else {
            capsize = tree._capnumlist.length;
            _caps = tree._caps;
            for (int i = 0; i < tree._capnumlist.length; i++) {
                // TODO: _caps[tree._capnumlist[i]] = i;
                _caps.put(tree._capnumlist[i], i);
            }
        }

        _counting = true;

        for (; ;) {
            if (!_counting)
                _emitted = new int[_count];

            curNode = tree._root;
            curChild = 0;

            emit(RegexCode.Lazybranch, 0);

            for (; ;) {
                if (curNode._children == null) {
                    emitFragment(curNode._type, curNode, 0);
                } else if (curChild < curNode._children.size()) {
                    emitFragment(curNode._type | BeforeChild, curNode, curChild);

                    curNode = curNode._children.get(curChild);
                    pushInt(curChild);
                    curChild = 0;
                    continue;
                }

                if (emptyStack())
                    break;

                curChild = popInt();
                curNode = curNode._next;

                emitFragment(curNode._type | AfterChild, curNode, curChild);
                curChild++;
            }

            patchJump(0, curPos());
            emit(RegexCode.Stop);

            if (!_counting)
                break;

            _counting = false;
        }

        fcPrefix = RegexFCD.firstChars(tree);

        prefix = RegexFCD.prefix(tree);
        rtl = ((tree._options & RegexOptions.RightToLeft) != 0);

        Locale culture = (tree._options & RegexOptions.CultureInvariant) !=
                0 ? Locale.ROOT : Locale.getDefault();
        if (prefix != null && prefix.prefix().length() > 0)
            bmPrefix = new RegexBoyerMoore(prefix.prefix(), prefix.isCaseInsensitive(), rtl, culture);
        else
            bmPrefix = null;

        anchors = RegexFCD.anchors(tree);

        return new RegexCode(_emitted, _Stringtable, _trackcount, _caps, capsize, bmPrefix, fcPrefix, anchors, rtl);
    }

    /*
     * The main RegexCode generator. It does a depth-first walk
     * through the tree and calls EmitFragment to emits code before
     * and after each child of an interior node, and at each leaf.
     */
    private void emitFragment(int nodetype, RegexNode node, int CurIndex) {
        int bits = 0;

        if (nodetype <= RegexNode.Ref) {
            if (node.useOptionR())
                bits |= RegexCode.Rtl;
            if ((node._options & RegexOptions.IgnoreCase) != 0)
                bits |= RegexCode.Ci;
        }

        switch (nodetype) {
            case RegexNode.Concatenate | BeforeChild:
            case RegexNode.Concatenate | AfterChild:
            case RegexNode.Empty:
                break;

            case RegexNode.Alternate | BeforeChild:
                if (CurIndex < node._children.size() - 1) {
                    pushInt(curPos());
                    emit(RegexCode.Lazybranch, 0);
                }
                break;

            case RegexNode.Alternate | AfterChild: {

                if (CurIndex < node._children.size() - 1) {
                    int LBPos = popInt();
                    pushInt(curPos());
                    emit(RegexCode.Goto, 0);
                    patchJump(LBPos, curPos());
                } else {
                    for (int i = 0; i < CurIndex; i++) {
                        patchJump(popInt(), curPos());
                    }
                }
                break;
            }

            case RegexNode.Testref | BeforeChild:
                switch (CurIndex) {
                    case 0:
                        emit(RegexCode.Setjump);
                        pushInt(curPos());
                        emit(RegexCode.Lazybranch, 0);
                        emit(RegexCode.Testref, mapCapnum(node._m));
                        emit(RegexCode.Forejump);
                        break;
                }
                break;

            case RegexNode.Testref | AfterChild:
                switch (CurIndex) {
                    case 0: {
                        int Branchpos = popInt();
                        pushInt(curPos());
                        emit(RegexCode.Goto, 0);
                        patchJump(Branchpos, curPos());
                        emit(RegexCode.Forejump);
                        if (node._children.size() > 1) {
                            break;
                        }
                        // TODO:
                        // fallthrough
                        // goto case 1;
                    }
                    case 1:
                        patchJump(popInt(), curPos());
                        break;
                }
                break;

            case RegexNode.Testgroup | BeforeChild:
                switch (CurIndex) {
                    case 0:
                        emit(RegexCode.Setjump);
                        emit(RegexCode.Setmark);
                        pushInt(curPos());
                        emit(RegexCode.Lazybranch, 0);
                        break;
                }
                break;

            case RegexNode.Testgroup | AfterChild:
                switch (CurIndex) {
                    case 0:
                        emit(RegexCode.Getmark);
                        emit(RegexCode.Forejump);
                        break;
                    case 1:
                        int Branchpos = popInt();
                        pushInt(curPos());
                        emit(RegexCode.Goto, 0);
                        patchJump(Branchpos, curPos());
                        emit(RegexCode.Getmark);
                        emit(RegexCode.Forejump);

                        if (node._children.size() > 2) {
                            break;
                        }
                        // TODO:
                        // fallthrough
                        // goto case 2;

                    case 2:
                        patchJump(popInt(), curPos());
                        break;
                }
                break;

            case RegexNode.Loop | BeforeChild:
            case RegexNode.Lazyloop | BeforeChild:

                if (node._n < Integer.MAX_VALUE || node._m > 1)
                    emit(node._m == 0 ? RegexCode.Nullcount : RegexCode.Setcount, node._m == 0 ? 0 : 1 - node._m);
                else
                    emit(node._m == 0 ? RegexCode.Nullmark : RegexCode.Setmark);

                if (node._m == 0) {
                    pushInt(curPos());
                    emit(RegexCode.Goto, 0);
                }
                pushInt(curPos());
                break;

            case RegexNode.Loop | AfterChild:
            case RegexNode.Lazyloop | AfterChild: {
                int StartJumpPos = curPos();
                int Lazy = (nodetype - (RegexNode.Loop | AfterChild));

                if (node._n < Integer.MAX_VALUE || node._m > 1)
                    emit(RegexCode.Branchcount + Lazy, popInt(), node._n == Integer.MAX_VALUE ? Integer.MAX_VALUE : node._n - node._m);
                else
                    emit(RegexCode.Branchmark + Lazy, popInt());

                if (node._m == 0)
                    patchJump(popInt(), StartJumpPos);
            }
            break;

            case RegexNode.Group | BeforeChild:
            case RegexNode.Group | AfterChild:
                break;

            case RegexNode.Capture | BeforeChild:
                emit(RegexCode.Setmark);
                break;

            case RegexNode.Capture | AfterChild:
                emit(RegexCode.Capturemark, mapCapnum(node._m), mapCapnum(node._n));
                break;

            case RegexNode.Require | BeforeChild:
                // NOTE: the following line causes lookahead/lookbehind to be
                // NON-BACKTRACKING. It can be commented out with (*)
                emit(RegexCode.Setjump);

                emit(RegexCode.Setmark);
                break;

            case RegexNode.Require | AfterChild:
                emit(RegexCode.Getmark);

                // NOTE: the following line causes lookahead/lookbehind to be
                // NON-BACKTRACKING. It can be commented out with (*)
                emit(RegexCode.Forejump);
                break;

            case RegexNode.Prevent | BeforeChild:
                emit(RegexCode.Setjump);
                pushInt(curPos());
                emit(RegexCode.Lazybranch, 0);
                break;

            case RegexNode.Prevent | AfterChild:
                emit(RegexCode.Backjump);
                patchJump(popInt(), curPos());
                emit(RegexCode.Forejump);
                break;

            case RegexNode.Greedy | BeforeChild:
                emit(RegexCode.Setjump);
                break;

            case RegexNode.Greedy | AfterChild:
                emit(RegexCode.Forejump);
                break;

            case RegexNode.One:
            case RegexNode.Notone:
                emit(node._type | bits, (int) node._ch);
                break;

            case RegexNode.Notoneloop:
            case RegexNode.Notonelazy:
            case RegexNode.Oneloop:
            case RegexNode.Onelazy:
                if (node._m > 0)
                    emit(((node._type == RegexNode.Oneloop || node._type == RegexNode.Onelazy) ?
                            RegexCode.Onerep : RegexCode.Notonerep) | bits, (int) node._ch, node._m);
                if (node._n > node._m)
                    emit(node._type | bits, (int) node._ch, node._n == Integer.MAX_VALUE ?
                            Integer.MAX_VALUE : node._n - node._m);
                break;

            case RegexNode.Setloop:
            case RegexNode.Setlazy:
                if (node._m > 0)
                    emit(RegexCode.Setrep | bits, stringCode(node._str), node._m);
                if (node._n > node._m)
                    emit(node._type | bits, stringCode(node._str),
                            (node._n == Integer.MAX_VALUE) ? Integer.MAX_VALUE : node._n - node._m);
                break;

            case RegexNode.Multi:
                emit(node._type | bits, stringCode(node._str));
                break;

            case RegexNode.Set:
                emit(node._type | bits, stringCode(node._str));
                break;

            case RegexNode.Ref:
                emit(node._type | bits, mapCapnum(node._m));
                break;

            case RegexNode.Nothing:
            case RegexNode.Bol:
            case RegexNode.Eol:
            case RegexNode.Boundary:
            case RegexNode.Nonboundary:
            case RegexNode.ECMABoundary:
            case RegexNode.NonECMABoundary:
            case RegexNode.Beginning:
            case RegexNode.Start:
            case RegexNode.EndZ:
            case RegexNode.End:
                emit(node._type);
                break;

            default:
                throw new IllegalArgumentException(R.format(R.UnexpectedOpcode, nodetype));
        }
    }
}