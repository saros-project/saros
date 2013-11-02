package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.SummaryItemComposite;
import de.fu_berlin.inf.dpp.ui.wizards.ConfigurationWizard;
import de.fu_berlin.inf.dpp.util.ArrayUtils;
import de.fu_berlin.inf.nebula.explanation.note.SimpleNoteComposite;
import de.fu_berlin.inf.nebula.utils.FontUtils;
import de.fu_berlin.inf.nebula.utils.LayoutUtils;
import de.fu_berlin.inf.nebula.widgets.SimpleIllustratedComposite;
import de.fu_berlin.inf.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

/**
 * Final {@link WizardPage} for the {@link ConfigurationWizard} that summarizes
 * the made configuration.
 * 
 * @author bkahlert
 */
public class ConfigurationSummaryWizardPage extends WizardPage {
    public static final String TITLE = de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_title;
    public static final String DESCRIPTION = de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_description;

    Composite composite;
    SimpleIllustratedComposite jid;
    SimpleIllustratedComposite autoConnection;
    SimpleIllustratedComposite uPnPOption;
    SimpleIllustratedComposite skypeUsername;
    SimpleIllustratedComposite statisticSubmission;
    SimpleIllustratedComposite errorLogSubmission;

    private static final Logger log = Logger
        .getLogger(ConfigurationSummaryWizardPage.class);

    public ConfigurationSummaryWizardPage() {
        super(ConfigurationSummaryWizardPage.class.getName());
        SarosPluginContext.initComponent(this);
        setTitle(TITLE);
        setDescription(DESCRIPTION);
    }

    @Override
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
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_label_success);
        FontUtils.makeBold(successLabel);

        SimpleNoteComposite check = new SimpleNoteComposite(
            autoConnectComposite,
            SWT.BORDER,
            ImageManager.ELCL_PREFERENCES_OPEN,
            de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_check_settings);
        check.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        check.setSpacing(5);

        SimpleNoteComposite addContacts = new SimpleNoteComposite(
            autoConnectComposite,
            SWT.BORDER,
            ImageManager.ELCL_CONTACT_ADD,
            de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_addContacts);

        addContacts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));
        addContacts.setSpacing(5);

        SimpleNoteComposite shareProjects = new SimpleNoteComposite(
            autoConnectComposite,
            SWT.BORDER,
            ImageManager.ELCL_PROJECT_SHARE,
            de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_share_project);
        shareProjects.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));
        shareProjects.setSpacing(5);

        return leftColumn;
    }

    protected Composite createRightColumn(Composite composite) {
        Group rightColumn = new Group(composite, SWT.NONE);
        rightColumn.setLayout(LayoutUtils.createGridLayout(5, 0));
        rightColumn
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_right_column_your_configuration);

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

        this.uPnPOption = new SummaryItemComposite(networkSettingsComposite,
            SWT.NONE);
        this.uPnPOption.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));

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

        List<EnterXMPPAccountWizardPage> enterXMPPAccountWizardPages = ArrayUtils
            .getInstances(getWizard().getPages(),
                EnterXMPPAccountWizardPage.class);

        List<ConfigurationSettingsWizardPage> configurationSettingsWizardPages = ArrayUtils
            .getInstances(getWizard().getPages(),
                ConfigurationSettingsWizardPage.class);

        if (enterXMPPAccountWizardPages.isEmpty()
            || configurationSettingsWizardPages.isEmpty())
            return;

        EnterXMPPAccountWizardPage enterXMPPAccountWizardPage = enterXMPPAccountWizardPages
            .get(0);

        ConfigurationSettingsWizardPage configurationSettingsWizardPage = configurationSettingsWizardPages
            .get(0);

        JID jid = enterXMPPAccountWizardPage.getJID();
        boolean autoConnect = configurationSettingsWizardPage.isAutoConnect();
        boolean uPnPEnabled = configurationSettingsWizardPage
            .getPortmappingDevice() != null;
        String skypeUsername = configurationSettingsWizardPage.isSkypeUsage() ? configurationSettingsWizardPage
            .getSkypeUsername() : ""; //$NON-NLS-1$
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
                this.autoConnection
                    .setContent(new IllustratedText(
                        ImageManager.ELCL_XMPP_CONNECTED,
                        de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_connect_auto));
            } else {
                this.autoConnection
                    .setContent(new IllustratedText(
                        ImageManager.DLCL_XMPP_CONNECTED,
                        de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_connect_auto_not));
            }
        }

        if (this.uPnPOption != null) {
            if (uPnPEnabled) {
                this.uPnPOption
                    .setContent(new IllustratedText(
                        ImageManager.ICON_UPNP,
                        de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_use_upnp));
            } else {

                Image disabledUPnP = null;
                try {
                    disabledUPnP = new Image(null, ImageManager.ICON_UPNP,
                        SWT.IMAGE_DISABLE);
                } catch (Exception e) {
                    log.debug("Unable to convert image:" + e.getMessage()); //$NON-NLS-1$
                }
                if (disabledUPnP != null)
                    this.uPnPOption
                        .setContent(new IllustratedText(
                            disabledUPnP,
                            de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_use_upnp_not));
            }
        }

        if (this.skypeUsername != null) {
            if (!skypeUsername.isEmpty()) {
                this.skypeUsername
                    .setContent(new IllustratedText(
                        ImageManager.ELCL_CONTACT_SKYPE_CALL,
                        MessageFormat
                            .format(
                                de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_skype_show_username,
                                skypeUsername)));
            } else {
                this.skypeUsername
                    .setContent(new IllustratedText(
                        ImageManager.DLCL_CONTACT_SKYPE_CALL,
                        de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSummaryWizardPage_skype_show_username_not));
            }
        }

        if (this.statisticSubmission != null) {
            if (statisticSubmissionAllowed) {
                this.statisticSubmission
                    .setContent(new IllustratedText(
                        ImageManager.ETOOL_STATISTIC,
                        Messages
                            .getString("feedback.statistic.page.statistic.submission"))); //$NON-NLS-1$
            } else {
                this.statisticSubmission
                    .setContent(new IllustratedText(
                        ImageManager.DTOOL_STATISTIC,
                        Messages
                            .getString("feedback.statistic.page.statistic.noSubmission"))); //$NON-NLS-1$
            }
        }

        if (this.errorLogSubmission != null) {
            if (errorLogSubmissionAllowed) {
                this.errorLogSubmission.setContent(new IllustratedText(
                    ImageManager.ETOOL_CRASH_REPORT, Messages
                        .getString("feedback.statistic.page.error.log"))); //$NON-NLS-1$
            } else {
                this.errorLogSubmission.setContent(new IllustratedText(
                    ImageManager.DTOOL_CRASH_REPORT, Messages
                        .getString("feedback.statistic.page.error.noLog"))); //$NON-NLS-1$
            }
        }

        this.composite.layout(false, true);
    }
}
