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

import com.intellij.ui.components.JBScrollPane;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultEditorKit;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 * Wizard page with text area for displaying multiple lines of info.
 */
public class TextAreaPage extends AbstractWizardPage {
    private JTextArea display;
    private String title = "";
    private Color fontColor = Color.BLACK;

    /**
     * Constructor with custom ID
     *
     * @param fileListPageId
     * @param title          identifier
     * @param pageListener
     */
    public TextAreaPage(String fileListPageId, String title,
        PageActionListener pageListener) {
        super(fileListPageId, pageListener);
        this.title = title;
        create();
    }

    private void create() {
        setLayout(new BorderLayout());

        JPanel middlePanel = new JPanel();
        middlePanel.setBorder(new TitledBorder(new EtchedBorder(), title));

        display = new JTextArea(10, 48);
        display.getDocument()
            .putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        display.setEditable(false);
        display.setForeground(fontColor);

        JScrollPane scroll = new JBScrollPane(display);
        scroll.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        middlePanel.add(scroll);

        add(middlePanel, BorderLayout.CENTER);

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
    }

    /**
     * Adds text paragraph
     *
     * @param text
     */
    public void addLine(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                display.append(text + "\n");
            }
        });
    }

    @Override
    public boolean isBackButtonEnabled() {
        return false;
    }

    @Override
    public boolean isNextButtonEnabled() {
        return true;
    }
}
