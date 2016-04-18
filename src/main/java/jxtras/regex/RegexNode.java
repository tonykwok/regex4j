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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// This RegexNode class is  to the Regex package.
// It is built into a parsed tree for a regular expression.

// Implementation notes:
//
// Since the node tree is a temporary data structure only used
// during compilation of the regexp to integer codes, it's
// designed for clarity and convenience rather than
// space efficiency.
//
// RegexNodes are built into a tree, linked by the _children list.
// Each node also has a _parent and _ichild member indicating
// its parent and which child # it is in its parent's list.
//
// RegexNodes come in as many types as there are constructs in
// a regular expression, for example, "concatenate", "alternate",
// "one", "rept", "group". There are also node types for basic
// peephole optimizations, e.g., "onerep", "notsetrep", etc.
//
// Because perl 5 allows "lookback" groups that scan backwards,
// each node also gets a "direction". Normally the value of
// booleanean _backward = false.
//
// During parsing, top-level nodes are also stacked onto a parse
// stack (a stack of trees). For this purpose we have a _next
// pointer. [Note that to save a few bytes, we could overload the
// _parent pointer instead.]
//
// On the parse stack, each tree has a "role" - basically, the
// nonterminal in the grammar that the parser has currently
// assigned to the tree. That code is stored in _role.
//
// Finally, some of the different kinds of nodes have data.
// Two integers (for the looping constructs) are stored in
// _operands, an an object (either a string or a set)
// is stored in _data
final class RegexNode {
    // RegexNode types

    // the following are leaves, and correspond to primitive operations

    // static final int Onerep     = RegexCode.Onerep;     // c,n      a {n}
    // static final int Notonerep  = RegexCode.Notonerep;  // c,n      .{n}
    // static final int Setrep     = RegexCode.Setrep;     // set,n    \d {n}

    static final int Oneloop = RegexCode.Oneloop;                 // c,n      a*
    static final int Notoneloop = RegexCode.Notoneloop;           // c,n      .*
    static final int Setloop = RegexCode.Setloop;                 // set,n    \d*

    static final int Onelazy = RegexCode.Onelazy;                 // c,n      a*?
    static final int Notonelazy = RegexCode.Notonelazy;           // c,n      .*?
    static final int Setlazy = RegexCode.Setlazy;                 // set,n    \d*?

    static final int One = RegexCode.One;                         // char     a
    static final int Notone = RegexCode.Notone;                   // char     . [^a]
    static final int Set = RegexCode.Set;                         // set      [a-z] \w \s \d

    static final int Multi = RegexCode.Multi;                     // string   abcdef
    static final int Ref = RegexCode.Ref;                         // index    \1

    static final int Bol = RegexCode.Bol;                          //          ^
    static final int Eol = RegexCode.Eol;                          //          $
    static final int Boundary = RegexCode.Boundary;                //          \b
    static final int Nonboundary = RegexCode.Nonboundary;          //          \B
    static final int ECMABoundary = RegexCode.ECMABoundary;        // \b
    static final int NonECMABoundary = RegexCode.NonECMABoundary;  // \B
    static final int Beginning = RegexCode.Beginning;              //          \A
    static final int Start = RegexCode.Start;                      //          \G
    static final int EndZ = RegexCode.EndZ;                        //          \Z
    static final int End = RegexCode.End;                          //          \z

    // (note: End               = 21;)

    // interior nodes do not correpond to primitive operations, but
    // control structures compositing other operations

    // concat and alternate take n children, and can run forward or backwards

    static final int Nothing = 22;                                 //          []
    static final int Empty = 23;                                   //          ()

    static final int Alternate = 24;                               //          a|b
    static final int Concatenate = 25;                             //          ab

    static final int Loop = 26;                                    // m,x      * + ? {,}
    static final int Lazyloop = 27;                                // m,x      *? +? ?? {,}?

    static final int Capture = 28;                                 // n        ()
    static final int Group = 29;                                   //          (?:)
    static final int Require = 30;                                 //          (?=) (?<=)
    static final int Prevent = 31;                                 //          (?!) (?<!)
    static final int Greedy = 32;                                  //          (?>) (?<)
    static final int Testref = 33;                                 //          (?(n) | )
    static final int Testgroup = 34;                               //          (?(...) | )

    // RegexNode data members

    int _type;

    List<RegexNode> _children;

    String _str;
    char _ch;
    int _m;
    int _n;
    final int _options;

    RegexNode _next;

    RegexNode(int type, int options) {
        _type = type;
        _options = options;
    }

    RegexNode(int type, int options, char ch) {
        _type = type;
        _options = options;
        _ch = ch;
    }

    RegexNode(int type, int options, String str) {
        _type = type;
        _options = options;
        _str = str;
    }

    RegexNode(int type, int options, int m) {
        _type = type;
        _options = options;
        _m = m;
    }

    RegexNode(int type, int options, int m, int n) {
        _type = type;
        _options = options;
        _m = m;
        _n = n;
    }

    boolean useOptionR() {
        return (_options & RegexOptions.RightToLeft) != 0;
    }

    RegexNode reverseLeft() {
        if (useOptionR() && _type == Concatenate && _children != null) {
            Collections.reverse(_children);
            // TODO: change to related java API?
            // _children.Reverse(0, _children.Count);
        }

        return this;
    }


    // Pass type as OneLazy or OneLoop
    void makeRep(int type, int min, int max) {
        _type += (type - One);
        _m = min;
        _n = max;
    }

    // Removes redundant nodes from the subtree, and returns a reduced subtree.
    RegexNode reduce() {
        RegexNode n;

        switch (type()) {
            case Alternate:
                n = reduceAlternation();
                break;

            case Concatenate:
                n = reduceConcatenation();
                break;

            case Loop:
            case Lazyloop:
                n = reduceRep();
                break;

            case Group:
                n = reduceGroup();
                break;

            case Set:
            case Setloop:
                n = reduceSet();
                break;

            default:
                n = this;
                break;
        }

        return n;
    }


    // Simple optimization. If a concatenation or alternation has only
    // one child strip out the intermediate node. If it has zero children,
    // turn it into an empty.
    RegexNode stripEnation(int emptyType) {
        switch (childCount()) {
            case 0:
                return new RegexNode(emptyType, _options);
            case 1:
                return childAt(0);
            default:
                return this;
        }
    }

    // Simple optimization. Once parsed into a tree, noncapturing groups
    // serve no function, so strip them out.
    RegexNode reduceGroup() {
        RegexNode u;

        for (u = this; u.type() == Group; )
            u = u.childAt(0);

        return u;
    }

    // Nested repeaters just get multiplied with each other if they're not
    // too lumpy
    RegexNode reduceRep() {
        RegexNode u;
        RegexNode child;
        int type;
        int min;
        int max;

        u = this;
        type = type();
        min = _m;
        max = _n;

        for (; ;) {
            if (u.childCount() == 0)
                break;

            child = u.childAt(0);

            // multiply reps of the same type only
            if (child.type() != type) {
                int childType = child.type();

                if (!(childType >= Oneloop && childType <= Setloop && type == Loop ||
                        childType >= Onelazy && childType <= Setlazy && type == Lazyloop))
                    break;
            }

            // child can be too lumpy to blur, e.g., (a {100,105}) {3} or (a {2,})?
            // [but things like (a {2,})+ are not too lumpy...]
            if (u._m == 0 && child._m > 1 || child._n < child._m * 2)
                break;

            u = child;
            if (u._m > 0)
                u._m = min = ((Integer.MAX_VALUE - 1) / u._m < min) ? Integer.MAX_VALUE : u._m * min; // TODO: Int32 -> Integer
            if (u._n > 0)
                u._n = max = ((Integer.MAX_VALUE - 1) / u._n < max) ? Integer.MAX_VALUE : u._n * max; // TODO: Int32 -> Integer
        }

        return min == Integer.MAX_VALUE ? new RegexNode(Nothing, _options) : u; // TODO: Int32 -> Integer
    }

    // Simple optimization. If a set is a singleton, an inverse singleton,
    // or empty, it's transformed accordingly.
    RegexNode reduceSet() {
        // Extract empty-set, one and not-one case as special
        if (RegexCharClass.isEmpty(_str)) {
            _type = Nothing;
            _str = null;
        } else if (RegexCharClass.isSingleton(_str)) {
            _ch = RegexCharClass.singletonChar(_str);
            _str = null;
            _type += (One - Set);
        } else if (RegexCharClass.isSingletonInverse(_str)) {
            _ch = RegexCharClass.singletonChar(_str);
            _str = null;
            _type += (Notone - Set);
        }

        return this;
    }

    // Basic optimization. Single-letter alternations can be replaced
    // by faster set specifications, and nested alternations with no
    // intervening operators can be flattened:
    //
    // a|b|c|def|g|h -> [a-c]|def|[gh]
    // apple|(?:orange|pear)|grape -> apple|orange|pear|grape
    RegexNode reduceAlternation() {
        // Combine adjacent sets/chars

        boolean wasLastSet;
        boolean lastNodeCannotMerge;
        /* RegexOptions */
        int optionsLast;
        /* RegexOptions */
        int optionsAt;
        int i;
        int j;
        RegexNode at;
        RegexNode prev;

        if (_children == null)
            return new RegexNode(RegexNode.Nothing, _options);

        wasLastSet = false;
        lastNodeCannotMerge = false;
        // TODO: change from 0 to NONE
        optionsLast = RegexOptions.None;

        for (i = 0, j = 0; i < _children.size(); i++, j++) {
            at = _children.get(i);

            if (j < i) {
                // TODO: _children[j] = at;
                _children.set(j, at);
            }

            for (; ;) {
                if (at._type == Alternate) {
                    for (int k = 0; k < at._children.size(); k++) {
                        at._children.get(k)._next = this;
                    }
                    // TODO: _children.InsertRange(i + 1, at._children);
                    _children.addAll(i + 1, at._children);
                    j--;
                } else if (at._type == Set || at._type == One) {
                    // Cannot merge sets if L or I options differ, or if either are negated.
                    optionsAt = at._options & (RegexOptions.RightToLeft | RegexOptions.IgnoreCase);


                    if (at._type == Set) {
                        if (!wasLastSet || optionsLast != optionsAt || lastNodeCannotMerge || !RegexCharClass.isMergeable(at._str)) {
                            wasLastSet = true;
                            lastNodeCannotMerge = !RegexCharClass.isMergeable(at._str);
                            optionsLast = optionsAt;
                            break;
                        }
                    } else if (!wasLastSet || optionsLast != optionsAt || lastNodeCannotMerge) {
                        wasLastSet = true;
                        lastNodeCannotMerge = false;
                        optionsLast = optionsAt;
                        break;
                    }


                    // The last node was a Set or a One, we're a Set or One and our options are the same.
                    // Merge the two nodes.
                    j--;
                    prev = _children.get(j);

                    RegexCharClass prevCharClass;
                    if (prev._type == RegexNode.One) {
                        prevCharClass = new RegexCharClass();
                        prevCharClass.addChar(prev._ch);
                    } else {
                        prevCharClass = RegexCharClass.parse(prev._str);
                    }

                    if (at._type == RegexNode.One) {
                        prevCharClass.addChar(at._ch);
                    } else {
                        RegexCharClass atCharClass = RegexCharClass.parse(at._str);
                        prevCharClass.addCharClass(atCharClass);
                    }

                    prev._type = RegexNode.Set;
                    prev._str = prevCharClass.toString();

                } else if (at._type == RegexNode.Nothing) {
                    j--;
                } else {
                    wasLastSet = false;
                    lastNodeCannotMerge = false;
                }
                break;
            }
        }

        if (j < i) {
            // TODO: _children.RemoveRange(j, i - j);
            _children.subList(j, i).clear();
        }

        return stripEnation(RegexNode.Nothing);
    }

    // Basic optimization. Adjacent strings can be concatenated.
    //
    // (?:abc)(?:def) -> abcdef
    RegexNode reduceConcatenation() {
        // Eliminate empties and concat adjacent strings/chars

        boolean wasLastString;
        /* RegexOptions */
        int optionsLast;
        /* RegexOptions */
        int optionsAt;
        int i;
        int j;

        if (_children == null)
            return new RegexNode(RegexNode.Empty, _options);

        wasLastString = false;
        optionsLast = 0;

        for (i = 0, j = 0; i < _children.size(); i++, j++) {
            RegexNode at;
            RegexNode prev;

            at = _children.get(i);

            if (j < i) {
                // TODO: _children[j] = at;
                _children.set(j, at);
            }

            if (at._type == RegexNode.Concatenate &&
                    ((at._options & RegexOptions.RightToLeft) == (_options & RegexOptions.RightToLeft))) {
                for (int k = 0; k < at._children.size(); k++)
                    at._children.get(k)._next = this;

                // TODO: _children.InsertRange(i + 1, at._children);
                _children.addAll(i + 1, at._children);
                j--;
            } else if (at._type == RegexNode.Multi ||
                    at._type == RegexNode.One) {
                // Cannot merge strings if L or I options differ
                optionsAt = at._options & (RegexOptions.RightToLeft | RegexOptions.IgnoreCase);

                if (!wasLastString || optionsLast != optionsAt) {
                    wasLastString = true;
                    optionsLast = optionsAt;
                    continue;
                }

                prev = _children.get(--j);

                if (prev._type == RegexNode.One) {
                    prev._type = RegexNode.Multi;
                    prev._str = String.valueOf(prev._ch); // TODO: Convert.ToString(prev._ch, CultureInfo.InvariantCulture);
                }

                if ((optionsAt & RegexOptions.RightToLeft) == 0) {
                    if (at._type == RegexNode.One)
                        prev._str += at._ch; // TODO: .ToString();
                    else
                        prev._str += at._str;
                } else {
                    if (at._type == RegexNode.One)
                        prev._str = at._ch /* TODO: .ToString() */ + prev._str;
                    else
                        prev._str = at._str + prev._str;
                }

            } else if (at._type == RegexNode.Empty) {
                j--;
            } else {
                wasLastString = false;
            }
        }

        if (j < i)
            // TODO: _children.RemoveRange(j, i - j);
            _children.subList(j, i).clear();

        return stripEnation(RegexNode.Empty);
    }

    RegexNode makeQuantifier(boolean lazy, int min, int max) {
        RegexNode result;

        if (min == 0 && max == 0)
            return new RegexNode(RegexNode.Empty, _options);

        if (min == 1 && max == 1)
            return this;

        switch (_type) {
            case RegexNode.One:
            case RegexNode.Notone:
            case RegexNode.Set:

                makeRep(lazy ? RegexNode.Onelazy : RegexNode.Oneloop, min, max);
                return this;

            default:
                result = new RegexNode(lazy ? RegexNode.Lazyloop : RegexNode.Loop, _options, min, max);
                result.addChild(this);
                return result;
        }
    }

    void addChild(RegexNode newChild) {
        RegexNode reducedChild;

        if (_children == null)
            _children = new ArrayList<RegexNode>(4);

        reducedChild = newChild.reduce();

        _children.add(reducedChild);
        reducedChild._next = this;
    }

    RegexNode childAt(int i) {
        return _children.get(i);
    }

    int childCount() {
        return _children == null ? 0 : _children.size();
    }

    int type() {
        return _type;
    }

    //#if DBG
    static String[] TypeStr = new String[]{
            "Onerep", "Notonerep", "Setrep",
            "Oneloop", "Notoneloop", "Setloop",
            "Onelazy", "Notonelazy", "Setlazy",
            "One", "Notone", "Set",
            "Multi", "Ref",
            "Bol", "Eol", "Boundary", "Nonboundary",
            "ECMABoundary", "NonECMABoundary",
            "Beginning", "Start", "EndZ", "End",
            "Nothing", "Empty",
            "Alternate", "Concatenate",
            "Loop", "Lazyloop",
            "Capture", "Group", "Require", "Prevent", "Greedy",
            "Testref", "Testgroup"};

    String description() {

        StringBuilder ArgSb = new StringBuilder();

        ArgSb.append(TypeStr[_type]);

        if ((_options & RegexOptions.ExplicitCapture) != 0)
            ArgSb.append("-N"); // TODO: change from -C to -N
        if ((_options & RegexOptions.IgnoreCase) != 0)
            ArgSb.append("-I");
        if ((_options & RegexOptions.RightToLeft) != 0)
            ArgSb.append("-R"); // TODO: change from -L to -R
        if ((_options & RegexOptions.Multiline) != 0)
            ArgSb.append("-M");
        if ((_options & RegexOptions.Singleline) != 0)
            ArgSb.append("-S");
        if ((_options & RegexOptions.IgnorePatternWhitespace) != 0)
            ArgSb.append("-X");
        if ((_options & RegexOptions.ECMAScript) != 0)
            ArgSb.append("-E");

        switch (_type) {
            case Oneloop:
            case Notoneloop:
            case Onelazy:
            case Notonelazy:
            case One:
            case Notone:
                ArgSb.append("(Ch = " + RegexCharClass.charDescription(_ch) + ")");
                break;
            case Capture:
                ArgSb.append("(index = " + _m + ", unindex = " + _n + ")");
                break;
            case Ref:
            case Testref:
                ArgSb.append("(index = " + _m + ")");
                break;
            case Multi:
                ArgSb.append("(String = " + _str + ")");
                break;
            case Set:
            case Setloop:
            case Setlazy:
                ArgSb.append("(Set = " + RegexCharClass.setDescription(_str) + ")");
                break;
        }

        switch (_type) {
            case Oneloop:
            case Notoneloop:
            case Onelazy:
            case Notonelazy:
            case Setloop:
            case Setlazy:
            case Loop:
            case Lazyloop:
                ArgSb.append("(Min = " + _m + ", Max = " + (_n == Integer.MAX_VALUE ? "inf" : _n) + ")");
                break;
        }

        return ArgSb.toString();
    }

    static final String Space = "                                ";

    void dump() {
        List<Integer> Stack = new ArrayList<Integer>();
        RegexNode CurNode;
        int CurChild;

        CurNode = this;
        CurChild = 0;

        System.out.println(CurNode.description());

        for (; ;) {
            if (CurNode._children != null && CurChild < CurNode._children.size()) {
                Stack.add(CurChild + 1);
                CurNode = CurNode._children.get(CurChild);
                CurChild = 0;

                int Depth = Stack.size();
                if (Depth > 32)
                    Depth = 32;
                // TODO: Space.substring(0, Depth)
                System.out.println(Space.substring(0, Depth) + CurNode.description());
            } else {
                if (Stack.size() == 0)
                    break;

                CurChild = Stack.get(Stack.size() - 1);
                // TODO: Stack.RemoveAt(Stack.Count - 1);
                Stack.remove(Stack.size() - 1);
                CurNode = CurNode._next;
            }
        }
    }
}