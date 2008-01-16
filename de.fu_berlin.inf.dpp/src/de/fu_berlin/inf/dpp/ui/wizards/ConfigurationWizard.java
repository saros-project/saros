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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A wizard to configure Saros.
 * 
 */
public class ConfigurationWizard extends Wizard {
	
	/**
	 * 
	 */
	private class FirstPage extends WizardPage implements IWizardPage2 {
		
		Button createButton, enterButton;
		
		protected FirstPage() {
			super("firstPage");

			setTitle("Saros Configuration");
			setDescription("This dialog will guide you through the configuration of Saros.");
		}

		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(1, false));

			Label inviterLabel = new Label(composite, SWT.NONE);
			inviterLabel.setText("Welcome to the configuration Wizard of SarosDo you have an existing jabber account that you want to use with Saros?");

			createButton = new Button(composite, SWT.RADIO);
			createButton.setText("No, I want to create a new Jabber account now.");
			createButton.setSelection(true);
			
			enterButton = new Button (composite, SWT.RADIO);
			enterButton.setText("Yes, let me enter my account information.");
			enterButton.setSelection(false);
			
			setControl(composite);
		}

		public boolean performFinish() {
			return true;
		}
	}

	public ConfigurationWizard() {
		setWindowTitle("Saros Configuration");
		setHelpAvailable(false);
		setNeedsProgressMonitor(true);
		
		// wizards.add(new FirstPage());
		wizards.add(new RegisterAccountPage(false, false, true));
		wizards.add(new NetworkSettingsPage());
		wizards.add(new PrivacyPage());
	}
	
	List<IWizardPage2> wizards = new LinkedList<IWizardPage2>(); 
	
	@Override
	public void addPages() {
		for (IWizardPage2 wizard : wizards){
			addPage(wizard);	
		}
	}

	@Override
	public boolean performFinish() {
	
		for (IWizardPage2 wizard : wizards){
			if (!wizard.performFinish()){
				getContainer().showPage(wizard);
				return false;
			}
		}
	
		return true;
	}
	
	@Override
	public boolean performCancel() {
		return true;
	}
	
}
