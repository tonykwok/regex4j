## Regex4j

Regex4j is a port of the ```System.Text.RegularExpressions``` module of [Microsoft's .Net Core Libraries](https://github.com/dotnet/corefx) written in Java as compensation for the Java built-in regex engine, meanwhile, avoids the ```StackOverflowError``` issues ([JDK-6337993](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6337993), [JDK-6882582](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6882582) and [JDK-8078476](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8078476)), which have been in the ```java.util.regex``` package since Java 1.4 and still exist in the latest JDK.

## Usage

The usage of Regex4j is much similar as ```System.Text.RegularExpressions```, here're some tips for you to get started quickly:

> **Tips**
>- Regex4j does not support verbatim string literals, that's to say, you have to add the backslash character (```\```) explicitly for escape sequences when you try to translate some regex patterns from C# to Java
>- Regex4j does not support ```TimeSpan```, instead you can pass ```int``` values when constructs the ```Regex``` object. The valid range are ```[0, Regex.MAXIMUM_MATCH_TIMEOUT]```, and the default match timeout is ```Regex.INFINITE_MATCH_TIMEOUT``` which means match timeout is switched off
>- Regex4j does not support ```array-like``` element accessing on ```Collection``` object, neither by index nor by name, alternatively, you can use ```#get(...)```to archive what you want, e.g. ```GroupCollection.get(int index)``` or ```GroupCollection.get(String name)```
>- Regex4j does not support method calling without ```()```, e.g. ```Group.Value``` should always be replaced by ```Group.value()```
>- The last but most important thing is: method names in Regex4j are all written in ```lowerCamelCase```, pay much more attention to this when you switch role from C# to Java

The following example demonstrates how to extract the protocol and port number from an ```URL``` and return the protocol followed by a colon followed by the port number.

* C#

```c#
using System;
using System.Text.RegularExpressions;

public class Application {
    public static void Main() {
        string url = "git://tony.guo@github.com:29418/regex4j.git";
        Regex r = new Regex(@"^(?<proto>\w+)://[^/]+?(?<port>:\d+)?/", RegexOptions.None, TimeSpan.FromMilliseconds(150));
        Match m = r.Match(url);

        if (m.Success) {
            Console.WriteLine(m.Groups["proto"].Value + m.Groups["port"].Value);
        }
    }
}
```

* Java

```java
import jxtras.regex.Regex;

public class Application {
    public static void main(String... args) {
        String url = "git://tony.guo@github.com:29418/regex4j.git";
        Regex r = new Regex("^(?<proto>\\w+)://[^/]+?(?<port>:\\d+)?/", RegexOptions.None, 150 /* millisecond */);
        Match m = r.match(url);

        if (m.success()) {
            System.out.println(m.groups().get("proto").value() + m.groups().get("port").value());
        }
    }
}
```

Both of the above 2 code snippets should display the same following output:

    git:29418

In this example, the regular expression pattern ```^(?<proto>\w+)://[^/]+?(?<port>:\d+)?/``` is interpreted as shown in the following table:

| Pattern             | Description
|:--------------------|:-----------
|```^```              | Begin the match at the start of the string.
|```(?<proto>\w+)```  | Match one or more word characters. Name this group proto.
|```://```            | Match a colon followed by two slash marks.
|```[^/]+?```         | Match one or more occurrences (but as few as possible) of any character other than a slash mark.
|```(?<port>:\d+)?``` | Match zero or one occurrence of a colon followed by one or more digit characters. Name this group port.
|```/```              | Match a slash mark.

Here're some topics on ```Regular Expression Optimization```, just for your reference.

>* http://www.javaworld.com/article/2077757/core-java/optimizing-regular-expressions-in-java.html
>* http://www.informit.com/guides/content.aspx?g=dotnet&seqNum=692
>* https://msdn.microsoft.com/en-us/library/gg578045(v=vs.110).aspx?cs-save-lang=1&cs-lang=vb#code-snippet-1

## Limitations

* Regex4j does not support ```CultureInfo```, though Java has its own corresponding class called ```Locale```, they're completely different
* Regex4j does not fully support ```Unicode```, it may contain some unexpected issues, so use at your own risk
* Regex4j does not support regex compilation, including ```RegexOptions.Compiled```, ```RegexOptions.Precompiled```, ```Regex.compileToAssemble()```

## Copyright

* Copyright (C) The JXTRAS Project and Contributors
* Copyright (C) .NET Foundation and Contributors
