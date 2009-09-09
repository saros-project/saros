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
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesExtensionProvider;

public class ActivitiesExtensionProviderTest extends TestCase {

    protected static final JID jid = new JID("testman@jabber.cc");
    protected static final String source = jid.toString();
    protected static final IPath path = new Path("testpath");
    protected static final Timestamp jupiterTime = new JupiterVectorTime(1, 3);
    protected static final ActivitiesExtensionProvider provider = new ActivitiesExtensionProvider();

    protected static final Operation timestamp = new TimestampOperation();
    protected static final Operation noOp = new NoOperation();
    protected static final Operation insert = new InsertOperation(
        34,
        "One Line Delimiters\r\nLinux\nSeveral\n\nTabs\t\t\tEncoding Test‰¸‰ˆ¸‡·");
    protected static final Operation delete = new DeleteOperation(37,
        "One Line Delimiters\r\nLinux\nSeveral\n\nTabs\t\t\tEncoding Test‰¸‰ˆ¸‡·");
    protected static final Operation easySplit = new SplitOperation(insert,
        delete);
    protected static final Operation nestedSplit = new SplitOperation(insert,
        new SplitOperation(new SplitOperation(delete, insert),
            new SplitOperation(insert, easySplit)));

    protected static final IActivity[] activities = new IActivity[] {
        new EditorActivity(source, EditorActivity.Type.Activated, path),
        new FileActivity(source, FileActivity.Type.Created, path, null,
            new byte[] { 34, 72 }, Purpose.ACTIVITY),
        new FolderActivity(source, FolderActivity.Type.Created, path),
        new RoleActivity(source, "user@server", UserRole.DRIVER),
        new TextEditActivity(source, 23, "foo\r\ntest\n\nbla", "bar", path),
        new TextSelectionActivity(source, 1, 2, path),
        new ViewportActivity(source, 5, 10, path) };

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

        assertRoundtrip(new JupiterActivity(jupiterTime, op, jid, path));
    }

    public void assertRoundtrip(IActivity activity)
        throws XmlPullParserException, IOException {

        assertRoundtrip(createPacketExtension(activity));
    }

    public void testActivities() throws XmlPullParserException, IOException {
        for (IActivity activity : activities) {
            assertRoundtrip(activity);
        }
    }

    public void testEditorActivity() {
        for (EditorActivity.Type type : EditorActivity.Type.values()) {
            try {
                new EditorActivity("user@server", type, null);
                if (type != EditorActivity.Type.Activated) {
                    fail();
                }
            } catch (IllegalArgumentException e) {
                // Expected exception.
            }
        }
    }

    public void testEmptyExtension() {
        List<TimedActivity> activities = Collections.emptyList();
        try {
            aProvider.create("Session-ID", activities);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    public void testGapInSequenceNumbers() throws XmlPullParserException,
        IOException {
        IActivity activity = new EditorActivity(source,
            EditorActivity.Type.Activated, null);

        List<TimedActivity> timedActivities = new ArrayList<TimedActivity>(2);
        timedActivities.add(new TimedActivity(activity, jid, 20));
        timedActivities.add(new TimedActivity(activity, jid, 22));

        PacketExtension extension = aProvider.create("Session-ID",
            timedActivities);

        assertRoundtrip(extension);
    }

    public void testLineDelimiters() throws XmlPullParserException, IOException {
        for (String lineEnding : new String[] { "\n", "\r", "\r\n" }) {
            assertRoundtrip(new TextEditActivity(source, 42, lineEnding, "",
                path));
        }
    }

    protected void assertRoundtrip(PacketExtension extension)
        throws XmlPullParserException, IOException {
        String xml = extension.toXML().replaceAll("\r", "");
        assertEquals(aProvider.getPayload(extension), aProvider
            .getPayload(parseExtension(xml)));
    }

    protected PacketExtension createPacketExtension(IActivity activity) {
        return aProvider.create("4711", Collections
            .singletonList(new TimedActivity(activity, new JID(activity
                .getSource()), 42)));
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
