package saros.net.xmpp.contact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Class to store Contact Group Information. This class is immutable and thereby Thread-safe. */
public class XMPPContactGroup {

  private final String groupName;
  private final List<XMPPContact> contacts;

  XMPPContactGroup(String groupName, Collection<XMPPContact> contacts) {
    this.groupName = Objects.requireNonNull(groupName);
    this.contacts = Collections.unmodifiableList(new ArrayList<XMPPContact>(contacts));
  }

  /**
   * Get all Contacts belonging to this group.
   *
   * @return List of XMPPContacts in this group
   */
  public List<XMPPContact> getContacts() {
    return contacts;
  }

  /**
   * Name of this group.
   *
   * @return group name
   */
  public String getName() {
    return groupName;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof XMPPContactGroup)) return false;
    XMPPContactGroup other = (XMPPContactGroup) obj;
    return Objects.equals(groupName, other.groupName) && Objects.equals(contacts, other.contacts);
  }

  @Override
  public int hashCode() {
    return 31 * Objects.hashCode(groupName) + Objects.hashCode(contacts);
  }

  @Override
  public String toString() {
    return "XMPPContactGroup [name=" + groupName + ", contacts=" + contacts + "]";
  }
}
