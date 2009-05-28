package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import de.fu_berlin.inf.dpp.net.JID;

public abstract class JupiterTestCase extends TestCase {

    public JID jidServer = new JID("Server");
    public JID jidC1 = new JID("Alice");
    public JID jidAlice = jidC1;
    public JID jidC2 = new JID("Bob");
    public JID jidBob = jidC2;
    public JID jidC3 = new JID("Carl");
    public JID jidCarl = jidC3;

    static {
        PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
    }

    protected SimulateNetzwork network;

    @Override
    public void setUp() {
        network = new SimulateNetzwork();
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
            jidServer);

        network.addClient(s1);

        ClientSynchronizedDocument[] result = new ClientSynchronizedDocument[number];

        for (int i = 0; i < result.length; i++) {
            result[i] = new ClientSynchronizedDocument(jidServer, initialText,
                network, new JID("C" + i));
            network.addClient(result[i]);
            s1.addProxyClient(result[i].getJID());
        }

        Thread.sleep(100);
        return result;
    }
}
