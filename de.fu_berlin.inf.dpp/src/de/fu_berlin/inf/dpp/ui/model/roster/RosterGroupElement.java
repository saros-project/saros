package de.fu_berlin.inf.dpp.ui.model.roster;

import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.RosterGroup;

/**
 * Wrapper for {@link RosterGroup RosterGroups} in use with {@link Viewer Viewers}
 *
 * @author bkahlert
 */
public class RosterGroupElement extends TreeElement {
  private final RosterGroup group;
  private final RosterEntryElement[] children;

  public RosterGroupElement(RosterGroup group, RosterEntryElement[] children) {
    this.group = group;
    this.children = children;
  }

  public RosterGroup getRosterGroup() {
    return group;
  }

  @Override
  public StyledString getStyledText() {
    return new StyledString(group.getName());
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
    if (obj == this) return true;

    if (!(obj instanceof RosterGroupElement)) return false;

    RosterGroupElement rosterGroupElement = (RosterGroupElement) obj;
    return group.equals(rosterGroupElement.group);
  }

  @Override
  public int hashCode() {
    return group != null ? group.hashCode() : 0;
  }
}
