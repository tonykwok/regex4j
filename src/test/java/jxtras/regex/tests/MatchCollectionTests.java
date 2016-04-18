package jxtras.regex.tests;

import jxtras.regex.Match;
import jxtras.regex.MatchCollection;
import jxtras.regex.Regex;
import jxtras.regex.tests.support.Assert;
import jxtras.regex.tests.support.Fact;

import java.util.Iterator;

public class MatchCollectionTests {
    @Fact
    public static void EnumeratorTest1() {
        Regex regex = new Regex("e");
        MatchCollection collection = regex.matches("dotnet");
        Iterator<Match> enumerator = collection.iterator();

        try {
            enumerator.next();
            System.out.println("Expected Regex to throw IndexOutOfBoundsException and nothing was thrown.");
        } catch (IndexOutOfBoundsException e) {
            // expected
        } catch (Exception e) {
            System.out.printf("Expected Regex to throw IndexOutOfBoundsException and following exception was thrown:\n{%s}.", e);
        }
        Assert.True(enumerator.hasNext());
        Match current = enumerator.next();
        Assert.True(current != null);
        Assert.Equal(4, current.index());
        Assert.Equal("e", current.value());
        Assert.False(enumerator.hasNext());
        try {
            enumerator.next();
            System.out.println("Expected Regex to throw IndexOutOfBoundsException and nothing was thrown.");
        } catch (IndexOutOfBoundsException e) {
            // expected
        } catch (Exception e) {
            System.out.printf("Expected Regex to throw IndexOutOfBoundsException and following exception was thrown:\n{%s}.", e);
        }
    }

    @Fact
    public static void EnumeratorTest2() {
        Regex regex = new Regex("t");
        MatchCollection collection = regex.matches("dotnet");
        Iterator<Match> enumerator = collection.iterator();

        for (int i = 0; i < collection.count(); i++) {
            enumerator.hasNext();

            Assert.Equal(enumerator.next(), collection.get(i));
        }

        Assert.False(enumerator.hasNext());
        try {
            enumerator.next();
            System.out.println("Expected Regex to throw IndexOutOfBoundsException and nothing was thrown.");
        } catch (IndexOutOfBoundsException e) {
            // expected
        } catch (Exception e) {
            System.out.printf("Expected Regex to throw IndexOutOfBoundsException and following exception was thrown:\n{%s}.", e);
        }
    }
}