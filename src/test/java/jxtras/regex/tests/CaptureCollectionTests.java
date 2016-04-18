package jxtras.regex.tests;

import jxtras.regex.Capture;
import jxtras.regex.Match;
import jxtras.regex.Regex;
import jxtras.regex.tests.support.Assert;
import jxtras.regex.CaptureCollection;
import jxtras.regex.tests.support.Fact;

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