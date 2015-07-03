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

import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.FlowLayout;

/**
 * Class presents header panel for wizards.
 */
public class HeaderPanel extends JPanel {

    private Icon icon;

    private JTextArea textMain;
    private JLabel textTitle;
    private Color backColor = Color.WHITE;

    /**
     * Constructor creates header panel with given title and description
     *
     * @param title header title
     * @param text  header description
     */
    public HeaderPanel(String title, String text) {
        create(title, text);
    }

    /**
     * Method creates header panel with given title and description
     *
     * @param title header title
     * @param text  header description
     */
    private void create(String title, String text) {
        setLayout(new FlowLayout());

        icon = IconManager
            .getIcon("/icons/saros/invitation.png", "invitation");
        textMain = new JTextArea();
        textMain.setEditable(false);

        textTitle = new JLabel();
        String textConvertedToJLabelHTML = convertTextToJLabelHTML(title);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(backColor);

        textTitle.setText(textConvertedToJLabelHTML);
        titlePanel.add(textTitle);

        textMain.setText(text);
        textMain.setSize(500, 100);

        textMain.setWrapStyleWord(true);
        textMain.setLineWrap(true);

        JPanel textPanel = new JPanel();

        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.add(titlePanel);
        textPanel.add(textMain);
        textPanel.setBackground(backColor);

        add(textPanel);

        JLabel lblIcon = new JLabel();
        lblIcon.setIcon(icon);
        add(lblIcon);

        setBackground(backColor);
    }

    private String convertTextToJLabelHTML(String text) {
        return "<html>" + text.replace("\n", "<br>") + "</html>";
    }

    public String getTitle() {
        return textTitle.getText();
    }

    public void setTitle(String title) {
        this.textTitle.setText(title);
    }

    public String getText() {
        return textTitle.getText();
    }

    public void setText(String text) {
        this.textMain.setText(text);
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }
}
