package de.fu_berlin.inf.dpp.ui.widgets.chatControl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ChatState;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.communication.chat.ChatElement;
import de.fu_berlin.inf.dpp.communication.chat.ChatElement.ChatElementType;
import de.fu_berlin.inf.dpp.communication.chat.ChatHistory;
import de.fu_berlin.inf.dpp.communication.chat.IChat;
import de.fu_berlin.inf.dpp.communication.chat.IChatListener;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.sounds.SoundManager;
import de.fu_berlin.inf.dpp.ui.sounds.SoundPlayer;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.IChatControlListener;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.IChatDisplayListener;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.MessageEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.parts.ChatDisplay;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.parts.ChatInput;
import de.fu_berlin.inf.dpp.ui.widgets.session.ChatRoomsComposite;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.nebula.explanation.ExplanationComposite;
import de.fu_berlin.inf.nebula.utils.ColorUtils;

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
    protected List<IChatControlListener> chatControlListeners = new ArrayList<IChatControlListener>();

    private static final Logger log = Logger.getLogger(ChatControl.class);

    @Inject
    protected ISarosSessionManager sessionManager;

    /**
     * This {@link IChatDisplayListener} is used to forward events fired in the
     * {@link ChatDisplay} so the user only has to add listeners on the
     * {@link ChatControl} and not on all its child components.
     */
    protected IChatDisplayListener chatDisplayListener = new IChatDisplayListener() {
        public void chatCleared(ChatClearedEvent event) {
            colorCache.clear();

            ChatControl.this.chat.clearHistory();
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
                String message = getInputText().trim();

                if (message.length() > 0) {
                    ChatControl.this.notifyMessageEntered(message);

                    sendMessage(message);
                }

                e.doit = false;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.keyCode) {
            case SWT.CR:
            case SWT.KEYPAD_CR:
                /*
                 * We do not want the ENTER to be inserted
                 */
                e.doit = false;
                return;
            default:
                determineCurrentState();
                break;
            }

            ChatControl.this.notifyCharacterEntered(e.character);
        }
    };

    private ISarosSession session;

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        /**
         * Makes sure refreshing the chat is done in the SWT thread
         */
        private void refreshFromHistoryInSWT() {
            Utils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {
                    ChatControl.this.refreshFromHistory();
                }
            });
        }

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            synchronized (ChatControl.this) {
                session = newSarosSession;
            }

            // The chat contains the pre-session colors. Refresh it, to clear
            // the cache and use the in-session colors.
            refreshFromHistoryInSWT();
        }

        @Override
        public void sessionEnding(ISarosSession oldSarosSession) {
            synchronized (ChatControl.this) {
                session = null;
            }

            // The chat contains the in-session colors. Refresh it, to clear the
            // color cache and use the pre-session colors again.
            refreshFromHistoryInSWT();
        }

    };

    private IChatListener chatListener = new IChatListener() {

        @Override
        public void messageReceived(final JID sender, final String message) {
            Utils.runSafeSWTAsync(log, new Runnable() {

                @Override
                public void run() {
                    if (ChatControl.this.isDisposed()) {
                        chat.removeChatListener(chatListener);
                        chatRooms.openChat(chat, false);
                    }

                    addChatLine(new ChatElement(message, sender, new Date()));

                    if (!isOwnJID(sender)) {
                        SoundPlayer.playSound(SoundManager.MESSAGE_RECEIVED);
                    } else {
                        SoundPlayer.playSound(SoundManager.MESSAGE_SENT);
                    }
                }
            });

        }

        @Override
        public void stateChanged(final JID jid, final ChatState state) {
            Utils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {
                    if (isOwnJID(jid)) {
                        return;
                    }

                    CTabItem tab = chatRooms.getChatTab(chat);

                    if (state == ChatState.composing) {
                        tab.setImage(ChatRoomsComposite.composingImage);
                    } else {
                        tab.setImage(ChatRoomsComposite.chatViewImage);
                    }
                }
            });
        }

        @Override
        public void connected(JID jid) {
            Utils.runSafeSWTAsync(null, new Runnable() {

                @Override
                public void run() {
                    chatInput.setEnabled(true);
                }
            });
        }

        @Override
        public void disconnected(JID jid) {
            Utils.runSafeSWTAsync(null, new Runnable() {

                @Override
                public void run() {
                    chatInput.setEnabled(false);
                }
            });
        }

        @Override
        public void joined(JID jid) {
            if (!isOwnJID(jid)) {
                addChatLine(new ChatElement(jid, new Date(),
                    ChatElementType.JOIN));
            }
        }

        @Override
        public void left(JID jid) {
            if (!isOwnJID(jid)) {
                addChatLine(new ChatElement(jid, new Date(),
                    ChatElementType.LEAVE));
            }
        }

    };

    /**
     * Chat layer
     */
    protected SashForm sashForm;
    protected ChatRoomsComposite chatRooms;
    protected ChatDisplay chatDisplay;
    protected ChatInput chatInput;
    protected IChat chat;

    public ChatControl(ChatRoomsComposite chatRooms, IChat chat,
        Composite parent, int style, Color displayBackgroundColor,
        Color inputBackgroundColor, final int minVisibleInputLines) {
        super(parent, style & ~SWT.BORDER);

        SarosPluginContext.initComponent(this);

        int chatDisplayStyle = (style & SWT.BORDER) | SWT.V_SCROLL
            | SWT.H_SCROLL;
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

        this.chatRooms = chatRooms;

        this.chat = chat;
        this.chat.addChatListener(chatListener);

        this.sessionManager.addSarosSessionListener(sessionListener);

        for (ChatElement chatElement : this.chat.getHistory()) {
            addChatLine(chatElement);
        }
    }

    public boolean isOwnJID(JID jid) {
        return jid.equals(chat.getJID());
    }

    /**
     * Recreates the {@link ChatControl}s contents on the base of the
     * {@link ChatHistory}
     */
    public void refreshFromHistory() {
        List<ChatElement> entries = chat.getHistory();

        ChatControl.colorCache.clear();
        silentClear();

        for (ChatElement element : entries) {
            addChatLine(element);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        chat.removeChatListener(chatListener);
    }

    private static final Map<JID, Color> colorCache = new HashMap<JID, Color>();

    public void addChatLine(ChatElement element) {
        JID sender = element.getSender();

        Color color = colorCache.get(sender);
        if (color == null) {
            synchronized (ChatControl.this) {
                if (session != null) {
                    User user = session.getUser(sender);

                    // add default lightness to cached color
                    Color userColor = SarosAnnotation.getUserColor(user);
                    color = ColorUtils.addLightness(userColor,
                        SarosAnnotation.getLightnessScale());
                    userColor.dispose();

                    colorCache.put(sender, color);
                } else if (isOwnJID(element.getSender())) {
                    color = this.getDisplay().getSystemColor(SWT.COLOR_CYAN);
                } else {
                    color = this.getDisplay().getSystemColor(SWT.COLOR_GRAY);
                }
            }
        }

        chatDisplay.addChatLine(sender, color, element.toString(),
            element.getDate());
    }

    /**
     * Sets the chat input's text
     * 
     * @param string
     *            the new text
     */
    public void setInputText(String string) {
        chatInput.setText(string);
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
     * Sends message if there is any input to send.
     */
    private void sendMessage(String message) {

        if (message.length() != 0) {
            try {
                chat.sendMessage(message);
                setInputText("");
            } catch (Exception exception) {
                addChatLine(new ChatElement("error while sending message: "
                    + exception.getMessage(), chat.getJID(), new Date()));
            }
        }
    }

    private void determineCurrentState() {
        try {
            if (ChatControl.this.getInputText().length() == 0) {
                ChatControl.this.chat.setCurrentState(ChatState.inactive);
            } else {
                ChatControl.this.chat.setCurrentState(ChatState.composing);
            }
        } catch (XMPPException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * Adds a {@link IChatControlListener}
     * 
     * @param chatControlListener
     */
    public void addChatControlListener(IChatControlListener chatControlListener) {
        this.chatControlListeners.add(chatControlListener);
    }

    /**
     * Removes a {@link IChatControlListener}
     * 
     * @param chatControlListener
     */
    public void removeChatControlListener(
        IChatControlListener chatControlListener) {
        this.chatControlListeners.remove(chatControlListener);
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
        chatDisplay.clear();
    }

    /**
     * @see ChatDisplay#silentClear()
     */
    public void silentClear() {
        chatDisplay.silentClear();
    }

    @Override
    public boolean setFocus() {
        return chatInput.setFocus();
    }
}
