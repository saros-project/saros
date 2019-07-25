package saros.misc.xstream;

import static org.junit.Assert.assertEquals;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.net.xmpp.JID;
import saros.session.ISarosSession;
import saros.session.User;

public class UserConverterTest {

  private static XStream xstream;
  private static User alice;
  private static User bob;

  @BeforeClass
  public static void prepare() {
    JID aliceJid = new JID("alice@saros-con");
    alice = new User(aliceJid, true, true, null);

    JID bobJid = new JID("bob@saros-con");
    bob = new User(bobJid, false, false, null);

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
