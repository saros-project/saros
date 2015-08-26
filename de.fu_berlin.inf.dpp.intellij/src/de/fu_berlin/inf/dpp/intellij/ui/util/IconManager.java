/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.ui.util;

import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import java.net.URL;

/**
 * Class caches all icons used in application
 */
public class IconManager {
    public static final Logger LOG = Logger.getLogger(IconManager.class);

    public static final ImageIcon SESSIONS_ICON = getIcon(
        "/icons/famfamfam/session_tsk.png", "sessions");
    public static final ImageIcon CONTACT_ONLINE_ICON = getIcon(
        "/icons/famfamfam/buddy_saros_obj.png", "contactOnLine");
    public static final ImageIcon CONTACT_OFFLINE_ICON = getIcon(
        "/icons/famfamfam/buddy_offline_obj.png", "contactOffLine");
    public static final ImageIcon CONTACTS_ICON = getIcon(
        "/icons/famfamfam/group.png", "contacts");

    public static final ImageIcon FOLLOW_ICON = getIcon(
        "/icons/famfamfam/followmode.png", "follow");

    public static final ImageIcon IN_SYNC_ICON = getIcon(
        "/icons/famfamfam/in_sync.png", "Files are consistent");
    public static final ImageIcon OUT_OF_SYNC_ICON = getIcon(
        "/icons/famfamfam/out_sync.png", "Files are NOT consistent");

    /**
     * Creates icon by image path. Path must start with a slash and be
     * relative the the src folder.
     */
    public static ImageIcon getIcon(String path, String description) {
        URL url = IconManager.class.getResource(path);
        if (url == null) {
            LOG.error("Could not load icon " + path
                + ". Path does not exist in resources: " + path);
        }

        return new ImageIcon(url, description);
    }

}
