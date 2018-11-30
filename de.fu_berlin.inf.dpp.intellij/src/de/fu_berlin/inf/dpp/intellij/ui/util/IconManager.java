package de.fu_berlin.inf.dpp.intellij.ui.util;

import java.net.URL;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

/** Class caches all icons used in application */
public class IconManager {
  public static final Logger LOG = Logger.getLogger(IconManager.class);

  public static final ImageIcon SESSIONS_ICON =
      getIcon("/icons/famfamfam/session_tsk.png", "sessions");
  public static final ImageIcon CONTACT_ONLINE_ICON =
      getIcon("/icons/famfamfam/contact_saros_obj.png", "contactOnLine");
  public static final ImageIcon CONTACT_OFFLINE_ICON =
      getIcon("/icons/famfamfam/contact_offline_obj.png", "contactOffLine");
  public static final ImageIcon CONTACTS_ICON = getIcon("/icons/famfamfam/group.png", "contacts");

  public static final ImageIcon FOLLOW_ICON = getIcon("/icons/famfamfam/followmode.png", "follow");

  public static final ImageIcon IN_SYNC_ICON =
      getIcon("/icons/famfamfam/in_sync.png", "Files are consistent");
  public static final ImageIcon OUT_OF_SYNC_ICON =
      getIcon("/icons/famfamfam/out_sync.png", "Files are NOT consistent");

  /**
   * Creates icon by image path. Path must start with a slash and be relative the the src folder.
   */
  public static ImageIcon getIcon(String path, String description) {
    URL url = IconManager.class.getResource(path);
    if (url == null) {
      LOG.error("Could not load icon " + path + ". Path does not exist in resources: " + path);
    }

    return new ImageIcon(url, description);
  }
}
