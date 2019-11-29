package saros.net.xmpp.contact;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemStatus;
import saros.exceptions.OperationCanceledException;
import saros.net.ConnectionState;
import saros.net.ResourceFeature;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.contact.ContactStatus.Type;
import saros.net.xmpp.contact.IContactsUpdate.UpdateType;
import saros.net.xmpp.subscription.SubscriptionHandler;
import saros.net.xmpp.subscription.SubscriptionListener;
import saros.repackaged.picocontainer.Disposable;
import saros.util.NamedThreadFactory;

/**
 * The purpose of this Class is to bundle all XMPP Contact interactions.
 *
 * <p>Contact lists are provided via {@link #getContactGroups()}, {@link #getContactsWithoutGroup()}
 * and {@link #getAllContacts()}. Provided contacts are managed by this class, and updated on
 * changes provided by the underlying Smack library. It is possible to receive notifications via
 * {@link #addListener(IContactsUpdate)} about these updates. If the Smack connection was ended the
 * Contacts are still returned in a Offline / Subscription Mode and XMPPContact objects change after
 * reconnect.
 *
 * <p>It is possible to rename and remove contacts via {@link #renameContact(XMPPContact, String)}
 * and {@link #removeContact(XMPPContact)}. If the connection is offline these methods will ignore
 * calls.
 *
 * <p>This class is thread-safe and all public methods are non-blocking.
 *
 * <p>The XMPP Subscription model is not implemented completely, but good enough to work fine enough
 * in most cases, especially in the normal add / remove scenarios which Saros supports.
 */
public class XMPPContactsService implements Disposable {
  private static final Logger log = Logger.getLogger(XMPPContactsService.class);

  private final List<IContactsUpdate> updateListeners = new CopyOnWriteArrayList<>();
  private final DiscoveryService discoveryService = new DiscoveryService();

  /**
   * Single-Threaded Executor to off-load work and providing thread-safety by being the only Thread
   * updating fields. Therefore must be used internally for all write access and read access to
   * non-volatile / non-final fields.
   */
  private final ExecutorService contactsExecutor =
      new ThreadPoolExecutor(
          0,
          1,
          30,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<Runnable>(),
          new NamedThreadFactory("XMPPContactService-ContactsThread", false));
  /** Modification only within contactsExecutor */
  private final ContactStore contacts = new ContactStore();
  /** Access to current Connection Service. */
  private final XMPPConnectionService connectionService;
  /** Only access within contactsExecutor */
  private Roster roster;

  private final IConnectionListener connectionListener =
      (connection, state) -> contactsExecutor.execute(() -> connectionChanged(connection, state));

  private final RosterListener rosterListener =
      new RosterListener() {
        @Override
        public void entriesAdded(Collection<String> addresses) {
          log.debug("add " + addresses);
          contactsExecutor.execute(() -> contactsAdded(addresses));
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
          log.debug("delete " + addresses);
          contactsExecutor.execute(() -> contactsRemoved(addresses));
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
          log.debug("update " + addresses);
          contactsExecutor.execute(() -> contactsUpdated(addresses));
        }

        @Override
        public void presenceChanged(Presence presence) {
          /*
           * Note that this listener is triggered for presence (mode) changes only (e.g presence
           * of types available and unavailable. Subscription-related presence packets will not
           * cause this method to be called.
           * https://github.com/igniterealtime/Smack/blob/0cec5713d1f93ee99a2226702d64a963e9981933/source/org/jivesoftware/smack/RosterListener.java#L75-L77
           */
          log.debug(presence.getFrom() + ": " + presence.getType());
          contactsExecutor.execute(() -> contactResourceChangedPresence(presence));
        }
      };

  /**
   * Because the RosterListener is not forwarding all subscription events we listen additionally to
   * Presence Packets filtered by {@link SubscriptionHandler}.
   */
  private final SubscriptionListener subscriptionListener =
      new SubscriptionListener() {
        @Override
        public void subscriptionCanceled(JID jid) {
          log.debug("subscription canceled from " + jid);
          contactsExecutor.execute(() -> contactCanceledSubscription(jid));
        }
      };

  public XMPPContactsService(
      XMPPConnectionService connectionService, SubscriptionHandler subscriptionHandler) {
    this.connectionService = connectionService;
    connectionService.addListener(connectionListener);
    subscriptionHandler.addSubscriptionListener(subscriptionListener);
  }

  /**
   * Get a unmodifiable list of all contacts on the roster, regardless of group mappings.
   *
   * @return List of XMPPContacts
   */
  public List<XMPPContact> getAllContacts() {
    return Collections.unmodifiableList(contacts.getAll());
  }

  /**
   * Get a unmodifiable list of all groups with contacts on the roster.
   *
   * @return List of XMPPContactGroups
   */
  public List<XMPPContactGroup> getContactGroups() {
    return Collections.unmodifiableList(contacts.getGroupsList());
  }

  /**
   * Get a unmodifiable collection of all contacts that have no assigned group on the roster.
   *
   * @return List of XMPPContacts
   */
  public Collection<XMPPContact> getContactsWithoutGroup() {
    return Collections.unmodifiableCollection(contacts.getUngrouped());
  }

  /**
   * Add a Listener to receive contact updates.
   *
   * @param listener
   */
  public void addListener(IContactsUpdate listener) {
    updateListeners.add(listener);
  }

  /**
   * Remove Listener.
   *
   * @param listener
   */
  public void removeListener(IContactsUpdate listener) {
    updateListeners.remove(listener);
  }

  /**
   * Add a contact to the roster.
   *
   * @param jid JID of the Contact to add
   * @param nickname Optional a Nickname for the Contact
   * @param questionDialog BiPredicate which will be called by this method with {@code String title}
   *     and {@code String message} if user interaction is needed.
   * @throws OperationCanceledException If information about JID can not be found / retrieved from
   *     Server and User canceled further trying, or Smack experienced an error.
   */
  public void addContact(JID jid, String nickname, BiPredicate<String, String> questionDialog)
      throws OperationCanceledException {
    AddContactUtility.addToRoster(connectionService, jid, nickname, questionDialog);
  }

  /**
   * Get a known contact by address.
   *
   * @param jid contact address
   * @return Optional with contact if found
   */
  public Optional<XMPPContact> getContact(String jid) {
    String baseJid = new JID(jid).getBase();
    return Optional.ofNullable(contacts.get(baseJid));
  }

  /**
   * Remove a contact completely from roster.
   *
   * <p>This method runs asynchronous and can fail silently (logging errors). If the removal was
   * successful, the roster sends an update and this will change the contact list leading to a
   * notification to all {@link IContactsUpdate} listener.
   *
   * <p>Known errors:
   *
   * <ul>
   *   <li>Smack possibly includes 'ask' attribute in roster items when sending requests
   *       (https://issues.igniterealtime.org/browse/SMACK-766) - catching exception, deletion still
   *       works sometimes, should be fixed by Smack update 4.2.1
   * </ul>
   *
   * @param contact XMPPContact to remove from roster
   */
  public void removeContact(XMPPContact contact) {
    contactsExecutor.execute(
        () -> {
          if (roster == null) return;

          RosterEntry entry = roster.getEntry(contact.getBareJid().getRAW());
          if (entry == null) {
            log.error("Remove of " + contact + " was not possible, RosterEntry not found!");
            return;
          }

          try {
            roster.removeEntry(entry);
          } catch (XMPPException e) {
            log.error("Remove of " + contact + " was not possible", e);
          }
        });
  }

  /**
   * Rename a contact on roster.
   *
   * <p>This method runs asynchronous and can fail silently. If the rename was successful, the
   * roster sends an update and this will change the contact leading to a notification to all {@link
   * IContactsUpdate} listener.
   *
   * <p>Known errors:
   *
   * <ul>
   *   <li>Contact rename only works correct after subscription is done. If the subscription is
   *       pending, Smack will provide a listener update and a new getEntry will provide the changed
   *       nickname, but this action is only local and not visible after reconnect.
   * </ul>
   *
   * @param contact XMPPContact to rename on roster
   */
  public void renameContact(XMPPContact contact, String newName) {
    contactsExecutor.execute(
        () -> {
          if (roster == null) return;

          RosterEntry entry = roster.getEntry(contact.getBareJid().getRAW());
          if (entry == null) {
            log.error("Rename of " + contact + " was not possible, RosterEntry not found!");
            return;
          }

          String changeTo = newName;
          if (changeTo != null) {
            changeTo = changeTo.trim();
            if ("".equals(changeTo)) changeTo = null;
          }
          if (!Objects.equals(entry.getName(), changeTo)) entry.setName(changeTo);
        });
  }

  @Override
  public void dispose() {
    contactsExecutor.shutdownNow();
    discoveryService.stop();
  }

  private void connectionChanged(Connection connection, ConnectionState state) {
    if (state == ConnectionState.CONNECTING) {
      roster = connection.getRoster();
      if (roster == null) {
        log.fatal("A new connection without a roster should not happen!");
        return;
      }

      roster.addRosterListener(rosterListener);
      discoveryService.connectionChanged(connection);
      contacts.clear();
    } else if (state == ConnectionState.CONNECTED) {
      /* The roster provided list is probably empty after connection start, and we get contacts
      later via {@link RosterListener#entriesAdded(Collection)}, but just to be sure. */
      contacts.init(roster);
      notifyListeners(null, UpdateType.CONNECTED);
    } else if (state == ConnectionState.DISCONNECTING || state == ConnectionState.ERROR) {
      if (roster != null) roster.removeRosterListener(rosterListener);
      roster = null;
      discoveryService.connectionChanged(null);

      contacts.getAll().forEach(XMPPContact::removeResources);
      notifyListeners(null, UpdateType.NOT_CONNECTED);
    }
  }

  private void contactsAdded(Collection<String> addresses) {
    if (roster == null) return;

    for (String address : addresses) {
      RosterEntry entry = roster.getEntry(address);
      if (entry == null) {
        log.error("Should not happen: Contact added, but RosterEntry is missing for: " + address);
        continue;
      }

      XMPPContact contact = contacts.getOrCreateXMPPUser(entry);
      contacts.setContactGroups(contact, entry.getGroups());
      notifyListeners(contact, UpdateType.ADDED);
    }
  }

  private void contactsRemoved(Collection<String> addresses) {
    if (roster == null) return;

    for (String address : addresses) {
      XMPPContact removedContact = contacts.remove(address);
      if (removedContact == null) {
        log.warn("Strange behaviour: Contact for " + address + " not found!");
        continue;
      }

      discoveryService.removeResources(removedContact.getResources());
      removedContact.removeResources();
      removedContact.setBaseStatus(ContactStatus.TYPE_REMOVED);
      notifyListeners(removedContact, UpdateType.REMOVED);
    }
  }

  private void contactsUpdated(Collection<String> addresses) {
    if (roster == null) return;

    for (String address : addresses) {
      XMPPContact contact = contacts.get(address);
      if (contact == null) {
        log.error("Should not happen: Contact for " + address + " not found!");
        continue;
      }

      RosterEntry entry = roster.getEntry(address);
      if (entry == null) {
        log.error("Should not happen: Contact found, but RosterEntry is missing for: " + address);
        continue;
      }

      if (handlePendingSubscription(contact, entry)) notifyListeners(contact, UpdateType.STATUS);

      if (contacts.setContactGroups(contact, entry.getGroups()))
        notifyListeners(contact, UpdateType.GROUP_MAPPING);

      if (contact.setNickname(entry.getName()))
        notifyListeners(contact, UpdateType.NICKNAME_CHANGED);
    }
  }

  private void contactResourceChangedPresence(Presence presence) {
    if (roster == null) return;

    String address = presence.getFrom();
    XMPPContact contact = contacts.get(address);
    if (contact == null) {
      log.error("Should not happen: Contact " + address + " for presence update not found!");
      return;
    }

    JID fullJid = new JID(address);
    if (presence.isAvailable()) {
      handleContactResourceAvailable(contact, fullJid, presence);
    } else {
      handleContactResourceUnavailable(contact, fullJid);
    }
  }

  private void contactCanceledSubscription(JID jid) {
    XMPPContact contact = contacts.get(jid.getRAW());
    if (contact == null) {
      log.debug("Strange behaviour: no contact found for canceled subscription from " + jid);
      return;
    }

    discoveryService.removeResources(contact.getResources());
    contact.removeResources();
    contact.setBaseStatus(ContactStatus.TYPE_SUBSCRIPTION_CANCELED);
    notifyListeners(contact, UpdateType.STATUS);
  }

  private boolean handlePendingSubscription(XMPPContact contact, RosterEntry entry) {
    if (entry.getStatus() == ItemStatus.SUBSCRIPTION_PENDING)
      return contact.setBaseStatus(ContactStatus.TYPE_SUBSCRIPTION_PENDING);
    return false;
  }

  private void handleContactResourceAvailable(XMPPContact contact, JID fullJid, Presence presence) {
    contact.setSubscribed();

    ContactStatus status;
    if (presence.isAway()) {
      status = ContactStatus.newInstance(Type.AWAY, presence.getMode().toString());
    } else {
      status = ContactStatus.TYPE_AVAILABLE;
    }
    if (contact.setResourceStatus(fullJid, status)) notifyListeners(contact, UpdateType.STATUS);

    discoveryService.queryFeatureSupport(
        fullJid, createFeatureQueryResultHandler(fullJid, contact));
  }

  private void handleContactResourceUnavailable(XMPPContact contact, JID fullJid) {
    discoveryService.removeResources(Collections.singletonList(fullJid));
    if (contact.removeResource(fullJid)) notifyListeners(contact, UpdateType.STATUS);
  }

  /**
   * Consumer to be called by DiscoveryService after a positive query.
   *
   * @param fullJid
   * @param contact
   * @return Consumer to be called after a positive query
   */
  private Consumer<EnumSet<ResourceFeature>> createFeatureQueryResultHandler(
      JID fullJid, XMPPContact contact) {
    return features -> {
      contactsExecutor.execute(
          () -> {
            if (contact.setFeatureSupport(fullJid, features))
              notifyListeners(contact, UpdateType.FEATURE_SUPPORT);
          });
    };
  }

  private void notifyListeners(XMPPContact contact, UpdateType updateType) {
    log.debug("updated " + contact + " - " + updateType);
    Optional<XMPPContact> optionalContact = Optional.ofNullable(contact);

    for (IContactsUpdate listener : updateListeners) {
      try {
        listener.update(optionalContact, updateType);
      } catch (RuntimeException e) {
        log.error("invoking listener: " + listener + " failed", e);
      }
    }
  }
}
