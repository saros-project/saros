package de.fu_berlin.inf.dpp.ui.preferencePages;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.net.util.NetworkingUtils;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.bitlet.weupnp.GatewayDevice;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

/** @author Stefan Rossbach */
@Component(module = "prefs")
public final class NetworkPreferencePage extends PreferencePage
    implements IWorkbenchPreferencePage {

  @Inject private Saros saros;

  @Inject private IUPnPService upnpService;

  private Object lastCheckedItem;

  private Label gatewayInfo;

  private Table upnpDevicesTable;

  private Button buttonOnlyAllowIBB;
  private Button buttonOnlyAllowMediatedSocks5;
  private Text localSocks5PortText;
  private Button buttonAllowAlternativeSocks5Port;

  private Text stunIPAddressText;
  private Text stunPortText;

  private Text localSocks5CandidatesText;
  private Button buttonIncludeUPNPGatewayAddress;
  private Button buttonQueryLocalCandidates;

  public NetworkPreferencePage() {

    SarosPluginContext.initComponent(this);

    setPreferenceStore(saros.getPreferenceStore());
    setDescription(Messages.NetworkPreferencePage_network_settings);
  }

  @Override
  public void init(IWorkbench workbench) {
    // No init necessary
  }

  @Override
  public boolean performOk() {

    setErrorMessage(null);

    final String stunIPAddress = stunIPAddressText.getText().trim();
    final String stunPort = stunPortText.getText().trim();

    if (!stunIPAddress.isEmpty() && !checkStunIpAddress(stunIPAddress)) {
      setErrorMessage(Messages.NetworkPreferencePage_text_stun_server_not_valid);
      return false;
    }

    if (!stunPort.isEmpty() && !checkPort(stunPortText.getText())) {
      setErrorMessage(Messages.NetworkPreferencePage_text_stun_server_not_valid2);
      return false;
    }

    if (!checkPort(localSocks5PortText.getText())) {
      setErrorMessage(Messages.NetworkPreferencePage_text_direct_connection_not_valid);
      return false;
    }

    getPreferenceStore()
        .setValue(PreferenceConstants.FORCE_IBB_CONNECTIONS, buttonOnlyAllowIBB.getSelection());

    getPreferenceStore()
        .setValue(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED,
            buttonOnlyAllowMediatedSocks5.getSelection() || buttonOnlyAllowIBB.getSelection());

    getPreferenceStore()
        .setValue(
            PreferenceConstants.FILE_TRANSFER_PORT, Integer.valueOf(localSocks5PortText.getText()));

    getPreferenceStore()
        .setValue(
            PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER,
            buttonAllowAlternativeSocks5Port.getSelection());

    getPreferenceStore()
        .setValue(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_CANDIDATES, localSocks5CandidatesText.getText());

    getPreferenceStore()
        .setValue(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_USE_UPNP_EXTERNAL_ADDRESS,
            buttonIncludeUPNPGatewayAddress.getSelection());

    getPreferenceStore().setValue(PreferenceConstants.STUN, stunIPAddress);

    getPreferenceStore()
        .setValue(
            PreferenceConstants.STUN_PORT, stunPort.isEmpty() ? 0 : Integer.valueOf(stunPort));

    final String currentSavedUPNPDeviceID =
        getPreferenceStore().getString(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID);

    String currentSelectedUPNPDeviceID = null;

    for (TableItem item : upnpDevicesTable.getItems()) {
      if (!item.getChecked()) continue;

      currentSelectedUPNPDeviceID = (String) item.getData();
    }

    if (currentSelectedUPNPDeviceID != null) {
      getPreferenceStore()
          .setValue(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID, currentSelectedUPNPDeviceID);

      if (!currentSelectedUPNPDeviceID.equals(currentSavedUPNPDeviceID))
        SarosView.showNotification(
            Messages.NetworkPreferencePage_upnp_activation,
            Messages.NetworkPreferencePage_upnp_activation_text);

    } else {
      getPreferenceStore().setToDefault(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID);
    }

    return super.performOk();
  }

  @Override
  protected Control createContents(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(1, true);
    layout.verticalSpacing = 15;
    composite.setLayout(layout);

    Label socks5GroupDescriptionLabel = new Label(composite, SWT.NONE);
    socks5GroupDescriptionLabel.setText(Messages.NetworkPreferencePage_sock5_label_text);

    createSocks5OptionsGroup(composite);

    Label stunGroupDescriptionLabel = new Label(composite, SWT.NONE);
    stunGroupDescriptionLabel.setText(Messages.NetworkPreferencePage_stungroup_label_text);
    createStunServerGroup(composite);

    Label upnpGroupDescriptionLabel = new Label(composite, SWT.NONE);
    upnpGroupDescriptionLabel.setText(Messages.NetworkPreferencePage_upnp_label_Text);

    createUpnpGroup(composite);
    createAdvancedSocks5Group(composite);

    initialize();

    updateUpnpDevicesTable(new ArrayList<GatewayDevice>(), null);
    discoverUpnpGateways();
    return composite;
  }

  private void initialize() {
    stunIPAddressText.setText(getPreferenceStore().getString(PreferenceConstants.STUN));

    int stunPort = getPreferenceStore().getInt(PreferenceConstants.STUN_PORT);

    stunPortText.setText(stunPort == 0 ? "" : String.valueOf(stunPort));

    localSocks5PortText.setText(
        String.valueOf(getPreferenceStore().getInt(PreferenceConstants.FILE_TRANSFER_PORT)));

    boolean ibbIsForced =
        saros.getPreferenceStore().getBoolean(PreferenceConstants.FORCE_IBB_CONNECTIONS);
    boolean localSocks5ProxyDisabled =
        saros.getPreferenceStore().getBoolean(PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED);

    buttonOnlyAllowIBB.setSelection(ibbIsForced);

    buttonOnlyAllowMediatedSocks5.setSelection(!ibbIsForced && localSocks5ProxyDisabled);

    buttonAllowAlternativeSocks5Port.setSelection(
        saros
            .getPreferenceStore()
            .getBoolean(PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER));

    localSocks5CandidatesText.setText(
        getPreferenceStore().getString(PreferenceConstants.LOCAL_SOCKS5_PROXY_CANDIDATES));

    buttonIncludeUPNPGatewayAddress.setSelection(
        getPreferenceStore()
            .getBoolean(PreferenceConstants.LOCAL_SOCKS5_PROXY_USE_UPNP_EXTERNAL_ADDRESS));

    // upnp gateway information is handled by the UpnpManager !

    updateCompositeStates();
  }

  @Override
  protected void performDefaults() {
    stunIPAddressText.setText(getPreferenceStore().getDefaultString(PreferenceConstants.STUN));

    int stunPort = getPreferenceStore().getDefaultInt(PreferenceConstants.STUN_PORT);

    stunPortText.setText(stunPort == 0 ? "" : String.valueOf(stunPort));

    localSocks5PortText.setText(
        String.valueOf(getPreferenceStore().getDefaultInt(PreferenceConstants.FILE_TRANSFER_PORT)));

    localSocks5PortText.setText(
        String.valueOf(getPreferenceStore().getInt(PreferenceConstants.FILE_TRANSFER_PORT)));

    buttonOnlyAllowIBB.setSelection(
        saros.getPreferenceStore().getDefaultBoolean(PreferenceConstants.FORCE_IBB_CONNECTIONS));

    buttonOnlyAllowMediatedSocks5.setSelection(
        saros
            .getPreferenceStore()
            .getDefaultBoolean(PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED));

    buttonAllowAlternativeSocks5Port.setSelection(
        saros
            .getPreferenceStore()
            .getDefaultBoolean(PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER));

    localSocks5CandidatesText.setText(
        getPreferenceStore().getDefaultString(PreferenceConstants.LOCAL_SOCKS5_PROXY_CANDIDATES));

    buttonIncludeUPNPGatewayAddress.setSelection(
        getPreferenceStore()
            .getDefaultBoolean(PreferenceConstants.LOCAL_SOCKS5_PROXY_USE_UPNP_EXTERNAL_ADDRESS));

    for (TableItem item : upnpDevicesTable.getItems()) item.setChecked(false);

    super.performDefaults();

    updateCompositeStates();
  }

  private void updateCompositeStates() {
    buttonOnlyAllowIBB.setEnabled(false);
    buttonOnlyAllowMediatedSocks5.setEnabled(false);
    localSocks5PortText.setEnabled(false);
    buttonAllowAlternativeSocks5Port.setEnabled(false);
    localSocks5CandidatesText.setEnabled(false);
    buttonIncludeUPNPGatewayAddress.setEnabled(false);
    buttonQueryLocalCandidates.setEnabled(false);

    if (buttonOnlyAllowIBB.getSelection()) {
      buttonOnlyAllowIBB.setEnabled(true);
      return;
    }

    if (buttonOnlyAllowMediatedSocks5.getSelection()) {
      buttonOnlyAllowMediatedSocks5.setEnabled(true);
      return;
    }

    buttonOnlyAllowIBB.setEnabled(true);
    buttonOnlyAllowMediatedSocks5.setEnabled(true);
    localSocks5PortText.setEnabled(true);
    buttonAllowAlternativeSocks5Port.setEnabled(true);
    localSocks5CandidatesText.setEnabled(true);
    buttonIncludeUPNPGatewayAddress.setEnabled(true);
    buttonQueryLocalCandidates.setEnabled(true);
  }

  private Group createUpnpGroup(Composite parent) {

    Group group = new Group(parent, SWT.NONE);
    group.setText(Messages.NetworkPreferencePage_upnp_devices);

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;

    group.setLayout(gridLayout);
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

    gatewayInfo = new Label(group, SWT.CENTER);
    gatewayInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    upnpDevicesTable = new Table(group, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);

    upnpDevicesTable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    upnpDevicesTable.addListener(
        SWT.Selection,
        new Listener() {
          @Override
          public void handleEvent(Event event) {
            if (event.detail == SWT.CHECK) handleGatewaySelection(event);
          }
        });

    gatewayInfo.setVisible(false);
    upnpDevicesTable.setVisible(false);

    return group;
  }

  private Group createSocks5OptionsGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);

    group.setText(Messages.NetworkPreferencePage_connection_established);

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;

    group.setLayout(gridLayout);
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    buttonOnlyAllowIBB = new Button(group, SWT.CHECK);
    buttonOnlyAllowIBB.setText(Messages.NetworkPreferencePage_button_establish_connection);
    buttonOnlyAllowIBB.setToolTipText(Messages.NetworkPreferencePage_tooltip_establish_connection);

    buttonOnlyAllowIBB.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            updateCompositeStates();
          }
        });

    buttonOnlyAllowMediatedSocks5 = new Button(group, SWT.CHECK);
    buttonOnlyAllowMediatedSocks5.setText(
        Messages.NetworkPreferencePage_buttonOnlyAllowMediatedSocks5_text);
    buttonOnlyAllowMediatedSocks5.setToolTipText(
        Messages.NetworkPreferencePage_buttonOnlyAllowMediatedSocks5_tooltip);

    buttonOnlyAllowMediatedSocks5.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            updateCompositeStates();
          }
        });

    Composite row = new Composite(group, SWT.NONE);
    row.setLayout(new GridLayout(2, false));
    row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    Label localSocks5PortLabel = new Label(row, SWT.CENTER);
    localSocks5PortLabel.setText(Messages.NetworkPreferencePage_localSocks5PortLabel_text);
    localSocks5PortLabel.setToolTipText(
        Messages.NetworkPreferencePage_localSocks5PortLabel_tooltip);

    localSocks5PortText = new Text(row, SWT.SINGLE | SWT.BORDER);
    localSocks5PortText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    buttonAllowAlternativeSocks5Port = new Button(group, SWT.CHECK);
    buttonAllowAlternativeSocks5Port.setText(
        Messages.NetworkPreferencePage_buttonAllowAlternativeSocks5Port_text);
    buttonAllowAlternativeSocks5Port.setToolTipText(
        Messages.NetworkPreferencePage_buttonAllowAlternativeSocks5Port_tooltip);

    return group;
  }

  private Group createStunServerGroup(Composite parent) {
    Group group = new Group(parent, SWT.NONE);

    group.setText(Messages.NetworkPreferencePage_stun_server);

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;

    group.setLayout(gridLayout);
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    Label ipAddressLabel = new Label(group, SWT.CENTER);
    ipAddressLabel.setText(Messages.NetworkPreferencePage_adress);
    ipAddressLabel.setToolTipText(Messages.NetworkPreferencePage_adress_tooltip);

    stunIPAddressText = new Text(group, SWT.SINGLE | SWT.BORDER);
    stunIPAddressText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    Label portLabel = new Label(group, SWT.CENTER);
    portLabel.setText(Messages.NetworkPreferencePage_port);
    portLabel.setToolTipText(Messages.NetworkPreferencePage_port_tooltip);

    stunPortText = new Text(group, SWT.SINGLE | SWT.BORDER);
    stunPortText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    return group;
  }

  private Group createAdvancedSocks5Group(Composite parent) {
    Group group = new Group(parent, SWT.NONE);

    group.setText("Additional Socks5 Proxy options");

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    gridLayout.makeColumnsEqualWidth = false;

    group.setLayout(gridLayout);
    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    Label socks5CandidatesLabel = new Label(group, SWT.CENTER);
    socks5CandidatesLabel.setText("Socks5 candidates:");
    socks5CandidatesLabel.setToolTipText(
        "Comma separated list of Socks5 candidates (IP addresses or host names) that should be used during Socks5 connection establishment.\nLeave blank to use all local available IP addresses as candidates.");

    socks5CandidatesLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    localSocks5CandidatesText = new Text(group, SWT.SINGLE | SWT.BORDER);
    localSocks5CandidatesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    buttonQueryLocalCandidates = new Button(group, SWT.PUSH);

    buttonQueryLocalCandidates.addSelectionListener(
        new SelectionListener() {

          @Override
          public void widgetDefaultSelected(SelectionEvent event) {
            // NOP
          }

          @Override
          public void widgetSelected(SelectionEvent event) {
            try {
              List<InetAddress> addresses = NetworkingUtils.getAllNonLoopbackLocalIPAddresses(true);

              List<String> ipLiterals = new ArrayList<String>();

              for (InetAddress address : addresses) ipLiterals.add(address.getHostAddress());

              localSocks5CandidatesText.setText(StringUtils.join(ipLiterals, ", "));

            } catch (Exception e) {
              // ignore
              return;
            }
          }
        });

    buttonQueryLocalCandidates.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

    buttonQueryLocalCandidates.setText("Query local Socks5 candidates");

    buttonIncludeUPNPGatewayAddress = new Button(group, SWT.CHECK);

    buttonIncludeUPNPGatewayAddress.setText("Include external IP address of UPNP gateway(s)");

    buttonIncludeUPNPGatewayAddress.setToolTipText(
        "If checked the external IP address of the selected UPNP gateways(s) will be included in the Socks5 candidate list");

    GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);

    data.horizontalSpan = 3;
    buttonIncludeUPNPGatewayAddress.setLayoutData(data);

    return group;
  }

  private void discoverUpnpGateways() {
    gatewayInfo.setText(Messages.NetworkPreferencePage_discover_upnp_gateway);
    gatewayInfo.pack();
    gatewayInfo.setVisible(true);

    ThreadUtils.runSafeAsync(
        "dpp-upnp-resolver-cfg",
        null,
        new Runnable() {

          @Override
          public void run() {

            final List<GatewayDevice> gateways = upnpService.getGateways(false);

            SWTUtils.runSafeSWTAsync(
                null,
                new Runnable() {
                  @Override
                  public void run() {

                    if (gatewayInfo.isDisposed() || upnpDevicesTable.isDisposed()) return;

                    if (gateways == null || gateways.isEmpty()) {
                      gatewayInfo.setText(Messages.NetworkPreferencePage_upnp_device_not_found);
                      gatewayInfo.pack();
                      return;
                    }

                    gatewayInfo.setVisible(false);
                    updateUpnpDevicesTable(
                        gateways,
                        getPreferenceStore()
                            .getString(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID));
                    upnpDevicesTable.setVisible(true);
                  }
                });
          }
        });
  }

  private void handleGatewaySelection(Event event) {

    for (TableItem item : upnpDevicesTable.getItems()) {
      if (item.equals(event.item) && !item.equals(lastCheckedItem)) {
        lastCheckedItem = item;
        item.setChecked(true);
      } else if (item.equals(event.item) && item.equals(lastCheckedItem)) {
        item.setChecked(false);
        lastCheckedItem = null;
      } else item.setChecked(false);
    }
  }

  private void updateUpnpDevicesTable(List<GatewayDevice> gateways, String defaultGatewayID) {
    upnpDevicesTable.setLinesVisible(true);
    upnpDevicesTable.setHeaderVisible(true);

    while (upnpDevicesTable.getColumnCount() > 0) upnpDevicesTable.getColumn(0).dispose();

    upnpDevicesTable.removeAll();

    TableColumn column;
    TableItem item;

    column = new TableColumn(upnpDevicesTable, SWT.NONE);
    column.setText(Messages.NetworkPreferencePage_allow_port_mapping);

    column = new TableColumn(upnpDevicesTable, SWT.NONE);
    column.setText(Messages.NetworkPreferencePage_device);

    column = new TableColumn(upnpDevicesTable, SWT.NONE);
    column.setText(Messages.NetworkPreferencePage_local_ip);

    column = new TableColumn(upnpDevicesTable, SWT.NONE);
    column.setText(Messages.NetworkPreferencePage_external_ip);

    column = new TableColumn(upnpDevicesTable, SWT.NONE);
    column.setText(Messages.NetworkPreferencePage_device_ip);

    for (GatewayDevice device : gateways) {
      String name;
      String internalIpAddress;
      String externalIpAddress;
      String deviceIpAddress;

      name = device.getFriendlyName();
      internalIpAddress = device.getLocalAddress().getHostAddress();
      deviceIpAddress = device.getDeviceAddress().getHostAddress();

      try {
        if (!device.isConnected()) externalIpAddress = Messages.NetworkPreferencePage_not_connected;
        else externalIpAddress = device.getExternalIPAddress();
      } catch (Exception e) {
        externalIpAddress = Messages.NetworkPreferencePage_not_available;
      }

      item = new TableItem(upnpDevicesTable, SWT.NULL);
      item.setText(1, name);
      item.setText(2, internalIpAddress);
      item.setText(3, externalIpAddress);
      item.setText(4, deviceIpAddress);
      item.setData(device.getUSN());

      if (device.getUSN().equals(defaultGatewayID)) {
        lastCheckedItem = item;
        item.setChecked(true);
      }
    }

    for (int i = 0; i < upnpDevicesTable.getColumnCount(); i++)
      upnpDevicesTable.getColumn(i).pack();
  }

  private boolean checkStunIpAddress(String address) {
    try {
      InetAddress.getByName(address);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean checkPort(String port) {
    try {
      int p = Integer.valueOf(port);
      if (p <= 0 || p >= 65536) return false;
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
