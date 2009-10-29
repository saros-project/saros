package de.fu_berlin.inf.dpp.concurrent.watchdog;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.TextEditActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SessionView;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.util.StackTrace;

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
public class ConsistencyWatchdogClient extends AbstractActivityProvider {

    private static Logger log = Logger
        .getLogger(ConsistencyWatchdogClient.class);

    protected ISharedProject sharedProject;

    /**
     * @Inject Injected via Constructor Injection
     */
    protected IsInconsistentObservable inconsistencyToResolve;

    @Inject
    protected EditorManager editorManager;

    /**
     * @Inject Injected via Constructor Injection
     */
    protected SessionManager sessionManager;

    @Inject
    protected ITransmitter transmitter;

    @Inject
    protected DataTransferManager dataTransferManager;

    /**
     * Returns the variable proxy which stores the current inconsistency state
     * 
     */
    public ObservableValue<Boolean> getConsistencyToResolve() {
        return this.inconsistencyToResolve;
    }

    protected HashMap<IPath, ChecksumActivity> stats = new HashMap<IPath, ChecksumActivity>();

    protected Set<IPath> pathsWithWrongChecksums = new CopyOnWriteArraySet<IPath>();

    protected ISessionListener sessionListener = new AbstractSessionListener() {
        private ISharedProjectListener sharedProjectListner = new AbstractSharedProjectListener() {

            @Override
            public void roleChanged(User user) {

                if (user.isRemote())
                    return;

                // Clear our checksums
                latestChecksums.clear();
            }
        };

        @Override
        public void sessionEnded(ISharedProject newSharedProject) {

            newSharedProject
                .removeActivityProvider(ConsistencyWatchdogClient.this);
            newSharedProject.removeListener(sharedProjectListner);

            sharedProject = null;
        }

        @Override
        public void sessionStarted(ISharedProject newSharedProject) {
            sharedProject = newSharedProject;

            stats.clear();

            pathsWithWrongChecksums.clear();
            inconsistencyToResolve.setValue(false);

            newSharedProject
                .addActivityProvider(ConsistencyWatchdogClient.this);
            newSharedProject.addListener(sharedProjectListner);
        }
    };

    public ConsistencyWatchdogClient(SessionManager sessionManager,
        IsInconsistentObservable inconsistentObservable) {
        this.sessionManager = sessionManager;
        this.inconsistencyToResolve = inconsistentObservable;

        this.sessionManager.addSessionListener(sessionListener);
    }

    public void dispose() {
        this.sessionManager.removeSessionListener(sessionListener);
    }

    public Map<IPath, ChecksumActivity> latestChecksums = new HashMap<IPath, ChecksumActivity>();

    protected IActivityReceiver activityDataObjectReceiver = new AbstractActivityReceiver() {
        @Override
        public void receive(ChecksumActivity checksumActivityDataObject) {

            latestChecksums.put(checksumActivityDataObject.getPath(),
                checksumActivityDataObject);

            performCheck(checksumActivityDataObject);
        }

        @Override
        public void receive(TextEditActivity text) {
            latestChecksums.remove(text.getEditor());
        }

        @Override
        public void receive(FileActivity fileActivityDataObject) {

            // Recoveries do not invalidate checksums :-)
            if (fileActivityDataObject.isRecovery())
                return;

            /*
             * (we do not need to handle FolderActivities because all files are
             * created/deleted via FileActivity)
             */

            switch (fileActivityDataObject.getType()) {
            case Created:
            case Removed:
                latestChecksums.remove(fileActivityDataObject.getPath());
                break;
            case Moved:
                latestChecksums.remove(fileActivityDataObject.getPath());
                latestChecksums.remove(fileActivityDataObject.getOldPath());
                break;
            default:
                log.error("Unhandled FileActivity.Type: "
                    + fileActivityDataObject);
            }
        }
    };

    /**
     * Returns the set of files for which the ConsistencyWatchdog has identified
     * an inconsistency
     */
    public Set<IPath> getPathsWithWrongChecksums() {
        return this.pathsWithWrongChecksums;
    }

    /**
     * boolean condition variable used to interrupt another thread from
     * performing a recovery in {@link #runRecovery(SubMonitor)}
     */
    private AtomicBoolean cancelRecovery = new AtomicBoolean();

    /**
     * Lock used exclusively in {@link #runRecovery(SubMonitor)} to prevent two
     * recovery operations running concurrently.
     */
    private Lock lock = new ReentrantLock();

    /**
     * Start a consistency recovery by sending a checksum error to the host and
     * waiting for his reply. <br>
     * The <strong>cancellation</strong> of this method is <strong>not
     * implemented</strong>, so cancelling the given monitor does not have any
     * effect.
     * 
     * @blocking This method returns after the recovery has finished
     * 
     * @client Can only be called on the client!
     */
    public void runRecovery(SubMonitor monitor) {
        if (sharedProject.isHost())
            throw new IllegalStateException("Can only be called on the client");

        if (!lock.tryLock()) {
            log.error("Restarting Checksum Error Handling"
                + " while another operation is running");
            try {
                // Try to cancel currently running recovery
                do {
                    cancelRecovery.set(true);
                } while (!lock.tryLock(100, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                log.error("Not designed to be interruptable");
                return;
            }
        }

        // Lock has been acquired
        try {
            cancelRecovery.set(false);

            Set<IPath> pathsOfHandledFiles = new HashSet<IPath>(
                pathsWithWrongChecksums);

            for (final IPath path : pathsOfHandledFiles) {

                if (cancelRecovery.get())
                    return;

                // Save document before asking host to resend
                try {
                    editorManager.saveLazy(path);
                } catch (FileNotFoundException e) {
                    // Sending the checksum error message should recreate
                    // this file
                }
            }

            if (cancelRecovery.get())
                return;

            int remainingFilesBefore;
            monitor.beginTask("Consistency recovery", pathsOfHandledFiles
                .size());

            // Send checksumErrorMessage to host
            transmitter.sendFileChecksumErrorMessage(Collections
                .singletonList(sharedProject.getHost().getJID()),
                pathsOfHandledFiles, false);

            // block until all inconsistencies are resolved
            Set<IPath> remainingFiles = new HashSet<IPath>(pathsOfHandledFiles);
            while (remainingFiles.size() > 0) {

                if (cancelRecovery.get())
                    return;

                remainingFilesBefore = remainingFiles.size();
                remainingFiles.retainAll(pathsWithWrongChecksums);
                monitor.worked(remainingFilesBefore - remainingFiles.size());

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
        } finally {
            monitor.done();
            lock.unlock();
        }
    }

    @Override
    public void exec(IActivity activityDataObject) {
        activityDataObject.dispatch(activityDataObjectReceiver);
    }

    private boolean isInconsistent(ChecksumActivity checksum) {
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

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = EditorManager.getDocumentProvider(input);

        try {
            provider.connect(input);
        } catch (CoreException e) {
            log.warn("Could not check checksum of file " + path.toOSString());
            return false;
        }

        try {
            IDocument doc = provider.getDocument(input);

            // if doc is still null give up
            if (doc == null) {
                log.warn("Could not check checksum of file "
                    + path.toOSString());
                return false;
            }

            if ((doc.getLength() != checksum.getLength())
                || (doc.get().hashCode() != checksum.getHash())) {

                log.debug(String.format(
                    "Inconsistency detected: %s L(%d %s %d) H(%x %s %x)", path
                        .toString(), doc.getLength(),
                    doc.getLength() == checksum.getLength() ? "==" : "!=",
                    checksum.getLength(), doc.get().hashCode(), doc.get()
                        .hashCode() == checksum.getHash() ? "==" : "!=",
                    checksum.getHash()));

                return true;
            }
        } finally {
            provider.disconnect(input);
        }
        return false;
    }

    /**
     * Will run a consistency check.
     * 
     * @return whether a consistency check could be performed or not (for
     *         instance because no current checksum is available)
     * 
     * @swt This must be called from SWT
     * 
     * @client This can only be called on the client
     */
    public boolean performCheck(IPath path) {

        if (sharedProject == null) {
            log.warn("Session already ended. Cannot perform consistency check",
                new StackTrace());
            return false;
        }

        ChecksumActivity checksumActivityDataObject = latestChecksums.get(path);
        if (checksumActivityDataObject != null) {
            performCheck(checksumActivityDataObject);
            return true;
        } else {
            return false;
        }
    }

    protected synchronized void performCheck(ChecksumActivity checksumActivity) {

        if (sharedProject.isDriver()
            && !sharedProject.getConcurrentDocumentClient().isCurrent(
                checksumActivity))
            return;

        if (isInconsistent(checksumActivity)) {
            pathsWithWrongChecksums.add(checksumActivity.path);
        } else {
            pathsWithWrongChecksums.remove(checksumActivity.path);
        }

        if (pathsWithWrongChecksums.isEmpty()) {
            if (inconsistencyToResolve.getValue()) {
                log.info("All Inconsistencies are resolved");
                inconsistencyToResolve.setValue(false);
            }
        } else {
            if (!inconsistencyToResolve.getValue()) {
                log.info("Inconsistencies have been detected");
                inconsistencyToResolve.setValue(true);
            }
        }
    }
}
