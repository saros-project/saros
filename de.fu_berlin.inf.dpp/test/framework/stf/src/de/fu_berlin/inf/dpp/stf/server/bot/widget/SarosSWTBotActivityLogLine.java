package de.fu_berlin.inf.dpp.stf.server.bot.widget;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.hamcrest.SelfDescribing;

import de.fu_berlin.inf.dpp.ui.widgets.activitylog.items.ActivityLogLine;

/**
 * Represents a {@link ActivityLogLine} widget in the activity log tab.
 */
public class SarosSWTBotActivityLogLine extends AbstractSWTBot<ActivityLogLine> {

    /**
     * Constructs a new instance of this object.
     * 
     * @param w
     *            the widget.
     * @throws WidgetNotFoundException
     *             if the widget is <code>null</code> or widget has been
     *             disposed.
     */
    public SarosSWTBotActivityLogLine(ActivityLogLine w)
        throws WidgetNotFoundException {
        this(w, null);
    }

    /**
     * Constructs a new instance of this object.
     * 
     * @param w
     *            the widget.
     * @param description
     *            the description of the widget, this will be reported by
     *            {@link #toString()}
     * @throws WidgetNotFoundException
     *             if the widget is <code>null</code> or widget has been
     *             disposed.
     */
    public SarosSWTBotActivityLogLine(ActivityLogLine w,
        SelfDescribing description) throws WidgetNotFoundException {
        super(w, description);
    }
}