## Welcome to Regex4j!

Regex4j is a Java port of System.Text.RegularExpressions module of Microsoft's .Net Core Libraries.

## Getting Started

The usage of regex4j is much samilar as System.Text.RegularExpressions:

* C# Version:

        public class Application {
            public static void Main() {
                string[] emailAddresses = { "david.jones@proseware.com", "d.j@server1.proseware.com",
                                      "jones@ms1.proseware.com", "j.@server1.proseware.com",
                                      "j@proseware.com9", "js#internal@proseware.com",
                                      "j_9@[129.126.118.1]", "j..s@proseware.com",
                                      "js*@proseware.com", "js@proseware..com",
                                      "js@proseware.com9", "j.s@server1.proseware.com",
                                       "\"j\\\"s\\\"\"@proseware.com", "js@contoso.中国" };
    
                foreach (var emailAddress in emailAddresses) {
                    if (isValidEmail(emailAddress)) {
                        Console.WriteLine("Valid: {0}", emailAddress);
                    } else {
                        Console.WriteLine("Invalid: {0}", emailAddress);
                    }
                }
            }
   
            private static final bool isValidEmail(string str) {
                if (String.IsNullOrEmpty(str))
                    return false;
    
                // Return true if str is in valid e-mail format.
                try {
                    return Regex.IsMatch(str,
                            @"^(?("")("".+?(?<!\\)""@)|(([0-9a-z]((\.(?!\.))|[-!#\$%&'\*\+/=\?\^`\{\}\|~\w])*)(?<=[0-9a-z])@))" +
                            @"(?(\[)(\[(\d{1,3}\.){3}\d{1,3}\])|(([0-9a-z][-\w]*[0-9a-z]*\.)+[a-z0-9][\-a-z0-9]{0,22}[a-z0-9]))$",
                            RegexOptions.IgnoreCase, TimeSpan.FromMilliseconds(250));
                } catch (RegexMatchTimeoutException) {
                    return false;
                }
            }
        }

* Java Version:

        public class Application {
            public static void main(String... args) {
                string[] emailAddresses = { "david.jones@proseware.com", "d.j@server1.proseware.com",
                                          "jones@ms1.proseware.com", "j.@server1.proseware.com",
                                          "j@proseware.com9", "js#internal@proseware.com",
                                          "j_9@[129.126.118.1]", "j..s@proseware.com",
                                          "js*@proseware.com", "js@proseware..com",
                                          "js@proseware.com9", "j.s@server1.proseware.com",
                                           "\"j\\\"s\\\"\"@proseware.com", "js@contoso.中国" };
        
                foreach (var emailAddress in emailAddresses) {
                    if (isValidEmail(emailAddress)) {
                        System.out.println("Valid: {0}", emailAddress);
                    } else {
                        System.out.println("Invalid: {0}", emailAddress);
                    }
                } 
            }
            
            private static final boolean isValidEmail(String str) {
                if (str == null || str.length() == 0)
                    return false;
        
                // Return true if str is in valid e-mail format.
                try {
                    return Regex.isMatch(str,
                            @"^(?("")("".+?(?<!\\)""@)|(([0-9a-z]((\.(?!\.))|[-!#\$%&'\*\+/=\?\^`\{\}\|~\w])*)(?<=[0-9a-z])@))" +
                            @"(?(\[)(\[(\d{1,3}\.){3}\d{1,3}\])|(([0-9a-z][-\w]*[0-9a-z]*\.)+[a-z0-9][\-a-z0-9]{0,22}[a-z0-9]))$",
                            RegexOptions.IgnoreCase, TimeSpan.FromMilliseconds(250));
                } catch (RegexMatchTimeoutException exc) {
                    return false;
                }
            }
        }

Both of these 2 examples display the same following output:

        Valid: david.jones@proseware.com
        Valid: d.j@server1.proseware.com
        Valid: jones@ms1.proseware.com
        Invalid: j.@server1.proseware.com
        Valid: j@proseware.com9
        Valid: js#internal@proseware.com
        Valid: j_9@[129.126.118.1]
        Invalid: j..s@proseware.com
        Invalid: js*@proseware.com
        Invalid: js@proseware..com
        Valid: js@proseware.com9
        Valid: j.s@server1.proseware.com
        Valid: "j\"s\""@proseware.com
        Valid: js@contoso.中国

n this example, the regular expression pattern ^(?(")(".+?(?<!\\)"@)|(([0-9a-z]((\.(?!\.))|[-!#\$%&'\*\+/=\?\^`\{\}\|~\w])*)(?<=[0-9a-z])@))(?(\[)(\[(\d{1,3}\.){3}\d{1,3}\])|(([0-9a-z][-\w]*[0-9a-z]*\.)+[a-z0-9][\-a-z0-9]{0,22}[a-z0-9]))$ is interpreted as shown in the following table. Note that the regular expression is compiled using the RegexOptions.IgnoreCase flag.

| Pattern | Description
|:--------|:-----------
^ | Begin the match at the start of the string.
(?(") | Determine whether the first character is a quotation mark. (?(") is the beginning of an alternation construct.
(?("")("".+?(?<!\\)""@) | If the first character is a quotation mark, match a beginning quotation mark followed by at least one occurrence of any character, followed by an ending quotation mark. The ending quotation mark must not be preceded by a backslash character (\). (?<! is the beginning of a zero-width negative lookbehind assertion. The string should conclude with an at sign (@).
|(([0-9a-z] | If the first character is not a quotation mark, match any alphabetic character from a to z or A to Z (the comparison is case insensitive), or any numeric character from 0 to 9.
(\.(?!\.)) | If the next character is a period, match it. If it is not a period, look ahead to the next character and continue the match. (?!\.) is a zero-width negative lookahead assertion that prevents two consecutive periods from appearing in the local part of an email address.
|[-!#\$%&'\*\+/=\?\^`\{\}\|~\w] | If the next character is not a period, match any word character or one of the following characters: -!#$%'*+=?^`{}|~.
((\.(?!\.))|[-!#\$%'\*\+/=\?\^`\{\}\|~\w])* | Match the alternation pattern (a period followed by a non-period, or one of a number of characters) zero or more times.
@ | Match the @ character.
(?<=[0-9a-z]) | Continue the match if the character that precedes the @ character is A through Z, a through z, or 0 through 9. The (?<=[0-9a-z]) construct defines a zero-width positive lookbehind assertion.
(?(\[) | Check whether the character that follows @ is an opening bracket.
(\[(\d{1,3}\.){3}\d{1,3}\]) | If it is an opening bracket, match the opening bracket followed by an IP address (four sets of one to three digits, with each set separated by a period) and a closing bracket.
|(([0-9a-z][-\w]*[0-9a-z]*\.)+ | If the character that follows @ is not an opening bracket, match one alphanumeric character with a value of A-Z, a-z, or 0-9, followed by zero or more occurrences of a word character or a hyphen, followed by zero or one alphanumeric character with a value of A-Z, a-z, or 0-9, followed by a period. This pattern can be repeated one or more times, and must be followed by the top-level domain name.
[a-z0-9][\-a-z0-9]{0,22}[a-z0-9])) | The top-level domain name must begin and end with an alphanumeric character (a-z, A-Z, and 0-9). It can also include from zero to 22 ASCII characters that are either alphanumeric or hyphens.
$ | End the match at the end of the string.

## Limitations

* Regex4j does not support CultureInfo, though Java owns a coresponding class called Locale, they're different definately!
* Regex4j does not support verbatim string literals, that's to say, when you translate some patterns to Java, you need to add "\" explicitly!
* Regex4j does not completely support Unicode, it may contain some unexpected issue, so use at your own risk!
