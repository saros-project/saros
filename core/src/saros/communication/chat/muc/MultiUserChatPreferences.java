package de.fu_berlin.inf.dpp.communication.chat.muc;

/** Preferences for a {@link MultiUserChat} session. */
public final class MultiUserChatPreferences {

  private String service;
  private String roomName;
  private String password;
  private String roomAddress;

  /**
   * The name of the room in the form "roomName@service", where "service" is the domain at which the
   * multi-user chat service is running. Make sure to provide a valid JID.
   *
   * @param service the hostname for the multi-user chat service which may be <code>null</code> to
   *     indicate that no multi-user chat service was found
   * @param roomName the name of the room
   * @param password the password for the room
   */
  public MultiUserChatPreferences(String service, String roomName, String password) {
    this.service = service;
    this.roomName = roomName;
    this.password = password;
    this.roomAddress = service == null ? null : roomName + "@" + service;
  }

  /**
   * Returns the hostname for at which the multi-user chat service is running.
   *
   * @return the hostname or <code>null</code> if no service was provided
   */
  public String getService() {
    return service;
  }

  /**
   * Returns the room name.
   *
   * @return the room name
   */
  public String getRoomName() {
    return roomName;
  }

  /**
   * Returns the password for the room.
   *
   * @return the password for the room
   */
  public String getPassword() {
    return password;
  }

  /**
   * Returns the name of the room in the form "roomName@service", where "service" is the hostname at
   * which the multi-user chat service is running.
   *
   * @return the name of the room or <code>null</code> if no service was provided
   * @see org.jivesoftware.smackx.muc
   *     .MultiUserChat#MultiUserChat(org.jivesoftware.smack.Connection, String)
   */
  public String getRoom() {
    return roomAddress;
  }
}
