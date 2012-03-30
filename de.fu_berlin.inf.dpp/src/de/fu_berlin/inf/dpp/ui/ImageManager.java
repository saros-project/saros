package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.fu_berlin.inf.dpp.Saros;

/**
 * Handles references to all used images throughout this plug-in.
 */
public class ImageManager {

    /*
     * overlays
     */
    public static final ImageDescriptor OVERLAY_FOLLOWMODE = getImageDescriptor("icons/ovr16/followmode.png"); //$NON-NLS-1$
    public static final ImageDescriptor OVERLAY_READONLY = getImageDescriptor("icons/ovr16/readonly.png"); //$NON-NLS-1$
    public static final ImageDescriptor OVERLAY_AWAY = getImageDescriptor("icons/ovr16/away.png"); //$NON-NLS-1$

    /*
     * wizard banners
     */
    public static final ImageDescriptor WIZBAN_CONFIGURATION = getImageDescriptor("icons/wizban/configuration_wiz.gif"); //$NON-NLS-1$
    public static final ImageDescriptor WIZBAN_CREATE_XMPP_ACCOUNT = getImageDescriptor("icons/wizban/xmpp_create_account_wiz.gif"); //$NON-NLS-1$
    public static final ImageDescriptor WIZBAN_EDIT_XMPP_ACCOUNT = getImageDescriptor("icons/wizban/xmpp_edit_account_wiz.gif"); //$NON-NLS-1$
    public static final ImageDescriptor WIZBAN_ADD_BUDDY = getImageDescriptor("icons/wizban/add_buddy_wiz.gif"); //$NON-NLS-1$
    public static final ImageDescriptor WIZBAN_SHARE_PROJECT_OUTGOING = getImageDescriptor("icons/wizban/share_project_outgoing_wiz.gif"); //$NON-NLS-1$
    public static final ImageDescriptor WIZBAN_SHARE_PROJECT_ADD_PROJECTS = getImageDescriptor("icons/wizban/share_project_add_projects_wiz.gif"); //$NON-NLS-1$
    public static final ImageDescriptor WIZBAN_SHARE_PROJECT_ADD_BUDDIES = getImageDescriptor("icons/wizban/share_project_add_buddies_wiz.gif"); //$NON-NLS-1$
    public static final ImageDescriptor WIZBAN_SHARE_PROJECT_INCOMING = getImageDescriptor("icons/wizban/share_project_incoming_wiz.gif"); //$NON-NLS-1$

    /*
     * tool bar
     */
    public static final Image ETOOL_STATISTIC = getImage("icons/etool16/statistic_misc.png"); //$NON-NLS-1$
    public static final Image DTOOL_STATISTIC = getImage("icons/dtool16/statistic_misc.png"); //$NON-NLS-1$
    public static final Image ETOOL_CRASH_REPORT = getImage("icons/etool16/crash_report_misc.png"); //$NON-NLS-1$
    public static final Image DTOOL_CRASH_REPORT = getImage("icons/dtool16/crash_report_misc.png"); //$NON-NLS-1$
    public static final Image ETOOL_NEW_PROJECT = getImage("icons/etool16/new_project.gif"); //$NON-NLS-1$
    public static final ImageDescriptor ETOOL_EDIT = getImageDescriptor("icons/etool16/edit.gif"); //$NON-NLS-1$
    public static final ImageDescriptor ETOOL_TEST_CONNECTION = getImageDescriptor("icons/etool16/test_con.gif"); //$NON-NLS-1$

    /*
     * local tool bar
     */
    public static final Image ELCL_SPACER = getImage("icons/elcl16/spacer.png"); //$NON-NLS-1$
    public static final Image DLCL_SPACER = getImage("icons/dlcl16/spacer.png"); //$NON-NLS-1$
    public static final Image ELCL_PREFERENCES_OPEN = getImage("icons/elcl16/preferences_open_tsk.png"); //$NON-NLS-1$
    public static final Image ELCL_XMPP_CONNECTED = getImage("icons/elcl16/xmpp_disconnect_tsk.png"); //$NON-NLS-1$
    public static final Image DLCL_XMPP_CONNECTED = getImage("icons/dlcl16/xmpp_disconnect_tsk.png"); //$NON-NLS-1$
    public static final Image ELCL_BUDDY_SKYPE_CALL = getImage("icons/elcl16/buddy_skype_call_tsk.png"); //$NON-NLS-1$
    public static final Image DLCL_BUDDY_SKYPE_CALL = getImage("icons/dlcl16/buddy_skype_call_tsk.png"); //$NON-NLS-1$
    public static final Image ELCL_BUDDY_ADD = getImage("icons/elcl16/buddy_add_tsk.png"); //$NON-NLS-1$
    public static final Image DLCL_BUDDY_ADD = getImage("icons/dlcl16/buddy_add_tsk.png"); //$NON-NLS-1$
    public static final Image ELCL_PROJECT_SHARE = getImage("icons/elcl16/project_share_tsk.png"); //$NON-NLS-1$
    public static final Image DLCL_PROJECT_SHARE = getImage("icons/dlcl16/project_share_tsk.png"); //$NON-NLS-1$
    public static final Image ELCL_PROJECT_SHARE_LEAVE = getImage("icons/elcl16/project_share_leave_tsk.png"); //$NON-NLS-1$
    public static final Image DLCL_PROJECT_SHARE_LEAVE = getImage("icons/dlcl16/project_share_leave_tsk.png"); //$NON-NLS-1$
    public static final Image ELCL_PROJECT_SHARE_TERMINATE = getImage("icons/elcl16/project_share_terminate_tsk.png"); //$NON-NLS-1$
    public static final Image DLCL_PROJECT_SHARE_TERMINATE = getImage("icons/dlcl16/project_share_terminate_tsk.png"); //$NON-NLS-1$
    public static final Image ELCL_PROJECT_SHARE_ADD_PROJECTS = getImage("icons/elcl16/project_share_add_projects_tsk.png"); //$NON-NLS-1$
    public static final Image DLCL_PROJECT_SHARE_ADD_PROJECTS = getImage("icons/dlcl16/project_share_add_projects_tsk.png"); //$NON-NLS-1$
    public static final Image ELCL_PROJECT_SHARE_ADD_BUDDIES = getImage("icons/elcl16/project_share_add_buddies_tsk.png"); //$NON-NLS-1$
    public static final Image DLCL_PROJECT_SHARE_ADD_BUDDIES = getImage("icons/dlcl16/project_share_add_buddies_tsk.png"); //$NON-NLS-1$
    public static final Image ELCL_SAROS_SESSION_STOP_PROCESS = getImage("icons/elcl16/saros_session_stop_process_tsk.png"); //$NON-NLS-1$
    public static final Image DLCL_SAROS_SESSION_STOP_PROCESS = getImage("icons/dlcl16/saros_session_stop_process_tsk.png"); //$NON-NLS-1$
    public static final Image ELCL_DELETE = getImage("icons/btn/deleteaccount.png"); //$NON-NLS-1$

    /*
     * objects
     */
    public static final Image ICON_GROUP = getImage("icons/obj16/group_obj.png"); //$NON-NLS-1$
    public static final Image ICON_BUDDY = getImage("icons/obj16/buddy_obj.png"); //$NON-NLS-1$
    public static final Image ICON_BUDDY_OFFLINE = getImage("icons/obj16/buddy_offline_obj.png"); //$NON-NLS-1$
    public static final Image ICON_BUDDY_AWAY = new DecorationOverlayIcon(
        ICON_BUDDY, OVERLAY_AWAY, IDecoration.TOP_RIGHT).createImage();
    public static final Image ICON_BUDDY_SAROS = getImage("icons/obj16/buddy_saros_obj.png"); //$NON-NLS-1$
    public static Image ICON_UPNP = getImage("icons/obj16/upnp_obj.png"); //$NON-NLS-1$

    public static final Image ICON_BUDDY_SAROS_FOLLOWMODE = new DecorationOverlayIcon(
        ICON_BUDDY_SAROS, OVERLAY_FOLLOWMODE, IDecoration.TOP_LEFT)
        .createImage();
    public static final Image ICON_BUDDY_SAROS_FOLLOWMODE_READONLY = new DecorationOverlayIcon(
        ICON_BUDDY_SAROS_FOLLOWMODE, OVERLAY_READONLY, IDecoration.BOTTOM_RIGHT)
        .createImage();
    public static final Image ICON_BUDDY_SAROS_FOLLOWMODE_READONLY_AWAY = new DecorationOverlayIcon(
        ICON_BUDDY_SAROS_FOLLOWMODE_READONLY, OVERLAY_AWAY,
        IDecoration.TOP_RIGHT).createImage();
    public static final Image ICON_BUDDY_SAROS_FOLLOWMODE_AWAY = new DecorationOverlayIcon(
        ICON_BUDDY_SAROS_FOLLOWMODE, OVERLAY_AWAY, IDecoration.TOP_RIGHT)
        .createImage();

    public static final Image ICON_BUDDY_SAROS_READONLY = new DecorationOverlayIcon(
        ICON_BUDDY_SAROS, OVERLAY_READONLY, IDecoration.BOTTOM_RIGHT)
        .createImage();
    public static final Image ICON_BUDDY_SAROS_READONLY_AWAY = new DecorationOverlayIcon(
        ICON_BUDDY_SAROS_READONLY, OVERLAY_AWAY, IDecoration.TOP_RIGHT)
        .createImage();

    public static final Image ICON_BUDDY_SAROS_AWAY = new DecorationOverlayIcon(
        ICON_BUDDY_SAROS, OVERLAY_AWAY, IDecoration.TOP_RIGHT).createImage();

    /**
     * Returns an image from the file at the given plug-in relative path.
     * 
     * @param path
     * @return image; the returned image <b>MUST be disposed after usage</b> to
     *         free up memory
     */
    public static Image getImage(String path) {
        return new Image(Display.getDefault(), getImageDescriptor(path)
            .getImageData());
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path.
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(Saros.SAROS, path);
    }

}
