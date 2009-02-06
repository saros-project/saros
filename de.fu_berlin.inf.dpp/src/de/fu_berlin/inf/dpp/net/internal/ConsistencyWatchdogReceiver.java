package de.fu_berlin.inf.dpp.net.internal;

import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumErrorExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions;
import de.fu_berlin.inf.dpp.project.CurrentProjectProxy;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager.ConnectionSessionListener;
import de.fu_berlin.inf.dpp.ui.ErrorMessageDialog;
import de.fu_berlin.inf.dpp.util.Util;

public class ConsistencyWatchdogReceiver implements ConnectionSessionListener {

    private static Logger log = Logger
        .getLogger(ConsistencyWatchdogReceiver.class.getName());

    protected long lastReceivedActivityTime;

    ChecksumErrorExtension checksumError = new ChecksumErrorExtension();
    ChecksumExtension checksum = new ChecksumExtension();

    PacketListener listener = new PacketListener() {

        public void processPacket(Packet packet) {
            Message message = (Message) packet;

            if (checksum.hasExtension(message)) {
                processChecksumExtension(message, project.getVariable());
            }

            if (checksumError.hasExtension(message)) {
                processChecksumErrorExtension(message);
            }

            if (PacketExtensions.getJupiterRequestExtension(message) != null) {
                lastReceivedActivityTime = System.currentTimeMillis();
            }
        }
    };

    XMPPConnection connection;

    ITransmitter transmitter;

    CurrentProjectProxy project;

    public ConsistencyWatchdogReceiver(ITransmitter transmitter,
        CurrentProjectProxy project) {
        this.project = project;
        this.transmitter = transmitter;
    }

    public void dispose() {
        // Nothing to dispose about :-D
    }

    public void prepare(XMPPConnection connection) {
        this.connection = connection;
    }

    public void start() {
        if (this.connection != null) {

            // TODO filter for correct session
            connection.addPacketListener(listener, new AndFilter(
                new MessageTypeFilter(Message.Type.chat), Util.orFilter(
                    checksum.getFilter(), checksumError.getFilter(),
                    RequestPacketExtension.getFilter())));
        }
    }

    public void stop() {
        // TODO Think about queueing or what happens if we don't listen for
        // packets while stopped
        if (this.connection != null) {
            connection.removePacketListener(listener);
        }
        this.connection = null;
    }

    private void processChecksumErrorExtension(final Message message) {

        log.debug("ChecksumError received");

        DefaultPacketExtension checksumErrorExtension = checksumError
            .getExtension(message);

        final String path = checksumErrorExtension
            .getValue(PacketExtensions.FILE_PATH);

        final boolean resolved = Boolean.parseBoolean(checksumErrorExtension
            .getValue("resolved"));

        if (resolved) {
            log.debug("synchronisation completed, inconsistency resolved");
            ErrorMessageDialog.closeChecksumErrorMessage();
            return;
        }

        ErrorMessageDialog.showChecksumErrorMessage(path);

        // Host
        if (Saros.getDefault().getSessionManager().getSharedProject().isHost()) {
            log.warn("Checksum Error for " + path);

            new Thread() {

                @Override
                public void run() {

                    // wait until no more activities are received
                    while (System.currentTimeMillis()
                        - lastReceivedActivityTime < 1500) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    EditorManager.getDefault().saveText(new Path(path), true);

                    transmitter.sendActivities(project.getVariable(),
                        Collections.singletonList(new TimedActivity(
                            new FileActivity(FileActivity.Type.Created,
                                new Path(path)),
                            ActivitySequencer.UNDEFINED_TIME)));

                    // // TODO CJ: thinking about a better solution with
                    // // activity sequencer and jupiter
                    //
                    // Saros.getDefault().getSessionManager().getSharedProject()
                    // .getConcurrentDocumentManager()
                    // .resetJupiterDocument(new Path(path));
                    //
                    // log.debug("Sending file to clients");
                    // try {
                    // transmitter.sendFile(new JID(message.getFrom()), Saros
                    // .getDefault().getSessionManager()
                    // .getSharedProject().getProject(),
                    // new Path(path), -1);
                    // } catch (IOException e) {
                    // log.error("File could not be send:", e);
                    // // TODO This means we were really unable to send
                    // // this file. No more falling back.
                    // }
                }
            }.start();
        }
    }

    ExecutorService executor = new ThreadPoolExecutor(1, 1, 0,
        TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1));

    private void processChecksumExtension(final Message message,
        final ISharedProject project) {

        final DefaultPacketExtension ext = checksum.getExtension(message);

        try {
            executor.submit(new Runnable() {
                public void run() {
                    try {
                        log.debug("Processing Checksum");

                        int count = Integer.parseInt(ext.getValue("quantity"));
                        DocumentChecksum[] checksums = new DocumentChecksum[count];

                        for (int i = 1; i <= count; i++) {
                            IPath path = Path.fromPortableString(ext
                                .getValue("path" + i));
                            int length = Integer.parseInt(ext.getValue("length"
                                + i));
                            int hash = Integer.parseInt(ext
                                .getValue("hash" + i));
                            checksums[i - 1] = new DocumentChecksum(path,
                                length, hash);
                        }
                        project.getConcurrentDocumentManager()
                            .checkConsistency(checksums);
                    } catch (RuntimeException e) {
                        log.error("Failed to check consistency", e);
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            // Ignore Checksums that arrive before we are done processing the
            // last set of Checksums.
            log
                .warn("Received Checksums before processing of previous checksums finished");
        }
    }

}
