package de.fu_berlin.inf.dpp.intellij.ui.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import java.awt.Component;

/**
 * Dialog helper used to show messages in safe manner by starting it in UI thread.
 */
public class SafeDialogUtils {
    private static final Logger LOG = Logger.getLogger(SafeDialogUtils.class);

    @Inject
    private static Project project;

    static {
        SarosPluginContext.initComponent(new SafeDialogUtils());
    }

    private SafeDialogUtils() {
    }

    /**
     * Shows an input dialog in the UI thread.
     */
    public static String showInputDialog(final String message,
        final String initialValue, final String title) {

        LOG.info("Showing input dialog: " + title + " - " + message + " - " +
            initialValue);

        final StringBuilder response = new StringBuilder();

        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
                String option = Messages
                    .showInputDialog(project, message, title,
                        Messages.getQuestionIcon(), initialValue, null);
                if (option != null) {
                    response.append(option);
                }
            }
        });

        return response.toString();
    }

    public static void showWarning(final String message, final String title) {
        LOG.info("Showing warning dialog: " + title + " - " + message);

        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
                Messages.showWarningDialog(project, message, title);
            }
        });
    }

    public static void showError(final String message, final String title) {
        LOG.info("Showing error dialog: " + title + " - " + message);

        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
                Messages.showErrorDialog(project, message, title);
            }
        });
    }

    public static void showError(final Component component,
        final String message, final String title) {

        LOG.info("Showing error dialog: " + title + " - " + message);

        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
                Messages.showErrorDialog(component, message, title);
            }
        });
    }
}
