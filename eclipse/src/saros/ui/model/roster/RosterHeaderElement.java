package de.fu_berlin.inf.dpp.ui.model.roster;

import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.model.HeaderElement;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.Roster;

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
