package saros.ui.widgetGallery.demoSuits.preferences;

import org.eclipse.jface.preference.IPreferencePage;
import saros.ui.preferencePages.PersonalizationPreferencePage;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.PreferencePageDemo;

@Demo
public class PersonalizationPageDemo extends PreferencePageDemo {

  @Override
  public IPreferencePage getPreferencePage() {
    return new PersonalizationPreferencePage();
  }
}
