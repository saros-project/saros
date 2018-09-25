package de.fu_berlin.inf.dpp.preferences;

/**
 * Eclipse implementation of the {@link Preferences}, that wraps a
 * {@link org.eclipse.jface.preference.IPreferenceStore} in an
 * {@link EclipsePreferenceStoreAdapter}.
 * 
 * Preferences that are custom to Eclipse may be defined here.
 */
public class EclipsePreferences extends Preferences {

    public EclipsePreferences(
        org.eclipse.jface.preference.IPreferenceStore store) {
        super(new EclipsePreferenceStoreAdapter(store));
    }
}