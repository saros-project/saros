package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.RequestExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.RequestPacketExtension;

public class RequestExtensionProviderTest extends TestCase {
    private MXParser parser;

    public void testTimestampRequest() throws XmlPullParserException,
        IOException {
        Request req = new Request(1, new JupiterVectorTime(1, 3),
            new TimestampOperation(), new JID("ori79@jabber.cc"), new Path(
                "hello"));
        RequestPacketExtension requestPacket = new RequestPacketExtension("1",
            req);
        RequestExtensionProvider provider = new RequestExtensionProvider();

        parser = new MXParser();
        parser.setInput(new StringReader(requestPacket.toXML()));

        RequestPacketExtension requestExtension = (RequestPacketExtension) provider
            .parseExtension(parser);
        assertEquals(req, requestExtension.getRequest());
    }

    public void xtestNoOPRequest() throws XmlPullParserException, IOException {
        Request req = new Request(1, new JupiterVectorTime(1, 3),
            new NoOperation(), new JID("ori79@jabber.cc"), new Path("hello"));
        RequestPacketExtension requestPacket = new RequestPacketExtension("1",
            req);
        RequestExtensionProvider provider = new RequestExtensionProvider();

        parser = new MXParser();
        parser.setInput(new StringReader(requestPacket.toXML()));

        RequestPacketExtension requestExtension = (RequestPacketExtension) provider
            .parseExtension(parser);
        assertEquals(req, requestExtension.getRequest());
    }

    public void xtestInsertRequest() throws XmlPullParserException, IOException {
        Request req = new Request(1, new JupiterVectorTime(1, 3),
            new InsertOperation(34, "insert text"), new JID("ori79@jabber.cc"),
            new Path("hello"));
        RequestPacketExtension requestPacket = new RequestPacketExtension("1",
            req);
        RequestExtensionProvider provider = new RequestExtensionProvider();

        parser = new MXParser();
        parser.setInput(new StringReader(requestPacket.toXML()));

        RequestPacketExtension requestExtension = (RequestPacketExtension) provider
            .parseExtension(parser);
        assertEquals(req, requestExtension.getRequest());
    }

    public void testDeleteRequest() throws XmlPullParserException, IOException {
        Request req = new Request(1, new JupiterVectorTime(1, 3),
            new DeleteOperation(34, "insert text"), new JID("ori79@jabber.cc"),
            new Path("hello"));
        RequestPacketExtension requestPacket = new RequestPacketExtension("1",
            req);
        RequestExtensionProvider provider = new RequestExtensionProvider();

        parser = new MXParser();
        parser.setInput(new StringReader(requestPacket.toXML()));

        RequestPacketExtension requestExtension = (RequestPacketExtension) provider
            .parseExtension(parser);
        assertEquals(req, requestExtension.getRequest());
    }

    public void testSplitRequest() throws XmlPullParserException, IOException {
        Request req = new Request(1, new JupiterVectorTime(1, 3),
            new SplitOperation(new InsertOperation(34, "insert text"),
                new DeleteOperation(34, "insert text")), new JID(
                "ori79@jabber.cc"), new Path("hello"));
        RequestPacketExtension requestPacket = new RequestPacketExtension("1",
            req);
        RequestExtensionProvider provider = new RequestExtensionProvider();

        parser = new MXParser();
        parser.setInput(new StringReader(requestPacket.toXML()));

        RequestPacketExtension requestExtension = (RequestPacketExtension) provider
            .parseExtension(parser);
        assertEquals(req, requestExtension.getRequest());
    }

    public void testSplitRequest2() throws XmlPullParserException, IOException {
        Request req = new Request(1, new JupiterVectorTime(1, 3),
            new SplitOperation(new DeleteOperation(34, "insert text"),
                new DeleteOperation(37, "insert text")), new JID(
                "ori79@jabber.cc"), new Path("hello"));
        RequestPacketExtension requestPacket = new RequestPacketExtension("1",
            req);
        RequestExtensionProvider provider = new RequestExtensionProvider();

        parser = new MXParser();
        parser.setInput(new StringReader(requestPacket.toXML()));

        RequestPacketExtension requestExtension = (RequestPacketExtension) provider
            .parseExtension(parser);
        assertEquals(req, requestExtension.getRequest());
    }

    public void testSplitRequest3() throws XmlPullParserException, IOException {
        Request req = new Request(1, new JupiterVectorTime(1, 3),
            new SplitOperation(new DeleteOperation(34, "insert text"),
                new NoOperation()), new JID("ori79@jabber.cc"), new Path(
                "hello"));
        RequestPacketExtension requestPacket = new RequestPacketExtension("1",
            req);
        RequestExtensionProvider provider = new RequestExtensionProvider();

        parser = new MXParser();
        parser.setInput(new StringReader(requestPacket.toXML()));

        RequestPacketExtension requestExtension = (RequestPacketExtension) provider
            .parseExtension(parser);
        assertEquals(req, requestExtension.getRequest());
    }

}
