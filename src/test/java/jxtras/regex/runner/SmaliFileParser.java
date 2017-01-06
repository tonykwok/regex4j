package jxtras.regex.runner;

import jxtras.regex.MatchCollection;
import jxtras.regex.Regex;
import jxtras.regex.RegexOptions;

/**
 * Created by 28851063 on 1/6/17.
 */
public class SmaliFileParser {
    public static void main(String... args) {
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
