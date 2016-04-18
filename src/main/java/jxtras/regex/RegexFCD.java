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

/**
 * This RegexFCD class is internal to the Regex package.
 * It builds a bunch of FC information (RegexFC) about
 * the regex for optimization purposes.
 * <p/>
 * Implementation notes:
 * <p/>
 * This step is as simple as walking the tree and emitting
 * sequences of codes.
 */
final class RegexFCD {
    private int[] _intStack;
    private int _intDepth;
    private RegexFC[] _fcStack;
    private int _fcDepth;
    private boolean _skipAllChildren;      // don't process any more children at the current level
    private boolean _skipchild;            // don't process the current child.
    private boolean _failed = false;

    private static final int BeforeChild = 64;
    private static final int AfterChild = 128;

    // where the regex can be pegged

    static final int Beginning = 0x0001;
    static final int Bol = 0x0002;
    static final int Start = 0x0004;
    static final int Eol = 0x0008;
    static final int EndZ = 0x0010;
    static final int End = 0x0020;
    static final int Boundary = 0x0040;
    static final int ECMABoundary = 0x0080;

    /*
     * This is the one of the only two functions that should be called from outside.
     * It takes a RegexTree and computes the set of chars that can start it.
     */
    static RegexPrefix firstChars(RegexTree t) {
        RegexFCD s = new RegexFCD();
        RegexFC fc = s.regexFCFromRegexTree(t);

        if (fc == null || fc._nullable) {
            return null;
        }

        Locale culture = ((t._options & RegexOptions.CultureInvariant) != 0) ? Locale.ROOT :
                Locale.getDefault();
        return new RegexPrefix(fc.getFirstChars(culture), fc.isCaseInsensitive());
    }

    /*
     * This is a related computation: it takes a RegexTree and computes the
     * leading substring if it see one. It's quite trivial and gives up easily.
     */
    static RegexPrefix prefix(RegexTree tree) {
        RegexNode curNode;
        RegexNode concatNode = null;
        int nextChild = 0;

        curNode = tree._root;

        for (; ; ) {
            switch (curNode._type) {
                case RegexNode.Concatenate:
                    if (curNode.childCount() > 0) {
                        concatNode = curNode;
                        nextChild = 0;
                    }
                    break;

                case RegexNode.Greedy:
                case RegexNode.Capture:
                    curNode = curNode.childAt(0);
                    concatNode = null;
                    continue;

                case RegexNode.Oneloop:
                case RegexNode.Onelazy:
                    if (curNode._m > 0) {
                        StringBuilder sb = new StringBuilder(); // ADD
                        for (int repeat = 0; repeat < curNode._m; repeat++) { // ADD
                            sb.append(curNode._ch); // ADD
                        } // ADD
                        String pref = sb.toString(); // TODO: String.Empty.PadRight(curNode._m, curNode._ch);
                        return new RegexPrefix(pref, 0 != (curNode._options & RegexOptions.IgnoreCase));
                    } else
                        return RegexPrefix._empty;

                case RegexNode.One:
                    return new RegexPrefix(String.valueOf(curNode._ch) /*.ToString(CultureInfo.InvariantCulture)*/, 0 != (curNode._options & RegexOptions.IgnoreCase));

                case RegexNode.Multi:
                    return new RegexPrefix(curNode._str, 0 != (curNode._options & RegexOptions.IgnoreCase));

                case RegexNode.Bol:
                case RegexNode.Eol:
                case RegexNode.Boundary:
                case RegexNode.ECMABoundary:
                case RegexNode.Beginning:
                case RegexNode.Start:
                case RegexNode.EndZ:
                case RegexNode.End:
                case RegexNode.Empty:
                case RegexNode.Require:
                case RegexNode.Prevent:
                    break;

                default:
                    return RegexPrefix._empty;
            }

            if (concatNode == null || nextChild >= concatNode.childCount())
                return RegexPrefix._empty;

            curNode = concatNode.childAt(nextChild++);
        }
    }

    /*
     * Yet another related computation: it takes a RegexTree and computes the
     * leading anchors that it encounters.
     */
    static int anchors(RegexTree tree) {
        RegexNode curNode;
        RegexNode concatNode = null;
        int nextChild = 0;
        int result = 0;

        curNode = tree._root;

        for (; ; ) {
            switch (curNode._type) {
                case RegexNode.Concatenate:
                    if (curNode.childCount() > 0) {
                        concatNode = curNode;
                        nextChild = 0;
                    }
                    break;

                case RegexNode.Greedy:
                case RegexNode.Capture:
                    curNode = curNode.childAt(0);
                    concatNode = null;
                    continue;

                case RegexNode.Bol:
                case RegexNode.Eol:
                case RegexNode.Boundary:
                case RegexNode.ECMABoundary:
                case RegexNode.Beginning:
                case RegexNode.Start:
                case RegexNode.EndZ:
                case RegexNode.End:
                    return result | anchorFromType(curNode._type);

                case RegexNode.Empty:
                case RegexNode.Require:
                case RegexNode.Prevent:
                    break;

                default:
                    return result;
            }

            if (concatNode == null || nextChild >= concatNode.childCount())
                return result;

            curNode = concatNode.childAt(nextChild++);
        }
    }

    /*
     * Convert anchor type to anchor bit.
     */
    private static int anchorFromType(int type) {
        switch (type) {
            case RegexNode.Bol:
                return Bol;
            case RegexNode.Eol:
                return Eol;
            case RegexNode.Boundary:
                return Boundary;
            case RegexNode.ECMABoundary:
                return ECMABoundary;
            case RegexNode.Beginning:
                return Beginning;
            case RegexNode.Start:
                return Start;
            case RegexNode.EndZ:
                return EndZ;
            case RegexNode.End:
                return End;
            default:
                return 0;
        }
    }

    static String anchorDescription(int anchors) {
        StringBuilder sb = new StringBuilder();

        if (0 != (anchors & Beginning)) sb.append(", Beginning");
        if (0 != (anchors & Start)) sb.append(", Start");
        if (0 != (anchors & Bol)) sb.append(", Bol");
        if (0 != (anchors & Boundary)) sb.append(", Boundary");
        if (0 != (anchors & ECMABoundary)) sb.append(", ECMABoundary");
        if (0 != (anchors & Eol)) sb.append(", Eol");
        if (0 != (anchors & End)) sb.append(", End");
        if (0 != (anchors & EndZ)) sb.append(", EndZ");

        if (sb.length() >= 2)
            // TODO: (sb.toString().substring(2, sb.length() - 2))
            return sb.substring(2, sb.length());

        return "None";
    }

    /*
     * private constructor; can't be created outside
     */
    private RegexFCD() {
        _fcStack = new RegexFC[32];
        _intStack = new int[32];
    }

    /*
     * To avoid recursion, we use a simple integer stack.
     * This is the push.
     */
    private void pushInt(int I) {
        if (_intDepth >= _intStack.length) {
            int[] expanded = new int[_intDepth * 2];

            System.arraycopy(_intStack, 0, expanded, 0, _intDepth); // TODO: API diff in Java?

            _intStack = expanded;
        }

        _intStack[_intDepth++] = I;
    }

    /*
     * True if the stack is empty.
     */
    private boolean isIntEmpty() {
        return _intDepth == 0;
    }

    /*
     * This is the pop.
     */
    private int popInt() {
        return _intStack[--_intDepth];
    }

    /*
      * We also use a stack of RegexFC objects.
      * This is the push.
      */
    private void pushFC(RegexFC fc) {
        if (_fcDepth >= _fcStack.length) {
            RegexFC[] expanded = new RegexFC[_fcDepth * 2];

            System.arraycopy(_fcStack, 0, expanded, 0, _fcDepth); // TODO: API diff in Java?
            _fcStack = expanded;
        }

        _fcStack[_fcDepth++] = fc;
    }

    /*
     * True if the stack is empty.
     */
    private boolean isFCEmpty() {
        return _fcDepth == 0;
    }

    /*
     * This is the pop.
     */
    private RegexFC popFC() {
        return _fcStack[--_fcDepth];
    }

    /*
     * This is the top.
     */
    private RegexFC topFC() {
        return _fcStack[_fcDepth - 1];
    }

    /*
     * The main FC computation. It does a shortcutted depth-first walk
     * through the tree and calls CalculateFC to emits code before
     * and after each child of an interior node, and at each leaf.
     */
    private RegexFC regexFCFromRegexTree(RegexTree tree) {
        RegexNode curNode;
        int curChild;

        curNode = tree._root;
        curChild = 0;

        for (; ; ) {
            if (curNode._children == null) {
                // This is a leaf node
                calculateFC(curNode._type, curNode, 0);
            } else if (curChild < curNode._children.size() && !_skipAllChildren) {
                // This is an interior node, and we have more children to analyze
                calculateFC(curNode._type | BeforeChild, curNode, curChild);

                if (!_skipchild) {
                    curNode = curNode._children.get(curChild); // TODO:
                    // this stack is how we get a depth first walk of the tree.
                    pushInt(curChild);
                    curChild = 0;
                } else {
                    curChild++;
                    _skipchild = false;
                }
                continue;
            }

            // This is an interior node where we've finished analyzing all the children, or
            // the end of a leaf node.
            _skipAllChildren = false;

            if (isIntEmpty())
                break;

            curChild = popInt();
            curNode = curNode._next;

            calculateFC(curNode._type | AfterChild, curNode, curChild);
            if (_failed)
                return null;

            curChild++;
        }

        if (isFCEmpty())
            return null;

        return popFC();
    }

    /*
     * Called in Beforechild to prevent further processing of the current child
     */
    private void skipChild() {
        _skipchild = true;
    }

    /*
     * FC computation and shortcut cases for each node type
     */
    private void calculateFC(int nodeType, RegexNode node, int curIndex) {
        boolean ci = false;
        boolean rtl = false;

        if (nodeType <= RegexNode.Ref) {
            if ((node._options & RegexOptions.IgnoreCase) != 0) {
                ci = true;
            }
            if ((node._options & RegexOptions.RightToLeft) != 0) {
                rtl = true;
            }
        }

        switch (nodeType) {
            case RegexNode.Concatenate | BeforeChild:
            case RegexNode.Alternate | BeforeChild:
            case RegexNode.Testref | BeforeChild:
            case RegexNode.Loop | BeforeChild:
            case RegexNode.Lazyloop | BeforeChild:
                break;

            case RegexNode.Testgroup | BeforeChild:
                if (curIndex == 0) {
                    skipChild();
                }
                break;

            case RegexNode.Empty:
                pushFC(new RegexFC(true));
                break;

            case RegexNode.Concatenate | AfterChild:
                if (curIndex != 0) {
                    RegexFC child = popFC();
                    RegexFC cumul = topFC();

                    _failed = !cumul.addFC(child, true);
                }

                if (!topFC()._nullable) {
                    _skipAllChildren = true;
                }
                break;

            case RegexNode.Testgroup | AfterChild:
                if (curIndex > 1) {
                    RegexFC child = popFC();
                    RegexFC cumul = topFC();

                    _failed = !cumul.addFC(child, false);
                }
                break;

            case RegexNode.Alternate | AfterChild:
            case RegexNode.Testref | AfterChild:
                if (curIndex != 0) {
                    RegexFC child = popFC();
                    RegexFC cumul = topFC();

                    _failed = !cumul.addFC(child, false);
                }
                break;

            case RegexNode.Loop | AfterChild:
            case RegexNode.Lazyloop | AfterChild:
                if (node._m == 0) {
                    topFC()._nullable = true;
                }
                break;

            case RegexNode.Group | BeforeChild:
            case RegexNode.Group | AfterChild:
            case RegexNode.Capture | BeforeChild:
            case RegexNode.Capture | AfterChild:
            case RegexNode.Greedy | BeforeChild:
            case RegexNode.Greedy | AfterChild:
                break;

            case RegexNode.Require | BeforeChild:
            case RegexNode.Prevent | BeforeChild:
                skipChild();
                pushFC(new RegexFC(true));
                break;

            case RegexNode.Require | AfterChild:
            case RegexNode.Prevent | AfterChild:
                break;

            case RegexNode.One:
            case RegexNode.Notone:
                pushFC(new RegexFC(node._ch, nodeType == RegexNode.Notone, false, ci));
                break;

            case RegexNode.Oneloop:
            case RegexNode.Onelazy:
                pushFC(new RegexFC(node._ch, false, node._m == 0, ci));
                break;

            case RegexNode.Notoneloop:
            case RegexNode.Notonelazy:
                pushFC(new RegexFC(node._ch, true, node._m == 0, ci));
                break;

            case RegexNode.Multi:
                if (node._str.length() == 0)
                    pushFC(new RegexFC(true));
                else if (!rtl)
                    pushFC(new RegexFC(node._str.charAt(0), false, false, ci));
                else
                    pushFC(new RegexFC(node._str.charAt(node._str.length() - 1), false, false, ci));
                break;

            case RegexNode.Set:
                pushFC(new RegexFC(node._str, false, ci));
                break;

            case RegexNode.Setloop:
            case RegexNode.Setlazy:
                pushFC(new RegexFC(node._str, node._m == 0, ci));
                break;

            case RegexNode.Ref:
                pushFC(new RegexFC(RegexCharClass.AnyClass, true, false));
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
                pushFC(new RegexFC(true));
                break;

            default:
                throw new IllegalArgumentException(R.format(R.UnexpectedOpcode, nodeType));
        }
    }
}

final class RegexFC {
    RegexCharClass _cc;
    boolean _nullable;
    boolean _caseInsensitive;

    RegexFC(boolean nullable) {
        _cc = new RegexCharClass();
        _nullable = nullable;
    }

    RegexFC(char ch, boolean not, boolean nullable, boolean caseInsensitive) {
        _cc = new RegexCharClass();

        if (not) {
            if (ch > 0)
                _cc.addRange('\0', (char) (ch - 1));
            if (ch < 0xFFFF)
                _cc.addRange((char) (ch + 1), '\uFFFF');
        } else {
            _cc.addRange(ch, ch);
        }

        _caseInsensitive = caseInsensitive;
        _nullable = nullable;
    }

    RegexFC(String charClass, boolean nullable, boolean caseInsensitive) {
        _cc = RegexCharClass.parse(charClass);

        _nullable = nullable;
        _caseInsensitive = caseInsensitive;
    }

    boolean addFC(RegexFC fc, boolean concatenate) {
        if (!_cc.canMerge() || !fc._cc.canMerge()) {
            return false;
        }

        if (concatenate) {
            if (!_nullable) {
                return true;
            }

            if (!fc._nullable) {
                _nullable = false;
            }
        } else {
            if (fc._nullable) {
                _nullable = true;
            }
        }

        _caseInsensitive |= fc._caseInsensitive;
        _cc.addCharClass(fc._cc);
        return true;
    }

    String getFirstChars(Locale culture) {
        if (_caseInsensitive) {
            _cc.addLowercase(culture);
        }

        return _cc.toString();
    }

    boolean isCaseInsensitive() {
        return _caseInsensitive;
    }
}

final class RegexPrefix {
    String _prefix;
    boolean _caseInsensitive;

    static RegexPrefix _empty = new RegexPrefix("", /*String.Empty,*/ false);

    RegexPrefix(String prefix, boolean ci) {
        _prefix = prefix;
        _caseInsensitive = ci;
    }

    String prefix() {
        return _prefix;
    }

    boolean isCaseInsensitive() {
        return _caseInsensitive;
    }
}