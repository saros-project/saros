package saros.editor.internal;

import org.apache.log4j.Logger;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.filesystem.ResourceAdapterFactory;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;

/**
 * This Document provider tries tell others that files are not editable if the local user has no
 * write access.
 *
 * <p>As it is up to Eclipse to choose the appropriate document provider for files this provider
 * will likely only works on plain text documents, e.g files with <tt>txt</tt> extension.
 */
@Component(module = "eclipse")
public final class SharedDocumentProvider extends TextFileDocumentProvider {

  private static final Logger LOG = Logger.getLogger(SharedDocumentProvider.class);

  private volatile ISarosSession session;

  @Inject private ISarosSessionManager sessionManager;

  private boolean hasWriteAccess;

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        @Override
        public void sessionStarted(final ISarosSession session) {
          hasWriteAccess = session.hasWriteAccess();
          session.addListener(sessionListener);
          SharedDocumentProvider.this.session = session;
        }

        @Override
        public void sessionEnded(final ISarosSession session, SessionEndReason reason) {
          assert SharedDocumentProvider.this.session == session;
          session.removeListener(sessionListener);
          SharedDocumentProvider.this.session = null;
        }
      };

  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void permissionChanged(final User user) {

          if (!user.isLocal()) return;

          final ISarosSession currentSession = session;

          hasWriteAccess = currentSession != null && currentSession.hasWriteAccess();
        }
      };

  /** This constructor is necessary when Eclipse creates a SharedDocumentProvider. */
  public SharedDocumentProvider() {

    LOG.debug("SharedDocumentProvider created by Eclipse");

    SarosPluginContext.initComponent(this);

    final ISarosSession currentSession = sessionManager.getSession();

    if (currentSession != null) sessionLifecycleListener.sessionStarted(currentSession);

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  @Override
  public boolean isModifiable(Object element) {
    if (!isShared(element)) return super.isModifiable(element);

    return hasWriteAccess && super.isModifiable(element);
  }

  private boolean isShared(Object element) {
    if (!(element instanceof IFileEditorInput)) return false;

    final ISarosSession currentSession = session;

    if (currentSession == null) return false;

    IFileEditorInput fileEditorInput = (IFileEditorInput) element;

    return currentSession.isShared(
        ResourceAdapterFactory.create(fileEditorInput.getFile().getProject()));
  }
}
