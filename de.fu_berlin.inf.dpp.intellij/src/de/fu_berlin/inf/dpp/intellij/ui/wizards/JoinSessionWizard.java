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

package de.fu_berlin.inf.dpp.intellij.ui.wizards;

import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.util.JobWithStatus;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.HeaderPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.InfoPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.PageActionListener;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.negotiation.ProcessTools.CancelLocation;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

import static de.fu_berlin.inf.dpp.negotiation.ProcessTools.CancelOption;

/**
 * A wizard that guides the user through an incoming invitation process.
 * <p/>
 * FIXME:
 *  Long-Running Operation after each step
 *  cancellation by a remote party
 *  auto-advance.
 */
public class JoinSessionWizard extends Wizard {
    public static final String PAGE_INFO_ID = "JoinSessionInfo";

    private static final Logger LOG = Logger.getLogger(JoinSessionWizard.class);

    private final IncomingSessionNegotiation process;

    private final PageActionListener actionListener = new PageActionListener() {
        @Override
        public void back() {

        }

        @Override
        public void next() {
            joinSession();
        }

        @Override
        public void cancel() {
            performCancel();
        }
    };

    /**
     * Creates wizard UI
     *
     * @param process The negotiation this wizard displays
     */
    public JoinSessionWizard(IncomingSessionNegotiation process) {
        super(Messages.JoinSessionWizard_title, new HeaderPanel(
            Messages.ShowDescriptionPage_title2,
            Messages.ShowDescriptionPage_description));
        this.process = process;

        InfoPage infoPage = createInfoPage(process);

        registerPage(infoPage);

        create();
    }

    private InfoPage createInfoPage(IncomingSessionNegotiation process) {
        InfoPage infoPage = new InfoPage(PAGE_INFO_ID,
            Messages.JoinSessionWizard_accept, actionListener);
        infoPage.addText(process.getPeer().getName() + " "
            + Messages.JoinSessionWizard_info);
        infoPage.addText(process.getDescription());
        return infoPage;
    }

    /**
     * Runs {@link IncomingSessionNegotiation#accept(IProgressMonitor)} with
     * {@link #runTask(Runnable, String)}. If the result is a cancel or
     * error status, it displays an error message accordingly.
     */
    public void joinSession() {

        JobWithStatus job = new JobWithStatus() {
            @Override
            public void run() {
                status = process.accept(new NullProgressMonitor());
            }
        };

        runTask(job, "Joining session...");

        switch (job.status) {
        case OK:
            break;
        case CANCEL:
        case ERROR:
            showCancelMessage(process.getPeer(), process.getErrorMessage(),
                CancelLocation.LOCAL);
            break;
        case REMOTE_CANCEL:
        case REMOTE_ERROR:
            showCancelMessage(process.getPeer(), process.getErrorMessage(),
                CancelLocation.REMOTE);
            break;
        }

        close();
    }

    /**
     * Calls {@link IncomingSessionNegotiation#localCancel(String, CancelOption)}
     * in a separate thread.
     */
    public void performCancel() {
        ThreadUtils
            .runSafeAsync("CancelJoinSessionWizard", LOG, new Runnable() {
                    @Override
                    public void run() {
                        process.localCancel(null,
                            CancelOption.NOTIFY_PEER);
                    }
                }
            );
    }

    private void showCancelMessage(JID jid, String errorMsg,
        CancelLocation cancelLocation) {

        String peer = jid.getBase();

        if (errorMsg != null) {
            switch (cancelLocation) {
            case LOCAL:
                DialogUtils
                    .showError(this, Messages.JoinSessionWizard_inv_canceled,
                        Messages.JoinSessionWizard_inv_canceled_text
                            + Messages.JoinSessionWizard_8 + errorMsg
                    );
                break;
            case REMOTE:
                DialogUtils
                    .showError(this, Messages.JoinSessionWizard_inv_canceled,
                        MessageFormat.format(
                            Messages.JoinSessionWizard_inv_canceled_text2,
                            peer, errorMsg)
                    );
            }
        } else {
            switch (cancelLocation) {
            case LOCAL:
                break;
            case REMOTE:
                DialogUtils
                    .showInfo(this, Messages.JoinSessionWizard_inv_canceled,
                        MessageFormat.format(
                            Messages.JoinSessionWizard_inv_canceled_text3,
                            peer)
                    );
            }
        }
    }
}
