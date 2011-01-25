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
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Wizard for adding a new buddy to the roster of the currently connected user.
 */
public class AddContactWizard extends Wizard {

    private static final Logger log = Logger.getLogger(AddContactWizard.class
        .getName());

    // TODO Just to make the code more complicated!? And why false?
    public static final boolean allowToEnterNick = false;

    protected Saros saros;
    protected final AddContactPage page = new AddContactPage();

    public AddContactWizard(Saros saros) {
        setWindowTitle("New Buddy");
        this.saros = saros;

        this.addPage(page);
        this.setNeedsProgressMonitor(true);
        this.setHelpAvailable(false);
    }

    public static class AddContactPage extends WizardPage {
        protected Text idText;

        protected Text nicknameText;

        protected AddContactPage() {
            super("create");

            setTitle("New Buddy");
            setDescription("Add a new buddy to your Saros buddies");
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);

            composite.setLayout(new GridLayout(2, false));

            Label idLabel = new Label(composite, SWT.NONE);
            idLabel.setText("XMPP/Jabber ID");

            this.idText = new Text(composite, SWT.BORDER);
            this.idText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));

            if (allowToEnterNick) {

                Label nicknameLabel = new Label(composite, SWT.NONE);
                nicknameLabel.setText("Nickname");

                this.nicknameText = new Text(composite, SWT.BORDER);
                this.nicknameText.setLayoutData(new GridData(SWT.FILL,
                    SWT.CENTER, true, false));
            }
            hookListeners();
            updateNextEnablement();

            setControl(composite);
        }

        public JID getJID() {
            return new JID(this.idText.getText().trim());
        }

        public String getNickname() {
            if (!allowToEnterNick) {
                throw new IllegalStateException();
            }
            return this.nicknameText.getText().trim();
        }

        private void hookListeners() {
            ModifyListener listener = new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    updateNextEnablement();
                }
            };

            this.idText.addModifyListener(listener);
            if (allowToEnterNick) {
                this.nicknameText.addModifyListener(listener);
            }
        }

        /**
         * Email-Pattern was too strict:
         * 
         * <code> Pattern emailPattern = Pattern.compile(
         * "^[A-Z0-9._%+-]+@[A-Z0-9.-]+$\\.[A-Z]{2,4}",
         * Pattern.CASE_INSENSITIVE); </code>
         */
        Pattern userAtHostPattern = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+$", Pattern.CASE_INSENSITIVE);

        private void updateNextEnablement() {

            boolean done = (this.idText.getText().length() > 0);

            if (!done) {
                this.setErrorMessage(null);
                this.setMessage("Please enter a XMPP/Jabber ID");
                this.setPageComplete(false);
                return;
            }

            if (!userAtHostPattern.matcher(this.idText.getText().trim())
                .matches()) {
                this.setErrorMessage("Not a valid XMPP/Jabber ID (should be: id@server.domain)!");
                this.setMessage(null);
                this.setPageComplete(false);
                return;
            }

            if (allowToEnterNick) {
                if (getNickname().length() == 0) {
                    this.setMessage(
                        "Enter a nickname for the buddy (optional)",
                        IMessageProvider.INFORMATION);
                } else {
                    this.setMessage(null);
                }
            }

            this.setErrorMessage(null);
            setPageComplete(true);
        }
    }

    @Override
    public boolean performFinish() {

        final JID jid = this.page.getJID();
        final String nickname = allowToEnterNick ? page.getNickname() : "";

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                    try {
                        doAddContact(jid, nickname, SubMonitor.convert(monitor));
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

    protected void doAddContact(JID jid, String nickname, SubMonitor monitor)
        throws InvocationTargetException {

        monitor.beginTask("Adding " + jid, 2);
        try {
            try {
                if (saros.getRoster().contains(jid.toString())) {
                    openError("Buddy already added", "The buddy "
                        + "you were looking to add is already stored in your "
                        + "buddy list.");
                }

                else if (!saros.isJIDonServer(jid, monitor.newChild(1))) {
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
                if (allowToEnterNick && !(nickname.length() == 0)) {
                    saros.addContact(jid, nickname, null, monitor.newChild(1));
                } else {
                    saros.addContact(jid, jid.toString(), null,
                        monitor.newChild(1));
                }
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
