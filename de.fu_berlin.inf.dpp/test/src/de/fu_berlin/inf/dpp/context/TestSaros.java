package de.fu_berlin.inf.dpp.context;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

import de.fu_berlin.inf.dpp.Saros;

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
}
