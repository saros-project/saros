package de.fu_berlin.inf.dpp.feedback;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Parent class for FeedbackManager and StatisticManager
 */
public abstract class AbstractFeedbackManager {

    public static final int UNDEFINED = -1;
    public static final int UNKNOWN = 0;
    public static final int ALLOW = 1;
    public static final int FORBID = 2;

    protected Saros saros;

    public AbstractFeedbackManager(Saros saros) {
        this.saros = saros;
        ensureConsistentPreferences();
    }

    /**
     * Ensures that the preferences the specific manager manages are consistent
     * after plugin start, i.e. if they are not existing in the global scope,
     * the value from the workspace (might be the default) is globally set. If
     * there exists a different value in the workspace than in the global scope,
     * then the local value is overwritten. <br>
     * <br>
     * 
     * This must be done for all values kept both globally and per workspace.
     */
    abstract protected void ensureConsistentPreferences();

    /**
     * Sets the global value for the given preference key if it is undefined to
     * the local value from the workspace. If a global value exists and it is
     * different from the local value, then the local value is overwritten.<br>
     * <br>
     * NOTE: Use this method only with a preference key that represents an
     * Integer value.
     * 
     * @param preferenceKey
     */
    protected void makePrefConsistent(String preferenceKey) {
        int globalValue = saros.getConfigPrefs().getInt(preferenceKey,
            UNDEFINED);
        int localValue = saros.getPreferenceStore().getInt(preferenceKey);

        if (globalValue == UNDEFINED) {
            saros.getConfigPrefs().putInt(preferenceKey, localValue);
            saros.saveConfigPrefs();
        } else if (globalValue != localValue) {
            saros.getPreferenceStore().setValue(preferenceKey, globalValue);
        }
    }

    /**
     * Tries to run the given IRunnableWithProgress asynchronously in the active
     * workbench window, i.e. the progress of this operation will be shown in
     * the status bar of the active window. If there is no active workbench
     * window, e.g. during start or stop of the plugin, an IProgressMonitor is
     * used to run the runnable and show its progress.
     * 
     * @nonblocking
     * @cancelable
     */
    protected static void runAsyncInWorkbenchWindow(final Logger log,
        final IRunnableWithProgress runnable) {

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                try {
                    Util.getRunnableContext().run(true, true, runnable);
                } catch (InvocationTargetException e) {
                    log.error("An internal error occurred while running"
                        + " this runnable:", e.getCause());
                } catch (InterruptedException e) {
                    log.warn("Running this runnable was interrupted"
                        + " by the user");
                }
            }
        });
    }
}
