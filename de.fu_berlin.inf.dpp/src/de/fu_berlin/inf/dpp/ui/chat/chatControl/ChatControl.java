package de.fu_berlin.inf.dpp.ui.chat.chatControl;

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

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.IChatListener;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.MessageEnteredEvent;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.parts.ChatDisplay;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.parts.ChatInput;
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
    protected Vector<IChatListener> chatListeners = new Vector<IChatListener>();

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

        this.chatDisplay = new ChatDisplay(sashForm, chatDisplayStyle,
            displayBackgroundColor);

        this.chatDisplay.setAlwaysShowScrollBars(true);

        this.chatInput = new ChatInput(sashForm, chatInputStyle);

        /*
         * Generate MessageEnteredEvents
         */
        this.chatInput.addKeyListener(new KeyAdapter() {
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
        });

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
    }

    /**
     * @see ChatDisplay#addChatLine(User, String)
     */
    public void addChatLine(User user, String message) {
        this.chatDisplay.addChatLine(user, message);
    }

    /**
     * @see ChatDisplay#addChatLine(Object, Color, String)
     */
    public void addChatLine(Object user, Color color, String message) {
        this.chatDisplay.addChatLine(user, color, message);
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
     * Adds a chat listener
     * 
     * @param chatListener
     */
    public void addChatListener(IChatListener chatListener) {
        this.chatListeners.addElement(chatListener);
    }

    /**
     * Removes a chat listener
     * 
     * @param chatListener
     */
    public void removeChatListener(IChatListener chatListener) {
        this.chatListeners.removeElement(chatListener);
    }

    /**
     * Notify all chat listener about entered character
     * 
     * @param character
     *            the entered character
     */
    public void notifyCharacterEntered(Character character) {
        for (IChatListener chatListener : this.chatListeners) {
            chatListener.characterEntered(new CharacterEnteredEvent(this,
                character));
        }
    }

    /**
     * Notify all chat listener about entered text
     * 
     * @param message
     *            the entered text
     */
    public void notifyMessageEntered(String message) {
        for (IChatListener chatListener : this.chatListeners) {
            chatListener.messageEntered(new MessageEnteredEvent(this, message));
        }
    }

    /**
     * Clears the ChatDisplay
     */
    public void clear() {
        this.chatDisplay.clear();
    }

    @Override
    public boolean setFocus() {
        return this.chatInput.setFocus();
    }

}
