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

package de.fu_berlin.inf.dpp.intellij.ui.widgets.progress;

import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Creates independent progress monitor window
 */
//todo: use de.fu_berlin.inf.dpp.monitoring.IProgressMonitor in all IntelliJ classes
public class ProgressFrame extends MonitorProgressBar
    implements IProgressMonitor {

    public static final String TITLE = "Progress monitor";
    public static final String BUTTON_CANCEL = "Cancel";

    //todo: replace when Saros class will be in repository
    private Container parent = null; // Saros.instance().getMainPanel();

    private JFrame frmMain;
    private JButton btnCancel;

    /**
     * Constructor with default title
     */
    public ProgressFrame() {
        this(TITLE);
    }

    /**
     * Constructor with explicit title
     *
     * @param title dialog title
     */
    public ProgressFrame(String title) {
        frmMain = new JFrame(title);
        frmMain.setSize(300, 160);
        frmMain.setLocationRelativeTo(parent);

        Container pane = frmMain.getContentPane();
        pane.setLayout(null);

        frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        btnCancel = new JButton(BUTTON_CANCEL);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setCanceled(true);
            }
        });

        JLabel infoLabel = display.getInfoLabel();

        pane.add(infoLabel);
        pane.add(btnCancel);

        JProgressBar progressBar = display.getProgressBar();
        pane.add(progressBar);

        infoLabel.setBounds(10, 15, 200, 15);
        progressBar.setBounds(10, 50, 280, 20);
        btnCancel.setBounds(100, 85, 100, 25);

        frmMain.setResizable(false);
        frmMain.setVisible(true);

        this.frmMain.repaint(); //todo: test if this is necessary at all

    }

    @Override
    public void done() {
        super.done();
        frmMain.dispose();
    }

}
