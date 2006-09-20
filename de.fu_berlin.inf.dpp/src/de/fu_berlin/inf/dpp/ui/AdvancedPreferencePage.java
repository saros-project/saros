package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.fu_berlin.inf.dpp.PreferenceConstants;

/**
 * Contains the advanced preferences - consisting of preferences that are geared
 * towards developers and power users and that are not necessary for normal use.
 * 
 * @author rdjemili
 */
public class AdvancedPreferencePage extends FieldEditorPreferencePage 
    implements IWorkbenchPreferencePage {

    public AdvancedPreferencePage() {
        super(GRID);
    }
    
    @Override
    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(PreferenceConstants.DEBUG,
            "Show Jabber debug window (needs restart).", getFieldEditorParent()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage
     */
    public void init(IWorkbench workbench) {
    }
}
