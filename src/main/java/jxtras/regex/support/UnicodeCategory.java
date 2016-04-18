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

package jxtras.regex.support;

/**
 * Defines the Unicode category of a character.
 * <p/>
 * This enumeration is based on The Unicode Standard, version 5.0. For more information, see the
 * "UCD File Format" and "General Category Values" subtopics at the Unicode Character Database.
 * <p/>
 * The Unicode Standard defines the following:
 * <p/>
 * A surrogate pair is a coded character representation for a single abstract character that
 * consists of a sequence of two code units, where the first unit of the pair is a high surrogate
 * and the second is a low surrogate. A high surrogate is a Unicode code point in the range U+D800
 * through U+DBFF and a low surrogate is a Unicode code point in the range U+DC00 through U+DFFF.
 * <p/>
 * A combining character sequence is a combination of a base character and one or more combining
 * characters. A surrogate pair represents a base character or a combining character. A combining
 * character is either spacing or nonspacing. A spacing combining character takes up a spacing
 * position by itself when rendered, while a nonspacing combining character does not. Diacritics
 * are an example of nonspacing combining characters.
 * <p/>
 * A modifier letter is a free-standing spacing character that, like a combining character,
 * indicates modifications of a preceding letter.
 * <p/>
 * An enclosing mark is a nonspacing combining character that surrounds all previous characters up
 * to and including a base character.
 * <p/>
 * A format character is a character that is not normally rendered but that affects the layout of
 * text or the operation of text processes.
 * <p/>
 * The Unicode Standard defines several variations to some punctuation marks. For example, a hyphen
 * can be one of several code values that represent a hyphen, such as U+002D (hyphen-minus)
 * or U+00AD (soft hyphen) or U+2010 (hyphen) or U+2011 (nonbreaking hyphen). The same is true for
 * dashes, space characters, and quotation marks.
 * <p/>
 * The Unicode Standard also assigns codes to representations of decimal digits that are specific
 * to a given script or language, for example, U+0030 (digit zero)
 * and U+0660 (Arabic-Indic digit zero).
 */
public final class UnicodeCategory {
    /**
     * Uppercase letter.
     * <p/>
     * Signified by the Unicode designation "Lu" (letter, uppercase).
     */
    public static final int UppercaseLetter = 0;

    /**
     * Lowercase letter.
     * <p/>
     * Signified by the Unicode designation "Ll" (letter, lowercase).
     */
    public static final int LowercaseLetter = 1;

    /**
     * Titlecase letter.
     * <p/>
     * Signified by the Unicode designation "Lt" (letter, titlecase).
     */
    public static final int TitlecaseLetter = 2;

    /**
     * Modifier letter character, which is free-standing spacing character that indicates
     * modifications of a preceding letter.
     * <p/>
     * Signified by the Unicode designation "Lm" (letter, modifier).
     */
    public static final int ModifierLetter = 3;

    /**
     * Letter that is not an uppercase letter, a lowercase letter, a titlecase letter, or a
     * modifier letter.
     * <p/>
     * Signified by the Unicode designation "Lo" (letter, other).
     */
    public static final int OtherLetter = 4;

    /**
     * Nonspacing character that indicates modifications of a base character.
     * <p/>
     * Signified by the Unicode designation "Mn" (mark, nonspacing).
     */
    public static final int NonSpacingMark = 5;

    /**
     * Spacing character that indicates modifications of a base character and affects the width of
     * the glyph for that base character.
     * <p/>
     * Signified by the Unicode designation "Mc" (mark, spacing combining).
     */
    public static final int SpacingCombiningMark = 6;

    /**
     * Enclosing mark character, which is a nonspacing combining character that surrounds all
     * previous characters up to and including a base character.
     * <p/>
     * Signified by the Unicode designation "Me" (mark, enclosing).
     */
    public static final int EnclosingMark = 7;

    /**
     * Decimal digit character, that is, a character in the range 0 through 9.
     * <p/>
     * Signified by the Unicode designation "Nd" (number, decimal digit).
     */
    public static final int DecimalDigitNumber = 8;

    /**
     * Number represented by a letter, instead of a decimal digit, for example, the Roman numeral
     * for five, which is "V".
     * <p/>
     * Signified by the Unicode designation "Nl" (number, letter).
     */
    public static final int LetterNumber = 9;

    /**
     * Number that is neither a decimal digit nor a letter number, for example, the fraction 1/2.
     * <p/>
     * Signified by the Unicode designation "No" (number, other).
     */
    public static final int OtherNumber = 10;

    /**
     * Space character, which has no glyph but is not a control or format character.
     * <p/>
     * Signified by the Unicode designation "Zs" (separator, space).
     */
    public static final int SpaceSeparator = 11;

    /**
     * Character that is used to separate lines of text.
     * <p/>
     * Signified by the Unicode designation "Zl" (separator, line).
     */
    public static final int LineSeparator = 12;

    /**
     * Character used to separate paragraphs.
     * <p/>
     * Signified by the Unicode designation "Zp" (separator, paragraph).
     */
    public static final int ParagraphSeparator = 13;

    /**
     * Control code character, with a Unicode value of U+007F or in the range U+0000 through U+001F
     * or U+0080 through U+009F.
     * <p/>
     * Signified by the Unicode designation "Cc" (other, control).
     */
    public static final int Control = 14;

    /**
     * Format character that affects the layout of text or the operation of text processes, but is
     * not normally rendered.
     * <p/>
     * Signified by the Unicode designation "Cf" (other, format).
     */
    public static final int Format = 15;

    /**
     * High surrogate or a low surrogate character. Surrogate code values are in the range U+D800
     * through U+DFFF.
     * <p/>
     * Signified by the Unicode designation "Cs" (other, surrogate).
     */
    public static final int Surrogate = 16;

    /**
     * Private-use character, with a Unicode value in the range U+E000 through U+F8FF.
     * <p/>
     * Signified by the Unicode designation "Co" (other, private use).
     */
    public static final int PrivateUse = 17;

    /**
     * Connector punctuation character that connects two characters.
     * <p/>
     * Signified by the Unicode designation "Pc" (punctuation, connector).
     */
    public static final int ConnectorPunctuation = 18;

    /**
     * Dash or hyphen character.
     * <p/>
     * Signified by the Unicode designation "Pd" (punctuation, dash).
     */
    public static final int DashPunctuation = 19;

    /**
     * Opening character of one of the paired punctuation marks, such as parentheses,
     * square brackets, and braces.
     * <p/>
     * Signified by the Unicode designation "Ps" (punctuation, open).
     */
    public static final int OpenPunctuation = 20;

    /**
     * Closing character of one of the paired punctuation marks, such as parentheses, square
     * brackets, and braces.
     * <p/>
     * Signified by the Unicode designation "Pe" (punctuation, close).
     */
    public static final int ClosePunctuation = 21;

    /**
     * Opening or initial quotation mark character.
     * <p/>
     * Signified by the Unicode designation "Pi" (punctuation, initial quote). T
     */
    public static final int InitialQuotePunctuation = 22;

    /**
     * Closing or final quotation mark character.
     * <p/>
     * Signified by the Unicode designation "Pf" (punctuation, final quote).
     */
    public static final int FinalQuotePunctuation = 23;

    /**
     * Punctuation character that is not a connector, a dash, open punctuation, close punctuation,
     * an initial quote, or a final quote.
     * <p/>
     * Signified by the Unicode designation "Po" (punctuation, other).
     */
    public static final int OtherPunctuation = 24;

    /**
     * Mathematical symbol character, such as "+" or "= ".
     * <p/>
     * Signified by the Unicode designation "Sm" (symbol, math).
     */
    public static final int MathSymbol = 25;

    /**
     * Currency symbol character. Signified by the Unicode designation "Sc" (symbol, currency)
     */
    public static final int CurrencySymbol = 26;

    /**
     * Modifier symbol character, which indicates modifications of surrounding characters.
     * For example, the fraction slash indicates that the number to the left is the numerator and
     * the number to the right is the denominator.
     * <p/>
     * Signified by the Unicode designation "Sk" (symbol, modifier).
     */
    public static final int ModifierSymbol = 27;

    /**
     * Symbol character that is not a mathematical symbol, a currency symbol or a modifier symbol.
     * <p/>
     * Signified by the Unicode designation "So" (symbol, other).
     */
    public static final int OtherSymbol = 28;

    /**
     * Character that is not assigned to any Unicode category.
     * <p/>
     * Signified by the Unicode designation "Cn" (other, not assigned).
     */
    public static final int OtherNotAssigned = 29;
}