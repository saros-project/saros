package de.fu_berlin.inf.dpp.activities.business;

import org.junit.Test;

public class ShareConsoleActivityTest extends AbstractActivityTest {

    private final String consoleContent = "\nHello World 4\n";

    @Override
    @Test
    public void testConversion() {
        ShareConsoleActivity shareConsoleActivity;
        shareConsoleActivity = new ShareConsoleActivity(source, consoleContent);
        testConversionAndBack(shareConsoleActivity);
    }
}
