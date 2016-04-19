package jxtras.regex;

public class Demo {
    public static void main(String... args) {
        String url = "git://tony.guo@github.com:29418/regex4j.git";
        Regex r = new Regex("^(?<proto>\\w+)://[^/]+?(?<port>:\\d+)?/", RegexOptions.None, 150 /* millisecond */);
        Match m = r.match(url);

        if (m.success()) {
            System.out.println(m.groups().get("proto").value() + m.groups().get("port").value());
        }
    }
}