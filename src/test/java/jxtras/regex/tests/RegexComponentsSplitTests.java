package jxtras.regex.tests;

import jxtras.regex.Regex;
import jxtras.regex.RegexOptions;
import jxtras.regex.support.Strings;
import jxtras.regex.tests.support.Assert;
import jxtras.regex.tests.support.Fact;

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
