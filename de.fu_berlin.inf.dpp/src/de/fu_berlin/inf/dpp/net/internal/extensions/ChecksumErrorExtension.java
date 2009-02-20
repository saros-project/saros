/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public abstract class ChecksumErrorExtension extends
    SessionDefaultPacketExtension {

    public static final String FILE_PATH = "CE_FILE_PATH";

    public static final String RESOLVED = "CE_RESOLVED";

    public ChecksumErrorExtension() {
        super("FileChecksumError");
    }

    public PacketExtension create(IPath path, boolean resolved) {
        DefaultPacketExtension extension = create();

        extension.setValue(FILE_PATH, path.toOSString());
        extension.setValue(RESOLVED, String.valueOf(resolved));

        return extension;
    }

    public static ChecksumErrorExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(
            ChecksumErrorExtension.class);
    }

    @Override
    public void processMessage(JID sender, Message message) {

        DefaultPacketExtension checksumErrorExtension = getExtension(message);

        final String path = checksumErrorExtension.getValue(FILE_PATH);

        final boolean resolved = Boolean.parseBoolean(checksumErrorExtension
            .getValue(RESOLVED));

        checksumErrorReceived(sender, new Path(path), resolved);
    }

    public abstract void checksumErrorReceived(JID sender, IPath path,
        boolean resolved);
}