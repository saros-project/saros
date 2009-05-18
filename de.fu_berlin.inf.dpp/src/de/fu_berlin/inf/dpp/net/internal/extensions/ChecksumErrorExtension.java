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

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

@Component(module = "net")
public class ChecksumErrorExtension extends SessionDefaultPacketExtension {

    public static final String FILE_PATH = "CE_FILE_PATH";

    public static final String QUANTITY = "CE_QUANTITY";

    public static final String RESOLVED = "CE_RESOLVED";

    public ChecksumErrorExtension(SessionIDObservable sessionID) {
        super(sessionID, "FileChecksumError");
    }

    public PacketExtension create(Set<IPath> paths, boolean resolved) {
        DefaultPacketExtension extension = create();

        extension.setValue(QUANTITY, paths.size() + "");

        int i = 1;
        for (IPath path : paths) {
            extension.setValue(FILE_PATH + i, path.toPortableString());
            i++;
        }

        extension.setValue(RESOLVED, String.valueOf(resolved));

        return extension;
    }

    @Override
    public void processMessage(JID sender, Message message) {

        DefaultPacketExtension checksumErrorExtension = getExtension(message);

        final int quantity = Integer.parseInt(checksumErrorExtension
            .getValue(QUANTITY));

        Set<IPath> paths = new CopyOnWriteArraySet<IPath>();

        for (int i = 1; i <= quantity; i++) {
            paths.add(Path.fromPortableString(checksumErrorExtension
                .getValue(FILE_PATH + i)));
        }

        final boolean resolved = Boolean.parseBoolean(checksumErrorExtension
            .getValue(RESOLVED));

        checksumErrorReceived(sender, paths, resolved);
    }

    public void checksumErrorReceived(JID sender, Set<IPath> paths,
        boolean resolved) {
        throw new UnsupportedOperationException(
            "This implementation of the ChecksumErrorExtension should only be used to construct Extensions to be sent.");
    }

}