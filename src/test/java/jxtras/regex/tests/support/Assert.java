package jxtras.regex.tests.support;

import java.util.Objects;

public class Assert {
    public static void Fail(String message) {
        throw new AssertionError("fail: " + message);
    }

    public static void True(boolean condition) {
        if (condition) {
            return;
        }
        throw new AssertionError("fail");
    }

    public static void True(boolean condition, String message) {
        if (condition) {
            return;
        }
        throw new AssertionError("fail: " + message);
    }

    public static void False(boolean condition) {
        if (!condition) {
            return;
        }
        throw new AssertionError("fail");
    }

    public static void False(boolean condition, String message) {
        if (!condition) {
            return;
        }
        throw new AssertionError("fail: " + message);
    }

    public static void Equal(Object A, Object B) {
        if (Objects.deepEquals(A, B)) {
            return;
        }
        throw new AssertionError("should be Equals");
    }

    public static void Throws(Class<? extends Throwable> cls) {
        throw new AssertionError(cls + " should be occurred");
    }
}
