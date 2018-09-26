package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;

import org.apache.log4j.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class for handling activities on local editors and transforming them to calls to
 * {@link EditorManager} for generating activities .
 */
public class LocalEditorHandler {

    private final static Logger LOG = Logger
        .getLogger(LocalEditorHandler.class);

    private final ProjectAPI projectAPI;
    private final VirtualFileConverter virtualFileConverter;

    /**
     * This is just a reference to {@link EditorManager}'s editorPool and not a
     * separate pool.
     */
    private EditorPool editorPool;

    private EditorManager manager;

    public LocalEditorHandler(ProjectAPI projectAPI,
        VirtualFileConverter virtualFileConverter) {

        this.projectAPI = projectAPI;
        this.virtualFileConverter = virtualFileConverter;
    }

    /**
     * Initializes all fields that require an EditorManager. It has to be called
     * after the constructor and before the object is used, otherwise it will not
     * work.
     * <p/>
     * The reason for this late initialization is that this way the LocalEditorHandler
     * can be instantiated by the PicoContainer, otherwise there would be a cyclic
     * dependency.
     *
     * @param editorManager - an EditorManager
     */
    public void initialize(EditorManager editorManager) {
        this.editorPool = editorManager.getEditorPool();
        this.manager = editorManager;
        projectAPI
            .addFileEditorManagerListener(editorManager.getFileListener());
    }

    /**
     * Opens an editor for the passed virtualFile, adds it to the pool of
     * currently open editors and calls
     * {@link EditorManager#startEditor(Editor)} with it.
     * <p>
     * <b>Note:</b> This only works for shared resources.
     * </p>
     *
     * @param virtualFile path of the file to open
     * @param activate    activate editor after opening
     * @return the opened <code>Editor</code> or <code>null</code> if the given
     * file does not belong to a shared module
     */
    @Nullable
    public Editor openEditor(
        @NotNull
            VirtualFile virtualFile, boolean activate) {

        if (!manager.hasSession()) {
            return null;
        }

        SPath path = virtualFileConverter.convertToPath(virtualFile);

        if (path == null) {
            LOG.debug("Ignored open editor request for file " + virtualFile +
                " as it does not belong to a shared module");

            return null;
        }

        return openEditor(virtualFile,path,activate);
    }

    /**
     * Opens an editor for the passed virtualFile, adds it to the pool of
     * currently open editors and calls
     * {@link EditorManager#startEditor(Editor)} with it.
     * <p>
     * <b>Note:</b> This only works for shared resources that belong to the
     * given module.
     * </p>
     *
     * @param virtualFile path of the file to open
     * @param project     module the file belongs to
     * @param activate    activate editor after opening
     * @return the opened <code>Editor</code> or <code>null</code> if the given
     * file does not belong to a shared module
     */
    @Nullable
    public Editor openEditor(
        @NotNull
            VirtualFile virtualFile,
        @NotNull
            IProject project, boolean activate) {

        IResource resource = virtualFileConverter
            .getResource(virtualFile, project);

        if (resource == null) {
            LOG.debug("Could not open Editor for file " + virtualFile +
                " as it does not belong to the given module " + project);

            return null;
        }

        return openEditor(virtualFile, new SPath(resource), activate);
    }

    /**
     * Opens an editor for the passed virtualFile, adds it to the pool of
     * currently open editors and calls
     * {@link EditorManager#startEditor(Editor)} with it.
     * <p>
     * <b>Note:</b> This only works for shared resources.
     * </p>
     * <p>
     * <b>Note:</b> This method expects the VirtualFile and the SPath to point
     * to the same resource.
     * </p>
     *
     * @param virtualFile path of the file to open
     * @param path        saros resource representation of the file
     * @param activate    activate editor after opening
     * @return the opened <code>Editor</code> or <code>null</code> if the given
     * file does not exist or does not belong to a shared module
     */
    @Nullable
    private Editor openEditor(@NotNull VirtualFile virtualFile,
        @NotNull SPath path, boolean activate){

        if(!virtualFile.exists()){
            LOG.debug("Could not open Editor for file " + virtualFile +
                " as it does not exist");

            return null;

        }else if (!manager.getSession().isShared(path.getResource())) {
            LOG.debug("Ignored open editor request for file " + virtualFile +
                " as it is not shared");

            return null;
        }

        Editor editor = projectAPI.openEditor(virtualFile, activate);

        editorPool.add(path, editor);
        manager.startEditor(editor);

        LOG.debug("Opened Editor " + editor + " for file " + virtualFile);

        return editor;
    }

    /**
     * Removes a file from the editorPool and calls
     * {@link EditorManager#generateEditorClosed(SPath)}
     *
     * @param virtualFile
     */
    public void closeEditor(@NotNull VirtualFile virtualFile) {
        SPath path = virtualFileConverter.convertToPath(virtualFile);
        if (path != null) {
            editorPool.removeEditor(path);
            manager.generateEditorClosed(path);
        }
    }

    /**
     * Removes the resource belonging to the given path from the editor pool
     *
     * @param path path
     */
    public void removeEditor(@NotNull SPath path){
        editorPool.removeEditor(path);
    }

    /**
     * Saves the document under path..
     *
     * @param path
     */
    public void saveFile(SPath path) {
        Document doc = editorPool.getDocument(path);
        if (doc != null) {
            projectAPI.saveDocument(doc);
        } else {
            LOG.warn("Document does not exist: " + path);
        }
    }

    /**
     * Calls {@link EditorManager#generateEditorActivated(SPath)}.
     *
     * @param file
     */
    public void activateEditor(@NotNull VirtualFile file) {
        SPath path = virtualFileConverter.convertToPath(file);
        if (path != null) {
            manager.generateEditorActivated(path);
        }
    }

    public void sendEditorActivitySaved(SPath path) {
        // FIXME: not sure how to do it intelliJ
    }

    /**
     * @param path
     * @return <code>true</code>, if the path is opened in an editor.
     */
    public boolean isOpenEditor(SPath path) {
        Document doc = editorPool.getDocument(path);
        if (doc == null) {
            return false;
        }

        return projectAPI.isOpen(doc);
    }
}
