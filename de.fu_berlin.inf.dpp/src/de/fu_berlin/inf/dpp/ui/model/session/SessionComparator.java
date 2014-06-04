package de.fu_berlin.inf.dpp.ui.model.session;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.model.HeaderElement;
import de.fu_berlin.inf.dpp.ui.model.ITreeElement;

/**
 * Comparator for {@link ITreeElement} for {@link SessionInput}.
 * 
 * @author bkahlert
 */
public class SessionComparator extends ViewerComparator {
    private final ViewerComparator contentComparator;

    public SessionComparator(final ViewerComparator contentComparator) {
        this.contentComparator = contentComparator;
    }

    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
        /*
         * Compares top level elements, place session on top
         */
        if (e1 instanceof HeaderElement
            && !(e1 instanceof SessionHeaderElement)
            && e2 instanceof SessionHeaderElement)
            return 1;

        if (e1 instanceof SessionHeaderElement && e2 instanceof HeaderElement
            && !(e2 instanceof SessionHeaderElement))
            return -1;

        /*
         * Compares session part
         */
        if (e1 instanceof UserElement && e2 instanceof UserElement) {
            final User user1 = (User) ((UserElement) e1).getUser();
            final User user2 = (User) ((UserElement) e2).getUser();

            final String userDisplayName1 = ((UserElement) e1).getStyledText()
                .toString();
            final String userDisplayName2 = ((UserElement) e1).getStyledText()
                .toString();

            if (user1.equals(user2))
                return 0;

            if (user1.isHost())
                return -1;

            if (user2.isHost())
                return +1;

            return userDisplayName1.compareToIgnoreCase(userDisplayName2);
        }

        /*
         * Compares content part
         */
        if (contentComparator != null)
            return contentComparator.compare(viewer, e1, e2);
        else
            return super.compare(viewer, e1, e2);
    }
}
