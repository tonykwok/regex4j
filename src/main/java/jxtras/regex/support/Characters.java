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

public final class Characters {
    // Unicode category values from Unicode U+0000 ~ U+00FF.
    // Store them in byte[] array to save space.
    private static final byte[] categoryForLatin1 = {
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,                 // 0000 - 0007
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,                 // 0008 - 000F
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,                 // 0010 - 0017
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,                 // 0018 - 001F
            (byte) UnicodeCategory.SpaceSeparator,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.CurrencySymbol,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.OtherPunctuation,        // 0020 - 0027
            (byte) UnicodeCategory.OpenPunctuation,
            (byte) UnicodeCategory.ClosePunctuation,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.MathSymbol,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.DashPunctuation,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.OtherPunctuation,        // 0028 - 002F
            (byte) UnicodeCategory.DecimalDigitNumber,
            (byte) UnicodeCategory.DecimalDigitNumber,
            (byte) UnicodeCategory.DecimalDigitNumber,
            (byte) UnicodeCategory.DecimalDigitNumber,
            (byte) UnicodeCategory.DecimalDigitNumber,
            (byte) UnicodeCategory.DecimalDigitNumber,
            (byte) UnicodeCategory.DecimalDigitNumber,
            (byte) UnicodeCategory.DecimalDigitNumber,      // 0030 - 0037
            (byte) UnicodeCategory.DecimalDigitNumber,
            (byte) UnicodeCategory.DecimalDigitNumber,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.MathSymbol,
            (byte) UnicodeCategory.MathSymbol,
            (byte) UnicodeCategory.MathSymbol,
            (byte) UnicodeCategory.OtherPunctuation,        // 0038 - 003F
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,         // 0040 - 0047
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,         // 0048 - 004F
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,         // 0050 - 0057
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.OpenPunctuation,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.ClosePunctuation,
            (byte) UnicodeCategory.ModifierSymbol,
            (byte) UnicodeCategory.ConnectorPunctuation,    // 0058 - 005F
            (byte) UnicodeCategory.ModifierSymbol,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,         // 0060 - 0067
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,         // 0068 - 006F
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,         // 0070 - 0077
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.OpenPunctuation,
            (byte) UnicodeCategory.MathSymbol,
            (byte) UnicodeCategory.ClosePunctuation,
            (byte) UnicodeCategory.MathSymbol,
            (byte) UnicodeCategory.Control,                 // 0078 - 007F
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,                 // 0080 - 0087
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,                 // 0088 - 008F
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,                 // 0090 - 0097
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,
            (byte) UnicodeCategory.Control,                 // 0098 - 009F
            (byte) UnicodeCategory.SpaceSeparator,
            (byte) UnicodeCategory.OtherPunctuation,
            (byte) UnicodeCategory.CurrencySymbol,
            (byte) UnicodeCategory.CurrencySymbol,
            (byte) UnicodeCategory.CurrencySymbol,
            (byte) UnicodeCategory.CurrencySymbol,
            (byte) UnicodeCategory.OtherSymbol,
            (byte) UnicodeCategory.OtherSymbol,             // 00A0 - 00A7
            (byte) UnicodeCategory.ModifierSymbol,
            (byte) UnicodeCategory.OtherSymbol,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.InitialQuotePunctuation,
            (byte) UnicodeCategory.MathSymbol,
            (byte) UnicodeCategory.DashPunctuation,
            (byte) UnicodeCategory.OtherSymbol,
            (byte) UnicodeCategory.ModifierSymbol,          // 00A8 - 00AF
            (byte) UnicodeCategory.OtherSymbol,
            (byte) UnicodeCategory.MathSymbol,
            (byte) UnicodeCategory.OtherNumber,
            (byte) UnicodeCategory.OtherNumber,
            (byte) UnicodeCategory.ModifierSymbol,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.OtherSymbol,
            (byte) UnicodeCategory.OtherPunctuation,        // 00B0 - 00B7
            (byte) UnicodeCategory.ModifierSymbol,
            (byte) UnicodeCategory.OtherNumber,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.FinalQuotePunctuation,
            (byte) UnicodeCategory.OtherNumber,
            (byte) UnicodeCategory.OtherNumber,
            (byte) UnicodeCategory.OtherNumber,
            (byte) UnicodeCategory.OtherPunctuation,        // 00B8 - 00BF
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,         // 00C0 - 00C7
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,         // 00C8 - 00CF
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.MathSymbol,              // 00D0 - 00D7
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.UppercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,         // 00D8 - 00DF
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,         // 00E0 - 00E7
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,         // 00E8 - 00EF
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.MathSymbol,            // 00F0 - 00F7
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,
            (byte) UnicodeCategory.LowercaseLetter,         // 00F8 - 00FF
    };

    public static int getUnicodeCategory(char c) {
        if (isLatin1(c)) {
            return getLatin1UnicodeCategory(c);
        }

        return internalGetCategoryValue(c);
    }

    static byte internalGetCategoryValue(int c) {
        if (c < 0 || c > 0x10ffff) {
            throw new IllegalArgumentException("c is not in valid Unicode range.");
        }

        byte category = UnicodeCategory.OtherNotAssigned;
        int type = Character.getType(c);
        switch (type) {
            case Character.UNASSIGNED:
                category = UnicodeCategory.OtherNotAssigned;
                break;
            case Character.UPPERCASE_LETTER:
                category = UnicodeCategory.UppercaseLetter;
                break;
            case Character.LOWERCASE_LETTER:
                category = UnicodeCategory.LowercaseLetter;
                break;
            case Character.TITLECASE_LETTER:
                category = UnicodeCategory.TitlecaseLetter;
                break;
            case Character.MODIFIER_LETTER:
                category = UnicodeCategory.ModifierLetter;
                break;
            case Character.OTHER_LETTER:
                category = UnicodeCategory.OtherLetter;
                break;
            case Character.NON_SPACING_MARK:
                category = UnicodeCategory.NonSpacingMark;
                break;
            case Character.ENCLOSING_MARK:
                category = UnicodeCategory.EnclosingMark;
                break;
            case Character.COMBINING_SPACING_MARK:
                category = UnicodeCategory.SpacingCombiningMark;
                break;
            case Character.DECIMAL_DIGIT_NUMBER:
                category = UnicodeCategory.DecimalDigitNumber;
                break;
            case Character.LETTER_NUMBER:
                category = UnicodeCategory.LetterNumber;
                break;
            case Character.OTHER_NUMBER:
                category = UnicodeCategory.OtherNumber;
                break;
            case Character.SPACE_SEPARATOR:
                category = UnicodeCategory.SpaceSeparator;
                break;
            case Character.LINE_SEPARATOR:
                category = UnicodeCategory.LineSeparator;
                break;
            case Character.PARAGRAPH_SEPARATOR:
                category = UnicodeCategory.ParagraphSeparator;
                break;
            case Character.CONTROL:
                category = UnicodeCategory.Control;
                break;
            case Character.FORMAT:
                category = UnicodeCategory.Format;
                break;
            case Character.PRIVATE_USE:
                category = UnicodeCategory.PrivateUse;
                break;
            case Character.SURROGATE:
                category = UnicodeCategory.Surrogate;
                break;
            case Character.DASH_PUNCTUATION:
                category = UnicodeCategory.DashPunctuation;
                break;
            // TODO: is this mapping correct?
            case Character.START_PUNCTUATION:
                category = UnicodeCategory.OpenPunctuation;
                break;
            // TODO: is this mapping correct?
            case Character.END_PUNCTUATION:
                category = UnicodeCategory.ClosePunctuation;
                break;
            case Character.CONNECTOR_PUNCTUATION:
                break;
            case Character.OTHER_PUNCTUATION:
                break;
            case Character.MATH_SYMBOL:
                category = UnicodeCategory.MathSymbol;
                break;
            case Character.CURRENCY_SYMBOL:
                category = UnicodeCategory.CurrencySymbol;
                break;
            case Character.MODIFIER_SYMBOL:
                category = UnicodeCategory.ModifierSymbol;
                break;
            case Character.OTHER_SYMBOL:
                category = UnicodeCategory.OtherSymbol;
                break;
            case Character.INITIAL_QUOTE_PUNCTUATION:
                category = UnicodeCategory.InitialQuotePunctuation;
                break;
            case Character.FINAL_QUOTE_PUNCTUATION:
                category = UnicodeCategory.FinalQuotePunctuation;
                break;
            default:
                throw new IllegalStateException("Incompatible Unicode category.");
        }

        // Make sure that OtherNotAssigned is the last category in UnicodeCategory.
        // If that changes, change the following assertion as well.
        if (category < 0 || category > UnicodeCategory.OtherNotAssigned) {
            throw new IllegalStateException("Unicode category is not in valid range.");
        }
        return category;
    }

    // Return true for all characters below or equal U+00ff, which is ASCII + Latin-1 Supplement.
    private static boolean isLatin1(char c) {
        return c <= 0x00ff;
    }

    private static int getLatin1UnicodeCategory(char c) {
        return categoryForLatin1[(int) c];
    }

    public static boolean isWhiteSpace(char c) {
        if (isLatin1(c)) {
            return (isWhiteSpaceLatin1(c));
        }

        return internalIsWhiteSpace(c);
    }

    static boolean internalIsWhiteSpace(char c) {
        int category = getUnicodeCategory(c);
        // In Unicode 3.0, U+2028 is the only character which is under the category "LineSeparator".
        // And U+2029 is the only character which is under the category "ParagraphSeparator".
        switch (category) {
            case UnicodeCategory.SpaceSeparator:
            case UnicodeCategory.LineSeparator:
            case UnicodeCategory.ParagraphSeparator:
                return true;
        }

        return false;
    }

    private static boolean isWhiteSpaceLatin1(char c) {
        // There are characters which belong to UnicodeCategory.Control but are considered as white spaces.
        // We use code point comparisons for these characters here as a temporary fix.

        // U+0009 = <control> HORIZONTAL TAB
        // U+000a = <control> LINE FEED
        // U+000b = <control> VERTICAL TAB
        // U+000c = <contorl> FORM FEED
        // U+000d = <control> CARRIAGE RETURN
        // U+0085 = <control> NEXT LINE
        // U+00a0 = NO-BREAK SPACE
        if ((c == ' ') || (c >= '\u0009' && c <= '\n' /* '\x000d' */) || c == '\u00A0' || c == '\u0085') {
            return true;
        }
        return false;
    }
}