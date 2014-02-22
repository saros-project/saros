package de.fu_berlin.inf.dpp.editor;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.util.AutoHashMap;

/**
 * Listener registered on Editors to be informed about their dirty state.
 * 
 * There is one global DirtyStateListener for all editors!
 */
public class DirtyStateListener implements IElementStateListener {

    private static final Logger log = Logger
        .getLogger(DirtyStateListener.class);

    protected final EditorManager editorManager;

    DirtyStateListener(EditorManager editorManager) {
        this.editorManager = editorManager;
    }

    public boolean enabled = true;

    @Override
    public void elementDirtyStateChanged(Object element, boolean isDirty) {

        if (!enabled)
            return;

        if (isDirty)
            return;

        if (!this.editorManager.hasWriteAccess)
            return;

        if (!(element instanceof FileEditorInput))
            return;

        final IFile file = ((FileEditorInput) element).getFile();
        final ISarosSession sarosSession = editorManager.sarosSession;

        if (sarosSession == null
            || !sarosSession.isShared(ResourceAdapterFactory.create(file
                .getProject()))) {
            return;
        }

        SWTUtils.runSafeSWTSync(log, new Runnable() {

            @Override
            public void run() {

                // Only trigger save events for files managed in the editor pool
                if (!editorManager.isConnected(file)) {
                    return;
                }

                EditorManager.log.debug("Dirty state reset for: "
                    + file.toString());
                editorManager.sendEditorActivitySaved(new SPath(
                    ResourceAdapterFactory.create(file)));
            }
        });
    }

    @Override
    public void elementContentAboutToBeReplaced(Object element) {
        // ignore
    }

    @Override
    public void elementContentReplaced(Object element) {
        // ignore
    }

    @Override
    public void elementDeleted(Object element) {
        // ignore
    }

    @Override
    public void elementMoved(Object originalElement, Object movedElement) {
        // ignore
    }

    AutoHashMap<IDocumentProvider, Set<IEditorInput>> documentProviders = AutoHashMap
        .getSetHashMap();

    public void register(IDocumentProvider documentProvider, IEditorInput input) {

        Set<IEditorInput> inputs = documentProviders.get(documentProvider);
        if (inputs.size() == 0) {
            documentProvider.addElementStateListener(this);
        }
        inputs.add(input);
    }

    public void unregister(IDocumentProvider documentProvider,
        IEditorInput input) {

        Set<IEditorInput> inputs = documentProviders.get(documentProvider);
        inputs.remove(input);
        if (inputs.size() == 0) {
            documentProvider.removeElementStateListener(this);
            documentProviders.remove(documentProvider);
        }
    }

    public void unregisterAll() {

        for (IDocumentProvider provider : documentProviders.keySet()) {
            log.warn("DocumentProvider was not correctly"
                + " unregistered yet, EditorPool must be corrupted: "
                + documentProviders);
            provider.removeElementStateListener(this);
        }
        documentProviders.clear();
    }

}