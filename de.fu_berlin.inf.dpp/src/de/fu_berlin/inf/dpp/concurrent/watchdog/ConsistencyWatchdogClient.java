package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SessionView;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
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

    @Inject
    protected IsInconsistentObservable inconsistencyToResolve;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected SessionManager sessionManager;

    protected ExecutorService executor = new ThreadPoolExecutor(1, 1, 0,
        TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1),
        new NamedThreadFactory("ChecksumCruncher-"));

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
     * an inconsistency (this is a subset of the files managed by Jupiter)
     */
    public Set<IPath> getPathsWithWrongChecksums() {
        return this.pathsWithWrongChecksums;
    }
}
