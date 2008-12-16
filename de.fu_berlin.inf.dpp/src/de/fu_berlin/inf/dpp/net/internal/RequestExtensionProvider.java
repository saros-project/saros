package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;

import org.eclipse.core.runtime.Path;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.RequestImpl;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.JID;

public class RequestExtensionProvider implements PacketExtensionProvider {

    public PacketExtension parseExtension(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        Request request = null;
        String sessionID = null;
        String path = null;
        String jid = null;
        try {
            boolean done = false;
            while (!done) {
                int eventType = parser.next();
                if (eventType == XmlPullParser.START_TAG) {

                    if (parser.getName().equals(RequestPacketExtension.ELEMENT)) {
                        parser.next();
                    }

                    if (parser.getName().equals(
                            RequestPacketExtension.SESSION_ID)) {
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

                    request = parseRequest(parser);

                    request.setEditorPath(new Path(path));
                    request.setJID(new JID(jid));

                } else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals(RequestPacketExtension.ELEMENT)) {
                        done = true;
                    }
                }
            }
        } catch (Exception e) {
            // System.out.println("Mist");
            e.printStackTrace();
        }

        return new RequestPacketExtension(sessionID, request);
        // return null;
    }

    private String parseSessionId(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.next(); // read text
        String sessionID = parser.getText();
        parser.next(); // read end tag

        return sessionID;
    }

    private String parsePath(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.next(); // read text
        String path = parser.getText();
        parser.next(); // read end tag

        return path;
    }

    private String parseJID(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.next(); // read text
        String jid = parser.getText();
        parser.next(); // read end tag

        return jid;
    }

    private int parseSideID(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.next(); // read text
        int id = Integer.parseInt(parser.getText());
        parser.next(); // read end tag

        return id;
    }

    private Request parseRequest(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        // // extract current editor for text edit.
        int id = 0;
        Timestamp timestamp = null;
        Operation op = null;
        if (parser.getName().equals(RequestPacketExtension.SIDE_ID)) {
            id = parseSideID(parser);
            parser.next();
        }

        if (parser.getName().equals(RequestPacketExtension.VECTOR_TIME)) {
            int local = Integer.parseInt(parser
                    .getAttributeValue(null, "local"));
            int remote = Integer.parseInt(parser.getAttributeValue(null,
                    "remote"));
            timestamp = new JupiterVectorTime(local, remote);
            parser.next();
            parser.next();
        }

        // if(parser.getName().equals(RequestPacketExtension.INSERT_OP)){
        // op = parseInsertOperation(parser);
        // return new RequestImpl(id,timestamp,op);
        // }
        //		
        // if(parser.getName().equals(RequestPacketExtension.DELETE_OP)){
        // op = parseDeleteOperation(parser);
        // return new RequestImpl(id,timestamp,op);
        // }
        // if(parser.getName().equals(RequestPacketExtension.NO_OP)){
        // op = new NoOperation();
        // return new RequestImpl(id,timestamp,op);
        // }
        if (parser.getName().equals(RequestPacketExtension.SPLIT_OP)) {
            parser.next();
            Operation op1 = parseSingleOperation(parser);
            parser.next();
            parser.next();
            Operation op2 = parseSingleOperation(parser);
            op = new SplitOperation(op1, op2);
            return new RequestImpl(id, timestamp, op);
        } else {
            op = parseSingleOperation(parser);
        }

        Request req = new RequestImpl(id, timestamp, op);
        //		
        return req;
    }

    private Operation parseSingleOperation(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        Operation op = null;
        if (parser.getName().equals(RequestPacketExtension.INSERT_OP)) {
            return parseInsertOperation(parser);
        }
        if (parser.getName().equals(RequestPacketExtension.DELETE_OP)) {
            return parseDeleteOperation(parser);

        }
        if (parser.getName().equals(RequestPacketExtension.NO_OP)) {
            return new NoOperation();
        }
        if (parser.getName().equals(RequestPacketExtension.TIMESTAMP_OP)) {
            return new TimestampOperation();
        }
        return op;
    }

    private Operation parseInsertOperation(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        Operation op = null;
        int pos = Integer.parseInt(parser.getAttributeValue(null,
                RequestPacketExtension.POSITION));
        int origin = Integer.parseInt(parser.getAttributeValue(null,
                RequestPacketExtension.ORIGIN));

        String text = "";
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText();
        }
        op = new InsertOperation(pos, text, origin);
        return op;
    }

    private Operation parseDeleteOperation(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        Operation op = null;
        int pos = Integer.parseInt(parser.getAttributeValue(null,
                RequestPacketExtension.POSITION));

        String text = "";
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.getText();
        }
        op = new DeleteOperation(pos, text);
        return op;
    }
    //	
    // private IActivity parseTextEditActivity(XmlPullParser parser) throws
    // XmlPullParserException,
    // IOException {
    //	
    // // extract current editor for text edit.
    // String pathString = parser.getAttributeValue(null, "path");
    // Path path = pathString.equals("null") ? null : new Path(pathString);
    //		
    // int offset = Integer.parseInt(parser.getAttributeValue(null, "offset"));
    // int replace = Integer.parseInt(parser.getAttributeValue(null,
    // "replace"));
    //	
    // String text = "";
    // if (parser.next() == XmlPullParser.TEXT) {
    // text = parser.getText();
    // }
    //	
    // return new TextEditActivity(offset, text, replace,path);
    // }
}
