package de.fu_berlin.inf.dpp.activities.business;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.test.mocks.SarosMocks;

public class ChangeColorActivityTest extends AbstractActivityTest {

    private static final String AFFECTED_USER = "affected@user";
    private List<User> affectedList;

    @Override
    @Before
    public void setup() {
        setupDefaultMocks();

        JID jidAffected = new JID(AFFECTED_USER);
        User affected = SarosMocks.mockUser(jidAffected);
        SarosMocks.addUserToSession(sarosSession, affected);

        affectedList = toListPlusNull(affected);

        replayDefaultMocks();
    }

    @Override
    @Test
    public void testConversion() {
        for (User affected : affectedList) {
            for (User target : targets) {
                for (int colorID = 0; colorID < SarosSession.MAX_USERCOLORS; colorID++) {
                    ChangeColorActivity cca;
                    try {
                        cca = new ChangeColorActivity(source, target, affected,
                            colorID);
                    } catch (IllegalArgumentException e) {
                        // consumed
                        continue;
                    }

                    testConversionAndBack(cca);
                }
            }
        }
    }
}
