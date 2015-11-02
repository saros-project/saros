package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import org.apache.log4j.Logger;

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
     * Adds the opened file to the editorPool and calls
     * {@link EditorManager#startEditor(Editor)}
     * on the opened Editor.
     *
     * @param virtualFile
     */
    public void openEditor(VirtualFile virtualFile) {
        SPath path = toPath(virtualFile);
        if (path == null)
            return;

        editorPool.add(path, projectAPI.getActiveEditor());
        manager.startEditor(projectAPI.getActiveEditor());
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

    private SPath toPath(VirtualFile virtualFile) {
        if (virtualFile == null || !virtualFile.exists() || !manager
            .hasSession()) {
            return null;
        }

        IResource resource = null;
        String path = virtualFile.getPath();

        //TODO: Replace manager.getSession() call by call to Project API
        for (IProject project : manager.getSession().getProjects()) {
            resource = project.getFile(path);
            if (resource != null) {
                break;
            }
        }
        return resource == null ? null : new SPath(resource);
    }
}
