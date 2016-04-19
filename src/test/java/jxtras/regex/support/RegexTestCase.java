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

package jxtras.regex.support;

import jxtras.regex.Match;
import jxtras.regex.Regex;
import jxtras.regex.RegexOptions;

import java.util.Locale;

public class RegexTestCase {
    private String _pattern;
    private String _input;
    private int _options;
    private String[] _expectedGroups;
    private Class<? extends Throwable> _expectedExceptionType;
    private Locale _cultureInfo;

    public RegexTestCase(String pattern, String input, String... expectedGroups) {
        this(pattern, RegexOptions.None, input, expectedGroups);
    }

    public RegexTestCase(String pattern, String input, Locale culture, String... expectedGroups) {
        this(pattern, RegexOptions.None, culture, input, expectedGroups);
    }

    public RegexTestCase(String pattern, int options, String input, String... expectedGroups) {
        _pattern = pattern;
        _options = options;
        _input = input;
        _expectedGroups = expectedGroups;
        _cultureInfo = null;
    }

    public RegexTestCase(String pattern, int options, Locale culture, String input, String... expectedGroups) {
        _pattern = pattern;
        _options = options;
        _input = input;
        _expectedGroups = expectedGroups;
        _cultureInfo = culture;
    }

    public RegexTestCase(String pattern, Class<? extends Throwable> expectedExceptionType) {
        this(pattern, RegexOptions.None, expectedExceptionType);
    }

    public RegexTestCase(String pattern, int options, Class<? extends Throwable> expectedExceptionType) {
        _pattern = pattern;
        _options = options;
        _expectedExceptionType = expectedExceptionType;
    }

    public String Pattern() {
        return _pattern;
    }

    public String Input() {
        return _input;
    }

    public int Options() {
        return _options;
    }

    public String[] ExpectedGroups() {
        return _expectedGroups;
    }

    public Class<? extends Throwable> ExpectedExceptionType() {
        return _expectedExceptionType;
    }

    public boolean ExpectException() {
        return null != _expectedExceptionType;
    }

    public boolean ExpectSuccess() {
        return null != _expectedGroups && _expectedGroups.length != 0;
    }

    public boolean Run() {
        Regex r;
        Match m;
        Locale originalCulture = null;

        try {
            if (null != _cultureInfo) {
                originalCulture = Locale.getDefault();
                Locale.setDefault(_cultureInfo);
            }


            try {
                r = new Regex(_pattern, _options);

                if (ExpectException()) {
                    System.out.printf("Err_09872anba! Expected Regex to throw %s exception and none was thrown", _expectedExceptionType);
                    return false;
                }
            } catch (Exception e) {
                if (ExpectException() && e.getClass() == _expectedExceptionType) {
                    return true;
                } else if (ExpectException()) {
                    System.out.printf("Err_4980asu! Expected exception of type %s and instead the following was thrown: \n%s", _expectedExceptionType, e);
                    return false;
                } else {
                    System.out.printf("Err_78394ayuua! Expected no exception to be thrown and the following was thrown: \n%s", e);
                    return false;
                }
            }

            m = r.match(_input);
        } finally {
            if (null != _cultureInfo) {
                Locale.setDefault(originalCulture);
            }
        }

        if (m.success() && !ExpectSuccess()) {
            System.out.println("Err_2270awanm! Did not expect the match to succeed");
            return false;
        } else if (!m.success() && ExpectSuccess()) {
            System.out.println("Err_68997asnzxn! Did not expect the match to fail");
            return false;
        }

        if (!ExpectSuccess()) {
            // The match was not suppose to succeed and it failed. There is no more checking to
            // do so the test was a success
            return true;
        }

        if (m.groups().count() != _expectedGroups.length) {
            System.out.printf("Err_0234jah! Expected %d groups and got %d groups", _expectedGroups.length, m.groups().count());
            return false;
        }

        if (!m.value().equals(_expectedGroups[0])) {
            System.out.printf("Err_611074ahhar Expected Value='%s' Actual='%s'", _expectedGroups[0], m.value());
            return false;
        }

        int[] groupNumbers = r.getGroupNumbers();
        String[] groupNames = r.getGroupNames();

        for (int i = 0; i < _expectedGroups.length; i++) {
            // Verify Group.Value
            if (!m.groups().get(groupNumbers[i]).value().equals(_expectedGroups[i])) {
                System.out.printf("Err_07823nhhl Expected Group[%d]='%s' actual='%s' Name=%s",
                        groupNumbers[i],
                        _expectedGroups[i],
                        m.groups().get(groupNumbers[i]),
                        r.groupNameFromNumber(groupNumbers[i]));
                return false;
            }

            // Verify the same thing is returned from groups when using either an int or string index
            if (m.groups().get(groupNumbers[i]) != m.groups().get(groupNames[i])) {
                System.out.printf("Err_08712saklj Expected Groups[%d]='%s' Groups[%s]='%s'",
                        groupNumbers[i],
                        m.groups().get(groupNumbers[i]),
                        groupNames[i],
                        m.groups().get(groupNames[i]));
                return false;
            }

            // Verify GetGroupNumberFromName
            if (groupNumbers[i] != r.groupNumberFromName(groupNames[i])) {
                System.out.printf("Err_68974aehui Expected GroupNumberFromName(%s)=%d actual %d",
                        groupNames[i],
                        groupNumbers[i],
                        r.groupNumberFromName(groupNames[i]));
                return false;
            }

            // Verify GetGroupNameFromNumber
            if (!groupNames[i].equals(r.groupNameFromNumber(groupNumbers[i]))) {
                System.out.printf("Err_3468plhmy Expected GroupNameFromNumber(%d)=%s actual %s",
                        groupNumbers[i],
                        groupNames[i],
                        r.groupNameFromNumber(groupNumbers[i]));
                return false;
            }
        }

        return true;
    }
}