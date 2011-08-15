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

    @Override
    public boolean performOk() {
        return super.performOk();
    }

    private Group inviteGroup;

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
    }

    @Override
    protected void createFieldEditors() {

        createInviteFields();

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

        addField(new BooleanFieldEditor(PreferenceConstants.PING_PONG,
            "Perform Latency Measurement using Ping Pong Activities",
            getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.DEBUG,
            "Show XMPP/Jabber debug window (needs restart).",
            getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {
        // No init necessary
    }

}