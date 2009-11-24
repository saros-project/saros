package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;

public abstract class JupiterTestCase extends TestCase {

    /**
     * User mock objects that expect calls to User#getJID any number of times.
     * 
     * @see JupiterTestCase#setUp()
     */
    public User alice;
    public User bob;
    public User carl;
    public User host;

    protected SimulateNetzwork network;

    static {
        PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
    }

    @Override
    public void setUp() {
        network = new SimulateNetzwork();

        alice = createUserMock("alice");
        bob = createUserMock("bob");
        carl = createUserMock("carl");
        host = createUserMock("host");
    }

    public static void assertEqualDocs(String s, DocumentTestChecker... docs) {

        for (int i = 0; i < docs.length; i++) {
            assertEquals(docs[i].getJID().toString(), s, docs[i].getDocument());
        }
    }

    public ClientSynchronizedDocument[] setUp(int number, String initialText)
        throws InterruptedException {

        // Create Server
        ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(network,
            host);

        network.addClient(s1);

        ClientSynchronizedDocument[] result = new ClientSynchronizedDocument[number];

        for (int i = 0; i < result.length; i++) {
            result[i] = new ClientSynchronizedDocument(host.getJID(),
                initialText, network, createUserMock("Client" + i));
            network.addClient(result[i]);
            s1.addProxyClient(result[i].getUser());
        }

        Thread.sleep(100);
        return result;
    }

    public static User createUserMock(String name) {
        User result = createMock(User.class);
        expect(result.getJID()).andReturn(new JID(name + "@jabber.org"))
            .anyTimes();
        replay(result);
        return result;
    }
}
