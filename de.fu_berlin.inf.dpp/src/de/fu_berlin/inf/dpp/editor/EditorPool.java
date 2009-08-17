package de.fu_berlin.inf.dpp.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.editor.internal.EditorListener;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * The EditorPool manages the IEditorParts of the local user. Currently only
 * those parts are supported by Saros (and thus managed in the EditorPool) which
 * can be traced to an {@link IFile} and an {@link ITextViewer}.
 */
class EditorPool {

    protected EditorManager editorManager;

    EditorPool(EditorManager editorManager) {
        this.editorManager = editorManager;
    }

    /**
     * The editorParts-map will return all EditorParts associated with a given
     * IPath. This can be potentielly many because a IFile (which can be
     * identified using a IPath) can be opened in multiple editors.
     */
    protected Map<IPath, HashSet<IEditorPart>> editorParts = new HashMap<IPath, HashSet<IEditorPart>>();

    /**
     * The editorInputMap contains all IEditorParts which are managed by the
     * EditorPool and stores the associated IEditorInput (the EditorInput could
     * also actually be retrieved directly from the IEditorPart).
     */
    protected Map<IEditorPart, IEditorInput> editorInputMap = new HashMap<IEditorPart, IEditorInput>();

    /**
     * Tries to add an {@link IEditorPart} to the {@link EditorPool}. This
     * method also connects the editorPart with its data source (identified by
     * associated {@link IFile}), makes it editable for driver, and registers
     * listeners:
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

        EditorManager.wpLog.trace("EditorPool.add invoked");

        IPath path = this.editorManager.editorAPI.getEditorPath(editorPart);
        if (path == null) {
            EditorManager.log.warn("Could not find path/resource for editor "
                + editorPart.getTitle());
            return;
        }
        if (getEditors(path).contains(editorPart)) {
            EditorManager.log.error(
                "EditorPart was added twice to the EditorPool: "
                    + editorPart.getTitle(), new StackTrace());
            return;
        }

        ITextViewer viewer = EditorAPI.getViewer(editorPart);
        if (viewer == null) {
            EditorManager.log.warn("This editor is not a ITextViewer: "
                + editorPart.getTitle());
            return;
        }

        IEditorInput input = editorPart.getEditorInput();

        IFile file = ResourceUtil.getFile(input);
        if (file == null) {
            EditorManager.log.warn("This editor does not use IFiles as input");
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
            this.editorManager.isDriver);

        IDocumentProvider documentProvider = EditorManager
            .getDocumentProvider(input);
        documentProvider
            .addElementStateListener(this.editorManager.dirtyStateListener);

        IDocument document = EditorManager.getDocument(editorPart);

        document.addDocumentListener(this.editorManager.documentListener);

        getEditors(path).add(editorPart);
        editorInputMap.put(editorPart, input);

        this.editorManager.lastEditTimes.put(path, System.currentTimeMillis());
        this.editorManager.lastRemoteEditTimes.put(path, System
            .currentTimeMillis());
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
    public IPath remove(IEditorPart editorPart, IProject project) {

        EditorManager.wpLog.trace("EditorPool.remove invoked");

        IEditorInput input = editorInputMap.remove(editorPart);
        if (input == null) {
            EditorManager.log
                .warn("EditorPart was never added to the EditorPool: "
                    + editorPart.getTitle());
            return null;
        }

        IFile file = ResourceUtil.getFile(input);
        if (file == null) {
            EditorManager.log.warn("Could not find file for editor input "
                + editorPart.getTitle());
            return null;
        }

        if (!ObjectUtils.equals(file.getProject(), project)) {
            EditorManager.log.warn("File is from incorrect project: "
                + file.getProject() + " should be " + project + ": " + file,
                new StackTrace());
        }

        IPath path = file.getProjectRelativePath();
        if (path == null) {
            EditorManager.log.warn("Could not find path for editor "
                + editorPart.getTitle());
            return null;
        }

        // TODO Remove should remove empty HashSets
        if (!getEditors(path).remove(editorPart)) {
            EditorManager.log
                .error("EditorPart was never added to the EditorPool: "
                    + editorPart.getTitle());
            return null;
        }

        // Unregister and unhook
        this.editorManager.editorAPI.setEditable(editorPart, true);
        this.editorManager.editorAPI.removeSharedEditorListener(
            this.editorManager, editorPart);

        IDocumentProvider documentProvider = EditorManager
            .getDocumentProvider(input);
        documentProvider
            .removeElementStateListener(this.editorManager.dirtyStateListener);

        IDocument document = documentProvider.getDocument(input);
        if (document == null) {
            EditorManager.log.warn("Could not disconnect from document: "
                + path);
        } else {
            document
                .removeDocumentListener(this.editorManager.documentListener);
        }

        this.editorManager.resetText(file);

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
    public Set<IEditorPart> getEditors(IPath path) {

        EditorManager.wpLog.trace("EditorPool.getEditors(" + path.toString()
            + ") invoked");
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

        EditorManager.wpLog.trace("EditorPool.getAllEditors invoked");

        Set<IEditorPart> result = new HashSet<IEditorPart>();

        for (Set<IEditorPart> parts : this.editorParts.values()) {
            result.addAll(parts);
        }
        return result;
    }

    /**
     * Removes all {@link IEditorPart} from the EditorPool.
     */
    public void removeAllEditors(IProject project) {

        EditorManager.wpLog.trace("EditorPool.removeAllEditors invoked");

        for (IEditorPart part : new HashSet<IEditorPart>(getAllEditors())) {
            remove(part, project);
        }

        assert getAllEditors().size() == 0;
    }

    /**
     * Will set all IEditorParts in the EditorPool to be editable by the local
     * user if isDriver == true. The editors will be locked if isDriver ==
     * false.
     */
    public void setDriverEnabled(boolean isDriver) {

        EditorManager.wpLog.trace("EditorPool.setDriverEnabled");

        for (IEditorPart editorPart : getAllEditors()) {
            this.editorManager.editorAPI.setEditable(editorPart, isDriver);
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