package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;
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

/**
 * Class responsible for parsing a Jupiter Request.
 * 
 * @author orieger
 * @author oezbek
 */
public class RequestExtensionProvider implements PacketExtensionProvider {

    private static final Logger log = Logger
        .getLogger(RequestExtensionProvider.class.getName());

    public PacketExtension parseExtension(XmlPullParser parser)
        throws XmlPullParserException, IOException {

        Request request = null;
        String sessionID = null;
        String path = null;
        String jid = null;
        int sideID = 0;
        Timestamp timestamp = null;
        try {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {

                if (parser.getName().equals(RequestPacketExtension.ELEMENT)) {
                    parser.next();
                }

                if (parser.getName().equals(RequestPacketExtension.SESSION_ID)) {
                    sessionID = parseSessionId(parser);
                    parser.next();
                }

                if (parser.getName().equals(RequestPacketExtension.PATH)) {
                    path = parsePath(parser);
                    parser.next();
                }

                if (parser.getName().equals(RequestPacketExtension.JID)) {
                    jid = parseJID(parser);
                    parser.next();
                }

                if (parser.getName().equals(RequestPacketExtension.SIDE_ID)) {
                    sideID = parseSideID(parser);
                    parser.next();
                }

                if (parser.getName().equals(RequestPacketExtension.VECTOR_TIME)) {
                    int local = Integer.parseInt(parser.getAttributeValue(null,
                        "local"));
                    int remote = Integer.parseInt(parser.getAttributeValue(
                        null, "remote"));
                    timestamp = new JupiterVectorTime(local, remote);
                    parser.next();
                    parser.next();
                }

                Operation op = parseOperation(parser);

                request = new Request(sideID, timestamp, op, new JID(jid), Path
                    .fromPortableString(path));
            }
        } catch (Exception e) {
            log.error("Internal error while parsing RequestPacket: ", e);
            return null;
        }

        // TODO Even if we failed???
        return new RequestPacketExtension(sessionID, request);
    }

    protected String parseSessionId(XmlPullParser parser)
        throws XmlPullParserException, IOException {
        parser.next(); // read text
        String sessionID = parser.getText();
        parser.next(); // read end tag

        return sessionID;
    }

    protected String parsePath(XmlPullParser parser)
        throws XmlPullParserException, IOException {
        parser.next(); // read text
        String path = parser.getText();
        parser.next(); // read end tag

        return path;
    }

    protected String parseJID(XmlPullParser parser)
        throws XmlPullParserException, IOException {
        parser.next(); // read text
        String jid = parser.getText();
        parser.next(); // read end tag

        return jid;
    }

    protected int parseSideID(XmlPullParser parser)
        throws XmlPullParserException, IOException {
        parser.next(); // read text
        int id = Integer.parseInt(parser.getText());
        parser.next(); // read end tag

        return id;
    }

    protected Operation parseOperation(XmlPullParser parser)
        throws XmlPullParserException, IOException {

        if (parser.getName().equals(RequestPacketExtension.SPLIT_OP)) {
            parser.next(); // Open tag
            Operation op1 = parseOperation(parser);
            Operation op2 = parseOperation(parser);
            parser.next(); // Advance to next operation
            return new SplitOperation(op1, op2);
        }
        if (parser.getName().equals(RequestPacketExtension.INSERT_OP)) {
            return parseInsertOperation(parser);
        }
        if (parser.getName().equals(RequestPacketExtension.DELETE_OP)) {
            return parseDeleteOperation(parser);
        }
        if (parser.getName().equals(RequestPacketExtension.NO_OP)) {
            parser.next();
            return new NoOperation();
        }
        if (parser.getName().equals(RequestPacketExtension.TIMESTAMP_OP)) {
            parser.next();
            return new TimestampOperation();
        }

        throw new IllegalArgumentException("Unexpected parser token: "
            + parser.getName());
    }

    protected Operation parseInsertOperation(XmlPullParser parser)
        throws XmlPullParserException, IOException {

        int pos = Integer.parseInt(parser.getAttributeValue(null,
            RequestPacketExtension.POSITION));
        int origin = Integer.parseInt(parser.getAttributeValue(null,
            RequestPacketExtension.ORIGIN));

        String text = "";
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText();
            int type = parser.next();
            assert type == XmlPullParser.END_TAG; // Close tag
        }
        parser.next(); // Advance to next operation
        return new InsertOperation(pos, text, origin);
    }

    protected Operation parseDeleteOperation(XmlPullParser parser)
        throws XmlPullParserException, IOException {

        int pos = Integer.parseInt(parser.getAttributeValue(null,
            RequestPacketExtension.POSITION));

        String text = "";
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText();
            int type = parser.next();
            assert type == XmlPullParser.END_TAG; // Close tag
        }
        parser.next(); // Advance to next operation
        return new DeleteOperation(pos, text);
    }
}
