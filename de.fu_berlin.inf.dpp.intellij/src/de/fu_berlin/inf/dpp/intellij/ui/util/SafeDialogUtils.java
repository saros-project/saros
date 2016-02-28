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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import org.picocontainer.annotations.Inject;

import java.awt.Component;

/**
 * Dialog helper used to show messages in safe manner by starting it in UI thread.
 */
public class SafeDialogUtils {
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
        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
                Messages.showWarningDialog(project, message, title);
            }
        });
    }

    public static void showError(final String message, final String title) {
        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
                Messages.showErrorDialog(project, message, title);
            }
        });
    }

    public static void showError(final Component component,
        final String message, final String title) {
        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
                Messages.showErrorDialog(component, message, title);
            }
        });
    }
}
