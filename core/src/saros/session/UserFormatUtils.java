package saros.session;

import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;

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
