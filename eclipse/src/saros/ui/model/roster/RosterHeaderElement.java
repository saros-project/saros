package saros.ui.model.roster;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.Roster;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.model.HeaderElement;
import saros.ui.model.TreeElement;

/**
 * Container {@link TreeElement} for a {@link Roster}
 *
 * @author bkahlert
 */
public class RosterHeaderElement extends HeaderElement {
  protected RosterContentProvider rosterContentProvider;
  protected Roster roster;

  public RosterHeaderElement(
      Font font, RosterContentProvider rosterContentProvider, Roster roster) {
    super(font);
    this.rosterContentProvider = rosterContentProvider;
    this.roster = roster;
  }

  @Override
  public StyledString getStyledText() {
    StyledString styledString = new StyledString();
    styledString.append(Messages.RosterHeaderElement_contacts, boldStyler);
    return styledString;
  }

  @Override
  public Image getImage() {
    return ImageManager.ICON_GROUP;
  }

  @Override
  public boolean hasChildren() {
    return true;
  }

  @Override
  public Object[] getChildren() {
    return rosterContentProvider.getElements(roster);
  }
}
