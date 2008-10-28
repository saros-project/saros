package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;

/**
 * A Wizard Page for entering the Port Configuration.
 * 
 * @author rdjemili
 *
 */
public class NetworkSettingsPage extends WizardPage implements IWizardPage2 {

	protected NetworkSettingsPage() {
		super("networksettings");
	}
	private Text portText, skypeText;
	private Button autoText;

	public void createControl(Composite parent) {
		
		Composite root = new Composite(parent, SWT.NONE);

		root.setLayout(new GridLayout(2, false));

		setTitle("Configure Network Settings");
		setDescription("Configure your network settings for use with Saros");
		
		Label portDescription = new Label(root, SWT.NONE);
		GridData twoColumn = new GridData();
		twoColumn.horizontalSpan = 2;
		portDescription.setLayoutData(twoColumn);
		portDescription.setText("Choose your incoming port and configure your firewall to accept incoming connections over this port.");
		
		Label serverLabel = new Label(root, SWT.NONE);
		serverLabel.setText("Port:");

		portText = new Text(root, SWT.BORDER);
		portText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label autoConnectDesc = new Label(root, SWT.NONE);
		autoConnectDesc.setLayoutData(twoColumn);
		autoConnectDesc.setText("Should Saros automatically connect on Eclipse startup?");
		
		new Label(root, SWT.NONE);
		
		autoText = new Button(root, SWT.CHECK | SWT.LEFT);
		autoText.setText("Startup automatically.");
		
		Label skypeDesc = new Label(root, SWT.NONE);
		skypeDesc.setLayoutData(twoColumn);
		skypeDesc.setText("Saros can send your Skype username to your peer if you provide it here");
		
		Label skype = new Label(root, SWT.NONE);
		skype.setText("Skype Username:");
		skypeText = new Text(root, SWT.BORDER);
		skypeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		

		// Set initial values
		IPreferenceStore preferences = Saros.getDefault().getPreferenceStore();
		portText.setText(String.valueOf(preferences.getInt(PreferenceConstants.FILE_TRANSFER_PORT)));
		autoText.setSelection(preferences.getBoolean(PreferenceConstants.AUTO_CONNECT));
		skypeText.setText(preferences.getString(PreferenceConstants.SKYPE_USERNAME));

		ModifyListener m = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				update();
			}
		};
		
		portText.addModifyListener(m);
				
		autoText.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                update();
            }
        });
		
		
		skypeText.addModifyListener(m);
		
		update();

		setControl(root);
	}
	
	private void update() {
		try {
			Integer.parseInt(portText.getText());
			setPageComplete(true);
			setErrorMessage(null);
		} catch (Exception e){
			setPageComplete(false);
			setErrorMessage("Port should a number (for instance 7777)");
		}
	}

	public boolean performFinish() {
		IPreferenceStore preferences = Saros.getDefault().getPreferenceStore();
		preferences.setValue(PreferenceConstants.FILE_TRANSFER_PORT, portText.getText());
		preferences.setValue(PreferenceConstants.AUTO_CONNECT, autoText.getSelection());
		preferences.setValue(PreferenceConstants.SKYPE_USERNAME, skypeText.getText());
		
		return true;
	}
}
