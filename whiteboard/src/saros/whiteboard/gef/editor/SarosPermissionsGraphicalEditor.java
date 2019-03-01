package saros.whiteboard.gef.editor;

import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.session.ISarosSession;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SarosSessionManager;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.ui.util.SWTUtils;

/**
 * The editor is enabled/disabled respective the Saros user permissions, read-only or write-access.
 *
 * @author jurke
 */
public abstract class SarosPermissionsGraphicalEditor extends BlockableGraphicalEditor {

  protected ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession session) {
          session.addListener(sessionListener);
          setEnabledInSWTThread(session.getLocalUser().hasWriteAccess());
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          session.removeListener(sessionListener);
          setEnabledInSWTThread(true);
        }
      };

  protected ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void permissionChanged(User user) {
          if (user.isLocal()) {
            setEnabledInSWTThread(user.hasWriteAccess());
          }
        }
      };

  private void setEnabledInSWTThread(final boolean enable) {
    SWTUtils.runSafeSWTAsync(
        null,
        new Runnable() {

          @Override
          public void run() {
            SarosPermissionsGraphicalEditor.this.setEnabled(enable);
          }
        });
  }

  @Inject private SarosSessionManager sessionManager;

  public SarosPermissionsGraphicalEditor() {
    SarosPluginContext.initComponent(this);
    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  /*
   * If the view is opened after session start, we have to check the
   * permissions.
   *
   * (non-Javadoc)
   *
   * @see org.eclipse.gef.ui.parts.GraphicalEditor#initializeGraphicalViewer()
   */
  @Override
  protected void initializeGraphicalViewer() {
    ISarosSession session = sessionManager.getSession();
    if (session != null) {
      setEnabled(session.getLocalUser().hasWriteAccess());
    }
  }
}
