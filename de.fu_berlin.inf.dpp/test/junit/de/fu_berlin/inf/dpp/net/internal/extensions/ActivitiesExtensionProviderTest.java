package de.fu_berlin.inf.dpp.net.internal.extensions;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;
import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.XStreamExtensionProvider;

public class ActivitiesExtensionProviderTest {

    static {
        XStreamExtensionProvider.setNameSpace("foo");
    }

    @Test
    public void testNoPrettyPrintInMarshalledObjects() throws Exception {
        IActivityDataObject activityDataObject = new EditorActivityDataObject(
            new JID("alice@test"), EditorActivity.Type.ACTIVATED, null);

        List<IActivityDataObject> activities = new ArrayList<IActivityDataObject>();

        activities.add(activityDataObject);
        activities.add(activityDataObject);

        PacketExtension extension = ActivitiesExtension.PROVIDER
            .create(new ActivitiesExtension("Session-ID", activities, 0));

        String marshalled = extension.toXML();
        assertFalse(marshalled.contains("\r"));
        assertFalse(marshalled.contains("\n"));
        assertFalse(marshalled.contains("\t"));
        assertFalse(marshalled.contains("  "));
    }
}
