package de.fu_berlin.inf.dpp.activities.business;

import java.util.List;

import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.business.ProgressActivity.ProgressAction;
import de.fu_berlin.inf.dpp.session.User;

public class ProgressActivityTest extends AbstractActivityTest {

    @Test
    @Override
    public void testConversion() {
        List<String> progressIDs = toListPlusNull("", "abc", "123");
        int[] workCurrents = { 0, 10, 20, 100 };
        int[] workTotals = { 0, 11, 21, 100 };
        List<String> taskNames = toListPlusNull("", "abc", "123");
        List<ProgressAction> actions = toListPlusNull(ProgressAction.values());

        for (User target : targets) {
            for (String progressID : progressIDs) {
                for (int workCurrent : workCurrents) {
                    for (int workTotal : workTotals) {
                        for (String taskName : taskNames) {
                            for (ProgressAction action : actions) {
                                ProgressActivity pa;
                                try {
                                    pa = new ProgressActivity(source, target,
                                        progressID, workCurrent, workTotal,
                                        taskName, action);
                                } catch (IllegalArgumentException e) {
                                    continue;
                                }

                                testConversionAndBack(pa);
                            }
                        }
                    }
                }
            }
        }
    }

}
