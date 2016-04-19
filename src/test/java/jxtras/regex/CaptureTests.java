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

import jxtras.regex.support.Assert;
import jxtras.regex.support.Fact;

public class CaptureTests {
    @Fact
    public static void Capture_Test() {
        Match match = Regex.match("adfadsfSUCCESSadsfadsf", ".*\\B(SUCCESS)\\B.*");
        int[] iMatch1 = {0, 22};
        String strMatch1 = "adfadsfSUCCESSadsfadsf";

        String[] strGroup1 = {"adfadsfSUCCESSadsfadsf", "SUCCESS"};
        int[] iGroup1 = {7, 7};
        String[] strGrpCap1 = {"SUCCESS"};

        Assert.True(match.success(), "Fail Do not found a match");

        Assert.True(match.value().equals(strMatch1), "Expected to return TRUE");
        Assert.Equal(match.index(), iMatch1[0]);
        Assert.Equal(match.length(), iMatch1[1]);
        Assert.Equal(match.captures().count(), 1);

        Assert.True(match.captures().get(0).value().equals(strMatch1), "Expected to return TRUE");
        Assert.Equal(match.captures().get(0).index(), iMatch1[0]);
        Assert.Equal(match.captures().get(0).length(), iMatch1[1]);

        Assert.Equal(match.groups().count(), 2);

        //Group 0 always is the Match
        Assert.True(match.groups().get(0).value().equals(strMatch1), "Expected to return TRUE");
        Assert.Equal(match.groups().get(0).index(), iMatch1[0]);
        Assert.Equal(match.groups().get(0).length(), iMatch1[1]);
        Assert.Equal(match.groups().get(0).captures().count(), 1);


        //Group 0's Capture is always the Match
        Assert.True(match.groups().get(0).captures().get(0).value().equals(strMatch1), "Expected to return TRUE");
        Assert.Equal(match.groups().get(0).captures().get(0).index(), iMatch1[0]);
        Assert.Equal(match.groups().get(0).captures().get(0).length(), iMatch1[1]);

        for (int i = 1; i < match.groups().count(); i++) {
            Assert.True(match.groups().get(i).value().equals(strGroup1[i]), "Expected to return TRUE");
            Assert.Equal(match.groups().get(i).index(), iGroup1[0]);
            Assert.Equal(match.groups().get(i).length(), iGroup1[0]);
            Assert.Equal(match.groups().get(i).captures().count(), 1);
        }
    }
}
