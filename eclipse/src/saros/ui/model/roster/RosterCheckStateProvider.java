package saros.ui.model.roster;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jivesoftware.smack.Roster;

/**
 * Implements a {@link ICheckStateProvider} for use with {@link Roster} displaying JFace {@link
 * Viewer}s.
 *
 * @author bkahlert
 */
public class RosterCheckStateProvider implements ICheckStateProvider {

  protected Map<Object, Boolean> checkStates = new HashMap<Object, Boolean>();

  @Override
  public boolean isChecked(Object element) {
    if (element instanceof RosterEntryElement) {
      Boolean checked = checkStates.get(element);
      if (checked != null && checked) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean isGrayed(Object element) {
    if (element instanceof RosterEntryElement) {
      return false;
    }

    return true;
  }

  public void setChecked(Object element, boolean checked) {
    checkStates.put(element, checked);
  }
}
