/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.eclipse.core.runtime.IPath;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public class ChecksumErrorExtension extends
        SessionDefaultPacketExtension {

    public ChecksumErrorExtension() {
        super("FileChecksumError");
    }

    public PacketExtension create(IPath path, boolean resolved) {
        DefaultPacketExtension extension = create();

        extension.setValue(PacketExtensions.FILE_PATH, path.toOSString());
        extension.setValue("resolved", resolved ? "true" : "false");

        return extension;
    }

    public static ChecksumErrorExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(ChecksumErrorExtension.class);
    }
}