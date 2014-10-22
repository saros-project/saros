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

import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import org.picocontainer.annotations.Inject;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Container;

/**
 * Dialog message helper that starts Dialogs in the current Thread.
 */
public class DialogUtils {

    @Inject
    private static Saros saros;

    private static final Container CONTAINER;

    private DialogUtils() {
    }

    static {
        SarosPluginContext.initComponent(new DialogUtils());
        CONTAINER = saros.getMainPanel();
    }

    public static void showError(Component parent, String title, String msg) {
        JOptionPane.showInternalMessageDialog(parent, msg, title,
            JOptionPane.ERROR_MESSAGE);
    }

    public static void showError(String title, String msg) {
        showError(getDefaultContainer(), msg, title);
    }

    public static void showWarning(Component parent, String title, String msg) {
        JOptionPane.showInternalMessageDialog(parent, msg, title,
            JOptionPane.WARNING_MESSAGE);
    }

    public static void showWarning(String title, String msg) {
        showWarning(getDefaultContainer(), msg, title);
    }

    public static boolean showConfirm(Component parent, String title,
        String msg) {
        int resp = JOptionPane.showConfirmDialog(parent, msg, title,
            JOptionPane.OK_CANCEL_OPTION);
        return resp == JOptionPane.OK_OPTION;
    }

    public static boolean showConfirm(String title, String msg) {
        return showConfirm(getDefaultContainer(), msg, title);
    }

    public static boolean showQuestion(Component parent, String title,
        String msg) {
        int answer = JOptionPane
            .showConfirmDialog(parent, msg, title, JOptionPane.YES_NO_OPTION);

        return answer == JOptionPane.YES_OPTION;
    }

    public static boolean showQuestion(String title, String msg) {
        return showQuestion(getDefaultContainer(), msg, title);
    }

    public static void showInfo(Container parent, String title, String msg) {
        JOptionPane.showMessageDialog(parent, msg, title,
            JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showInfo(String title, String msg) {
        showInfo(getDefaultContainer(), msg, title);
    }

    public static Container getDefaultContainer() {
        return CONTAINER;
    }
}
