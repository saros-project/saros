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
package de.fu_berlin.inf.dpp.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.ui.wizards.pages.AddBuddyWizardPage;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Wizard for adding a new buddy to the roster of the currently connected user.
 */
public class AddBuddyWizard extends Wizard {

    private static final Logger log = Logger.getLogger(AddBuddyWizard.class
        .getName());

    protected Saros saros;
    protected final AddBuddyWizardPage page = new AddBuddyWizardPage();

    public AddBuddyWizard(Saros saros) {
        setWindowTitle("New Buddy");
        this.saros = saros;

        this.addPage(page);
        this.setNeedsProgressMonitor(true);
        this.setHelpAvailable(false);
    }

    @Override
    public boolean performFinish() {
        final JID jid = this.page.getJID();

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                    try {
                        doAddContact(jid, SubMonitor.convert(monitor));
                    } catch (CancellationException e) {
                        throw new InterruptedException();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            log.error(e.getCause().getMessage(), e.getCause());
            page.setErrorMessage(e.getMessage());
            // leave wizard open
            return false;
        } catch (InterruptedException e) {
            log.debug("Adding buddy " + jid.toString()
                + " was canceled by the user.");
        }
        // close the wizard
        return true;
    }

    protected void doAddContact(JID jid, SubMonitor monitor)
        throws InvocationTargetException {

        monitor.beginTask("Adding " + jid, 2);
        try {
            try {
                if (saros.getRoster().contains(jid.toString())) {
                    openError("Buddy already added", "The buddy "
                        + "you were looking to add is already stored in your "
                        + "buddy list.");
                }

                else if (!RosterUtils.isJIDonServer(saros.getConnection(), jid,
                    monitor.newChild(1))) {
                    if (!openQuestionDialog("Buddy not found", "The buddy "
                        + jid + " could not be found on the server."
                        + " Please make sure you spelled the name correctly.\n"
                        + "It is also possible that the server didn't"
                        + " return a correct answer for this buddy."
                        + " Do you want to add it anyway?")) {
                        throw new InvocationTargetException(new XMPPException(
                            "ServiceDiscovery returned no results."), "Buddy "
                            + jid + " couldn't be found on server.");
                    }
                    log.debug("The buddy " + jid + " couldn't be found."
                        + " The user chose to add it anyway.");
                }
            } catch (XMPPException e) {

                String error = extractDiscoveryErrorString(jid, e);

                // ask the user what to do
                if (!openQuestionDialog("Buddy look-up failed",
                    "We weren't able to determine wether your buddy's JID "
                        + jid + " is valid because of the following error:\n\n"
                        + error + "\n\n" + "Do you want to add it anyway?")) {
                    // don't add buddy
                    throw new InvocationTargetException(e,
                        "The XMPP/Jabber server did not support a query for whether "
                            + jid + " is a valid JID.");
                }
                log.warn("The XMPP/Jabber server did not support a query for"
                    + " whether " + jid + " is a valid JID: " + e.getMessage()
                    + ". The user chose to add it anyway.", e);
            }

            // now add the buddy to the Roster
            try {
                RosterUtils.addToRoster(saros.getConnection(), jid, jid.toString(),
                    null, monitor.newChild(1));
            } catch (XMPPException e) {
                throw new InvocationTargetException(e, "Couldn't add buddy "
                    + jid + " to Saros buddies: " + e.getMessage());
            }
        } finally {
            monitor.done();
        }
    }

    public String extractDiscoveryErrorString(JID jid, XMPPException e) {

        if (e.getMessage().contains("item-not-found")) {
            return "Buddy " + jid + " couldn't be found on server.";
        } else if (e.getMessage().contains("remote-server-not-found")) {
            return "The server " + jid.getDomain()
                + " couldn't be connected to.";
        } else if (e.getMessage().contains("No response from the server")) {
            return "Checking for buddy " + jid.getName()
                + " timed out on server " + jid.getDomain();
        } else {
            return e.getMessage();
        }
    }

    /**
     * Shows a QuestionDialog to the user and returns his answer.
     * 
     * @blocking
     * @param title
     *            the dialogs title
     * @param message
     *            the message to show to the user
     * @return true, if he clicked Yes, false otherwise
     */
    protected boolean openQuestionDialog(final String title,
        final String message) {
        try {
            return Util.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {
                    return MessageDialog.openQuestion(getShell(), title,
                        message);
                }
            });
        } catch (Exception e) {
            log.error("An internal error ocurred while trying"
                + " to open the question dialog.");
            return false;
        }
    }

    /**
     * Shows a error message to the user and waits for him to confirm.
     * 
     * @blocking
     * 
     * @param title
     *            the error title
     * @param message
     *            the message to show to the user
     */

    protected void openError(final String title, final String message) {
        try {
            Util.runSWTSync(new Callable<Void>() {
                public Void call() {
                    MessageDialog.openError(getShell(), title, message);
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("An internal error ocurred while trying"
                + " to open the error message.");
        }

    }
}
