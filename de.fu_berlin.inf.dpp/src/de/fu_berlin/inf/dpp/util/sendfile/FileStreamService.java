package de.fu_berlin.inf.dpp.util.sendfile;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.internal.StreamService;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.actions.SendFileAction;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.util.Utils;

public class FileStreamService extends StreamService {
    private static final Logger log = Logger.getLogger(FileStreamService.class);

    protected SendFileAction sendFileAction;

    @Override
    public int[] getChunkSize() {
        return new int[] { 64 * 1024 };
    }

    @Override
    public String getServiceName() {
        return "SendFile"; //$NON-NLS-1$
    }

    @Override
    public int getStreamsPerSession() {
        return 1;
    }

    @Override
    public long[] getMaximumDelay() {
        return new long[] { 500 };
    }

    @Override
    public boolean sessionRequest(final User from, final Object file) {
        if (sendFileAction == null)
            // no action hooked yet
            return false;

        log.info(from + " wants to send us a file."); //$NON-NLS-1$

        final FileDescription description;
        if (file instanceof FileDescription) {
            description = (FileDescription) file;
        } else {
            log.error("Other party send no FileDescription!"); //$NON-NLS-1$
            return false;
        }

        Callable<Boolean> askUser = new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {

                return DialogUtils
                    .openQuestionMessageDialog(
                        EditorAPI.getShell(),
                        Messages.SendFileAction_dialog_incoming_file_transfer_title,
                        MessageFormat
                            .format(
                                Messages.SendFileAction_dialog_incoming_file_transfer_message,
                                description.name,
                                Utils.formatByte(description.size), from));
            }
        };

        try {
            return Utils.runSWTSync(askUser);
        } catch (Exception e) {
            log.error("Unexpected exception: ", e); //$NON-NLS-1$
            return false;
        }
    }

    @Override
    public void startSession(final StreamSession newSession) {
        if (sendFileAction == null) {
            // no action hooked yet
            newSession.stopSession();
            return;
        }

        log.info("Starting FileTransferSession from " //$NON-NLS-1$
            + newSession.getRemoteJID());

        ReceiveFileJob job = new ReceiveFileJob(newSession);
        job.setUser(true);
        job.schedule();
    }

    public void hookAction(SendFileAction sendFileAction) {
        this.sendFileAction = sendFileAction;
    }

}