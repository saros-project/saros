package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.invitation.IncomingInvitationProcess;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.VersionManager;

/**
 * A wizard page that displays the name of the inviter and the description
 * provided with the invitation.
 */
class ShowDescriptionPage extends WizardPage {

    private final JoinSessionWizard joinSessionWizard;

    protected ShowDescriptionPage(JoinSessionWizard joinSessionWizard,
        VersionManager manager, IncomingInvitationProcess invProcess) {
        super("firstPage");
        this.joinSessionWizard = joinSessionWizard;

        setTitle("Session Invitation");
        setDescription("You have been invited to join a shared project session."
            + " Click next if you want to accept the invitation.");
        setImageDescriptor(SarosUI
            .getImageDescriptor("icons/wizban/invitation.png"));

        /*
         * Show compatibility issues and inform the user what to do (but the
         * user can always proceed).
         */
        VersionManager.VersionInfo vInfo = invProcess.versionInfo;

        String remoteSarosVersion = vInfo.version.toString();
        switch (vInfo.compatibility) {

        case TOO_NEW:
            setMessage("Your Saros version is: " + manager.getVersion()
                + ". Your peer has an older version: " + remoteSarosVersion
                + ".\n Please tell your peer to check for updates!"
                + " Proceeding with incompatible versions"
                + " may cause malfunctions!", WARNING);
            break;

        case OK:
            break;

        case TOO_OLD:
        default:
            setMessage("Your Saros version is too old: "
                + manager.getVersion().toString()
                + ". Your peer has a newer version: " + remoteSarosVersion
                + ".\n Please check for updates!"
                + " Proceeding with incompatible versions"
                + " may cause malfunctions!", WARNING);
        }
    }

    @Override
    public boolean canFlipToNextPage() {
        // Important! otherwise getNextPage() is called
        return true;
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        Label inviterText = new Label(composite, SWT.WRAP);
        inviterText.setText(this.joinSessionWizard.process.getDescription());

        setControl(composite);

        if (joinSessionWizard.getPreferenceUtils().isAutoAcceptInvitation()) {
            joinSessionWizard.pressWizardButton(IDialogConstants.NEXT_ID);
        }
    }
}