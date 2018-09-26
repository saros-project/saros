package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.ITextOperation;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJProjectImplV2;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class applies the logic for activities that were received from remote.
 */
public class LocalEditorManipulator {

    private static final Logger LOG = LogManager
        .getLogger(LocalEditorManipulator.class);

    private final ProjectAPI projectAPI;
    private final EditorAPI editorAPI;

    /**
     * This is just a reference to {@link EditorManager}'s editorPool and not a
     * separate pool.
     */
    private EditorPool editorPool;

    private EditorManager manager;

    public LocalEditorManipulator(ProjectAPI projectAPI, EditorAPI editorAPI) {
        this.projectAPI = projectAPI;
        this.editorAPI = editorAPI;
    }

    /**
     * Initializes all fields that require an EditorManager.
     */
    public void initialize(EditorManager editorManager) {
        editorPool = editorManager.getEditorPool();
        manager = editorManager;
    }

    /**
     * Opens an editor for the passed virtualFile, adds it to the pool of
     * currently open editors and calls
     * {@link EditorManager#startEditor(Editor)} with it.
     * <p>
     * <b>Note:</b> This does only work for shared resources.
     *
     * @param path path of the file to open
     * @param activate activate editor after opening
     * @return the editor for the given path,
     * or <code>null</code> if the file does not exist or is not shared
     */
    public Editor openEditor(SPath path, boolean activate) {
        if (!manager.getSession().isShared(path.getResource())) {
            LOG.warn("Ignored open editor request for path " + path +
                " as it is not shared");

            return null;
        }

        IntelliJProjectImplV2 intelliJProject = (IntelliJProjectImplV2)
            path.getProject().getAdapter(IntelliJProjectImplV2.class);

        VirtualFile virtualFile = intelliJProject
            .findVirtualFile(path.getProjectRelativePath());

        if (virtualFile == null || !virtualFile.exists()) {
            LOG.warn("Could not open Editor for path " + path + " as a " +
                "matching VirtualFile does not exist or could not be found");

            return null;
        }

        //todo: in case it is already open, need to activate only, not open
        Editor editor = projectAPI.openEditor(virtualFile, activate);

        manager.startEditor(editor);
        editorPool.add(path, editor);

        LOG.debug("Opened Editor " + editor + " for file " + virtualFile);

        return editor;
    }

    /**
     * Closes the editor under path.
     *
     * @param path
     */
    public void closeEditor(SPath path) {
        editorPool.removeEditor(path);

        LOG.debug("Removed editor for path " + path + " from EditorPool");

        IntelliJProjectImplV2 intelliJProject = (IntelliJProjectImplV2)
            path.getProject().getAdapter(IntelliJProjectImplV2.class);

        VirtualFile virtualFile = intelliJProject
            .findVirtualFile(path.getProjectRelativePath());

        if (virtualFile == null || !virtualFile.exists()) {
            LOG.warn("Could not close Editor for path " + path + " as a " +
                "matching VirtualFile does not exist or could not be found");

            return;
        }

        if (projectAPI.isOpen(virtualFile)) {
            projectAPI.closeEditor(virtualFile);
        }

        LOG.debug("Closed editor for file " + virtualFile);
    }

    /**
     * Replaces the content of the document at the given path. The text is only
     * replaced, if the editor is writable.
     *
     * @param path path of the editor
     * @param text text to set the document's content to
     * @return Returns <code>true</code> if replacement was successful,
     * <code>false</code> if the path was <code>null></code>, if the path points
     * to a non-existing document or the document was not writable.
     */
    public boolean replaceText(SPath path, String text) {
        Document doc = editorPool.getDocument(path);
        if (doc == null) {
            return false;
        }
        if (!doc.isWritable()) {
            LOG.error("File to replace text in is not writeable: " + path);
            return false;
        }

        editorAPI.setText(doc, text);
        return true;
    }

    /**
     * Applies the text operations on the path and marks them in color.
     *
     * @param path
     * @param operations
     */
    public void applyTextOperations(SPath path, Operation operations) {
        Document doc = editorPool.getDocument(path);

        /*
         * If the document was not opened in an editor yet, it is not in the
         * editorPool so we have to create it temporarily here.
         */
        if (doc == null) {
            IntelliJProjectImplV2 module = (IntelliJProjectImplV2)
                path.getProject().getAdapter(IntelliJProjectImplV2.class);

            VirtualFile virtualFile = module
                .findVirtualFile(path.getProjectRelativePath());

            if (virtualFile == null || !virtualFile.exists()) {
                LOG.warn("Could not apply TextOperations " + operations
                    + " as the VirtualFile for path " + path
                    + " does not exist or could not be found");

                return;
            }

            doc = projectAPI.getDocument(virtualFile);

            if (doc == null) {
                LOG.warn("Could not apply TextOperations " + operations
                    + " as the Document for VirtualFile " + virtualFile
                    + " could not be found");

                return;
            }
        }

         /*
         * Disable documentListener temporarily to avoid being notified of the
         * change
         */
        manager.disableDocumentListener();
        for (ITextOperation op : operations.getTextOperations()) {
            if (op instanceof DeleteOperation) {
                editorAPI.deleteText(doc, op.getPosition(),
                    op.getPosition() + op.getTextLength());
            } else {
                boolean writePermission = doc.isWritable();
                if (!writePermission) {
                    doc.setReadOnly(false);
                }
                editorAPI.insertText(doc, op.getPosition(), op.getText());
                if (!writePermission) {
                    doc.setReadOnly(true);
                }
            }
        }

        manager.enableDocumentListener();
    }

    /**
     * Sets the viewport of the editor for path to the specified range.
     *
     * @param path
     * @param lineStart
     * @param lineEnd
     */
    public void setViewPort(final SPath path, final int lineStart,
        final int lineEnd) {
        Editor editor = editorPool.getEditor(path);
        if (editor != null) {
            editorAPI.setViewPort(editor, lineStart, lineEnd);
        }
    }

    /**
     * Adjusts viewport. Focus is set on the center of the range, but priority
     * is given to selected lines.
     *
     * @param editor    Editor of the open Editor
     * @param range     viewport of the followed user. Must not be <code>null</code>.
     * @param selection text selection of the followed user. Must not be <code>null</code>.
     */
    public void adjustViewport(Editor editor, LineRange range,
        TextSelection selection) {
        if (editor == null || selection == null || range == null) {
            return;
        }

        editorAPI.setSelection(editor, selection.getOffset(),
            selection.getOffset() + selection.getLength(), null);
        editorAPI.setViewPort(editor, range.getStartLine(),
            range.getStartLine() + range.getNumberOfLines());

        //todo: implement actual viewport adjustment logic
    }
}
