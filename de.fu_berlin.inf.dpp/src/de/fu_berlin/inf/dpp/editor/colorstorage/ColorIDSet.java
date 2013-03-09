package de.fu_berlin.inf.dpp.editor.colorstorage;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Represents a combination of user colors. It is used to give the same
 * combination of participants the same colors in each session.
 * 
 * <pre>
 * 
 * This class is <b>NOT</b> thread safe.
 * 
 * Updating or retrieving the set via the provided methods
 * of the {@link ColorIDSetStorage} class <b>must not</b> be
 * called while using the methods that are provided by this class.
 * </pre>
 */
public final class ColorIDSet implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<JID, UserColorID> assignedUserColorIDs;

    private volatile long timestamp;

    /**
     * Creates a {@link ColorIDSet}.
     * 
     * @param colorIDs
     *            a map from JIDs to ColorIds
     */
    ColorIDSet(Map<JID, UserColorID> colorIDs) {
        if (!isValidMap(colorIDs))
            throw new IllegalArgumentException(
                "a color ID is set multiple times");

        this.assignedUserColorIDs = colorIDs;
        resetTimestamp();
    }

    /**
     * Creates a {@link ColorIDSet} and sets color for each JID to
     * {@value UserColorID#UNKNOWN}
     * 
     * @param jids
     *            a set of JIDs
     */
    ColorIDSet(Collection<JID> jids) {
        assignedUserColorIDs = new HashMap<JID, UserColorID>();
        for (JID jid : jids)
            assignedUserColorIDs.put(jid, new UserColorID());

        resetTimestamp();
    }

    /**
     * Gets the color id for a given JID.
     * 
     * @param jid
     * @return colorId or {@value UserColorID#UNKNOWN} if the JID was not found
     *         in the set or no color id is assigned to that JID
     */
    public int getColor(JID jid) {
        if (!assignedUserColorIDs.containsKey(jid))
            return UserColorID.UNKNOWN;

        return assignedUserColorIDs.get(jid).getCurrent();
    }

    /**
     * Gets the favorite color id for a given JID.
     * 
     * @param jid
     * @return favorite colorId or {@value UserColorID#UNKNOWN} if the JID was
     *         not found in the set or no favorite color id is assigned to that
     *         JID
     */
    public int getFavoriteColor(JID jid) {
        if (!assignedUserColorIDs.containsKey(jid))
            return UserColorID.UNKNOWN;

        return assignedUserColorIDs.get(jid).getFavorite();
    }

    /**
     * Gets a new copy of the participants.
     * 
     * @return participants contained
     */
    public Set<JID> getParticipants() {
        return new HashSet<JID>(assignedUserColorIDs.keySet());
    }

    /**
     * Tells when the set was used last.
     * 
     * @return timestamp of that last usage
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns whether colorId is not yet in use of this set
     * 
     * @param colorID
     * @return <code>false</code> if colorId is in use <code>true</code>
     *         otherwise, because it is still available
     */
    public boolean isAvailable(int colorID) {

        for (UserColorID c : assignedUserColorIDs.values())
            if (c.getCurrent() == colorID)
                return false;

        return true;
    }

    /**
     * Two color ID sets are considered equals if they contain the same set of
     * JIDs.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColorIDSet)) {
            return false;
        }

        ColorIDSet other = (ColorIDSet) o;
        return assignedUserColorIDs.keySet().equals(
            other.assignedUserColorIDs.keySet());
    }

    @Override
    public int hashCode() {
        return assignedUserColorIDs.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String delim = ",";

        for (Iterator<JID> it = assignedUserColorIDs.keySet().iterator(); it
            .hasNext();) {
            JID jid = it.next();
            Integer id = getColor(jid);

            result.append(jid).append("=").append(id);
            if (it.hasNext()) {
                result.append(delim);
            }
        }

        result.append("?").append(timestamp);

        return result.toString();
    }

    /**
     * Resets the timestamp to the current system.
     */
    void resetTimestamp() {
        timestamp = System.currentTimeMillis();
    }

    /**
     * Sets the Timestamp.
     * 
     * @param time
     *            the timestamp
     */
    void setTimestamp(long time) {
        timestamp = time;
    }

    /**
     * Set the colorId of the set for a given user's JID
     * 
     * @param jid
     * @param colorID
     */
    void setColor(JID jid, int colorID) throws IllegalArgumentException {
        if (containsColorConflicts(jid, colorID))
            throw new IllegalArgumentException(
                "a color ID is set multiple times");

        if (!assignedUserColorIDs.containsKey(jid))
            throw new IllegalArgumentException(jid
                + " cannot be found in the current set");

        assignedUserColorIDs.get(jid).setCurrent(colorID);
    }

    /**
     * Set the favorite colorId of the set for a given user's JID
     * 
     * @param jid
     * @param colorID
     */
    void setFavoriteColor(JID jid, int colorID) throws IllegalArgumentException {

        if (!assignedUserColorIDs.containsKey(jid))
            throw new IllegalArgumentException(jid
                + " cannot be found in the current set");

        assignedUserColorIDs.get(jid).setFavorite(colorID);
    }

    /**
     * Check whether a new colorId causes a conflict in a given map of colorIds.
     * 
     * @param jid
     * @param colorID
     * @return
     */
    private boolean containsColorConflicts(JID jid, int colorID) {

        if (colorID == UserColorID.UNKNOWN || getColor(jid) == colorID)
            return false;

        return !isAvailable(colorID);
    }

    /**
     * Checks whether a given map is valid (no duplicate keys and values)
     * 
     * @param colorIDs
     * @return <code>true</code> if no color id duplicates exist
     *         <code>false</code> otherwise
     */
    private boolean isValidMap(Map<JID, UserColorID> colorIDs) {

        HashSet<Integer> usedColorIDs = new HashSet<Integer>();

        for (UserColorID id : colorIDs.values()) {
            if (id.getCurrent() == UserColorID.UNKNOWN)
                continue;

            if (usedColorIDs.contains(id.getCurrent()))
                return false;

            usedColorIDs.add(id.getCurrent());
        }

        return true;
    }

    /**
     * Creates a new set, containing the added JIDs.
     * 
     * @param jids
     * @return
     */
    ColorIDSet extendSet(Collection<JID> jids) {
        Map<JID, UserColorID> newColorIDs = new HashMap<JID, UserColorID>();

        newColorIDs.putAll(assignedUserColorIDs);

        for (JID jid : jids) {
            if (!newColorIDs.containsKey(jid))
                newColorIDs.put(jid, new UserColorID());
        }

        return new ColorIDSet(newColorIDs);
    }
}
