package de.fu_berlin.inf.dpp.ui.widgets.chat;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.IDEInteractionActivity.Element;
import de.fu_berlin.inf.dpp.activities.TestRunActivity.State;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.awareness.AwarenessUpdateListener;
import de.fu_berlin.inf.dpp.awareness.actions.ActionType;
import de.fu_berlin.inf.dpp.awareness.actions.ActionTypeDataHolder;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

/**
 * This composite displays an activity log tab to show action awareness
 * information.
 */
public class ActivityLogControl extends Composite {

    private static final Logger LOG = Logger
        .getLogger(ActivityLogControl.class);

    private final StyledText display;

    @Inject
    private AwarenessInformationCollector collector;

    @Inject
    private ISarosSessionManager sessionManager;

    /**
     * Creates a new {@link ActivityLogControl} which displays the content of
     * the activity log tab.
     * 
     * @param parent
     *            The parent of this composite
     * @param style
     *            The style of this composite
     */
    public ActivityLogControl(final Composite parent, int style) {
        super(parent, SWT.NONE);

        setLayout(new FillLayout());
        display = new StyledText(this, SWT.FULL_SELECTION | SWT.MULTI
            | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | (style & SWT.BORDER));

        addListener(SWT.Activate, new Listener() {
            @Override
            public void handleEvent(Event e) {
                display.setFocus();
            }
        });
        SarosPluginContext.initComponent(this);
        addDisposeListener(disposeListener);

        /*
         * listens to the collector to get updates about new awareness
         * information
         */
        collector.addAwarenessUpdateListener(awarenessUpdateListener);
    }

    private void addMessage(final String message) {
        display.append("\n");
        display.append(message);
        display.setTopIndex(display.getLineCount() - 1);
    }

    private final AwarenessUpdateListener awarenessUpdateListener = new AwarenessUpdateListener() {

        @Override
        public void update(final ActionTypeDataHolder data) {
            if (data == null) {
                return;
            }

            final ActionType type = data.getType();
            final User user = data.getUser();

            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    if (isDisposed()) {
                        return;
                    }
                    if (type == ActionType.ADD_TESTRUN) {

                        final String name = data.getTestRunName();
                        final State state = data.getTestRunState();
                        addMessage(user + " : " + name + " : " + state);

                    } else if (type == ActionType.ADD_REFACTORING) {

                        final String description = data
                            .getRefactoringDescription();
                        addMessage(user + " : " + description);

                    } else if (type == ActionType.ADD_IDEELEMENT) {

                        final String title = data.getIdeTitle();
                        final Element element = data.getIdeElementType();
                        addMessage(user + " : " + title + " (" + element + ")");

                    } else if (type == ActionType.ADD_CREATEDFILE) {

                        final String filename = data.getCreatedFileName();
                        addMessage(user + " : " + filename);
                    } else {
                        return;
                    }
                }
            });
        }
    };

    private final DisposeListener disposeListener = new DisposeListener() {
        @Override
        public void widgetDisposed(DisposeEvent e) {
            collector.removeAwarenessUpdateListener(awarenessUpdateListener);
            collector = null;
        }
    };
}