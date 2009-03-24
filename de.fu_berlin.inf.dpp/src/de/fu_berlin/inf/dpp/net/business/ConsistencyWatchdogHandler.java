package de.fu_berlin.inf.dpp.net.business;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
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
import de.fu_berlin.inf.dpp.ui.SessionView;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.Pair;
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

            /*
             * TODO This only checks for the file being opened in an editor, but
             * it might be open in the background with changes!
             */
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
            String pathsOfInconsistencies = Util.toOSString(paths);

            if (resolved) {
                log.info("Synchronisation completed, inconsistency resolved");
                closeChecksumErrorMessage(pathsOfInconsistencies, from);
                return;
            }

            log.debug("Checksum Error for " + pathsOfInconsistencies + "from "
                + from.getBase());

            showChecksumErrorMessage(pathsOfInconsistencies, from);

            if (Saros.getDefault().getSessionManager().getSharedProject()
                .isHost()) {
                Util.runSafeAsync("ConsistencyWatchdog-Start", log,
                    new Runnable() {
                        public void run() {
                            performConsistencyRecovery(from, paths);
                        }
                    });
            } else {
                // check if inconsistencies exists on this side too,
                // then run ConsistencyAction to resolve these
                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        // get the session view
                        IViewPart view = Util
                            .findView("de.fu_berlin.inf.dpp.ui.SessionView");
                        if (view != null)
                            ((SessionView) view)
                                .runConsistencyActionIfEnabled();
                    }
                });
            }
        }

        protected HashMap<Pair<String, JID>, MessageDialog> actualChecksumErrorDialogs = new HashMap<Pair<String, JID>, MessageDialog>();

        /**
         * This method creates and opens an error message which informs the user
         * that inconsistencies are handled and he should wait until the
         * inconsistencies are resolved. The Message are saved in a HashMap with
         * a pair of JID of the user and a string representation of the paths of
         * the handled files as key. You can use
         * <code>closeChecksumErrorMessage</code> with the same arguments to
         * close this message again.
         * 
         * @see #closeChecksumErrorMessage(String, JID)
         * 
         * @param pathes
         *            a string representation of the handled files.
         * @param from
         *            JID
         * 
         */
        protected void showChecksumErrorMessage(final String pathes,
            final JID from) {

            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    MessageDialog md = new MessageDialog(
                        Display.getDefault().getActiveShell(),
                        "Consistency Problem!",
                        null,
                        "Inconsitent file state has detected. File "
                            + pathes
                            + " from user "
                            + from.getBase()
                            + " has to be synchronized with project host. Please wait until the inconsistencies are resolved.",
                        MessageDialog.WARNING, new String[0], 0);
                    actualChecksumErrorDialogs.put(new Pair<String, JID>(
                        pathes, from), md);
                }
            });

            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    MessageDialog md = actualChecksumErrorDialogs
                        .get(new Pair<String, JID>(pathes, from));
                    if (md != null)
                        md.open();
                }
            });
        }

        /**
         * Closes the ChecksumError message identified by the JID from the user
         * who had the inconsistencies and a string representation of the
         * handled files. The string representation must be the same which are
         * used to show the message with <code>showChecksumErrorMessage</code>.
         * 
         * @see #showChecksumErrorMessage(String, JID)
         * 
         * @param from
         *            JID of user who had the inconsistencies
         * @param pathsOfInconsistencies
         *            a string representation of the paths of handled files
         * 
         */
        protected void closeChecksumErrorMessage(final String paths,
            final JID from) {
            Util.runSafeSWTAsync(log, new Runnable() {
                Pair<String, JID> key = new Pair<String, JID>(paths, from);
                MessageDialog md = actualChecksumErrorDialogs.get(key);

                public void run() {
                    if (md != null) {
                        md.close();
                        actualChecksumErrorDialogs.remove(key);
                    }
                }
            });
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
