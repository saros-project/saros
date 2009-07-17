package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IDocument;
import org.picocontainer.annotations.Inject;

import bmsi.util.Diff;
import bmsi.util.DiffPrint;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
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
 */
@Component(module = "consistency")
public class ConsistencyWatchdogClient {

    private static Logger log = Logger
        .getLogger(ConsistencyWatchdogClient.class);

    protected Set<IPath> pathsWithWrongChecksums = new CopyOnWriteArraySet<IPath>();

    protected boolean executingChecksumErrorHandling;

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

    protected ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0,
        TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2),
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

            log.info("Received consistency file [" + from.getName() + "] "
                + path.toString());

            ISharedProject project = sessionManager.getSharedProject();

            // Project might have ended in between
            if (project == null)
                return false;

            final IFile file = project.getProject().getFile(path);

            if (log.isInfoEnabled()) {
                input = logDiff(log, from, path, input, file);
            }

            final InputStream toWrite = input;
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {

                    // TODO should be reported to the user
                    SubMonitor monitor = SubMonitor
                        .convert(new NullProgressMonitor());
                    try {
                        FileUtil.writeFile(toWrite, file, monitor);
                    } catch (CoreException e) {
                        // TODO inform user
                        log.error("Could not restore file: " + file);
                    }
                }
            });

            ConcurrentDocumentManager concurrentManager = project
                .getConcurrentDocumentManager();

            // The file contents has been replaced, now reset Jupiter
            if (concurrentManager.isManagedByJupiter(path))
                concurrentManager.resetJupiterClient(path);

            // Trigger a new consistency check, so we don't have to wait for new
            // checksums from the host
            if (executor.getQueue().size() == 0)
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
            executor.submit(Util.wrapSafe(log, new Runnable() {
                public void run() {
                    performCheck(currentChecksums);
                }
            }));
        } catch (RejectedExecutionException e) {
            /*
             * Ignore Checksums that arrive before we are done processing the
             * last set of Checksums.
             */
            log.warn("Received Checksums before processing"
                + " of previous checksums finished");
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
            log.warn("Consistency Check triggered with out"
                + " preceeding call to setChecksums()");
            return;
        }

        log.trace(String.format(
            "Received %d checksums for %d existing inconsistencies", checksums
                .size(), pathsWithWrongChecksums.size()));

        Set<IPath> newInconsistencies = new CopyOnWriteArraySet<IPath>();

        for (DocumentChecksum checksum : checksums) {
            if (isInconsistent(checksum)) {
                newInconsistencies.add(checksum.getPath());
            }
        }

        pathsWithWrongChecksums = newInconsistencies;
        if (pathsWithWrongChecksums.isEmpty()) {
            if (inconsistencyToResolve.getValue()) {
                log.info("All Inconsistencies are resolved");
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

        if (!checksum.existsFile()) {
            /*
             * If the checksum tells us that the file does not exist at the
             * host, check whether we still have it. If it exists, we do have an
             * inconsistency
             */
            return file.exists();
        }

        /*
         * If the checksum tells us, that the file exists, but we do not have
         * it, it is an inconsistency as well
         */
        if (!file.exists()) {
            return true;
        }

        IDocument doc = editorManager.getDocument(path);

        // if doc is still null give up
        if (doc == null) {
            log.warn("Could not check checksum of file " + path.toOSString());
            return false;
        }

        if ((doc.getLength() != checksum.getLength())
            || (doc.get().hashCode() != checksum.getHash())) {

            long lastEdited = editorManager.getLastEditTime(path);

            long lastRemoteEdited = editorManager.getLastRemoteEditTime(path);

            if ((System.currentTimeMillis() - lastEdited) > 4000
                && (System.currentTimeMillis() - lastRemoteEdited > 4000)) {

                log.debug(String.format(
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
        sharedProject = newSharedProject;
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
                log.info("Diff of inconsistency: \n" + writer);
            }
        } catch (CoreException e) {
            log.error("Can't read file content", e);
        } catch (IOException e) {
            log.error("Can't convert file content to String", e);
        }
        return input;
    }

    Object lock;

    /**
     * Start a consistency recovery by sending a checksum error to the host and
     * waiting for his reply.
     * 
     * @blocking This method returns after the recovery has finished
     */
    public void runRecovery() {

        final Object myLock;

        synchronized (this) {

            lock = myLock = new Object();

            if (executingChecksumErrorHandling) {
                log.error("Restarting Checksum Error Handling"
                    + " while another operation is running");
                while (executingChecksumErrorHandling) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            // Raise flag, so we know that we are currently performing a
            // recovery
            executingChecksumErrorHandling = true;
        }

        Set<IPath> pathsOfHandledFiles = new HashSet<IPath>(
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
        transmitter.sendFileChecksumErrorMessage(Collections
            .singletonList(sharedProject.getHost().getJID()),
            pathsOfHandledFiles, false);

        // block until all inconsistencies are resolved
        Set<IPath> remainingFiles = new HashSet<IPath>(pathsOfHandledFiles);
        while (remainingFiles.size() > 0 && lock == myLock) {
            remainingFiles.retainAll(pathsWithWrongChecksums);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }

        // Unregister from dataTransferManager
        dataTransferManager.removeDataReceiver(receiver);

        // Send message to host that inconsistency are handled
        transmitter.sendFileChecksumErrorMessage(Collections
            .singletonList(sharedProject.getHost().getJID()),
            pathsOfHandledFiles, true);

        // Clear-flag indicating recovery is done
        executingChecksumErrorHandling = false;
    }
}
