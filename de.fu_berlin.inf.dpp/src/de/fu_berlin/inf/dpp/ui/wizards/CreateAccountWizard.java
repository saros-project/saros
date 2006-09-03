/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;

/**
 * An wizard that is used to create Jabber accounts.
 * 
 * @author rdjemili
 */
public class CreateAccountWizard extends Wizard {
    private class CreateAccountPage extends WizardPage {
        private Text      serverText;
        private Text      userText;
        private Text      passwordText;
        private Button    prefButton;

        protected CreateAccountPage() {
            super("create");
            
            setTitle("New User Account");
            setDescription("Create a new user account for a Jabber server");
        }
        
        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            
            composite.setLayout(new GridLayout(2, false));
            
            Label serverLabel = new Label(composite, SWT.NONE);
            serverLabel.setText("Jabber Server");
            
            serverText = new Text(composite, SWT.BORDER);
            serverText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            serverText.setText("jabber.org");
            
            Label userLabel = new Label(composite, SWT.NONE);
            userLabel.setText("Username");
            
            userText = new Text(composite, SWT.BORDER);
            userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            
            Label pwLabel = new Label(composite, SWT.NONE);
            pwLabel.setText("Password");
            
            passwordText = new Text(composite, SWT.BORDER);
            passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            passwordText.setEchoChar('*');
            
            prefButton = new Button(composite, SWT.CHECK | SWT.SEPARATOR);
            prefButton.setText("Store the new configuration in your preferences.");
            prefButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
            
            hookListeners();
            updateNextEnablement();
            
            setControl(composite);
        }
        
        public String getServer() {
            return serverText.getText();
        }
        
        public String getUsername() {
            return userText.getText();
        }
        
        public String getPassword() {
            return passwordText.getText();
        }
        
        public boolean isStoreInPreferences() {
            return prefButton.getSelection();
        }
        
        private void hookListeners() {
            ModifyListener listener = new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    updateNextEnablement();
                }
            };
            
            serverText.addModifyListener(listener);
            userText.addModifyListener(listener);
            passwordText.addModifyListener(listener);
        }
        
        private void updateNextEnablement() {
            boolean done = serverText.getText().length() > 0 &&
                userText.getText().length() > 0 &&
                passwordText.getText().length() > 0;
            
            setPageComplete(done);
        }
    }
    
    private CreateAccountPage page = new CreateAccountPage();
    
    public CreateAccountWizard() {
        setWindowTitle("New User Account");
        setHelpAvailable(false);
        setNeedsProgressMonitor(true);
    }
    
    @Override
    public void addPages() {
        addPage(page);
    }
    
    @Override
    public boolean performFinish() {
        final String server = page.getServer();
        final String username = page.getUsername();
        final String password = page.getPassword();
        final boolean storeInPreferences = page.isStoreInPreferences();
        
        try {
            getContainer().run(false, false, new IRunnableWithProgress(){
                public void run(IProgressMonitor monitor) 
                    throws InvocationTargetException, InterruptedException {
                	
					createAccount(server, username, password, 
                        storeInPreferences, monitor);
                }
            });
            
        } catch (InvocationTargetException e) {
            page.setMessage(e.getCause().getMessage(), IMessageProvider.ERROR);
            e.printStackTrace();
            return false;
            
        } catch (InterruptedException e) {
        	page.setMessage(e.getCause().getMessage(), IMessageProvider.ERROR);
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    private void createAccount(String server, String username, String password, 
            boolean storeInPrefernces, IProgressMonitor monitor) throws InvocationTargetException {
        
        try {
            Saros.getDefault().createAccount(server, username, password, monitor);
            
            if (storeInPrefernces)
                storeToPreferences();
            
        } catch (final XMPPException e) {
            throw new InvocationTargetException(e);
        }
    }
    
    private void storeToPreferences() {
        IPreferenceStore preferences = Saros.getDefault().getPreferenceStore();
        preferences.putValue(PreferenceConstants.SERVER, page.getServer());
        preferences.putValue(PreferenceConstants.USERNAME, page.getUsername());
        preferences.putValue(PreferenceConstants.PASSWORD, page.getPassword());
    }
}
