/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.Collection;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public class ChecksumExtension extends SessionDefaultPacketExtension {

    public ChecksumExtension() {
        super("DocChecksum");
    }

    public PacketExtension create(Collection<DocumentChecksum> checksums) {
        DefaultPacketExtension extension = create();

        extension.setValue("quantity", Integer.toString(checksums.size()));

        int i = 1;
        for (DocumentChecksum checksum : checksums) {
            extension.setValue("path" + Integer.toString(i), checksum
                    .getPath().toPortableString());
            extension.setValue("length" + Integer.toString(i), Integer
                    .toString(checksum.getLength()));
            extension.setValue("hash" + Integer.toString(i), Integer
                    .toString(checksum.getHash()));
            i++;
        }

        return extension;

    }

    public static ChecksumExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(ChecksumExtension.class);
    }
}