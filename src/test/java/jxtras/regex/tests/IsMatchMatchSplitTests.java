package jxtras.regex.tests;

import jxtras.regex.Regex;
import jxtras.regex.RegexOptions;
import jxtras.regex.support.Strings;
import jxtras.regex.tests.support.Assert;
import jxtras.regex.tests.support.Fact;

public class IsMatchMatchSplitTests {
    /*
    Tested Methods:
        public static Boolean IsMatch() variants
        public static Match Match(string input, String pattern, String options);     "abc","[aBc]","i"
        public static String[] Split(string input, String pattern, String options);     "[abc]", "i"
        "1A2B3C4"
    */

    @Fact
    public static void IsMatchMatchSplit() {
        //////////// Global Variables used for all tests
        String strLoc = "Loc_000oo";
        String strValue = Strings.EMPTY;
        int iCountErrors = 0;
        int iCountTestcases = 0;
        String s;
        String[] sa;
        String[] saExp1 =
                {
                        "a", "b", "c"
                }

                ;
        String[] saExp2 =
                {
                        "1", "2", "3", "4"
                }

                ;
        int i = 0;
        try
        {
            /////////////////////////  START TESTS ////////////////////////////
            ///////////////////////////////////////////////////////////////////
            // [] public static Boolean IsMatch() variants
            //-----------------------------------------------------------------
            strLoc = "Loc_498yg";
            iCountTestcases++;
            if (!Regex.isMatch("abc", "abc"))
            {
                iCountErrors++;
                System.out.println("Err_234fsadg! doesnot match");
            }

            if (!Regex.isMatch("abc", "aBc", RegexOptions.IgnoreCase))
            {
                iCountErrors++;
                System.out.println("Err_7432rwe! doesnot match");
            }

            if (!Regex.isMatch("abc", "aBc", RegexOptions.IgnoreCase))
            {
                iCountErrors++;
                System.out.println("Err_7432rwe! doesnot match");
            }

            strLoc = "Loc_0003";
            iCountTestcases++;
            // [] Scenario 3
            //-----------------------------------------------------------------
            strLoc = "Loc_746tegd";
            iCountTestcases++;
            s = "1A2B3C4";
            sa = Regex.split(s, "[abc]", RegexOptions.IgnoreCase);
            for (i = 0; i < sa.length; i++)
            {
                if (!saExp2[i].equals(sa[i]))
                {
                    iCountErrors++;
                    System.out.println("Err_452wfdf! doesnot match");
                }
            }
            ///////////////////////////////////////////////////////////////////
            /////////////////////////// END TESTS /////////////////////////////
        }
        catch (Exception exc_general)
        {
            ++iCountErrors;
            System.out.println("Error Err_8888yyy!  strLoc==" + strLoc + ", exc_general==" + exc_general.toString());
        }

        ////  Finish Diagnostics
        Assert.Equal(0, iCountErrors);
    }
}
