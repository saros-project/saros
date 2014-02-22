package de.fu_berlin.inf.dpp.activities.business;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.test.mocks.SarosMocks;

public class ChecksumErrorActivityTest extends AbstractActivityTest {

    @Test
    @Override
    public void testConversion() {
        List<SPath> shortList = new ArrayList<SPath>();
        shortList.add(SarosMocks.mockSPath());

        List<SPath> longerList = new ArrayList<SPath>();
        longerList.add(SarosMocks.mockSPath());
        longerList.add(SarosMocks.mockSPath());
        longerList.add(SarosMocks.mockSPath());
        longerList.add(SarosMocks.mockSPath());

        @SuppressWarnings("unchecked")
        List<List<SPath>> pathsValues = toListPlusNull(shortList, longerList);
        List<String> recoveryIDs = toListPlusNull("", "abc", "123");

        for (User target : targets) {
            for (List<SPath> paths : pathsValues) {
                for (String recoveryID : recoveryIDs) {
                    ChecksumErrorActivity cea;
                    try {
                        cea = new ChecksumErrorActivity(source, target, paths,
                            recoveryID);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    testConversionAndBack(cea);
                }
            }
        }
    }
}
