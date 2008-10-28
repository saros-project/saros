package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;

/**
 * Contains the advanced preferences - consisting of preferences that are geared
 * towards developers and power users and that are not necessary for normal use.
 * 
 * @author rdjemili
 */
public class AdvancedPreferencePage extends FieldEditorPreferencePage implements
	IWorkbenchPreferencePage {

    public AdvancedPreferencePage() {
	super(FieldEditorPreferencePage.GRID);
	setPreferenceStore(Saros.getDefault().getPreferenceStore());
	setDescription("Advanced settings geared toward developers and power users.");
    }

    @Override
    protected void createFieldEditors() {
	addField(new StringFieldEditor(PreferenceConstants.SKYPE_USERNAME,
		"Skype name:", getFieldEditorParent()));

	addField(new IntegerFieldEditor(PreferenceConstants.FILE_TRANSFER_PORT,
		"File transfer port (needs reconnect):", getFieldEditorParent()));

	addField(new BooleanFieldEditor(PreferenceConstants.DEBUG,
		"Show Jabber debug window (needs restart).",
		getFieldEditorParent()));

	addField(new BooleanFieldEditor(
		PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT,
		"Avoid direct file transfer connection", getFieldEditorParent()));

	addField(new IntegerFieldEditor(
		PreferenceConstants.CHATFILETRANSFER_CHUNKSIZE,
		"Chunk size for chat data transfer", getFieldEditorParent()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage
     */
    public void init(IWorkbench workbench) {
    }
}
