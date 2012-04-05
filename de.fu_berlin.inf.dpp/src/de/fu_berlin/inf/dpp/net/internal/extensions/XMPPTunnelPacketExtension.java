package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

import de.fu_berlin.inf.dpp.net.packet.Packet;
import de.fu_berlin.inf.dpp.net.packet.PacketType;

/**
 * This extension will tunnel all Saros internal {@link Packet}s through an
 * existing XMPP connection.
 */
public class XMPPTunnelPacketExtension implements PacketExtension {

    public static final String ELEMENT_NAME = "packet";
    public static final String NAMESPACE = "http://saros-project.org/protocol/tunnel";

    private Packet packet;
    private byte[] data;

    public XMPPTunnelPacketExtension(Packet packet) {
        this.packet = packet;
    }

    public XMPPTunnelPacketExtension(byte[] data) {
        this.data = data;
    }

    /**
     * Returns the {@link Packet} that was transfered by this extension
     * 
     * @return the packet that this extension is currently holding
     */
    public Packet getPacket() {
        if (packet != null)
            return packet;

        try {

            InputStream in = new ByteArrayInputStream(Base64.decodeBase64(data));

            short id = new DataInputStream(in).readShort();

            Class<?> clazz = PacketType.CLASS.get(id);

            if (clazz == null)
                return null;

            Packet packet = (Packet) clazz.newInstance();

            packet.deserialize(new DataInputStream(in));

            return packet;

        } catch (Exception e) {
            throw new RuntimeException("error while deserializing packet: "
                + e.getMessage(), e);
        }
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String toXML() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(512);

        try {
            new DataOutputStream(out).writeShort(packet.getType().getID());
            packet.serialize(new DataOutputStream(out));
            return "<" + getElementName() + " xmlns=\"" + getNamespace()
                + "\">"
                + new String(Base64.encodeBase64(out.toByteArray()), "UTF-8")
                + "</" + getElementName() + ">";
        } catch (IOException e) {
            // this MUST NOT HAPPEN !
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Provider class that will parse the payload of a
     * {@link XMPPTunnelPacketExtension}
     */
    public static class Provider implements PacketExtensionProvider {

        @Override
        public PacketExtension parseExtension(XmlPullParser parser)
            throws Exception {
            /*
             * XML ... if it doesn't solve your problem, you aren't using enough
             * of it
             */
            return new XMPPTunnelPacketExtension(parser.nextText().getBytes(
                "UTF-8"));
        }
    }
}
