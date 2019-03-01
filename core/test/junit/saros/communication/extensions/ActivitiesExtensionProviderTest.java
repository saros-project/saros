package saros.communication.extensions;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import org.jivesoftware.smack.packet.PacketExtension;
import org.junit.Test;
import saros.activities.EditorActivity;
import saros.activities.IActivity;
import saros.net.xmpp.JID;
import saros.session.User;

public class ActivitiesExtensionProviderTest {

  @Test
  public void testNoPrettyPrintInMarshalledObjects() throws Exception {
    User user = new User(new JID("alice@test"), true, true, 0, 0);

    IActivity activity = new EditorActivity(user, EditorActivity.Type.ACTIVATED, null);

    List<IActivity> activities = new ArrayList<IActivity>();

    activities.add(activity);
    activities.add(activity);

    PacketExtension extension =
        ActivitiesExtension.PROVIDER.create(new ActivitiesExtension("Session-ID", activities, 0));

    String marshalled = extension.toXML();
    assertFalse(marshalled.contains("\r"));
    assertFalse(marshalled.contains("\n"));
    assertFalse(marshalled.contains("\t"));
    assertFalse(marshalled.contains("  "));
  }
}
