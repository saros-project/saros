package de.fu_berlin.inf.dpp.ui.widgets.activitylog.items;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgets.SimpleRoundedComposite;

/**
 * This composite is used to display a separator between activity log lines of
 * different session participants.
 */
public class ActivityLinePartnerChangeSeparator extends SimpleRoundedComposite {

    protected String username;

    /**
     * Creates a separator between two different session participants in the
     * activity log tab.
     * 
     * @param parent
     *            The parent composite of this composite
     * @param username
     *            The name of the user who will be displayed in this separator
     * @param color
     *            The color with which this separator will be filled
     * */
    public ActivityLinePartnerChangeSeparator(Composite parent,
        String username, Color color) {
        super(parent, SWT.BORDER);
        this.setBackground(color);
        this.username = username;

        /*
         * SimpleRoundedComposite layouts automatically itself. Thus, putting a
         * second empty string causes the username to be aligned left
         */
        setTexts(new String[] { username, "" });
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        setTexts(new String[] { username, "" });
    }
}