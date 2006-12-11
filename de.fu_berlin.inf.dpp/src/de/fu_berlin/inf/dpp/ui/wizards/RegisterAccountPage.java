/**
 * 
 */
package de.fu_berlin.inf.dpp.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;

public class RegisterAccountPage extends WizardPage implements IWizardPage2 {

	private Text serverText;

	private Text userText;

	private Text passwordText;

	private Text repeatPasswordText;

	private Button prefButton;

	private boolean createAccount;

	private boolean showPrefButton;

	private boolean storePreferences;
	
	public RegisterAccountPage() {
		this(true, true, true);
	}

	public RegisterAccountPage(boolean createAccount, boolean showPrefButton, boolean storePreferences) {
		super("create");
		this.createAccount = createAccount;
		this.showPrefButton = showPrefButton;
		this.storePreferences = storePreferences;
	}

	public void createControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);

		root.setLayout(new GridLayout(2, false));

		if (createAccount) {
			setTitle("Create New User Account");
			setDescription("Create a new user account for a Jabber server");
		} else {
			setTitle("Enter User Account");
			setDescription("Enter your account information and Jabber server");
		}

		Label serverLabel = new Label(root, SWT.NONE);
		serverLabel.setText("Jabber Server");

		serverText = new Text(root, SWT.BORDER);
		serverText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		serverText.setText("jabber.org");

		Label userLabel = new Label(root, SWT.NONE);
		userLabel.setText("Username");

		userText = new Text(root, SWT.BORDER);
		userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label pwLabel = new Label(root, SWT.NONE);
		pwLabel.setText("Password");

		passwordText = new Text(root, SWT.BORDER);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		passwordText.setEchoChar('*');

		Label rpwLabel = new Label(root, SWT.NONE);
		rpwLabel.setText("Repeat Password");

		repeatPasswordText = new Text(root, SWT.BORDER);
		repeatPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		repeatPasswordText.setEchoChar('*');

		if (showPrefButton) {
			prefButton = new Button(root, SWT.CHECK | SWT.SEPARATOR);
			prefButton.setSelection(storePreferences);
			prefButton.setText("Store the new configuration in your preferences.");
			prefButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}

		if (!createAccount) {
			
			Button createAccountButton = new Button(root, SWT.NONE);
			createAccountButton.setText("Create Account");
			createAccountButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							try {
								Shell shell = Display.getDefault().getActiveShell();
								
								CreateAccountWizard wizard = new CreateAccountWizard(true, false, false);
								boolean success = Window.OK == new WizardDialog(shell, wizard).open();
								
								if (success){
									RegisterAccountPage.this.passwordText.setText(wizard.getPassword());
									RegisterAccountPage.this.repeatPasswordText.setText(wizard.getPassword());
									RegisterAccountPage.this.serverText.setText(wizard.getServer());
									RegisterAccountPage.this.userText.setText(wizard.getUsername());
								}
								
							} catch (Exception e) {
								Saros.getDefault().getLog().log(
									new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
										"Error while running enter account wizard", e));
							}
						}
					});
				}
			});
		}

		setInitialValues();

		hookListeners();
		updateNextEnablement();

		setControl(root);
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
		if (showPrefButton) {
			return prefButton.getSelection();
		}
		return storePreferences;
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
		repeatPasswordText.addModifyListener(listener);
		if (showPrefButton) {
			prefButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					// do nothing
				}

				public void widgetSelected(SelectionEvent e) {
					IPreferenceStore preferences = Saros.getDefault().getPreferenceStore();
					if (preferences.getString(PreferenceConstants.USERNAME).length() != 0
						&& prefButton.getSelection()) {
						setMessage(
							"Storing the configuration will override the existing settings.",
							DialogPage.WARNING);
					} else {
						setMessage(null);
					}
				}

			});
		}
	}

	private void updateNextEnablement() {

		boolean passwordsMatch = passwordText.getText().equals(repeatPasswordText.getText());
		boolean done = serverText.getText().length() > 0 && userText.getText().length() > 0
			&& passwordText.getText().length() > 0 && passwordsMatch;

		if (passwordsMatch) {
			setErrorMessage(null);
		} else {
			setErrorMessage("Passwords don't match.");
		}

		setPageComplete(done);
	}

	public void setInitialValues() {
		IPreferenceStore preferences = Saros.getDefault().getPreferenceStore();
		serverText.setText(preferences.getDefaultString(PreferenceConstants.SERVER));
		if (showPrefButton) {
			prefButton
				.setSelection(preferences.getString(PreferenceConstants.USERNAME).length() == 0);
		}
	}

	public boolean performFinish() {

		if (createAccount) {

			final String server = getServer();
			final String username = getUsername();
			final String password = getPassword();
			final boolean storeInPreferences = isStoreInPreferences();

			try {
				getContainer().run(false, false, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
						try {
							Saros.getDefault().createAccount(server, username, password, monitor);

							if (storeInPreferences) {
								IPreferenceStore preferences = Saros.getDefault()
									.getPreferenceStore();
								preferences.setValue(PreferenceConstants.SERVER, server);
								preferences.setValue(PreferenceConstants.USERNAME, username);
								preferences.setValue(PreferenceConstants.PASSWORD, password);
							}

						} catch (final XMPPException e) {
							throw new InvocationTargetException(e);
						}
					}
				});

			} catch (InvocationTargetException e) {
				String s = ((XMPPException)e.getCause()).getXMPPError().getMessage();
				
				if (s == null && ((XMPPException)e.getCause()).getXMPPError().getCode() == 409){
					s = "Account already exists";
				}
				
				setMessage(e.getCause().getMessage() + ": " + (s != null ? s : "No Explanation"), IMessageProvider.ERROR);
				return false;

			} catch (InterruptedException e) {
				setMessage(e.getCause().getMessage(), IMessageProvider.ERROR);
				return false;
			}

			return true;
		}

		else {
			if (isStoreInPreferences()) {

				final String server = getServer();
				final String username = getUsername();
				final String password = getPassword();

				IPreferenceStore preferences = Saros.getDefault().getPreferenceStore();
				preferences.setValue(PreferenceConstants.SERVER, server);
				preferences.setValue(PreferenceConstants.USERNAME, username);
				preferences.setValue(PreferenceConstants.PASSWORD, password);
			}
			return true;
		}
	}

}