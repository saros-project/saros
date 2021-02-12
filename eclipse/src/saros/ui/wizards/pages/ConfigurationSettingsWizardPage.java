package saros.ui.wizards.pages;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import saros.SarosPluginContext;
import saros.net.upnp.IUPnPService;
import saros.net.upnp.IGateway;
import saros.preferences.Preferences;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.LayoutUtils;
import saros.ui.util.SWTUtils;
import saros.ui.widgets.IllustratedComposite;
import saros.util.ThreadUtils;

/** Allows the user to enter general configuration parameters for use with Saros. */
public class ConfigurationSettingsWizardPage extends WizardPage {

  private static final Logger log = Logger.getLogger(ConfigurationSettingsWizardPage.class);

  public static final String TITLE = saros.ui.Messages.ConfigurationSettingsWizardPage_title;
  public static final String DESCRIPTION =
      saros.ui.Messages.ConfigurationSettingsWizardPage_description;

  @Inject protected Preferences preferences;

  @Inject protected IUPnPService upnpService;

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
  protected Text skypeUsernameText;

  protected Button statisticSubmissionButton;
  protected Button errorLogSubmissionButton;

  private List<IGateway> gateways;

  @Override
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(1, true, 5, 10));
    setControl(composite);

    Composite leftColumn = createLeftColumn(composite);
    leftColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    setInitialValues();
    populateGatewayCombo();
    hookListeners();
    updateGatewaysComboEnablement();
    updatePageCompletion();
  }

  protected Composite createLeftColumn(Composite composite) {
    Group leftColumn = new Group(composite, SWT.NONE);
    leftColumn.setLayout(LayoutUtils.createGridLayout(5, 0));
    leftColumn.setText(saros.ui.Messages.ConfigurationSettingsWizardPage_left_column_connection);

    /*
     * prepare network setting composite
     */
    Composite autoconnectComposite =
        new IllustratedComposite(leftColumn, SWT.TOP, ImageManager.ELCL_XMPP_CONNECTED);
    autoconnectComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    autoconnectComposite.setLayout(LayoutUtils.createGridLayout(0, 5));
    autoconnectComposite.setBackgroundMode(SWT.INHERIT_NONE);

    /*
     * auto connect
     */
    Label autoConnectLabel = new Label(autoconnectComposite, SWT.WRAP);
    autoConnectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    autoConnectLabel.setText(saros.ui.Messages.ConfigurationSettingsWizardPage_label_autoconnect);

    this.autoConnectButton = new Button(autoconnectComposite, SWT.CHECK | SWT.LEFT);
    this.autoConnectButton.setText(
        saros.ui.Messages.ConfigurationSettingsWizardPage_button_autoconnect);

    /*
     * separator
     */
    new Label(leftColumn, SWT.SEPARATOR | SWT.HORIZONTAL)
        .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

    // Gateway port mapping setting
    Composite gatewayComposite =
        new IllustratedComposite(leftColumn, SWT.TOP, ImageManager.ICON_UPNP);
    gatewayComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    gatewayComposite.setLayout(LayoutUtils.createGridLayout(0, 5));
    gatewayComposite.setBackgroundMode(SWT.INHERIT_NONE);

    portmappingLabel = new Label(gatewayComposite, SWT.WRAP);
    portmappingLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    portmappingLabel.setText(saros.ui.Messages.ConfigurationSettingsWizardPage_label_portmapping);

    this.setupPortmappingButton = new Button(gatewayComposite, SWT.CHECK | SWT.LEFT | SWT.WRAP);
    this.setupPortmappingButton.setText(
        saros.ui.Messages.ConfigurationSettingsWizardPage_button_portmapping);
    this.setupPortmappingButton.setToolTipText(
        saros.ui.Messages.ConfigurationSettingsWizardPage_button_portmapping_tooltip);
    setupPortmappingButton.setEnabled(false);

    Composite comboCompo = new Composite(gatewayComposite, SWT.TOP | SWT.LEFT);
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
    Composite skypeComposite =
        new IllustratedComposite(leftColumn, SWT.TOP, ImageManager.ELCL_CONTACT_SKYPE_CALL);
    skypeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    skypeComposite.setLayout(LayoutUtils.createGridLayout(2, false, 0, 5));
    skypeComposite.setBackgroundMode(SWT.INHERIT_NONE);

    Label skypeLabel = new Label(skypeComposite, SWT.WRAP);
    skypeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    skypeLabel.setText(saros.ui.Messages.ConfigurationSettingsWizardPage_label_skype);

    Composite skypeUsageComposite = new Composite(skypeComposite, SWT.NONE);
    skypeUsageComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    skypeUsageComposite.setLayout(LayoutUtils.createGridLayout(2, false, 0, 5));

    this.skypeUsageButton = new Button(skypeUsageComposite, SWT.CHECK);
    this.skypeUsageButton.setText(saros.ui.Messages.ConfigurationSettingsWizardPage_yes_use);

    this.skypeUsernameText = new Text(skypeComposite, SWT.BORDER);
    this.skypeUsernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    return leftColumn;
  }

  protected void setInitialValues() {
    this.autoConnectButton.setSelection(preferences.isAutoConnecting());
    this.setupPortmappingButton.setSelection(preferences.isAutoPortmappingEnabled());

    String skypeUsername = preferences.getSkypeUserName();
    this.skypeUsageButton.setSelection(!skypeUsername.isEmpty());
    this.skypeUsernameText.setText(skypeUsername);
  }

  protected void hookListeners() {
    this.setupPortmappingButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            updateGatewaysComboEnablement();
          }
        });

    this.skypeUsageButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            skypeUsernameText.setEnabled(skypeUsageButton.getSelection());
            if (skypeUsageButton.getSelection()) skypeUsernameText.setFocus();
          }
        });

    Listener listener = e -> updatePageCompletion();

    this.autoConnectButton.addListener(SWT.Selection, listener);
    this.setupPortmappingButton.addListener(SWT.Selection, listener);
    this.skypeUsageButton.addListener(SWT.Selection, listener);
    this.skypeUsernameText.addListener(SWT.Modify, listener);
  }

  protected void updateGatewaysComboEnablement() {
    gatewaysCombo.setEnabled(setupPortmappingButton.getSelection());
  }

  /** Populates the gateway combo box with discovered gateways. */
  protected void populateGatewayCombo() {

    gatewaysCombo.setEnabled(false);
    gatewayInfo.setText(Messages.ConfigurationSettingsWizardPage_0);
    gatewayInfo.pack();

    // do not block during discovery
    ThreadUtils.runSafeAsync(
        "upnp-resolver-cfg",
        null,
        new Runnable() {

          @Override
          public void run() {

            final List<IGateway> currentGateways = upnpService.getGateways(false);

            // GUI work from SWT thread
            SWTUtils.runSafeSWTAsync(
                null,
                new Runnable() {

                  @Override
                  public void run() {
                    if (ConfigurationSettingsWizardPage.this.getControl().isDisposed()) return;

                    populateGatewaySelectionControls(
                        currentGateways, gatewaysCombo, gatewayInfo, setupPortmappingButton);
                  }
                });
          }
        });
  }

  protected void updatePageCompletion() {
    if (skypeUsageButton.getSelection() && skypeUsernameText.getText().trim().isEmpty()) {
      setPageComplete(false);
      return;
    }

    setPageComplete(true);
  }

  /*
   * WizardPage Results
   */

  public boolean isAutoConnect() {
    return this.autoConnectButton.getSelection();
  }

  public IGateway getPortmappingDevice() {
    if (setupPortmappingButton.getSelection()) {
      int sel = gatewaysCombo.getSelectionIndex();
      if (sel != -1) {
        return gateways.get(sel);
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

  /**
   * Setups gateway SWT controls by populating a gateway combobox and configuring an information
   * Label and the enabling checkbox.
   *
   * @param combo {@link Combo} selector to populate with discovered gateways
   * @param info {@link Label} displaying status information of the discovery
   * @param checkbox {@link Button} checkbox to enable/disable UPnP support
   */
  private void populateGatewaySelectionControls(
      List<IGateway> gateways, final Combo combo, final Label info, final Button checkbox) {

    combo.setEnabled(false);
    checkbox.setEnabled(false);
    combo.removeAll();

    // if no devices are found, return now - nothing to populate
    if (gateways == null || gateways.isEmpty()) {
      info.setText(Messages.UPnPUIUtils_no_gateway);
      info.getParent().pack();
      return;
    }

    this.gateways = new ArrayList<IGateway>();

    // insert found gateways into combobox
    for (IGateway gw : gateways) {
      try {
        String name = gw.getFriendlyName();
        if (!gw.isConnected()) name += Messages.UPnPUIUtils_disconnected;

        combo.add(name);
        this.gateways.add(gw);

      } catch (Exception e) {
        log.debug("Error updating UPnP selector:" + e.getMessage()); // $NON-NLS-1$
        // ignore faulty gateway
      }
    }

    // if valid gateway found, show info and enable
    if (combo.getItemCount() > 0) {
      checkbox.setEnabled(true);
      combo.setEnabled(true);
      combo.select(0);
      combo.pack();
      info.setVisible(false);
    } else {
      info.setText(Messages.UPnPUIUtils_no_valid_gateway);
    }
    info.getParent().pack();
  }
}
