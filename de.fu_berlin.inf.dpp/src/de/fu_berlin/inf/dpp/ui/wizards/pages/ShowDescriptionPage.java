package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.util.VersionManager;

/**
 * A wizard page that displays the name of the inviter and the description
 * provided with the invitation.
 */
public class ShowDescriptionPage extends WizardPage {

    private final JoinSessionWizard joinSessionWizard;
    private String description;

    public ShowDescriptionPage(JoinSessionWizard joinSessionWizard,
        VersionManager manager, IncomingSessionNegotiation invProcess) {
        super(Messages.ShowDescriptionPage_title);
        this.joinSessionWizard = joinSessionWizard;
        this.description = joinSessionWizard.process.getDescription();

        setTitle(Messages.ShowDescriptionPage_title2);
        setDescription(Messages.ShowDescriptionPage_description);
        setImageDescriptor(ImageManager
            .getImageDescriptor("icons/wizban/invitation.png")); //$NON-NLS-1$

        /*
         * Show compatibility issues and inform the user what to do (but the
         * user can always proceed).
         */
        VersionManager.VersionInfo vInfo = invProcess.versionInfo;

        String remoteSarosVersion = vInfo.version.toString();
        switch (vInfo.compatibility) {

        case TOO_NEW:
            setMessage(MessageFormat.format(
                Messages.ShowDescriptionPage_error_too_new,
                manager.getVersion(), remoteSarosVersion), WARNING);
            break;

        case OK:
            break;

        case TOO_OLD:
        default:
            setMessage(MessageFormat.format(
                Messages.ShowDescriptionPage_error_too_old, manager
                    .getVersion().toString(), remoteSarosVersion), WARNING);
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
        inviterText.setText(this.description);

        setControl(composite);

        if (joinSessionWizard.getPreferenceUtils().isAutoAcceptInvitation()) {
            joinSessionWizard.pressWizardButton(IDialogConstants.NEXT_ID);
        }
    }
}