package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.text.MessageFormat;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * A wizard page that displays the name of the inviter and the description
 * provided with the invitation.
 */
public class ShowDescriptionPage extends WizardPage {

    private IncomingSessionNegotiation sessionNegotiation;

    private VersionManager versionManager;

    public ShowDescriptionPage(VersionManager versionManager,
        IncomingSessionNegotiation sessionNegotiation) {
        super(Messages.ShowDescriptionPage_title);
        this.sessionNegotiation = sessionNegotiation;
        this.versionManager = versionManager;

        setTitle(Messages.ShowDescriptionPage_title2);
        setDescription(Messages.ShowDescriptionPage_description);
        setImageDescriptor(ImageManager
            .getImageDescriptor("icons/wizban/invitation.png")); //$NON-NLS-1$
    }

    @Override
    public boolean canFlipToNextPage() {
        // Important! otherwise getNextPage() is called
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        sessionNegotiation = null;
        versionManager = null;
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        String completeDescription = RosterUtils.getNickname(null,
            sessionNegotiation.getPeer())
            + " has invited you to a Saros session with the currently shared project(s):\n"
            + sessionNegotiation.getDescription();

        Label inviterText = new Label(composite, SWT.WRAP);
        inviterText.setText(completeDescription);

        setControl(composite);

        /*
         * Show compatibility issues and inform the user what to do (but the
         * user can always proceed).
         */
        VersionInfo remoteVersion = sessionNegotiation.getRemoteVersionInfo();

        String remoteSarosVersion = remoteVersion.version.toString();
        switch (remoteVersion.compatibility) {

        case TOO_NEW:
            setMessage(MessageFormat.format(
                Messages.ShowDescriptionPage_error_too_new,
                versionManager.getVersion(), remoteSarosVersion), WARNING);
            break;

        case OK:
            break;

        case TOO_OLD:
        default:
            setMessage(MessageFormat.format(
                Messages.ShowDescriptionPage_error_too_old, versionManager
                    .getVersion().toString(), remoteSarosVersion), WARNING);
        }
    }
}