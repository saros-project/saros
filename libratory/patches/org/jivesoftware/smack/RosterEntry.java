/**
 * $RCSfile$ $Revision: 13662 $ $Date: 2013-05-17 15:05:57 -0700 (Fri, 17 May 2013) $
 *
 * <p>Copyright 2003-2007 Jive Software.
 *
 * <p>All rights reserved. Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.RosterPacket;

/**
 * Each user in your roster is represented by a roster entry, which contains the user's JID and a
 * name or nickname you assign.
 *
 * @author Matt Tucker
 */
public class RosterEntry {

  private String user;
  private String name;
  private RosterPacket.ItemType type;
  private RosterPacket.ItemStatus status;
  private final Roster roster;
  private final Connection connection;

  /**
   * Creates a new roster entry.
   *
   * @param user the user.
   * @param name the nickname for the entry.
   * @param type the subscription type.
   * @param status the subscription status (related to subscriptions pending to be approbed).
   * @param connection a connection to the XMPP server.
   */
  RosterEntry(
      String user,
      String name,
      RosterPacket.ItemType type,
      RosterPacket.ItemStatus status,
      Roster roster,
      Connection connection) {
    this.user = user;
    this.name = name;
    this.type = type;
    this.status = status;
    this.roster = roster;
    this.connection = connection;
  }

  /**
   * Returns the JID of the user associated with this entry.
   *
   * @return the user associated with this entry.
   */
  public String getUser() {
    return user;
  }

  /**
   * Returns the name associated with this entry.
   *
   * @return the name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name associated with this entry.
   *
   * @param name the name.
   */
  public void setName(String name) {
    // Do nothing if the name hasn't changed.
    if (name != null && name.equals(this.name)) {
      return;
    }

    // Create a RosterEntry-copy with the new name to create an
    // update-RosterPacket
    RosterEntry updatedRosterEntry =
        new RosterEntry(this.user, name, this.type, this.status, this.roster, this.connection);
    RosterPacket packet = new RosterPacket();
    packet.setType(IQ.Type.SET);
    packet.addRosterItem(toRosterItem(updatedRosterEntry));
    connection.sendPacket(packet);
  }

  /**
   * Updates the state of the entry with the new values.
   *
   * @param name the nickname for the entry.
   * @param type the subscription type.
   * @param status the subscription status (related to subscriptions pending to be approbed).
   */
  void updateState(String name, RosterPacket.ItemType type, RosterPacket.ItemStatus status) {
    this.name = name;
    this.type = type;
    this.status = status;
  }

  /**
   * Returns an unmodifiable collection of the roster groups that this entry belongs to.
   *
   * @return an iterator for the groups this entry belongs to.
   */
  public Collection<RosterGroup> getGroups() {
    List<RosterGroup> results = new ArrayList<RosterGroup>();
    // Loop through all roster groups and find the ones that contain this
    // entry. This algorithm should be fine
    for (RosterGroup group : roster.getGroups()) {
      if (group.contains(this)) {
        results.add(group);
      }
    }
    return Collections.unmodifiableCollection(results);
  }

  /**
   * Returns the roster subscription type of the entry. When the type is RosterPacket.ItemType.none
   * or RosterPacket.ItemType.from, refer to {@link RosterEntry getStatus()} to see if a
   * subscription request is pending.
   *
   * @return the type.
   */
  public RosterPacket.ItemType getType() {
    return type;
  }

  /**
   * Returns the roster subscription status of the entry. When the status is
   * RosterPacket.ItemStatus.SUBSCRIPTION_PENDING, the contact has to answer the subscription
   * request.
   *
   * @return the status.
   */
  public RosterPacket.ItemStatus getStatus() {
    return status;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    if (name != null) {
      buf.append(name).append(": ");
    }
    buf.append(user);
    Collection<RosterGroup> groups = getGroups();
    if (!groups.isEmpty()) {
      buf.append(" [");
      Iterator<RosterGroup> iter = groups.iterator();
      RosterGroup group = iter.next();
      buf.append(group.getName());
      while (iter.hasNext()) {
        buf.append(", ");
        group = iter.next();
        buf.append(group.getName());
      }
      buf.append("]");
    }
    return buf.toString();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object != null && object instanceof RosterEntry) {
      return user.equals(((RosterEntry) object).getUser());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return this.user.hashCode();
  }

  /**
   * Indicates whether some other object is "equal to" this by comparing all members.
   *
   * <p>The {@link #equals(Object)} method returns <code>true</code> if the user JIDs are equal.
   *
   * @param obj the reference object with which to compare.
   * @return <code>true</code> if this object is the same as the obj argument; <code>false</code>
   *     otherwise.
   */
  public boolean equalsDeep(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    RosterEntry other = (RosterEntry) obj;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (status == null) {
      if (other.status != null) return false;
    } else if (!status.equals(other.status)) return false;
    if (type == null) {
      if (other.type != null) return false;
    } else if (!type.equals(other.type)) return false;
    if (user == null) {
      if (other.user != null) return false;
    } else if (!user.equals(other.user)) return false;
    return true;
  }

  static RosterPacket.Item toRosterItem(RosterEntry entry) {
    RosterPacket.Item item = new RosterPacket.Item(entry.getUser(), entry.getName());
    item.setItemType(entry.getType());
    item.setItemStatus(entry.getStatus());
    // Set the correct group names for the item.
    for (RosterGroup group : entry.getGroups()) {
      item.addGroupName(group.getName());
    }
    return item;
  }
}
