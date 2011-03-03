package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleIllustratedComposite;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleIllustratedComposite.IllustratedText;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.note.SimpleNoteComposite;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.SummaryItemComposite;
import de.fu_berlin.inf.dpp.ui.wizards.ConfigurationWizard;
import de.fu_berlin.inf.dpp.ui.wizards.utils.WizardPageUtils;
import de.fu_berlin.inf.dpp.util.FontUtils;

/**
 * Final {@link WizardPage} for the {@link ConfigurationWizard} that summarizes
 * the made configuration.
 * 
 * @author bkahlert
 */
public class ConfigurationSummaryWizardPage extends WizardPage {
    public static final String TITLE = "Configuration Complete";
    public static final String DESCRIPTION = "Please click Finish to complete the Saros configuration.";

    @Inject
    protected Saros saros;

    @Inject
    protected PreferenceUtils preferenceUtils;

    Composite composite;
    SimpleIllustratedComposite jid;
    SimpleIllustratedComposite autoConnection;
    SimpleIllustratedComposite skypeUsername;
    SimpleIllustratedComposite statisticSubmission;
    SimpleIllustratedComposite errorLogSubmission;

    public ConfigurationSummaryWizardPage() {
        super(ConfigurationSummaryWizardPage.class.getName());
        SarosPluginContext.initComponent(this);
        setTitle(TITLE);
        setDescription(DESCRIPTION);
    }

    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(LayoutUtils.createGridLayout(2, true, 5, 10));
        setControl(composite);

        Composite leftColumn = createLeftColumn(composite);
        leftColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite rightColumn = createRightColumn(composite);
        rightColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        updatePageCompletion();
    }

    protected Composite createLeftColumn(Composite composite) {
        Composite leftColumn = new Composite(composite, SWT.NONE);
        leftColumn.setLayout(LayoutUtils.createGridLayout(5, 0));
        // leftColumn.setText("Complete");

        /*
         * auto connect
         */
        Composite autoConnectComposite = new Composite(leftColumn, SWT.NONE);
        autoConnectComposite.setLayoutData(new GridData(SWT.FILL,
            SWT.BEGINNING, true, true));
        autoConnectComposite.setLayout(LayoutUtils.createGridLayout(0, 5));

        Label successLabel = new Label(autoConnectComposite, SWT.WRAP);
        successLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));
        successLabel
            .setText("You are ready to finish your Saros configuration!");
        FontUtils.makeBold(successLabel);

        SimpleNoteComposite check = new SimpleNoteComposite(
            autoConnectComposite, SWT.BORDER,
            ImageManager.ELCL_PREFERENCES_OPEN,
            "Please verify your settings on the right and click Finish.");
        check.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        check.setSpacing(5);

        SimpleNoteComposite addBuddies = new SimpleNoteComposite(
            autoConnectComposite, SWT.BORDER, ImageManager.ELCL_BUDDY_ADD,
            "After completion you can add buddies to your buddy list.");
        addBuddies
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        addBuddies.setSpacing(5);

        SimpleNoteComposite shareProjects = new SimpleNoteComposite(
            autoConnectComposite, SWT.BORDER, ImageManager.ELCL_PROJECT_SHARE,
            "You can share projects via ...\n"
                + "... a right-click on a project or a buddy.\n"
                + "... the Saros menu in the Eclipse menu bar.");
        shareProjects.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));
        shareProjects.setSpacing(5);

        return leftColumn;
    }

    protected Composite createRightColumn(Composite composite) {
        Group rightColumn = new Group(composite, SWT.NONE);
        rightColumn.setLayout(LayoutUtils.createGridLayout(5, 0));
        rightColumn.setText("Your Configuration");

        /*
         * jid settings
         */
        Composite jidSettingsComposite = new Composite(rightColumn, SWT.NONE);
        jidSettingsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, true));
        jidSettingsComposite.setLayout(LayoutUtils.createGridLayout(0, 5));

        this.jid = new SummaryItemComposite(jidSettingsComposite, SWT.BOLD);
        this.jid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        /*
         * separator
         */
        new Label(rightColumn, SWT.SEPARATOR | SWT.HORIZONTAL)
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        /*
         * network settings
         */
        Composite networkSettingsComposite = new Composite(rightColumn,
            SWT.NONE);
        networkSettingsComposite.setLayoutData(new GridData(SWT.FILL,
            SWT.CENTER, true, true));
        networkSettingsComposite.setLayout(LayoutUtils.createGridLayout(0, 5));

        this.autoConnection = new SummaryItemComposite(
            networkSettingsComposite, SWT.NONE);
        this.autoConnection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));
        this.skypeUsername = new SummaryItemComposite(networkSettingsComposite,
            SWT.NONE);
        this.skypeUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));

        /*
         * separator
         */
        new Label(rightColumn, SWT.SEPARATOR | SWT.HORIZONTAL)
            .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        /*
         * statistic settings
         */
        Composite statisticSettingsComposite = new Composite(rightColumn,
            SWT.NONE);
        statisticSettingsComposite.setLayoutData(new GridData(SWT.FILL,
            SWT.CENTER, true, true));
        statisticSettingsComposite
            .setLayout(LayoutUtils.createGridLayout(0, 5));

        this.statisticSubmission = new SummaryItemComposite(
            statisticSettingsComposite, SWT.NONE);
        this.statisticSubmission.setLayoutData(new GridData(SWT.FILL,
            SWT.CENTER, true, false));
        this.errorLogSubmission = new SummaryItemComposite(
            statisticSettingsComposite, SWT.NONE);
        this.errorLogSubmission.setLayoutData(new GridData(SWT.FILL,
            SWT.CENTER, true, false));

        return rightColumn;
    }

    protected void updatePageCompletion() {
        setPageComplete(true);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible)
            return;

        List<EnterXMPPAccountWizardPage> enterXMPPAccountWizardPages = WizardPageUtils
            .getPage(this.getWizard(), EnterXMPPAccountWizardPage.class);
        List<ConfigurationSettingsWizardPage> configurationSettingsWizardPages = WizardPageUtils
            .getPage(this.getWizard(), ConfigurationSettingsWizardPage.class);
        if (enterXMPPAccountWizardPages.isEmpty()
            || configurationSettingsWizardPages.isEmpty())
            return;

        EnterXMPPAccountWizardPage enterXMPPAccountWizardPage = enterXMPPAccountWizardPages
            .get(0);
        ConfigurationSettingsWizardPage configurationSettingsWizardPage = configurationSettingsWizardPages
            .get(0);

        JID jid = enterXMPPAccountWizardPage.getJID();
        boolean autoConnect = configurationSettingsWizardPage.isAutoConnect();
        String skypeUsername = configurationSettingsWizardPage.isSkypeUsage() ? configurationSettingsWizardPage
            .getSkypeUsername() : "";
        boolean statisticSubmissionAllowed = configurationSettingsWizardPage
            .isStatisticSubmissionAllowed();
        boolean errorLogSubmissionAllowed = configurationSettingsWizardPage
            .isErrorLogSubmissionAllowed();

        if (this.jid != null) {
            this.jid.setContent(new IllustratedText(ImageManager.ELCL_SPACER,
                jid.getBase()));
        }

        if (this.autoConnection != null) {
            if (autoConnect) {
                this.autoConnection.setContent(new IllustratedText(
                    ImageManager.ELCL_XMPP_CONNECTED, "Connect automatically"));
            } else {
                this.autoConnection.setContent(new IllustratedText(
                    ImageManager.DLCL_XMPP_CONNECTED,
                    "Do not connect automatically"));
            }
        }

        if (this.skypeUsername != null) {
            if (!skypeUsername.isEmpty()) {
                this.skypeUsername.setContent(new IllustratedText(
                    ImageManager.ELCL_BUDDY_SKYPE_CALL, "Show Skype username: "
                        + skypeUsername));
            } else {
                this.skypeUsername.setContent(new IllustratedText(
                    ImageManager.DLCL_BUDDY_SKYPE_CALL,
                    "Do not show my Skype username"));
            }
        }

        if (this.statisticSubmission != null) {
            if (statisticSubmissionAllowed) {
                this.statisticSubmission
                    .setContent(new IllustratedText(
                        ImageManager.ETOOL_STATISTIC,
                        Messages
                            .getString("feedback.statistic.page.statistic.submission")));
            } else {
                this.statisticSubmission
                    .setContent(new IllustratedText(
                        ImageManager.DTOOL_STATISTIC,
                        Messages
                            .getString("feedback.statistic.page.statistic.noSubmission")));
            }
        }

        if (this.errorLogSubmission != null) {
            if (errorLogSubmissionAllowed) {
                this.errorLogSubmission.setContent(new IllustratedText(
                    ImageManager.ETOOL_CRASH_REPORT, Messages
                        .getString("feedback.statistic.page.error.log")));
            } else {
                this.errorLogSubmission.setContent(new IllustratedText(
                    ImageManager.DTOOL_CRASH_REPORT, Messages
                        .getString("feedback.statistic.page.error.noLog")));
            }
        }

        this.composite.layout(false, true);
    }
}
