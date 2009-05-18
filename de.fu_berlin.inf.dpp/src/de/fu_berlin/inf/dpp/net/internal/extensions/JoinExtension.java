/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class JoinExtension extends SessionDefaultPacketExtension {

    public JoinExtension(SessionIDObservable sessionIDObservable) {
        super(sessionIDObservable, "join");
    }

    public PacketExtension create(int colorID) {
        DefaultPacketExtension extension = create();

        extension.setValue(PacketExtensionUtils.COLOR_ID, String
            .valueOf(colorID));
        return extension;
    }

    @Override
    public void processMessage(JID sender, Message message) {
        DefaultPacketExtension extension = getExtension(message);

        int colorID = Integer.parseInt(extension
            .getValue(PacketExtensionUtils.COLOR_ID));

        joinReceived(sender, colorID);
    }

    public void joinReceived(JID sender, int colorID) {
        throw new UnsupportedOperationException(
            "This implementation should only be used to construct Extensions to be sent.");
    }

}