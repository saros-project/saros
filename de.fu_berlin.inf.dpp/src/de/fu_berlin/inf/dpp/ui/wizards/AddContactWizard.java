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

import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IMessageProvider;
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

/**
 * Wizard for adding a new contact to the roster of the currently connected
 * user.
 */
public class AddContactWizard extends Wizard {

    public static final boolean allowToEnterNick = false;

    protected Saros saros;

    public AddContactWizard(Saros saros) {
        setWindowTitle("New Contact");
        this.saros = saros;
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
                        WizardPage.INFORMATION);
                } else {
                    this.setMessage(null);
                }
            }

            this.setErrorMessage(null);
            setPageComplete(true);
        }
    }

    private final AddContactPage page = new AddContactPage();

    @Override
    public void addPages() {
        addPage(this.page);
    }

    @Override
    public boolean performFinish() {
        try {
            if (allowToEnterNick && !(page.getNickname().length() == 0)) {
                saros.addContact(this.page.getJID(), this.page.getNickname(),
                    null);
            } else {
                saros.addContact(this.page.getJID(), this.page.getJID()
                    .toString(), null);
            }
            return true;

        } catch (XMPPException e) {
            // contact not found
            if (e.getMessage().contains("item-not-found"))
                this.page.setMessage("Contact not found!",
                    IMessageProvider.ERROR);
            else
                this.page.setMessage(e.getMessage(), IMessageProvider.ERROR);
        }

        return false;
    }
}
