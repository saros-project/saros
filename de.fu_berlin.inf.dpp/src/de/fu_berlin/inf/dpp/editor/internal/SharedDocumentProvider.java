package de.fu_berlin.inf.dpp.editor.internal;

import org.apache.log4j.Logger;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * This Document provider tries tell others that files are not editable if not a
 * driver.
 */
@Component(module = "util")
public class SharedDocumentProvider extends TextFileDocumentProvider {

    private static final Logger log = Logger
        .getLogger(SharedDocumentProvider.class.getName());

    protected ISharedProject sharedProject;

    @Inject
    protected SessionManager sessionManager;

    protected boolean isDriver;

    protected ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            sharedProject = project;
            isDriver = sharedProject.isDriver();
            sharedProject.addListener(sharedProjectListener);
        }

        @Override
        public void sessionEnded(ISharedProject project) {
            assert sharedProject == project;
            sharedProject.removeListener(sharedProjectListener);
            sharedProject = null;
        }
    };

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void roleChanged(User user) {
            if (sharedProject != null) {
                isDriver = sharedProject.isDriver();
            } else {
                log.warn("Internal error: Shared project null in roleChanged!");
            }
        }
    };

    public SharedDocumentProvider(SessionManager sessionManager) {
        this.sessionManager = sessionManager;

        if (sessionManager.getSharedProject() != null) {
            sessionListener.sessionStarted(sessionManager.getSharedProject());
        }
        sessionManager.addSessionListener(sessionListener);
    }

    /**
     * This constructor is necessary when Eclipse creates a
     * SharedDocumentProvider.
     */
    public SharedDocumentProvider() {

        log.debug("SharedDocumentProvider created by Eclipse");

        Saros.reinject(this);

        if (sessionManager.getSharedProject() != null) {
            sessionListener.sessionStarted(sessionManager.getSharedProject());
        }
        sessionManager.addSessionListener(sessionListener);
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

        return this.isDriver && super.isModifiable(element);
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

        if (sharedProject == null)
            return false;

        IFileEditorInput fileEditorInput = (IFileEditorInput) element;

        return sharedProject.isShared(fileEditorInput.getFile().getProject());
    }
}