package jxtras.regex.support;

public class R {
    // strings used in Regex, RegexReplacement
    public static final String ArgumentNull_ArrayWithNullElements = "The array cannot contain null elements.";
    public static final String OnlyAllowedOnce = "This operation is only allowed once per object.";
    public static final String BeginIndexNotNegative = "Start index cannot be less than 0 or greater than input length.";
    public static final String LengthNotNegative = "Length cannot be less than 0 or exceed input length.";

    // strings used in RegexCompiler, RegexInterpreter
    public static final String UnimplementedState = "Unimplemented state.";

    // strings used in RegexFCD, RegexCode, RegexWriter
    public static final String UnexpectedOpcode = "Unexpected opcode in regular expression generation: {0}.";

    // strings used in RegexMatch
    public static final String NoResultOnFailed = "Result cannot be called on a failed Match.";

    // strings used in RegexParser
    public static final String UnterminatedBracket = "Unterminated [] set.";
    public static final String TooManyParents = "Too many )'s.";
    public static final String NestedQuantify = "Nested quantifier {0}.";
    public static final String QuantifyAfterNothing = "Quantifier {x,y} following nothing.";
    public static final String InternalError = "Internal error in ScanRegex.";
    public static final String IllegalRange = "Illegal {x,y} with x > y.";
    public static final String NotEnoughParens = "Not enough )'s.";
    public static final String BadClassInCharRange = "Cannot include class \\{0} in character range.";
    public static final String ReversedCharRange = "[x-y] range in reverse order.";
    public static final String UndefinedReference = "(?({0}) ) reference to undefined group.";
    public static final String MalformedReference = "(?({0}) ) malformed.";
    public static final String UnrecognizedGrouping = "Unrecognized grouping construct.";
    public static final String UnterminatedComment = "Unterminated (?#...) comment.";
    public static final String IllegalEndEscape = "Illegal \\ at end of pattern.";
    public static final String MalformedNameRef = "Malformed \\k<...> named back reference.";
    public static final String UndefinedBackref = "Reference to undefined group number {0}.";
    public static final String UndefinedNameRef = "Reference to undefined group name {0}.";
    public static final String TooFewHex = "Insufficient hexadecimal digits.";
    public static final String MissingControl = "Missing control character.";
    public static final String UnrecognizedControl = "Unrecognized control character.";
    public static final String UnrecognizedEscape = "Unrecognized escape sequence \\{0}.";
    public static final String IllegalCondition = "Illegal conditional (?(...)) expression.";
    public static final String TooManyAlternates = "Too many | in (?()|).";
    public static final String MakeException = "parsing \"{0} - {1}\"";
    public static final String IncompleteSlashP = "Incomplete \\p{X} character escape.";
    public static final String MalformedSlashP = "Malformed \\p{X} character escape.";
    public static final String InvalidGroupName = "Invalid group name: Group names must begin with a word character.";
    public static final String CapnumNotZero = "Capture number cannot be zero.";
    public static final String AlternationCantCapture = "Alternation conditions do not capture and cannot be named.";
    public static final String AlternationCantHaveComment = "Alternation conditions cannot be comments.";
    public static final String CaptureGroupOutOfRange = "Capture group numbers must be less than or equal to Int32.MaxValue.";
    public static final String SubtractionMustBeLast = "A subtraction must be the last element in a character class.";

    // strings used in RegexCharClass
    public static final String UnknownProperty = "Unknown property '{0}'.";

    // strings used in RegexReplacement
    public static final String ReplacementError = "Replacement pattern error.";
    public static final String CountTooSmall = "Count cannot be less than -1.";

    // string used in Regex*Collection
    public static final String EnumNotStarted = "Enumeration has either not started or has already finished.";
    public static final String Arg_InvalidArrayType = "Target array type is not compatible with the type of items in the collection.";
    public static final String Arg_RankMultiDimNotSupported = "Only single dimensional arrays are supported for the requested action.";

    // string used in RegexMatchTimeoutException
    public static final String RegexMatchTimeoutException_Occurred = "The RegEx engine has timed out while trying to match a pattern to an input string. This can occur for many reasons, including very large inputs or excessive backtracking caused by nested quantifiers, back-references and other factors.";

    // illegal default timeout:
    public static final String IllegalDefaultRegexMatchTimeoutInAppDomain = "System Property '%s' contains an invalid value or object for specifying a default matching timeout for Regex.";

    public static final String format(String format, Object... args) {
        return String.format(format, args);
    }
}