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

import jxtras.regex.Match;
import jxtras.regex.Regex;
import jxtras.regex.support.Strings;
import jxtras.regex.support.Assert;
import jxtras.regex.support.Fact;

public class RegexMatchTests0 {
    /*
    Tested Methods:
        public static Match Match(string input, string pattern);     Testing \B special character escape
            "adfadsfSUCCESSadsfadsf", ".*\\B(SUCCESS)\\B.*"
        public static Match Match(string input, string pattern);     Testing octal sequence matches
            "011", "\\060(\\061)?\\061"
        public static Match Match(string input, string pattern);     Testing hexadecimal sequence matches
            "012", "(\\x30\\x31\\x32)"
        public static Match Match(string input, string pattern);     Testing control character escapes???
            "2", "(\u0032)"
    */

    @Fact
    public static void RegexMatchTestCase0() {
        //////////// Global Variables used for all tests
        String strLoc = "Loc_000oo";
        String strValue = Strings.EMPTY;
        int iCountErrors = 0;
        int iCountTestcases = 0;
        Match match;
        String s;
        String strMatch1 = "adfadsfSUCCESSadsfadsf";
        int[] iMatch1 =
                {
                        0, 22
                };
        String[] strGroup1 =
                {
                        "adfadsfSUCCESSadsfadsf", "SUCCESS"
                };
        int[] iGroup1 =
                {
                        7, 7
                };
        String[] strGrpCap1 =
                {
                        "SUCCESS"
                };
        int[][] iGrpCap1 =
                {
                        {
                                7, 7
                        }
                };
        try {
            /////////////////////////  START TESTS ////////////////////////////
            ///////////////////////////////////////////////////////////////////
            // [] public static Match Match(string input, string pattern);     Testing \B special character escape
            //"adfadsfSUCCESSadsfadsf", ".*\\B(SUCCESS)\\B.*"
            //-----------------------------------------------------------------
            strLoc = "Loc_498yg";
            iCountTestcases++;
            match = Regex.match("adfadsfSUCCESSadsfadsf", ".*\\B(SUCCESS)\\B.*");
            if (!match.success()) {
                iCountErrors++;
                System.out.print("Err_7356wgd! Fail Do not found a match");
            } else {
                if (!match.value().equals(strMatch1) || (match.index() != iMatch1[0]) || (match.length() != iMatch1[1]) || (match.captures().count() != 1)) {
                    iCountErrors++;
                    System.out.printf("Err_98275dsg: unexpected return result");
                }

                //Match.Captures always is Match
                if (!match.captures().get(0).value().equals(strMatch1) || (match.captures().get(0).index() != iMatch1[0]) || (match.captures().get(0).length() != iMatch1[1])) {
                    iCountErrors++;
                    System.out.print("Err_2046gsg! unexpected return result");
                }

                if (match.groups().count() != 2) {
                    iCountErrors++;
                    System.out.print("Err_75324sg! unexpected return result");
                }

                //Group 0 always is the Match
                if (!match.groups().get(0).value().equals(strMatch1) || (match.groups().get(0).index() != iMatch1[0]) || (match.groups().get(0).length() != iMatch1[1]) || (match.groups().get(0).captures().count() != 1)) {
                    iCountErrors++;
                    System.out.print("Err_2046gsg! unexpected return result");
                }

                //Group 0's Capture is always the Match
                if (!match.groups().get(0).captures().get(0).value().equals(strMatch1) || (match.groups().get(0).captures().get(0).index() != iMatch1[0]) || (match.groups().get(0).captures().get(0).length() != iMatch1[1])) {
                    iCountErrors++;
                    System.out.print("Err_2975edg!! unexpected return result");
                }

                for (int i = 1; i < match.groups().count(); i++) {
                    if (!match.groups().get(i).value().equals(strGroup1[i]) || (match.groups().get(i).index() != iGroup1[0]) || (match.groups().get(i).length() != iGroup1[1]) || (match.groups().get(i).captures().count() != 1)) {
                        iCountErrors++;
                        System.out.printf("Err_1954eg_" + i + "! unexpected return result, Value = <{%s}:{%s}>, Index = <{%d}:{%d}>, Length = <{%d}:{%d}>",
                                match.groups().get(i).value(),
                                strGroup1[i],
                                match.groups().get(i).index(),
                                iGroup1[0],
                                match.groups().get(i).length(),
                                iGroup1[1]);
                    }

                    for (int j = 0; j < match.groups().get(i).captures().count(); j++) {
                        if (!match.groups().get(i).captures().get(j).value().equals(strGrpCap1[j]) || (match.groups().get(i).captures().get(j).index() != iGrpCap1[j][0]) || (match.groups().get(i).captures().get(j).length() != iGrpCap1[j][1])) {
                            iCountErrors++;
                            System.out.printf("Err_5072dn_" + i + "_" + j + "!! unexpected return result, Value = {%s}, Index = {%d}, Length = {%d}", match.groups().get(i).captures().get(j).value(), match.groups().get(i).captures().get(j).index(), match.groups().get(i).captures().get(j).length());
                        }
                    }
                }
            }

            // [] public static Match Match(string input, string pattern);     Testing octal sequence matches
            //"011", "\\060(\\061)?\\061"
            //-----------------------------------------------------------------
            strLoc = "Loc_298vy";
            iCountTestcases++;
            //Octal \061 is ASCII 49
            match = Regex.match("011", "\060(\061)?\061");
            if (!match.success()) {
                iCountErrors++;
                System.out.print("Err_576trffg! Do not found octal sequence match");
            }

            // [] public static Match Match(string input, string pattern);     Testing hexadecimal sequence matches
            //"012", "(\\x30\\x31\\x32)"
            //-----------------------------------------------------------------
            strLoc = "Loc_746tegd";
            iCountTestcases++;
            //Hex \x31 is ASCII 49 TODO: java does not support \xFFF chars, so use unicode instead
            match = Regex.match("012", "(\u0030\u0031\u0032)");
            if (!match.success()) {
                iCountErrors++;
                System.out.print("Err_674tgdg! Do not found hexadecimal sequence match");
            }

            // [] public static Match Match(string input, string pattern);     Testing control character escapes???
            //"2", "(\u0032)"
            //-----------------------------------------------------------------
            strLoc = "Loc_743gf";
            iCountTestcases++;
            s = "4"; // or "\u0034"
            match = Regex.match(s, "(\u0034)");
            if (!match.success()) {
                iCountErrors++;
                System.out.print("Err_4532gvfs! Do not found unicode character match");
            }

            ///////////////////////////////////////////////////////////////////
            /////////////////////////// END TESTS /////////////////////////////
        } catch (Exception exc_general) {
            ++iCountErrors;
            System.out.print("Error: Err_8888yyy!  strLoc==" + strLoc + ", exc_general==" + exc_general.toString());
        }

        ////  Finish Diagnostics
        Assert.Equal(0, iCountErrors);
    }
}
