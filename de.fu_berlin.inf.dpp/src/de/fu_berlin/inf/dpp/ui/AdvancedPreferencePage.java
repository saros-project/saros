package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
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

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(PreferenceConstants.SKYPE_USERNAME,
            "Skype name:", getFieldEditorParent()));

        addField(new IntegerFieldEditor(PreferenceConstants.FILE_TRANSFER_PORT,
            "File transfer port (needs reconnect):", getFieldEditorParent()));

        addField(new BooleanFieldEditor(
            PreferenceConstants.SKIP_SYNC_SELECTABLE,
            "Offer possibility to skip synchronisation in Session Invitation dialog",
            getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.DEBUG,
            "Show Jabber debug window (needs restart).", getFieldEditorParent()));

        addField(new BooleanFieldEditor(
            PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT,
            "Avoid direct file transfer connection (needs restart)",
            getFieldEditorParent()));

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
