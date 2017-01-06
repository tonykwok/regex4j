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

import jxtras.regex.Capture;
import jxtras.regex.CaptureCollection;
import jxtras.regex.Match;
import jxtras.regex.Regex;
import jxtras.regex.support.Assert;
import jxtras.regex.support.Fact;

import java.util.Iterator;

public class CaptureCollectionTests {
    @Fact
    public static void CaptureCollection_GetEnumeratorTest_Negative() {
        Regex rgx1 = new Regex("(?<A1>a*)(?<A2>b*)(?<A3>c*)");
        String strInput = "aaabbccccccccccaaaabc";
        Match mtch1 = rgx1.match(strInput);
        CaptureCollection captrc1 = mtch1.captures();

        Iterator<Capture> enmtr1 = captrc1.iterator();

        try {
            enmtr1.next();
            System.out.println("Expected Regex to throw IndexOutOfBoundsException and nothing was thrown.");
        } catch (IndexOutOfBoundsException e) {
            // expected
        } catch (Exception e) {
            System.out.printf("Expected Regex to throw IndexOutOfBoundsException and following exception was thrown:\n{%s}.", e);
        }

        for (int i = 0; i < captrc1.count(); i++) {
            enmtr1.hasNext();
        }

        enmtr1.hasNext();

        try {
            enmtr1.next();
            System.out.println("Expected Regex to throw IndexOutOfBoundsException and nothing was thrown.");
        } catch (IndexOutOfBoundsException e) {
            // expected
        } catch (Exception e) {
            System.out.printf("Expected Regex to throw IndexOutOfBoundsException and following exception was thrown:\n{%s}.", e);
        }
    }

    @Fact
    public static void CaptureCollection_GetEnumeratorTest() {
        Regex rgx1 = new Regex("(?<A1>a*)(?<A2>b*)(?<A3>c*)");
        String strInput = "aaabbccccccccccaaaabc";
        Match mtch1 = rgx1.match(strInput);
        CaptureCollection captrc1 = mtch1.captures();

        Iterator<Capture> enmtr1 = captrc1.iterator();

        for (int i = 0; i < captrc1.count(); i++) {
            enmtr1.hasNext();

            Assert.Equal(enmtr1.next(), captrc1.get(i));
        }

        Assert.False(enmtr1.hasNext(), "Err_5! enmtr1.MoveNext returned true");

        try {
            enmtr1.next();
            System.out.println("Expected Regex to throw IndexOutOfBoundsException and nothing was thrown.");
        } catch (IndexOutOfBoundsException e) {
            // expected
        } catch (Exception e) {
            System.out.printf("Expected Regex to throw IndexOutOfBoundsException and following exception was thrown:\n{%s}.", e);
        }
    }
}