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

package jxtras.regex.runner;

import jxtras.regex.*;
import jxtras.regex.tests.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestCasesRunner {

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
    }
}