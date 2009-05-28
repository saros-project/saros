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
public class DataTransferExtension extends SessionDefaultPacketExtension {

    public static final String DT_NAME = "DT_NAME";

    public static final String DT_DESC = "DT_DESC";

    public static final String DT_INDEX = "DT_INDEX";

    public static final String DT_MAX_INDEX = "DT_MAX_INDEX";

    public static final String DT_DATA = "DT_BASE64";

    public DataTransferExtension(SessionIDObservable sessionIDObservable) {
        super(sessionIDObservable, "DataTransfer");
    }

    public PacketExtension create(String name, String desc, int index,
        int maxIndex, String data) {
        DefaultPacketExtension extension = create();

        extension.setValue(DT_NAME, name);
        extension.setValue(DT_DESC, desc);
        extension.setValue(DT_DATA, data);
        extension.setValue(DT_INDEX, String.valueOf(index));
        extension.setValue(DT_MAX_INDEX, String.valueOf(maxIndex));

        return extension;

    }

    @Override
    public void processMessage(JID sender, Message message) {
        DefaultPacketExtension dt = getExtension(message);

        String name = dt.getValue(DT_NAME);
        String data = dt.getValue(DT_DATA);
        String desc = dt.getValue(DT_DESC);
        int index = Integer.parseInt(dt.getValue(DT_INDEX));
        int maxIndex = Integer.parseInt(dt.getValue(DT_MAX_INDEX));

        chunkReceived(sender, name, desc, index, maxIndex, data);
    }

    public void chunkReceived(JID sender, String name, String desc, int index,
        int maxIndex, String data) {
        throw new UnsupportedOperationException(
            "This implementation should only be used to construct Extensions to be sent.");
    }

}