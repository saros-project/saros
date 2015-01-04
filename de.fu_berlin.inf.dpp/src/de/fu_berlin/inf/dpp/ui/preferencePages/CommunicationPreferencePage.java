package de.fu_berlin.inf.dpp.ui.preferencePages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

@Component(module = "prefs")
public class CommunicationPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    @Inject
    private IPreferenceStore prefs;

    private StringFieldEditor chatserver;
    private BooleanFieldEditor useCustomChatServer;
    private BooleanFieldEditor useIRCStyleChatLayout;
    private StringFieldEditor skypeName;

    public CommunicationPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        SarosPluginContext.initComponent(this);
        setPreferenceStore(prefs);
        setDescription("Settings for Chat.");
    }

    @Override
    public void init(IWorkbench workbench) {
        // NOP
    }

    @Override
    protected void createFieldEditors() {

        useIRCStyleChatLayout = new BooleanFieldEditor(
            EclipsePreferenceConstants.USE_IRC_STYLE_CHAT_LAYOUT,
            "Use IRC style for chats", getFieldEditorParent());

        chatserver = new StringFieldEditor(
            EclipsePreferenceConstants.CUSTOM_MUC_SERVICE,
            "Custom chatserver: ", getFieldEditorParent());

        useCustomChatServer = new BooleanFieldEditor(
            EclipsePreferenceConstants.FORCE_CUSTOM_MUC_SERVICE,
            "Always use custom chatserver", getFieldEditorParent());

        skypeName = new StringFieldEditor(PreferenceConstants.SKYPE_USERNAME,
            "Skype name:", getFieldEditorParent());

        addField(useIRCStyleChatLayout);
        addField(chatserver);
        addField(useCustomChatServer);
        addField(skypeName);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (prefs
            .getBoolean(EclipsePreferenceConstants.FORCE_CUSTOM_MUC_SERVICE)) {
            useCustomChatServer.setEnabled(!chatserver.getStringValue()
                .isEmpty(), getFieldEditorParent());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getSource() instanceof FieldEditor) {
            FieldEditor field = (FieldEditor) event.getSource();

            if (field.getPreferenceName().equals(
                EclipsePreferenceConstants.CUSTOM_MUC_SERVICE)) {
                String serverName = event.getNewValue().toString();
                useCustomChatServer.setEnabled(!serverName.isEmpty(),
                    getFieldEditorParent());
            }
        }
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
    }
}
