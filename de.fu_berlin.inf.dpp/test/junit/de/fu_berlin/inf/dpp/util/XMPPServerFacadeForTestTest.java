package de.fu_berlin.inf.dpp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.test.xmpp.XMPPServerFacadeForTests;
import de.fu_berlin.inf.dpp.test.xmpp.XmppUser;

/**
 * @author cordes
 */
public class XMPPServerFacadeForTestTest {

    @Ignore
    @Test
    public void test() throws XMPPException {
        XMPPServerFacadeForTests.startServer();
        XMPPServerFacadeForTests facade = new XMPPServerFacadeForTests();
        XmppUser user = facade.getNextUser();
        assertEquals("alice", user.getUsername());
        tryToLogin(user);

        XmppUser user2 = facade.getNextUser();
        assertEquals("bob", user2.getUsername());
        tryToLogin(user2);

        facade.deleteAllCreatedAccounts();

        try {
            tryToLogin(user);
            fail("It should not be possible to login with this credentials.");
        } catch (Exception e) {
            // everthing is fine
        }
        try {
            tryToLogin(user2);
            fail("It should not be possible to login with this credentials.");
        } catch (Exception e) {
            // everthing is fine
        }
        XMPPServerFacadeForTests.stopServer();
    }

    private void tryToLogin(XmppUser user) throws XMPPException {
        XMPPConnection connection = new XMPPConnection(user.getServerAdress());
        connection.connect();
        connection.login(user.getUsername(), user.getPassword());
        assertTrue(connection.isConnected());
    }
}
