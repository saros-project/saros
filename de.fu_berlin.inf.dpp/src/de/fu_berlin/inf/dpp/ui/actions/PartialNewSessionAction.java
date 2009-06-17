/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.ui.PartialProjectSelectionDialog;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Start to share a project (a "session").
 * 
 * This action is created by Eclipse!
 * 
 * @author rdjemili
 * 
 * 
 * 
 */
@Component(module = "action")
public class PartialNewSessionAction extends GeneralNewSessionAction {

    private static final Logger log = Logger
        .getLogger(PartialNewSessionAction.class.getName());

    /**
     * @review runSafe OK
     */
    public void run(IAction action) {
        final PartialProjectSelectionDialog dialog = new PartialProjectSelectionDialog(
            this.selectedProject);

        if (dialog.open() == Dialog.OK) {
            Util.runSafeSync(log, new Runnable() {
                public void run() {
                    runNewSession(dialog.getSelectedResources());
                }
            });
        }
    }
}
