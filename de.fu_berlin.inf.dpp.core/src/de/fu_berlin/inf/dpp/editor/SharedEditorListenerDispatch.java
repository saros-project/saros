package de.fu_berlin.inf.dpp.editor;

import java.util.concurrent.CopyOnWriteArrayList;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.session.User;

/**
 * {@link ISharedEditorListener} which can dispatch to a changing set of
 * {@link ISharedEditorListener}s.
 */
public class SharedEditorListenerDispatch implements ISharedEditorListener {

    private CopyOnWriteArrayList<ISharedEditorListener> editorListeners = new CopyOnWriteArrayList<ISharedEditorListener>();

    public void add(ISharedEditorListener editorListener) {
        editorListeners.addIfAbsent(editorListener);
    }

    public void remove(ISharedEditorListener editorListener) {
        editorListeners.remove(editorListener);
    }

    @Override
    public void activeEditorChanged(User user, SPath path) {
        for (ISharedEditorListener listener : editorListeners)
            listener.activeEditorChanged(user, path);
    }

    @Override
    public void editorRemoved(User user, SPath path) {
        for (ISharedEditorListener listener : editorListeners)
            listener.editorRemoved(user, path);
    }

    @Override
    public void userWithWriteAccessEditorSaved(SPath path, boolean replicated) {
        for (ISharedEditorListener listener : editorListeners)
            listener.userWithWriteAccessEditorSaved(path, replicated);
    }

    @Override
    public void followModeChanged(User user, boolean isFollowed) {
        for (ISharedEditorListener listener : editorListeners)
            listener.followModeChanged(user, isFollowed);
    }

    @Override
    public void textEditRecieved(User user, SPath editor, String text,
        String replacedText, int offset) {

        for (ISharedEditorListener listener : editorListeners)
            listener.textEditRecieved(user, editor, text, replacedText, offset);
    }

    @Override
    public void textSelectionMade(TextSelectionActivity selection) {
        for (ISharedEditorListener listener : editorListeners)
            listener.textSelectionMade(selection);
    }

    @Override
    public void viewportChanged(ViewportActivity viewport) {
        for (ISharedEditorListener listener : editorListeners)
            listener.viewportChanged(viewport);
    }

    @Override
    public void jumpedToUser(User jumpedTo) {
        for (ISharedEditorListener listener : editorListeners)
            listener.jumpedToUser(jumpedTo);
    }

    @Override
    public void viewportGenerated(ViewportActivity viewport) {
        for (ISharedEditorListener listener : editorListeners)
            listener.viewportGenerated(viewport);
    }

    @Override
    public void colorChanged() {
        for (ISharedEditorListener listener : editorListeners)
            listener.colorChanged();
    }
}
