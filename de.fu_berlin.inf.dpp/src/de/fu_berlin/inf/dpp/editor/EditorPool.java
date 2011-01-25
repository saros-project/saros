package de.fu_berlin.inf.dpp.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.editor.internal.EditorListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * The EditorPool manages the IEditorParts of the local user. Currently only
 * those parts are supported by Saros (and thus managed in the EditorPool) which
 * can be traced to an {@link IFile} and an {@link ITextViewer}.
 */
class EditorPool {

    private static Logger log = Logger.getLogger(EditorPool.class.getName());

    protected EditorManager editorManager;

    EditorPool(EditorManager editorManager) {
        this.editorManager = editorManager;
    }

    /**
     * The editorParts-map will return all EditorParts associated with a given
     * IPath. This can be potentially many because a IFile (which can be
     * identified using a IPath) can be opened in multiple editors.
     */
    protected Map<SPath, HashSet<IEditorPart>> editorParts = new HashMap<SPath, HashSet<IEditorPart>>();

    /**
     * The editorInputMap contains all IEditorParts which are managed by the
     * EditorPool and stores the associated IEditorInput (the EditorInput could
     * also actually be retrieved directly from the IEditorPart).
     */
    protected Map<IEditorPart, IEditorInput> editorInputMap = new HashMap<IEditorPart, IEditorInput>();

    /**
     * Tries to add an {@link IEditorPart} to the {@link EditorPool}. This
     * method also connects the editorPart with its data source (identified by
     * associated {@link IFile}), makes it editable for user with
     * {@link User.Permission#WRITE_ACCESS}, and registers listeners:
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
     * This method will print a warning and return without any effect if the
     * given IEditorPart does not a.) represent an IFile, b.) which can be
     * referred to using an IPath and c.) the IEditorPart can be mapped to an
     * ITextViewer.
     * 
     * The method is robust against adding the same IEditorPart twice.
     */
    public void add(IEditorPart editorPart) {

        SPath path = editorManager.editorAPI.getEditorPath(editorPart);

        if (path == null) {
            log.warn("Could not find path/resource for editor "
                + editorPart.getTitle());
            return;
        }

        log.trace("EditorPool.add (" + path.toString() + ") invoked");

        if (getEditors(path).contains(editorPart)) {
            log.error("EditorPart was added twice to the EditorPool: "
                + editorPart.getTitle(), new StackTrace());
            return;
        }

        ITextViewer viewer = EditorAPI.getViewer(editorPart);
        if (viewer == null) {
            log.warn("This editor is not a ITextViewer: "
                + editorPart.getTitle());
            return;
        }

        IEditorInput input = editorPart.getEditorInput();

        IFile file = ResourceUtil.getFile(input);
        if (file == null) {
            log.warn("This editor does not use IFiles as input");
            return;
        }

        /*
         * Connecting causes Conversion of Delimiters which trigger Selection
         * and Save Activities, so connect before adding listeners
         */
        this.editorManager.connect(file);

        this.editorManager.editorAPI.addSharedEditorListener(
            this.editorManager, editorPart);
        this.editorManager.editorAPI.setEditable(editorPart,
            this.editorManager.hasWriteAccess);

        IDocumentProvider documentProvider = EditorManager
            .getDocumentProvider(input);

        // TODO Not registering is very helpful to find errors related to file
        // transfer problems
        this.editorManager.dirtyStateListener.register(documentProvider, input);

        IDocument document = EditorManager.getDocument(editorPart);

        document.addDocumentListener(this.editorManager.documentListener);

        getEditors(path).add(editorPart);
        editorInputMap.put(editorPart, input);

        this.editorManager.lastEditTimes.put(path, System.currentTimeMillis());
        this.editorManager.lastRemoteEditTimes.put(path,
            System.currentTimeMillis());
    }

    public SPath getCurrentPath(IEditorPart editorPart,
        ISarosSession sarosSession) {

        IEditorInput input = editorInputMap.get(editorPart);
        if (input == null) {
            log.warn("EditorPart was never added to the EditorPool: "
                + editorPart.getTitle());
            return null;
        }

        IFile file = ResourceUtil.getFile(input);
        if (file == null) {
            log.warn("Could not find file for editor input "
                + editorPart.getTitle());
            return null;
        }

        if (!sarosSession.isShared(file.getProject())) {
            log.warn("File is from incorrect project: " + file.getProject()
                + " should be " + sarosSession + ": " + file, new StackTrace());
        }

        IPath path = file.getProjectRelativePath();
        if (path == null) {
            log.warn("Could not find path for editor " + editorPart.getTitle());
        }
        return new SPath(file);
    }

    /**
     * Tries to remove an {@link IEditorPart} from {@link EditorPool}. This
     * Method also disconnects the editorPart from its data source (identified
     * by associated {@link IFile}) and removes registered listeners:
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
     * @return {@link IPath} of the Editor that was removed from the Pool, or
     *         <code>null</code> on error.
     */
    public IPath remove(IEditorPart editorPart, ISarosSession sarosSession) {

        log.trace("EditorPool.remove " + editorPart + "invoked");

        IEditorInput input = editorInputMap.remove(editorPart);
        if (input == null) {
            log.warn("EditorPart was never added to the EditorPool: "
                + editorPart.getTitle());
            return null;
        }

        IFile file = ResourceUtil.getFile(input);
        if (file == null) {
            log.warn("Could not find file for editor input "
                + editorPart.getTitle());
            return null;
        }

        if (!sarosSession.isShared(file.getProject())) {
            log.warn("File is from incorrect project: " + file.getProject()
                + " should be " + sarosSession + ": " + file, new StackTrace());
        }

        IPath path = file.getProjectRelativePath();
        if (path == null) {
            log.warn("Could not find path for editor " + editorPart.getTitle());
            return null;
        }

        // TODO Remove should remove empty HashSets
        if (!getEditors(new SPath(file)).remove(editorPart)) {
            log.error("EditorPart was never added to the EditorPool: "
                + editorPart.getTitle());
            return null;
        }

        // Unregister and unhook
        this.editorManager.editorAPI.setEditable(editorPart, true);
        this.editorManager.editorAPI.removeSharedEditorListener(
            this.editorManager, editorPart);

        IDocumentProvider documentProvider = EditorManager
            .getDocumentProvider(input);
        this.editorManager.dirtyStateListener.unregister(documentProvider,
            input);

        IDocument document = documentProvider.getDocument(input);
        if (document == null) {
            log.warn("Could not disconnect from document: " + path);
        } else {
            document
                .removeDocumentListener(this.editorManager.documentListener);
        }

        this.editorManager.disconnect(file);

        return path;
    }

    /**
     * Returns all IEditorParts which have been added to this IEditorPool which
     * display a file using the given path.
     * 
     * @param path
     *            {@link IPath} of the Editor
     * 
     * @return set of relating IEditorPart
     * 
     */
    public Set<IEditorPart> getEditors(SPath path) {

        log.trace(".getEditors(" + path.toString() + ") invoked");
        if (!editorParts.containsKey(path)) {
            HashSet<IEditorPart> result = new HashSet<IEditorPart>();
            editorParts.put(path, result);
            return result;
        }
        return editorParts.get(path);
    }

    /**
     * Returns all IEditorParts actually managed in the EditorPool.
     * 
     * @return set of all {@link IEditorPart} from the {@link EditorPool}.
     * 
     */
    public Set<IEditorPart> getAllEditors() {

        log.trace("EditorPool.getAllEditors invoked");

        Set<IEditorPart> result = new HashSet<IEditorPart>();

        for (Set<IEditorPart> parts : this.editorParts.values()) {
            result.addAll(parts);
        }
        return result;
    }

    /**
     * Removes all {@link IEditorPart} from the EditorPool.
     */
    public void removeAllEditors(ISarosSession sarosSession) {

        log.trace("EditorPool.removeAllEditors invoked");

        for (IEditorPart part : new HashSet<IEditorPart>(getAllEditors())) {
            remove(part, sarosSession);
        }

        assert getAllEditors().size() == 0;
    }

    /**
     * Will set all IEditorParts in the EditorPool to be editable by the local
     * user if has {@link User.Permission#WRITE_ACCESS}. The editors will be
     * locked otherwise.
     */
    public void setWriteAccessEnabled(boolean hasWriteAccess) {

        log.trace("EditorPool.setEditable");

        for (IEditorPart editorPart : getAllEditors()) {
            this.editorManager.editorAPI
                .setEditable(editorPart, hasWriteAccess);
        }
    }

    /**
     * Returns true iff the given IEditorPart is managed by the
     * {@link EditorPool}. See EditorPool for a description of which
     * IEditorParts are managed.
     */
    public boolean isManaged(IEditorPart editor) {
        return editorInputMap.containsKey(editor);
    }
}