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

import jxtras.regex.Match;
import jxtras.regex.Regex;
import jxtras.regex.support.Strings;
import jxtras.regex.support.Assert;
import jxtras.regex.Group;
import jxtras.regex.support.Fact;

public class GroupNamesAndNumbers {
    /*
     * Tested Methods:
     * public string[] GetGroupNames();
     * public int[] GetGroupNumbers();
     * public string GroupNameFromNumber(int i);
     * public int GroupNumberFromName(string name);
    */
    @Fact
    public static void GroupNamesAndNumberTestCase() {
        //////////// Global Variables used for all tests
        String strLoc = "Loc_000oo";
        String strValue = Strings.EMPTY;
        int iCountErrors = 0;
        int iCountTestcases = 0;
        Regex r;
        String s;
        String[] expectedNames;
        String[] expectedGroups;
        int[] expectedNumbers;
        try {
            /////////////////////////  START TESTS ////////////////////////////
            ///////////////////////////////////////////////////////////////////
            //[]Vanilla
            s = "Ryan Byington";
            r = new Regex("(?<first_name>\\S+)\\s(?<last_name>\\S+)");
            strLoc = "Loc_498yg";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "first_name", "last_name"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 1, 2
                    }

            ;
            expectedGroups = new String[]
                    {
                            "Ryan Byington", "Ryan", "Byington"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_79793asdwk! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_12087ahas! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_08712saopz! Unexpected Groups");
            }

            //[]RegEx from SDK
            s = "abc208923xyzanqnakl";
            r = new Regex("((?<One>abc)\\d+)?(?<Two>xyz)(.*)");
            strLoc = "Loc_0822aws";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "1", "2", "One", "Two"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 1, 2, 3, 4
                    }

            ;
            expectedGroups = new String[]
                    {
                            "abc208923xyzanqnakl", "abc208923", "anqnakl", "abc", "xyz"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_79793asdwk! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_12087ahas! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_0822klas! Unexpected Groups");
            }

            //[]RegEx with numeric names
            s = "0272saasdabc8978xyz][]12_+-";
            r = new Regex("((?<256>abc)\\d+)?(?<16>xyz)(.*)");
            strLoc = "Loc_0982asd";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "1", "2", "16", "256"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 1, 2, 16, 256
                    }

            ;
            expectedGroups = new String[]
                    {
                            "abc8978xyz][]12_+-", "abc8978", "][]12_+-", "xyz", "abc"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_79793asdwk! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_12087ahas! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_7072ankla! Unexpected Groups");
            }

            //[]RegEx with numeric names and string names
            s = "0272saasdabc8978xyz][]12_+-";
            r = new Regex("((?<4>abc)(?<digits>\\d+))?(?<2>xyz)(?<everything_else>.*)");
            strLoc = "Loc_98968asdf";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "1", "2", "digits", "4", "everything_else"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 1, 2, 3, 4, 5
                    }

            ;
            expectedGroups = new String[]
                    {
                            "abc8978xyz][]12_+-", "abc8978", "xyz", "8978", "abc", "][]12_+-"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_9496sad! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_6984awsd! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_7072ankla! Unexpected Groups");
            }

            //[]RegEx with 0 numeric names
            try {
                r = new Regex("foo(?<0>bar)");
                iCountErrors++;
                System.out.println("Err_16891 Expected Regex to throw IllegalArgumentException and nothing was thrown");
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_9877sawa Expected Regex to throw ArgumentException and the following exception was thrown:\n%s", e);
            }

            //[]RegEx without closing >
            try {
                r = new Regex("foo(?<1bar)");
                iCountErrors++;
                System.out.println("Err_2389uop Expected Regex to throw IllegalArgumentException and nothing was thrown");
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_3298asoia Expected Regex to throw ArgumentException and the following exception was thrown:\n%s", e);
            }

            //[] Duplicate string names
            s = "Ryan Byington";
            r = new Regex("(?<first_name>\\S+)\\s(?<first_name>\\S+)");
            strLoc = "Loc_sdfa9849";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "first_name"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 1
                    }

            ;
            expectedGroups = new String[]
                    {
                            "Ryan Byington", "Byington"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_32189asdd! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_7978assd! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_98732soiya! Unexpected Groups");
            }

            //[] Duplicate numeric names
            s = "Ryan Byington";
            r = new Regex("(?<15>\\S+)\\s(?<15>\\S+)");
            strLoc = "Loc_89198asda";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "15"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 15
                    }

            ;
            expectedGroups = new String[]
                    {
                            "Ryan Byington", "Byington"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_97654awwa! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_6498asde! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_316jkkl! Unexpected Groups");
            }

            /******************************************************************
             Repeat the same steps from above but using (?'foo') instead
             ******************************************************************/
            //[]Vanilla
            s = "Ryan Byington";
            r = new Regex("(?'first_name'\\S+)\\s(?'last_name'\\S+)");
            strLoc = "Loc_0982aklpas";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "first_name", "last_name"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 1, 2
                    }

            ;
            expectedGroups = new String[]
                    {
                            "Ryan Byington", "Ryan", "Byington"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_464658safsd! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_15689asda! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_31568kjkj! Unexpected Groups");
            }

            //[]RegEx from SDK
            s = "abc208923xyzanqnakl";
            r = new Regex("((?'One'abc)\\d+)?(?'Two'xyz)(.*)");
            strLoc = "Loc_98977uouy";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "1", "2", "One", "Two"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 1, 2, 3, 4
                    }

            ;
            expectedGroups = new String[]
                    {
                            "abc208923xyzanqnakl", "abc208923", "anqnakl", "abc", "xyz"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_65498yuiy! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_5698yuiyh! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_2168hkjh! Unexpected Groups");
            }

            //[]RegEx with numeric names
            s = "0272saasdabc8978xyz][]12_+-";
            r = new Regex("((?'256'abc)\\d+)?(?'16'xyz)(.*)");
            strLoc = "Loc_9879hjly";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "1", "2", "16", "256"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 1, 2, 16, 256
                    }

            ;
            expectedGroups = new String[]
                    {
                            "abc8978xyz][]12_+-", "abc8978", "][]12_+-", "xyz", "abc"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_21689hjkh! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_2689juj! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_2358adea! Unexpected Groups");
            }

            //[]RegEx with numeric names and string names
            s = "0272saasdabc8978xyz][]12_+-";
            r = new Regex("((?'4'abc)(?'digits'\\d+))?(?'2'xyz)(?'everything_else'.*)");
            strLoc = "Loc_23189uioyp";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "1", "2", "digits", "4", "everything_else"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 1, 2, 3, 4, 5
                    }

            ;
            expectedGroups = new String[]
                    {
                            "abc8978xyz][]12_+-", "abc8978", "xyz", "8978", "abc", "][]12_+-"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_3219hjkj! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_23189aseq! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_2318adew! Unexpected Groups");
            }

            //[]RegEx with 0 numeric names
            try {
                r = new Regex("foo(?'0'bar)");
                iCountErrors++;
                System.out.println("Err_16891 Expected Regex to throw IllegalArgumentException and nothing was thrown");
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_9877sawa Expected Regex to throw ArgumentException and the following exception was thrown:\n%s", e);
            }

            //[]RegEx without closing >
            try {
                r = new Regex("foo(?'1bar)");
                iCountErrors++;
                System.out.println("Err_979asja Expected Regex to throw ArgumentException and nothing was thrown");
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                iCountErrors++;
                System.out.printf("Err_16889asdfw Expected Regex to throw ArgumentException and the following exception was thrown:\n {0}", e);
            }

            //[] Duplicate string names
            s = "Ryan Byington";
            r = new Regex("(?'first_name'\\S+)\\s(?'first_name'\\S+)");
            strLoc = "Loc_2318opa";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "first_name"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 1
                    }

            ;
            expectedGroups = new String[]
                    {
                            "Ryan Byington", "Byington"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_28978adfe! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_3258adsw! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_2198asd! Unexpected Groups");
            }

            //[] Duplicate numeric names
            s = "Ryan Byington";
            r = new Regex("(?'15'\\S+)\\s(?'15'\\S+)");
            strLoc = "Loc_3289hjaa";
            iCountTestcases++;
            expectedNames = new String[]
                    {
                            "0", "15"
                    }

            ;
            expectedNumbers = new int[]
                    {
                            0, 15
                    }

            ;
            expectedGroups = new String[]
                    {
                            "Ryan Byington", "Byington"
                    }

            ;
            if (!VerifyGroupNames(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_13289asd! Unexpected GroupNames");
            }

            if (!VerifyGroupNumbers(r, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_23198asdf! Unexpected GroupNumbers");
            }

            if (!VerifyGroups(r, s, expectedGroups, expectedNames, expectedNumbers)) {
                iCountErrors++;
                System.out.println("Err_15689teraku! Unexpected Groups");
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

    public static boolean VerifyGroupNames(Regex r, String[] expectedNames, int[] expectedNumbers) {
        String[] names = r.getGroupNames();
        if (names.length != expectedNames.length) {
            System.out.printf("Err_08722aswa! Expect %d names actual=%d", expectedNames.length, names.length);
            return false;
        }

        for (int i = 0; i < expectedNames.length; i++) {
            if (!names[i].equals(expectedNames[i])) {
                System.out.printf("Err_09878asfas! Expected GroupNames[%d]=%s actual=%s", i, expectedNames[i], names[i]);
                return false;
            }

            if (!expectedNames[i].equals(r.groupNameFromNumber(expectedNumbers[i]))) {
                System.out.printf("Err_6589sdafn!GroupNameFromNumber(%d)=%s actual=%s", expectedNumbers[i], expectedNames[i], r.groupNameFromNumber(expectedNumbers[i]));
                return false;
            }
        }

        return true;
    }

    public static boolean VerifyGroupNumbers(Regex r, String[] expectedNames, int[] expectedNumbers) {
        int[] numbers = r.getGroupNumbers();
        if (numbers.length != expectedNumbers.length) {
            System.out.printf("Err_7978awoyp! Expect %d numbers actual=%d", expectedNumbers.length, numbers.length);
            return false;
        }

        for (int i = 0; i < expectedNumbers.length; i++) {
            if (numbers[i] != expectedNumbers[i]) {
                System.out.printf("Err_4342asnmc! Expected GroupNumbers[%d]=%d actual=%d", i, expectedNumbers[i], numbers[i]);
                return false;
            }

            if (expectedNumbers[i] != r.groupNumberFromName(expectedNames[i])) {
                System.out.printf("Err_98795ajkas!GroupNumberFromName(%d)=%d actual=%d", expectedNames[i], expectedNumbers[i], r.groupNumberFromName(expectedNames[i]));
                return false;
            }
        }

        return true;
    }

    public static boolean VerifyGroups(Regex r, String s, String[] expectedGroups, String[] expectedNames, int[] expectedNumbers) {
        Match m = r.match(s);
        Group g;
        if (!m.success()) {
            System.out.println("Err_08220kha Match not a success");
            return false;
        }

        if (m.groups().count() != expectedGroups.length) {
            System.out.printf("Err_9722asqa! Expect %d groups actual=%d", expectedGroups.length, m.groups().count());
            return false;
        }

        for (int i = 0; i < expectedNumbers.length; i++) {
            if (null == (g = m.groups().get(expectedNames[i])) || !expectedGroups[i].equals(g.value())) {
                System.out.printf("Err_3327nkoo! Expected Groups[%s]=%s actual=%s", expectedNames[i], expectedGroups[i], g == null ? "<null>" : g.value());
                return false;
            }

            if (null == (g = m.groups().get(expectedNumbers[i])) || !expectedGroups[i].equals(g.value())) {
                System.out.printf("Err_9465sdjh! Expected Groups[%d]=%s actual=%s", expectedNumbers[i], expectedGroups[i], g == null ? "<null>" : g.value());
                return false;
            }
        }

        return true;
    }
}
