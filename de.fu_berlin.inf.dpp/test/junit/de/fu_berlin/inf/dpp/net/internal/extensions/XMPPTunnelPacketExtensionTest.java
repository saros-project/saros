package de.fu_berlin.inf.dpp.net.internal.extensions;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import de.fu_berlin.inf.dpp.net.packet.Packet;
import de.fu_berlin.inf.dpp.net.packet.VersionRequestPacket;

public class XMPPTunnelPacketExtensionTest {

    @Test
    public void testSerializeAndDeserialzePacket() throws Exception {

        Packet versionRequest = new VersionRequestPacket("Hallo");

        XMPPTunnelPacketExtension extension = new XMPPTunnelPacketExtension(
            versionRequest);

        String xml = extension.toXML();

        PacketExtensionProvider provider = new XMPPTunnelPacketExtension.Provider();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new StringReader(xml));
        xpp.next();

        VersionRequestPacket parsed = (VersionRequestPacket) ((XMPPTunnelPacketExtension) provider
            .parseExtension(xpp)).getPacket();

        assertEquals("Hallo", parsed.getVersion());

    }
}
