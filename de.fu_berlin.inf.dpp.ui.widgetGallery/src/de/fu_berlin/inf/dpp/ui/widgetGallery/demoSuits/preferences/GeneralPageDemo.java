package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.preferences;

import de.fu_berlin.inf.dpp.ui.preferencePages.GeneralPreferencePage;
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.PreferencePageDemo;
import org.eclipse.jface.preference.IPreferencePage;

@Demo
public class GeneralPageDemo extends PreferencePageDemo {

  @Override
  public IPreferencePage getPreferencePage() {
    return new GeneralPreferencePage();
  }
}
