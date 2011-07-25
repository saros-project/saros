package de.fu_berlin.inf.dpp.ui.preferencePages;

import org.bitlet.weupnp.GatewayDevice;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
import de.fu_berlin.inf.dpp.ui.util.UPnPUIUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Contains the advanced preferences - consisting of preferences that are geared
 * towards developers and power users and that are not necessary for normal use.
 * 
 * @author rdjemili
 * @author jurke
 */
@Component(module = "prefs")
public class AdvancedPreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

    @Inject
    protected Saros saros;
    
    @Inject
    protected UPnPManager upnpManager;

    @Inject
    DataTransferManager dataTransferManager;

    public AdvancedPreferencePage() {
        super(FieldEditorPreferencePage.GRID);

        SarosPluginContext.initComponent(this);

        setPreferenceStore(saros.getPreferenceStore());
        setDescription("Advanced settings geared toward developers and power users.");
    }

    @Override
    public boolean performOk() {

        saros.getPreferenceStore().setValue(
            PreferenceConstants.FILE_TRANSFER_PORT,
            Integer.valueOf(ftPort.getText()).intValue());

        saros.getPreferenceStore().setValue(
            PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER,
            tryNextPorts.getSelection());

        int gwSel = gatewaySelector.getSelectionIndex();
        GatewayDevice selGwDevice = null;
        if (allowUPnP.getSelection() && gwSel != -1) {
            selGwDevice = upnpManager.getGateways().get(gwSel);
        }

        if (upnpManager.setSelectedGateway(selGwDevice)) {
            
            if (!dataTransferManager.disconnectInBandBytestreams())
                SarosView
                    .showNotification(
                        "UPnP Activation",
                    "For UPnP to take full effect, please reconnect with your XMPP account.");

        }

        return super.performOk();
    }

    private Group ftGroup;
    private Group inviteGroup;
    private Composite composite;
    private BooleanFieldEditor ftOverXMPP;
    private BooleanFieldEditor proxyDisabled;
    private Text ftPort;
    private Button tryNextPorts;
    private Button allowUPnP;
    private Combo gatewaySelector;
    private Label gatewayInfo;

    private void updateFieldEnablement() {
        updateForceFiletranferOverXMPP(ftOverXMPP.getBooleanValue());
    }

    private void updateForceFiletranferOverXMPP(boolean set) {
        proxyDisabled.setEnabled(!set, ftGroup);
        set |= proxyDisabled.getBooleanValue();
        updateFileTransferProxyDisabled(set);
    }

    private void updateFileTransferProxyDisabled(boolean set) {
        boolean toEnable = !set && upnpManager.getGateways() != null
            && upnpManager.getGateways().isEmpty() == false;

        ftPort.setEnabled(!set);
        tryNextPorts.setEnabled(!set);
        gatewaySelector.setEnabled(toEnable);
        allowUPnP.setEnabled(toEnable);

        // disable portmapping when disabling proxy
        if (set)
            allowUPnP.setSelection(false);
    }

    private void createInviteFields() {
        inviteGroup = new Group(getFieldEditorParent(), SWT.NONE);
        inviteGroup.setText("Invitation");
        inviteGroup.setLayout(new GridLayout(2, false));
        GridData inviteGridData = new GridData(SWT.FILL, SWT.CENTER, true,
            false);
        inviteGridData.horizontalSpan = 2;
        inviteGroup.setLayoutData(inviteGridData);

        addField(new BooleanFieldEditor(
            PreferenceConstants.SKIP_SYNC_SELECTABLE,
            "Offer possibility to skip synchronisation in Session Invitation dialog",
            inviteGroup));

        addField(new BooleanFieldEditor(
            PreferenceConstants.AUTO_ACCEPT_INVITATION,
            "Automatically accept incoming invitation (for debugging)",
            inviteGroup));

        addField(new BooleanFieldEditor(
            PreferenceConstants.AUTO_REUSE_PROJECT,
            "When automatically accepting invitation reuse existing project (for debugging)",
            inviteGroup));

        addField(new StringFieldEditor(
            PreferenceConstants.AUTO_INVITE,
            "Automatically invite the following comma separated buddies (use JabberIDs; for debugging)",
            inviteGroup));

        addField(new BooleanFieldEditor(
            PreferenceConstants.STREAM_PROJECT,
            "Stream invitation (recommended for large projects that experience errors during invitation)",
            inviteGroup));
    }

    /**
     * Adds a group with bytestream connection specific options with listeners
     * to enable/disable invalid options
     */
    protected void createFileTransferFields() {
        ftGroup = new Group(getFieldEditorParent(), SWT.NONE);
        ftGroup
            .setText("File transfer (changes require reconnection to the server)"); //$NON-NLS-1$
        ftGroup.setLayout(new GridLayout(2, false));
        GridData ftGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        ftGridData.horizontalSpan = 2;
        ftGroup.setLayoutData(ftGridData);

        ftOverXMPP = new BooleanFieldEditor(
            PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT,
            "Force file transfer over XMPP network (slow)", ftGroup);
        addField(ftOverXMPP);

        proxyDisabled = new BooleanFieldEditor(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED,
            "Disable local file transfer proxy for direct connections", ftGroup);
        addField(proxyDisabled);

        composite = new Composite(ftGroup, SWT.NONE);
        GridLayout gridlayout = new GridLayout();
        gridlayout.numColumns = 3;
        composite.setLayout(gridlayout);

        new Label(composite, SWT.LEFT)
            .setText("File transfer port (0 for random):");
        ftPort = new Text(composite, SWT.SINGLE | SWT.BORDER);

        Integer ftPortValue = saros.getPreferenceStore().getInt(
            PreferenceConstants.FILE_TRANSFER_PORT);
        ftPort.setText(ftPortValue.toString());

        tryNextPorts = new Button(composite, SWT.CHECK);
        tryNextPorts.setText("try next ports if already bound");
        tryNextPorts.setSelection(saros.getPreferenceStore().getBoolean(
            PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER));

        Composite comp2 = new Composite(ftGroup, SWT.NONE);
        RowLayout rowLayout = new RowLayout();
        rowLayout.wrap = false;
        rowLayout.center = false;
        rowLayout.pack = true;
        rowLayout.type = SWT.HORIZONTAL;
        rowLayout.spacing = 10;
        rowLayout.fill = true;
        comp2.setLayout(rowLayout);

        allowUPnP = new Button(comp2, SWT.CHECK);
        allowUPnP.setText("Allow UPnP port mapping on gateway:");
        allowUPnP
            .setToolTipText("Saros will setup a temporary port forwarding on your gateway to allow Saros to receive incoming connections.");
        allowUPnP
            .setSelection(!saros.getPreferenceStore()
                .getString(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID)
                .isEmpty());

        gatewaySelector = new Combo(comp2, SWT.DROP_DOWN | SWT.READ_ONLY);
        gatewayInfo = new Label(comp2, SWT.BOTTOM);
        gatewayInfo.setEnabled(false);

        updateForceFiletranferOverXMPP(getPreferenceStore().getBoolean(
            PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT));
        updateFileTransferProxyDisabled(getPreferenceStore().getBoolean(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED));

        populateGatewayCombo();

    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        super.propertyChange(arg0);
        if (arg0.getProperty().equals(FieldEditor.VALUE))
            updateFieldEnablement();
    }

    @Override
    protected void createFieldEditors() {

        createFileTransferFields();
        createInviteFields();

        addField(new IntegerFieldEditor(
            PreferenceConstants.CHATFILETRANSFER_CHUNKSIZE,
            "Chunk size for chat data transfer", getFieldEditorParent()));

        IntegerFieldEditor millisUpdateField = new IntegerFieldEditor(
            PreferenceConstants.MILLIS_UPDATE,
            "Interval (in milliseconds) between outgoing updates to peers",
            getFieldEditorParent());
        millisUpdateField.setValidRange(100, 1000);
        millisUpdateField
            .getLabelControl(getFieldEditorParent())
            .setToolTipText(
                "The length of interval between your edits being sent to others in your session."
                    + " If you find the rate of updates in the session is slow"
                    + " you can reduce this number to increase the interval."
                    + " (Requires session restart.)");

        addField(millisUpdateField);

        addField(new StringFieldEditor(PreferenceConstants.STUN,
            "STUN Server (example: stunserver.org)", getFieldEditorParent()));

        addField(new IntegerFieldEditor(PreferenceConstants.STUN_PORT,
            "STUN server port", getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.PING_PONG,
            "Perform Latency Measurement using Ping Pong Activities",
            getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.DEBUG,
            "Show XMPP/Jabber debug window (needs restart).",
            getFieldEditorParent()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage
     */
    public void init(IWorkbench workbench) {
        // No init necessary
    }

    /**
     * Populates the gateway combobox with discovered gateways.
     */
    protected void populateGatewayCombo() {
        if (upnpManager.getGateways() == null) {
            gatewaySelector.setEnabled(false);
            gatewayInfo.setText("Searching for gateways...");
            gatewayInfo.pack();

            Utils.runSafeAsync(null, new Runnable() {

                public void run() {
                    upnpManager.discoverGateways();

                    // GUI work from SWT thread
                    Utils.runSafeSWTAsync(null, new Runnable() {
                        public void run() {

                            UPnPUIUtils.populateGatewaySelectionControls(
                                upnpManager, gatewaySelector, gatewayInfo,
                                allowUPnP);
                            if (proxyDisabled.getBooleanValue()) {
                                gatewaySelector.setEnabled(!proxyDisabled
                                    .getBooleanValue());
                                allowUPnP.setEnabled(!proxyDisabled
                                    .getBooleanValue());
                            }
                        }
                    });
                }
            });

        } else {

            UPnPUIUtils.populateGatewaySelectionControls(upnpManager,
                gatewaySelector, gatewayInfo, allowUPnP);

            if (proxyDisabled.getBooleanValue()) {
                gatewaySelector.setEnabled(!proxyDisabled.getBooleanValue());
                allowUPnP.setEnabled(!proxyDisabled.getBooleanValue());
            }
        }

    }
}
