package jxtras.regex.runner;

import jxtras.regex.Group;
import jxtras.regex.Match;
import jxtras.regex.Regex;
import jxtras.regex.RegexOptions;

/**
 * Created by 28851063 on 1/6/17.
 */
public class Debug {
    public static void main(String... args) {
        String textToSearch = "Welcome to http://www.moserware.com/!";
        String pattern = "http://([^\\s/]+)/?";

        Match m = new Regex(pattern, RegexOptions.None | RegexOptions.Debug).match(textToSearch);
        System.out.println();
        System.out.println("Full uri = " + m.value());
        System.out.println("Host = " + m.groups().get(1).value());

        textToSearch = "azAZ1024689";
        pattern = "[\\d-[\\D]]+";

        Match m2 = new Regex(pattern, RegexOptions.ECMAScript | RegexOptions.Debug).match(textToSearch);
        System.out.println();
        System.out.println("match = " + m2.value());
        for (Group group : m2.groups()) {
            System.out.println("group = " + group.value());
        }
    }
}
