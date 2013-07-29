package de.fu_berlin.inf.dpp.activities.business;

import org.junit.Test;

public class StopFollowingActivityTest extends AbstractActivityTest {

    @Test
    @Override
    public void testConversion() {
        StopFollowingActivity sfa = new StopFollowingActivity(source);

        testConversionAndBack(sfa);
    }

}
