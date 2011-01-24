package de.fu_berlin.inf.dpp.ui.widgets.chatControl;

import java.util.Date;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.IChatControlListener;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.IChatDisplayListener;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.MessageEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.parts.ChatDisplay;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.parts.ChatInput;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.ExplanationComposite;

/**
 * This composite displays a chat conversation and the possibility to enter
 * text.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout and adding
 * sub {@link Control}s correctly.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER and those supported by {@link ExplanationComposite}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>{@link MessageEnteredEvent}</dd>
 * </dl>
 * 
 * @author bkahlert
 * 
 */
public class ChatControl extends Composite {
    protected Vector<IChatControlListener> chatControlListeners = new Vector<IChatControlListener>();

    /**
     * This {@link IChatDisplayListener} is used to forward events fired in the
     * {@link ChatDisplay} so the user only has to add listeners on the
     * {@link ChatControl} and not on all its child components.
     */
    protected IChatDisplayListener chatDisplayListener = new IChatDisplayListener() {
        public void chatCleared(ChatClearedEvent event) {
            ChatControl.this.notifyChatCleared(event);
        }
    };

    /**
     * This {@link KeyAdapter} is used to forward events fired in the
     * {@link ChatInput} so the user only has to add listeners on the
     * {@link ChatControl} and not on all its child components.
     */
    protected KeyAdapter chatInputListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.keyCode) {
            case SWT.CR:
            case SWT.KEYPAD_CR:
                if (e.stateMask == 0) {
                    String message = ChatControl.this.getInputText().trim();
                    ChatControl.this.setInputText("");
                    if (!message.isEmpty())
                        ChatControl.this.notifyMessageEntered(message);

                    /*
                     * We do not want the ENTER to be inserted
                     */
                    e.doit = false;
                }
                break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            ChatControl.this.notifyCharacterEntered(e.character);
        }
    };

    /**
     * Chat layer
     */
    protected SashForm sashForm;
    protected ChatDisplay chatDisplay;
    protected ChatInput chatInput;

    public ChatControl(Composite parent, int style,
        Color displayBackgroundColor, Color inputBackgroundColor,
        final int minVisibleInputLines) {
        super(parent, style & ~SWT.BORDER);

        int chatDisplayStyle = (style & SWT.BORDER) | SWT.V_SCROLL;
        int chatInputStyle = (style & SWT.BORDER) | SWT.MULTI | SWT.V_SCROLL
            | SWT.WRAP;

        this.setLayout(new FillLayout());

        this.sashForm = new SashForm(this, SWT.VERTICAL);

        // ChatDisplay
        this.chatDisplay = new ChatDisplay(sashForm, chatDisplayStyle,
            displayBackgroundColor);
        this.chatDisplay.setAlwaysShowScrollBars(true);
        this.chatDisplay.addChatDisplayListener(this.chatDisplayListener);

        // ChatInput
        this.chatInput = new ChatInput(sashForm, chatInputStyle);
        this.chatInput.addKeyListener(this.chatInputListener);

        /*
         * Updates SashForm weights to emulate a fixed ChatInput height
         */
        this.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                int fullHeight = ChatControl.this.getSize().y;
                int chatInputHeight = ChatControl.this.chatInput.getSize().y;
                int lineHeight = (int) Math.round(chatInput.getFont()
                    .getFontData()[0].getHeight() * 1.4);
                int minChatInputHeight = minVisibleInputLines * lineHeight;
                if (chatInputHeight < minChatInputHeight) {
                    chatInputHeight = minChatInputHeight;
                }

                int newChatDisplayHeight = fullHeight - chatInputHeight;

                if (newChatDisplayHeight <= 0 || chatInputHeight <= 0)
                    return;

                sashForm.setWeights(new int[] { newChatDisplayHeight,
                    chatInputHeight });
            }
        });

        /*
         * no need for dispose handling because all child controls (and
         * listeners) are disposed automatically
         */
    }

    /**
     * @see ChatDisplay#addChatLine(Object, Color, String, Date)
     */
    public void addChatLine(Object sender, Color color, String message,
        Date receivedOn) {
        this.chatDisplay.addChatLine(sender, color, message, receivedOn);
    }

    /**
     * Sets the chat input's text
     * 
     * @param string
     *            the new text
     */
    public void setInputText(String string) {
        this.chatInput.setText(string);
    }

    /**
     * Return entered text in the chat input
     * 
     * @return the entered text
     */
    public String getInputText() {
        return this.chatInput.getText();
    }

    /**
     * Adds a {@link IChatControlListener}
     * 
     * @param chatControlListener
     */
    public void addChatControlListener(IChatControlListener chatControlListener) {
        this.chatControlListeners.addElement(chatControlListener);
    }

    /**
     * Removes a {@link IChatControlListener}
     * 
     * @param chatControlListener
     */
    public void removeChatControlListener(
        IChatControlListener chatControlListener) {
        this.chatControlListeners.removeElement(chatControlListener);
    }

    /**
     * Notify all {@link IChatControlListener}s about entered character
     * 
     * @param character
     *            the entered character
     */
    public void notifyCharacterEntered(Character character) {
        for (IChatControlListener chatControlListener : this.chatControlListeners) {
            chatControlListener.characterEntered(new CharacterEnteredEvent(
                this, character));
        }
    }

    /**
     * Notify all {@link IChatControlListener}s about entered text
     * 
     * @param message
     *            the entered text
     */
    public void notifyMessageEntered(String message) {
        for (IChatControlListener chatControlListener : this.chatControlListeners) {
            chatControlListener.messageEntered(new MessageEnteredEvent(this,
                message));
        }
    }

    /**
     * Notify all {@link IChatDisplayListener}s about a cleared chat
     */
    public void notifyChatCleared(ChatClearedEvent event) {
        for (IChatControlListener chatControlListener : this.chatControlListeners) {
            chatControlListener.chatCleared(event);
        }
    }

    /**
     * @see ChatDisplay#clear()
     */
    public void clear() {
        this.chatDisplay.clear();
    }

    /**
     * @see ChatDisplay#silentClear()
     */
    public void silentClear() {
        this.chatDisplay.silentClear();
    }

    @Override
    public boolean setFocus() {
        return this.chatInput.setFocus();
    }
}
