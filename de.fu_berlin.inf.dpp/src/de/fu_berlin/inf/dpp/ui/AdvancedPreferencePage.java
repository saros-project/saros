package de.fu_berlin.inf.dpp.ui;

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

        Saros.reinject(this);

        setPreferenceStore(saros.getPreferenceStore());
        setDescription("Advanced settings geared toward developers and power users.");
    }

    private Group ftGroup;
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
        ftGroup.setText("File transfer"); //$NON-NLS-1$

        GridLayout gridLayout = new GridLayout(2, false);
        ftGroup.setLayout(gridLayout);

        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        ftGroup.setLayoutData(gridData);

        ftOverXMPP = new BooleanFieldEditor(
            PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT,
            "Force file transfer over XMPP network (slow, needs reconnect)",
            ftGroup);

        proxyDisabled = new BooleanFieldEditor(
            PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED,
            "Disable local file transfer proxy for direct connections (needs reconnect)",
            ftGroup);

        // note: fix to have two columns for the port field
        composite = new Composite(ftGroup, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(gridData);

        ftPort = new IntegerFieldEditor(PreferenceConstants.FILE_TRANSFER_PORT,
            "File transfer port (needs reconnect):", composite);

        tryNextPorts = new BooleanFieldEditor(
            PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER,
            "Try next ports for file transfer if already bound", ftGroup);

        updateForceFiletranferOverXMPP(getPreferenceStore().getBoolean(
            PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT));

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
        addField(new StringFieldEditor(PreferenceConstants.SKYPE_USERNAME,
            "Skype name:", getFieldEditorParent()));

        createPortFields();

        addField(new BooleanFieldEditor(
            PreferenceConstants.SKIP_SYNC_SELECTABLE,
            "Offer possibility to skip synchronisation in Session Invitation dialog",
            getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.DEBUG,
            "Show Jabber debug window (needs restart).", getFieldEditorParent()));

        addField(new IntegerFieldEditor(
            PreferenceConstants.CHATFILETRANSFER_CHUNKSIZE,
            "Chunk size for chat data transfer", getFieldEditorParent()));

        addField(new StringFieldEditor(PreferenceConstants.STUN,
            "STUN Server (example: stunserver.org, needs restart)",
            getFieldEditorParent()));

        addField(new IntegerFieldEditor(PreferenceConstants.STUN_PORT,
            "STUN server port (needs restart)", getFieldEditorParent()));

        addField(new BooleanFieldEditor(
            PreferenceConstants.AUTO_ACCEPT_INVITATION,
            "Automatically accept incoming invitation (for debugging)",
            getFieldEditorParent()));

        addField(new BooleanFieldEditor(
            PreferenceConstants.AUTO_REUSE_PROJECT,
            "When automatically accepting invitation reuse existing project (for debugging)",
            getFieldEditorParent()));

        addField(new StringFieldEditor(
            PreferenceConstants.AUTO_INVITE,
            "Automatically invite the following comma separated users (for debugging)",
            getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.PING_PONG,
            "Perform Latency Measurement using Ping Pong Activities",
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
}
