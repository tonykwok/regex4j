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

package jxtras.regex.tests;

import jxtras.regex.Regex;
import jxtras.regex.RegexOptions;
import jxtras.regex.support.Strings;
import jxtras.regex.support.Assert;
import jxtras.regex.support.Fact;

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
