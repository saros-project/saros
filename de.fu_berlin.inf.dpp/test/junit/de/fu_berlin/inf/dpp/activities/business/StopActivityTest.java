package de.fu_berlin.inf.dpp.activities.business;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.StopActivity.State;
import de.fu_berlin.inf.dpp.activities.business.StopActivity.Type;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.mocks.SarosMocks;

public class StopActivityTest extends AbstractActivityTest {

    private static final String INITIATOR_USER = "initiator@user";
    private List<User> initiators;

    @Override
    @Before
    public void setup() {
        setupDefaultMocks();

        JID jid = new JID(INITIATOR_USER);
        User initiator = SarosMocks.mockUser(jid);
        SarosMocks.addUserToSession(sarosSession, initiator);

        initiators = toListPlusNull(initiator);

        replayDefaultMocks();
    }

    @Test
    @Override
    public void testConversion() {
        List<Type> types = toListPlusNull(Type.values());
        List<State> states = toListPlusNull(State.values());
        List<String> stopActivityIDs = toListPlusNull("", "abc", "123");

        for (User initiator : initiators) {
            for (User target : targets) {
                for (Type type : types) {
                    for (State state : states) {
                        for (String stopActivityID : stopActivityIDs) {
                            StopActivity sta;
                            try {
                                sta = new StopActivity(source, initiator,
                                    target, type, state, stopActivityID);
                            } catch (IllegalArgumentException e) {
                                continue;
                            }

                            testConversionAndBack(sta);
                        }
                    }
                }
            }
        }
    }
}
