package saros.ui.model.mdns;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/** Comparator for sorting {@link MDNSEntryElement MDNS entry elements} in alphabetic order. */
public class MDNSComparator extends ViewerComparator {

  @Override
  public int compare(final Viewer viewer, final Object e1, final Object e2) {

    if (e1 instanceof MDNSEntryElement && e2 instanceof MDNSEntryElement) {
      String entryName1 = ((MDNSEntryElement) e1).getName();
      String entryName2 = ((MDNSEntryElement) e2).getName();
      return entryName1.compareToIgnoreCase(entryName2);
    }

    return super.compare(viewer, e1, e2);
  }
}
