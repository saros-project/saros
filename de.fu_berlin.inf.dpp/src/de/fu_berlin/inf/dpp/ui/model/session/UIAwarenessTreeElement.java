package de.fu_berlin.inf.dpp.ui.model.session;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import de.fu_berlin.inf.dpp.activities.IDEInteractionActivity.Element;
import de.fu_berlin.inf.dpp.activities.TestRunActivity.State;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.ImageManager;

/**
 * This tree element is supposed to be used as a child element of the user entry
 * in the Saros session view. It displays current action awareness information
 * about the user, e.g. the opened dialog, the activated view, information about
 * test runs or performed refactorings.
 * */
public class UIAwarenessTreeElement extends AwarenessInformationTreeElement {

    /**
     * Creates a new {@link AwarenessInformationTreeElement} for the given
     * <code>user</code>.
     * 
     * @param user
     *            The user which is used to determine the appropriate action
     *            awareness information.
     */
    public UIAwarenessTreeElement(final User user,
        final EditorManager editorManager,
        final AwarenessInformationCollector collector) {
        super(user, editorManager, collector);
    }

    @Override
    public StyledString getStyledText() {
        StyledString styledString = new StyledString();

        if (collector.getCurrentTestRunName(user) != null) {

            // a test run has started or finished
            String name = collector.getCurrentTestRunName(user);
            State state = collector.getCurrentTestRunState(user);

            String startMessage = "Runs test";
            String endMessage = "Has run test";

            if (name != null && state != null) {
                switch (state) {
                case UNDEFINED:
                    styledString.append(startMessage + " '" + name + "'");
                    break;
                case OK:
                    styledString.append(endMessage + " '" + name
                        + "', result: SUCCESS");
                    break;
                case ERROR:
                    styledString.append(endMessage + " '" + name
                        + "', result: ERROR");
                    break;
                case FAILURE:
                    styledString.append(endMessage + " '" + name
                        + "', result: FAILURE");
                    break;
                default:
                    // 'IGNORED' which is (now) not interesting for us
                    break;
                }
            }
        } else if (collector.getCurrentRefactoringDescription(user) != null) {

            // a refactoring was performed
            String description = collector
                .getCurrentRefactoringDescription(user);

            if (description != null)
                styledString.append("Refactored: " + description);

        } else if (collector.getOpenIDEElementTitle(user) != null) {

            // a normal IDE interaction was made
            String uiTitle = collector.getOpenIDEElementTitle(user);
            Element uiType = collector.getOpenIDEElementType(user);

            if (uiTitle != null && uiType != null) {
                if (uiType == Element.DIALOG)
                    styledString.append("Open dialog: " + uiTitle);
                else
                    styledString.append("Active view: " + uiTitle);
            }
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