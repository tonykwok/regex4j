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
import jxtras.regex.support.Fact;

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