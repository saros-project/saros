package de.fu_berlin.inf.dpp.ui.model.roster;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.jivesoftware.smack.Roster;

import de.fu_berlin.inf.dpp.ui.model.ITreeElement;

/**
 * Comparator for {@link ITreeElement} describing {@link Roster} entities.
 * <p>
 * {@link RosterGroupElement}s and {@link RosterEntryElement}s are sorted
 * alphabetically.
 * <p>
 * First {@link RosterGroupElement}s and then {@link RosterEntryElement}s are
 * displayed.
 */
public class RosterComparator extends ViewerComparator {
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof RosterGroupElement
            && e2 instanceof RosterEntryElement)
            return -1;

        if (e1 instanceof RosterEntryElement
            && e2 instanceof RosterGroupElement)
            return 1;

        if (e1 instanceof RosterGroupElement
            && e2 instanceof RosterGroupElement) {
            String groupName1 = ((RosterGroupElement) e1).rosterGroup.getName();
            String groupName2 = ((RosterGroupElement) e2).rosterGroup.getName();
            return groupName1.compareToIgnoreCase(groupName2);
        }

        return super.compare(viewer, e1, e2);
    }
}
