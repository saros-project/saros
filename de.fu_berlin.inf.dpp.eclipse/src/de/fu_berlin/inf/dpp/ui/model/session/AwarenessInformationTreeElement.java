package de.fu_berlin.inf.dpp.ui.model.session;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.remote.EditorState;
import de.fu_berlin.inf.dpp.editor.remote.UserEditorStateManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;

/**
 * This is a tree element that can be displayed as a child element of the user
 * entry in the Saros session view {@link Viewer Viewers} showing information
 * about the state of that user / his past actions or whatever awareness
 * information migh help to be more productive in a session.
 * 
 * @author Alexander Waldmann (contact@net-corps.de)
 */
public class AwarenessInformationTreeElement extends TreeElement {

    protected final EditorManager editorManager;

    protected final AwarenessInformationCollector collector;

    protected final User user;

    public AwarenessInformationTreeElement(final User user,
        final EditorManager editorManager,
        final AwarenessInformationCollector collector) {

        this.user = user;
        this.editorManager = editorManager;
        this.collector = collector;
    }

    /**
     * Combines the available awareness information to a styled string
     * 
     * TODO: (optional) create a new renderer that presents the information in a
     * more user friendly way, not just text
     */
    @Override
    public StyledString getStyledText() {
        StyledString styledString = new StyledString();

        boolean first = true;
        for (String detail : getAwarenessDetails()) {
            styledString.append((first ? "" : " - ") + detail);
            first = false;
        }
        return styledString;
    }

    /**
     * Display an appropriate image for this element depending on the awareness
     * information that is currently shown.
     * 
     * At the moment this is only a "file object" icon in case the user has a
     * file opened.
     * 
     * TODO: set icons properly depending on the state of the user/his actions.
     */
    @Override
    public Image getImage() {

        return editorManager.getRemoteOpenEditors(user).size() > 0 ? PlatformUI
            .getWorkbench().getSharedImages()
            .getImage(ISharedImages.IMG_OBJ_FILE) : org.eclipse.jdt.ui.JavaUI
            .getSharedImages().getImage(
                org.eclipse.jdt.ui.ISharedImages.IMG_FIELD_DEFAULT);
    }

    public Object getUser() {
        return this.user;
    }

    /**
     * Retrieve information about the progress of the invitation (if there is
     * any) and awareness information that benefits the users (like showing
     * which file the user is currently viewing, who he is following etc.)
     */
    private List<String> getAwarenessDetails() {
        List<String> details = new ArrayList<String>();

        final UserEditorStateManager mgr = editorManager
            .getUserEditorStateManager();

        if (mgr == null)
            return details;

        final EditorState activeEditor = mgr.getState(user)
            .getActiveEditorState();
        /*
         * The other user has a non-shared editor open, i.e. the remote editor
         * shows a file which is not part of the session.
         */
        if (activeEditor == null) {
            details.add("non-shared file open");
            return details;
        }

        SPath activeFile = activeEditor.getPath();
        if (activeFile != null) {
            /*
             * path.getProjectRelativePath() could be too long, sometimes the
             * name would be enough...
             * 
             * TODO: make this configurable?
             */
            details.add(activeFile.getProject().getName() + ": "
                + activeFile.getFile().getProjectRelativePath().toString());
        }

        return details;
    }
}
