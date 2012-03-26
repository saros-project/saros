package de.fu_berlin.inf.dpp.ui.model.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;

/**
 * Wrapper for {@link RosterGroup RosterGroups} in use with {@link Viewer
 * Viewers}
 * 
 * @author bkahlert
 */
public class RosterGroupElement extends TreeElement {
    protected Roster roster;
    protected RosterGroup rosterGroup;

    public RosterGroupElement(Roster roster, RosterGroup rosterGroup) {
        this.roster = roster;
        this.rosterGroup = rosterGroup;
    }

    @Override
    public StyledString getStyledText() {
        return new StyledString(this.rosterGroup.getName());
    }

    @Override
    public Image getImage() {
        return ImageManager.ICON_GROUP;
    }

    @Override
    public Object[] getChildren() {
        Collection<RosterEntry> rosterEntries = rosterGroup.getEntries();
        List<RosterEntryElement> children = new ArrayList<RosterEntryElement>();
        for (RosterEntry rosterEntry : rosterEntries) {
            children.add(new RosterEntryElement(roster, new JID(rosterEntry
                .getUser())));
        }
        return children.toArray();
    }

    @Override
    public boolean hasChildren() {
        return rosterGroup.getEntryCount() > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof RosterGroupElement)) {
            return false;
        }

        RosterGroupElement rosterGroupElement = (RosterGroupElement) obj;
        return this.rosterGroup.equals(rosterGroupElement.rosterGroup);
    }

    @Override
    public int hashCode() {
        return (this.rosterGroup != null) ? this.rosterGroup.hashCode() : 0;
    }
}
