package saros.ui.preferencePages;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import saros.Saros;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.preferences.EclipsePreferenceConstants;
import saros.preferences.PreferenceConstants;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.Messages;

/**
 * Contains the advanced preferences - consisting of preferences that are geared towards developers
 * and power users and that are not necessary for normal use.
 *
 * @author rdjemili
 * @author jurke
 */
@Component(module = "prefs")
public class AdvancedPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

  @Inject protected Saros saros;

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

    addField(
        new RadioGroupFieldEditor(
            EclipsePreferenceConstants.AUTO_STOP_EMPTY_SESSION,
            "Auto stop empty session",
            3,
            new String[][] {
              {"Always", MessageDialogWithToggle.ALWAYS},
              {"Never", MessageDialogWithToggle.NEVER},
              {"Prompt", MessageDialogWithToggle.PROMPT}
            },
            getFieldEditorParent(),
            true));

    if (debugMode) {
      addField(
          new BooleanFieldEditor(
              PreferenceConstants.SMACK_DEBUG_MODE,
              Messages.AdvancedPreferencePage_show_xmpp_debug,
              getFieldEditorParent()));
    }

    if (Boolean.getBoolean("saros.server.SUPPORTED")) {
      addField(
          new BooleanFieldEditor(
              EclipsePreferenceConstants.SERVER_ACTIVATED,
              Messages.AdvancedPreferencePage_activate_server,
              getFieldEditorParent()));
    }

    addField(
        new BooleanFieldEditor(
            PreferenceConstants.INSTANT_SESSION_START_PREFERRED,
            Messages.AdvancedPreferencePage_instant_session_start_preferred,
            getFieldEditorParent()));
  }

  @Override
  public void init(IWorkbench workbench) {
    // No init necessary
  }
}
