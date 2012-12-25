package de.fu_berlin.inf.dpp.editor;

import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.ui.IEditorPart;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.business.ViewportActivity;

/**
 * Empty abstract implementation of the ISharedEditorListener interface
 */
public abstract class AbstractSharedEditorListener implements
    ISharedEditorListener {

    @Override
    public void activeEditorChanged(User user, SPath path) {
        // does nothing
    }

    @Override
    public void editorRemoved(User user, SPath path) {
        // does nothing
    }

    @Override
    public void userWithWriteAccessEditorSaved(SPath path, boolean replicated) {
        // does nothing
    }

    @Override
    public void followModeChanged(User user, boolean isFollowed) {
        // does nothing
    }

    @Override
    public void textEditRecieved(User user, SPath editor, String text,
        String replacedText, int offset) {
        // does nothing
    }

    @Override
    public void textSelectionMade(TextSelectionActivity selection) {
        // does nothing
    }

    @Override
    public void viewportChanged(ViewportActivity viewport) {
        // does nothing
    }

    @Override
    public void jumpedToUser(User jumpedTo) {
        // does nothing
    }

    @Override
    public void viewportGenerated(IEditorPart part, ILineRange viewport,
        SPath path) {
        // does nothing

    }

    @Override
    public void colorChanged() {
        // does nothing
    }
}
