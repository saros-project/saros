package de.fu_berlin.inf.dpp.net.business;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.RequestPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumErrorExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions;
import de.fu_berlin.inf.dpp.project.CurrentProjectProxy;
import de.fu_berlin.inf.dpp.ui.ErrorMessageDialog;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * @Component The single instance of this class per application is managed by
 *            PicoContainer
 */
public class ConsistencyWatchdogReceiver {

    private static Logger log = Logger
        .getLogger(ConsistencyWatchdogReceiver.class.getName());

    protected long lastReceivedActivityTime;

    ChecksumErrorExtension checksumError = new ChecksumErrorExtension() {

        @Override
        public void checksumErrorReceived(final JID from, final IPath path,
            boolean resolved) {

            log.debug("ChecksumError received");

            if (resolved) {
                log.debug("synchronisation completed, inconsistency resolved");
                ErrorMessageDialog.closeChecksumErrorMessage();
                return;
            }

            ErrorMessageDialog.showChecksumErrorMessage(path.toOSString());

            // Host
            if (Saros.getDefault().getSessionManager().getSharedProject()
                .isHost()) {
                log.warn("Checksum Error for " + path);

                new Thread() {

                    @Override
                    public void run() {
                        try {

                            // wait until no more activities are received
                            while (System.currentTimeMillis()
                                - lastReceivedActivityTime < 1500) {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }

                            // save document
                            Display.getDefault().syncExec(new Runnable() {
                                public void run() {
                                    Set<IEditorPart> editors = EditorManager
                                        .getDefault().getEditors(path);
                                    if (editors != null && editors.size() > 0) {
                                        editors.iterator().next().doSave(
                                            new NullProgressMonitor());
                                    }

                                }
                            });

                            // reset jupiter
                            Saros.getDefault().getSessionManager()
                                .getSharedProject()
                                .getConcurrentDocumentManager()
                                .resetJupiterDocument(path);

                            // send the file to client
                            try {
                                transmitter.sendFile(from, Saros.getDefault()
                                    .getSessionManager().getSharedProject()
                                    .getProject(), path, -1);
                            } catch (IOException e) {
                                log
                                    .error("Could not sent file for consistency resolution");
                                // TODO This means we were really unable to send
                                // this file. No more falling back.
                            }

                            // TODO Should we not rather send an Activity?
                            // transmitter.sendActivities(project.getVariable(),
                            // Collections.singletonList(new TimedActivity(
                            // new FileActivity(FileActivity.Type.Created,
                            // new Path(path)),
                            // ActivitySequencer.UNDEFINED_TIME)));
                        } catch (RuntimeException e) {
                            log
                                .error(
                                    "Internal Error while processing an checksum error",
                                    e);
                        }
                    }
                }.start();
            }

        }

    };

    ChecksumExtension checksum = new ChecksumExtension() {

        ExecutorService executor = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1));

        @Override
        public void checksumsReceived(JID sender,
            final List<DocumentChecksum> checksums) {
            try {
                executor.submit(new Runnable() {
                    public void run() {
                        try {
                            project.getVariable()
                                .getConcurrentDocumentManager()
                                .checkConsistency(checksums);
                        } catch (RuntimeException e) {
                            log.error("Failed to check consistency", e);
                        }
                    }
                });
            } catch (RejectedExecutionException e) {
                // Ignore Checksums that arrive before we are done processing
                // the
                // last set of Checksums.
                log
                    .warn("Received Checksums before processing of previous checksums finished");
            }
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

    public ConsistencyWatchdogReceiver(XMPPChatReceiver receiver) {

        receiver.addPacketListener(listener, new AndFilter(
            new MessageTypeFilter(Message.Type.chat),

            Util.orFilter(checksum.getFilter(), checksumError.getFilter(),
                RequestPacketExtension.getFilter())));
    }

}
