package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.picocontainer.annotations.Inject;

import bmsi.util.Diff;
import bmsi.util.DiffPrint;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.IDataReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SessionView;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This class is responsible to process checksums sent to us from the server by
 * checking our locally existing files against them.
 * 
 * If an inconsistency is detected the inconsistency state is set via the
 * {@link IsInconsistentObservable}. This enables the {@link ConsistencyAction}
 * (a.k.a. the yellow triangle) in the {@link SessionView}.
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class ConsistencyWatchdogClient {

    private static Logger logger = Logger
        .getLogger(ConsistencyWatchdogClient.class);

    protected final Set<IPath> pathsWithWrongChecksums = new CopyOnWriteArraySet<IPath>();

    protected boolean executingChecksumErrorHandling;

    protected Set<IPath> pathsOfHandledFiles;

    protected ISharedProject sharedProject;

    @Inject
    protected IsInconsistentObservable inconsistencyToResolve;

    @Inject
    protected EditorManager editorManager;

    protected SessionManager sessionManager;

    @Inject
    protected ITransmitter transmitter;

    @Inject
    protected DataTransferManager dataTransferManager;

    protected ExecutorService executor = new ThreadPoolExecutor(1, 1, 0,
        TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1),
        new NamedThreadFactory("ChecksumCruncher-"));

    IDataReceiver receiver = new IDataReceiver() {

        public boolean receivedArchive(TransferDescription data,
            InputStream input) {
            return false;
        }

        public boolean receivedFileList(TransferDescription data,
            InputStream input) {
            return false;
        }

        public boolean receivedResource(JID from, IPath path,
            InputStream input, int sequenceNumber) {

            logger.debug("Received consistency file [" + from.getName() + "] "
                + path.toString());

            ISharedProject project = sessionManager.getSharedProject();

            // Project might have ended in between
            if (project == null)
                return false;

            final IFile file = project.getProject().getFile(path);

            if (logger.isDebugEnabled()) {
                input = logDiff(logger, from, path, input, file);
            }

            final InputStream toWrite = input;
            Util.runSafeSWTSync(logger, new Runnable() {
                public void run() {
                    FileUtil.writeFile(toWrite, file);
                }
            });

            ConcurrentDocumentManager concurrentManager = project
                .getConcurrentDocumentManager();

            // The file contents has been replaced, now reset Jupiter
            if (concurrentManager.isManagedByJupiter(path))
                concurrentManager.resetJupiterClient(path);

            // Trigger a new consistency check, so we don't have to wait for new
            // checksums from the host
            checkConsistency();

            return true;
        }

    };

    /**
     * Starts a new Consistency Check.
     * 
     * If a check is already in progress, nothing happens (but a warning)
     * 
     * @nonBlocking This method returns immediately.
     */
    public void checkConsistency() {

        try {
            executor.submit(Util.wrapSafe(logger, new Runnable() {
                public void run() {
                    performCheck(currentChecksums);
                }
            }));
        } catch (RejectedExecutionException e) {
            /*
             * Ignore Checksums that arrive before we are done processing the
             * last set of Checksums.
             */
            logger
                .warn("Received Checksums before processing of previous checksums finished");
        }

    }

    protected List<DocumentChecksum> currentChecksums;

    public void setChecksums(List<DocumentChecksum> checksums) {
        this.currentChecksums = checksums;
    }

    /**
     * Checks the local documents against the given checksums.
     * 
     * Use the VariableProxy getConsistenciesToResolve() to be notified if
     * inconsistencies are found or resolved.
     * 
     * @param checksums
     *            the checksums to check the documents against
     * 
     * @nonReentrant This method cannot be called twice at the same time.
     */
    public void performCheck(List<DocumentChecksum> checksums) {

        if (checksums == null) {
            logger
                .warn("Consistency Check triggered with out preceeding call to setChecksums()");
            return;
        }

        logger.trace(String.format(
            "Received %d checksums for %d inconsistencies", checksums.size(),
            pathsWithWrongChecksums.size()));

        pathsWithWrongChecksums.clear();

        for (DocumentChecksum checksum : checksums) {
            if (isInconsistent(checksum)) {
                pathsWithWrongChecksums.add(checksum.getPath());
            }
        }

        if (pathsWithWrongChecksums.isEmpty()) {
            if (inconsistencyToResolve.getValue()) {
                logger.info("All Inconsistencies are resolved");
                inconsistencyToResolve.setValue(false);
            }
        } else {
            inconsistencyToResolve.setValue(true);
        }

    }

    private boolean isInconsistent(DocumentChecksum checksum) {
        IPath path = checksum.getPath();

        ISharedProject sharedProject = sessionManager.getSharedProject();
        IFile file = sharedProject.getProject().getFile(path);
        if (!file.exists()) {
            return true;
        }

        IDocument doc = editorManager.getDocument(path);

        // if doc is still null give up
        if (doc == null) {
            logger
                .warn("Could not check checksum of file " + path.toOSString());
            return false;
        }

        if ((doc.getLength() != checksum.getLength())
            || (doc.get().hashCode() != checksum.getHash())) {

            long lastEdited = editorManager.getLastEditTime(path);

            long lastRemoteEdited = editorManager.getLastRemoteEditTime(path);

            if ((System.currentTimeMillis() - lastEdited) > 4000
                && (System.currentTimeMillis() - lastRemoteEdited > 4000)) {

                logger.debug(String.format(
                    "Inconsistency detected: %s L(%d %s %d) H(%x %s %x)", path
                        .toString(), doc.getLength(),
                    doc.getLength() == checksum.getLength() ? "==" : "!=",
                    checksum.getLength(), doc.get().hashCode(), doc.get()
                        .hashCode() == checksum.getHash() ? "==" : "!=",
                    checksum.getHash()));

                return true;
            }
        }
        return false;
    }

    /**
     * Returns the variable proxy which stores the current inconsistency state
     * 
     */
    public ObservableValue<Boolean> getConsistencyToResolve() {
        return this.inconsistencyToResolve;
    }

    /**
     * Returns the set of files for which the ConsistencyWatchdog has identified
     * an inconsistency
     */
    public Set<IPath> getPathsWithWrongChecksums() {
        return this.pathsWithWrongChecksums;
    }

    /**
     * This method starts or stops the error-handling when an inconsistency are
     * detected.
     * 
     * @param newState
     *            If <code>true</code> the method starts the error handling. If
     *            <code>false</code> the watchdog unregister his
     *            {@link IDataReceiver} and finish the error handling.
     */
    public void setChecksumErrorHandling(boolean newState) {

        if (executingChecksumErrorHandling && newState) {
            logger.warn("Restarting Checksum Error Handling"
                + " while another operation is running");
            // HACK If we programmed correctly this should not happen
            executingChecksumErrorHandling = false;
        }

        if (newState != executingChecksumErrorHandling) {

            executingChecksumErrorHandling = newState;

            if (newState) {

                pathsOfHandledFiles = new CopyOnWriteArraySet<IPath>(
                    pathsWithWrongChecksums);

                // Register as a receiver of incoming files...
                dataTransferManager.addDataReceiver(receiver);

                for (final IPath path : pathsOfHandledFiles) {

                    // Save document before asking host to resend
                    try {
                        editorManager.saveLazy(path);
                    } catch (FileNotFoundException e) {
                        // Sending the checksum error message should recreate
                        // this file
                    }
                }

                // Send checksumErrorMessage to host
                transmitter.sendFileChecksumErrorMessage(getParticipants(),
                    pathsOfHandledFiles, false);

            } else {

                // Unregister from dataTransferManager
                dataTransferManager.removeDataReceiver(receiver);

                // Send message to host that inconsistency are handled
                transmitter.sendFileChecksumErrorMessage(getParticipants(),
                    pathsOfHandledFiles, true);

                pathsOfHandledFiles.clear();
            }
        }
    }

    public List<JID> getParticipants() {
        ArrayList<JID> result = new ArrayList<JID>();
        for (User user : sharedProject.getParticipants()) {
            result.add(user.getJID());
        }
        return result;
    }

    public ConsistencyWatchdogClient(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        sessionManager.addSessionListener(new AbstractSessionListener() {
            @Override
            public void sessionStarted(ISharedProject session) {
                setSharedProject(session);
            }

            @Override
            public void sessionEnded(ISharedProject session) {
                setSharedProject(null);
            }
        });

        setSharedProject(sessionManager.getSharedProject());
    }

    private void setSharedProject(ISharedProject newSharedProject) {

        // Unregister from previous project
        if (sharedProject != null) {
            this.pathsOfHandledFiles.clear();

        }

        sharedProject = newSharedProject;

        // Register to new project
        if (sharedProject != null) {
            this.pathsOfHandledFiles = new CopyOnWriteArraySet<IPath>();
        }
    }

    public static InputStream logDiff(Logger log, JID from, IPath path,
        InputStream input, IFile file) {
        try {
            // save input in a byte[] for later
            byte[] inputBytes = IOUtils.toByteArray(input);

            // reset input
            input = new ByteArrayInputStream(inputBytes);

            // get stream from old file
            InputStream oldStream = file.getContents();
            InputStream newStream = new ByteArrayInputStream(inputBytes);

            // read Lines from
            Object[] oldContent = IOUtils.readLines(oldStream).toArray();
            Object[] newContent = IOUtils.readLines(newStream).toArray();

            // Calculate diff of the two files
            Diff diff = new Diff(oldContent, newContent);
            Diff.Change script = diff.diff_2(false);

            // log diff
            DiffPrint.UnifiedPrint print = new DiffPrint.UnifiedPrint(
                oldContent, newContent);
            Writer writer = new StringWriter();
            print.setOutput(writer);
            print.print_script(script);

            String diffAsString = writer.toString();

            if (diffAsString == null || diffAsString.trim().length() == 0) {
                log.error("No inconsistency found in file [" + from.getName()
                    + "] " + path.toString());
            } else {
                log.debug("Diff of inconsistency: \n" + writer);
            }
        } catch (CoreException e) {
            log.error("Can't read file content", e);
        } catch (IOException e) {
            log.error("Can't convert file content to String", e);
        }
        return input;
    }
}
