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

package de.fu_berlin.inf.dpp.intellij.ui.wizards.pages;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Wizard page for displaying text information.
 * Usage:
 * <p/>
 * InfoPage page = new InfoPage("New session wizard", "Accept", actionListener);
 * <p/>
 * infoPage.addText("This is a lengthy info text");
 * infoPage.addText("spanning several text areas");
 */
public class InfoPage extends AbstractWizardPage {
    private String nextButtonTitle = "Accept";
    private JPanel infoPanel;

    public InfoPage(String id, String nextButtonTitle,
        PageActionListener pageListener) {
        super(id, pageListener);
        this.nextButtonTitle = nextButtonTitle;
        create();
    }

    private void create() {
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        add(infoPanel);
    }

    /**
     * Adds text paragraph
     *
     * @param text
     */
    public void addText(String text) {
        JTextArea textItem = new JTextArea(text);
        textItem.setLineWrap(true);
        textItem.setWrapStyleWord(true);
        textItem.setEditable(false);
        textItem.setBackground(infoPanel.getBackground());
        textItem.setPreferredSize(new Dimension(560, 30));

        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        itemPanel.add(textItem);

        infoPanel.add(itemPanel);
    }

    @Override
    public String getNextButtonTitle() {
        return nextButtonTitle;
    }
}
