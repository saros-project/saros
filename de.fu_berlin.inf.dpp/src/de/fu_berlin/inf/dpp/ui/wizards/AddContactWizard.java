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
 * Wizard for adding a new contact to the roster of the currently connected
 * user.
 */
public class AddContactWizard extends Wizard {

    private static final Logger log = Logger.getLogger(AddContactWizard.class
        .getName());

    public static final boolean allowToEnterNick = false;

    protected Saros saros;
    protected final AddContactPage page = new AddContactPage();

    public AddContactWizard(Saros saros) {
        setWindowTitle("New Contact");
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

            setTitle("New Contact");
            setDescription("Add a new contact to your Jabber roster");
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);

            composite.setLayout(new GridLayout(2, false));

            Label idLabel = new Label(composite, SWT.NONE);
            idLabel.setText("Jabber ID");

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
                this.setMessage("Please enter a Jabber-ID");
                this.setPageComplete(false);
                return;
            }

            if (!userAtHostPattern.matcher(this.idText.getText().trim())
                .matches()) {
                this
                    .setErrorMessage("Not a valid Jabber-ID (should be: id@server.domain)!");
                this.setMessage(null);
                this.setPageComplete(false);
                return;
            }

            if (allowToEnterNick) {
                if (getNickname().length() == 0) {
                    this.setMessage(
                        "Enter a Nickname for the Contact (optional)",
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
            log.debug("Adding contact " + jid.toString()
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
                if (!saros.isJIDonServer(jid, monitor.newChild(1))) {
                    if (!openQuestionDialog("Contact not found", "The contact "
                        + jid + " could not be found on the server."
                        + " Please make sure you spelled the name correctly.\n"
                        + "It is also possible that the server didn't"
                        + " return a correct answer for this contact."
                        + " Do you want to add it anyway?")) {
                        throw new InvocationTargetException(new XMPPException(
                            "ServiceDiscovery returned no results."),
                            "Contact " + jid + " couldn't be found on server.");
                    }
                    log.debug("The contact " + jid + " couldn't be found."
                        + " The user chose to add it anyway.");
                }
            } catch (XMPPException e) {
                // handle the different exceptions
                if (e.getMessage().contains("item-not-found")) {
                    throw new InvocationTargetException(e, "Contact " + jid
                        + " couldn't be found on server.");
                }

                if (e.getMessage().contains("remote-server-not-found")) {
                    throw new InvocationTargetException(e, "The server "
                        + jid.getDomain() + " couldn't be found.");
                }

                if (e.getMessage().contains("No response from the server")) {
                    throw new InvocationTargetException(e,
                        "Couldn't connect to server " + jid.getDomain());
                }
                // ask the user what to do
                if (!openQuestionDialog("XMPP Error",
                    "We weren't able to determine wether your contact's JID "
                        + jid + " is valid because the XMPP server"
                        + " seems to not support the query.\n"
                        + "Do you want to add it anyway?")) {
                    // don't add contact
                    throw new InvocationTargetException(e,
                        "The XMPP server did not support a query for whether "
                            + jid + " is a valid JID.");
                }
                log.debug("The XMPP server did not support a query for"
                    + " whether " + jid + " is a valid JID: " + e.getMessage()
                    + ". The user chose to add it anyway.");
            }

            // now add the contact to the Roster
            try {
                if (allowToEnterNick && !(nickname.length() == 0)) {
                    saros.addContact(jid, nickname, null, monitor.newChild(1));
                } else {
                    saros.addContact(jid, jid.toString(), null, monitor
                        .newChild(1));
                }
            } catch (XMPPException e) {
                throw new InvocationTargetException(e, "Couldn't add contact "
                    + jid + " to Roster.");
            }
        } finally {
            monitor.done();
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
}
