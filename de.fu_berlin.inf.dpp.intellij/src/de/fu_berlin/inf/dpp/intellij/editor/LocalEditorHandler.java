package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJProjectImplV2;

import de.fu_berlin.inf.dpp.session.IReferencePointManager;
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
    /**
     * This is just a reference to {@link EditorManager}'s editorPool and not a
     * separate pool.
     */
    private EditorPool editorPool;

    private EditorManager manager;

    public LocalEditorHandler(ProjectAPI projectAPI) {
        this.projectAPI = projectAPI;
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
     *
     * @param virtualFile path of the file to open
     * @param activate activate editor after opening
     */
    public void openEditor(VirtualFile virtualFile, boolean activate) {
        SPath path = toPath(virtualFile);

        if (path == null) {
            LOG.debug("Ignored open editor request for file " + virtualFile +
                " as it does not belong to a shared module");

            return;

        }

        openEditor(virtualFile,path,activate);
    }

    /**
     * Opens an editor for the passed virtualFile, adds it to the pool of
     * currently open editors and calls
     * {@link EditorManager#startEditor(Editor)} with it.
     * <p>
     * <b>Note:</b> This only works for shared resources that belong to the
     * given module.
     *
     * @param virtualFile path of the file to open
     * @param project module the file belongs to
     * @param activate activate editor after opening
     */
    public void openEditor(VirtualFile virtualFile, IProject project,
        boolean activate){

        IResource resource = getResource(virtualFile, project);

        if (resource == null) {
            LOG.debug("Could not open Editor for file " + virtualFile +
                " as it does not belong to the given module " + project);

            return;
        }

        openEditor(virtualFile, new SPath(resource), activate);
    }



    /**
     * Opens an editor for the passed virtualFile, adds it to the pool of
     * currently open editors and calls
     * {@link EditorManager#startEditor(Editor)} with it.
     * <p>
     * <b>Note:</b> This only works for shared resources.
     * <p>
     * <b>Note:</b> This method expects the VirtualFile and the SPath to point
     * to the same resource.
     *
     * @param virtualFile path of the file to open
     * @param path saros resource representation of the file
     * @param activate activate editor after opening
     */
    private void openEditor(@NotNull VirtualFile virtualFile,
        @NotNull SPath path, boolean activate){

        if(!virtualFile.exists()){
            LOG.debug("Could not open Editor for file " + virtualFile +
                " as it does not exist");

            return;

        }else if (!manager.getSession().isShared(path.getResource())) {
            LOG.debug("Ignored open editor request for file " + virtualFile +
                " as it is not shared");

            return;
        }

        Editor editor = projectAPI.openEditor(virtualFile, activate);

        editorPool.add(path, editor);
        manager.startEditor(editor);

        LOG.debug("Opened Editor " + editor + " for file " + virtualFile);
    }

    /**
     * Removes a file from the editorPool and calls
     * {@link EditorManager#generateEditorClosed(SPath)}
     *
     * @param virtualFile
     */
    public void closeEditor(VirtualFile virtualFile) {
        SPath path = toPath(virtualFile);
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
        editorPool.removeAll(path);
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
    public void activateEditor(VirtualFile file) {
        SPath path = toPath(file);
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

    /**
     * Returns an <code>SPath</code> representing the passed file.
     *
     * @param virtualFile file to get the <code>SPath</code> for
     *
     * @return an <code>SPath</code> representing the passed file or
     *         <code>null</code> if the passed file is null or does not exist,
     *         there currently is no session, or the file does not belong to a
     *         shared module
     */
    @Nullable
    private SPath toPath(VirtualFile virtualFile) {
        if (virtualFile == null || !virtualFile.exists() || !manager
            .hasSession()) {
            return null;
        }

        IResource resource = null;

        IReferencePointManager referencePointManager = manager.getSession()
        .getComponent(IReferencePointManager.class);

        for (IProject project : referencePointManager.getProjects(
            manager.getSession().getReferencePoints())) {
            resource = getResource(virtualFile, project);

            if(resource != null){
                break;
            }
        }

        return resource == null ? null : new SPath(resource);
    }

    /**
     * Returns an <code>IResource</code> for the passed VirtualFile.
     *
     * @param virtualFile file to get the <code>IResource</code> for
     * @param project module the file belongs to
     * @return an <code>IResource</code> for the passed file or
     *         <code>null</code> it does not belong to the passed module.
     */
    @Nullable
    private static IResource getResource(@NotNull VirtualFile virtualFile,
        @NotNull IProject project) {

        IntelliJProjectImplV2 module = (IntelliJProjectImplV2) project
            .getAdapter(IntelliJProjectImplV2.class);

        return module.getResource(virtualFile);
    }
}
