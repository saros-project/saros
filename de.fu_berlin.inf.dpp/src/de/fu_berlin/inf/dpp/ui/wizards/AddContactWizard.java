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

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;

public class AddContactWizard extends Wizard {

	private class AddContactPage extends WizardPage {
		private Text idText;

		private Text nicknameText;

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

			idText = new Text(composite, SWT.BORDER);
			idText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			Label nicknameLabel = new Label(composite, SWT.NONE);
			nicknameLabel.setText("Nickname");

			nicknameText = new Text(composite, SWT.BORDER);
			nicknameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			hookListeners();
			updateNextEnablement();

			setControl(composite);
		}

		public JID getJID() {
			return new JID(idText.getText());
		}

		public String getNickname() {
			return nicknameText.getText();
		}

		private void hookListeners() {
			ModifyListener listener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updateNextEnablement();
				}
			};

			idText.addModifyListener(listener);
			nicknameText.addModifyListener(listener);
		}

		private void updateNextEnablement() {
			boolean done = idText.getText().length() > 0 && nicknameText.getText().length() > 0;

			setPageComplete(done);
		}
	}

	private AddContactPage page = new AddContactPage();

	public AddContactWizard() {
		setWindowTitle("New Contact");
		setHelpAvailable(false);
	}

	@Override
	public void addPages() {
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		try {
			Saros.getDefault().addContact(page.getJID(), page.getNickname(), null);
			return true;

		} catch (Exception e) {
			page.setMessage(e.getMessage(), IMessageProvider.ERROR);
			e.printStackTrace();
		}

		return false;
	}
}
