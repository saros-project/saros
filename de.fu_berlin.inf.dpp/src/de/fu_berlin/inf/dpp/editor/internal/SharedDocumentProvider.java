package de.fu_berlin.inf.dpp.editor.internal;

import org.apache.log4j.Logger;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.NullSessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.User;

/**
 * This Document provider tries tell others that files are not editable if the
 * local user has no write access.
 * <p>
 * As it is up to Eclipse to choose the appropriate document provider for files
 * this provider will likely only works on plain text documents, e.g files with
 * <tt>txt</tt> extension.
 */
@Component(module = "eclipse")
public final class SharedDocumentProvider extends TextFileDocumentProvider {

    private static final Logger LOG = Logger
        .getLogger(SharedDocumentProvider.class);

    private volatile ISarosSession session;

    @Inject
    private ISarosSessionManager sessionManager;

    private boolean hasWriteAccess;

    private final ISessionLifecycleListener sessionLifecycleListener = new NullSessionLifecycleListener() {

        @Override
        public void sessionStarted(final ISarosSession session) {
            hasWriteAccess = session.hasWriteAccess();
            session.addListener(sharedProjectListener);
            SharedDocumentProvider.this.session = session;
        }

        @Override
        public void sessionEnded(final ISarosSession session) {
            assert SharedDocumentProvider.this.session == session;
            session.removeListener(sharedProjectListener);
            SharedDocumentProvider.this.session = null;
        }
    };

    private final ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void permissionChanged(final User user) {

            if (!user.isLocal())
                return;

            final ISarosSession currentSession = session;

            hasWriteAccess = currentSession != null
                && currentSession.hasWriteAccess();
        }
    };

    /**
     * This constructor is necessary when Eclipse creates a
     * SharedDocumentProvider.
     */
    public SharedDocumentProvider() {

        LOG.debug("SharedDocumentProvider created by Eclipse");

        SarosPluginContext.initComponent(this);

        final ISarosSession currentSession = sessionManager.getSarosSession();

        if (currentSession != null)
            sessionLifecycleListener.sessionStarted(currentSession);

        sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    }

    @Override
    public boolean isModifiable(Object element) {
        if (!isInSharedProject(element))
            return super.isModifiable(element);

        return hasWriteAccess && super.isModifiable(element);
    }

    private boolean isInSharedProject(Object element) {

        if (!(element instanceof IFileEditorInput))
            return false;

        final ISarosSession currentSession = session;

        if (currentSession == null)
            return false;

        IFileEditorInput fileEditorInput = (IFileEditorInput) element;

        return currentSession.isShared(ResourceAdapterFactory
            .create(fileEditorInput.getFile().getProject()));
    }
}