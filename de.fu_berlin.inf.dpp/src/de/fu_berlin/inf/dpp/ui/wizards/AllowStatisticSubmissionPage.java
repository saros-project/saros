package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;

/**
 * A WizardPage which lets the user specify whether he wants to submit statistic
 * informations to us or not.
 * 
 * @author Lisa Dohrmann
 */
public class AllowStatisticSubmissionPage extends WizardPage implements
    IWizardPage2 {

    protected Button allowButton;

    @Inject
    protected Saros saros;

    @Inject
    protected StatisticManager statisticManager;

    public AllowStatisticSubmissionPage() {
        super("statistics"); //$NON-NLS-1$
        setTitle(Messages.getString("feedback.statistic.page.title")); //$NON-NLS-1$
        setDescription(Messages
            .getString("feedback.statistic.page.description")); //$NON-NLS-1$

        Saros.reinject(this);
    }

    /**
     * Creates a label to present a longer message to the user and two radio
     * buttons to let the user enable or disable the statistic submission
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        layout.verticalSpacing = 20;
        composite.setLayout(layout);
        composite
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label message = new Label(composite, SWT.WRAP);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint = composite.getClientArea().width;
        message.setLayoutData(gd);
        message.setText(Messages.getString("feedback.statistic.page.request")); //$NON-NLS-1$

        Group group = new Group(composite, SWT.NONE);
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        allowButton = new Button(group, SWT.RADIO);
        allowButton.setText(Messages.getString("feedback.statistic.page.yes")); //$NON-NLS-1$
        allowButton.setSelection(true);

        Button forbidButton = new Button(group, SWT.RADIO);
        forbidButton.setText(Messages.getString("feedback.statistic.page.no")); //$NON-NLS-1$

        setPageComplete(true);
        setControl(composite);
    }

    /**
     * This is called if the Wizard finishes successfully and stores if the user
     * allowed or forbade the statistic submission.
     */
    public boolean performFinish() {
        // store selection in the workspace and in global preferences
        statisticManager.setStatisticSubmissionAllowed(allowButton
            .getSelection());

        return true;
    }

}
