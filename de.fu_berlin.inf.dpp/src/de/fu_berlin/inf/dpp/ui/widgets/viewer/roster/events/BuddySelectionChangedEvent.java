package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events;

import de.fu_berlin.inf.dpp.net.JID;

public class BuddySelectionChangedEvent {
    private JID jid;
    private boolean isSelected;

    /**
     * @param jid
     *            of the buddy who's selection changed
     * @param isSelected
     *            new selection state
     */
    public BuddySelectionChangedEvent(JID jid, boolean isSelected) {
        super();
        this.jid = jid;
        this.isSelected = isSelected;
    }

    public JID getJid() {
        return jid;
    }

    public boolean isSelected() {
        return isSelected;
    }

}
