package saros.ui.widgetGallery.demoSuits.preferences;

import org.eclipse.jface.preference.IPreferencePage;
import saros.ui.preferencePages.GeneralPreferencePage;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.PreferencePageDemo;

@Demo
public class GeneralPageDemo extends PreferencePageDemo {

  @Override
  public IPreferencePage getPreferencePage() {
    return new GeneralPreferencePage();
  }
}
