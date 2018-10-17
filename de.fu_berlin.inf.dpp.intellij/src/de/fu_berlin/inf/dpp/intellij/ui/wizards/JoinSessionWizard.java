package de.fu_berlin.inf.dpp.intellij.ui.wizards;

import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.JobWithStatus;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.HeaderPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.InfoPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.PageActionListener;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelLocation;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;

import java.awt.Window;
import java.text.MessageFormat;

import static de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;

/**
 * A wizard that guides the user through an incoming invitation process.
 * <p/>
 * FIXME:
 * Long-Running Operation after each step
 * cancellation by a remote party
 * auto-advance.
 */
public class JoinSessionWizard extends Wizard {
    public static final String PAGE_INFO_ID = "JoinSessionInfo";

    private static final Logger LOG = Logger.getLogger(JoinSessionWizard.class);

    private final IncomingSessionNegotiation negotiation;

    private final PageActionListener actionListener = new PageActionListener() {
        @Override
        public void back() {
            // Not interested
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
     * @param negotiation The negotiation this wizard displays
     */
    public JoinSessionWizard(Window parent, IncomingSessionNegotiation negotiation) {
        super(parent, Messages.JoinSessionWizard_title,
            new HeaderPanel(Messages.ShowDescriptionPage_title2,
                Messages.ShowDescriptionPage_description));
        this.negotiation = negotiation;

        InfoPage infoPage = createInfoPage(negotiation);

        registerPage(infoPage);

        create();
    }

    private InfoPage createInfoPage(IncomingSessionNegotiation negotiation) {
        InfoPage infoPage = new InfoPage(PAGE_INFO_ID,
            Messages.JoinSessionWizard_accept, actionListener);
        infoPage.addText(negotiation.getPeer().getName() + " "
            + Messages.JoinSessionWizard_info);
        infoPage.addText(negotiation.getDescription());
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
                status = negotiation.accept(new NullProgressMonitor());
            }
        };

        runTask(job, "Joining session...");

        switch (job.status) {
        case OK:
            break;
        case CANCEL:
        case ERROR:
            showCancelMessage(negotiation.getPeer(),
                negotiation.getErrorMessage(), CancelLocation.LOCAL);
            break;
        case REMOTE_CANCEL:
        case REMOTE_ERROR:
            showCancelMessage(negotiation.getPeer(),
                negotiation.getErrorMessage(), CancelLocation.REMOTE);
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
                    negotiation.localCancel(null, CancelOption.NOTIFY_PEER);
                }
            });
    }

    private void showCancelMessage(JID jid, String errorMsg,
        CancelLocation cancelLocation) {

        String peer = jid.getBase();

        if (errorMsg != null) {
            switch (cancelLocation) {
            case LOCAL:
                NotificationPanel.showError(
                    Messages.JoinSessionWizard_inv_canceled_text
                        + Messages.JoinSessionWizard_8 + errorMsg,
                    Messages.JoinSessionWizard_inv_canceled);
                break;
            case REMOTE:
                NotificationPanel.showError(MessageFormat
                    .format(Messages.JoinSessionWizard_inv_canceled_text2, peer,
                        errorMsg), Messages.JoinSessionWizard_inv_canceled);
            }
        } else {
            switch (cancelLocation) {
            case LOCAL:
                break;
            case REMOTE:
                NotificationPanel.showInformation(MessageFormat
                    .format(Messages.JoinSessionWizard_inv_canceled_text3,
                        peer), Messages.JoinSessionWizard_inv_canceled);
            }
        }
    }
}
