package de.fu_berlin.inf.dpp.ui.model.session;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import de.fu_berlin.inf.dpp.activities.IDEInteractionActivity.Element;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.ImageManager;

/**
 * This is a tree element that can be displayed as a child element of the user
 * entry in the Saros session view, showing information about the currently
 * opened dialog or focused view of that user.
 * */
public class UIAwarenessTreeElement extends AwarenessInformationTreeElement {

    public UIAwarenessTreeElement(final User user,
        final EditorManager editorManager,
        final AwarenessInformationCollector collector) {
        super(user, editorManager, collector);
    }

    @Override
    public StyledString getStyledText() {
        StyledString styledString = new StyledString();
        String uiTitle = collector.getOpenIDEElementTitle(user);
        Element uiType = collector.getOpenIDEElementType(user);
        if (uiTitle != null && uiType != null) {
            if (uiType == Element.DIALOG)
                styledString.append("Open dialog: " + uiTitle);
            else
                styledString.append("Active view: " + uiTitle);
        }

        return styledString;
    }

    /**
     * Display an appropriate image for this element depending on the awareness
     * information that is currently shown.
     * 
     * At the moment this is only a "file object" icon.
     */
    @Override
    public Image getImage() {
        return ImageManager.ELCL_DIALOG;
    }
}