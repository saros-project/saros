package saros.ui.preferencePages;

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
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.preferences.EclipsePreferenceConstants;
import saros.preferences.PreferenceConstants;

@Component(module = "prefs")
public class CommunicationPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

  @Inject private IPreferenceStore prefs;

  private Group chatGroup;
  private StringFieldEditor chatserver;
  private BooleanFieldEditor useCustomChatServer;
  private BooleanFieldEditor useIRCStyleChatLayout;
  private StringFieldEditor skypeName;

  public CommunicationPreferencePage() {
    super(FieldEditorPreferencePage.GRID);
    SarosPluginContext.initComponent(this);
    setPreferenceStore(prefs);
  }

  @Override
  public void init(IWorkbench workbench) {
    // NOP
  }

  @Override
  protected void createFieldEditors() {
    chatGroup = new Group(getFieldEditorParent(), SWT.NONE);
    chatGroup.setText("Settings for Chat");
    chatGroup.setLayout(new GridLayout(2, false));

    GridData chatGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    chatGridData.horizontalSpan = 2;

    chatGroup.setLayoutData(chatGridData);

    useIRCStyleChatLayout =
        new BooleanFieldEditor(
            EclipsePreferenceConstants.USE_IRC_STYLE_CHAT_LAYOUT,
            "Use IRC style for chats",
            chatGroup);

    Composite chatServerGroup = new Composite(chatGroup, SWT.NONE);
    chatServerGroup.setLayout(new GridLayout(2, false));
    chatServerGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    chatserver =
        new StringFieldEditor(
            EclipsePreferenceConstants.CUSTOM_MUC_SERVICE, "Custom chatserver: ", chatServerGroup);

    useCustomChatServer =
        new BooleanFieldEditor(
            EclipsePreferenceConstants.FORCE_CUSTOM_MUC_SERVICE,
            "Always use custom chatserver",
            chatGroup);

    skypeName =
        new StringFieldEditor(
            PreferenceConstants.SKYPE_USERNAME, "Skype name:", getFieldEditorParent());

    addField(useIRCStyleChatLayout);
    addField(chatserver);
    addField(useCustomChatServer);
    addField(skypeName);
  }

  @Override
  public void initialize() {
    super.initialize();
    if (prefs.getBoolean(EclipsePreferenceConstants.FORCE_CUSTOM_MUC_SERVICE)) {
      useCustomChatServer.setEnabled(!chatserver.getStringValue().isEmpty(), chatGroup);
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {

    if (event.getSource() instanceof FieldEditor) {
      FieldEditor field = (FieldEditor) event.getSource();

      if (field.getPreferenceName().equals(EclipsePreferenceConstants.CUSTOM_MUC_SERVICE)) {
        String serverName = event.getNewValue().toString();
        useCustomChatServer.setEnabled(!serverName.isEmpty(), getFieldEditorParent());
      }
    }
  }

  @Override
  protected void performDefaults() {
    super.performDefaults();
  }
}
