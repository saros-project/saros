package saros.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import saros.Saros;

/** Handles references to all used images throughout this plug-in. */
public class ImageManager {

  /*
   * overlays
   */
  public static final ImageDescriptor OVERLAY_FOLLOWMODE =
      getImageDescriptor("icons/ovr16/followmode.png"); // $NON-NLS-1$
  public static final ImageDescriptor OVERLAY_READONLY =
      getImageDescriptor("icons/ovr16/readonly.png"); // $NON-NLS-1$
  public static final ImageDescriptor OVERLAY_AWAY =
      getImageDescriptor("icons/ovr16/away.png"); // $NON-NLS-1$

  /*
   * wizard banners
   */
  public static final ImageDescriptor WIZBAN_CONFIGURATION =
      getImageDescriptor("icons/wizban/configuration_wiz.gif"); // $NON-NLS-1$
  public static final ImageDescriptor WIZBAN_CREATE_XMPP_ACCOUNT =
      getImageDescriptor("icons/wizban/xmpp_create_account_wiz.gif"); // $NON-NLS-1$
  public static final ImageDescriptor WIZBAN_EDIT_XMPP_ACCOUNT =
      getImageDescriptor("icons/wizban/xmpp_edit_account_wiz.gif"); // $NON-NLS-1$
  public static final ImageDescriptor WIZBAN_ADD_CONTACT =
      getImageDescriptor("icons/wizban/add_contact_wiz.gif"); // $NON-NLS-1$
  public static final ImageDescriptor WIZBAN_SESSION_INCOMING =
      getImageDescriptor("icons/wizban/session_incoming_wiz.gif"); // $NON-NLS-1$
  public static final ImageDescriptor WIZBAN_SESSION_OUTGOING =
      getImageDescriptor("icons/wizban/session_outgoing_wiz.gif"); // $NON-NLS-1$
  public static final ImageDescriptor WIZBAN_SESSION_ADD_PROJECTS =
      getImageDescriptor("icons/wizban/session_add_projects_wiz.gif"); // $NON-NLS-1$
  public static final ImageDescriptor WIZBAN_SESSION_ADD_CONTACTS =
      getImageDescriptor("icons/wizban/session_add_contacts_wiz.gif"); // $NON-NLS-1$

  /*
   * tool bar
   */
  public static final Image ETOOL_STATISTIC =
      getImage("icons/etool16/statistic_misc.png"); // $NON-NLS-1$
  public static final Image DTOOL_STATISTIC =
      getImage("icons/dtool16/statistic_misc.png"); // $NON-NLS-1$
  public static final Image ETOOL_CRASH_REPORT =
      getImage("icons/etool16/crash_report_misc.png"); // $NON-NLS-1$
  public static final Image DTOOL_CRASH_REPORT =
      getImage("icons/dtool16/crash_report_misc.png"); // $NON-NLS-1$
  public static final Image ETOOL_NEW_PROJECT =
      getImage("icons/etool16/new_project.gif"); // $NON-NLS-1$
  public static final ImageDescriptor ETOOL_EDIT =
      getImageDescriptor("icons/etool16/edit.gif"); // $NON-NLS-1$
  public static final ImageDescriptor ETOOL_TEST_CONNECTION =
      getImageDescriptor("icons/etool16/test_con.gif"); // $NON-NLS-1$

  /*
   * local tool bar
   */
  public static final Image ELCL_SPACER = getImage("icons/elcl16/spacer.png"); // $NON-NLS-1$
  public static final Image DLCL_SPACER = getImage("icons/dlcl16/spacer.png"); // $NON-NLS-1$
  public static final Image ELCL_PREFERENCES_OPEN =
      getImage("icons/elcl16/preferences_open_tsk.png"); // $NON-NLS-1$
  public static final Image ELCL_XMPP_CONNECTED =
      getImage("icons/elcl16/xmpp_disconnect_tsk.png"); // $NON-NLS-1$
  public static final Image DLCL_XMPP_CONNECTED =
      getImage("icons/dlcl16/xmpp_disconnect_tsk.png"); // $NON-NLS-1$
  public static final Image ELCL_CONTACT_SKYPE_CALL =
      getImage("icons/elcl16/contact_skype_call_tsk.png"); // $NON-NLS-1$
  public static final Image DLCL_CONTACT_SKYPE_CALL =
      getImage("icons/dlcl16/contact_skype_call_tsk.png"); // $NON-NLS-1$
  public static final Image ELCL_CONTACT_ADD =
      getImage("icons/elcl16/contact_add_tsk.png"); // $NON-NLS-1$
  public static final Image DLCL_CONTACT_ADD =
      getImage("icons/dlcl16/contact_add_tsk.png"); // $NON-NLS-1$
  public static final Image ELCL_SESSION = getImage("icons/elcl16/session_tsk.png"); // $NON-NLS-1$
  public static final Image DLCL_SESSION = getImage("icons/dlcl16/session_tsk.png"); // $NON-NLS-1$
  public static final Image ELCL_SESSION_LEAVE =
      getImage("icons/elcl16/session_leave_tsk.png"); // $NON-NLS-1$
  public static final Image DLCL_SESSION_LEAVE =
      getImage("icons/dlcl16/session_leave_tsk.png"); // $NON-NLS-1$
  public static final Image ELCL_SESSION_TERMINATE =
      getImage("icons/elcl16/session_terminate_tsk.png"); // $NON-NLS-1$
  public static final Image DLCL_SESSION_TERMINATE =
      getImage("icons/dlcl16/session_terminate_tsk.png"); // $NON-NLS-1$
  public static final Image ELCL_SESSION_ADD_PROJECTS =
      getImage("icons/elcl16/session_add_projects_tsk.png"); // $NON-NLS-1$
  public static final Image DLCL_SESSION_ADD_PROJECTS =
      getImage("icons/dlcl16/session_add_projects_tsk.png"); // $NON-NLS-1$
  public static final Image ELCL_SESSION_ADD_CONTACTS =
      getImage("icons/elcl16/session_add_contacts_tsk.png"); // $NON-NLS-1$
  public static final Image DLCL_SESSION_ADD_CONTACTS =
      getImage("icons/dlcl16/session_add_contacts_tsk.png"); // $NON-NLS-1$
  public static final Image ELCL_SAROS_SESSION_STOP_PROCESS =
      getImage("icons/elcl16/saros_session_stop_process_tsk.png"); // $NON-NLS-1$
  public static final Image DLCL_SAROS_SESSION_STOP_PROCESS =
      getImage("icons/dlcl16/saros_session_stop_process_tsk.png"); // $NON-NLS-1$
  public static final Image ELCL_DIALOG = getImage("icons/elcl16/dialog.gif"); // $NON-NLS-1$
  public static final Image ELCL_DELETE = getImage("icons/btn/deleteaccount.png"); // $NON-NLS-1$
  public static final ImageDescriptor ELCL_OPEN_PREFERENCES =
      getImageDescriptor("icons/elcl16/preferences_open_tsk.png"); // $NON-NLS-1$

  /*
   * objects
   */
  public static final Image ICON_UPNP = getImage("icons/obj16/upnp_obj.png"); // $NON-NLS-1$

  public static final Image ICON_GROUP = getImage("icons/obj16/group_obj.png"); // $NON-NLS-1$
  public static final Image ICON_CONTACT = getImage("icons/obj16/contact_obj.png"); // $NON-NLS-1$
  public static final Image ICON_CONTACT_OFFLINE =
      getImage("icons/obj16/contact_offline_obj.png"); // $NON-NLS-1$
  public static final Image ICON_CONTACT_AWAY =
      new DecorationOverlayIcon(ICON_CONTACT, OVERLAY_AWAY, IDecoration.TOP_RIGHT).createImage();

  public static final Image ICON_CONTACT_SAROS_SUPPORT =
      getImage("icons/obj16/contact_saros_obj.png"); // $NON-NLS-1$

  public static final Image ICON_USER_SAROS_FOLLOWMODE =
      new DecorationOverlayIcon(
              ICON_CONTACT_SAROS_SUPPORT, OVERLAY_FOLLOWMODE, IDecoration.TOP_LEFT)
          .createImage();

  public static final Image ICON_USER_SAROS_FOLLOWMODE_DISABLED =
      new DecorationOverlayIcon(ICON_CONTACT_OFFLINE, OVERLAY_FOLLOWMODE, IDecoration.TOP_LEFT)
          .createImage();

  public static final Image ICON_USER_SAROS_FOLLOWMODE_READONLY =
      new DecorationOverlayIcon(
              ICON_USER_SAROS_FOLLOWMODE, OVERLAY_READONLY, IDecoration.BOTTOM_RIGHT)
          .createImage();

  public static final Image ICON_USER_SAROS_FOLLOWMODE_READONLY_AWAY =
      new DecorationOverlayIcon(
              ICON_USER_SAROS_FOLLOWMODE_READONLY, OVERLAY_AWAY, IDecoration.TOP_RIGHT)
          .createImage();

  public static final Image ICON_USER_SAROS_FOLLOWMODE_AWAY =
      new DecorationOverlayIcon(ICON_USER_SAROS_FOLLOWMODE, OVERLAY_AWAY, IDecoration.TOP_RIGHT)
          .createImage();

  public static final Image ICON_USER_SAROS_READONLY =
      new DecorationOverlayIcon(
              ICON_CONTACT_SAROS_SUPPORT, OVERLAY_READONLY, IDecoration.BOTTOM_RIGHT)
          .createImage();

  public static final Image ICON_USER_SAROS_READONLY_AWAY =
      new DecorationOverlayIcon(ICON_USER_SAROS_READONLY, OVERLAY_AWAY, IDecoration.TOP_RIGHT)
          .createImage();

  public static final Image ICON_USER_SAROS_AWAY =
      new DecorationOverlayIcon(ICON_CONTACT_SAROS_SUPPORT, OVERLAY_AWAY, IDecoration.TOP_RIGHT)
          .createImage();

  /**
   * Returns an image from the file at the given plug-in relative path.
   *
   * @param path
   * @return image; the returned image <b>MUST be disposed after usage</b> to free up memory
   */
  public static Image getImage(String path) {
    return new Image(Display.getDefault(), getImageDescriptor(path).getImageData());
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in relative path.
   *
   * @param path the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(Saros.PLUGIN_ID, path);
  }
}
