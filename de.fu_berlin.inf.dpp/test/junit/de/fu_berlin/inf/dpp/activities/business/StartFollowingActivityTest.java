package de.fu_berlin.inf.dpp.activities.business;

import org.junit.Test;

import de.fu_berlin.inf.dpp.session.User;

public class StartFollowingActivityTest extends AbstractActivityTest {

    @Test
    @Override
    public void testConversion() {
        for (User followedUser : targets) {
            StartFollowingActivity sfa;
            try {
                sfa = new StartFollowingActivity(source, followedUser);
            } catch (IllegalArgumentException e) {
                continue;
            }

            testConversionAndBack(sfa);
        }
    }
}
