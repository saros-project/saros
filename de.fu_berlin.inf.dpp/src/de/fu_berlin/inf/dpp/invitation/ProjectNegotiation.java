package de.fu_berlin.inf.dpp.invitation;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * 
 * 
 * This abstract class is the superclass for {@link OutgoingProjectNegotiation}
 * and {@link IncomingProjectNegotiation}.
 */
public abstract class ProjectNegotiation extends CancelableProcess {

    private static final Logger log = Logger
        .getLogger(ProjectNegotiation.class);

    @Inject
    protected ProjectNegotiationObservable projectExchangeProcesses;
    protected JID peer;
    @Inject
    protected ITransmitter transmitter;
    protected String processID;

    @Inject
    protected SarosNet sarosNet;

    /**
     * The file transfer manager can be <code>null</code> if no connection was
     * established or was lost when the class was instantiated.
     * 
     */
    protected FileTransferManager fileTransferManager;

    /**
     * While sending all the projects with a big archive containing the project
     * archives, we create a temp-File. This file is named "projectID" +
     * projectIDDelimiter + "a random number chosen by 'Java'" + ".zip" This
     * delimiter is the string that separates projectID and this random number.
     * Now we can assign the zip archive to the matching project.
     * 
     * WARNING: If changed compatibility is broken
     */
    protected final String projectIDDelimiter = "&&&&";

    @Inject
    protected ISarosSessionManager sessionManager;

    public ProjectNegotiation(JID peer, SarosContext sarosContext) {
        this.peer = peer;

        sarosContext.initComponent(this);

        projectExchangeProcesses.addProjectExchangeProcess(this);

        Connection connection = sarosNet.getConnection();

        if (connection != null)
            fileTransferManager = new FileTransferManager(connection);

    }

    /**
     * 
     * @return the names of the projects that are shared by the peer. projectID
     *         => projectName
     */
    public abstract Map<String, String> getProjectNames();

    public abstract String getProcessID();

    public JID getPeer() {
        return this.peer;
    }

    @Override
    protected void notifyCancellation(SarosCancellationException exception) {

        if (!(exception instanceof LocalCancellationException))
            return;

        LocalCancellationException cause = (LocalCancellationException) exception;

        if (cause.getCancelOption() != CancelOption.NOTIFY_PEER)
            return;

        log.debug("notifying remote contact " + Utils.prefix(getPeer())
            + " of the local project negotiation cancellation");

        transmitter.sendCancelSharingProjectMessage(getPeer(),
            cause.getMessage());
    }

    /**
     * Monitors a {@link FileTransfer} and waits until it is completed or
     * aborted.
     * 
     * @param transfer
     *            the transfer to monitor
     * @param monitor
     *            the progress monitor that is <b>already initialized</b> to
     *            consume <b>100 ticks</b> to use for reporting progress to the
     *            user. It is the caller's responsibility to call done() on the
     *            given monitor. Accepts null, indicating that no progress
     *            should be reported and that the operation cannot be cancelled.
     * 
     * @throws SarosCancellationException
     *             if the transfer was aborted either on local side or remote
     *             side, see also {@link LocalCancellationException} and
     *             {@link RemoteCancellationException}
     * @throws IOException
     *             if an I/O error occurred
     */
    protected void monitorFileTransfer(FileTransfer transfer,
        IProgressMonitor monitor) throws SarosCancellationException,
        IOException {

        if (monitor == null)
            monitor = new NullProgressMonitor();

        long fileSize = transfer.getFileSize();
        int lastWorked = 0;

        StopWatch watch = new StopWatch();
        watch.start();

        while (!transfer.isDone()) {
            if (monitor.isCanceled()) {
                transfer.cancel();
                continue;
            }

            // may return -1 if the transfer has not yet started
            long bytesWritten = transfer.getAmountWritten();

            if (bytesWritten < 0)
                bytesWritten = 0;

            int worked = (int) ((100 * bytesWritten) / fileSize);
            int delta = worked - lastWorked;

            if (delta > 0) {
                lastWorked = worked;
                monitor.worked(delta);
            }

            long bytesPerSecond = watch.getTime();

            if (bytesPerSecond > 0)
                bytesPerSecond = (bytesWritten * 1000) / bytesPerSecond;

            long secondsLeft = 0;

            if (bytesPerSecond > 0)
                secondsLeft = (fileSize - bytesWritten) / bytesPerSecond;

            String remaingTime = "Remaining time: "
                + (bytesPerSecond == 0 ? "N/A" : Utils
                    .formatDuration(secondsLeft)
                    + " ("
                    + Utils.formatByte(bytesPerSecond) + "/s)");

            monitor.subTask(remaingTime);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                monitor.setCanceled(true);
                continue;
            }
        }

        org.jivesoftware.smackx.filetransfer.FileTransfer.Status status = transfer
            .getStatus();

        if (status
            .equals(org.jivesoftware.smackx.filetransfer.FileTransfer.Status.complete))
            return;

        if (status
            .equals(org.jivesoftware.smackx.filetransfer.FileTransfer.Status.cancelled)
            && monitor.isCanceled())
            throw new LocalCancellationException();

        if (status
            .equals(org.jivesoftware.smackx.filetransfer.FileTransfer.Status.cancelled))
            throw new RemoteCancellationException(null);

        if (status
            .equals(org.jivesoftware.smackx.filetransfer.FileTransfer.Status.error))
            throw new IOException(transfer.getError().getMessage(),
                transfer.getException());

        throw new RemoteCancellationException(null);
    }
}
