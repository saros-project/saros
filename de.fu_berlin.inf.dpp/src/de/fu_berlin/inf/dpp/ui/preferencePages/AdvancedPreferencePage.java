package de.fu_berlin.inf.dpp.ui.preferencePages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

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

    public AdvancedPreferencePage() {
        super(FieldEditorPreferencePage.GRID);

        SarosPluginContext.initComponent(this);

        setPreferenceStore(saros.getPreferenceStore());
        setDescription("Advanced settings geared toward developers and power users.");
    }

    private Group ftGroup;
    private Group inviteGroup;
    private Composite composite;
    private BooleanFieldEditor ftOverXMPP;
    private BooleanFieldEditor proxyDisabled;
    private IntegerFieldEditor ftPort;
    private BooleanFieldEditor tryNextPorts;

    private void updateFieldEnablement() {
        updateForceFiletranferOverXMPP(ftOverXMPP.getBooleanValue());
    }

    private void updateForceFiletranferOverXMPP(boolean set) {
        proxyDisabled.setEnabled(!set, ftGroup);
        set |= proxyDisabled.getBooleanValue();
        updateFileTransferProxyDisabled(set);
    }

    private void updateFileTransferProxyDisabled(boolean set) {
        ftPort.setEnabled(!set, composite);
        tryNextPorts.setEnabled(!set, ftGroup);
    }

    /**
     * Adds a group with bytestream connection specific options with listeners
     * to enable/disable invalid options
     */
    protected void createPortFields() {

        ftGroup = new Group(getFieldEditorParent(), SWT.NONE);
        ftGroup
            .setText("File transfer (changes require reconnection to the server)"); //$NON-NLS-1$

        inviteGroup = new Group(getFieldEditorParent(), SWT.NONE);
        inviteGroup.setText("Invitation");

        ftGroup.setLayout(new GridLayout(2, false));
        inviteGroup.setLayout(new GridLayout(2, false));

        GridData ftGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        GridData inviteGridData = new GridData(SWT.FILL, SWT.CENTER, true,
            false);
        ftGridData.horizontalSpan = 2;
        inviteGridData.horizontalSpan = 2;
        ftGroup.setLayoutData(ftGridData);
        inviteGroup.setLayoutData(inviteGridData);

        ftOverXMPP = new BooleanFieldEditor(
            PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT,
            "Force file transfer over XMPP network (slow)", ftGroup);

        proxyDisabled = new BooleanFieldEditor(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED,
            "Disable local file transfer proxy for direct connections", ftGroup);

        // note: fix to have two columns for the port field
        composite = new Composite(ftGroup, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        ftGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(ftGridData);

        ftPort = new IntegerFieldEditor(PreferenceConstants.FILE_TRANSFER_PORT,
            "File transfer port:", composite);

        tryNextPorts = new BooleanFieldEditor(
            PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER,
            "Try next ports for file transfer if already bound", ftGroup);

        updateForceFiletranferOverXMPP(getPreferenceStore().getBoolean(
            PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT));
        updateFileTransferProxyDisabled(getPreferenceStore().getBoolean(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED));

        addField(ftOverXMPP);
        addField(proxyDisabled);
        addField(ftPort);
        addField(tryNextPorts);
    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        super.propertyChange(arg0);
        if (arg0.getProperty().equals(FieldEditor.VALUE))
            updateFieldEnablement();
    }

    @Override
    protected void createFieldEditors() {

        createPortFields();

        addField(new BooleanFieldEditor(
            PreferenceConstants.SKIP_SYNC_SELECTABLE,
            "Offer possibility to skip synchronisation in Session Invitation dialog",
            inviteGroup));

        addField(new BooleanFieldEditor(PreferenceConstants.DEBUG,
            "Show XMPP/Jabber debug window (needs restart).",
            getFieldEditorParent()));

        addField(new IntegerFieldEditor(
            PreferenceConstants.CHATFILETRANSFER_CHUNKSIZE,
            "Chunk size for chat data transfer", getFieldEditorParent()));

        addField(new StringFieldEditor(PreferenceConstants.STUN,
            "STUN Server (example: stunserver.org, needs restart)",
            getFieldEditorParent()));

        addField(new IntegerFieldEditor(PreferenceConstants.STUN_PORT,
            "STUN server port (needs restart)", getFieldEditorParent()));

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

        addField(new BooleanFieldEditor(PreferenceConstants.PING_PONG,
            "Perform Latency Measurement using Ping Pong Activities",
            getFieldEditorParent()));

        addField(new BooleanFieldEditor(
            PreferenceConstants.STREAM_PROJECT,
            "Stream invitation (recommended for large projects that experience errors during invitation)",
            inviteGroup));

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage
     */
    public void init(IWorkbench workbench) {
        // No init necessary
    }
}
