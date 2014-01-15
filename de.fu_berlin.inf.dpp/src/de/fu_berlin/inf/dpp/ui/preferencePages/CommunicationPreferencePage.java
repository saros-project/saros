package de.fu_berlin.inf.dpp.ui.preferencePages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
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

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

//FIXME the layout of this page is completely BROKEN !!!
@Component(module = "prefs")
public class CommunicationPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    @Inject
    private IPreferenceStore prefs;

    private StringFieldEditor chatserver;
    private BooleanFieldEditor useCustomChatServer;
    private StringFieldEditor skypeName;

    private Group chatGroup;
    private Composite chatServerGroup;

    public CommunicationPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        SarosPluginContext.initComponent(this);
        setPreferenceStore(prefs);
        setDescription("Settings for Chat and VoIP Functionality.");
    }

    @Override
    public void init(IWorkbench workbench) {
        // NOP
    }

    @Override
    protected void createFieldEditors() {
        chatGroup = new Group(getFieldEditorParent(), SWT.NONE);

        GridData chatGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        GridData voipGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);

        chatGridData.horizontalSpan = 2;
        voipGridData.horizontalSpan = 2;

        chatGroup.setText("Chat");
        chatGroup.setLayout(new GridLayout(2, false));

        chatGroup.setLayoutData(chatGridData);

        chatServerGroup = new Composite(chatGroup, SWT.NONE);
        chatServerGroup.setLayout(new GridLayout(2, false));
        chatServerGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
            false));

        chatserver = new StringFieldEditor(
            PreferenceConstants.CUSTOM_MUC_SERVICE, "Custom chatserver: ",
            chatServerGroup);

        useCustomChatServer = new BooleanFieldEditor(
            PreferenceConstants.FORCE_CUSTOM_MUC_SERVICE,
            "Always use custom chatserver", chatGroup);

        skypeName = new StringFieldEditor(PreferenceConstants.SKYPE_USERNAME,
            "Skype name:", getFieldEditorParent());

        addField(chatserver);
        addField(useCustomChatServer);
        addField(skypeName);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (prefs.getBoolean(PreferenceConstants.FORCE_CUSTOM_MUC_SERVICE)) {
            useCustomChatServer.setEnabled(!chatserver.getStringValue()
                .isEmpty(), chatGroup);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getSource() instanceof FieldEditor) {
            FieldEditor field = (FieldEditor) event.getSource();

            if (field.getPreferenceName().equals(
                PreferenceConstants.CUSTOM_MUC_SERVICE)) {
                String serverName = event.getNewValue().toString();
                useCustomChatServer
                    .setEnabled(!serverName.isEmpty(), chatGroup);
            }
        }
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
    }
}
