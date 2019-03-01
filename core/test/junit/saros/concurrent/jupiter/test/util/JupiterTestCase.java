package de.fu_berlin.inf.dpp.concurrent.jupiter.test.util;

import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.User;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;

public abstract class JupiterTestCase {

  /**
   * User mock objects that expect calls to User#getJID any number of times.
   *
   * @see JupiterTestCase#setup()
   */
  public User alice;

  public User bob;
  public User carl;
  public User host;

  protected NetworkSimulator network;

  static {
    PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
  }

  @Before
  public void setup() {
    network = new NetworkSimulator();

    alice = createUser("alice");
    bob = createUser("bob");
    carl = createUser("carl");
    host = createUser("host");
  }

  public static void assertEqualDocs(String s, DocumentTestChecker... docs) {

    for (int i = 0; i < docs.length; i++) {
      assertEquals(docs[i].getUser().toString(), s, docs[i].getDocument());
    }
  }

  public ClientSynchronizedDocument[] setUp(int number, String initialText) {

    // Create Server
    ServerSynchronizedDocument s1 = new ServerSynchronizedDocument(network, host);

    network.addClient(s1);

    ClientSynchronizedDocument[] result = new ClientSynchronizedDocument[number];

    for (int i = 0; i < result.length; i++) {
      result[i] =
          new ClientSynchronizedDocument(host, initialText, network, createUser("Client" + i));
      network.addClient(result[i]);
      s1.addProxyClient(result[i].getUser());
    }

    return result;
  }

  public static User createUser(String name) {
    return new User(new JID(name + "@jabber.org"), false, true, 0, 0);
  }
}
