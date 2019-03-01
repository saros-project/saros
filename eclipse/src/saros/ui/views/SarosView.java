package saros.ui.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.editor.EditorManager;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.roster.IRosterListener;
import saros.net.xmpp.roster.RosterTracker;
import saros.preferences.EclipsePreferenceConstants;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.ui.BalloonNotification;
import saros.ui.Messages;
import saros.ui.actions.ChangeColorAction;
import saros.ui.actions.ChangeWriteAccessAction;
import saros.ui.actions.ChangeXMPPAccountAction;
import saros.ui.actions.ConsistencyAction;
import saros.ui.actions.DeleteContactAction;
import saros.ui.actions.Disposable;
import saros.ui.actions.FollowModeAction;
import saros.ui.actions.FollowThisPersonAction;
import saros.ui.actions.JumpToUserWithWriteAccessPositionAction;
import saros.ui.actions.LeaveSessionAction;
import saros.ui.actions.NewContactAction;
import saros.ui.actions.OpenChatAction;
import saros.ui.actions.OpenPreferencesAction;
import saros.ui.actions.RemoveUserAction;
import saros.ui.actions.RenameContactAction;
import saros.ui.actions.SendFileAction;
import saros.ui.actions.SkypeAction;
import saros.ui.model.roster.RosterEntryElement;
import saros.ui.sounds.SoundPlayer;
import saros.ui.sounds.Sounds;
import saros.ui.util.LayoutUtils;
import saros.ui.util.ModelFormatUtils;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;
import saros.ui.widgets.ConnectionStateComposite;
import saros.ui.widgets.chat.ChatRoomsComposite;
import saros.ui.widgets.viewer.ViewerComposite;
import saros.ui.widgets.viewer.session.MDNSSessionDisplayComposite;
import saros.ui.widgets.viewer.session.XMPPSessionDisplayComposite;

/**
 * @JTourBusStop 1, The Interface Tour:
 *
 * <p>This tour shows you a few keys parts of the Saros interface and how it functions. It will give
 * you a glimpse of the main views used by Saros, an idea about how to code the user interface and
 * how it communicates with the underlying business logic.
 *
 * <p>We begin here at the SarosView, the central class that implements the Saros Eclipse view.
 * Notice that each view inherits from the Eclipse ViewPart, which manages most of the view's
 * mechanics, leaving us to fill in the missing parts specific to our view.
 */

/**
 * This view displays the contact list, the Saros Session and Saros Chat.
 *
 * @author patbit
 */
@Component(module = "ui")
public class SarosView extends ViewPart {

  private static final Logger LOG = Logger.getLogger(SarosView.class);

  public static final String ID = "saros.ui.views.SarosView";

  private static final boolean MDNS_MODE = Boolean.getBoolean("saros.net.ENABLE_MDNS");

  private final IRosterListener rosterListener =
      new IRosterListener() {
        /**
         * Stores the most recent presence for each user, so we can keep track of away/available
         * changes which should not update the RosterView.
         */
        private Map<String, Presence> lastPresenceMap = new HashMap<String, Presence>();

        @Override
        public void presenceChanged(Presence presence) {

          final boolean playAvailableSound =
              preferenceStore.getBoolean(
                  EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE);

          final boolean playUnavailableSound =
              preferenceStore.getBoolean(
                  EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE);

          Presence lastPresence = lastPresenceMap.put(presence.getFrom(), presence);

          if ((lastPresence == null || !lastPresence.isAvailable())
              && presence.isAvailable()
              && playAvailableSound) {
            SoundPlayer.playSound(Sounds.USER_ONLINE);
          }

          if ((lastPresence != null)
              && lastPresence.isAvailable()
              && !presence.isAvailable()
              && playUnavailableSound) {
            SoundPlayer.playSound(Sounds.USER_OFFLINE);
          }
        }
      };

  private final IPartListener2 partListener =
      new IPartListener2() {
        @Override
        public void partInputChanged(IWorkbenchPartReference partRef) {
          // do nothing
        }

        @Override
        public void partVisible(IWorkbenchPartReference partRef) {
          // do nothing
        }

        @Override
        public void partHidden(IWorkbenchPartReference partRef) {
          // do nothing
        }

        @Override
        public void partOpened(IWorkbenchPartReference partRef) {
          // do nothing
        }

        @Override
        public void partDeactivated(IWorkbenchPartReference partRef) {
          if (sessionDisplay != null && !sessionDisplay.isDisposed()) {
            sessionDisplay
                .getViewer()
                .setSelection(
                    new ISelection() {
                      @Override
                      public boolean isEmpty() {
                        return true;
                      }
                    });
          }
        }

        @Override
        public void partClosed(IWorkbenchPartReference partRef) {
          getViewSite().getPage().removePartListener(partListener);
        }

        @Override
        public void partBroughtToTop(IWorkbenchPartReference partRef) {
          // do nothing
        }

        @Override
        public void partActivated(IWorkbenchPartReference partRef) {
          // do nothing
        }
      };

  private final IPropertyChangeListener propertyListener =
      new IPropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
          if (event.getProperty().equals(EclipsePreferenceConstants.ENABLE_BALLOON_NOTIFICATION)) {
            showBalloonNotifications = Boolean.valueOf(event.getNewValue().toString());
          }
        }
      };

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          showStopNotification(session.getHost(), reason);
        }
      };

  protected Composite leftComposite;

  protected ViewerComposite<?> sessionDisplay;

  protected ChatRoomsComposite chatRooms;

  @Inject protected IPreferenceStore preferenceStore;

  @Inject protected ISarosSessionManager sarosSessionManager;

  @Inject protected EditorManager editorManager;

  @Inject protected RosterTracker rosterTracker;

  @Inject protected XMPPConnectionService connectionService;

  private static volatile boolean showBalloonNotifications;

  /**
   * Stores actions by their {@link IAction#getId() ID}, so they can (1) be {@linkplain
   * #getAction(String) retrieved} and (2) {@linkplain Disposable#dispose() disposed} when and if
   * necessary.
   */
  /*
   * TODO What about having actions as (disposable and recreatable?)
   * singletons? This map (together with register and get methods) would not
   * be necessary, actions could be added to the menus at will (through a
   * method that would remember only the disposable ones, so they can be
   * disposed when necessary).
   */
  private Map<String, IAction> registeredActions = new HashMap<String, IAction>();

  public SarosView() {
    super();
    SarosPluginContext.initComponent(this);
    preferenceStore.addPropertyChangeListener(propertyListener);
    sarosSessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    showBalloonNotifications =
        preferenceStore.getBoolean(EclipsePreferenceConstants.ENABLE_BALLOON_NOTIFICATION);
  }

  /**
   * @JTourBusStop 2, The Interface Tour:
   *
   * <p>The createPartControl method constructs the view's controls.
   *
   * <p>Notice that the SarosView class doesn't contain everything. Rather it arranges and manages
   * other components which carry out most of the functionality.
   *
   * <p>You should have noticed that the Saros view is divided into parts, left and right. The left
   * side is a composite of the session information and the roster. The right side alternates
   * between an info/chat window.
   */
  @Override
  public void createPartControl(Composite parent) {

    parent.setLayout(new FillLayout());

    final SashForm baseSashForm = new SashForm(parent, SWT.SMOOTH);

    /*
     * LEFT COLUMN
     */
    leftComposite = new Composite(baseSashForm, SWT.BORDER);
    leftComposite.setLayout(LayoutUtils.createGridLayout());
    leftComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

    /** Sash weight remembering */
    leftComposite.addControlListener(
        new ControlListener() {
          @Override
          public void controlResized(ControlEvent e) {
            preferenceStore.setValue(
                EclipsePreferenceConstants.SAROSVIEW_SASH_WEIGHT_LEFT,
                baseSashForm.getWeights()[0]);
            preferenceStore.setValue(
                EclipsePreferenceConstants.SAROSVIEW_SASH_WEIGHT_RIGHT,
                baseSashForm.getWeights()[1]);
          }

          @Override
          public void controlMoved(ControlEvent e) {
            // NOP
          }
        });

    ConnectionStateComposite connectionStateComposite =
        new ConnectionStateComposite(leftComposite, SWT.NONE);
    connectionStateComposite.setLayoutData(LayoutUtils.createFillHGrabGridData());

    if (MDNS_MODE) {
      sessionDisplay = new MDNSSessionDisplayComposite(leftComposite, SWT.V_SCROLL);
    } else {
      sessionDisplay = new XMPPSessionDisplayComposite(leftComposite, SWT.V_SCROLL);
    }

    sessionDisplay.setLayoutData(LayoutUtils.createFillGridData());

    final Control control = sessionDisplay.getViewer().getControl();

    control.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseDoubleClick(MouseEvent event) {

            if (!(control instanceof Tree)) return;

            TreeItem treeItem = ((Tree) control).getItem(new Point(event.x, event.y));

            if (treeItem == null) return;

            RosterEntryElement rosterEntryElement =
                Platform.getAdapterManager()
                    .getAdapter(treeItem.getData(), RosterEntryElement.class);

            if (rosterEntryElement == null) return;

            chatRooms.openChat(rosterEntryElement.getJID(), true);
          }
        });

    /*
     * RIGHT COLUMN
     */
    Composite rightComposite = new Composite(baseSashForm, SWT.NONE);
    rightComposite.setLayout(new FillLayout());

    /*
     * Initialize sash form weights from preferences (remembering the layout
     * of the saros view), if no prefs exist (first start) use a 50/50 space
     * distribution.
     *
     * Can only set the sash weights after adding all direct child elements
     * of the baseSashForm.
     */
    int[] weights =
        new int[] {
          preferenceStore.getInt(EclipsePreferenceConstants.SAROSVIEW_SASH_WEIGHT_LEFT),
          preferenceStore.getInt(EclipsePreferenceConstants.SAROSVIEW_SASH_WEIGHT_RIGHT)
        };
    baseSashForm.setWeights(weights);

    chatRooms = new ChatRoomsComposite(rightComposite, SWT.NONE, rosterTracker);

    /**
     * @JTourBusStop 3, The Interface Tour:
     *
     * <p>There are a few additional things in the Saros view.
     *
     * <p>There is tool bar that holds the icons along the top (also see addToolbarItems() below).
     *
     * <p>Also, there are context menus which appear when you: - right-click on a person in your
     * current session - right-click on a contact in the contact list.
     */
    createActions();

    /*
     * Toolbar
     */
    IActionBars bars = getViewSite().getActionBars();
    IToolBarManager toolBar = bars.getToolBarManager();
    addToolBarItems(toolBar);

    /*
     * Context Menu
     */
    MenuManager menuManager = new MenuManager();
    menuManager.setRemoveAllWhenShown(true);
    addMenuStartSeparator(menuManager);
    addRosterMenuItems(menuManager);
    addSessionMenuItems(menuManager);
    addAdditionsSeparator(menuManager);

    Viewer sessionViewer = sessionDisplay.getViewer();
    Menu menu = menuManager.createContextMenu(sessionViewer.getControl());
    sessionViewer.getControl().setMenu(menu);
    getSite().registerContextMenu(menuManager, sessionViewer);
    getSite().setSelectionProvider(sessionViewer);

    rosterTracker.addRosterListener(rosterListener);
    rosterListener.rosterChanged(connectionService.getRoster());

    getViewSite().getPage().addPartListener(partListener);
  }

  protected void addToolBarItems(IToolBarManager toolBar) {
    toolBar.add(getAction(ChangeXMPPAccountAction.ACTION_ID));
    toolBar.add(getAction(NewContactAction.ACTION_ID));
    toolBar.add(getAction(OpenPreferencesAction.ACTION_ID));
    toolBar.add(new Separator());
    toolBar.add(getAction(FollowModeAction.ACTION_ID));
    toolBar.add(getAction(ConsistencyAction.ACTION_ID));
    toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    toolBar.add(getAction(LeaveSessionAction.ACTION_ID));
  }

  /** @param menuManager */
  protected void addRosterMenuItems(MenuManager menuManager) {

    menuManager.addMenuListener(
        new IMenuListener() {
          @Override
          public void menuAboutToShow(final IMenuManager manager) {
            /*
             * Do not display the following actions if participants are
             * selected.
             */
            List<User> participants =
                SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();
            if (participants.size() > 0) return;

            /*
             * Do not display the following actions if no contacts are
             * selected.
             */
            List<JID> contacts =
                SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();
            if (contacts.size() == 0) return;

            /*
             * disabled because feature does not work properly(current
             * version 14.1.31)
             */
            // manager.add(getAction(SkypeAction.class));
            manager.add(new Separator());
            manager.add(getAction(OpenChatAction.ACTION_ID));
            manager.add(getAction(SendFileAction.ACTION_ID));
            manager.add(getAction(RenameContactAction.ACTION_ID));
            manager.add(getAction(DeleteContactAction.ACTION_ID));
          }
        });
  }

  /** @param menuManager */
  protected void addSessionMenuItems(MenuManager menuManager) {

    /*
     * TODO The decision whether to show an entry at all is made here,
     * whereas the decision whether to enable an entry is encapsulated in
     * each action. That does not feel right.
     */
    menuManager.addMenuListener(
        new IMenuListener() {

          @Override
          public void menuAboutToShow(IMenuManager manager) {
            /*
             * Do not display the following actions if no participants are
             * selected.
             */
            List<User> participants =
                SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();
            if (participants.size() == 0) return;

            /*
             * Do not display the following actions if non-participants are
             * selected.
             */
            List<JID> contacts =
                SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

            if (contacts.size() > 0) return;

            boolean isHost = false;

            ISarosSession session = sarosSessionManager.getSession();

            if (session != null) isHost = session.isHost();

            if (participants.size() != 1) return;

            if (participants.get(0).isLocal()) {
              manager.add(getAction(ChangeColorAction.ACTION_ID));

              if (isHost) {
                manager.add(getAction(ChangeWriteAccessAction.WriteAccess.ACTION_ID));

                manager.add(getAction(ChangeWriteAccessAction.ReadOnly.ACTION_ID));
              }
            } else {
              if (isHost) {
                manager.add(getAction(ChangeWriteAccessAction.WriteAccess.ACTION_ID));

                manager.add(getAction(ChangeWriteAccessAction.ReadOnly.ACTION_ID));

                manager.add(getAction(RemoveUserAction.ACTION_ID));
                manager.add(new Separator());
              }
              manager.add(getAction(FollowThisPersonAction.ACTION_ID));
              manager.add(getAction(JumpToUserWithWriteAccessPositionAction.ACTION_ID));
              manager.add(new Separator());
              manager.add(getAction(OpenChatAction.ACTION_ID));
              manager.add(getAction(SendFileAction.ACTION_ID));
            }
          }
        });
  }

  /**
   * Adds the {@link IWorkbenchActionConstants#MB_ADDITIONS additions} {@link Separator} to the
   * {@link MenuManager} in order to let others extend the menu.
   *
   * @param menuManager
   */
  protected void addAdditionsSeparator(MenuManager menuManager) {
    menuManager.addMenuListener(
        new IMenuListener() {
          @Override
          public void menuAboutToShow(IMenuManager manager) {
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
          }
        });
  }

  protected void addMenuStartSeparator(MenuManager menuManager) {
    menuManager.addMenuListener(
        new IMenuListener() {
          @Override
          public void menuAboutToShow(IMenuManager manager) {
            Separator menuStart = new Separator("menustart");
            menuStart.setVisible(false);
            manager.add(menuStart);
          }
        });
  }

  @Override
  public void dispose() {
    super.dispose();

    rosterTracker.removeRosterListener(rosterListener);

    for (IAction action : registeredActions.values())
      if (action instanceof Disposable) ((Disposable) action).dispose();
  }

  /**
   * Display a notification next to the given control.
   *
   * @param title
   * @param text
   * @param control
   */
  public static void showNotification(
      final String title, final String text, final Control control) {
    if (title == null) throw new NullPointerException("title is null");

    if (text == null) throw new NullPointerException("text is null");

    if (!showBalloonNotifications) return;

    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {

            if (control != null) {
              BalloonNotification.showNotification(control, title, text);
              return;
            }

            IViewPart sarosView = SWTUtils.findView(SarosView.ID);
            /*
             * If no session view is open then show the balloon notification
             * in the control which has the keyboard focus
             */

            Control sarosViewControl;

            if (sarosView != null) {
              sarosViewControl = ((SarosView) sarosView).leftComposite;
            } else {
              sarosViewControl = Display.getDefault().getFocusControl();
            }

            BalloonNotification.showNotification(sarosViewControl, title, text);
          }
        });
  }

  /**
   * Displays a notification next to the Saros View. If the view cannot be found the notification is
   * displayed next to the element that has the current focus. The visibility time of the
   * notification will vary, depending on how much words the text contains. This method <b>SHOULD
   * NOT</b> be called directly from the business logic.
   *
   * @param title the title of the notification
   * @param text the text of the notification
   * @throws NullPointerException if title or text is <code>null</code>
   */
  public static void showNotification(final String title, final String text) {
    showNotification(title, text, null);
  }

  /** TODO Move to (yet-to-be-created) IDE-independent NotificationHandler class */
  public static void showStopNotification(User user, SessionEndReason reason) {
    String text = null;
    String title = null;

    switch (reason) {
      case KICKED:
        title = Messages.SessionStop_host_removed_you_title;
        text = ModelFormatUtils.format(Messages.SessionStop_host_removed_you_message, user);
        break;

      case HOST_LEFT:
        title = Messages.SessionStop_host_closed_session_title;
        text = ModelFormatUtils.format(Messages.SessionStop_host_closed_session_message, user);
        break;
      case CONNECTION_LOST:
        // TODO display the error
        return;
      case LOCAL_USER_LEFT:
        return;
      default:
        LOG.warn("no UI notification available for stop reason: " + reason);
        return;
    }

    showNotification(title, text);
  }

  /**
   * Remove any balloon notifications that might be left, because they have become obsolete for a
   * reason
   */
  public static void clearNotifications() {
    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            BalloonNotification.removeAllActiveNotifications();
          }
        });
  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub
  }

  private void createActions() {

    // ContextMenus Session
    registerAction(ChangeWriteAccessAction.WriteAccess.newInstance());
    registerAction(ChangeWriteAccessAction.ReadOnly.newInstance());
    registerAction(new FollowThisPersonAction());
    registerAction(new JumpToUserWithWriteAccessPositionAction());
    registerAction(new SendFileAction());
    registerAction(new ChangeColorAction());
    registerAction(new RemoveUserAction());

    // ContextMenus Roster/Contact list
    registerAction(new SkypeAction());
    registerAction(new RenameContactAction());
    registerAction(new DeleteContactAction());

    // ContextMenus Both
    registerAction(new OpenChatAction(chatRooms));

    // Toolbar
    registerAction(new ChangeXMPPAccountAction());
    registerAction(new NewContactAction());
    registerAction(new OpenPreferencesAction());
    registerAction(new FollowModeAction());
    registerAction(new ConsistencyAction());
    registerAction(new LeaveSessionAction());
  }

  private IAction getAction(String id) {
    IAction action = registeredActions.get(id);

    if (action == null)
      throw new IllegalArgumentException("an action for id " + id + " is not registered");

    return action;
  }

  private IAction registerAction(IAction action) {
    IAction oldAction = registeredActions.put(action.getId(), action);

    if (oldAction != null)
      throw new IllegalArgumentException(
          "tried to register action with id " + action.getId() + " more than once");

    return action;
  }
}
