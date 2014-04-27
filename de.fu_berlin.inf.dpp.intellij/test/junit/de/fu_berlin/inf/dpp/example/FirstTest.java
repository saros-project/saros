package de.fu_berlin.inf.dpp.example;

import org.junit.Assert;
import org.junit.Test;

public class FirstTest {

    @Test
    public void failsDeliberately() {
        Assert.fail("Expected to fail, used to configure build scripts.");
    }
}
