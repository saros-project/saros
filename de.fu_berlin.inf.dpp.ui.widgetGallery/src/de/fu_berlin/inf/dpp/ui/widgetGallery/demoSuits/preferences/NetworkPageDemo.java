package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.preferences;

import de.fu_berlin.inf.dpp.ui.preferencePages.NetworkPreferencePage;
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.PreferencePageDemo;
import org.eclipse.jface.preference.IPreferencePage;

@Demo
public class NetworkPageDemo extends PreferencePageDemo {

  @Override
  public IPreferencePage getPreferencePage() {
    return new NetworkPreferencePage();
  }
}
