/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class LeaveExtension extends SessionDefaultPacketExtension {

    public LeaveExtension(SessionIDObservable sessionIDObservable) {
        super(sessionIDObservable, "leave");
    }

    @Override
    public DefaultPacketExtension create() {
        return super.create();
    }

    @Override
    public void processMessage(JID sender, Message message) {
        leaveReceived(sender);
    }

    public void leaveReceived(JID sender) {
        throw new UnsupportedOperationException(
            "This implementation should only be used to construct Extensions to be sent.");
    }

}