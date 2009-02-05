/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public class DataTransferExtension extends
        SessionDefaultPacketExtension {

    public DataTransferExtension() {
        super("DataTransfer");
    }

    public PacketExtension create(String name, String desc, int index,
            int count, String data) {
        DefaultPacketExtension extension = create();

        extension.setValue(PacketExtensions.DT_NAME, name);
        extension.setValue(PacketExtensions.DT_DESC, desc);
        extension.setValue(PacketExtensions.DT_DATA, data);

        String split = index + "/" + count;
        extension.setValue(PacketExtensions.DT_SPLIT, split);

        return extension;

    }

    public static DataTransferExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(DataTransferExtension.class);
    }
}