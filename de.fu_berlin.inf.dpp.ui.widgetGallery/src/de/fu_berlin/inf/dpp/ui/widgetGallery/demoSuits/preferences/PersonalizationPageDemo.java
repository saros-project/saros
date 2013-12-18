package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.preferences;

import org.eclipse.jface.preference.IPreferencePage;

import de.fu_berlin.inf.dpp.ui.preferencePages.PersonalizationPreferencePage;
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.PreferencePageDemo;

@Demo
public class PersonalizationPageDemo extends PreferencePageDemo {

    @Override
    public IPreferencePage getPreferencePage() {
        return new PersonalizationPreferencePage();
    }

}
