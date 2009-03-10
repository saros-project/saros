/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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

    public static final String QUANTITY = "CE_QUANTITY";

    public static final String RESOLVED = "CE_RESOLVED";

    public ChecksumErrorExtension() {
        super("FileChecksumError");
    }

    public PacketExtension create(Set<IPath> paths, boolean resolved) {
        DefaultPacketExtension extension = create();

        extension.setValue(QUANTITY, paths.size() + "");

        int i = 1;
        for (IPath path : paths) {
            extension.setValue(FILE_PATH + i, path.toOSString());
            i++;
        }

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

        final int quantity = Integer.parseInt(checksumErrorExtension
            .getValue(QUANTITY));

        Set<IPath> paths = new CopyOnWriteArraySet<IPath>();

        for (int i = 1; i <= quantity; i++) {
            final String path = checksumErrorExtension.getValue(FILE_PATH + i);
            paths.add(new Path(path));
        }

        final boolean resolved = Boolean.parseBoolean(checksumErrorExtension
            .getValue(RESOLVED));

        checksumErrorReceived(sender, paths, resolved);
    }

    public abstract void checksumErrorReceived(JID sender, Set<IPath> paths,
        boolean resolved);
}