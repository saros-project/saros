package de.fu_berlin.inf.dpp.ui.model;

/**
 * Represent an entry in a contact list.
 *
 * This class is immutable.
 */
public class Contact {
    private final boolean isOnline;
    private final boolean isHidden;
    private final String displayName;

    /**
     * @param displayName the name of the contact as it should be displayed
     * @param isOnline boolean indicating online status
     */
    public Contact(String displayName, boolean isOnline) {
        this.displayName = displayName;
        this.isOnline = isOnline;
        isHidden = false;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public String getDisplayName() {
        return displayName;
    }
}
