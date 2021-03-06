package saros.ui.views;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.communication.InfoManager;
import saros.editor.EditorManager;
import saros.net.xmpp.contact.ContactStatus.Type;
import saros.net.xmpp.contact.IContactsUpdate;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.preferences.EclipsePreferenceConstants;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.ui.BalloonNotification;
import saros.ui.Messages;
import saros.ui.model.roster.RosterEntryElement;
import saros.ui.sounds.SoundPlayer;
import saros.ui.sounds.Sounds;
import saros.ui.util.LayoutUtils;
import saros.ui.util.SWTUtils;
import saros.ui.widgets.ConnectionStateComposite;
import saros.ui.widgets.chat.ChatRoomsComposite;
import saros.ui.widgets.viewer.ViewerComposite;
import saros.ui.widgets.viewer.session.XMPPSessionDisplayComposite;
import saros.util.CoreUtils;

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

/** This view displays the contact list, the Saros Session and Saros Chat. */
@Component(module = "ui")
public class SarosView {

    private static final Logger log = Logger.getLogger(SarosView.class);

    public static final String ID = "saros.ui.views.SarosView";

    private final IContactsUpdate contactsUpdate = new IContactsUpdate() {
        private final Set<XMPPContact> wasAlreadyAvailable = new HashSet<>();

        @Override
        public void update(Optional<XMPPContact> contactOptional,
            UpdateType type) {
            if (type != IContactsUpdate.UpdateType.STATUS
                || !contactOptional.isPresent()) {
                return;
            }

            XMPPContact contact = contactOptional.get();
            Type contactStatus = contact.getStatus().getType();
            boolean wasAvailable = wasAlreadyAvailable.contains(contact);

            if (contactStatus == Type.AVAILABLE && !wasAvailable) {
                wasAlreadyAvailable.add(contact);
                if (playAvailableSound)
                    SoundPlayer.playSound(Sounds.USER_ONLINE);
            } else if (contactStatus == Type.OFFLINE && wasAvailable) {
                wasAlreadyAvailable.remove(contact);
                if (playUnavailableSound)
                    SoundPlayer.playSound(Sounds.USER_OFFLINE);
            }
        }
    };

    private final IPartListener partListener = new IPartListener() {

        @Override
        public void partVisible(MPart part) {
            // TODO Auto-generated method stub

        }

        @Override
        public void partHidden(MPart part) {
            // TODO Auto-generated method stub

        }

        @Override
        public void partDeactivated(MPart part) {
            if (sessionDisplay != null && !sessionDisplay.isDisposed()) {
                sessionDisplay.getViewer().setSelection(new ISelection() {
                    @Override
                    public boolean isEmpty() {
                        return true;
                    }
                });
            }
        }

        @Override
        public void partBroughtToTop(MPart part) {
            // TODO Auto-generated method stub

        }

        @Override
        public void partActivated(MPart part) {
            // TODO Auto-generated method stub

        }
    };

    private final IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            boolean newValue = Boolean
                .parseBoolean(event.getNewValue().toString());
            switch (event.getProperty()) {
            case EclipsePreferenceConstants.ENABLE_BALLOON_NOTIFICATION:
                showBalloonNotifications = newValue;
                break;
            case EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE:
                playAvailableSound = newValue;
                break;
            case EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE:
                playUnavailableSound = newValue;
                break;
            }
        }
    };

    private final ISessionLifecycleListener sessionLifecycleListener = new ISessionLifecycleListener() {
        @Override
        public void sessionEnded(ISarosSession session,
            SessionEndReason reason) {
            showStopNotification(session.getHost(), reason);
        }
    };

    protected Composite leftComposite;

    protected ViewerComposite<?> sessionDisplay;

    static ChatRoomsComposite chatRooms;

    @Inject
    protected IPreferenceStore preferenceStore;

    @Inject
    protected ISarosSessionManager sarosSessionManager;

    @Inject
    protected EditorManager editorManager;

    @Inject
    private XMPPContactsService contactsService;

    @Inject
    private InfoManager infoManager;

    private static EPartService ePartService;

    private static volatile boolean showBalloonNotifications;
    private volatile boolean playAvailableSound;
    private volatile boolean playUnavailableSound;

    private Composite notificationAnchor;

    public SarosView() {
        SarosPluginContext.initComponent(this);
        preferenceStore.addPropertyChangeListener(propertyListener);
        sarosSessionManager
            .addSessionLifecycleListener(sessionLifecycleListener);

        showBalloonNotifications = preferenceStore
            .getBoolean(EclipsePreferenceConstants.ENABLE_BALLOON_NOTIFICATION);
        playAvailableSound = preferenceStore.getBoolean(
            EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE);
        playUnavailableSound = preferenceStore.getBoolean(
            EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE);
    }

    /**
     * @JTourBusStop 2, The Interface Tour:
     *
     *               <p>
     *               The createPartControl method constructs the view's
     *               controls.
     *
     *               <p>
     *               Notice that the SarosView class doesn't contain everything.
     *               Rather it arranges and manages other components which carry
     *               out most of the functionality.
     *
     *               <p>
     *               You should have noticed that the Saros view is divided into
     *               parts, left and right. The left side is a composite of the
     *               session information and the roster. The right side
     *               alternates between an info/chat window.
     */
    @PostConstruct
    public void createPartControl(Composite parent, EPartService partService,
        EMenuService menuService, ESelectionService selectionService) {

        ePartService = partService;

        GridData gridData;

        final GridLayout layout = new GridLayout(1, false);

        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;

        parent.setLayout(layout);

        final SashForm baseSashForm = new SashForm(parent, SWT.SMOOTH);

        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);

        baseSashForm.setLayoutData(gridData);
        /*
         * LEFT COLUMN
         */
        leftComposite = new Composite(baseSashForm, SWT.BORDER);
        leftComposite.setLayout(LayoutUtils.createGridLayout());
        leftComposite.setBackground(
            Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

        /** Sash weight remembering */
        leftComposite.addControlListener(new ControlListener() {
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

        ConnectionStateComposite connectionStateComposite = new ConnectionStateComposite(
            leftComposite, SWT.NONE);
        connectionStateComposite
            .setLayoutData(LayoutUtils.createFillHGrabGridData());

        sessionDisplay = new XMPPSessionDisplayComposite(leftComposite,
            SWT.V_SCROLL);
        sessionDisplay.setLayoutData(LayoutUtils.createFillGridData());

        final Control control = sessionDisplay.getViewer().getControl();

        control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent event) {

                if (!(control instanceof Tree))
                    return;

                TreeItem treeItem = ((Tree) control)
                    .getItem(new Point(event.x, event.y));

                if (treeItem == null)
                    return;

                RosterEntryElement rosterEntryElement = Platform
                    .getAdapterManager()
                    .getAdapter(treeItem.getData(), RosterEntryElement.class);

                if (rosterEntryElement == null)
                    return;

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
        int[] weights = new int[] {
            preferenceStore
                .getInt(EclipsePreferenceConstants.SAROSVIEW_SASH_WEIGHT_LEFT),
            preferenceStore.getInt(
                EclipsePreferenceConstants.SAROSVIEW_SASH_WEIGHT_RIGHT) };
        baseSashForm.setWeights(weights);

        chatRooms = new ChatRoomsComposite(rightComposite, SWT.NONE);

        notificationAnchor = new Composite(parent, SWT.NONE);

        gridData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
        gridData.heightHint = 0;

        notificationAnchor.setLayoutData(gridData);
        notificationAnchor.setVisible(false);

        Viewer sessionViewer = sessionDisplay.getViewer();

        menuService.registerContextMenu(sessionViewer.getControl(),
            "saros.ui.popup.menu");

        sessionViewer
            .addSelectionChangedListener(new ISelectionChangedListener() {

                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    selectionService.setSelection(event.getSelection());
                }
            });

        contactsService.addListener(contactsUpdate);

        partService.addPartListener(partListener);
    }

    @PreDestroy
    public void dispose(EPartService partService) {
        partService.removePartListener(partListener);
        contactsService.removeListener(contactsUpdate);
    }

    /**
     * Display a notification next to the given control.
     *
     * @param title
     * @param text
     * @param control
     */
    public static void showNotification(final String title, final String text,
        final Control control) {
        if (title == null)
            throw new NullPointerException("title is null");

        if (text == null)
            throw new NullPointerException("text is null");

        if (!showBalloonNotifications)
            return;

        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {

                Control attachToControl = control;

                if (attachToControl != null) {
                    BalloonNotification.showNotification(attachToControl,
                        SWT.LEFT | SWT.BOTTOM, title, text);
                    return;
                }

                if (ePartService != null) {
                    MPart sarosView = ePartService
                        .findPart("saros.ui.e4.views.SarosView");
                    if (sarosView != null && sarosView.getObject() != null) {
                        attachToControl = ((SarosView) sarosView
                            .getObject()).notificationAnchor;
                    }
                } else {
                    attachToControl = Display.getCurrent().getFocusControl();
                }
                // IViewPart sarosView = SWTUtils.findView(SarosView.ID);

                /*
                 * If no session view is open then show the balloon notification
                 * in the control which has the keyboard focus
                 */

                /*
                 * if (sarosView != null) { attachToControl = ((SarosView)
                 * sarosView).notificationAnchor; } else { attachToControl =
                 * Display.getCurrent().getFocusControl(); }
                 */

                BalloonNotification.showNotification(attachToControl,
                    SWT.LEFT | SWT.BOTTOM, title, text);
            }
        });
    }

    /**
     * Displays a notification next to the Saros View. If the view cannot be
     * found the notification is displayed next to the element that has the
     * current focus. The visibility time of the notification will vary,
     * depending on how much words the text contains. This method <b>SHOULD
     * NOT</b> be called directly from the business logic.
     *
     * @param title
     *            the title of the notification
     * @param text
     *            the text of the notification
     * @throws NullPointerException
     *             if title or text is <code>null</code>
     */
    public static void showNotification(final String title, final String text) {
        showNotification(title, text, null);
    }

    /**
     * TODO Move to (yet-to-be-created) IDE-independent NotificationHandler
     * class
     */
    public static void showStopNotification(User user,
        SessionEndReason reason) {
        String text = null;
        String title = null;

        switch (reason) {
        case KICKED:
            title = Messages.SessionStop_host_removed_you_title;
            text = CoreUtils
                .format(Messages.SessionStop_host_removed_you_message, user);
            break;

        case HOST_LEFT:
            title = Messages.SessionStop_host_closed_session_title;
            text = CoreUtils
                .format(Messages.SessionStop_host_closed_session_message, user);
            break;
        case CONNECTION_LOST:
            // TODO display the error
            return;
        case LOCAL_USER_LEFT:
            return;
        default:
            log.warn("no UI notification available for stop reason: " + reason);
            return;
        }

        showNotification(title, text);
    }

    /**
     * Remove any balloon notifications that might be left, because they have
     * become obsolete for a reason
     */
    public static void clearNotifications() {
        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                BalloonNotification.removeAllActiveNotifications();
            }
        });
    }
}
