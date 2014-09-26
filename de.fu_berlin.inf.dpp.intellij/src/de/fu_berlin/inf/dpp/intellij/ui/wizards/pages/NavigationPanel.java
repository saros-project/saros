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

import de.fu_berlin.inf.dpp.intellij.ui.wizards.Wizard;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

/**
 * Default navigation panel creates panel with 3 default buttons
 * back, next and cancel.
 */

public class NavigationPanel extends JPanel {
    public enum Position {
        FIRST, MIDDLE, LAST
    }

    public static final String NEXT_ACTION = "next";
    public static final String BACK_ACTION = "back";
    public static final String CANCEL_ACTION = "cancel";

    public static final String TITLE_NEXT = "Next>>>";
    public static final String TITLE_BACK = "<<<Back";
    public static final String TITLE_CANCEL = "Cancel";
    public static final String TITLE_FINISH = "Finish";

    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    public NavigationPanel() {
        backButton = new JButton(TITLE_BACK);
        nextButton = new JButton(TITLE_NEXT);
        cancelButton = new JButton(TITLE_CANCEL);
        create();
    }

    /**
     * Method creates panel UI.
     */
    private void create() {
        JPanel buttonPanel = this;
        Box buttonBox = new Box(BoxLayout.X_AXIS);

        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

        backButton.setActionCommand(BACK_ACTION);
        buttonBox.add(backButton);
        buttonBox.add(Box.createHorizontalStrut(10));

        nextButton.setActionCommand(NEXT_ACTION);
        buttonBox.add(nextButton);

        cancelButton.setActionCommand(CANCEL_ACTION);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(cancelButton);

        buttonPanel.add(buttonBox, BorderLayout.EAST);
    }

    /**
     * Adds action listener to all buttons.
     *
     * @param actionListener action listener
     */
    public void addActionListener(ActionListener actionListener) {
        backButton.addActionListener(actionListener);
        nextButton.addActionListener(actionListener);
        cancelButton.addActionListener(actionListener);
    }

    /**
     * Methods changes enable status of back and next buttons according to
     * position in the page list (see {@link Wizard.WizardPageModel}.
     *
     * @param position          page position in the page list
     * @param backButtonVisible
     * @param nextButtonVisible
     */
    public void setPosition(Position position, boolean backButtonVisible,
        boolean nextButtonVisible) {
        backButton.setVisible(false);
        nextButton.setVisible(false);

        switch (position) {
        case FIRST:
            backButton.setEnabled(false);

            nextButton.setEnabled(true && nextButtonVisible);
            nextButton.setVisible(nextButtonVisible);
            break;
        case MIDDLE:
            backButton.setEnabled(true && backButtonVisible);
            backButton.setVisible(backButtonVisible);

            nextButton.setEnabled(true && nextButtonVisible);
            nextButton.setVisible(nextButtonVisible);
            break;
        case LAST:
            nextButton.setEnabled(true && nextButtonVisible);
            nextButton.setVisible(nextButtonVisible);
            nextButton.setText(TITLE_FINISH);
            nextButton.repaint();
            break;
        default:
            backButton.setEnabled(false);
            nextButton.setEnabled(false);
        }
    }

    public void disableNextButton() {
        nextButton.setEnabled(false);
    }

    public void enableNextButton() {
        nextButton.setEnabled(true);
    }

    public void setNextButtonText(String nextButtonTitle) {
        nextButton.setText(nextButtonTitle);
    }
}
