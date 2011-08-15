package de.fu_berlin.inf.dpp.ui.preferencePages;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.bitlet.weupnp.GatewayDevice;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.UPnP.UPnPManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.Utils;

/** @author Stefan Rossbach */

@Component(module = "prefs")
public final class NetworkPreferencePage extends PreferencePage implements
    IWorkbenchPreferencePage {

    @Inject
    private Saros saros;

    @Inject
    private UPnPManager upnpManager;

    @Inject
    private DataTransferManager dataTransferManager;

    private Object lastCheckedItem;

    private Label gatewayInfo;

    private Table upnpDevicesTable;

    private Button buttonOnlyAllowIBB;
    private Button buttonOnlyAllowMediatedSocks5;
    private Text localSocks5PortText;
    private Button buttonAllowAlternativeSocks5Port;

    private Text stunIpAddressText;
    private Text stunPortAddressText;

    public NetworkPreferencePage() {

        SarosPluginContext.initComponent(this);

        setPreferenceStore(saros.getPreferenceStore());
        setDescription("Saros networks settings:");
    }

    public void init(IWorkbench workbench) {
        // No init necessary
    }

    @Override
    public boolean performOk() {

        setErrorMessage(null);

        if (!checkStunIpAddress(stunIpAddressText.getText())) {
            setErrorMessage("The STUN Server address is either not valid or could not be resolved");
            return false;
        }

        if (!checkPort(stunPortAddressText.getText())) {
            setErrorMessage("The STUN port is not valid. Must be in range of 1 - 65535");
            return false;
        }

        if (!checkPort(localSocks5PortText.getText())) {
            setErrorMessage("The direct connection port is not valid. Must be in range of 1 - 65535");
            return false;
        }

        getPreferenceStore().setValue(
            PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT,
            buttonOnlyAllowIBB.getSelection());

        getPreferenceStore().setValue(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED,
            buttonOnlyAllowMediatedSocks5.getSelection());

        getPreferenceStore().setValue(PreferenceConstants.FILE_TRANSFER_PORT,
            Integer.valueOf(localSocks5PortText.getText()));

        getPreferenceStore().setValue(
            PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER,
            buttonAllowAlternativeSocks5Port.getSelection());

        getPreferenceStore().setValue(PreferenceConstants.STUN,
            stunIpAddressText.getText());

        getPreferenceStore().setValue(PreferenceConstants.STUN_PORT,
            Integer.valueOf(stunPortAddressText.getText()));

        // FIXME: you can only remove a gateway after the discovery is performed
        if (upnpManager.getGateways() != null) {
            upnpManager.setSelectedGateway(null);
            for (TableItem item : upnpDevicesTable.getItems()) {
                if (item.getChecked()) {
                    if (upnpManager.setSelectedGateway((GatewayDevice) item
                        .getData())) {

                        if (!dataTransferManager.disconnectInBandBytestreams())
                            SarosView
                                .showNotification("UPnP Activation",
                                    "For UPnP to take full effect, please reconnect with your XMPP account.");
                    }
                }
            }
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
        socks5GroupDescriptionLabel
            .setText("These options configure the behaviour of how Saros tries to establish a session.\nChanges requires a reconnect to the XMPP Server.");

        createSocks5OptionsGroup(composite);

        Label stunGroupDescriptionLabel = new Label(composite, SWT.NONE);
        stunGroupDescriptionLabel
            .setText("A STUN server is used to determine your public IP address if you are behind a NAT device\n"
                + "which has no or deactivated UPnP support. Please make sure you are forwarding the direct\n"
                + "connection port or no direct connection may be established.");
        createStunServerGroup(composite);

        Label upnpGroupDescriptionLabel = new Label(composite, SWT.NONE);
        upnpGroupDescriptionLabel
            .setText("For a fast file sharing Saros needs a direct TCP connection. Direct TCP connections normally are not possible\n"
                + "if you are behind a Router due to the Network Address Translation (NAT) of the Router. Enabling UPnP port mapping\n"
                + "on your Router allows Saros to communicate with the router to forward a port without your manual intervention.");

        createUpnpGroup(composite);

        initialize();

        updateUpnpDevicesTable(new ArrayList<GatewayDevice>(), null);
        discoverUpnpGateways();
        return composite;
    }

    private void initialize() {
        stunIpAddressText.setText(getPreferenceStore().getString(
            PreferenceConstants.STUN));

        stunPortAddressText.setText(String.valueOf(getPreferenceStore().getInt(
            PreferenceConstants.STUN_PORT)));

        localSocks5PortText.setText(String.valueOf(getPreferenceStore().getInt(
            PreferenceConstants.FILE_TRANSFER_PORT)));

        buttonOnlyAllowIBB.setSelection(saros.getPreferenceStore().getBoolean(
            PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT));

        buttonOnlyAllowMediatedSocks5.setSelection(saros.getPreferenceStore()
            .getBoolean(PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED));

        buttonAllowAlternativeSocks5Port.setSelection(saros
            .getPreferenceStore().getBoolean(
                PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER));

        // upnp gateway information is handled by the UpnpManager !

        updateCompositeStates();
    }

    @Override
    protected void performDefaults() {
        stunIpAddressText.setText(getPreferenceStore().getDefaultString(
            PreferenceConstants.STUN));

        stunPortAddressText.setText(String.valueOf(getPreferenceStore()
            .getDefaultInt(PreferenceConstants.STUN_PORT)));

        localSocks5PortText.setText(String.valueOf(getPreferenceStore()
            .getDefaultInt(PreferenceConstants.FILE_TRANSFER_PORT)));

        localSocks5PortText.setText(String.valueOf(getPreferenceStore().getInt(
            PreferenceConstants.FILE_TRANSFER_PORT)));

        buttonOnlyAllowIBB.setSelection(saros.getPreferenceStore()
            .getDefaultBoolean(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT));

        buttonOnlyAllowMediatedSocks5
            .setSelection(saros.getPreferenceStore().getDefaultBoolean(
                PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED));

        buttonAllowAlternativeSocks5Port.setSelection(saros
            .getPreferenceStore().getDefaultBoolean(
                PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER));

        super.performDefaults();

        updateCompositeStates();
    }

    private void updateCompositeStates() {
        buttonOnlyAllowIBB.setEnabled(false);
        buttonOnlyAllowMediatedSocks5.setEnabled(false);
        localSocks5PortText.setEnabled(false);
        buttonAllowAlternativeSocks5Port.setEnabled(false);

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

    }

    private Group createUpnpGroup(Composite parent) {

        Group group = new Group(parent, SWT.NONE);
        group.setText("UPnP Devices");

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;

        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        gatewayInfo = new Label(group, SWT.CENTER);
        gatewayInfo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
            false));

        upnpDevicesTable = new Table(group, SWT.CHECK | SWT.BORDER
            | SWT.FULL_SELECTION);

        upnpDevicesTable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));

        upnpDevicesTable.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK)
                    handleGatewaySelection(event);
            }
        });

        gatewayInfo.setVisible(false);
        upnpDevicesTable.setVisible(false);

        return group;
    }

    private Group createSocks5OptionsGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);

        group.setText("Connection establishment");

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;

        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        buttonOnlyAllowIBB = new Button(group, SWT.CHECK);
        buttonOnlyAllowIBB
            .setText("Only establish connections over IBB [only very small bandwidth is guaranteed]");
        buttonOnlyAllowIBB
            .setToolTipText("If checked connections are established through XEP-0047: In-Band Bytestreams only");

        buttonOnlyAllowIBB.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateCompositeStates();
            }
        });

        buttonOnlyAllowMediatedSocks5 = new Button(group, SWT.CHECK);
        buttonOnlyAllowMediatedSocks5
            .setText("Only establish connections over an external Socks5 Proxy Server [high bandwidth may be not guaranteed]");
        buttonOnlyAllowMediatedSocks5
            .setToolTipText("If checked connections are established through an external Socks5 Proxy server which is provided by your XMPP Server");

        buttonOnlyAllowMediatedSocks5
            .addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateCompositeStates();
                }
            });

        Composite row = new Composite(group, SWT.NONE);
        row.setLayout(new GridLayout(2, false));
        row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label localSocks5PortLabel = new Label(row, SWT.CENTER);
        localSocks5PortLabel.setText("Direct connection port: ");
        localSocks5PortLabel
            .setToolTipText("The port used for outgoing invitations when a direct connection is possible");

        localSocks5PortText = new Text(row, SWT.SINGLE | SWT.BORDER);
        localSocks5PortText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));

        buttonAllowAlternativeSocks5Port = new Button(group, SWT.CHECK);
        buttonAllowAlternativeSocks5Port
            .setText("Allow binding to next available port");
        buttonAllowAlternativeSocks5Port
            .setToolTipText("If checked the next available port is used\nif the given direct connection port is already bound\nby another application");

        return group;
    }

    private Group createStunServerGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);

        group.setText("STUN Server");

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;

        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label ipAddressLabel = new Label(group, SWT.CENTER);
        ipAddressLabel.setText("Address: ");
        ipAddressLabel
            .setToolTipText("The address of a server e.g stunserver.org or its literal representation e.g 132.177.123.13");

        stunIpAddressText = new Text(group, SWT.SINGLE | SWT.BORDER);
        stunIpAddressText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));
        Label portLabel = new Label(group, SWT.CENTER);
        portLabel.setText("Port: ");
        portLabel
            .setToolTipText("The port of the server (default STUN Port is 3478)");

        stunPortAddressText = new Text(group, SWT.SINGLE | SWT.BORDER);
        stunPortAddressText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
            true, false));

        return group;
    }

    private static final char[] PROGRESS = { '|', '/', '-', '\\' };
    private int progressIndex = 0;

    private void discoverUpnpGateways() {
        if (upnpManager.getGateways() == null) {
            gatewayInfo.setText("discovering UPnP gateway devices");
            gatewayInfo.pack();
            gatewayInfo.setVisible(true);

            Utils.runSafeAsync(null, new Runnable() {

                public void run() {

                    upnpManager.startGatewayDiscovery(false);

                    while (upnpManager.getGateways() == null) {
                        Utils.runSafeSWTAsync(null, new Runnable() {
                            public void run() {
                                if (!gatewayInfo.isDisposed()) {
                                    gatewayInfo
                                        .setText("discovering UPnP gateway devices "
                                            + PROGRESS[progressIndex]);
                                    gatewayInfo.pack();
                                }
                            }
                        });

                        progressIndex++;

                        if (progressIndex == PROGRESS.length)
                            progressIndex = 0;

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }

                    Utils.runSafeSWTAsync(null, new Runnable() {
                        public void run() {
                            List<GatewayDevice> gateways = upnpManager
                                .getGateways();

                            if (gatewayInfo.isDisposed()
                                || upnpDevicesTable.isDisposed())
                                return;

                            if (gateways.isEmpty()) {
                                gatewayInfo
                                    .setText("could not find an UPnP device");
                                gatewayInfo.pack();
                                return;
                            }

                            gatewayInfo.setVisible(false);
                            updateUpnpDevicesTable(upnpManager.getGateways(),
                                upnpManager.getSelectedGateway());
                            upnpDevicesTable.setVisible(true);
                        }
                    });
                }
            });

        } else {
            List<GatewayDevice> gateways = upnpManager.getGateways();

            if (gateways.isEmpty()) {
                gatewayInfo.setText("could not find an UPnP device");
                gatewayInfo.pack();
                gatewayInfo.setVisible(true);
                return;
            }

            updateUpnpDevicesTable(upnpManager.getGateways(),
                upnpManager.getSelectedGateway());
            upnpDevicesTable.setVisible(true);
        }

    }

    private void handleGatewaySelection(Event event) {

        for (TableItem item : upnpDevicesTable.getItems()) {
            if (item.equals(event.item) && !item.equals(lastCheckedItem)) {
                lastCheckedItem = item;
                item.setChecked(true);
            } else if (item.equals(event.item) && item.equals(lastCheckedItem)) {
                item.setChecked(false);
                lastCheckedItem = null;
            } else
                item.setChecked(false);
        }
    }

    private void updateUpnpDevicesTable(List<GatewayDevice> gateways,
        GatewayDevice defaultGateway) {
        upnpDevicesTable.setLinesVisible(true);
        upnpDevicesTable.setHeaderVisible(true);

        while (upnpDevicesTable.getColumnCount() > 0)
            upnpDevicesTable.getColumn(0).dispose();
        upnpDevicesTable.removeAll();

        TableColumn column;
        TableItem item;

        column = new TableColumn(upnpDevicesTable, SWT.NONE);
        column.setText("Allow Port Mapping");

        column = new TableColumn(upnpDevicesTable, SWT.NONE);
        column.setText("Device");

        column = new TableColumn(upnpDevicesTable, SWT.NONE);
        column.setText("Local IP Address");

        column = new TableColumn(upnpDevicesTable, SWT.NONE);
        column.setText("External IP Address");

        column = new TableColumn(upnpDevicesTable, SWT.NONE);
        column.setText("Device IP Address");

        for (GatewayDevice device : gateways) {
            String name;
            String internalIpAddress;
            String externalIpAddress;
            String deviceIpAddress;

            name = device.getFriendlyName();
            internalIpAddress = device.getLocalAddress().getHostAddress();
            deviceIpAddress = device.getDeviceAddress().getHostAddress();

            try {
                if (!device.isConnected())
                    externalIpAddress = "not connected";
                else
                    externalIpAddress = device.getExternalIPAddress();
            } catch (Exception e) {
                externalIpAddress = "n/a";
            }

            item = new TableItem(upnpDevicesTable, SWT.NONE);
            item.setText(1, name);
            item.setText(2, internalIpAddress);
            item.setText(3, externalIpAddress);
            item.setText(4, deviceIpAddress);
            item.setData(device);

            if (device.equals(defaultGateway)) {
                lastCheckedItem = item;
                item.setChecked(true);
            }
        }

        for (int i = 0; i < upnpDevicesTable.getColumnCount(); i++) {
            upnpDevicesTable.getColumn(i).pack();
        }
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
            if (p <= 0 || p >= 65536)
                return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
