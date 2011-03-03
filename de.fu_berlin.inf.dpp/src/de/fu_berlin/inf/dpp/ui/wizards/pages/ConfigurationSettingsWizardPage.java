package de.fu_berlin.inf.dpp.ui.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgets.IllustratedComposite;
import de.fu_berlin.inf.dpp.ui.widgets.decoration.EmptyText;
import de.fu_berlin.inf.dpp.util.LinkListener;

/**
 * Allows the user to enter general configuration parameters for use with Saros.
 * 
 * @author bkahlert
 */
public class ConfigurationSettingsWizardPage extends WizardPage {
    public static final String TITLE = "General Settings";
    public static final String DESCRIPTION = "Configure your settings for use with Saros.";

    @Inject
    protected Saros saros;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected StatisticManager statisticManager;

    @Inject
    protected ErrorLogManager errorLogManager;

    public ConfigurationSettingsWizardPage() {
        super(ConfigurationSettingsWizardPage.class.getName());
        SarosPluginContext.initComponent(this);
        setTitle(TITLE);
        setDescription(DESCRIPTION);
    }

    protected Button autoConnectButton;
    protected Button skypeUsageButton;
    protected EmptyText skypeUsernameText;

    protected Button statisticSubmissionButton;
    protected Button errorLogSubmissionButton;

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(LayoutUtils.createGridLayout(2, true, 5, 10));
        setControl(composite);

        Composite leftColumn = createLeftColumn(composite);
        leftColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite rightColumn = createRightColumn(composite);
        rightColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        setInitialValues();
        hookListeners();
        updateSkypeUsernameEnablement();
        updatePageCompletion();
    }

    protected Composite createLeftColumn(Composite composite) {
        Group leftColumn = new Group(composite, SWT.NONE);
        leftColumn.setLayout(LayoutUtils.createGridLayout(5, 0));
        leftColumn.setText("Connection");

        /*
         * auto connect
         */
        Composite autoConnectComposite = new IllustratedComposite(leftColumn,
            SWT.TOP, ImageManager.ELCL_XMPP_CONNECTED);
        autoConnectComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, true));
        autoConnectComposite.setLayout(LayoutUtils.createGridLayout(0, 5));
        autoConnectComposite.setBackgroundMode(SWT.INHERIT_NONE);

        Label autoConnectLabel = new Label(autoConnectComposite, SWT.WRAP);
        autoConnectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));
        autoConnectLabel
            .setText("Automatically connect to XMPP/Jabber server on Eclipse startup?");

        this.autoConnectButton = new Button(autoConnectComposite, SWT.CHECK
            | SWT.LEFT);
        this.autoConnectButton.setText("Connect automatically");

        /*
         * separator
         */
        Label separator = new Label(leftColumn, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        /*
         * skype
         */
        Composite skypeComposite = new IllustratedComposite(leftColumn,
            SWT.TOP, ImageManager.ELCL_BUDDY_SKYPE_CALL);
        skypeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            true));
        skypeComposite.setLayout(LayoutUtils.createGridLayout(2, false, 0, 5));
        skypeComposite.setBackgroundMode(SWT.INHERIT_NONE);

        Label skypeLabel = new Label(skypeComposite, SWT.WRAP);
        skypeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false, 2, 1));
        skypeLabel.setText("Shall your buddies see your Skype username?");

        Composite skypeUsageComposite = new Composite(skypeComposite, SWT.NONE);
        skypeUsageComposite.setLayoutData(new GridData(SWT.BEGINNING,
            SWT.CENTER, false, false));
        skypeUsageComposite.setLayout(LayoutUtils.createGridLayout(2, false, 0,
            5));
        this.skypeUsageButton = new Button(skypeUsageComposite, SWT.CHECK);
        this.skypeUsageButton.setText("Yes, use:");

        Text skypeUsernameText = new Text(skypeComposite, SWT.BORDER);
        skypeUsernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));
        this.skypeUsernameText = new EmptyText(skypeUsernameText,
            "Skype username");

        return leftColumn;
    }

    protected Composite createRightColumn(Composite composite) {
        Group rightColumn = new Group(composite, SWT.NONE);
        rightColumn.setLayout(LayoutUtils.createGridLayout(5, 0));
        rightColumn.setText("Statistic");

        /*
         * statistic submission
         */
        Composite statisticSubmissionComposite = new IllustratedComposite(
            rightColumn, SWT.TOP, ImageManager.ETOOL_STATISTIC);
        statisticSubmissionComposite.setLayoutData(new GridData(SWT.FILL,
            SWT.CENTER, true, true));
        statisticSubmissionComposite.setLayout(LayoutUtils.createGridLayout(0,
            5));
        statisticSubmissionComposite.setBackgroundMode(SWT.INHERIT_NONE);

        Link message = new Link(statisticSubmissionComposite, SWT.WRAP);
        message.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        message.setText(Messages.getString("feedback.statistic.page.request")); //$NON-NLS-1$
        message.addListener(SWT.Selection, new LinkListener());

        statisticSubmissionButton = new Button(statisticSubmissionComposite,
            SWT.CHECK | SWT.WRAP);
        statisticSubmissionButton.setLayoutData(new GridData(SWT.FILL,
            SWT.CENTER, true, false));
        statisticSubmissionButton.setText(Messages
            .getString("feedback.statistic.page.statistic.submission")); //$NON-NLS-1$
        statisticSubmissionButton.setSelection(true);

        /*
         * separator
         */
        Label separator = new Label(rightColumn, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        /*
         * crash report submission
         */
        Composite crashReportSubmissionComposite = new IllustratedComposite(
            rightColumn, SWT.NONE, ImageManager.ETOOL_CRASH_REPORT);
        crashReportSubmissionComposite.setLayoutData(new GridData(SWT.FILL,
            SWT.CENTER, true, true));
        crashReportSubmissionComposite.setLayout(LayoutUtils.createGridLayout(
            0, 5));
        crashReportSubmissionComposite.setBackgroundMode(SWT.INHERIT_NONE);

        errorLogSubmissionButton = new Button(crashReportSubmissionComposite,
            SWT.CHECK | SWT.WRAP);
        errorLogSubmissionButton.setLayoutData(new GridData(SWT.FILL,
            SWT.BOTTOM, false, false));
        errorLogSubmissionButton.setText(Messages
            .getString("feedback.statistic.page.error.log")); //$NON-NLS-1$
        errorLogSubmissionButton.setSelection(true);

        return rightColumn;
    }

    protected void setInitialValues() {
        this.autoConnectButton.setSelection(preferenceUtils.isAutoConnecting());

        String skypeUsername = preferenceUtils.getSkypeUserName();
        this.skypeUsageButton.setSelection(!skypeUsername.isEmpty());
        this.skypeUsernameText.setText(skypeUsername);

        this.statisticSubmissionButton.setSelection(statisticManager
            .isStatisticSubmissionAllowed());

        this.errorLogSubmissionButton.setSelection(errorLogManager
            .isErrorLogSubmissionAllowed());
    }

    protected void hookListeners() {
        this.skypeUsageButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSkypeUsernameEnablement();
            }
        });

        this.skypeUsernameText.getControl().addFocusListener(
            new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (skypeUsernameText.getText().isEmpty()) {
                        skypeUsageButton.setSelection(false);
                        updateSkypeUsernameEnablement();
                    }
                }
            });

        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                updatePageCompletion();
            }
        };
        this.autoConnectButton.addListener(SWT.Selection, listener);
        this.skypeUsageButton.addListener(SWT.Selection, listener);
        this.skypeUsernameText.getControl().addListener(SWT.Modify, listener);
        this.statisticSubmissionButton.addListener(SWT.Selection, listener);
        this.errorLogSubmissionButton.addListener(SWT.Selection, listener);
    }

    protected void updateSkypeUsernameEnablement() {
        boolean selected = skypeUsageButton.getSelection();
        skypeUsernameText.setEnabled(selected);
        if (selected)
            skypeUsernameText.setFocus();
        else
            skypeUsageButton.setFocus();
    }

    protected void updatePageCompletion() {
        setPageComplete(true);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible)
            return;

        updateSkypeUsernameEnablement();
    }

    /*
     * WizardPage Results
     */

    public boolean isAutoConnect() {
        return this.autoConnectButton.getSelection();
    }

    public boolean isSkypeUsage() {
        return this.skypeUsageButton.getSelection();
    }

    public String getSkypeUsername() {
        return this.skypeUsernameText.getText();
    }

    public boolean isStatisticSubmissionAllowed() {
        return this.statisticSubmissionButton.getSelection();
    }

    public boolean isErrorLogSubmissionAllowed() {
        return this.errorLogSubmissionButton.getSelection();
    }
}
