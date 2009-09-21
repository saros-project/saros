package de.fu_berlin.inf.dpp.preferences;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.util.StateChangeNotifier;

public class LineDelimiterManipulator implements IPreferenceManipulator {

    private static final Logger log = Logger
        .getLogger(LineDelimiterManipulator.class);

    /**
     * Line delimiter stored in the project specific configuration. May be
     * <code>null</code> if there is no delimiter set in the configuration.
     * 
     * This will be used to restore the setting after the shared session.
     */
    protected String oldLineDelimiter;

    public IRestorePoint change(final IProject project) {

        /* Read the line delimiter from the project specific configuration. */
        oldLineDelimiter = getLineDelimiterPreference(project);
        log.debug("Project line delimiter: "
            + StringEscapeUtils.escapeJava(oldLineDelimiter));
        /*
         * If the host has no project specific line delimiter set, we do this
         * here, so it will be transfered to the clients.
         */
        IScopeContext[] scopeContexts = new IScopeContext[] {
            new ProjectScope(project), new InstanceScope() };
        IPreferencesService preferencesService = Platform
            .getPreferencesService();
        String platformLineDelimiter = System.getProperty("line.separator");
        String lineDelimiter = preferencesService.getString(
            Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR,
            platformLineDelimiter, scopeContexts);
        putLineDelimiterPreference(project, lineDelimiter);

        return new IRestorePoint() {

            public void restore() {
                putLineDelimiterPreference(project, oldLineDelimiter);
            }
        };
    }

    /**
     * Returns the project specific line delimiter preferences for given
     * project, or <code>null</code> if there are no line delimiter settings at
     * all.
     */
    protected static String getLineDelimiterPreference(IProject project) {
        return (new ProjectScope(project)).getNode(Platform.PI_RUNTIME).get(
            Platform.PREF_LINE_SEPARATOR, null);
    }

    /**
     * Puts the given line delimiter into the project specific settings and
     * makes sure they will be saved to the persistent store. If the argument is
     * <code>null</code> the preference will be deleted from the project
     * specific settings.
     */
    protected static void putLineDelimiterPreference(IProject project,
        @Nullable String lineDelimiter) {

        IEclipsePreferences node = (new ProjectScope(project))
            .getNode(Platform.PI_RUNTIME);
        if (lineDelimiter != null) {
            node.put(Platform.PREF_LINE_SEPARATOR, lineDelimiter);
        } else {
            node.remove(Platform.PREF_LINE_SEPARATOR);
        }
        try {
            node.flush();
        } catch (BackingStoreException e) {
            log.error("Cannot flush project preferences", e);
        }
    }

    public StateChangeNotifier<IPreferenceManipulator> getPreferenceStateChangeNotifier(
        IProject project) {
        return null;
    }

    /**
     * Returns <code>false</code> because there doesn't need to be changes on
     * the client side as the client gets this setting with the project from the
     * host.
     */
    public boolean isDangerousForClient() {
        return false;
    }

    /**
     * Returns <code>true</code> as for the host there must be a line delimiter
     * setting in the project specific configuration.
     */
    public boolean isDangerousForHost() {
        return true;
    }

    /**
     * Returns <code>true</code> if there are no project specific line delimiter
     * settings.
     */
    public boolean isEnabled(IProject project) {
        return getLineDelimiterPreference(project) != null;
    }
}
