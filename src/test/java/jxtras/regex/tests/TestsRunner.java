package jxtras.regex.tests;

import jxtras.regex.Group;
import jxtras.regex.Match;
import jxtras.regex.MatchCollection;
import jxtras.regex.Regex;
import jxtras.regex.RegexOptions;
import jxtras.regex.tests.support.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestsRunner {

    private static final Class<?>[] CASES = new Class<?>[]{
            CaptureCollectionTests.class,
            CaptureTests.class,
            EscapeUnescapeTests.class,
            GroupNamesAndNumbers.class,
            IsMatchMatchSplitTests.class,
            MatchCollectionTests.class,
            R2LGetGroupNamesMatchTests.class,
            RegexComponentsSplitTests.class,
            RegexConstructorTests.class,
            RegexMatchTests0.class,
            RegexMatchTests1.class,
            RegexMatchTests2.class,
            RegexMatchTests3.class,
            RegexMatchTests4.class,
            RegexMatchTests5.class,
            RegexMatchTests6.class,
            RegexMatchTests7.class,
            RegexMatchTests8.class,
            RegexMatchTests9.class,
            RegexMatchTests10.class,
            RegexMatchTests11.class,
            RegexMatchValueTests.class,
            RegexReplaceStringTests0.class,
            RegexReplaceStringTests1.class,
            RegexSplitTests.class,
            ReturnValueChecks.class,
            RightToLeft.class,
            RightToLeftMatchStartAtTests.class,
            SplitMatchIsMatchTests.class,
            RegexUnicodeCharTests.class,
            RegexLangElementsCoverageTests.class,
            CharacterClassSubtractionSimple.class,
            // static inner classes in this file are moved into support folder
            // Support.class
    };

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        String textToSearch = "Welcome to http://www.moserware.com/!";
        String pattern = "http://([^\\s/]+)/?";
//
//        Match m = new Regex(pattern, RegexOptions.None | RegexOptions.Debug).match(textToSearch);
//        System.out.println();
//        System.out.println("Full uri = " + m.value());
//        System.out.println("Host = " + m.groups().get(1).value());

        textToSearch = "azAZ1024689";
        pattern = "[\\d-[\\D]]+";

        Match m2 = new Regex(pattern, RegexOptions.ECMAScript | RegexOptions.Debug).match(textToSearch);
        System.out.println();
        System.out.println("match = " + m2.value());
        for (Group group : m2.groups()) {
            System.out.println("group = " + group.value());
        }

        int count = 0;
        for (Class<?> cls : CASES) {
            Method[] methods = cls.getDeclaredMethods();
            System.out.printf("[%3d]: " + cls.toString().substring(cls.toString().lastIndexOf('.') + 1), ++count);
            System.out.println();
            for (Method method : methods) {
                Annotation[] annotations = method.getDeclaredAnnotations();
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        if (annotation.toString().contains("Fact")) {
                            method.invoke(null, (Object[]) null);
                            System.out.println("       * " + method.toString().substring(method.toString().lastIndexOf('.') + 1) + " ... PASS!");
                        }
                    }
                }
            }
            System.out.println("=========================================================================");
        }

        System.out.println("=================     smali parser regex test    ========================");
        String fileText = "";
        MatchCollection matches;

        // smali field
        matches = new Regex(".field(\\s)*(?<modifiers>public |private |protected |static |final |volatile |transient )*(\\s)*(?<fieldname>[a-zA-Z0-9_$]+):L?(?<type>[a-zA-Z0-9_$/\\[]+)(?:[; =]+)?(?<fieldvalue>.+)?").matches(fileText);

        // hex string
        matches = new Regex("0x0*[1-7][a-fA-F0-9]{4,10}").matches(fileText);

        // smali jumps
        matches = new Regex("(?:(?:if-.+)|(?:packed-switch.+)|(?:sparse-switch.+)|(?:goto.+)):(?<targetlabel>.+)").matches(fileText);

        // smali jump target
        matches = new Regex(String.format(" {2,}:(?<targetlabel>%s)", "cond01")).matches(fileText);

        // smali methods and references
        matches = new Regex("L(?<path>[a-zA-Z0-9/$_]+)(?:;->)(?:(?<method>[a-zA-Z0-9/$<>_]+\\(.*\\).+))?(?:(?<field>[a-zA-Z0-9/$<>_]+))?").matches(fileText);

        // smaili methods
        matches = new Regex("\\.method(\\s)*(?<modifiers>public |protected |private |static |synthetic |final |native |varargs |declared-synchronized |abstract )*(\\s)*(?<constructor>constructor)?(\\s)*(?<methodname>[a-zA-Z0-9_<>$/;\\(\\)\\[]+)((?:.|\\r?\\n)*?)\\.end method", RegexOptions.ExplicitCapture | RegexOptions.IgnoreCase).matches(fileText);

        matches = new Regex("(?<=[,\\{\\W])(?<name>v(?<number>\\d+))(?=[,\\}\\W])").matches(fileText);
        System.out.println("=========================================================================");
    }
}