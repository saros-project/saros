package de.fu_berlin.inf.dpp.ui.model.rosterSession;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.ui.model.ITreeElement;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterComparator;

/**
 * Comparator for {@link ITreeElement} for {@link RosterSessionInput}.
 * 
 * @author bkahlert
 */
public class RosterSessionComparator extends ViewerComparator {
    protected RosterComparator rosterComparator;

    public RosterSessionComparator() {
        this.rosterComparator = new RosterComparator();
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        /*
         * Compares top level elements
         */
        if (e1 instanceof RosterHeaderElement
            && e2 instanceof SessionHeaderElement)
            return 1;

        if (e1 instanceof SessionHeaderElement
            && e2 instanceof RosterHeaderElement)
            return -1;

        /*
         * Compares session part
         */
        if (e1 instanceof UserElement && e2 instanceof UserElement) {
            User user1 = (User) Platform.getAdapterManager().getAdapter(e1,
                User.class);
            User user2 = (User) Platform.getAdapterManager().getAdapter(e2,
                User.class);

            if (user1.equals(user2))
                return 0;
            if (user1.isHost())
                return -1;
            if (user2.isHost())
                return +1;
            return user1.getJID().toString()
                .compareToIgnoreCase(user2.getJID().toString());
        }

        /*
         * Compares Roster part
         */
        return rosterComparator.compare(viewer, e1, e2);
    }
}
