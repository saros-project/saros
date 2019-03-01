package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;

/** Utility class for retrieving Format information on a User. */
public class UserFormatUtils {

  private UserFormatUtils() {}

  /**
   * Retrieves a user's nickname from the XMPP roster. If none is present it returns the base name.
   *
   * @param user
   * @return the user's nickname, or if none is set JID's base.
   */
  public static String getDisplayName(User user) {
    JID jid = user.getJID();
    return XMPPUtils.getNickname(null, jid, jid.getBase());
  }
}
