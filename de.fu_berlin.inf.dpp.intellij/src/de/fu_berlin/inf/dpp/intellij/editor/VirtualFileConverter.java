package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJProjectImplV2;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.NullSessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class to convert a VirtualFile of a shared module to a saros resource.
 */
public class VirtualFileConverter {

    private volatile ISarosSession session;

    /**
     * Registers a <code>ISessionLifecycleListener</code> with the given session
     * manager. This listener is used to obtain the current session object.
     *
     * @param sessionManager the session manager
     */
    public VirtualFileConverter(
        @NotNull
            ISarosSessionManager sessionManager) {

        sessionManager
            .addSessionLifecycleListener(new NullSessionLifecycleListener() {
                @Override
                public void sessionStarted(ISarosSession session) {

                    setSession(session);
                }

                @Override
                public void sessionEnded(ISarosSession session,
                    SessionEndReason reason) {

                    removeSession();
                }
            });
    }

    private void setSession(@NotNull ISarosSession session) {
        this.session = session;
    }

    private void removeSession(){
        this.session = null;
    }

    /**
     * Returns an <code>SPath</code> representing the passed file.
     *
     * @param virtualFile file to get the <code>SPath</code> for
     * @return an <code>SPath</code> representing the passed file or
     * <code>null</code> if the passed file is null or does not exist,
     * there currently is no session, or the file does not belong to a
     * shared module
     */
    @Nullable
    public SPath convertToPath(
        @NotNull
            VirtualFile virtualFile) {

        IResource resource = convertToResource(virtualFile);

        return resource == null ? null : new SPath(resource);
    }

    /**
     * Returns an <code>IResource</code> representing the passed file.
     *
     * @param virtualFile file to get the <code>IResource</code> for
     * @return an <code>IResource</code> representing the passed file or
     * <code>null</code> if the passed file is null or does not exist,
     * there currently is no session, or the file does not belong to a
     * shared module
     */
    @Nullable
    public IResource convertToResource(
        @NotNull
            VirtualFile virtualFile) {

        ISarosSession currentSession = session;

        if (!virtualFile.exists() || currentSession == null) {
            return null;
        }

        IResource resource = null;

        for (IProject project : currentSession.getProjects()) {
            resource = getResource(virtualFile, project);

            if (resource != null) {
                break;
            }
        }

        return resource;
    }

    /**
     * Returns an <code>IResource</code> for the passed VirtualFile and module.
     *
     * @param virtualFile file to get the <code>IResource</code> for
     * @param project     module the file belongs to
     * @return an <code>IResource</code> for the passed file or
     * <code>null</code> it does not belong to the passed module.
     */
    @Nullable
    public IResource getResource(
        @NotNull
            VirtualFile virtualFile,
        @NotNull
            IProject project) {

        IntelliJProjectImplV2 module = (IntelliJProjectImplV2) project
            .getAdapter(IntelliJProjectImplV2.class);

        return module.getResource(virtualFile);
    }
}
