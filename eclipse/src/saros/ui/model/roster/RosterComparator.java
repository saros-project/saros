package saros.ui.model.roster;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.jivesoftware.smack.Roster;
import saros.ui.model.ITreeElement;

/**
 * Comparator for {@link ITreeElement} describing {@link Roster} entities.
 *
 * <p>{@link RosterGroupElement}s and {@link RosterEntryElement}s are sorted alphabetically.
 *
 * <p>First {@link RosterGroupElement}s and then {@link RosterEntryElement}s are displayed.
 */
public class RosterComparator extends ViewerComparator {

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {

    if (e1 instanceof RosterGroupElement && e2 instanceof RosterEntryElement) return -1;

    if (e1 instanceof RosterEntryElement && e2 instanceof RosterGroupElement) return 1;

    if (e1 instanceof RosterGroupElement && e2 instanceof RosterGroupElement) {
      String groupName1 = ((RosterGroupElement) e1).getStyledText().toString();
      String groupName2 = ((RosterGroupElement) e2).getStyledText().toString();
      return groupName1.compareToIgnoreCase(groupName2);
    }

    if (e1 instanceof RosterEntryElement && e2 instanceof RosterEntryElement) {

      if (((RosterEntryElement) e1).isOnline() && !((RosterEntryElement) e2).isOnline()) return -1;

      if (!((RosterEntryElement) e1).isOnline() && ((RosterEntryElement) e2).isOnline()) return 1;

      String entryName1 = ((RosterEntryElement) e1).getStyledText().toString();
      String entryName2 = ((RosterEntryElement) e2).getStyledText().toString();
      return entryName1.compareToIgnoreCase(entryName2);
    }

    return super.compare(viewer, e1, e2);
  }
}
