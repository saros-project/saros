package de.fu_berlin.inf.dpp.ui.preferencePages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
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

    private Group inviteGroup;

    private void createInviteFields() {
        inviteGroup = new Group(getFieldEditorParent(), SWT.NONE);
        inviteGroup.setText(Messages.AdvancedPreferencePage_invitation);
        inviteGroup.setLayout(new GridLayout(2, false));
        GridData inviteGridData = new GridData(SWT.FILL, SWT.CENTER, true,
            false);
        inviteGridData.horizontalSpan = 2;
        inviteGroup.setLayoutData(inviteGridData);

        addField(new BooleanFieldEditor(
            PreferenceConstants.SKIP_SYNC_SELECTABLE,
            Messages.AdvancedPreferencePage_skip_synchronization, inviteGroup));
    }

    @Override
    protected void createFieldEditors() {

        createInviteFields();

        IntegerFieldEditor millisUpdateField = new IntegerFieldEditor(
            PreferenceConstants.MILLIS_UPDATE,
            Messages.AdvancedPreferencePage_peer_update, getFieldEditorParent());
        millisUpdateField.setValidRange(100, 1000);
        millisUpdateField.getLabelControl(getFieldEditorParent())
            .setToolTipText(
                Messages.AdvancedPreferencePage_interval_between_editings);

        addField(millisUpdateField);

        addField(new BooleanFieldEditor(PreferenceConstants.PING_PONG,
            Messages.AdvancedPreferencePage_ping_pong, getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.DEBUG,
            Messages.AdvancedPreferencePage_show_xmpp_debug,
            getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {
        // No init necessary
    }

}