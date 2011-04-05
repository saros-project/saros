package de.fu_berlin.inf.dpp.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.ui.IEditorPart;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.business.ViewportActivity;

/**
 * {@link ISharedEditorListener} which can dispatch to a changing set of
 * {@link ISharedEditorListener}s.
 */
public class SharedEditorListenerDispatch implements ISharedEditorListener {

    protected List<ISharedEditorListener> editorListeners = new ArrayList<ISharedEditorListener>();

    public void add(ISharedEditorListener editorListener) {
        if (!this.editorListeners.contains(editorListener)) {
            this.editorListeners.add(editorListener);
        }
    }

    public void remove(ISharedEditorListener editorListener) {
        this.editorListeners.remove(editorListener);
    }

    public void activeEditorChanged(User user, SPath path) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.activeEditorChanged(user, path);
        }
    }

    public void editorRemoved(User user, SPath path) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.editorRemoved(user, path);
        }
    }

    public void userWithWriteAccessEditorSaved(SPath path, boolean replicated) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.userWithWriteAccessEditorSaved(path, replicated);
        }
    }

    public void followModeChanged(User user, boolean isFollowed) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.followModeChanged(user, isFollowed);
        }
    }

    public void textEditRecieved(User user, SPath editor, String text,
        String replacedText, int offset) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.textEditRecieved(user, editor, text, replacedText, offset);
        }
    }

    public void textSelectionMade(TextSelectionActivity selection) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.textSelectionMade(selection);
        }

    }

    public void viewportChanged(ViewportActivity viewport) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.viewportChanged(viewport);
        }
    }

    public void jumpedToUser(User jumpedTo) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.jumpedToUser(jumpedTo);
        }
    }

    public void viewportGenerated(IEditorPart part, ILineRange viewport,
        SPath path) {
        for (ISharedEditorListener listener : editorListeners) {
            listener.viewportGenerated(part, viewport, path);
        }
    }

    public void colorChanged() {
        for (ISharedEditorListener listener : editorListeners) {
            listener.colorChanged();
        }
    }
}
