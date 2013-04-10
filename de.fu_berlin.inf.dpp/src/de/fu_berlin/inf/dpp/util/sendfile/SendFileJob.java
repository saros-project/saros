package de.fu_berlin.inf.dpp.util.sendfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.progress.IProgressConstants;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.exceptions.ConnectionException;
import de.fu_berlin.inf.dpp.exceptions.ReceiverGoneException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.util.StopWatch;

public class SendFileJob extends StreamJob {
    private static final Logger log = Logger.getLogger(SendFileJob.class);

    /**
     * 
     */
    private final StreamServiceManager streamServiceManager;
    private final FileStreamService fileStreamService;
    protected User user;
    protected boolean sendSuccessfully = false;
    protected File fileToSend;

    public SendFileJob(final StreamServiceManager streamServiceManager,
        final FileStreamService fileStreamService, User user, File fileToSend) {
        super(MessageFormat.format(Messages.SendFileAction_send_file_job_text,
            fileToSend.getName(), user.getHumanReadableName()));

        this.streamServiceManager = streamServiceManager;
        this.fileStreamService = fileStreamService;
        this.user = user;
        this.fileToSend = fileToSend;
    }

    @Override
    protected IStatus run(IProgressMonitor ipmonitor) {
        SubMonitor monitor = SubMonitor.convert(ipmonitor);
        monitor.beginTask(Messages.SendFileAction_monitor_set_up_session_text,
            101);

        setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);

        log.info("Asking " + user + " to accept our transfer."); //$NON-NLS-1$ //$NON-NLS-2$
        monitor.subTask(MessageFormat.format(
            Messages.SendFileAction_monitor_notifying_text,
            user.getHumanReadableName()));
        try {
            setStreamSession(this.streamServiceManager.createSession(
                this.fileStreamService, user,
                FileDescription.fromFile(fileToSend), null));
            monitor.worked(1);
        } catch (RemoteCancellationException e) {
            DialogUtils.showErrorPopup(log, getName(),
                Messages.SendFileAction_error_file_transfer_rejected_text, e,
                monitor);
            return Status.CANCEL_STATUS;
        } catch (ConnectionException e) {
            DialogUtils.showErrorPopup(log, getName(),
                Messages.SendFileAction_error_no_connection_establish_text, e,
                monitor);
            return Status.CANCEL_STATUS;
        } catch (TimeoutException e) {
            DialogUtils.showErrorPopup(log, getName(),
                Messages.SendFileAction_error_timed_out_text, e, monitor);
            return Status.CANCEL_STATUS;
        } catch (ExecutionException e) {
            monitor.subTask(MessageFormat.format(
                Messages.SendFileAction_unexpected_error, e.getMessage()));
            log.error("Unexpected error: ", e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Saros.SAROS,
                Messages.SendFileAction_status_could_not_create_session_text, e);
        } catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
        }

        monitor.setTaskName(Messages.SendFileAction_monitor_sending_text);

        try {
            send(getStreamSession(), fileToSend,
                monitor.newChild(100, SubMonitor.SUPPRESS_SETTASKNAME));

        } catch (InterruptedIOException e) {
            monitor.subTask(Messages.SendFileAction_monitor_canceled_text);
            return Status.CANCEL_STATUS;
        } catch (IOException e) {
            if (streamException == null) {
                // plain IOE
                log.error("Error while sending file: ", e); //$NON-NLS-1$
                return new Status(IStatus.ERROR, Saros.SAROS,
                    "Error while sending file", e); //$NON-NLS-1$
            } else {
                // IOE because stream is down
                if (streamException instanceof ReceiverGoneException) {
                    monitor
                        .subTask(Messages.SendFileAction_monitor_receiver_left_during_transfer_text);
                    return Status.CANCEL_STATUS;
                }
                if (streamException instanceof ConnectionException) {
                    monitor
                        .subTask(Messages.SendFileAction_monitor_lost_connection_text);
                    return Status.CANCEL_STATUS;
                }
                log.error("Unexpected error: ", streamException); //$NON-NLS-1$
                return new Status(IStatus.ERROR, Saros.SAROS,
                    Messages.SendFileAction_status_unexpected_error,
                    streamException);
            }
        } catch (RemoteCancellationException e) {
            monitor.subTask(Messages.SendFileAction_monitor_canceled_text);
            return Status.CANCEL_STATUS;
        } catch (SarosCancellationException e) {
            monitor.subTask(Messages.SendFileAction_monitor_canceled_text);
            return Status.CANCEL_STATUS;
        } finally {
            readyToStop.countDown();
            monitor.done();
        }

        monitor
            .setTaskName(sendSuccessfully ? Messages.SendFileAction_monitor_successful_sent_text
                : Messages.SendFileAction_monitor_not_sent_whole_file_text);
        return sendSuccessfully ? Status.OK_STATUS : Status.CANCEL_STATUS;
    }

    @Override
    protected void canceling() {
        if (getThread() != null)
            getThread().interrupt();
        getStreamSession().stopSession();
    }

    /**
     * 
     * @throws IOException
     *             Error while reading file or writing to stream.
     * @throws RemoteCancellationException
     *             remote user canceled the operation
     * @throws SarosCancellationException
     *             local user canceled the operation
     */
    protected void send(StreamSession session, File file, SubMonitor monitor)
        throws RemoteCancellationException, IOException,
        SarosCancellationException {
        monitor.subTask(Messages.SendFileAction_monitor_start_sending_text);
        // TODO care for files bigger than 2GB
        monitor
            .setWorkRemaining((file.length() + 1) > Integer.MAX_VALUE ? Integer.MAX_VALUE
                : (int) file.length() + 1);
        byte[] buffer = new byte[this.fileStreamService.getChunkSize()[0]];
        OutputStream out = session.getOutputStream(0);
        FileInputStream fileInputStream = new FileInputStream(file);
        int numRead = 0;
        long sendBytes = 0;
        monitor.worked(1);

        StopWatch watch = new StopWatch();
        watch.start();
        try {
            while ((numRead = fileInputStream.read(buffer)) > 0
                && streamException == null) {
                if (monitor.isCanceled())
                    throw new SarosCancellationException();
                if (Thread.interrupted() || stopped)
                    throw new RemoteCancellationException();

                out.write(buffer, 0, numRead);
                sendBytes += numRead;

                monitor.worked(numRead);
                monitor.subTask(watch.throughput(sendBytes));
            }

            out.flush();
            sendSuccessfully = sendBytes == file.length();
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(fileInputStream);
            monitor.done();
        }
    }
}