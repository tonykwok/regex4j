package jxtras.regex.tests;

import jxtras.regex.Regex;
import jxtras.regex.Match;
import jxtras.regex.tests.support.Assert;
import jxtras.regex.tests.support.Fact;

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
