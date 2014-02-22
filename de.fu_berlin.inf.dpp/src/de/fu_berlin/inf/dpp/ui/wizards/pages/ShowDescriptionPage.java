package de.fu_berlin.inf.dpp.ui.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.versioning.VersionManager;

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

        String nickname = XMPPUtils.getNickname(null,
            sessionNegotiation.getPeer());

        if (nickname == null)
            nickname = sessionNegotiation.getPeer().getBase();

        String completeDescription = nickname
            + " has invited you to a Saros session with the currently shared project(s):\n"
            + sessionNegotiation.getDescription();

        Label inviterText = new Label(composite, SWT.WRAP);
        inviterText.setText(completeDescription);

        setControl(composite);
    }
}