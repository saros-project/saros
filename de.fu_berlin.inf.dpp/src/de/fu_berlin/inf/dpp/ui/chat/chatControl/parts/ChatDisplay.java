package de.fu_berlin.inf.dpp.ui.chat.chatControl.parts;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.items.ChatLine;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.items.ChatLinePartnerChangeSeparator;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.items.ChatLineSeparator;

/**
 * This control displays a chat conversation between n users
 * 
 * @author bkahlert
 */
public class ChatDisplay extends ScrolledComposite {
    protected Composite contentComposite;
    protected Composite optionsComposite;
    protected Object lastUser;

    public ChatDisplay(Composite parent, int style, Color backgroundColor) {
        super(parent, style);

        this.contentComposite = new Composite(this, SWT.NONE);
        this.setContent(contentComposite);
        this.setExpandHorizontal(true);
        this.setExpandVertical(true);

        this.setBackgroundMode(SWT.INHERIT_DEFAULT);
        contentComposite.setBackground(backgroundColor);

        /*
         * NO LAYOUT needed, because ScrolledComposite sets it's own
         * automatically
         */
        GridLayout gridLayout = new GridLayout(1, false);
        contentComposite.setLayout(gridLayout);

        /*
         * Scroll to bottom if resized
         */
        this.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                ChatDisplay.this.refresh();
            }
        });
    }

    /**
     * Adds a line of options to modify the chat to the end of the
     * {@link ChatDisplay} and removes an eventually existing option bar.
     */
    protected void createOptionComposite() {
        if (this.optionsComposite != null)
            this.optionsComposite.dispose();

        this.optionsComposite = new Composite(contentComposite, SWT.NONE);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.optionsComposite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        this.optionsComposite.setLayout(gridLayout);

        final Button clearButton = new Button(optionsComposite, SWT.PUSH);
        clearButton.setText("Clear");
        clearButton.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                ChatDisplay.this.clear();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // irrelevant
            }
        });
        clearButton.setLayoutData(new GridData(SWT.END, SWT.END, true, true));

    }

    /**
     * Displays a new line containing the supplied message and a separator line
     * if necessary.
     * 
     * @param user
     *            who composed the message
     * @param message
     *            composed by the user
     */
    public void addChatLine(User user, String message) {
        addChatLine(user, null, message);
    }

    /**
     * Displays a new line containing the supplied message and a separator line
     * if necessary.
     * 
     * @param user
     *            who composed the message
     * @param color
     *            to be used to mark the user; only supported if user not of
     *            type {@link User}
     * @param message
     *            composed by the user
     *            <p>
     *            Note: If the user is of type {@link User} the user's
     *            associated color is used.<br/>
     *            If the user is of another type the <code>toString</code>
     *            method and the provided color is used to display the chat
     *            line.
     */
    @SuppressWarnings("static-access")
    public void addChatLine(Object user, Color color, String message) {
        if (lastUser != null && lastUser.equals(user)) { // same user
            ChatLineSeparator chatLineSeparator;
            if (user instanceof User) {
                chatLineSeparator = new ChatLineSeparator(contentComposite,
                    (User) user, new Date());
            } else {
                chatLineSeparator = new ChatLineSeparator(contentComposite,
                    user.toString(), color, new Date());
            }

            GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true,
                false);
            chatLineSeparator.setLayoutData(gridData);
        } else { // new / different user
            ChatLinePartnerChangeSeparator chatPartnerChangeLine;
            if (user instanceof User) {
                chatPartnerChangeLine = new ChatLinePartnerChangeSeparator(
                    contentComposite, (User) user, new Date());
            } else {
                chatPartnerChangeLine = new ChatLinePartnerChangeSeparator(
                    contentComposite, user.toString(), color, new Date());
            }

            GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true,
                false);
            chatPartnerChangeLine.setLayoutData(gridData);
        }

        ChatLine chatLine = new ChatLine(contentComposite, message);
        GridData chatLineGridData = new GridData(SWT.FILL, SWT.BEGINNING, true,
            false);
        chatLineGridData.horizontalIndent = ChatLinePartnerChangeSeparator.MARGIN_WIDTH;
        chatLine.setLayoutData(chatLineGridData);

        /*
         * Reposition the clear option to the end
         */
        this.createOptionComposite();

        this.refresh();

        lastUser = user;
    }

    /**
     * Return the chat items used to display the current conversation
     * 
     * @return ordered array of items like
     *         {@link ChatLinePartnerChangeSeparator}s and {@link ChatLine}s
     */
    protected Control[] getChatItems() {
        return this.contentComposite.getChildren();
    }

    /**
     * Computes the ideal render widths of non-{@link ChatLine}s and returns the
     * maximum.
     * 
     * @return the maximum ideal render width of all non-{@link ChatLine}s
     */
    protected int computeMaxNonChatLineWidth() {
        int maxNonChatLineWidth = 0;
        for (Control chatItem : getChatItems()) {
            if (!(chatItem instanceof ChatLine)) {
                int currentNonChatLineWidth = chatItem.computeSize(SWT.DEFAULT,
                    SWT.DEFAULT).x;
                maxNonChatLineWidth = Math.max(currentNonChatLineWidth,
                    maxNonChatLineWidth);
            }
        }
        return maxNonChatLineWidth;
    }

    /**
     * Layouts the contents anew, updates the scrollbar min size and scrolls to
     * the bottom
     */
    protected void refresh() {
        /*
         * Layout makes the added controls visible
         */
        this.contentComposite.layout();

        int verticalBarWidth = (this.getVerticalBar() != null) ? this
            .getVerticalBar().getSize().x : 0;

        int widthHint = Math.max(computeMaxNonChatLineWidth()
            + verticalBarWidth, ChatDisplay.this.getClientArea().width);

        final Point neededSize = ChatDisplay.this.contentComposite.computeSize(
            widthHint, SWT.DEFAULT);
        ChatDisplay.this.setMinSize(neededSize);
        ChatDisplay.this.setOrigin(0, neededSize.y);
    }

    /**
     * Clears the chat display
     */
    public void clear() {
        for (Control chatItem : getChatItems()) {
            chatItem.dispose();
        }
        this.refresh();
        this.lastUser = null;
    }
}
