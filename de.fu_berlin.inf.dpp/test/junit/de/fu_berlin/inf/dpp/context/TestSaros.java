package de.fu_berlin.inf.dpp.context;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/**
 * Test-Class for using functionality of {@link de.fu_berlin.inf.dpp.Saros}.
 * Overrides the extended constructor. Later this will overrides some
 * eclipse-specific logic and will provide it's own context so it will be
 * possible to start multiple test-instances.
 * 
 * @author cordes
 */
public class TestSaros extends Saros {

    private IPreferenceStore _preferenceStore;

    public TestSaros() {
        // do not call super().
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        if (_preferenceStore == null) {
            _preferenceStore = new PreferenceStore();
        }
        return _preferenceStore;
    }

    @Override
    public String toString() {
        return "testsaros";
    }

    // will be extended
    public IWorkspace getWorspace() {
        return null;
    }

    @Override
    public ISecurePreferences getSecurePrefs() {
        return null;
    }

    public String getUserName() {
        return getPreferenceStore().getString(PreferenceConstants.USERNAME);
    }
}
