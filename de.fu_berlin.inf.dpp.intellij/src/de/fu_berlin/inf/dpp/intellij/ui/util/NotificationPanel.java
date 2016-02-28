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
 *
 */

package de.fu_berlin.inf.dpp.intellij.ui.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

/**
 * Class uses IntelliJ API to show notifications
 */
public class NotificationPanel {
    private static final Logger LOG = Logger.getLogger(NotificationPanel.class);

    public static final String GROUP_NOTIFICATION_ID = "sarosNotification";
    public static final NotificationGroup GROUP_DISPLAY_ID_INFO = new NotificationGroup(
        GROUP_NOTIFICATION_ID, NotificationDisplayType.BALLOON, true);

    @Inject
    private static Project project;

    static {
        SarosPluginContext.initComponent(new NotificationPanel());
    }

    private NotificationPanel() {
    }

    /**
     * Dispaly the Notification.
     *
     * TODO: Move to core
     * TODO: Add different types of notification
     *
     * @param message
     *            content of the notification
     * @param title
     *            title of the notification.
     */
    public static void showNotification(String message, String title) {
        final Notification notification = GROUP_DISPLAY_ID_INFO
            .createNotification(title, message, NotificationType.INFORMATION,
                null);
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Notifications.Bus.notify(notification, project);
            }
        });

        LOG.info("Notification: " + title + ", " + message);
    }
}
