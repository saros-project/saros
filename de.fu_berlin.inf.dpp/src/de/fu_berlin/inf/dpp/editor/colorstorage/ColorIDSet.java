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

    private static final long serialVersionUID = 1209783943934281146L;

    // TODO move to a central class
    private static final int DEFAULT_COLOR_ID = -1;

    private final Map<JID, Integer> assignedColorIDs;

    private volatile long timestamp;

    /**
     * Creates a {@link ColorIDSet}.
     * 
     * @param colorIDs
     *            a map from JIDs to ColorIds
     */
    ColorIDSet(Map<JID, Integer> colorIDs) {
        if (!isValidMap(colorIDs))
            throw new IllegalArgumentException(
                "Invalid map. A ColorId is set multiple times.");

        this.assignedColorIDs = colorIDs;
        resetTimestamp();
    }

    /**
     * Creates a {@link ColorIDSet} and sets color for each JID to
     * {@value #DEFAULT_COLOR_ID}
     * 
     * @param jids
     *            a set of JIDs
     */
    ColorIDSet(Collection<JID> jids) {
        assignedColorIDs = new HashMap<JID, Integer>();
        for (JID jid : jids)
            assignedColorIDs.put(jid, DEFAULT_COLOR_ID);

        resetTimestamp();
    }

    /**
     * Gets the color id for a given JID
     * 
     * @param jid
     * @return colorId or {@value #DEFAULT_COLOR_ID} if the JID was not found in
     *         the set or no color id is assigned to that JID
     */
    public int getColorID(JID jid) {
        if (!assignedColorIDs.containsKey(jid))
            return DEFAULT_COLOR_ID;

        return assignedColorIDs.get(jid);
    }

    /**
     * Gets a new copy of the participants.
     * 
     * @return participants contained
     */
    public Set<JID> getParticipants() {
        return new HashSet<JID>(assignedColorIDs.keySet());
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
        return (!assignedColorIDs.values().contains(colorID));
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
        return assignedColorIDs.keySet()
            .equals(other.assignedColorIDs.keySet());
    }

    @Override
    public int hashCode() {
        return assignedColorIDs.hashCode();
    }

    @Override
    public synchronized String toString() {
        StringBuilder result = new StringBuilder();
        String delim = ",";

        for (Iterator<JID> it = assignedColorIDs.keySet().iterator(); it
            .hasNext();) {
            JID jid = it.next();
            Integer id = getColorID(jid);

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
        if (containsColorConflicts(assignedColorIDs, jid, colorID))
            throw new IllegalArgumentException(
                "A ColorId is set multiple times.");

        if (!assignedColorIDs.containsKey(jid))
            throw new IllegalArgumentException(
                "ColorIdSet does not allow for injecting new JID.");

        assignedColorIDs.put(jid, colorID);
    }

    /**
     * Check whether a new colorId causes a conflict in a given map of colorIds.
     * 
     * @param colorIDs
     * @param jid
     * @param colorID
     * @return
     */
    private boolean containsColorConflicts(Map<JID, Integer> colorIDs, JID jid,
        int colorID) {
        return getColorID(jid) != colorID && colorIDs.containsValue(colorID);
    }

    /**
     * Checks whether a given map is valid (no duplicate keys and values)
     * 
     * @param colorIDs
     * @return <code>true</code> if no color id duplicates exist
     *         <code>false</code> otherwise
     */
    private boolean isValidMap(Map<JID, Integer> colorIDs) {

        HashSet<Integer> colorIDsSet = new HashSet<Integer>();

        for (int i : colorIDs.values()) {
            if (i == DEFAULT_COLOR_ID)
                continue;

            if (colorIDsSet.contains(i))
                return false;

            colorIDsSet.add(i);
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
        Map<JID, Integer> newColorIDs = new HashMap<JID, Integer>();

        newColorIDs.putAll(assignedColorIDs);

        for (JID jid : jids) {
            if (!newColorIDs.containsKey(jid))
                newColorIDs.put(jid, DEFAULT_COLOR_ID);
        }

        return new ColorIDSet(newColorIDs);
    }
}
