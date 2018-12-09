package de.fu_berlin.inf.dpp.ui.widgets.viewer.session;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.ViewerComposite;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.picocontainer.annotations.Inject;

/**
 * Base composite to extend for creating composites that will display the current {@link
 * ISarosSession session}. This class offers support to trigger viewer refreshes on session changes
 * and adds logic to react to user interaction. In addition it enhances the visual appearance of the
 * user entries of the {@link Viewer viewer}. This composite does <strong>NOT</strong> handle
 * setting the layout.
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>NONE and those supported by {@link StructuredViewer}
 *   <dt><b>Events:</b>
 *   <dd>(none)
 * </dl>
 *
 * @author bkahlert
 * @author srossbach
 */
public abstract class SessionDisplayComposite extends ViewerComposite<TreeViewer> {

  private static final Logger LOGGER = Logger.getLogger(SessionDisplayComposite.class);

  @Inject protected ISarosSessionManager sessionManager;

  @Inject protected EditorManager editorManager;

  // FIXME the filter must be passed in
  private ViewerFilter filter;

  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void resourcesAdded(IProject project) {
          ViewerUtils.refresh(getViewer(), true);
        }
      };

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        /*
         * do not use sessionStarting as the context may still start and so we
         * get null in session#getComponent call in the SessionContentProvider
         * class !
         */ @Override
        public void sessionStarted(final ISarosSession session) {
          session.addListener(sessionListener);

          SWTUtils.runSafeSWTAsync(
              LOGGER,
              new Runnable() {

                @Override
                public void run() {
                  if (getViewer().getControl().isDisposed()) return;

                  if (filter != null) getViewer().removeFilter(filter);

                  updateViewer();
                  getViewer().expandAll();
                  filter = new HideContactsInSessionFilter(session);
                  getViewer().addFilter(filter);
                }
              });
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          session.removeListener(sessionListener);
          SWTUtils.runSafeSWTAsync(
              LOGGER,
              new Runnable() {

                @Override
                public void run() {
                  if (getViewer().getControl().isDisposed()) return;

                  if (filter != null) getViewer().removeFilter(filter);

                  filter = null;

                  updateViewer();
                  getViewer().expandAll();
                }
              });
        }
      };

  /**
   * Creates an new instance. After creation {@link #updateViewer()} will be called asynchronously.
   *
   * @param parent
   * @param style
   */
  protected SessionDisplayComposite(Composite parent, int style) {
    super(parent, style); // invokes configureViewer

    SarosPluginContext.initComponent(this);

    super.setLayout(LayoutUtils.createGridLayout());

    getViewer().getControl().setLayoutData(LayoutUtils.createFillGridData());

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);

    final ISarosSession session = sessionManager.getSession();

    if (session != null) {
      filter = new HideContactsInSessionFilter(session);
      getViewer().addFilter(filter);
    }

    addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            sessionManager.removeSessionLifecycleListener(sessionLifecycleListener);
            filter = null;
          }
        });

    hookViewer(getViewer());
  }

  @Override
  public final void setLayout(Layout layout) {
    // ignore
  }

  @Override
  protected final TreeViewer createViewer(int style) {
    return new TreeViewer(new Tree(this, style));
  }

  /**
   * Sets the input for the configured viewer.
   *
   * @see #getViewer()
   * @see #configureViewer(TreeViewer)
   */
  protected abstract void updateViewer();

  /**
   * Enables the viewer to react to user events and also changes the visual appearance of the {@link
   * TreeItem}s. In addition this method will trigger a content refresh after the configuration is
   * done.
   */
  private void hookViewer(TreeViewer viewer) {

    viewer.getControl().addMouseListener(new UserInteractionListener(editorManager));

    viewer.getTree().addListener(SWT.PaintItem, new UserElementDecorator());

    // run async because the CTOR init. chain is not finished at this point
    SWTUtils.runSafeSWTAsync(
        LOGGER,
        new Runnable() {

          @Override
          public void run() {

            if (SessionDisplayComposite.this.isDisposed()) return;

            updateViewer();
            getViewer().expandAll();
          }
        });
  }
}
