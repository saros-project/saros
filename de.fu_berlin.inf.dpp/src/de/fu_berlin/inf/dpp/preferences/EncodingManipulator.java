package de.fu_berlin.inf.dpp.preferences;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.util.BlockingProgressMonitor;
import de.fu_berlin.inf.dpp.util.StateChangeNotifier;

public class EncodingManipulator implements IPreferenceManipulator {

    private static final Logger log = Logger
        .getLogger(EncodingManipulator.class);

    /**
     * Flag telling if the {@link #oldEncoding} field contains a valid value
     * that needs to be restored by the
     * {@link IPreferenceManipulator.IRestorePoint} returned by
     * {@link #change(IProject)}.
     */
    protected boolean gotEncoding;

    /**
     * The old encoding set at project level before {@link #change(IProject)}
     * was executed. May be <code>null</code> if there was no encoding set at
     * project level. This field is only valid if {@link #gotEncoding} contains
     * <code>true</code>!
     */
    protected String oldEncoding;

    public IRestorePoint change(final IProject project) {
        try {
            gotEncoding = false;
            oldEncoding = getEncoding(project);
            String newEncoding = project.getDefaultCharset();
            log.debug("Project encoding: " + newEncoding);
            setEncoding(project, newEncoding);
            gotEncoding = true;
        } catch (CoreException e) {
            log.error("Could not set default encoding.", e);
        }

        return new IRestorePoint() {
            public void restore() {
                if (gotEncoding) {
                    setEncoding(project, oldEncoding);
                }
            }
        };
    }

    /**
     * Gets encoding from project settings. Returns <code>null</code> if there
     * is no encoding set on the project level.
     */
    protected String getEncoding(IProject project) throws CoreException {
        return project.getDefaultCharset(false);
    }

    /**
     * Sets given encoding as default encoding on the project. The encoding may
     * be <code>null</code> to set <em>no</em> encoding on project level.
     */
    protected void setEncoding(IProject project, @Nullable String encoding) {
        if (!project.exists())
            return;
        BlockingProgressMonitor monitor = new BlockingProgressMonitor();
        try {
            project.setDefaultCharset(encoding, monitor);
            monitor.await();
        } catch (CoreException e) {
            log.error("Could not set encoding on project.", e);
        } catch (InterruptedException e) {
            log.error("Code not designed to be interruptible", e);
            Thread.currentThread().interrupt();
        }
    }

    public StateChangeNotifier<IPreferenceManipulator> getPreferenceStateChangeNotifier(
        IProject project) {

        return null;
    }

    /**
     * Returns <code>false</code>.
     * 
     * @see #isDangerousForHost()
     */
    public boolean isDangerousForClient() {

        return false;
    }

    /**
     * Returns <code>true</code>. This manipulator is only interesting for the
     * host, because the project setting is transferred to the client.
     */
    public boolean isDangerousForHost() {

        return true;
    }

    /**
     * Checks if there is a need to set an encoding on the project level.
     */
    public boolean isEnabled(IProject project) {
        try {
            return getEncoding(project) == null;
        } catch (CoreException e) {
            log.error("Could not get project encoding.", e);
            return false;
        }
    }
}
