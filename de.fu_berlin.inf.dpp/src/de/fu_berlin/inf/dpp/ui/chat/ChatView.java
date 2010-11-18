package de.fu_berlin.inf.dpp.ui.chat;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smackx.ChatState;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.multiUserChat.IMultiUserChatListener;
import de.fu_berlin.inf.dpp.communication.multiUserChat.MessagingManager;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.actions.IMBeepAction;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.ChatControl;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.MessageEnteredEvent;
import de.fu_berlin.inf.dpp.ui.chat.history.ChatHistory;
import de.fu_berlin.inf.dpp.ui.chat.history.ChatHistoryEntry;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory.SimpleExplanatoryViewPart;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Saros' Chat View.
 * 
 * @author ologa
 * @author ahaferburg
 * @author bkahlert (complete reface - 2010/09/16)
 */
@Component(module = "ui")
public class ChatView extends SimpleExplanatoryViewPart {
    private static Logger log = Logger.getLogger(ChatView.class);

    /**
     * Default image for ChatView.
     */
    public static final Image chatViewImage = SarosUI
        .getImage("icons/comment.png");

    /**
     * Image while composing a message.
     */
    public static final Image composingImage = SarosUI
        .getImage("icons/composing.png");

    protected SimpleExplanation howtoExplanation = new SimpleExplanation(
        SWT.ICON_INFORMATION,
        "To use this chat you need to be connected to a Saros session.");

    protected SimpleExplanation refreshExplanation = new SimpleExplanation(
        SWT.ICON_INFORMATION, "Refreshing...");

    protected static boolean joinedRoom = false;

    protected ChatControl chatControl;

    protected ChatHistory chatHistory = new ChatHistory();

    @Inject
    protected MessagingManager messagingManager;

    protected Map<User, ChatState> userStates = new HashMap<User, ChatState>();

    @Inject
    protected SessionManager sessionManager;

    protected IMBeepAction imBeepAction;

    @Inject
    // TODO: see
    // https://sourceforge.net/tracker/?func=detail&aid=3102858&group_id=167540&atid=843362
    protected EditorManager editorManager;
    protected ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        @Override
        public void colorChanged() {
            if (chatControl != null && !chatControl.isDisposed()) {
                ChatView.this.refreshFromHistory();
            }
        }
    };

    protected IMultiUserChatListener chatListener = new IMultiUserChatListener() {

        public void userJoined(final User joinedUser) {
            ChatView.this.addChatLine(joinedUser, "... joined the chat.",
                new Date());

            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    User localUser = sessionManager.getSarosSession()
                        .getLocalUser();
                    if (localUser.equals(joinedUser)) {
                        ChatView.this.hideExplanation();
                        ChatView.joinedRoom = true;
                    }
                }
            });
        }

        public void userLeft(final User leftUser) {
            ChatView.this.addChatLine(leftUser, "... left the chat.",
                new Date());

            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    User localUser = sessionManager.getSarosSession()
                        .getLocalUser();
                    if (localUser.equals(leftUser)) {
                        ChatView.this.showExplanation(howtoExplanation);
                        ChatView.joinedRoom = false;
                    }
                }
            });
        }

        public void messageReceived(final User user, final String message) {
            ChatView.this.addChatLine(user, message, new Date());

            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {

                    /*
                     * Beep when receiving a foreign message
                     */
                    User localUser = sessionManager.getSarosSession()
                        .getLocalUser();
                    if (!localUser.equals(user))
                        imBeepAction.beep();
                }
            });
        }

        /*
         * Updates the state of the chat and keeps track of every users current
         * state in the chat. Furthermore changes the image of the view, if
         * anyone is composing a message.
         */
        public void stateChanged(final User sender, final ChatState state) {
            ChatView.log.debug("Received ChatState from " + sender + ": "
                + state.toString());

            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    /*
                     * Adds foreign sender with state to a map of states. If the
                     * sender was already in the map the old state gets
                     * overwritten.
                     */
                    User localUser = sessionManager.getSarosSession()
                        .getLocalUser();
                    if (localUser.equals(sender)) {
                        return;
                    }
                    userStates.put(sender, state);

                    /*
                     * Sets the composing ChatView image if somebody is
                     * composing a message; default image otherwise. Can use
                     * other ChatStates for different images as well.
                     */
                    Collection<ChatState> currentStates = userStates.values();
                    if (currentStates.contains(ChatState.composing)) {
                        ChatView.this.setTitleImage(composingImage);
                    } else {
                        ChatView.this.setTitleImage(chatViewImage);
                    }

                }
            });
        }
    };

    public ChatView() {
        Saros.reinject(this);
        editorManager.addSharedEditorListener(sharedEditorListener);
        messagingManager.addChatListener(chatListener);
    }

    @Override
    public void createContentPartControl(Composite parent) {
        parent.setLayout(new FillLayout());

        this.chatControl = new ChatControl(parent, SWT.BORDER, parent
            .getDisplay().getSystemColor(SWT.COLOR_WHITE), parent.getDisplay()
            .getSystemColor(SWT.COLOR_WHITE), 2);

        this.chatControl
            .addChatControlListener(new de.fu_berlin.inf.dpp.ui.chat.chatControl.events.IChatControlListener() {
                public void characterEntered(CharacterEnteredEvent event) {
                    /*
                     * Sends a message with state composing.
                     */
                    if (ChatView.this.chatControl.getInputText().isEmpty()) {
                        messagingManager.getSession().sendMessage(null,
                            ChatState.inactive);
                    } else {
                        messagingManager.getSession().sendMessage(null,
                            ChatState.composing);
                    }
                }

                public void messageEntered(MessageEnteredEvent event) {
                    String enteredMessage = event.getEnteredMessage();

                    if (messagingManager.getSession() != null) {
                        messagingManager.getSession().sendMessage(
                            enteredMessage, null);
                    }
                }

                public void chatCleared(ChatClearedEvent event) {
                    /*
                     * If the users chooses to clear the chat we do not want
                     * keep the information in the chat history
                     */
                    ChatView.this.chatHistory.clear();
                }
            });

        if (!joinedRoom) {
            this.showExplanation(howtoExplanation);
        }

        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(this.imBeepAction = new IMBeepAction("Toggle beep"));

        // Show eventually already received messages
        this.refreshFromHistory();
    }

    /**
     * Adds a new line to the chat control and logs it to the
     * {@link ChatHistory}
     * 
     * @param user
     * @param message
     * @param receivedOn
     */
    protected void addChatLine(final User user, final String message,
        final Date receivedOn) {

        chatHistory.addEntry(new ChatHistoryEntry(user, message, receivedOn));

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                if (ChatView.this.chatControl == null
                    || ChatView.this.chatControl.isDisposed())
                    return;

                String sender = user.getHumanReadableName();
                Color color = SarosAnnotation.getLightUserColor(user);
                ChatView.this.chatControl.addChatLine(sender, color, message,
                    receivedOn);
            }
        });
    }

    /**
     * Recreates the {@link ChatControl}s contents on the base of the
     * {@link ChatHistory}
     */
    public void refreshFromHistory() {
        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                if (ChatView.this.chatControl == null
                    || ChatView.this.chatControl.isDisposed())
                    return;

                /*
                 * As soon as we call ChatControl.clear it calls our own
                 * listener which will clear the ChatHistory. We therefore need
                 * to save the history before clearing the chat.
                 */
                ChatHistoryEntry[] entries = ChatView.this.chatHistory
                    .getEntries();
                ChatView.this.chatControl.clear();
                for (ChatHistoryEntry entry : entries) {
                    ChatView.this.addChatLine(entry.getSender(),
                        entry.getMessage(), entry.getReceivedOn());
                }
            }
        });
    }

    @Override
    public void setFocus() {
        this.chatControl.setFocus();
    }

    @Override
    public void dispose() {
        editorManager.removeSharedEditorListener(sharedEditorListener);
        messagingManager.removeChatListener(chatListener);
        super.dispose();
    }
}