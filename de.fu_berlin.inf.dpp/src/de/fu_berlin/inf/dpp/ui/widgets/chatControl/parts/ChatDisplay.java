package de.fu_berlin.inf.dpp.ui.widgets.chatControl.parts;

import java.util.Date;
import java.util.Vector;

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

import de.fu_berlin.inf.dpp.ui.widgets.SimpleRoundedComposite;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.IChatDisplayListener;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.items.ChatLine;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.items.ChatLinePartnerChangeSeparator;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.items.ChatLineSeparator;

/**
 * This control displays a chat conversation between n users
 * 
 * @author bkahlert
 */
public class ChatDisplay extends ScrolledComposite {
    protected Vector<IChatDisplayListener> chatDisplayListeners = new Vector<IChatDisplayListener>();

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
     * @param sender
     *            who composed the message
     * @param color
     *            to be used to mark the user
     * @param message
     *            composed by the sender
     * @param receivedOn
     *            the date the message was received
     */
    public void addChatLine(Object sender, Color color, String message,
        Date receivedOn) {
        /*
         * Sender line
         */
        if (lastUser != null && lastUser.equals(sender)) { // same user
            ChatLineSeparator chatLineSeparator = new ChatLineSeparator(
                contentComposite, sender.toString(), color, receivedOn);
            chatLineSeparator.setLayoutData(new GridData(SWT.FILL,
                SWT.BEGINNING, true, false));
        } else { // new / different user
            ChatLinePartnerChangeSeparator chatPartnerChangeLine = new ChatLinePartnerChangeSeparator(
                contentComposite, sender.toString(), color, receivedOn);
            chatPartnerChangeLine.setLayoutData(new GridData(SWT.FILL,
                SWT.BEGINNING, true, false));
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

        lastUser = sender;
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
     * Layouts the contents anew, updates the scrollbar min size and scrolls to
     * the bottom
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
        this.chatDisplayListeners.addElement(chatListener);
    }

    /**
     * Removes a {@link IChatDisplayListener}
     * 
     * @param chatListener
     */
    public void removeChatListener(IChatDisplayListener chatListener) {
        this.chatDisplayListeners.removeElement(chatListener);
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
