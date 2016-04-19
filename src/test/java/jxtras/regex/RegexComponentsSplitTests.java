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

import jxtras.regex.support.Strings;
import jxtras.regex.support.Assert;
import jxtras.regex.support.Fact;

public class RegexComponentsSplitTests {
    @Fact
    public static void RegexComponentsSplit() {
        //////////// Global Variables used for all tests
        String strLoc = "Loc_000oo";
        String strValue = Strings.EMPTY;
        int iCountErrors = 0;
        int iCountTestcases = 0;
        try {
            /////////////////////////  START TESTS ////////////////////////////
            ///////////////////////////////////////////////////////////////////
            for (int i = 0; i < s_regexTests.length; i++) {
                iCountTestcases++;
                if (!s_regexTests[i].Run()) {
                    System.out.printf("Err_79872asnko! Test {%d} FAILED Pattern={%s}, Input={%s}\n", i, s_regexTests[i].Pattern(), s_regexTests[i].Input());
                    iCountErrors++;
                }
            }
            ///////////////////////////////////////////////////////////////////
            /////////////////////////// END TESTS /////////////////////////////
        } catch (Exception exc_general) {
            ++iCountErrors;
            System.out.println("Error Err_8888yyy!  strLoc==" + strLoc + ", exc_general==" + exc_general.toString());
        }

        ////  Finish Diagnostics
        Assert.Equal(0, iCountErrors);
    }

    private static RegexComponentsSplitTestCase[] s_regexTests = new RegexComponentsSplitTestCase[]{
            /*********************************************************
             ValidCases
             *********************************************************/
            new RegexComponentsSplitTestCase("(\\s)?(-)", "once -upon-a time", new String[]
                    {
                            "once", " ", "-", "upon", "-", "a time"
                    }

            ),
            new RegexComponentsSplitTestCase("(\\s)?(-)", "once upon a time", new String[]
                    {
                            "once upon a time"
                    }

            ),
            new RegexComponentsSplitTestCase("(\\s)?(-)", "once - -upon- a- time", new String[]
                    {
                            "once", " ", "-", "", " ", "-", "upon", "-", " a", "-", " time"
                    }

            ),
            new RegexComponentsSplitTestCase("a(.)c(.)e", "123abcde456aBCDe789", new String[]
                    {
                            "123", "b", "d", "456aBCDe789"
                    }

            ),
            new RegexComponentsSplitTestCase("a(.)c(.)e", RegexOptions.IgnoreCase, "123abcde456aBCDe789", new String[]
                    {
                            "123", "b", "d", "456", "B", "D", "789"
                    }

            ),
            new RegexComponentsSplitTestCase("a(?<dot1>.)c(.)e", "123abcde456aBCDe789", new String[]
                    {
                            "123", "d", "b", "456aBCDe789"
                    }

            ),
            new RegexComponentsSplitTestCase("a(?<dot1>.)c(.)e", RegexOptions.IgnoreCase, "123abcde456aBCDe789", new String[]
                    {
                            "123", "d", "b", "456", "D", "B", "789"
                    }

            ),
            /*********************************************************
            RightToLeft
            *********************************************************/
            new RegexComponentsSplitTestCase("a(.)c(.)e", RegexOptions.RightToLeft, "123abcde456aBCDe789", new String[]
                    {
                            "123", "d", "b", "456aBCDe789"
                    }

            ),
            new RegexComponentsSplitTestCase("a(.)c(.)e", RegexOptions.IgnoreCase | RegexOptions.RightToLeft, "123abcde456aBCDe789", new String[]
                    {
                            "123", "d", "b", "456", "D", "B", "789"
                    }

            ),
            new RegexComponentsSplitTestCase("a(?<dot1>.)c(.)e", RegexOptions.RightToLeft, "123abcde456aBCDe789", new String[]
                    {
                            "123", "b", "d", "456aBCDe789"
                    }

            ),
            new RegexComponentsSplitTestCase("a(?<dot1>.)c(.)e", RegexOptions.RightToLeft | RegexOptions.IgnoreCase, "123abcde456aBCDe789", new String[]
                    {
                            "123", "b", "d", "456", "B", "D", "789"
                    }

            ),
    };

    static class RegexComponentsSplitTestCase {
        private String _pattern;
        private String _input;
        private int _options;
        private String[] _expectedResult;

        public RegexComponentsSplitTestCase(String pattern, String input, String[] expectedResult) {
            this(pattern, RegexOptions.None, input, expectedResult);
        }

        public RegexComponentsSplitTestCase(String pattern, int options, String input, String[] expectedResult) {
            _pattern = pattern;
            _options = options;
            _input = input;
            _expectedResult = expectedResult;
        }

        public String Pattern() {

            return _pattern;

        }

        public String Input() {

            return _input;
        }

        public int Options() {

            return _options;
        }

        public String[] ExpectedResult() {

            return _expectedResult;
        }

        public boolean ExpectSuccess() {

            return null != _expectedResult && _expectedResult.length != 0;
        }

        public boolean Run() {
            Regex r;
            String[] result = null;
            r = new Regex(_pattern, _options);
            try {
                result = r.split(_input);
            } catch (Exception e) {
                System.out.printf("Err_78394ayuua! Expected no exception to be thrown and the following was thrown: \n{%s}", e);
                return false;
            }

            if (result.length != _expectedResult.length) {
                System.out.printf("Err_13484pua! Expected string[].Length and actual differ expected={%d} actual={%d}", _expectedResult.length, result.length);
                return false;
            }

            for (int i = 0; i < _expectedResult.length; i++) {
                if (!result[i].equals(_expectedResult[i])) {
                    System.out.printf("Err_6897nzxn! Expected result and actual result differ expected='{%s}' actual='{%s}' at {%d}", _expectedResult[i], result[i], i);
                    return false;
                }
            }

            return true;
        }
    }
}
