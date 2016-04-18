package jxtras.regex.tests;

import jxtras.regex.Regex;
import jxtras.regex.support.Strings;
import jxtras.regex.tests.support.Assert;
import jxtras.regex.tests.support.Fact;

public class EscapeUnescapeTests {
    /*
    * Tested Methods:
    * public static String Escape(String str);     round tripping "#$^*+(){}<>\\|. "
    * public static String Unescape(string str);
    */
    @Fact
    public static void EscapeUnescape() {
        // Global Variables used for all tests
        String strLoc = "Loc_000oo";
        String strValue = Strings.EMPTY;
        int iCountErrors = 0;
        int iCountTestcases = 0;

        try {
            strLoc = "Loc_498yg";
            iCountTestcases++;
            String s1 = "#$^*+(){}<>\\|. ";
            String s2 = Regex.escape(s1);
            String s3 = Regex.unescape(s2);
            if (!s1.equals(s3)) {
                iCountErrors++;
                System.out.println("Err_234fsadg! does not match");
            }
        } catch (Exception exc_general) {
            ++iCountErrors;
            System.out.println("Error Err_8888yyy!  strLoc==" + strLoc + ", exc_general==" + exc_general.toString());
        }

        // Finish Diagnostics
        Assert.Equal(0, iCountErrors);
    }
}
