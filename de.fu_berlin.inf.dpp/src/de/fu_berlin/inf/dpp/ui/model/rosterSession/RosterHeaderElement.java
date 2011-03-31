package de.fu_berlin.inf.dpp.ui.model.rosterSession;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.Roster;

import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterContentProvider;

/**
 * Container {@link TreeElement}Êfor a {@link Roster}
 * 
 * @author bkahlert
 */
public class RosterHeaderElement extends HeaderElement {
    protected RosterContentProvider rosterContentProvider;
    protected Roster roster;

    public RosterHeaderElement(Font font,
        RosterContentProvider rosterContentProvider, Roster roster) {
        super(font);
        this.rosterContentProvider = rosterContentProvider;
        this.roster = roster;
    }

    @Override
    public StyledString getStyledText() {
        StyledString styledString = new StyledString();
        styledString.append("Buddies", boldStyler);
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
        return rosterContentProvider.getElements(this.roster);
    }
}
