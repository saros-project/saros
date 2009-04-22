package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
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

    protected JID jid = new JID("testman@jabber.cc");
    protected IPath path = new Path("testpath");
    protected Timestamp jupiterTime = new JupiterVectorTime(1, 3);
    protected ActivitiesExtensionProvider provider = new ActivitiesExtensionProvider();

    protected Operation timestamp = new TimestampOperation();
    protected Operation noOp = new NoOperation();
    protected Operation insert = new InsertOperation(34, "inserted text");
    protected Operation delete = new DeleteOperation(37, "deleted text");
    protected Operation easySplit = new SplitOperation(insert, delete);
    protected Operation nestedSplit = new SplitOperation(insert,
        new SplitOperation(new SplitOperation(delete, insert),
            new SplitOperation(insert, easySplit)));

    public void testRequests() throws XmlPullParserException, IOException {
        assertRoundtrip(timestamp);
        assertRoundtrip(noOp);
        assertRoundtrip(insert);
        assertRoundtrip(delete);
        assertRoundtrip(easySplit);
        assertRoundtrip(nestedSplit);
    }

    public void assertRoundtrip(Operation op) throws XmlPullParserException,
        IOException {
        ActivitiesPacketExtension requestPacket = createPacketExtension(op);
        assertEquals(requestPacket, parseExtension(requestPacket));
    }

    protected ActivitiesPacketExtension createPacketExtension(Operation op) {
        Request req = new Request(jupiterTime, op, jid, path);
        List<TimedActivity> timedRequests = Collections
            .singletonList(new TimedActivity(req, 42));
        return new ActivitiesPacketExtension("1", timedRequests);
    }

    protected ActivitiesPacketExtension parseExtension(
        ActivitiesPacketExtension packet) throws XmlPullParserException,
        IOException {
        MXParser parser = new MXParser();
        parser.setInput(new StringReader(packet.toXML()));
        parser.next();
        return provider.parseExtension(parser);
    }
}
