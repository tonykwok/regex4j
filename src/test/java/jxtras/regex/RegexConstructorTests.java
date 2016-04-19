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

public class RegexConstructorTests {
    @Fact
    public static void RegexConstructor() {
        //////////// Global Variables used for all tests
        String strLoc = "Loc_000oo";
        String strValue = Strings.EMPTY;
        int iCountErrors = 0;
        int iCountTestcases = 0;
        Regex r;
        try {
            /////////////////////////  START TESTS ////////////////////////////
            ///////////////////////////////////////////////////////////////////
            //[]RegEx with null expression
            strLoc = "Loc_sdfa9849";
            iCountTestcases++;
            try {
                r = new Regex(null, RegexOptions.None);
                iCountErrors++;
                System.out.println("Err_16891 Expected Regex to throw ArgumentNullException and nothing was thrown");
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_9877sawa Expected Regex to throw ArgumentNullException and the following exception was thrown:\n {%s}", e);
            }

            //[]RegEx with negative RegexOptions
            strLoc = "Loc_3198sdf";
            iCountTestcases++;
            try {
                r = new Regex("foo", -1);
                iCountErrors++;
                System.out.println("Err_2389asfd Expected Regex to throw ArgumentException and nothing was thrown");
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_898asdf Expected Regex to throw ArgumentException and the following exception was thrown:\n {%s}", e);
            }

            //[]RegEx with to high RegexOptions
            strLoc = "Loc_23198awd";
            iCountTestcases++;
            try {
                r = new Regex("foo", 0x400);
                iCountErrors++;
                System.out.println("Err_1238sadw Expected Regex to throw ArgumentException and nothing was thrown");
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_6579asdf Expected Regex to throw ArgumentException and the following exception was thrown:\n {%s}", e);
            }

            //[]RegEx with ECMA RegexOptions with all other valid options
            strLoc = "Loc_3198sdf";
            iCountTestcases++;
            try {
                r = new Regex("foo", RegexOptions.ECMAScript | RegexOptions.IgnoreCase | RegexOptions.Multiline | RegexOptions.CultureInvariant);
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_97878dsaw Expected Regex not to throw and the following exception was thrown:\n {%s}", e);
            }

            //[]RegEx with ECMA RegexOptions with all other valid options plus RightToLeft
            strLoc = "Loc_9875asd";
            iCountTestcases++;
            try {
                r = new Regex("foo", RegexOptions.ECMAScript | RegexOptions.IgnoreCase | RegexOptions.Multiline | RegexOptions.CultureInvariant | RegexOptions.RightToLeft);
                iCountErrors++;
                System.out.println("Err_9789swd Expected Regex to throw ArgumentException and nothing was thrown");
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_9489sdjk Expected Regex to throw ArgumentException and the following exception was thrown:\n {%s}", e);
            }

            //[]RegEx with ECMA RegexOptions with all other valid options plus ExplicitCapture
            strLoc = "Loc_54864jhlt";
            iCountTestcases++;
            try {
                r = new Regex("foo", RegexOptions.ECMAScript | RegexOptions.IgnoreCase | RegexOptions.Multiline | RegexOptions.CultureInvariant | RegexOptions.ExplicitCapture);
                iCountErrors++;
                System.out.println("Err_6556jhkj Expected Regex to throw ArgumentException and nothing was thrown");
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_2189jhss Expected Regex to throw ArgumentException and the following exception was thrown:\n {%s}", e);
            }

            //[]RegEx with ECMA RegexOptions with all other valid options plus Singleline
            strLoc = "Loc_9891asfes";
            iCountTestcases++;
            try {
                r = new Regex("foo", RegexOptions.ECMAScript | RegexOptions.IgnoreCase | RegexOptions.Multiline | RegexOptions.CultureInvariant | RegexOptions.Singleline);
                iCountErrors++;
                System.out.println("Err_3156add Expected Regex to throw ArgumentException and nothing was thrown");
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_456hjhj Expected Regex to throw ArgumentException and the following exception was thrown:\n {%s}", e);
            }

            //[]RegEx with ECMA RegexOptions with all other valid options plus IgnorePatternWhitespace
            strLoc = "Loc_23889asddf";
            iCountTestcases++;
            try {
                r = new Regex("foo", RegexOptions.ECMAScript | RegexOptions.IgnoreCase | RegexOptions.Multiline | RegexOptions.CultureInvariant | RegexOptions.IgnorePatternWhitespace);
                iCountErrors++;
                System.out.println("Err_3568sdae Expected Regex to throw ArgumentException and nothing was thrown");
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_4657dsacd Expected Regex to throw ArgumentException and the following exception was thrown:\n {%s}", e);
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
}