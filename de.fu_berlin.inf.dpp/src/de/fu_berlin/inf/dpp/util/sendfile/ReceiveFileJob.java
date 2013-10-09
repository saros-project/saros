package de.fu_berlin.inf.dpp.util.sendfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.progress.IProgressConstants;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.ConnectionException;
import de.fu_berlin.inf.dpp.exceptions.ReceiverGoneException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.util.Utils;

public class ReceiveFileJob extends StreamJob {
    private static final Logger log = Logger.getLogger(ReceiveFileJob.class);

    protected boolean receivedSuccessfully = false;
    protected FileDescription fileDescription;

    public ReceiveFileJob(StreamSession session) {
        super(Messages.SendFileAction_receive_title);
        setStreamSession(session);
    }

    @Override
    protected IStatus run(IProgressMonitor ipmonitor) {
        SubMonitor monitor = SubMonitor.convert(ipmonitor);
        setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);

        try {
            if (getStreamSession().getInitiationDescription() instanceof FileDescription) {
                fileDescription = (FileDescription) getStreamSession()
                    .getInitiationDescription();
            } else {
                log.error("Got no filedescription!");
                return new Status(IStatus.ERROR, Saros.SAROS,
                    Messages.SendFileAction_status_cannot_receive_file_text);
            }
            setName(MessageFormat.format(
                Messages.SendFileAction_status_receiving_text,
                fileDescription.name, getStreamSession().getRemoteJID()));
            monitor.beginTask(
                Messages.SendFileAction_monitor_choose_location_text, 102);

            File file;
            try {
                file = saveFile(fileDescription.name,
                    monitor.newChild(1, SubMonitor.SUPPRESS_SETTASKNAME));
            } catch (SarosCancellationException e) {
                monitor.subTask(Messages.SendFileAction_monitor_canceled_text);
                return Status.CANCEL_STATUS;
            }

            monitor.setTaskName(Messages.SendFileAction_monitor_receiving_text);

            FileOutputStream fileOutputStream;
            try {
                monitor.subTask(Messages.SendFileAction_monitor_opening_text);
                fileOutputStream = new FileOutputStream(file);
                monitor.worked(1);
            } catch (FileNotFoundException e) {
                DialogUtils.showErrorPopup(log,
                    Messages.SendFileAction_error_cannot_open_file_title,
                    Messages.SendFileAction_error_cannot_open_file_message, e,
                    monitor);
                return new Status(IStatus.ERROR, Saros.SAROS,
                    Messages.SendFileAction_status_cannot_open_file, e);
            }

            try {
                receive(getStreamSession(), fileOutputStream,
                    monitor.newChild(100, SubMonitor.SUPPRESS_SETTASKNAME));
            } catch (RemoteCancellationException e) {
                monitor.subTask(Messages.SendFileAction_monitor_canceled_text);
                return Status.CANCEL_STATUS;
            } catch (IOException e) {
                if (stopped) {
                    // interrupted IO
                    monitor
                        .subTask(Messages.SendFileAction_monitor_canceled_text);
                    return Status.CANCEL_STATUS;
                }
                if (streamException == null) {
                    // plain IOE
                    log.error("Error while receiving file: ", e);
                    return new Status(IStatus.ERROR, Saros.SAROS,
                        Messages.SendFileAction_error_receiving_file_text, e);
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
                    log.error("Unexpected error: ", streamException);
                    return new Status(IStatus.ERROR, Saros.SAROS,
                        Messages.SendFileAction_status_unexpected_error_text,
                        streamException);
                }
            } catch (SarosCancellationException e) {
                monitor.subTask(Messages.SendFileAction_monitor_canceled_text);
                return Status.CANCEL_STATUS;
            }

        } finally {
            if (!stopped && streamException == null)
                getStreamSession().stopSession();
            readyToStop.countDown();
            monitor.done();
        }

        monitor
            .setTaskName(receivedSuccessfully ? Messages.SendFileAction_monitor_successful_received_file_text
                : Messages.SendFileAction_monitor_could_not_receive_whole_file_text);
        return receivedSuccessfully ? Status.OK_STATUS : Status.CANCEL_STATUS;
    }

    protected void receive(final StreamSession session,
        FileOutputStream fileOutputStream, SubMonitor monitor)
        throws IOException, SarosCancellationException,
        RemoteCancellationException {
        monitor.subTask(Messages.SendFileAction_monitor_start_receive_file);

        long fileSize = ((FileDescription) session.getInitiationDescription()).size;
        // TODO care for files bigger than 2GB
        monitor
            .setWorkRemaining(fileSize > Integer.MAX_VALUE ? Integer.MAX_VALUE
                : (int) fileSize);

        long received = 0;
        InputStream in = session.getInputStream(0);
        byte[] buffer = new byte[session.getService().getChunkSize()[0]];

        StopWatch watch = new StopWatch();
        watch.start();
        try {
            int readBytes;
            boolean canceled = false;
            while ((readBytes = in.read(buffer)) > 0) {
                received += readBytes;
                fileOutputStream.write(buffer, 0, readBytes);

                monitor.worked(readBytes);
                monitor.subTask(Utils.throughput(received, watch.getTime()));

                if (monitor.isCanceled() && !canceled) {
                    // just stop once
                    getStreamSession().stopSession();
                    canceled = true;
                }
                if (Thread.interrupted() && !monitor.isCanceled())
                    throw new RemoteCancellationException();
            }
            fileOutputStream.flush();

            if (monitor.isCanceled())
                throw new SarosCancellationException();

            receivedSuccessfully = received == fileSize;

        } finally {
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(in);
            monitor.done();
        }
    }

    /**
     * Prompts the user to choose a location for saving a file. If the file
     * exists, user is asked for confirmation to overwrite it, if he declines he
     * will be prompted again.
     * 
     * @return chosen file
     * @throws SarosCancellationException
     *             Dialog was canceled
     */
    protected File saveFile(final String targetFilename, SubMonitor monitor)
        throws SarosCancellationException {
        Callable<String> getFile = new Callable<String>() {

            @Override
            public String call() throws Exception {
                FileDialog fd = new FileDialog(EditorAPI.getShell(), SWT.SAVE);
                fd.setText(Messages.SendFileAction_dialog_choose_location_title);
                fd.setFileName(targetFilename);
                File file = null;
                String destinationFilename;
                boolean again = false;
                do {
                    destinationFilename = fd.open();

                    if (destinationFilename == null)
                        // canceled
                        return null;

                    file = new File(destinationFilename);

                    if (file.exists()) {
                        again = !DialogUtils.openQuestionMessageDialog(
                            EditorAPI.getShell(),
                            Messages.SendFileAction_dialog_file_exists_title,
                            Messages.SendFileAction_dialog_file_exists_message);

                    }
                } while (again);
                return destinationFilename;
            }
        };

        monitor.setWorkRemaining(1);

        String filename = null;
        try {
            filename = SWTUtils.runSWTSync(getFile);
        } catch (Exception e) {
            // should not happen
            log.error("Unexpected error: ", e);
        } finally {
            monitor.worked(1);
            monitor.done();
        }

        if (filename == null)
            throw new SarosCancellationException();

        return new File(filename);
    }

}