package de.fu_berlin.inf.dpp.activities.business;

import org.junit.Test;

import de.fu_berlin.inf.dpp.User;

public class NOPActivityTest extends AbstractActivityTest {

    @Override
    @Test
    public void testConversion() {
        int[] ids = { 0, 1, 10, 1024 };

        for (User target : targets) {
            for (int id : ids) {
                NOPActivity na;
                try {
                    na = new NOPActivity(source, target, id);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                testConversionAndBack(na);
            }
        }
    }
}
