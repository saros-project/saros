package de.fu_berlin.inf.dpp.ui.widgets.chat.parts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleRoundedComposite;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.IChatDisplayListener;
import de.fu_berlin.inf.dpp.ui.widgets.chat.items.ChatLine;
import de.fu_berlin.inf.dpp.ui.widgets.chat.items.ChatLinePartnerChangeSeparator;
import de.fu_berlin.inf.dpp.ui.widgets.chat.items.ChatLineSeparator;

/**
 * This control displays a chat conversation between n users
 * 
 * @author bkahlert
 */
public class ChatDisplay extends ScrolledComposite {

    protected List<IChatDisplayListener> chatDisplayListeners = new ArrayList<IChatDisplayListener>();

    protected Composite contentComposite;
    protected Composite optionsComposite;

    protected JID lastUser;

    public ChatDisplay(Composite parent, int style, Color backgroundColor) {
        super(parent, style);

        this.contentComposite = new Composite(this, SWT.NONE);
        this.contentComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        this.setContent(contentComposite);
        this.setExpandHorizontal(true);
        this.setExpandVertical(true);
        this.getVerticalBar().setIncrement(50);

        // Focus content composite on activation to enable scrolling.
        this.addListener(SWT.Activate, new Listener() {
            @Override
            public void handleEvent(Event e) {
                contentComposite.setFocus();
            }
        });

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
            @Override
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
        clearButton.setText(Messages.ChatDisplay_clear);
        clearButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ChatDisplay.this.clear();
            }

            @Override
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
     * @param jid
     *            JID of the sender who composed the message
     * @param color
     *            the color to be used to mark the user
     * @param message
     *            composed by the sender
     * @param receivedOn
     *            the date the message was received
     */
    public void addChatLine(JID jid, String displayName, Color color,
        String message, Date receivedOn) {
        /*
         * Sender line
         */
        if (lastUser != null && lastUser.equals(jid)) { // same user
            ChatLineSeparator chatLineSeparator = new ChatLineSeparator(
                contentComposite, displayName, color, receivedOn);
            chatLineSeparator.setLayoutData(new GridData(SWT.FILL,
                SWT.BEGINNING, true, false));
            chatLineSeparator.setData(jid);
        } else { // new / different user
            ChatLinePartnerChangeSeparator chatPartnerChangeLine = new ChatLinePartnerChangeSeparator(
                contentComposite, displayName, color, receivedOn);
            chatPartnerChangeLine.setLayoutData(new GridData(SWT.FILL,
                SWT.BEGINNING, true, false));
            chatPartnerChangeLine.setData(jid);
        }

        /*
         * Message line
         */
        ChatLine chatLine = new ChatLine(contentComposite, message);
        GridData chatLineGridData = new GridData(SWT.FILL, SWT.BEGINNING, true,
            false);
        chatLineGridData.horizontalIndent = SimpleRoundedComposite.MARGIN_WIDTH;
        chatLine.setLayoutData(chatLineGridData);

        /*
         * Reposition the clear option to the end
         */
        this.createOptionComposite();

        this.refresh();

        lastUser = jid;
    }

    /**
     * Updates the color of the chat line separators for a specific JID.
     * 
     * @param jid
     *            JID whose color should be updated
     * @param color
     *            the new color
     */
    public void updateColor(JID jid, Color color) {

        for (Control control : contentComposite.getChildren()) {
            if (!jid.equals(control.getData()))
                continue;

            if (control instanceof ChatLineSeparator
                || control instanceof ChatLinePartnerChangeSeparator)
                control.setBackground(color);
        }
    }

    /**
     * Updates the display name of the chat line separators for a specific JID.
     * 
     * @param jid
     *            the JID whose display name should be updated
     * @param displayName
     *            the new display name
     */
    public void updateDisplayName(JID jid, String displayName) {
        for (Control control : contentComposite.getChildren()) {
            if (!jid.equals(control.getData()))
                continue;

            if (control instanceof ChatLinePartnerChangeSeparator) {
                ChatLinePartnerChangeSeparator separator = (ChatLinePartnerChangeSeparator) control;
                separator.setUsername(displayName);
            }
        }

    }

    /**
     * Returns the chat items used to display the current conversation
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
     * Layouts the current contents, updates the scroll bar minimum size and
     * scrolls to the bottom.
     */
    public void refresh() {
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
     * Clears the {@link ChatDisplay}
     */
    public void clear() {
        this.silentClear();
        this.notifyChatCleared();
    }

    /**
     * Clears the {@link ChatDisplay} without firing events
     */
    public void silentClear() {
        for (Control chatItem : getChatItems()) {
            chatItem.dispose();
        }
        this.refresh();
        this.lastUser = null;
    }

    /**
     * Adds a {@link IChatDisplayListener}
     * 
     * @param chatListener
     */
    public void addChatDisplayListener(IChatDisplayListener chatListener) {
        this.chatDisplayListeners.add(chatListener);
    }

    /**
     * Removes a {@link IChatDisplayListener}
     * 
     * @param chatListener
     */
    public void removeChatListener(IChatDisplayListener chatListener) {
        this.chatDisplayListeners.remove(chatListener);
    }

    /**
     * Notify all {@link IChatDisplayListener}s about a cleared chat
     */
    public void notifyChatCleared() {
        for (IChatDisplayListener chatListener : this.chatDisplayListeners) {
            chatListener.chatCleared(new ChatClearedEvent(this));
        }
    }
}
