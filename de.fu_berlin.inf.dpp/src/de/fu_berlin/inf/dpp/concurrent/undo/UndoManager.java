package de.fu_berlin.inf.dpp.concurrent.undo;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IActivityReceiver;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.jupiter.InclusionTransformation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.GOTOInclusionTransformation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.ITextOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.undo.OperationHistory.EditorHistoryEntry;
import de.fu_berlin.inf.dpp.concurrent.undo.OperationHistory.Type;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This UndoManager is not an implementation of the Eclipse IUndoManager. It
 * only listens to the internal Eclipse UndoManager, catches every Typing
 * Operation about to be undone and calls instead this.undo().
 * 
 * Warning: After the first Undo the Undo button stays activated, even if there
 * is nothing to undo. This is caused by design because we cancel the regular
 * Eclipse Undo operation. So there is always something to undo in the Eclipse
 * History and the button is never deactivated.
 * 
 * TODO This UndoManager is switched off currently. To activate it it has to be
 * added to the PicoContainer in Saros.class.
 */
@Component(module = "undo")
public class UndoManager implements Disposable, IActivityProvider {

    private static Logger log = Logger.getLogger(UndoManager.class.getName());

    protected List<TextEditActivity> expectedActivities = new LinkedList<TextEditActivity>();

    /**
     * Every IUndoableOperation has a label to classify it. Typing operations
     * have this label - as long as Eclipse doesn't change the label.
     */
    protected static final String TYPING_LABEL = "Typing";

    @Inject
    protected Saros saros;

    @Inject
    protected PreferenceUtils preferences;

    protected SessionManager sessionManager;

    protected ISharedProject sharedProject;

    protected InclusionTransformation transformation = new GOTOInclusionTransformation();

    /**
     * The concurrent OperationHistory
     */
    protected OperationHistory undoHistory = new OperationHistory();

    protected IOperationHistory eclipseHistory = OperationHistoryFactory
        .getOperationHistory();

    protected IUndoContext context = IOperationHistory.GLOBAL_UNDO_CONTEXT;

    protected EditorManager editorManager;

    protected List<IActivityProvider> providers = new LinkedList<IActivityProvider>();

    protected List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

    protected IPath currentActiveEditor = null;

    /**
     * This UndoManager is disabled when not in a Saros session.
     */
    protected boolean enabled;

    /**
     * To avoid splitting local operations in too many pieces, the local
     * operations that are added to the history are combined with the subsequent
     * ones. A combined operation begins when Eclipse triggered the
     * OPERATION_ADDED history event and ends right before the next one.
     */
    protected Operation currentLocalCompositeOperation = null;

    /**
     * The currentLocalAtomicOperation is the latest operation that was applied
     * locally.
     */
    protected Operation currentLocalAtomicOperation = null;

    /**
     * The most important part of the undo integration. If an Undo/Redo is
     * triggered the Eclipse history consults all of its IOperationApprover
     * whether the Undo / Redo may be applied. We veto it and call this.undo() /
     * this.redo() instead.
     */
    protected IOperationApprover operationBlocker = new IOperationApprover() {

        protected String opInfo(IUndoableOperation operation) {
            return ("new " + operation.getLabel() + ": " + operation);
        }

        public IStatus proceedRedoing(final IUndoableOperation operation,
            IOperationHistory history, IAdaptable info) {

            if (!enabled)
                return Status.OK_STATUS;

            if (currentActiveEditor == null) {
                log.info("Redo called on an unknown editor");
                return Status.OK_STATUS;
            }

            if (log.isDebugEnabled())
                log.debug(opInfo(operation));

            if (operation.getLabel().equals(TYPING_LABEL)
                || operation.getLabel().equals(NullOperation.LABEL)) {

                updateCurrentLocalAtomicOperation(null);
                storeCurrentLocalOperation();

                Util.runSafeSWTSync(log, new Runnable() {
                    public void run() {
                        log.debug("redoing operation " + operation);
                        redo(currentActiveEditor);

                        /*
                         * For reactivating redo an undo has to be simulated, so
                         * the Eclipse history knows, there is something to
                         * redo.
                         */
                        if (undoHistory.canRedo(currentActiveEditor))
                            simulateUndo();
                    }
                });
                return Status.CANCEL_STATUS;
            }
            return Status.OK_STATUS;
        }

        public IStatus proceedUndoing(final IUndoableOperation operation,
            IOperationHistory history, IAdaptable info) {

            if (!enabled)
                return Status.OK_STATUS;

            if (currentActiveEditor == null) {
                log.info("Undo called on an unknown editor");
                return Status.OK_STATUS;
            }

            if (log.isDebugEnabled())
                log.debug(opInfo(operation));

            if (operation.getLabel().equals(TYPING_LABEL)) {
                updateCurrentLocalAtomicOperation(null);
                storeCurrentLocalOperation();

                Util.runSafeSWTSync(log, new Runnable() {
                    public void run() {
                        log.debug("undoing operation " + operation);
                        undo(currentActiveEditor);

                        if (undoHistory.canRedo(currentActiveEditor))
                            simulateUndo();
                    }
                });
                return Status.CANCEL_STATUS;
            }
            return Status.OK_STATUS;
        }
    };

    /**
     * A NullOperation is an IUndoableOperation that can be executed, undone and
     * redone without having any effect, except returning OK_STATUS.
     */
    protected static class NullOperation extends AbstractOperation {
        /*
         * The user shouldn't see a difference in the label between normal
         * typing operations and NullOperations. But we should be able to
         * distinguish them. So a single space is added to the typing label.
         */
        public static final String LABEL = TYPING_LABEL + " ";

        public NullOperation() {
            super(LABEL);
        }

        @Override
        public IStatus execute(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {
            return Status.OK_STATUS;
        }

        @Override
        public IStatus redo(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {
            return Status.OK_STATUS;
        }

        @Override
        public IStatus undo(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {
            return Status.OK_STATUS;
        }

    }

    /**
     * The UndoManager only works during a running Saros session. Otherwise the
     * Eclipse UndoManager has to manage Undos and Redos. This SessionListener
     * enables and disables the UndoManager and does cleanup works at the begin
     * and the end of a session.
     */
    protected ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            undoHistory.clear();
            project.addActivityProvider(UndoManager.this);
            enabled = preferences.isConcurrentUndoActivated();
            eclipseHistory.addOperationApprover(operationBlocker);
            sharedProject = project;
        }

        @Override
        public void sessionEnded(ISharedProject project) {
            project.removeActivityProvider(UndoManager.this);
            undoHistory.clear();
            enabled = false;
            eclipseHistory.removeOperationApprover(operationBlocker);
            sharedProject = null;
            currentLocalCompositeOperation = null;
            currentLocalAtomicOperation = null;
        }
    };

    /**
     * Updates the local current active editor. If the editor changes the
     * current local operation is stored in history.
     */
    protected ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {

        @Override
        public void activeEditorChanged(User user, IPath newActiveEditor) {

            if (!user.isLocal() || currentActiveEditor == newActiveEditor)
                return;

            updateCurrentLocalAtomicOperation(null);
            storeCurrentLocalOperation();
            currentActiveEditor = newActiveEditor;
        }

        @Override
        public void editorRemoved(User user, IPath closedEditor) {
            if (currentActiveEditor == closedEditor) {
                updateCurrentLocalAtomicOperation(null);
                storeCurrentLocalOperation();
                currentActiveEditor = null;
            }

            undoHistory.clearEditorHistory(closedEditor);
        }
    };

    protected IActivityListener activityListener = new IActivityListener() {

        public void activityCreated(IActivity activity) {
            activity.dispatch(activityReceiver);
        }
    };

    protected IActivityReceiver activityReceiver = new AbstractActivityReceiver() {

        /**
         * @return true if the given activity was created locally
         */
        protected boolean local(IActivity activity) {
            return activity.getSource().equals(saros.getMyJID());
        }

        /**
         * Updates the current local operation and adds remote operations to the
         * undo history.
         */
        @Override
        public void receive(TextEditActivity textEditActivity) {

            if (!enabled)
                return;

            /*
             * When performing an undo/redo there are fired activities which are
             * expected and have to be ignored when coming back.
             */
            if (expectedActivities.remove(textEditActivity))
                return;

            Operation operation = textEditActivity.toOperation();

            if (!local(textEditActivity)) {
                if (currentLocalCompositeOperation != null)
                    currentLocalCompositeOperation = transformation.transform(
                        currentLocalCompositeOperation, operation,
                        Boolean.FALSE);
                if (currentLocalAtomicOperation != null) {
                    currentLocalAtomicOperation = transformation.transform(
                        currentLocalAtomicOperation, operation, Boolean.FALSE);
                }
                log.debug("adding remote " + operation + " to history");
                undoHistory.add(textEditActivity.getEditor(), Type.REMOTE,
                    operation);
            } else {
                if (!textEditActivity.getEditor().equals(currentActiveEditor)) {
                    log
                        .error("Editor of the local TextEditActivity is not the current "
                            + "active editor. Possibly the current active editor is not"
                            + " up to date.");
                    return;
                }
                updateCurrentLocalAtomicOperation(operation);
            }
            return;
        }
    };

    public IOperationHistoryListener historyListener = new IOperationHistoryListener() {

        public void historyNotification(OperationHistoryEvent event) {

            if (!enabled)
                return;

            /*
             * OPERATION_ADDED is triggered when Eclipse adds a new operation to
             * its history. We do the same on this event.
             */
            if (event.getEventType() == OperationHistoryEvent.OPERATION_ADDED) {
                storeCurrentLocalOperation();
                updateCurrentLocalAtomicOperation(null);
            }

            /*
             * OPERATION_CHANGED is triggered when Eclipse changes the operation
             * that is recently added to its history.
             * 
             * For example: Eclipse adds Insert(3,"A") to its operation and
             * later on the user enters B at position 4. If Eclipse decides not
             * to add a new Undo step it changes the most recent operation in
             * history to Insert(3, "AB")
             */
            if (event.getEventType() == OperationHistoryEvent.OPERATION_CHANGED) {
                updateCurrentLocalAtomicOperation(null);
            }
        }
    };

    public UndoManager(SessionManager sessionManager,
        EditorManager editorManager) {

        if (log.isDebugEnabled())
            DefaultOperationHistory.DEBUG_OPERATION_HISTORY_APPROVAL = true;

        OperationHistoryFactory.getOperationHistory()
            .addOperationHistoryListener(historyListener);

        sessionManager.addSessionListener(sessionListener);
        this.sessionManager = sessionManager;

        editorManager.addActivityListener(this.activityListener);
        addProvider(editorManager);
        this.editorManager = editorManager;

        editorManager.addSharedEditorListener(sharedEditorListener);
    }

    // just for testing
    protected UndoManager() {
        DefaultOperationHistory.DEBUG_OPERATION_HISTORY_APPROVAL = true;

        OperationHistoryFactory.getOperationHistory()
            .addOperationHistoryListener(historyListener);
    }

    /**
     * @return operation that reverts the effect of the latest local operation
     *         in the given editor
     */
    protected Operation calcUndoOperation(IPath editor) {

        if (!undoHistory.canUndo(editor))
            return new NoOperation(); // nothing to undo

        Operation lastLocal = undoHistory.getLatestLocal(editor);

        assert lastLocal != null;

        Operation undoOperation = lastLocal.invert();

        for (EditorHistoryEntry entry : undoHistory
            .entriesToLatestLocal(editor)) {

            Operation operation = entry.getOperation();

            undoOperation = transformation.transform(undoOperation, operation,
                Boolean.TRUE);

            log.debug("transformed undo: " + undoOperation);
        }

        undoHistory.replaceType(editor, lastLocal, Type.LOCAL, Type.REMOTE);
        // it is not relevant any more, so it is set remote

        undoHistory.add(editor, Type.REDOABLE, undoOperation); // save for redo

        return undoOperation;
    }

    /**
     * @return operation that reverts the effect of the latest undo in the given
     *         editor
     */
    protected Operation calcRedoOperation(IPath editor) {

        if (!undoHistory.canRedo(editor))
            return new NoOperation(); // nothing to redo

        Operation lastUndo = undoHistory.getLatestRedoable(editor);

        assert lastUndo != null;

        Operation redoOperation = lastUndo.invert();

        for (EditorHistoryEntry entry : undoHistory
            .entriesToLatestRedoable(editor)) {

            redoOperation = transformation.transform(redoOperation, entry
                .getOperation(), Boolean.TRUE);
        }
        undoHistory.replaceType(editor, lastUndo, Type.REDOABLE, Type.REMOTE);
        // it is not relevant any more, so it is set remote

        undoHistory.add(editor, Type.LOCAL, redoOperation);
        log.debug("adding " + lastUndo + " (cause: redo internal)");

        return redoOperation;
    }

    protected void undo(IPath editor) {

        Operation op = calcUndoOperation(editor);
        log.debug("calculated undo: " + op);

        // don't waste the network
        if (op instanceof NoOperation) {
            log.debug("nothing to undo in " + editor);
            return;
        }

        for (TextEditActivity activity : op
            .toTextEdit(editor, saros.getMyJID())) {
            log.debug("undone: " + activity + " in " + editor);
            fireActivity(activity);
        }
    }

    protected void redo(IPath editor) {

        Operation op = calcRedoOperation(editor);

        for (TextEditActivity activity : op
            .toTextEdit(editor, saros.getMyJID())) {
            log.debug("redone: " + activity + " in " + editor);
            fireActivity(activity);
        }
    }

    public void dispose() {
        OperationHistoryFactory.getOperationHistory()
            .removeOperationHistoryListener(historyListener);
        sessionManager.removeSessionListener(sessionListener);
        editorManager.removeActivityListener(activityListener);
        enabled = false;
        eclipseHistory.removeOperationApprover(operationBlocker);
        editorManager.removeSharedEditorListener(sharedEditorListener);
    }

    public OperationHistory getHistory() {
        return this.undoHistory;
    }

    public void addProvider(IActivityProvider provider) {
        if (!providers.contains(provider))
            providers.add(provider);
    }

    public void removeProvider(IActivityProvider provider) {
        providers.remove(provider);
    }

    protected void fireActivity(TextEditActivity activity) {

        expectedActivities.add(activity);

        List<ITextOperation> textOps = activity.toOperation()
            .getTextOperations();

        IFile file = sharedProject.getProject().getFile(currentActiveEditor);

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = EditorManager.getDocumentProvider(input);

        try {
            provider.connect(input);
        } catch (CoreException e) {
            log.error("Could not connect to a document provider on file '"
                + file.toString() + "':", e);
            return;
        }

        try {
            IDocument doc = provider.getDocument(input);
            if (doc == null) {
                log.error("Could not connect to a document provider on file '"
                    + file.toString() + "':", new StackTrace());
                return;
            }

            for (ITextOperation textOp : textOps) {
                try {
                    if (textOp instanceof DeleteOperation)
                        doc.replace(textOp.getPosition(), textOp
                            .getTextLength(), "");
                    if (textOp instanceof InsertOperation)
                        doc.replace(textOp.getPosition(), 0, textOp.getText());
                } catch (BadLocationException e) {
                    log.error("Invalid location for " + textOp);
                }
            }
        } finally {
            provider.disconnect(input);
        }
    }

    public void addActivityListener(IActivityListener listener) {
        if (!activityListeners.contains(listener)) {
            activityListeners.add(listener);
        }
    }

    public void exec(IActivity activity) {
        activity.dispatch(activityReceiver);
    }

    public void removeActivityListener(IActivityListener listener) {
        activityListeners.remove(listener);
    }

    /**
     * Adds an undone NullOperation (i.e. has no effects on executing, undoing
     * or redoing) to the Eclipse IOperationHistory to be sure that redoing is
     * possible. This is necessary for the UI Redo possibility being activated.
     */
    protected void simulateUndo() {
        IUndoableOperation auxOp = new NullOperation();
        try {
            auxOp.addContext(context);
            eclipseHistory.execute(auxOp, null, null);
            eclipseHistory.undo(context, null, null);
            if (!eclipseHistory.canRedo(context))
                log.error("Simulating Undo failed");
        } catch (ExecutionException e) {
            log.error("Simulating Undo failed");
        }
    }

    /**
     * Adds the current local composite operation to the undo history (if not
     * null) and resets it afterwards.
     */
    protected void storeCurrentLocalOperation() {
        if (currentLocalCompositeOperation == null)
            return;
        if (currentActiveEditor == null) {
            log
                .warn("Cannot store current local operation. Current active editor is unknown");
            return;
        }
        undoHistory.add(currentActiveEditor, Type.LOCAL,
            currentLocalCompositeOperation);
        currentLocalCompositeOperation = null;
        log.debug("stored current local operation");
    }

    /**
     * Integrates the current local atomic operation in the current local
     * composite operation and replaces the atomic operation by the given
     * operation.
     */
    protected void updateCurrentLocalAtomicOperation(Operation newMRO) {
        if (currentLocalCompositeOperation == null) {
            currentLocalCompositeOperation = currentLocalAtomicOperation;
        } else if (currentLocalAtomicOperation != null)
            currentLocalCompositeOperation = new SplitOperation(
                currentLocalCompositeOperation, currentLocalAtomicOperation);
        currentLocalAtomicOperation = newMRO;
    }
}
