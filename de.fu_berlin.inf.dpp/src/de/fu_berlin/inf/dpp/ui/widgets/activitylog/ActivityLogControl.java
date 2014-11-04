package de.fu_berlin.inf.dpp.ui.widgets.activitylog;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.awareness.AwarenessUpdateListener;
import de.fu_berlin.inf.dpp.awareness.actions.ActionTypeDataHolder;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionListener;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.NullSarosSessionListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.widgets.chat.ChatRoomsComposite;

/**
 * This composite displays an activity log tab to show action awareness
 * information.
 */
public class ActivityLogControl extends Composite {

    private static final Logger LOG = Logger
        .getLogger(ActivityLogControl.class);

    private final Map<User, Color> colorCache = new HashMap<User, Color>();

    private ISarosSession session;

    @Inject
    private AwarenessInformationCollector collector;

    @Inject
    private ISarosSessionManager sessionManager;

    private final ActivityLogDisplay logDisplay;
    private final SashForm sashForm;

    private final ISarosSessionListener sessionListener = new NullSarosSessionListener() {

        @Override
        public void sessionStarted(final ISarosSession session) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {

                    if (isDisposed())
                        return;

                    if (ActivityLogControl.this.session != null) {
                        ActivityLogControl.this.session
                            .removeListener(projectListener);
                    }

                    session.addListener(projectListener);

                    ActivityLogControl.this.session = session;

                    updateColors();
                }

            });
        }

        @Override
        public void sessionEnding(final ISarosSession session) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {

                    if (isDisposed())
                        return;

                    session.removeListener(projectListener);

                    ActivityLogControl.this.session = null;

                    updateColors();
                }

            });
        }
    };

    private final ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void userJoined(User user) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
                @Override
                public void run() {

                    if (isDisposed())
                        return;

                    updateColors();
                }
            });
        }
    };

    private final DisposeListener disposeListener = new DisposeListener() {
        @Override
        public void widgetDisposed(DisposeEvent e) {
            sessionManager.removeSarosSessionListener(sessionListener);
            sessionManager = null;

            collector.removeAwarenessUpdateListener(awarenessUpdateListener);
            collector = null;

            if (session != null)
                session.removeListener(projectListener);

            session = null;
            clearColorCache();
        }
    };

    /**
     * Creates a new {@link ActivityLogControl} which displays the content of
     * the activity log tab.
     * 
     * @param parent
     *            The parent of this composite
     * @param style
     *            The SWT style code
     */
    public ActivityLogControl(final ChatRoomsComposite chatRooms,
        final Composite parent, final int style,
        final Color displayBackgroundColor) {

        super(parent, style & ~SWT.BORDER);

        SarosPluginContext.initComponent(this);

        final int activityLogDisplayStyle = (style & SWT.BORDER) | SWT.V_SCROLL
            | SWT.H_SCROLL;

        setLayout(new FillLayout());

        sashForm = new SashForm(this, SWT.VERTICAL);

        logDisplay = new ActivityLogDisplay(sashForm, activityLogDisplayStyle,
            displayBackgroundColor);
        logDisplay.setAlwaysShowScrollBars(true);

        addListener(SWT.Activate, new Listener() {
            @Override
            public void handleEvent(Event e) {
                logDisplay.setFocus();
            }
        });

        session = sessionManager.getSarosSession();
        sessionManager.addSarosSessionListener(sessionListener);

        if (session != null)
            session.addListener(projectListener);

        addDisposeListener(disposeListener);

        collector.addAwarenessUpdateListener(awarenessUpdateListener);
    }

    /**
     * Updates the colors of the {@linkplain ActivityLogDisplay log display},
     * purging the color cache first and rebuild it afterwards.
     */
    public void updateColors() {
        checkWidget();

        if (session == null)
            return;

        /*
         * FIXME this can purge colors for a user no longer in the session ! but
         * the data is still displayed in the log display
         */
        clearColorCache();

        for (User user : session.getUsers())
            logDisplay.updateEntityColor(user, getColorForUser(user));
    }

    private final AwarenessUpdateListener awarenessUpdateListener = new AwarenessUpdateListener() {

        @Override
        public void update(final ActionTypeDataHolder data) {

            assert data != null;

            final User user = data.getUser();
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    if (isDisposed())
                        return;

                    Color color = getColorForUser(user);

                    if (color != null)
                        logDisplay.addMessage(user, data, color);

                }
            });
        }
    };

    /**
     * Gets the color for the given user. Updates the color cache if necessary.
     */
    private Color getColorForUser(User user) {
        Color color = colorCache.get(user);

        if (color != null)
            return color;

        color = SarosAnnotation.getUserColor(user);
        colorCache.put(user, color);
        return color;
    }

    /**
     * Clears the color cache and disposes the stored colors.
     */
    private void clearColorCache() {
        for (Color color : colorCache.values())
            color.dispose();

        colorCache.clear();
    }
}