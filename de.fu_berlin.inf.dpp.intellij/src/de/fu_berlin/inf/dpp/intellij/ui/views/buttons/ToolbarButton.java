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

package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import com.intellij.util.ui.UIUtil;
import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.net.URL;

/**
 * Common class for Toolbar button implementations.
 */
public abstract class ToolbarButton extends JButton {

    private static final Logger LOG = Logger.getLogger(ToolbarButton.class);

    /**
     * Creates a button with the specified actionCommand, Icon and toolTipText.
     */
    protected ToolbarButton(String actionCommand, String tooltipText,
        String iconPath, String altText) {
        setActionCommand(actionCommand);
        setIcon(iconPath, altText);
        setToolTipText(tooltipText);
    }

    /**
     * Tries to load the icon from the specified path and sets only the altText
     * if the loading fails.
     */
    protected void setIcon(String path, String altText) {
        URL imageURL = ToolbarButton.class.getClassLoader().getResource(path);
        if (imageURL != null) {
            setIcon(new ImageIcon(imageURL, altText));
        } else {
            setText(altText);
            LOG.error("Resource not found: " + path);
        }
    }

    /**
     * calls {@link #setEnabled(boolean)} from the UI thread.
     */
    protected void setEnabledFromUIThread(final boolean enabled) {
        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
                setEnabled(enabled);
            }
        });
    }

}
