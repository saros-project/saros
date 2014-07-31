package de.fu_berlin.inf.dpp.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.editor.internal.IEditorAPI;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * The EditorPool manages the <code>IEditorParts</code> of the local user.
 * <p>
 * Currently only those parts are supported which can be traced to an
 * {@link IFile} and an {@link ITextViewer}.
 */
class EditorPool {

    private static final Logger LOG = Logger.getLogger(EditorPool.class);

    private final EditorManager editorManager;
    private final IEditorAPI editorAPI;

    EditorPool(EditorManager editorManager, IEditorAPI editorAPI) {
        this.editorManager = editorManager;
        this.editorAPI = editorAPI;
    }

    /**
     * The editorParts-map will return all EditorParts associated with a given
     * SPath. This can be potentially many because a IFile (which can be
     * identified using a IPath) can be opened in multiple editors.
     */
    private Map<SPath, Set<IEditorPart>> editorParts = new HashMap<SPath, Set<IEditorPart>>();

    /**
     * The editorInputMap contains all IEditorParts which are managed by the
     * EditorPool and stores the associated IEditorInput (the EditorInput could
     * also actually be retrieved directly from the IEditorPart).
     */
    private final Map<IEditorPart, IEditorInput> editorInputMap = new HashMap<IEditorPart, IEditorInput>();

    private final Map<IEditorPart, EditorListener> editorListeners = new HashMap<IEditorPart, EditorListener>();

    /**
     * Adds an {@link IEditorPart} to the pool. This method also connects the
     * editorPart with its data source (identified by associated {@link IFile}),
     * makes it editable for user with {@link Permission#WRITE_ACCESS}, and
     * registers listeners:
     * <ul>
     * <li>{@link IElementStateListener} on {@link IDocumentProvider} - listens
     * for the changes in the file connected with the editor (e.g. file gets
     * 'dirty')</li>
     * <li>{@link IDocumentListener} on {@link IDocument} - listens for changes
     * in the document (e.g. documents text gets changed)</li>
     * <li>{@link EditorListener} on {@link IEditorPart} - listens for basic
     * events needed for tracking of text selection and viewport changes (e.g.
     * mouse events, keyboard events)</li>
     * </ul>
     * 
     * This method will return without any effect if the given IEditorPart does
     * not a.) represent an IFile, b.) which can be referred to using an IPath
     * and c.) the IEditorPart can be mapped to an ITextViewer.
     * 
     */
    public void add(final IEditorPart editorPart) {

        LOG.trace("EditorPool.add " + editorPart + " invoked");

        final SPath path = editorAPI.getEditorPath(editorPart);

        if (path == null) {
            LOG.warn("could not find path/resource for editor: "
                + editorPart.getTitle());
            return;
        }

        if (isManaged(path, editorPart)) {
            LOG.error(
                "editor part was added twice to the pool: "
                    + editorPart.getTitle(), new StackTrace());
            return;
        }

        ITextViewer viewer = EditorAPI.getViewer(editorPart);
        if (viewer == null) {
            LOG.warn("editor part is not a ITextViewer: "
                + editorPart.getTitle());
            return;
        }

        final IEditorInput input = editorPart.getEditorInput();

        final IFile file = ResourceUtil.getFile(input);

        if (file == null) {
            LOG.warn("editor part does not use a file storage");
            return;
        }

        /*
         * Connecting causes Conversion of Delimiters which trigger Selection
         * and Save Activities, so connect before adding listeners
         */
        editorManager.connect(file);

        final EditorListener listener = new EditorListener(editorManager);
        listener.bind(editorPart);

        /*
         * OMG ... either pull this call out of this class or access the
         * ediorManager variables in a better manner
         */
        editorAPI.setEditable(editorPart, editorManager.hasWriteAccess
            && !editorManager.isLocked);

        final IDocumentProvider documentProvider = EditorManager
            .getDocumentProvider(input);

        // TODO Not registering is very helpful to find errors related to file
        // transfer problems
        editorManager.dirtyStateListener.register(documentProvider, input);

        final IDocument document = EditorManager.getDocument(editorPart);

        document.addDocumentListener(editorManager.documentListener);

        Set<IEditorPart> parts = editorParts.get(path);

        if (parts == null) {
            parts = new HashSet<IEditorPart>();
            editorParts.put(path, parts);
        }

        editorInputMap.put(editorPart, input);
        editorListeners.put(editorPart, listener);
        parts.add(editorPart);
    }

    public SPath getCurrentPath(final IEditorPart editorPart) {

        final IEditorInput input = editorInputMap.get(editorPart);

        if (input == null) {
            LOG.warn("editor part was never added to the pool: "
                + editorPart.getTitle());
            return null;
        }

        for (final Entry<SPath, Set<IEditorPart>> entry : editorParts
            .entrySet()) {

            if (entry.getValue().contains(editorPart))
                return entry.getKey();
        }

        return null;
    }

    /**
     * Removes an {@link IEditorPart} from the pool.
     * <p>
     * This Method also disconnects the editorPart from its data source
     * (identified by associated {@link IFile}) and removes registered
     * listeners:
     * <ul>
     * <li>{@link IElementStateListener} from {@link IDocumentProvider}</li>
     * <li>{@link IDocumentListener} from {@link IDocument}</li>
     * <li>{@link EditorListener} from {@link IEditorPart}</li>
     * </ul>
     * 
     * This method also makes the Editor editable.
     * 
     * @param editorPart
     *            editorPart to be removed
     * 
     * @return {@link SPath} of the Editor that was removed from the Pool, or
     *         <code>null</code> on error.
     */
    public SPath remove(final IEditorPart editorPart) {

        LOG.trace("EditorPool.remove " + editorPart + " invoked");

        final SPath path = editorAPI.getEditorPath(editorPart);

        if (path == null) {
            LOG.warn("could not find path/resource for editor: "
                + editorPart.getTitle());
            return null;
        }

        if (!isManaged(path, editorPart)) {
            LOG.error("editor part was never added to the pool: "
                + editorPart.getTitle());
            return null;
        }

        IEditorInput input = editorInputMap.remove(editorPart);

        assert input != null;

        final IFile file = ResourceUtil.getFile(input);

        assert file != null;

        // Unregister and unhook
        editorAPI.setEditable(editorPart, true);

        editorListeners.remove(editorPart).unbind();

        final IDocumentProvider documentProvider = EditorManager
            .getDocumentProvider(input);

        editorManager.dirtyStateListener.unregister(documentProvider, input);

        final IDocument document = documentProvider.getDocument(input);

        if (document == null) {
            LOG.warn("could not disconnect from document: " + path);
        } else {
            document.removeDocumentListener(editorManager.documentListener);
        }

        editorManager.disconnect(file);

        editorParts.get(path).remove(editorPart);

        return path;
    }

    /**
     * Returns all IEditorParts which have been added to this pool which display
     * a file using the given path.
     * 
     * @param path
     *            {@link IPath} of the Editor
     * 
     * @return set of relating IEditorPart
     * 
     */
    public Set<IEditorPart> getEditors(final SPath path) {

        final HashSet<IEditorPart> result = new HashSet<IEditorPart>();

        if (editorParts.containsKey(path))
            result.addAll(editorParts.get(path));

        return result;
    }

    /**
     * Returns all IEditorParts actually managed by this pool.
     * 
     * @return set of all {@link IEditorPart} from the {@link EditorPool}.
     * 
     */
    public Set<IEditorPart> getAllEditors() {

        final Set<IEditorPart> result = new HashSet<IEditorPart>();

        for (final Set<IEditorPart> parts : editorParts.values())
            result.addAll(parts);

        return result;
    }

    /**
     * Removes all {@link IEditorPart} from the EditorPool.
     */
    public void removeAllEditors() {

        LOG.trace("EditorPool.removeAllEditors invoked");

        for (final IEditorPart part : new HashSet<IEditorPart>(getAllEditors()))
            remove(part);

        assert getAllEditors().size() == 0;
    }

    /**
     * Changes the editable state of all editors currently managed by this pool.
     * 
     * @see IEditorAPI#setEditable(IEditorPart, boolean)
     */
    public void setEditable(final boolean editable) {

        LOG.trace("EditorPool.setEditable invoked");

        for (final IEditorPart editorPart : getAllEditors())
            editorAPI.setEditable(editorPart, editable);
    }

    /**
     * Returns if the given <code>IEditorPart</code> is managed by this pool.
     */
    public boolean isManaged(final IEditorPart editor) {
        return editorInputMap.containsKey(editor);
    }

    private boolean isManaged(final SPath path, final IEditorPart editor) {
        final Set<IEditorPart> editorsForPath = editorParts.get(path);

        if (editorsForPath == null)
            return false;

        return editorsForPath.contains(editor);
    }
}
