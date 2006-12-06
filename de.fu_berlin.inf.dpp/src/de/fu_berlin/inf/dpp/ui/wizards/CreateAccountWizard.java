/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * (c) Christopher Oezbek - 2006
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

import org.eclipse.jface.wizard.Wizard;

/**
 * An wizard that is used to create Jabber accounts.
 * 
 * @author rdjemili
 * @author coezbek
 */
public class CreateAccountWizard extends Wizard {
	
	private RegisterAccountPage page;

	public CreateAccountWizard(boolean createAccount, boolean showStoreInPrefsButton, boolean storeInPrefsDefault) {
		
		if (createAccount){
			setWindowTitle("Create New User Account");
		} else {
			setWindowTitle("Enter User Account");
		}
		page = new RegisterAccountPage(createAccount, showStoreInPrefsButton, storeInPrefsDefault);
		setHelpAvailable(false);
		setNeedsProgressMonitor(true);
	}

	public String getServer() {
		return server;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	String server, password, username;
	
	@Override
	public void addPages() {
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		if (page.performFinish()){
			server = page.getServer();
			username = page.getUsername();
			password = page.getPassword();
			
			return true;
		} else {
			return false;
		}
	}

}
