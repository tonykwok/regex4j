package jxtras.regex.tests;

import jxtras.regex.Match;
import jxtras.regex.Regex;
import jxtras.regex.support.Strings;
import jxtras.regex.tests.support.Assert;
import jxtras.regex.tests.support.Fact;

public class R2LGetGroupNamesMatchTests {
    /*
    Tested Methods:
        public static Boolean RightToLeft;
        public static String[] GetGroupNames();     "(?<first_name>\\S+)\\s(?<last_name>\\S+)"
        public static Match Match(string input);
            "David Bau"
        public static Boolean IsMatch(string input);     //D+
            "12321"
    */

    @Fact
    public static void R2LGetGroupNamesMatch() {
        //////////// Global Variables used for all tests
        String strLoc = "Loc_000oo";
        String strValue = Strings.EMPTY;
        int iCountErrors = 0;
        int iCountTestcases = 0;
        Regex r;
        Match m;
        String s;
        String[] names;
        try {
            /////////////////////////  START TESTS ////////////////////////////
            ///////////////////////////////////////////////////////////////////
            s = "David Bau";
            r = new Regex("(?<first_name>\\S+)\\s(?<last_name>\\S+)");
            // [] public static String[] GetGroupNames();     "(?<first_name>\\S+)\\s(?<last_name>\\S+)"
            //-----------------------------------------------------------------
            strLoc = "Loc_498yg";
            iCountTestcases++;
            names = r.getGroupNames();
            if (!names[0].equals("0")) {
                iCountErrors++;
                System.out.println("Err_234fsadg! unexpected result");
            }

            if (!names[1].equals("first_name")) {
                iCountErrors++;
                System.out.println("Err_234fsadg! unexpected result");
            }

            if (!names[2].equals("last_name")) {
                iCountErrors++;
                System.out.println("Err_234fsadg! unexpected result");
            }

            // [] public static Match Match(string input);
            //"David Bau"
            //-----------------------------------------------------------------
            strLoc = "Loc_563sdfg";
            iCountTestcases++;
            m = r.match(s);
            if (!m.success()) {
                iCountErrors++;
                System.out.println("Err_87543! doesnot match");
            }

            // [] public static Match Match(string input);
            //"David Bau"
            //-----------------------------------------------------------------
            strLoc = "Loc_298vy";
            iCountTestcases++;
            s = "12321";
            r = new Regex("\\D+");
            if (r.isMatch(s)) {
                iCountErrors++;
                System.out.println("Err_fsdf! doesnot match");
            }
            ///////////////////////////////////////////////////////////////////
            /////////////////////////// END TESTS /////////////////////////////
        } catch (Exception exc_general) {
            ++iCountErrors;
            System.out.println("Error Err_8888yyy!  strLoc==" + strLoc + ", exc_general==" + exc_general.toString());
        }
    }
}