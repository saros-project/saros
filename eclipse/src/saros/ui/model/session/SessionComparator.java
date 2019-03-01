package saros.ui.model.session;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import saros.ui.model.HeaderElement;
import saros.ui.model.ITreeElement;

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
        && e2 instanceof SessionHeaderElement) return 1;

    if (e1 instanceof SessionHeaderElement
        && e2 instanceof HeaderElement
        && !(e2 instanceof SessionHeaderElement)) return -1;

    /*
     * Compares session part
     */
    if (e1 instanceof UserElement && e2 instanceof UserElement) {
      return ((UserElement) e1).compareTo((UserElement) e2);
    }

    /*
     * Compares content part
     */
    if (contentComparator != null) return contentComparator.compare(viewer, e1, e2);
    else return super.compare(viewer, e1, e2);
  }
}
