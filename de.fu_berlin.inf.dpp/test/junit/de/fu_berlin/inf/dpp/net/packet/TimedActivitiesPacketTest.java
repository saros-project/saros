package de.fu_berlin.inf.dpp.net.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.JupiterActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.TimedActivities;

public class TimedActivitiesPacketTest {

    private JID jid = new JID("userXYZ@jabber.org");
    private int sequenceNumber = 0;

    @Test
    public void testSerialiazationandDeserialisation() throws Exception {
        SPathDataObject path = new SPathDataObject("pid", new Path("testpath"),
            "xtx");

        List<TimedActivityDataObject> activities = new ArrayList<TimedActivityDataObject>();

        activities.add(createTimedActivity(new EditorActivityDataObject(jid,
            EditorActivity.Type.Activated, path)));

        activities.add(createTimedActivity(new ViewportActivityDataObject(jid,
            0, 0, path)));

        activities.add(createTimedActivity(new JupiterActivityDataObject(
            (Timestamp) null, (Operation) null, jid, path)));

        activities.add(createTimedActivity(new TextSelectionActivityDataObject(
            jid, 0, 0, path)));

        activities.add(createTimedActivity(new TextEditActivityDataObject(jid,
            0, "", "", path)));

        Packet packet = new TimedActivitiesPacket(new TimedActivities("4711",
            activities));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        packet.serialize(out);
        System.out.println(out.toByteArray().length);
        packet.deserialize(new ByteArrayInputStream(out.toByteArray()));

    }

    private TimedActivityDataObject createTimedActivity(
        IActivityDataObject object) {
        return new TimedActivityDataObject(object, jid, sequenceNumber++);
    }
}
