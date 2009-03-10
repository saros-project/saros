package de.fu_berlin.inf.dpp.net.business;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorPart;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.RequestPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumErrorExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions;
import de.fu_berlin.inf.dpp.project.CurrentProjectProxy;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.ErrorMessageDialog;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * @Component The single instance of this class per application is managed by
 *            PicoContainer
 */
public class ConsistencyWatchdogHandler {

    private static Logger log = Logger
        .getLogger(ConsistencyWatchdogHandler.class.getName());

    protected long lastReceivedActivityTime;

    /**
     * @host This is only called on the host
     */
    private void performConsistencyRecovery(JID from, Set<IPath> paths) {

        // wait until no more activities are received
        while (System.currentTimeMillis() - lastReceivedActivityTime < 1500) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        for (IPath path : paths) {

            // TODO Handle case, when file does not exist locally any
            // more
            final boolean wasReadOnly = FileUtil.setReadOnly(project
                .getProject().getFile(path), false);

            Set<IEditorPart> editors = EditorManager.getDefault().getEditors(
                path);
            if (editors != null && editors.size() > 0) {
                if (!EditorAPI.saveEditor(editors.iterator().next())) {
                    log.warn("Saving was canceled");
                }
            }

            // Reset Read Only flag
            if (wasReadOnly) {
                FileUtil.setReadOnly(project.getProject().getFile(path), true);
            }

            // Reset jupiter
            ConcurrentDocumentManager concurrentManager = project
                .getConcurrentDocumentManager();
            if (concurrentManager.isManagedByJupiterServer(from, path))
                concurrentManager.resetJupiterServer(from, path);

            // Send the file to client
            try {
                transmitter.sendFile(from, project.getProject(), path, -1,
                /*
                 * TODO CO The Callback should be used to show progress to the
                 * user
                 */
                new IFileTransferCallback() {

                    public void fileSent(IPath path) {
                        // do nothing
                    }

                    public void fileTransferFailed(IPath path, Exception e) {
                        // do nothing

                    }

                    public void transferProgress(int transfered) {
                        // do nothing
                    }

                });
            } catch (IOException e) {
                // TODO This means we were really unable to send
                // this file. No more falling back.
                log.error("Could not sent file for consistency resolution");
            }
        }

    }

    ChecksumErrorExtension checksumError = new ChecksumErrorExtension() {

        @Override
        public void checksumErrorReceived(final JID from,
            final Set<IPath> paths, boolean resolved) {

            // Concatenate paths
            StringBuilder sb = new StringBuilder();
            for (IPath path : paths) {
                if (sb.length() > 0)
                    sb.append(", ");

                sb.append(path.toOSString());
            }
            String pathsOfInconsistencies = sb.toString();

            if (resolved) {
                log.info("Synchronisation completed, inconsistency resolved");
                ErrorMessageDialog.closeChecksumErrorMessage(
                    pathsOfInconsistencies, from);
                return;
            }

            log.debug("Checksum Error for " + pathsOfInconsistencies + "from "
                + from.getBase());

            ErrorMessageDialog.showChecksumErrorMessage(pathsOfInconsistencies,
                from);

            if (Saros.getDefault().getSessionManager().getSharedProject()
                .isHost()) {
                Util.runSafeAsync("ConsistencyWatchdog-Start", log,
                    new Runnable() {
                        public void run() {
                            performConsistencyRecovery(from, paths);
                        }
                    });
            } else {
                // Client only needs to showChecksumErrorMessage
            }
        }
    };

    ChecksumExtension checksum = new ChecksumExtension() {

        @Override
        public void checksumsReceived(JID sender,
            final List<DocumentChecksum> checksums) {

            ISharedProject currentProject = project.getValue();

            assert currentProject != null;

            ConcurrentDocumentManager concurrentManager = currentProject
                .getConcurrentDocumentManager();

            concurrentManager.setChecksums(checksums);
            concurrentManager.checkConsistency();
        }
    };

    PacketListener listener = new PacketListener() {

        public void processPacket(Packet packet) {
            Message message = (Message) packet;

            checksum.processPacket(packet);

            checksumError.processPacket(packet);

            if (PacketExtensions.getJupiterRequestExtension(message) != null) {
                lastReceivedActivityTime = System.currentTimeMillis();
            }
        }
    };

    @Inject
    ITransmitter transmitter;

    @Inject
    CurrentProjectProxy project;

    public ConsistencyWatchdogHandler(XMPPChatReceiver receiver) {

        receiver.addPacketListener(listener, new AndFilter(
            new MessageTypeFilter(Message.Type.chat),

            Util.orFilter(checksum.getFilter(), checksumError.getFilter(),
                RequestPacketExtension.getFilter())));
    }

}
