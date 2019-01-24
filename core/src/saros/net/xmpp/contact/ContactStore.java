package saros.net.xmpp.contact;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import saros.net.xmpp.JID;

/**
 * This Class is used by {@link XMPPContactsService} to store Contact objects and its group mappings
 * during one connection and get them to return / apply updates.
 *
 * <p>A contact can be part of none, one or multiple groups.
 *
 * <p>Get methods (except {@link #getOrCreateXMPPUser(RosterEntry)} are safe to use outside the
 * {@link XMPPContactsService} Executor Thread. All others must be called within the Executor Thread
 * to avoid strange states like creating a new Contact instead of getting an existing one or a
 * removed Contact added to groups.
 */
class ContactStore {

  private static final String NO_GROUP = "NO_GROUP";
  private static final Function<XMPPContact, String> GET_BAREJID_STRING =
      (contact) -> contact.getBareJid().getRAW();

  /**
   * Mapping of Group Name to Map of BareJid and Contact. {@link #NO_GROUP} is used for contacts
   * without group mapping.
   */
  private final Map<String, Map<String, XMPPContact>> contactsGrouped = new ConcurrentHashMap<>();

  /** Clear all contact mappings. */
  void clear() {
    contactsGrouped.clear();
  }

  /**
   * Get a known user by address.
   *
   * @param address bareJid or fullJid
   * @return XMPPContact if found or null
   */
  XMPPContact get(String address) {
    String bareJid = new JID(address).getBase();
    for (Map<String, XMPPContact> map : contactsGrouped.values()) {
      XMPPContact xmppContact = map.get(bareJid);
      if (xmppContact != null) return xmppContact;
    }
    return null;
  }

  /**
   * Get a distinct list of all known contacts.
   *
   * @return list of all known {@link XMPPContact XMPPContacts}
   */
  List<XMPPContact> getAll() {
    return contactsGrouped.values().stream()
        .flatMap(map -> map.values().stream())
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Get a list of all groups as {@link XMPPContactGroup}.
   *
   * @return list of all groups as {@link XMPPContactGroup}
   */
  List<XMPPContactGroup> getGroupsList() {
    return contactsGrouped.entrySet().stream()
        .filter(group -> !group.getKey().equals(NO_GROUP))
        .map(group -> new XMPPContactGroup(group.getKey(), group.getValue().values()))
        .collect(Collectors.toList());
  }

  /**
   * Get a known XMPPUser or create a new one.
   *
   * @param entry
   * @return a new or already known XMPPUser
   */
  XMPPContact getOrCreateXMPPUser(RosterEntry entry) {
    XMPPContact contact = get(entry.getUser());
    if (contact != null) return contact;
    return XMPPContact.from(entry);
  }

  /**
   * Get all contacts not assigned to any group.
   *
   * @return Collection of all contacts not assigned to any group
   */
  Collection<XMPPContact> getUngrouped() {
    return contactsGrouped.getOrDefault(NO_GROUP, Collections.emptyMap()).values();
  }

  /**
   * This method asks the roster for contacts and fill its store.
   *
   * @param roster current Roster
   */
  void init(Roster roster) {
    addContacts(NO_GROUP, rosterEntriesToXMPPContacts(roster.getUnfiledEntries()));
    for (RosterGroup group : roster.getGroups()) {
      addContacts(group.getName(), rosterEntriesToXMPPContacts(group.getEntries()));
    }
  }

  private List<XMPPContact> rosterEntriesToXMPPContacts(Collection<RosterEntry> entries) {
    return entries.stream().map(this::getOrCreateXMPPUser).collect(Collectors.toList());
  }

  private void addContacts(String groupName, List<XMPPContact> contacts) {
    contactsGrouped
        .computeIfAbsent(groupName, newMap -> new ConcurrentHashMap<>())
        .putAll(
            contacts.stream().collect(Collectors.toMap(GET_BAREJID_STRING, Function.identity())));
  }

  /**
   * Remove a contact from all groups.
   *
   * @param bareJid
   * @return Removed contact if found or null
   */
  XMPPContact remove(String bareJid) {
    XMPPContact userRemoved = null;
    for (Map<String, XMPPContact> map : contactsGrouped.values()) {
      XMPPContact removed = map.remove(bareJid);
      if (removed != null) {
        userRemoved = removed;
      }
    }
    removeEmptyGroups();
    return userRemoved;
  }

  /**
   * Removes old mappings and set new Group mapping.
   *
   * @param contact
   * @param groups if list is empty added to default group
   * @return true if group mapping was changed
   */
  boolean setContactGroups(XMPPContact contact, Collection<RosterGroup> groups) {
    List<String> groupNames;
    if (groups.isEmpty()) {
      groupNames = Collections.singletonList(NO_GROUP);
    } else {
      groupNames = groups.stream().map(RosterGroup::getName).collect(Collectors.toList());
    }

    boolean removeChanged = removeContactFromGroupsExcluding(contact, groupNames);
    boolean addChanged = addContactToGroups(contact, groupNames);
    return removeChanged || addChanged;
  }

  private boolean addContactToGroups(XMPPContact contact, List<String> groupNames) {
    boolean changes = false;
    for (String groupName : groupNames) {
      boolean added =
          contactsGrouped
                  .computeIfAbsent(groupName, newMap -> new ConcurrentHashMap<>())
                  .putIfAbsent(GET_BAREJID_STRING.apply(contact), contact)
              == null;
      if (added) changes = true;
    }
    return changes;
  }

  private boolean removeContactFromGroupsExcluding(XMPPContact contact, List<String> exclude) {
    String jid = GET_BAREJID_STRING.apply(contact);

    long removed =
        contactsGrouped.entrySet().stream()
            .filter(entry -> !exclude.contains(entry.getKey()))
            .map(entry -> Optional.ofNullable(entry.getValue().remove(jid)))
            .filter(Optional::isPresent)
            .count();

    removeEmptyGroups();
    return removed != 0;
  }

  private void removeEmptyGroups() {
    contactsGrouped.entrySet().removeIf(group -> group.getValue().isEmpty());
  }
}
