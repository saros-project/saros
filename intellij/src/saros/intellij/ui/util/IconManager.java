package saros.intellij.ui.util;

import java.net.URL;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

/** Class caches all icons used in application */
public class IconManager {
  public static final Logger log = Logger.getLogger(IconManager.class);

  public static final ImageIcon SAROS_ICON = getIcon("/icons/saros/saros_misc.png", "Saros Icon");

  public static final ImageIcon SESSIONS_ICON =
      getIcon("/icons/famfamfam/session_tsk.png", "sessions");
  public static final ImageIcon CONTACT_ONLINE_ICON =
      getIcon("/icons/famfamfam/contact_saros_obj.png", "contactOnLine");
  public static final ImageIcon CONTACT_OFFLINE_ICON =
      getIcon("/icons/famfamfam/contact_offline_obj.png", "contactOffLine");
  public static final ImageIcon CONTACTS_ICON = getIcon("/icons/famfamfam/group.png", "contacts");

  public static final ImageIcon FOLLOW_ICON = getIcon("/icons/famfamfam/followmode.png", "follow");

  public static final ImageIcon IN_SYNC_ICON =
      getIcon("/icons/etool16/in_sync.png", "Files are consistent");
  public static final ImageIcon OUT_OF_SYNC_ICON =
      getIcon("/icons/etool16/out_sync.png", "Files are NOT consistent");

  public static final ImageIcon CONNECT_ICON =
      getIcon("/icons/famfamfam/connect.png", "Connect to XMPP/Jabber server");

  public static final ImageIcon LEAVE_SESSION_ICON =
      getIcon("/icons/famfamfam/session_leave_tsk.png", "leave");

  public static final ImageIcon TERMINATE_SESSION_ICON =
      getIcon("/icons/elcl16/session_terminate_tsk.png", "terminate session");

  public static final ImageIcon ADD_CONTACT_ICON =
      getIcon("/icons/famfamfam/contact_add_tsk.png", "addContact");
  public static final ImageIcon OPEN_PREFERENCES_ICON =
      getIcon("/icons/famfamfam/test_con.gif", "preferences");

  public static final ImageIcon ADD_USER_TO_SESSION =
      getIcon("/icons/elcl16/session_add_contacts_tsk.png", "add user to session");

  public static final ImageIcon REMOVE_USER_FROM_SESSION =
      getIcon("/icons/elcl16/contact_remove_tsk.png", "remove user from session");

  public static final ImageIcon SESSION_INVITATION_ICON =
      getIcon("/icons/saros/invitation.png", "invitation");

  /**
   * Creates icon by image path. Path must start with a slash and be relative the the src folder.
   */
  private static ImageIcon getIcon(String path, String description) {
    URL url = IconManager.class.getResource(path);
    if (url == null) {
      log.error("Could not load icon " + path + ". Path does not exist in resources: " + path);
    }

    return new ImageIcon(url, description);
  }
}
