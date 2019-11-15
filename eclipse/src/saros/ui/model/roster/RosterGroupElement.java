package saros.ui.model.roster;

import java.util.Arrays;
import java.util.Objects;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import saros.ui.ImageManager;
import saros.ui.model.TreeElement;

/** Wrapper for ContactGroups in use with {@link Viewer Viewers} */
public class RosterGroupElement extends TreeElement {
  private final String groupName;
  private final RosterEntryElement[] children;

  public RosterGroupElement(String groupName, RosterEntryElement[] children) {
    this.groupName = groupName;
    this.children = children;
  }

  @Override
  public StyledString getStyledText() {
    return new StyledString(groupName);
  }

  @Override
  public Image getImage() {
    return ImageManager.ICON_GROUP;
  }

  @Override
  public Object[] getChildren() {
    return children == null ? new Object[0] : children;
  }

  @Override
  public boolean hasChildren() {
    return children != null && children.length > 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    RosterGroupElement other = (RosterGroupElement) obj;
    return Objects.equals(groupName, other.groupName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(groupName);
  }

  @Override
  public String toString() {
    return "RosterGroupElement [group="
        + groupName
        + ", children="
        + Arrays.toString(children)
        + "]";
  }
}
