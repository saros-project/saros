package saros.editor.colorstorage;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents a combination of user colors. It is used to give the same combination of participants
 * the same colors in each session.
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

  private static final long serialVersionUID = 2L;

  private final Map<String, UserColorID> assignedUserColorIDs;

  private volatile long timestamp;

  /**
   * Creates a {@link ColorIDSet}.
   *
   * @param colorIDs a map from ids to color ids
   */
  ColorIDSet(Map<String, UserColorID> colorIDs) {
    if (!isValidMap(colorIDs))
      throw new IllegalArgumentException("a color ID is set multiple times");

    this.assignedUserColorIDs = colorIDs;
    resetTimestamp();
  }

  /**
   * Creates a {@link ColorIDSet} and sets color for each id to {@value UserColorID#UNKNOWN}
   *
   * @param ids
   */
  ColorIDSet(Collection<String> ids) {
    assignedUserColorIDs = new HashMap<String, UserColorID>();
    for (String id : ids) assignedUserColorIDs.put(id, new UserColorID());

    resetTimestamp();
  }

  /**
   * Gets the color id for a given id.
   *
   * @param id
   * @return color id or {@value UserColorID#UNKNOWN} if the id was not found in the set or no color
   *     id is assigned to that id
   */
  public int getColor(String id) {
    if (!assignedUserColorIDs.containsKey(id)) return UserColorID.UNKNOWN;

    return assignedUserColorIDs.get(id).getCurrent();
  }

  /**
   * Gets the favorite color id for a given id.
   *
   * @param id
   * @return favorite color id or {@value UserColorID#UNKNOWN} if the id was not found in the set or
   *     no favorite color id is assigned to that id
   */
  public int getFavoriteColor(String id) {
    if (!assignedUserColorIDs.containsKey(id)) return UserColorID.UNKNOWN;

    return assignedUserColorIDs.get(id).getFavorite();
  }

  /**
   * Gets a new copy of the participants.
   *
   * @return participants contained
   */
  public Set<String> getParticipants() {
    return new HashSet<String>(assignedUserColorIDs.keySet());
  }

  /**
   * Returns whether colorId is not yet in use of this set
   *
   * @param colorID
   * @return <code>false</code> if colorId is in use <code>true</code> otherwise, because it is
   *     still available
   */
  public boolean isAvailable(int colorID) {
    for (UserColorID c : assignedUserColorIDs.values()) if (c.getCurrent() == colorID) return false;

    return true;
  }

  /** Two color ID sets are considered equals if they contain the same set of JIDs. */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ColorIDSet)) {
      return false;
    }

    ColorIDSet other = (ColorIDSet) o;
    return assignedUserColorIDs.keySet().equals(other.assignedUserColorIDs.keySet());
  }

  @Override
  public int hashCode() {
    return assignedUserColorIDs.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    String delim = ",";

    for (Iterator<String> it = assignedUserColorIDs.keySet().iterator(); it.hasNext(); ) {
      String id = it.next();
      Integer cId = getColor(id);

      result.append(id).append("=").append(cId);
      if (it.hasNext()) {
        result.append(delim);
      }
    }

    result.append("?").append(timestamp);

    return result.toString();
  }

  /**
   * Tells when the set was used last.
   *
   * @return timestamp of that last usage
   */
  long getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the Timestamp.
   *
   * @param time the timestamp
   */
  void setTimestamp(long time) {
    timestamp = time;
  }

  /** Resets the timestamp to the current system. */
  void resetTimestamp() {
    timestamp = System.currentTimeMillis();
  }

  /**
   * Set the colorId of the set for a given user id.
   *
   * @param id
   * @param colorID
   */
  void setColor(String id, int colorID) throws IllegalArgumentException {
    if (containsColorConflicts(id, colorID))
      throw new IllegalArgumentException("a color ID is set multiple times");

    if (!assignedUserColorIDs.containsKey(id))
      throw new IllegalArgumentException(id + " cannot be found in the current set");

    assignedUserColorIDs.get(id).setCurrent(colorID);
  }

  /**
   * Set the favorite color id of the set for the given user id.
   *
   * @param id
   * @param colorID
   */
  void setFavoriteColor(String id, int colorID) throws IllegalArgumentException {

    if (!assignedUserColorIDs.containsKey(id))
      throw new IllegalArgumentException(id + " cannot be found in the current set");

    assignedUserColorIDs.get(id).setFavorite(colorID);
  }

  /**
   * Check whether a new color id causes a conflict in a given map of colorIds.
   *
   * @param id
   * @param colorID
   * @return
   */
  private boolean containsColorConflicts(String id, int colorID) {
    if (colorID == UserColorID.UNKNOWN || getColor(id) == colorID) return false;

    return !isAvailable(colorID);
  }

  /**
   * Checks whether a given map is valid (no duplicate keys and values)
   *
   * @param colorIDs
   * @return <code>true</code> if no color id duplicates exist <code>false</code> otherwise
   */
  private boolean isValidMap(Map<String, UserColorID> colorIDs) {
    HashSet<Integer> usedColorIDs = new HashSet<Integer>();

    for (UserColorID id : colorIDs.values()) {
      if (id.getCurrent() == UserColorID.UNKNOWN) continue;

      if (usedColorIDs.contains(id.getCurrent())) return false;

      usedColorIDs.add(id.getCurrent());
    }

    return true;
  }

  /**
   * Creates a new set, containing the added ids.
   *
   * @param ids
   * @return
   */
  ColorIDSet extendSet(Collection<String> ids) {
    Map<String, UserColorID> newColorIDs = new HashMap<String, UserColorID>();

    newColorIDs.putAll(assignedUserColorIDs);

    for (String id : ids) {
      if (!newColorIDs.containsKey(id)) newColorIDs.put(id, new UserColorID());
    }

    return new ColorIDSet(newColorIDs);
  }
}
