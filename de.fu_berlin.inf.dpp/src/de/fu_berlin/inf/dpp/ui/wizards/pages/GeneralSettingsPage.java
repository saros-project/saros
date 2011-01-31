package de.fu_berlin.inf.dpp.ui.wizards.pages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;

/**
 * A Wizard Page for entering Auto-Connect and the Skype User name.
 * 
 * @author rdjemili
 */
public class GeneralSettingsPage extends WizardPage implements IWizardPage2 {

    protected final Saros saros;

    protected PreferenceUtils preferenceUtils;

    public GeneralSettingsPage(Saros saros, PreferenceUtils preferenceUtils) {
        super("general settings");
        this.saros = saros;
        this.preferenceUtils = preferenceUtils;
    }

    private Text skypeText;
    private Button autoButton;

    public void createControl(Composite parent) {

        Composite root = new Composite(parent, SWT.NONE);

        root.setLayout(new GridLayout(2, false));

        setTitle("Configure Network Settings");
        setDescription("Configure your network settings for use with Saros");

        GridData twoColumn = new GridData();
        twoColumn.horizontalSpan = 2;

        Label autoConnectDesc = new Label(root, SWT.NONE);
        autoConnectDesc.setLayoutData(twoColumn);
        autoConnectDesc
            .setText("Should Saros automatically connect on Eclipse start-up?");

        new Label(root, SWT.NONE);

        this.autoButton = new Button(root, SWT.CHECK | SWT.LEFT);
        this.autoButton.setText("Connect automatically.");

        createSpacer(root, 2);

        twoColumn = new GridData();
        twoColumn.horizontalSpan = 2;

        Label skypeDesc = new Label(root, SWT.NONE);
        skypeDesc.setLayoutData(twoColumn);
        skypeDesc
            .setText("Saros can send your Skype username to your peer if you provide it here");

        Label skype = new Label(root, SWT.NONE);
        skype.setText("Skype Username:");
        this.skypeText = new Text(root, SWT.BORDER);
        this.skypeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));

        // Set initial values
        this.autoButton.setSelection(preferenceUtils.isAutoConnecting());
        this.skypeText.setText(preferenceUtils.getSkypeUserName());

        // no settings to check here, page can be set to complete right away
        setPageComplete(true);

        setControl(root);
    }

    protected void createSpacer(Composite composite, int columnSpan) {
        Label label = new Label(composite, SWT.NONE);
        GridData gd = new GridData();
        gd.horizontalSpan = columnSpan;
        label.setLayoutData(gd);
    }

    public boolean performFinish() {
        IPreferenceStore preferences = saros.getPreferenceStore();
        preferences.setValue(PreferenceConstants.AUTO_CONNECT,
            this.autoButton.getSelection());
        preferences.setValue(PreferenceConstants.SKYPE_USERNAME,
            this.skypeText.getText());

        return true;
    }
}
