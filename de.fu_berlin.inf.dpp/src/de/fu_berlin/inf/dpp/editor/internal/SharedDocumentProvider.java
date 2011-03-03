package de.fu_berlin.inf.dpp.editor.internal;

import org.apache.log4j.Logger;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * This Document provider tries tell others that files are not editable if
 * {@link User.Permission#READONLY_ACCESS}.
 */
@Component(module = "util")
public class SharedDocumentProvider extends TextFileDocumentProvider {

    private static final Logger log = Logger
        .getLogger(SharedDocumentProvider.class.getName());

    protected ISarosSession sarosSession;

    @Inject
    protected SarosSessionManager sessionManager;

    protected boolean hasWriteAccess;

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            sarosSession = newSarosSession;
            hasWriteAccess = sarosSession.hasWriteAccess();
            sarosSession.addListener(sharedProjectListener);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            assert sarosSession == oldSarosSession;
            sarosSession.removeListener(sharedProjectListener);
            sarosSession = null;
        }
    };

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void permissionChanged(User user) {
            if (sarosSession != null) {
                hasWriteAccess = sarosSession.hasWriteAccess();
            } else {
                log
                    .warn("Internal error: Shared project null in permissionChanged!");
            }
        }
    };

    public SharedDocumentProvider(SarosSessionManager sessionManager) {
        this.sessionManager = sessionManager;

        if (sessionManager.getSarosSession() != null) {
            sessionListener.sessionStarted(sessionManager.getSarosSession());
        }
        sessionManager.addSarosSessionListener(sessionListener);
    }

    /**
     * This constructor is necessary when Eclipse creates a
     * SharedDocumentProvider.
     */
    public SharedDocumentProvider() {

        log.debug("SharedDocumentProvider created by Eclipse");

        SarosPluginContext.reinject(this);

        if (sessionManager.getSarosSession() != null) {
            sessionListener.sessionStarted(sessionManager.getSarosSession());
        }
        sessionManager.addSarosSessionListener(sessionListener);
    }

    @Override
    public boolean isReadOnly(Object element) {
        return super.isReadOnly(element);
    }

    @Override
    public boolean isModifiable(Object element) {
        if (!isInSharedProject(element)) {
            return super.isModifiable(element);
        }

        return this.hasWriteAccess && super.isModifiable(element);
    }

    @Override
    public boolean canSaveDocument(Object element) {
        return super.canSaveDocument(element);
    }

    @Override
    public boolean mustSaveDocument(Object element) {
        return super.mustSaveDocument(element);
    }

    private boolean isInSharedProject(Object element) {

        if (sarosSession == null)
            return false;

        IFileEditorInput fileEditorInput = (IFileEditorInput) element;

        return sarosSession.isShared(fileEditorInput.getFile().getProject());
    }
}