package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jivesoftware.smack.packet.PacketExtension;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.FileActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.FolderActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.JupiterActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.RoleActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesExtensionProvider;

public class ActivitiesExtensionProviderTest extends TestCase {

    protected static final JID jid = new JID("testman@jabber.cc");
    protected static final IPath path = new Path("testpath");
    protected static final Timestamp jupiterTime = new JupiterVectorTime(1, 3);
    protected static final ActivitiesExtensionProvider provider = new ActivitiesExtensionProvider();

    protected static final Operation timestamp = new TimestampOperation();
    protected static final Operation noOp = new NoOperation();
    protected static final Operation insert = new InsertOperation(
        34,
        "One Line Delimiters\r\nLinux\nSeveral\n\nTabs\t\t\tEncoding Test�������");
    protected static final Operation delete = new DeleteOperation(
        37,
        "One Line Delimiters\r\nLinux\nSeveral\n\nTabs\t\t\tEncoding Test�������");
    protected static final Operation easySplit = new SplitOperation(insert,
        delete);
    protected static final Operation nestedSplit = new SplitOperation(insert,
        new SplitOperation(new SplitOperation(delete, insert),
            new SplitOperation(insert, easySplit)));

    protected static final IActivityDataObject[] activityDataObjects = new IActivityDataObject[] {
        new EditorActivityDataObject(jid, EditorActivity.Type.Activated, path),
        new FileActivityDataObject(jid, FileActivity.Type.Created, path, null,
            new byte[] { 34, 72 }, Purpose.ACTIVITY),
        new FolderActivityDataObject(jid, FolderActivity.Type.Created, path),
        new RoleActivityDataObject(jid, new JID("user@server"), UserRole.DRIVER),
        new TextEditActivityDataObject(jid, 23, "foo\r\ntest\n\nbla", "bar",
            path), new TextSelectionActivityDataObject(jid, 1, 2, path),
        new ViewportActivityDataObject(jid, 5, 10, path) };

    protected ActivitiesExtensionProvider aProvider = new ActivitiesExtensionProvider();

    public void testJupiterActivities() throws XmlPullParserException,
        IOException {
        assertRoundtrip(timestamp);
        assertRoundtrip(noOp);
        assertRoundtrip(insert);
        assertRoundtrip(delete);
        assertRoundtrip(easySplit);
        assertRoundtrip(nestedSplit);
    }

    public void assertRoundtrip(Operation op) throws XmlPullParserException,
        IOException {

        assertRoundtrip(new JupiterActivityDataObject(jupiterTime, op, jid,
            path));
    }

    public void assertRoundtrip(IActivityDataObject activityDataObject)
        throws XmlPullParserException, IOException {

        assertRoundtrip(createPacketExtension(activityDataObject));
    }

    public void testActivities() throws XmlPullParserException, IOException {
        for (IActivityDataObject activityDataObject : activityDataObjects) {
            assertRoundtrip(activityDataObject);
        }
    }

    public void testEditorActivity() {
        for (EditorActivity.Type type : EditorActivity.Type.values()) {
            try {
                new EditorActivity(null, type, null);
                if (type != EditorActivity.Type.Activated) {
                    fail();
                }
            } catch (IllegalArgumentException e) {
                // Expected exception.
            }
        }
    }

    public void testEmptyExtension() {
        List<TimedActivityDataObject> activities = Collections.emptyList();
        try {
            aProvider.create("Session-ID", activities);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    public void testGapInSequenceNumbers() throws XmlPullParserException,
        IOException {
        IActivityDataObject activityDataObject = new EditorActivityDataObject(
            jid, EditorActivity.Type.Activated, null);

        List<TimedActivityDataObject> timedActivities = new ArrayList<TimedActivityDataObject>(
            2);
        timedActivities.add(new TimedActivityDataObject(activityDataObject,
            jid, 20));
        timedActivities.add(new TimedActivityDataObject(activityDataObject,
            jid, 22));

        PacketExtension extension = aProvider.create("Session-ID",
            timedActivities);

        assertRoundtrip(extension);
    }

    public void testLineDelimiters() throws XmlPullParserException, IOException {
        for (String lineEnding : new String[] { "\n", "\r", "\r\n" }) {
            assertRoundtrip(new TextEditActivityDataObject(jid, 42, lineEnding,
                "", path));
        }
    }

    protected void assertRoundtrip(PacketExtension extension)
        throws XmlPullParserException, IOException {
        String xml = extension.toXML().replaceAll("\r", "");
        assertEquals(aProvider.getPayload(extension), aProvider
            .getPayload(parseExtension(xml)));
    }

    protected PacketExtension createPacketExtension(
        IActivityDataObject activityDataObject) {
        return aProvider.create("4711", Collections
            .singletonList(new TimedActivityDataObject(activityDataObject,
                activityDataObject.getSource(), 42)));
    }

    protected PacketExtension parseExtension(String xml)
        throws XmlPullParserException, IOException {
        /*
         * Smack calls the provider with a parser that is already within the
         * document, so we call the next() method to simulate this aspect.
         */
        MXParser parser = new MXParser();
        parser.setInput(new StringReader(xml));
        parser.next();

        return provider.parseExtension(parser);
    }
}
