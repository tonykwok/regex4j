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

import jxtras.regex.support.R;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>The Regex class represents the .NET Framework's regular expression engine. It can be used to
 * quickly parse large amounts of text to find specific character patterns; to extract, edit,
 * replace, or delete text substrings; and to add the extracted strings to a collection to generate
 * a report.<p/>
 * <p/>
 * <p>The Regex class represents an immutable, compiled regular expression. Also contains static
 * methods that allow use of regular expressions without instantiating a Regex explicitly.</p>
 */
public class Regex implements Serializable {
    private static final long serialVersionUID = 5073258162644648461L;

    // MAXIMUM_MATCH_TIMEOUT specifies the maximum acceptable match timeout.
    private static final int MAXIMUM_MATCH_TIMEOUT = Integer.MAX_VALUE - 1;

    // INFINITE_MATCH_TIMEOUT specifies that match timeout is switched OFF.
    // It allows for faster code paths compared to simply having a very large timeout.
    public static final int INFINITE_MATCH_TIMEOUT = -1;

    // DEFAULT_MATCH_TIMEOUT specifies the match timeout to use if no other timeout was specified.
    // Typically, it is set to INFINITE_MATCH_TIMEOUT.
    private static final int DEFAULT_MATCH_TIMEOUT = INFINITE_MATCH_TIMEOUT;

    // The string pattern provided
    String pattern;

    // The top-level options from the options string.
    int options;

    // Timeout for the execution of this regex
    int matchTimeout;

    // if captures are sparse, this is the Map capnum->index
    Map<Integer, Integer> caps;
    // if named captures are used, this maps names->index
    Map<String, Integer> capnames;

    // if captures are sparse or named captures are used, this is the sorted list of names
    String[] capslist;
    // the size of the capture array
    int capsize;

    // cached runner
    ExclusiveReference<RegexRunner> runnerref;
    // cached parsed replacement pattern
    SharedReference<RegexReplacement> replref;
    // if interpreted, this is the code for RegexInterpreter
    RegexCode code;
    boolean refsInitialized = false;

    // the cache of code and factories that are currently loaded
    static LinkedList<CachedCodeEntry> livecode = new LinkedList<CachedCodeEntry>();
    static int cacheSize = 15;

    /**
     * Creates a new instance of the Regex class for the specified
     * regular expression.
     */
    public Regex(String pattern) {
        this(pattern, RegexOptions.None, DEFAULT_MATCH_TIMEOUT, false);
    }

    /**
     * Creates a new instance of the Regex class for the specified
     * regular expression, with options that modify the pattern.
     *
     * @see RegexOptions
     */
    public Regex(String pattern, int options) {
        this(pattern, options, DEFAULT_MATCH_TIMEOUT, false);
    }

    /**
     * Creates a new instance of the Regex class for the specified
     * regular expression, with options that modify the pattern and a
     * value that specifies how long a pattern matching method
     * should attempt a match before it times out.
     */
    public Regex(String pattern, int options, int matchTimeout) {
        this(pattern, options, matchTimeout, false);
    }

    private Regex(String pattern, int options, int matchTimeout, boolean useCache) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null.");
        }

        if (options < RegexOptions.None || ((options) >> RegexOptions.MaxOptionShift) != 0) {
            throw new IllegalArgumentException("options is invalid.");
        }

        if ((options & RegexOptions.ECMAScript) != 0
                && (options & ~(RegexOptions.ECMAScript |
                                RegexOptions.IgnoreCase |
                                RegexOptions.Multiline |
                                RegexOptions.CultureInvariant |
                                RegexOptions.Debug)) != 0) {
            throw new IllegalArgumentException("options is invalid.");
        }

        validateMatchTimeout(matchTimeout);

        this.pattern = pattern;
        this.options = options;
        this.matchTimeout = matchTimeout;

        // Try to look up this regex in the cache.
        // We do this regardless of whether useCache is true since there's really no reason not to.
        Locale locale;
        if ((options & RegexOptions.CultureInvariant) != 0) {
            locale = Locale.ROOT; // "en_US"
        } else {
            locale = Locale.getDefault();
        }

        CachedCodeEntryKey key = new CachedCodeEntryKey(options, locale.toString(), pattern);
        CachedCodeEntry cached = lookupCachedAndUpdate(key);
        if (cached == null) {
            // Parse the input
            RegexTree tree = RegexParser.parse(pattern, options);

            // Extract the relevant information
            capnames = tree._capnames;
            capslist = tree._capslist;
            code = RegexWriter.write(tree);
            caps = code._caps;
            capsize = code._capsize;

            initializeReferences();

            if (useCache) {
                cacheCode(key);
            }
        } else {
            caps = cached._caps;
            capnames = cached._capnames;
            capslist = cached._capslist;
            capsize = cached._capsize;
            code = cached._code;
            runnerref = cached._runnerref;
            replref = cached._replref;
            refsInitialized = true;
        }
    }

    /**
     * Validates that the specified match timeout value is valid.
     * The valid range is {@code 0} &lt; matchTimeout &lt;= {@code Integer.MAX_VALUE}.
     *
     * @param matchTimeout The timeout value to validate.
     * @throw IllegalArgumentException If the specified timeout is not within a valid range.
     */
    protected static void validateMatchTimeout(int matchTimeout) {
        if (INFINITE_MATCH_TIMEOUT == matchTimeout) {
            return;
        }

        // Change this to make sure timeout is not longer then Environment.Ticks cycle length:
        if (0 < matchTimeout && matchTimeout <= MAXIMUM_MATCH_TIMEOUT)
            return;

        throw new IllegalArgumentException("matchTimeout");
    }

   /**
    * Escape a minimal set of metacharacters (\, *, +, ?, |, {, [, (, ), ^, $, ., #, and
    * whitespace) by replacing them with their \ codes. This converts a string so that
    * it can be used as a constant within a regular expression safely. (Note that the
    * reason # and whitespace must be escaped is so the string can be used safely
    * within an expression parsed with x mode. If future Regex features add
    * additional metacharacters, developers should depend on Escape to escape those
    * characters as well.)
    */
    public static String escape(String str) {
        if (str == null) {
            throw new IllegalArgumentException("str cannot be null.");
        }

        return RegexParser.escape(str);
    }

    /**
     * Unescapes any escaped characters in the input string.
     */
    public static String unescape(String str) {
        if (str == null) {
            throw new IllegalArgumentException("str cannot be null.");
        }

        return RegexParser.unescape(str);
    }

    /**
     * Gets the maximum number of entries in the current static cache of compiled regular expressions.
     */
    public static int cacheSize() {
        return cacheSize;
    }

    /**
     * Sets the maximum number of entries in the current static cache of compiled regular expressions.
     */
    public static void setCacheSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size cannot be negative.");
        }

        cacheSize = size;
        if (livecode.size() > cacheSize) {
            synchronized (livecode) {
                while (livecode.size() > cacheSize) {
                    livecode.removeLast();
                }
            }
        }
    }

    /**
     * Gets the options that were passed into the Regex constructor.
     */
    public int options() {
        return options;
    }

    /**
     * Gets the time-out interval of the current instance.
     */
    public int matchTimeout() {
        return matchTimeout;
    }

    /**
     * Gets a value that indicates whether the regular expression searches from right to left.
     *
     * @return {@code true} if the regex is leftward.
     */
    public boolean rightToLeft() {
        return useOptionR();
    }

    /**
     * Gets the regular expression pattern that was passed into the Regex constructor.
     */
    @Override
    public String toString() {
        return pattern;
    }

    /**
     * Gets an array of the group names that are used to capture groups
     * in the regular expression. Only needed if the regex is not known until
     * runtime, and one wants to extract captured groups. (Probably unusual,
     * but supplied for completeness.)
     *
     * @retrun The GroupNameCollection for the regular expression. This collection contains the
     * set of strings used to name capturing groups in the expression.
     */
    public String[] getGroupNames() {
        String[] result;

        if (capslist == null) {
            int max = capsize;
            result = new String[max];

            for (int i = 0; i < max; i++) {
                result[i] = Integer.toString(i);
                // TODO: Convert.ToString(i, CultureInfo.InvariantCulture);
            }
        } else {
            result = new String[capslist.length];
            System.arraycopy(capslist, 0, result, 0, capslist.length);
        }

        return result;
    }

    /**
     * Gets an array of the group numbers that are used to capture groups
     * in the regular expression. Only needed if the regex is not known until
     * runtime, and one wants to extract captured groups. (Probably unusual,
     * but supplied for completeness.)
     *
     * @return The integer group number corresponding to a group name.
     */
    public int[] getGroupNumbers() {
        int[] result;

        if (caps == null) {
            int max = capsize;
            result = new int[max];

            for (int i = 0; i < max; i++) {
                result[i] = i;
            }
        } else {
            result = new int[caps.size()];
            for (Map.Entry<Integer, Integer> entry : caps.entrySet()) {
                result[entry.getValue()] = entry.getKey();
            }
        }

        return result;
    }

    /**
     * Given a group number, maps it to a group name. Note that numbered
     * groups automatically get a group name that is the decimal string
     * equivalent of its number.
     *
     * @return A group name that corresponds to a group number or
     * {@code null} if the number is not a recognized group number.
     */
    public String groupNameFromNumber(int i) {
        if (capslist == null) {
            if (i >= 0 && i < capsize) {
                return Integer.toString(i);
                // TODO: i.ToString(CultureInfo.InvariantCulture);
            }
            return "";
        } else {
            if (caps != null) {
                Integer ret = caps.get(i);
                if (ret == null) {
                    return "";
                }
                i = ret;
            }

            if (i >= 0 && i < capslist.length) {
                return capslist[i];
            }
            return "";
        }
    }

    /**
     * Given a group name, maps it to a group number. Note that nubmered
     * groups automatically get a group name that is the decimal string
     * equivalent of its number.
     *
     * @return A group number that corresponds to a group name or
     * {@code -1} if the name is not a recognized group name.
     */
    public int groupNumberFromName(String name) {
        int result = -1;

        if (name == null)
            throw new IllegalArgumentException("name cannot be null.");

        // look up name if we have a hashmap of names
        if (capnames != null) {
            Integer ret = capnames.get(name);
            if (ret == null) {
                return -1;
            }
            return ret;
        }

        // convert to an int if it looks like a number
        // TODO: Is it can be simplified to use Integer.parse(String) instead?
        result = 0;
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i); // TODO: name[i]

            if (ch > '9' || ch < '0') {
                return -1;
            }

            result *= 10;
            result += (ch - '0');
        }

        // return int if it's in range
        if (result >= 0 && result < capsize) {
            return result;
        }
        return -1;
    }

    /**
     * Searches the input string for one or more occurrences of the text
     * supplied in the pattern parameter.
     */
    public static boolean isMatch(String input, String pattern) {
        return isMatch(input, pattern, RegexOptions.None, DEFAULT_MATCH_TIMEOUT);
    }

    /**
     * Searches the input string for one or more occurrences of the text
     * supplied in the pattern parameter with matching options supplied in the options
     * parameter.
     */
    public static boolean isMatch(String input, String pattern, int options) {
        return isMatch(input, pattern, options, DEFAULT_MATCH_TIMEOUT);
    }

    public static boolean isMatch(String input, String pattern, int options, int matchTimeout) {
        return new Regex(pattern, options, matchTimeout, true).isMatch(input);
    }

    /**
     * Searches the input string for one or more matches using the previous pattern
     * and options.
     *
     * @return {@code true} if the regex finds a match within the specified string.
     */
    public boolean isMatch(String input) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return isMatch(input, useOptionR() ? input.length() : 0);
    }

    /**
     * Searches the input string for one or more matches using the previous pattern
     * and options, with a new starting position.
     *
     * @return {@code true} if the regex finds a match after the specified position
     * (proceeding leftward if the regex is leftward and rightward otherwise)
     */
    public boolean isMatch(String input, int startAt) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return (null == run(true, -1, input, 0, input.length(), startAt));
    }

    /**
     * Searches the input string for one or more occurrences of the text
     * supplied in the pattern parameter.
     */
    public static Match match(String input, String pattern) {
        return match(input, pattern, RegexOptions.None, DEFAULT_MATCH_TIMEOUT);
    }

    /**
     * Searches the input string for one or more occurrences of the text
     * supplied in the pattern parameter. Matching is modified with an option
     * string.
     */
    public static Match match(String input, String pattern, int options) {
        return match(input, pattern, options, DEFAULT_MATCH_TIMEOUT);
    }

    public static Match match(String input, String pattern, int options, int matchTimeout) {
        return new Regex(pattern, options, matchTimeout, true).match(input);
    }

    /**
     * Finds the first match for the regular expression starting at the beginning
     * of the string (or at the end of the string if the regex is leftward).
     *
     * @return The precise result as a {@link Match} object.
     */
    public Match match(String input) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return match(input, useOptionR() ? input.length() : 0);
    }

    /**
     * Finds the first match, starting at the specified position.
     *
     * @return The precise result as a {@link Match} object.
     */
    public Match match(String input, int startAt) {
        if (input == null)
            throw new IllegalArgumentException("input must not be NULL");

        return run(false, -1, input, 0, input.length(), startAt);
    }

    /**
     * Finds the first match, restricting the search to the specified interval of
     * the char array.
     *
     * @return The precise result as a {@link Match} object.
     */
    public Match match(String input, int beginning, int length) {
        if (input == null)
            throw new IllegalArgumentException("input must not be NULL");

        return run(false, -1, input, beginning, length,
                useOptionR() ? beginning + length : beginning);
    }


    /**
     * Returns all the successful matches as if Match were called iteratively numerous times.
     */
    public static MatchCollection matches(String input, String pattern) {
        return matches(input, pattern, RegexOptions.None, DEFAULT_MATCH_TIMEOUT);
    }

    /**
     * Returns all the successful matches as if Match were called iteratively numerous times.
     */
    public static MatchCollection matches(String input, String pattern, int options) {
        return matches(input, pattern, options, DEFAULT_MATCH_TIMEOUT);
    }

    public static MatchCollection matches(String input, String pattern, int options, int matchTimeout) {
        return new Regex(pattern, options, matchTimeout, true).matches(input);
    }

    /**
     * Finds the first match for the regular expression starting at the beginning
     * of the string Enumerator(or at the end of the string if the regex is leftward).
     *
     * @return All the successful matches as if Match was called iteratively numerous times.
     */
    public MatchCollection matches(String input) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return matches(input, useOptionR() ? input.length() : 0);
    }

    /**
     * Finds the first match, starting at the specified position.
     *
     * @return All the successful matches as if Match was called iteratively numerous.
     */
    public MatchCollection matches(String input, int startAt) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return new MatchCollection(this, input, 0, input.length(), startAt);
    }

    /**
     * Replaces all occurrences of the {@code pattern} with the {@code replacement} pattern,
     * starting at the first character in the input string.
     */
    public static String replace(String input, String pattern, String replacement) {
        return replace(input, pattern, replacement, RegexOptions.None, DEFAULT_MATCH_TIMEOUT);
    }

    /**
     * Replaces all occurrences of the {@code pattern} with the {@code replacement} pattern,
     * starting at the first character in the input string.
     */
    public static String replace(String input, String pattern, String replacement, int options) {
        return replace(input, pattern, replacement, options, DEFAULT_MATCH_TIMEOUT);
    }

    /**
     * Replaces all occurrences of the {@code pattern} with the {@code replacement} pattern,
     * starting at the first character in the input string.
     */
    public static String replace(String input, String pattern, String replacement, int options, int matchTimeout) {
        return new Regex(pattern, options, matchTimeout, true).replace(input, replacement);
    }

    /**
     * Replaces all occurrences of the (previously defined) {@code pattern} with the
     * {@code replacement} pattern, starting at the first character in the input string.
     */
    public String replace(String input, String replacement) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return replace(input, replacement, -1, useOptionR() ? input.length() : 0);
    }

    /**
     * Replaces all occurrences of the (previously defined) {@code pattern} with the
     * {@code replacement} pattern, starting at the first character in the input string.
     */
    public String replace(String input, String replacement, int count) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return replace(input, replacement, count, useOptionR() ? input.length() : 0);
    }

    /**
     * Replaces all occurrences of the (previously defined) {@code pattern} with the
     * {@code replacement} pattern, starting at the character position {@code startAt}.
     */
    public String replace(String input, String replacement, int count, int startAt) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        if (replacement == null)
            throw new IllegalArgumentException("replacement cannot be null.");

        // a little code to grab a cached parsed replacement object
        RegexReplacement repl = replref.get();

        if (repl == null || !repl.pattern().equals(replacement)) {
            repl = RegexParser.parseReplacement(replacement, caps, capsize, capnames, options);
            replref.cache(repl);
        }

        return repl.replace(this, input, count, startAt);
    }

    /**
     * Replaces all occurrences of the {@code pattern} with the {@code evaluator} object.
     */
    public static String replace(String input, String pattern, MatchEvaluator evaluator) {
        return replace(input, pattern, evaluator, RegexOptions.None, DEFAULT_MATCH_TIMEOUT);
    }

    /**
     * Replaces all occurrences of the {@code pattern} with the {@code evaluator} object, starting
     * at the first character position.
     */
    public static String replace(String input, String pattern, MatchEvaluator evaluator, int options) {
        return replace(input, pattern, evaluator, options, DEFAULT_MATCH_TIMEOUT);
    }

    /**
     * Replaces all occurrences of the {@code pattern} with the {@code evaluator} object, starting
     * at the first character position.
     */
    public static String replace(String input, String pattern, MatchEvaluator evaluator, int options, int matchTimeout) {
        return new Regex(pattern, options, matchTimeout, true).replace(input, evaluator);
    }

    /**
     * Replaces all occurrences of the {@code pattern} with the recent {@code replacement} pattern,
     * starting at the first character position.
     */
    public String replace(String input, MatchEvaluator evaluator) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return replace(input, evaluator, -1, useOptionR() ? input.length() : 0);
    }

    /**
     * Replaces all occurrences of the {@code pattern} with the recent {@code replacement} pattern,
     * starting at the first character position.
     */
    public String replace(String input, MatchEvaluator evaluator, int count) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return replace(input, evaluator, count, useOptionR() ? input.length() : 0);
    }

    /**
     * Replaces all occurrences of the (previously defined) {@code pattern} with the
     * {@code evaluator} object, starting at the character position {@code startAt}.
     */
    public String replace(String input, MatchEvaluator evaluator, int count, int startAt) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return RegexReplacement.replace(evaluator, this, input, count, startAt);
    }


    /**
     * Splits the {@code input} string at the position defined by {@code pattern}.
     */
    public static String[] split(String input, String pattern) {
        return split(input, pattern, RegexOptions.None, DEFAULT_MATCH_TIMEOUT);
    }

    /**
     * Splits the {@code input} string at the position defined by {@code pattern}.
     */
    public static String[] split(String input, String pattern, int options) {
        return split(input, pattern, options, DEFAULT_MATCH_TIMEOUT);
    }

    /**
     * Splits the {@code input} string at the position defined by {@code pattern}.
     */
    public static String[] split(String input, String pattern, int options, int matchTimeout) {
        return new Regex(pattern, options, matchTimeout, true).split(input);
    }

    /**
     * Splits the {@code input} string at the position defined by a previous {@code pattern}.
     */
    public String[] split(String input) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return split(input, 0, useOptionR() ? input.length() : 0);
    }

    /**
     * Splits the {@code input} string at the position defined by a previous {@code pattern}.
     */
    public String[] split(String input, int count) {
        if (input == null)
            throw new IllegalArgumentException("input cannot be null.");

        return RegexReplacement.split(this, input, count, useOptionR() ? input.length() : 0);
    }

    /**
     * Splits the {@code input} string at the position defined by a previous {@code pattern}.
     */
    public String[] split(String input, int count, int startAt) {
        if (input == null)
            throw new IllegalArgumentException("input must not be NULL");

        return RegexReplacement.split(this, input, count, startAt);
    }

    void initializeReferences() {
        if (refsInitialized)
            throw new IllegalStateException(R.OnlyAllowedOnce);

        refsInitialized = true;
        runnerref = new ExclusiveReference<RegexRunner>();
        replref = new SharedReference<RegexReplacement>();
    }

    /*
     * Internal worker called by all the public APIs
     */
    Match run(boolean quick, int prevlen, String input, int beginning, int length, int startat) {
        if (startat < 0 || startat > input.length())
            throw new IllegalArgumentException(R.BeginIndexNotNegative);

        if (length < 0 || length > input.length())
            throw new IllegalArgumentException(R.LengthNotNegative);

        // There may be a cached runner; grab ownership of it if we can.
        RegexRunner runner = runnerref.get();

        // Create a RegexRunner instance if we need to
        if (runner == null) {
            runner = new RegexInterpreter(code,
                    useOptionInvariant() ? Locale.ROOT : Locale.getDefault()
            );
        }

        Match match = null;
        try {
            // Do the scan starting at the requested position
            match = runner.scan(this, input, beginning, beginning + length, startat, prevlen,
                    quick, matchTimeout);
        } finally {
            // Release or fill the cache slot
            runnerref.release(runner);
        }

        if (isDebugEnabled() && match != null) {
            match.dump();
        }

        return match;
    }

    /*
     * Find code cache based on options+pattern
     */
    private static CachedCodeEntry lookupCachedAndUpdate(CachedCodeEntryKey key) {
        synchronized (livecode) {
            for (CachedCodeEntry current : livecode) {
                if (current != null && current._key.equals(key)) {
                    // If we find an entry in the cache, move it to the head at the same time.
                    livecode.remove(current);
                    livecode.addFirst(current);
                    return current;
                }
            }
//            for (LinkedListNode<CachedCodeEntry> current = livecode.First; current != null; current = current.Next) {
//                if (current.Value._key == key) {
//                    // If we find an entry in the cache, move it to the head at the same time.
//                    livecode.Remove(current);
//                    livecode.AddFirst(current);
//                    return current.Value;
//                }
//            }
        }

        return null;
    }

    /*
     * Add current code to the cache
     */
    private void cacheCode(CachedCodeEntryKey key) {
        synchronized (livecode) {
            // first look for it in the cache and then move it to the head
            for (CachedCodeEntry current : livecode) {
                if (current != null && current._key.equals(key)) {
                    livecode.remove(current);
                    livecode.addFirst(current);
                }
            }

            // it wasn't in the cache, so we'll add a new one.  Shortcut out for the case where cacheSize is zero.
            if (cacheSize != 0) {
                CachedCodeEntry newCached = new CachedCodeEntry(key, capnames, capslist, code,
                        caps, capsize, runnerref, replref);
                livecode.addFirst(newCached);
                if (livecode.size() > cacheSize) {
                    livecode.removeLast();
                }
            }
        }
    }

    /*
     * True if the R option was set
     */
    protected boolean useOptionR() {
        return (options & RegexOptions.RightToLeft) != 0;
    }

    /*
     * True if the CultureInvariant option was set
     */
    boolean useOptionInvariant() {
        return (options & RegexOptions.CultureInvariant) != 0;
    }

    /*
     * True if the regex has debugging enabled
     */
    boolean isDebugEnabled() {
        return (options & RegexOptions.Debug) != 0;
    }
}

/*
 * Used as a key for CacheCodeEntry
 */
final class CachedCodeEntryKey {
    private final int _options;
    private final String _cultureKey;
    private final String _pattern;

    CachedCodeEntryKey(int options, String cultureKey, String pattern) {
        _options = options;
        _cultureKey = cultureKey;
        _pattern = pattern;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CachedCodeEntryKey)) {
            return false;
        }

        CachedCodeEntryKey key = (CachedCodeEntryKey) obj;
        return this._options == key._options && this._cultureKey.equals(key._cultureKey)
                && this._pattern.equals(key._pattern);
    }

    @Override
    public int hashCode() {
        return (_options ^ _cultureKey.hashCode() ^ _pattern.hashCode());
    }
}

/*
 * Used to cache byte codes.
 */
final class CachedCodeEntry {
    final CachedCodeEntryKey _key;
    final RegexCode _code;

    final Map<Integer, Integer> _caps;
    final Map<String, Integer> _capnames;

    final String[] _capslist;
    final int _capsize;
    final ExclusiveReference<RegexRunner> _runnerref;
    final SharedReference<RegexReplacement> _replref;

    CachedCodeEntry(CachedCodeEntryKey key, Map<String, Integer> capnames, String[] capslist,
                    RegexCode code, Map<Integer, Integer> caps, int capsize,
                    ExclusiveReference<RegexRunner> runner, SharedReference<RegexReplacement> repl) {
        _key = key;
        _capnames = capnames;
        _capslist = capslist;

        _code = code;
        _caps = caps;
        _capsize = capsize;

        _runnerref = runner;
        _replref = repl;
    }
}

/*
 * Used to cache one exclusive runner reference
 */
final class ExclusiveReference<T> {
    T _ref;
    Object _obj;
    AtomicBoolean _locked = new AtomicBoolean(false);

    /**
     * Return an object and grab an exclusive lock.
     * <p/>
     * If the exclusive lock can't be obtained, null is returned;
     * if the object can't be returned, the lock is released.
     */
    T get() {
        // try to obtain the lock
        if (false == _locked.getAndSet(true)) {
            // grab reference
            T obj = _ref;

            // release the lock and return null if no reference
            if (obj == null) {
                _locked.set(false);
                return null;
            }

            // remember the reference and keep the lock
            _obj = obj;
            return obj;
        }

        return null;
    }

    /**
     * Release an object back to the cache
     * <p/>
     * If the object is the one that's under lock, the lock
     * is released.
     * <p/>
     * If there is no cached object, then the lock is obtained
     * and the object is placed in the cache.
     */
    void release(T obj) {
        if (obj == null)
            throw new IllegalArgumentException("obj cannot be null.");

        // if this reference owns the lock, release it
        if (_obj == obj) {
            _obj = null;
            _locked.set(false);
            return;
        }

        // if no reference owns the lock, try to cache this reference
        if (_obj == null) {
            // try to obtain the lock
            if (false == _locked.getAndSet(true)) {
                // if there's really no reference, cache this reference
                if (_ref == null)
                    _ref = obj;

                // release the lock
                _locked.set(false);
                return;
            }
        }
    }
}

/**
 * Used to cache a weak reference in a thread-safe way.
 */
final class SharedReference<T> {
    WeakReference<T> _ref = new WeakReference<T>(null);
    AtomicBoolean _locked = new AtomicBoolean(false);

    /**
     * Return an object from a WeakReference, protected by a lock.
     * <p/>
     * If the exclusive lock can't be obtained, null is returned;
     * <p/>
     * Note that _ref.Target is referenced only under the protection
     * of the lock. (Is this necessary?)
     */
    T get() {
        if (false == _locked.getAndSet(true)) {
            T obj = _ref.get();
            _locked.set(false);
            return obj;
        }
        return null;
    }

    /**
     * Suggest an object into a WeakReference, protected by a lock.
     * <p/>
     * Note that _ref.Target is referenced only under the protection
     * of the lock. (Is this necessary?)
     */
    void cache(T obj) {
        if (false == _locked.getAndSet(true)) {
            _ref = new WeakReference<T>(obj);
            _locked.set(false);
        }
    }
}
