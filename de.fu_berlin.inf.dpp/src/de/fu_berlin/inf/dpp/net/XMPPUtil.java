package de.fu_berlin.inf.dpp.net;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;

public class XMPPUtil {

    /**
     * Retrieve XMPP Registration information from a server.
     * 
     * This implementation reuses code from Smack but also sets the from element
     * of the IQ-Packet so that the server could reply with information that the
     * account already exists as given by XEP-0077.
     * 
     * To see what additional information can be queried from the registration
     * object, refer to the XEP directly:
     * 
     * http://xmpp.org/extensions/xep-0077.html
     */
    public static synchronized Registration getRegistrationInfo(
        String toRegister, XMPPConnection connection) throws XMPPException {
        Registration reg = new Registration();
        reg.setTo(connection.getServiceName());
        reg.setFrom(toRegister);
        PacketFilter filter = new AndFilter(new PacketIDFilter(reg
            .getPacketID()), new PacketTypeFilter(IQ.class));
        PacketCollector collector = connection.createPacketCollector(filter);
        connection.sendPacket(reg);
        IQ result = (IQ) collector.nextResult(SmackConfiguration
            .getPacketReplyTimeout());

        // Stop queuing results
        collector.cancel();

        if (result == null) {
            // TODO This exception is shown incorrectly to the user!!
            throw new XMPPException("No response from server.");
        } else if (result.getType() == IQ.Type.ERROR) {
            throw new XMPPException(result.getError());
        } else {
            return (Registration) result;
        }
    }

}
