/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.eclipse.core.runtime.IPath;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public class JupiterErrorExtension extends SessionDefaultPacketExtension {

    public JupiterErrorExtension() {
        super("JupiterTransformationError");
    }

    public PacketExtension create(IPath path) {
        DefaultPacketExtension extension = create();

        extension.setValue(PacketExtensions.FILE_PATH, path.toOSString());

        return extension;
    }

    public static JupiterErrorExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(
            JupiterErrorExtension.class);
    }
}