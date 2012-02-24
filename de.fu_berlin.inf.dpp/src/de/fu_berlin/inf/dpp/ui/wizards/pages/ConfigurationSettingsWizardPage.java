package de.fu_berlin.inf.dpp.ui.wizards.pages;

import org.bitlet.weupnp.GatewayDevice;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.util.UPnPUIUtils;
import de.fu_berlin.inf.dpp.ui.widgets.decoration.EmptyText;
import de.fu_berlin.inf.dpp.util.LinkListener;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.nebula.utils.LayoutUtils;
import de.fu_berlin.inf.nebula.widgets.IllustratedComposite;

/**
 * Allows the user to enter general configuration parameters for use with Saros.
 * 
 * @author bkahlert
 */
public class ConfigurationSettingsWizardPage extends WizardPage {
    public static final String TITLE = de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_title;
    public static final String DESCRIPTION = de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_description;

    @Inject
    protected Saros saros;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected StatisticManager statisticManager;

    @Inject
    protected ErrorLogManager errorLogManager;

    @Inject
    protected IUPnPService upnpService;

    public ConfigurationSettingsWizardPage() {
        super(ConfigurationSettingsWizardPage.class.getName());
        SarosPluginContext.initComponent(this);
        setTitle(TITLE);
        setDescription(DESCRIPTION);
    }

    protected Button setupPortmappingButton;
    protected Label portmappingLabel;
    protected Combo gatewaysCombo;
    protected Label gatewayInfo;

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
        populateGatewayCombo();
        hookListeners();
        updateSkypeUsernameEnablement();
        updateGatewaysComboEnablement();
        updatePageCompletion();
    }

    protected Composite createLeftColumn(Composite composite) {
        Group leftColumn = new Group(composite, SWT.NONE);
        leftColumn.setLayout(LayoutUtils.createGridLayout(5, 0));
        leftColumn
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_left_column_connection);

        /*
         * prepare network setting composite
         */
        Composite autoconnectComposite = new IllustratedComposite(leftColumn,
            SWT.TOP, ImageManager.ELCL_XMPP_CONNECTED);
        autoconnectComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, true));
        autoconnectComposite.setLayout(LayoutUtils.createGridLayout(0, 5));
        autoconnectComposite.setBackgroundMode(SWT.INHERIT_NONE);

        /*
         * auto connect
         */
        Label autoConnectLabel = new Label(autoconnectComposite, SWT.WRAP);
        autoConnectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));
        autoConnectLabel
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_label_autoconnect);

        this.autoConnectButton = new Button(autoconnectComposite, SWT.CHECK
            | SWT.LEFT);
        this.autoConnectButton
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_button_autoconnect);

        /*
         * separator
         */
        new Label(leftColumn, SWT.SEPARATOR | SWT.HORIZONTAL)
            .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // Gateway port mapping setting
        Composite gatewayComposite = new IllustratedComposite(leftColumn,
            SWT.TOP, ImageManager.ICON_UPNP);
        gatewayComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            true));
        gatewayComposite.setLayout(LayoutUtils.createGridLayout(0, 5));
        gatewayComposite.setBackgroundMode(SWT.INHERIT_NONE);

        portmappingLabel = new Label(gatewayComposite, SWT.WRAP);
        portmappingLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));
        portmappingLabel
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_label_portmapping);

        this.setupPortmappingButton = new Button(gatewayComposite, SWT.CHECK
            | SWT.LEFT | SWT.WRAP);
        this.setupPortmappingButton
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_button_portmapping);
        this.setupPortmappingButton
            .setToolTipText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_button_portmapping_tooltip);
        setupPortmappingButton.setEnabled(false);

        Composite comboCompo = new Composite(gatewayComposite, SWT.TOP
            | SWT.LEFT);
        RowLayout rowLayout = new RowLayout();
        rowLayout.marginLeft = 16;
        comboCompo.setLayout(rowLayout);
        gatewaysCombo = new Combo(comboCompo, SWT.DROP_DOWN | SWT.READ_ONLY);
        gatewayInfo = new Label(comboCompo, SWT.NONE);
        gatewayInfo.setEnabled(false);

        /*
         * separator
         */
        new Label(leftColumn, SWT.SEPARATOR | SWT.HORIZONTAL)
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
        skypeLabel
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_label_skype);

        Composite skypeUsageComposite = new Composite(skypeComposite, SWT.NONE);
        skypeUsageComposite.setLayoutData(new GridData(SWT.BEGINNING,
            SWT.CENTER, false, false));
        skypeUsageComposite.setLayout(LayoutUtils.createGridLayout(2, false, 0,
            5));
        this.skypeUsageButton = new Button(skypeUsageComposite, SWT.CHECK);
        this.skypeUsageButton
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_yes_use);

        Text skypeUsernameText = new Text(skypeComposite, SWT.BORDER);
        skypeUsernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));
        this.skypeUsernameText = new EmptyText(
            skypeUsernameText,
            de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_skype_username);

        return leftColumn;
    }

    protected Composite createRightColumn(Composite composite) {
        Group rightColumn = new Group(composite, SWT.NONE);
        rightColumn.setLayout(LayoutUtils.createGridLayout(5, 0));
        rightColumn
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_statistic);

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
        message
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_feedback_descr);
        message.addListener(SWT.Selection, new LinkListener());

        statisticSubmissionButton = new Button(statisticSubmissionComposite,
            SWT.CHECK | SWT.WRAP);
        statisticSubmissionButton.setLayoutData(new GridData(SWT.FILL,
            SWT.CENTER, true, false));
        statisticSubmissionButton
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_feedback_allow_data);
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
        errorLogSubmissionButton
            .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_feedback_allow_crash);
        errorLogSubmissionButton.setSelection(true);

        return rightColumn;
    }

    protected void setInitialValues() {
        this.autoConnectButton.setSelection(preferenceUtils.isAutoConnecting());
        this.setupPortmappingButton.setSelection(preferenceUtils
            .isAutoPortmappingEnabled());

        String skypeUsername = preferenceUtils.getSkypeUserName();
        this.skypeUsageButton.setSelection(!skypeUsername.isEmpty());
        this.skypeUsernameText.setText(skypeUsername);

        this.statisticSubmissionButton.setSelection(statisticManager
            .isStatisticSubmissionAllowed());

        this.errorLogSubmissionButton.setSelection(errorLogManager
            .isErrorLogSubmissionAllowed());
    }

    protected void hookListeners() {
        this.setupPortmappingButton
            .addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateGatewaysComboEnablement();
                }
            });

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
        this.setupPortmappingButton.addListener(SWT.Selection, listener);
        this.skypeUsageButton.addListener(SWT.Selection, listener);
        this.skypeUsernameText.getControl().addListener(SWT.Modify, listener);
        this.statisticSubmissionButton.addListener(SWT.Selection, listener);
        this.errorLogSubmissionButton.addListener(SWT.Selection, listener);
    }

    protected void updateGatewaysComboEnablement() {
        gatewaysCombo.setEnabled(setupPortmappingButton.getSelection());
    }

    protected void updateSkypeUsernameEnablement() {
        boolean selected = skypeUsageButton.getSelection();
        skypeUsernameText.setEnabled(selected);
        if (selected)
            skypeUsernameText.setFocus();
        else
            skypeUsageButton.setFocus();
    }

    /**
     * Populates the gateway combobox with discovered gateways.
     */
    protected void populateGatewayCombo() {
        if (upnpService.getGateways() == null) {
            gatewaysCombo.setEnabled(false);
            gatewayInfo
                .setText(de.fu_berlin.inf.dpp.ui.Messages.ConfigurationSettingsWizardPage_0);
            gatewayInfo.pack();

            // dont block during discovery
            Utils.runSafeAsync(null, new Runnable() {

                public void run() {
                    upnpService.discoverGateways();

                    // GUI work from SWT thread
                    Utils.runSafeSWTAsync(null, new Runnable() {
                        public void run() {
                            UPnPUIUtils.populateGatewaySelectionControls(
                                upnpService, gatewaysCombo, gatewayInfo,
                                setupPortmappingButton);
                        }
                    });
                }
            });

        } else {
            UPnPUIUtils.populateGatewaySelectionControls(upnpService,
                gatewaysCombo, gatewayInfo, setupPortmappingButton);
        }
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

    public GatewayDevice getPortmappingDevice() {
        if (setupPortmappingButton.getSelection()) {
            int sel = gatewaysCombo.getSelectionIndex();
            if (sel != -1) {
                return upnpService.getGateways().get(sel);
            }
        }
        return null;
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
