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

public final class Strings {


    public static final String EMPTY = "";

    public static final boolean isNullOrEmpty(String value) {
        return (value == null || value.length() == 0);
    }

    public static final String substring(String value, int index, int length) {
        return value.substring(index, index + length);
    }

    public static final String substring(String value, int index) {
        return value.substring(index);
    }

    // Creates an array of strings by splitting this string at each
    // occurrence of a separator.  The separator is searched for, and if found,
    // the substring preceding the occurrence is stored as the first element in
    // the array of strings.  We then continue in this manner by searching
    // the substring that follows the occurrence.  On the other hand, if the separator
    // is not found, the array of strings will contain this instance as its only element.
    // If the separator is null
    // whitespace (i.e., Characters.isWhitespace) is used as the separator.
    //
    public static String[] split(String source, char[] separator) {
         return splitInternal(source, separator, Integer.MAX_VALUE, StringSplitOptions.None);
    }

    // Creates an array of strings by splitting this string at each
    // occurrence of a separator.  The separator is searched for, and if found,
    // the substring preceding the occurrence is stored as the first element in
    // the array of strings.  We then continue in this manner by searching
    // the substring that follows the occurrence.  On the other hand, if the separator
    // is not found, the array of strings will contain this instance as its only element.
    // If the separator is the empty string (i.e., String.Empty), then
    // whitespace (i.e., Character.IsWhitespace) is used as the separator.
    // If there are more than count different strings, the last n-(count-1)
    // elements are concatenated and added as the last String.
    //
    public static String[] split(String source, char[] separator, int count) {
        return splitInternal(source, separator, count, StringSplitOptions.None);
    }

    public static String[] split(String source, char[] separator, StringSplitOptions options) {
        return splitInternal(source, separator, Integer.MAX_VALUE, options);
    }

    public static String[] split(String source, char[] separator, int count, StringSplitOptions options) {
        return splitInternal(source, separator, count, options);
    }

    static String[] splitInternal(String source, char[] separator, int count, StringSplitOptions options) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be less than zero.");
        }

        boolean omitEmptyEntries = (options == StringSplitOptions.RemoveEmptyEntries);

        if ((count == 0) || (omitEmptyEntries && source.length() == 0)) {
            return new String[0];
        }

        int[] sepList = new int[source.length()];
        int numReplaces = makeSeparatorList(source, separator, sepList);

        // Handle the special case of no replaces and special count.
        if (0 == numReplaces || count == 1) {
            String[] stringArray = new String[1];
            stringArray[0] = source;
            return stringArray;
        }

        if (omitEmptyEntries) {
            return internalSplitOmitEmptyEntries(source, sepList, null, numReplaces, count);
        } else {
            return internalSplitKeepEmptyEntries(source, sepList, null, numReplaces, count);
        }
    }

    public static String[] split(String source, String[] separator, StringSplitOptions options) {
        return split(source, separator, Integer.MAX_VALUE, options);
    }

    public static String[] split(String source, String[] separator, int count, StringSplitOptions options) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be less than zero.");
        }

        boolean omitEmptyEntries = (options == StringSplitOptions.RemoveEmptyEntries);

        if (separator == null || separator.length == 0) {
            return splitInternal(source, null, count, options);
        }

        if ((count == 0) || (omitEmptyEntries && source.length() == 0)) {
            return new String[0];
        }

        int[] sepList = new int[source.length()];
        int[] lengthList = new int[source.length()];
        int numReplaces = makeSeparatorList(source, separator, sepList, lengthList);

        // Handle the special case of no replaces and special count.
        if (0 == numReplaces || count == 1) {
            String[] stringArray = new String[1];
            stringArray[0] = source;
            return stringArray;
        }

        if (omitEmptyEntries) {
            return internalSplitOmitEmptyEntries(source, sepList, lengthList, numReplaces, count);
        } else {
            return internalSplitKeepEmptyEntries(source, sepList, lengthList, numReplaces, count);
        }
    }

    // Note a few special case in this function:
    //     If there is no separator in the string, a string array which only contains
    //     the original string will be returned regardless of the count.
    //
    private static String[] internalSplitKeepEmptyEntries(String source, int[] sepList, int[] lengthList, int numReplaces, int count) {
        if (numReplaces < 0) {
            throw new IllegalArgumentException("numReplaces cannot be less than zero.");
        }

        if (count < 2) {
            throw new IllegalArgumentException("count cannot be less than 2.");
        }

        int currIndex = 0;
        int arrIndex = 0;

        count--;
        int numActualReplaces = (numReplaces < count) ? numReplaces : count;

        // Allocate space for the new array.
        // +1 for the string from the end of the last replace to the end of the String.
        String[] splitStrings = new String[numActualReplaces + 1];

        for (int i = 0; i < numActualReplaces && currIndex < source.length(); i++) {
            splitStrings[arrIndex++] = substring(source, currIndex, sepList[i] - currIndex);
            currIndex = sepList[i] + ((lengthList == null) ? 1 : lengthList[i]);
        }

        // Handle the last string at the end of the array if there is one.
        if (currIndex < source.length() && numActualReplaces >= 0) {
            splitStrings[arrIndex] = substring(source, currIndex);
        } else if (arrIndex == numActualReplaces) {
            // We had a separator character at the end of a string.  Rather than just allowing
            // a null character, we'll replace the last element in the array with an empty string.
            splitStrings[arrIndex] = EMPTY;

        }

        return splitStrings;
    }

    // This function will not keep the Empty String
    private static String[] internalSplitOmitEmptyEntries(String source, int[] sepList, int[] lengthList, int numReplaces, int count) {
        if (numReplaces < 0) {
            throw new IllegalArgumentException("numReplaces cannot be less than zero.");
        }

        if (count < 2) {
            throw new IllegalArgumentException("count cannot be less than 2.");
        }

        // Allocate array to hold items. This array may not be
        // filled completely in this function, we will create a
        // new array and copy string references to that new array.
        int maxItems = (numReplaces < count) ? (numReplaces + 1) : count;
        String[] splitStrings = new String[maxItems];

        int currIndex = 0;
        int arrIndex = 0;

        for (int i = 0; i < numReplaces && currIndex < source.length(); i++) {
            if (sepList[i] - currIndex > 0) {
                splitStrings[arrIndex++] = substring(source, currIndex, sepList[i] - currIndex);
            }
            currIndex = sepList[i] + ((lengthList == null) ? 1 : lengthList[i]);
            if (arrIndex == count - 1) {
                // If all the remaining entries at the end are empty, skip them
                while (i < numReplaces - 1 && currIndex == sepList[++i]) {
                    currIndex += ((lengthList == null) ? 1 : lengthList[i]);
                }
                break;
            }
        }

        // we must have at least one slot left to fill in the last string.
        // Contract.Assert(arrIndex < maxItems);

        // Handle the last string at the end of the array if there is one.
        if (currIndex < source.length()) {
            splitStrings[arrIndex++] = substring(source, currIndex);
        }

        String[] stringArray = splitStrings;
        if (arrIndex != maxItems) {
            stringArray = new String[arrIndex];
            for (int j = 0; j < arrIndex; j++) {
                stringArray[j] = splitStrings[j];
            }
        }
        return stringArray;
    }

    //--------------------------------------------------------------------
    // This function returns number of the places within baseString where
    // instances of characters in Separator occur.
    // Args: separator  -- A string containing all of the split characters.
    //       sepList    -- an array of ints for split char indicies.
    //--------------------------------------------------------------------
    private static int makeSeparatorList(String source, char[] separator, int[] sepList) {
        int foundCount = 0;

        if (separator == null || separator.length == 0) {
            // If they passed null or an empty string, look for whitespace.
            for (int i = 0; i < source.length() && foundCount < sepList.length; i++) {
                if (Characters.isWhiteSpace(source.charAt(i))) {
                    sepList[foundCount++] = i;
                }
            }
        } else {
            int sepListCount = sepList.length;
            int sepCount = separator.length;
            // If they passed in a string of chars, actually look for those chars.
            for (int i = 0; i < source.length() && foundCount < sepListCount; i++) {
                for (int j = 0, k = 0; j < sepCount; j++, k++) {
                    if (source.charAt(i) == separator[k]) {
                        sepList[foundCount++] = i;
                        break;
                    }
                }
            }
        }
        return foundCount;
    }

    //--------------------------------------------------------------------
    // This function returns number of the places within baseString where
    // instances of separator strings occur.
    // Args: separators -- An array containing all of the split strings.
    //       sepList    -- an array of ints for split string indicies.
    //       lengthList -- an array of ints for split string lengths.
    //--------------------------------------------------------------------
    private static int makeSeparatorList(String source, String[] separators, int[] sepList, int[] lengthList) {
        if (separators == null || separators.length == 0) {
            throw new IllegalArgumentException("separators can not be null or empty.");
        }

        int foundCount = 0;
        int sepListCount = sepList.length;
        int sepCount = separators.length;

        for (int i = 0; i < source.length() && foundCount < sepListCount; i++) {
            for (int j = 0; j < separators.length; j++) {
                String separator = separators[j];
                if (isNullOrEmpty(separator)) {
                    continue;
                }
                int currentSepLength = separator.length();
                if (source.charAt(i) == separator.charAt(0) && currentSepLength <= source.length() - i) {
                    if (currentSepLength == 1
                            || compareOrdinal(source, i, separator, 0, currentSepLength) == 0) {
                        sepList[foundCount] = i;
                        lengthList[foundCount] = currentSepLength;
                        foundCount++;
                        i += currentSepLength - 1;
                        break;
                    }
                }
            }
        }
        return foundCount;
    }

    public static int compareOrdinal(String strA, int indexA, String strB, int indexB, int length) {
        return compareOrdinal(strA, indexA, length, strB, indexB, length);
    }

    public static int compareOrdinal(String strA, String strB) {
        return compareOrdinal(strA, 0, strA.length(), strB, 0, strB.length());
    }

    public static final int compareOrdinal(String strA, int indexA, int lenA, String strB, int indexB, int lenB) {
        if (strA == null) {
            return strB == null ? 0 : -1;
        }
        if (strB == null) {
            return 1;
        }
        int lengthA = Math.min(lenA, strA.length() - indexA);
        int lengthB = Math.min(lenB, strB.length() - indexB);

        if (lengthA == lengthB && indexA == indexB && strA == strB)
            return 0;

        int ap = 0 + indexA;
        int end = ap + Math.min(lengthA, lengthB);
        int bp = 0 + indexB;
        while (ap < end) {
            char a = strA.charAt(ap);
            char b = strB.charAt(bp);
            if (a != b)
                return a - b;
            ap++;
            bp++;
        }
        return lengthA - lengthB;
    }
}