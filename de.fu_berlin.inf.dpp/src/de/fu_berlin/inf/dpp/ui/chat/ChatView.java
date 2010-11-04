package de.fu_berlin.inf.dpp.ui.chat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smackx.ChatState;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.MessagingManager;
import de.fu_berlin.inf.dpp.MessagingManager.IChatListener;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.actions.IMBeepAction;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.ChatControl;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.MessageEnteredEvent;
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

    private static Logger log = Logger.getLogger(ChatView.class);

    protected SimpleExplanation howTo = new SimpleExplanation(
        SWT.ICON_INFORMATION,
        "To use this chat you need to be connected to a Saros session.");

    protected ChatControl chatControl;

    @Inject
    protected MessagingManager messagingManager;

    protected Map<User, ChatState> userStates = new HashMap<User, ChatState>();

    @Inject
    protected SessionManager sessionManager;

    protected boolean sessionStarted = false;

    protected IMBeepAction imBeepAction;

    protected IChatListener chatListener = new IChatListener() {

        public void chatJoined(final User joinedUser) {
            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    ChatView.this.chatControl.addChatLine(joinedUser,
                        "... joined the chat.");
                    User localUser = sessionManager.getSarosSession()
                        .getLocalUser();
                    if (localUser.equals(joinedUser))
                        ChatView.this.hideExplanation();
                }
            });
        }

        public void chatLeft(final User leftUser) {
            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    ChatView.this.chatControl.addChatLine(leftUser,
                        "... left the chat.");

                    User localUser = sessionManager.getSarosSession()
                        .getLocalUser();
                    if (localUser.equals(leftUser))
                        ChatView.this.showExplanation(howTo);
                }
            });
        }

        public void chatMessageAdded(final User user, final String message) {
            ChatView.log
                .debug("Received Message from " + user + ": " + message);

            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    chatControl.addChatLine(user, message);

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
        public void chatStateUpdated(final User sender, final ChatState state) {
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
    }

    @Override
    public void createContentPartControl(Composite parent) {
        parent.setLayout(new FillLayout());

        this.chatControl = new ChatControl(parent, SWT.BORDER, parent
            .getDisplay().getSystemColor(SWT.COLOR_WHITE), parent.getDisplay()
            .getSystemColor(SWT.COLOR_WHITE), 2);

        this.chatControl
            .addChatListener(new de.fu_berlin.inf.dpp.ui.chat.chatControl.events.IChatListener() {
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
            });

        this.showExplanation(howTo);

        // Register the chat listener.
        // Run a possible join() in a separate thread to prevent the opening of
        // this view from blocking the SWT thread.
        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                messagingManager.addChatListener(chatListener);
            }
        });

        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(this.imBeepAction = new IMBeepAction("Toggle beep"));
    }

    @Override
    public void setFocus() {
        this.chatControl.setFocus();
    }

    @Override
    public void dispose() {
        messagingManager.removeChatListener(chatListener);
        super.dispose();
    }
}