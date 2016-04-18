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
 * <p>A {@code RegexOptions} value can be provided as a parameter to the following members of the
 * Regex class:</p>
 * <ul>
 *     <li>The {@link Regex#Regex(String, int)} class constructor.</li>
 *     <li>The {@link Regex#split(String, String, int)} method.</li>
 *     <li>The {@link Regex#isMatch(String, String, int)} method.</li>
 *     <li>The {@link Regex#match(String, String, int)} method.</li>
 *     <li>The {@link Regex#matches(String, String, int)} method.</li>
 *     <li>The {@link Regex#replace(String, String, String, int)} and
 *     {@link Regex#replace(String, String, MatchEvaluator, int)} methods.</li>
 * </ul>
 *
 * <p>A {@code RegexOptions} value can also be assigned directly to the Options property. Several
 * options provided by members of the {@code RegexOptions} enumeration (in particular, by its
 * {@link #ExplicitCapture}, {@link #IgnoreCase}, {@link #Multiline}, and {@link #Singleline}
 * members) can instead be provided by using an inline option character in the regular expression
 * pattern.</p>
 */
public final class RegexOptions {
    /**
     * Specifies that no options are set.
     */
    public static final int None = 0x0000;

    /**
     * Specifies case-insensitive matching.
     */
    public static final int IgnoreCase = 0x0001;                   // "i"

    /**
     * Multiline mode. Changes the meaning of ^ and $ so they match at the beginning and end,
     * respectively, of any line, and not just the beginning and end of the entire string.
     */
    public static final int Multiline = 0x0002;                    // "m"

    /**
     * Specifies that the only valid captures are explicitly named or numbered groups of the form
     * (?<name>…). This allows unnamed parentheses to act as noncapturing groups without the
     * syntactic clumsiness of the expression (?:…).
     */
    public static final int ExplicitCapture = 0x0004;              // "n"

    /**
     * Specifies that the regular expression is compiled to an assembly. This yields faster
     * execution but increases startup time. This value should not be assigned to the Options
     * property when calling the CompileToAssembly method. For more information, see the
     * "Compiled Regular Expressions" section in the Regular Expression Options topic.
     *
     * TODO: unsupported in java port version
     */
    // public static final int Compiled = 0x0008;                     // "c"

    /**
     * Specifies single-line mode. Changes the meaning of the dot (.) so it matches every character
     * (instead of every character except \n).
     */
    public static final int Singleline = 0x0010;                   // "s"

    /**
     * Eliminates unescaped white space from the pattern and enables comments marked with #.
     * However, this value does not affect or eliminate white space in , numeric , or tokens that
     * mark the beginning of individual .
     */
    public static final int IgnorePatternWhitespace = 0x0020;      // "x"

    /**
     * Specifies that the search will be from right to left instead of from left to right.
     */
    public static final int RightToLeft = 0x0040;                  // "r"

    /**
     * Enable debugging or not.
     */
    public static final int Debug = 0x0080;                        // "d"

    /**
     * Enables ECMAScript-compliant behavior for the expression. This value can be used only in
     * conjunction with the IgnoreCase, Multiline, and Compiled values. The use of this value with
     * any other values results in an exception.
     */
    public static final int ECMAScript = 0x0100;                   // "e"

    /**
     * Specifies that cultural differences in language is ignored.
     */
    public static final int CultureInvariant = 0x0200;             // 10,0000,0000 (10bits)

    static final int MaxOptionShift = 10;
}