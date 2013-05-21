package de.fu_berlin.inf.dpp.ui.preferencePages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.Messages;

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
        setDescription(Messages.AdvancedPreferencePage_description);
    }

    @Override
    public boolean performOk() {
        return super.performOk();
    }

    @Override
    protected void createFieldEditors() {

        boolean debugMode = false;

        assert (debugMode = true) == true;

        BooleanFieldEditor fieldEditor = new BooleanFieldEditor(
            PreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS,
            Messages.AdvancedPreferencePage_show_contribution_annotations,
            getFieldEditorParent());

        fieldEditor
            .getDescriptionControl(getFieldEditorParent())
            .setToolTipText(
                Messages.AdvancedPreferencePage_show_contribution_annotations_tooltip);

        addField(fieldEditor);

        addField(new BooleanFieldEditor(
            PreferenceConstants.ENABLE_BALLOON_NOTIFICATION,
            Messages.AdvancedPreferencePage_enable_balloon_notifications,
            getFieldEditorParent()));

        if (debugMode) {
            addField(new BooleanFieldEditor(PreferenceConstants.DEBUG,
                Messages.AdvancedPreferencePage_show_xmpp_debug,
                getFieldEditorParent()));

            addField(new BooleanFieldEditor(
                PreferenceConstants.SKIP_SYNC_SELECTABLE,
                Messages.AdvancedPreferencePage_skip_synchronization,
                getFieldEditorParent()));
        }

        addField(new BooleanFieldEditor(
            PreferenceConstants.VIDEOSHARING_ENABLED,
            Messages.AdvancedPreferencePage_enable_videosharing,
            getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.VOIP_ENABLED,
            Messages.AdvancedPreferencePage_enable_voip, getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
        // No init necessary
    }

}
