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
     * <p/>
     * TODO: Move to core
     * TODO: Add different types of notification
     *
     * @param message content of the notification
     * @param title   title of the notification.
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
