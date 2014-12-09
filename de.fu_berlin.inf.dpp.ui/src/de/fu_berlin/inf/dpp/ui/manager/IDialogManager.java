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

package de.fu_berlin.inf.dpp.ui.manager;

/**
 * This interface encapsulates functionality to show and close HTML-based
 * dialogs.
 * Those dialogs are displayed in a new window inside a browser.
 * The simultaneous display of multiple dialogs is supported.
 * However, there may one be one dialog for the same webpage open at the same time.
 */
public interface IDialogManager {

    /**
     * Shows a dialog displaying the given page.
     * For each page there may only be one open dialog window.
     * If this method is called when the dialog is already displayed,
     * nothing happens.
     *
     * @param startPage the relative path of the page inside the resource folder
     */
    public void showDialogWindow(String startPage);

    /**
     * Closes the dialog displaying the given page.
     *
     * @param startPage the relative path of the page inside the resource folder
     */
    public void closeDialogWindow(String startPage);
}
