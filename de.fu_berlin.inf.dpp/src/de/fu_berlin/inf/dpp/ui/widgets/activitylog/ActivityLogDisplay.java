package de.fu_berlin.inf.dpp.ui.widgets.activitylog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.fu_berlin.inf.dpp.awareness.actions.ActionTypeDataHolder;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleRoundedComposite;
import de.fu_berlin.inf.dpp.ui.widgets.activitylog.items.ActivityLinePartnerChangeSeparator;
import de.fu_berlin.inf.dpp.ui.widgets.activitylog.items.ActivityLogLine;

/**
 * This composite displays action awareness information about the session
 * participants.
 * */
public class ActivityLogDisplay extends ScrolledComposite {

    private Composite contentComposite;
    private Object lastUser;

    /**
     * Creates an {@link ActivityLogDisplay} to display the activity log in a
     * content with scrollbars.
     * 
     * @param parent
     *            The parent composite of this composite
     * @param style
     *            The SWT style code
     * @param backgroundColor
     *            The background color shown in this composite
     * */
    public ActivityLogDisplay(final Composite parent, final int style,
        Color backgroundColor) {
        super(parent, style);

        contentComposite = new Composite(this, SWT.NONE);
        setContent(contentComposite);
        setExpandHorizontal(true);
        setExpandVertical(true);

        setBackgroundMode(SWT.INHERIT_DEFAULT);
        contentComposite.setBackground(backgroundColor);

        getVerticalBar().setIncrement(50);

        // Focus content composite on activation to enable scrolling.
        addListener(SWT.Activate, new Listener() {
            @Override
            public void handleEvent(Event e) {
                contentComposite.setFocus();
            }
        });

        /*
         * NO LAYOUT needed, because ScrolledComposite sets it's own
         * automatically
         */
        GridLayout gridLayout = new GridLayout(1, false);
        contentComposite.setLayout(gridLayout);

        /*
         * Scroll to bottom if resized
         */
        addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                refresh();
            }
        });
    }

    /**
     * Displays a new line in the activity log containing action awareness
     * information and a separator line if necessary.
     * 
     * @param user
     *            The user who executed the activity
     * @param data
     *            The data holder, offering more information about the executed
     *            activity
     * @param color
     *            The color to be used to mark the user
     */
    public void addMessage(User user, ActionTypeDataHolder data, Color color) {

        // sender's line
        if (lastUser == null || !lastUser.equals(user)) {
            // new / different user
            ActivityLinePartnerChangeSeparator activityOriginatorChangeLine = new ActivityLinePartnerChangeSeparator(
                contentComposite, user.getNickname(), color);
            activityOriginatorChangeLine.setLayoutData(new GridData(SWT.FILL,
                SWT.BEGINNING, true, false));
            activityOriginatorChangeLine.setData(user);
        }

        // activity line
        ActivityLogLine activityLine = new ActivityLogLine(contentComposite,
            data);
        GridData activityLineGridData = new GridData(SWT.FILL, SWT.BEGINNING,
            true, false);
        activityLineGridData.horizontalIndent = SimpleRoundedComposite.MARGIN_WIDTH;
        activityLine.setLayoutData(activityLineGridData);

        refresh();

        lastUser = user;
    }

    /**
     * Computes the ideal render widths of non-{@link ActivityLogLine}s and
     * returns the maximum.
     * 
     * @return the maximum ideal render width of all non-{@link ActivityLogLine}
     *         s
     */
    private int computeMaxNonActivityLineWidth() {
        int maxNonActivityLineWidth = 0;
        for (Control activityItem : contentComposite.getChildren()) {
            if (!(activityItem instanceof ActivityLogLine)) {
                int currentNonActivityLineWidth = activityItem.computeSize(
                    SWT.DEFAULT, SWT.DEFAULT).x;
                maxNonActivityLineWidth = Math.max(currentNonActivityLineWidth,
                    maxNonActivityLineWidth);
            }
        }
        return maxNonActivityLineWidth;
    }

    /**
     * Layouts the contents anew, updates the scrollbar min size and scrolls to
     * the bottom
     */
    public void refresh() {
        /*
         * Layout makes the added controls visible
         */
        contentComposite.layout();

        int verticalBarWidth = (getVerticalBar() != null) ? this
            .getVerticalBar().getSize().x : 0;

        int widthHint = Math.max(computeMaxNonActivityLineWidth()
            + verticalBarWidth, getClientArea().width);

        final Point neededSize = contentComposite.computeSize(widthHint,
            SWT.DEFAULT);

        setMinSize(neededSize);
        setOrigin(0, neededSize.y);
    }

    public void updateEntityColor(final Object entity, final Color color) {
        for (Control control : contentComposite.getChildren()) {
            if (!entity.equals(control.getData())) {
                continue;
            }

            if (control instanceof ActivityLinePartnerChangeSeparator) {
                control.setBackground(color);
            }

        }
    }

    public void updateEntityName(final Object entity, final String name) {
        for (Control control : contentComposite.getChildren()) {
            if (!entity.equals(control.getData()))
                continue;

            if (control instanceof ActivityLinePartnerChangeSeparator) {
                ActivityLinePartnerChangeSeparator separator = (ActivityLinePartnerChangeSeparator) control;
                separator.setUsername(name);
            }
        }
    }
}