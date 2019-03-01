package de.fu_berlin.inf.dpp.misc.xstream;

import static org.junit.Assert.assertEquals;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserConverterTest {

  private static XStream xstream;
  private static User alice;
  private static User bob;

  @BeforeClass
  public static void prepare() {
    JID aliceJid = new JID("alice@saros-con");
    alice = new User(aliceJid, true, true, 0, 0);

    JID bobJid = new JID("bob@saros-con");
    bob = new User(bobJid, false, false, 0, 0);

    /* Mocks */
    ISarosSession session = EasyMock.createMock(ISarosSession.class);
    EasyMock.expect(session.getUser(aliceJid)).andStubReturn(alice);
    EasyMock.expect(session.getUser(bobJid)).andStubReturn(bob);
    EasyMock.replay(session);

    /* XStream */
    xstream = new XStream(new DomDriver());
    xstream.registerConverter(new UserConverter(session));
  }

  @Test
  public void conversionAndBack() {
    checkConversion(alice);
    checkConversion(bob);
  }

  private void checkConversion(User user) {
    User userCopy = (User) xstream.fromXML(xstream.toXML(user));
    assertEquals(user, userCopy);
  }
}
