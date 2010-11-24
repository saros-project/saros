package de.fu_berlin.inf.dpp.ui.chat;

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
import de.fu_berlin.inf.dpp.communication.muc.events.IMUCManagerListener;
import de.fu_berlin.inf.dpp.communication.muc.events.MUCManagerAdapter;
import de.fu_berlin.inf.dpp.communication.muc.session.MUCSession;
import de.fu_berlin.inf.dpp.communication.muc.session.events.IMUCSessionListener;
import de.fu_berlin.inf.dpp.communication.muc.session.history.MUCSessionHistory;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryElement;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryJoinElement;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryLeaveElement;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryMessageReceptionElement;
import de.fu_berlin.inf.dpp.communication.muc.singleton.MUCManagerSingletonWrapperChatView;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.actions.IMBeepAction;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.ChatControl;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.IChatControlListener;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.MessageEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory.SimpleExplanatoryViewPart;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Saros' {@link ChatView}.
 * 
 * @author bkahlert
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

    protected ChatControl chatControl;

    @Inject
    protected MUCManagerSingletonWrapperChatView mucManager;

    @Inject
    protected SarosSessionManager sessionManager;

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

    /**
     * Adds/removes an {@link IMUCSessionListener} to/from the
     * {@link MUCManagerSingletonWrapperChatView} depending on its availability.
     */
    protected IMUCManagerListener mucManagerListener = new MUCManagerAdapter() {
        @Override
        public void mucSessionJoined(MUCSession mucSession) {
            ChatView.this.attachToMUCSession(mucSession);
        }

        @Override
        public void mucSessionLeft(MUCSession mucSession) {
            ChatView.this.detachFromMUCSession(mucSession);
        }
    };

    /**
     * Handles events on the {@link MUCManagerSingletonWrapperChatView}
     */
    protected IMUCSessionListener mucSessionListener = new IMUCSessionListener() {
        public void joined(final JID jid) {
            ChatView.this.addChatLine(new MUCSessionHistoryJoinElement(jid,
                new Date()));

            if (ChatView.this.isOwnJID(jid)) {
                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        ChatView.this.hideExplanation();
                    }
                });
            }
        }

        public void left(final JID jid) {
            ChatView.this.addChatLine(new MUCSessionHistoryLeaveElement(jid,
                new Date()));

            if (ChatView.this.isOwnJID(jid)) {
                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        ChatView.this.showExplanation(howtoExplanation);
                    }
                });
            }
        }

        public void messageReceived(final JID jid, final String message) {
            ChatView.this
                .addChatLine(new MUCSessionHistoryMessageReceptionElement(jid,
                    new Date(), message));

            /*
             * Beep when receiving a FOREIGN message
             */
            if (!ChatView.this.isOwnJID(jid)) {
                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        imBeepAction.beep();
                    }
                });
            }
        }

        public void stateChanged(final JID sender, final ChatState state) {
            ChatView.log.debug("Received ChatState from " + sender + ": "
                + state.toString());

            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    if (mucManager.getMUCSession() != null) {
                        if (mucManager.getMUCSession().getForeignStatesCount(
                            ChatState.composing) > 0) {
                            ChatView.this.setTitleImage(composingImage);
                        } else {
                            ChatView.this.setTitleImage(chatViewImage);
                        }
                    }
                }
            });
        }
    };

    /**
     * Handles events that occur in the {@link ChatControl}
     */
    protected IChatControlListener chatControlListener = new IChatControlListener() {
        /**
         * Update one's own {@link ChatState}
         */
        public void characterEntered(CharacterEnteredEvent event) {

            if (ChatView.this.chatControl.getInputText().isEmpty()) {
                mucManager.getMUCSession().setState(ChatState.inactive);
            } else {
                mucManager.getMUCSession().setState(ChatState.composing);
            }
        }

        /**
         * Sends the entered message
         */
        public void messageEntered(MessageEnteredEvent event) {
            String enteredMessage = event.getEnteredMessage();

            if (mucManager.getMUCSession() != null) {
                mucManager.getMUCSession().sendMessage(enteredMessage);
            }
        }

        public void chatCleared(ChatClearedEvent event) {
            /*
             * If the users chooses to clear the chat we do not want keep the
             * information in the chat history
             */
            if (mucManager.getMUCSession() != null) {
                mucManager.getMUCSession().clearHistory();
            }

            // TODO: Dispose used light colors here
        }
    };

    public ChatView() {
        Saros.reinject(this);
        editorManager.addSharedEditorListener(sharedEditorListener);
        mucManager.addMUCManagerListener(mucManagerListener);
    }

    public void attachToMUCSession(MUCSession mucSession) {
        mucSession.addMUCSessionListener(mucSessionListener);
    }

    public void detachFromMUCSession(MUCSession mucSession) {
        mucSession.removeMUCSessionListener(mucSessionListener);
    }

    @Override
    public void createContentPartControl(Composite parent) {
        parent.setLayout(new FillLayout());

        this.chatControl = new ChatControl(parent, SWT.BORDER, parent
            .getDisplay().getSystemColor(SWT.COLOR_WHITE), parent.getDisplay()
            .getSystemColor(SWT.COLOR_WHITE), 2);

        this.chatControl.addChatControlListener(chatControlListener);

        /*
         * IMPORTANT: The user can open and close Views as he wishes. This means
         * that the live cycle of this ChatView is completely independent of the
         * global MUCSession. Therefore we need to correctly validate the
         * MUCSession's state when this ChatView is reopened.
         */
        if (this.joinedSession()) {
            this.attachToMUCSession(mucManager.getMUCSession());
        } else {
            this.showExplanation(howtoExplanation);
        }

        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(this.imBeepAction = new IMBeepAction("Toggle beep"));

        // Show already received messages
        this.refreshFromHistory();
    }

    /**
     * Used to cache the sender names belonging to a {@link JID} determined
     * {@link User}
     */
    protected Map<JID, String> senderCache = new HashMap<JID, String>();

    /**
     * Used to cache the {@link Color}s belonging to a {@link JID} determined
     * {@link User}
     */
    protected Map<JID, Color> colorCache = new HashMap<JID, Color>();

    /**
     * Adds a new line to the chat control
     * 
     * @param jid
     * @param message
     * @param receivedOn
     */
    protected void addChatLine(final JID jid, final String message,
        final Date receivedOn) {

        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return;

        User user = sarosSession.getUser(jid);
        /*
         * For the case a left user notification came after the moment the user
         * left the session we need to cache his metrics.
         */
        if (user != null) {
            senderCache.put(jid, user.getHumanReadableName());
            colorCache.put(jid, SarosAnnotation.getLightUserColor(user));
        }
        final String sender = senderCache.get(jid);
        final Color color = colorCache.get(jid);

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                if (ChatView.this.chatControl == null
                    || ChatView.this.chatControl.isDisposed())
                    return;

                ChatView.this.chatControl.addChatLine(sender, color, message,
                    receivedOn);
            }
        });
    }

    protected void addChatLine(MUCSessionHistoryJoinElement join) {
        addChatLine(join.getSender(), "... joined the chat.", join.getDate());
    }

    protected void addChatLine(MUCSessionHistoryLeaveElement leave) {
        addChatLine(leave.getSender(), "... left the chat.", leave.getDate());
    }

    protected void addChatLine(
        MUCSessionHistoryMessageReceptionElement messageReception) {
        addChatLine(messageReception.getSender(),
            messageReception.getMessage(), messageReception.getDate());
    }

    /**
     * Recreates the {@link ChatControl}s contents on the base of the
     * {@link MUCSessionHistory}
     */
    public void refreshFromHistory() {
        if (this.chatControl == null || this.chatControl.isDisposed()
            || mucManager.getMUCSession() == null)
            return;

        MUCSessionHistoryElement[] entries = mucManager.getMUCSession()
            .getHistory();
        chatControl.silentClear();
        for (MUCSessionHistoryElement element : entries) {
            if (element instanceof MUCSessionHistoryJoinElement)
                addChatLine((MUCSessionHistoryJoinElement) element);
            if (element instanceof MUCSessionHistoryLeaveElement)
                addChatLine((MUCSessionHistoryLeaveElement) element);
            if (element instanceof MUCSessionHistoryMessageReceptionElement)
                addChatLine((MUCSessionHistoryMessageReceptionElement) element);
        }
    }

    /**
     * Returns true if the provided JID equals the one used for connection to
     * {@link MUCManagerSingletonWrapperChatView}.
     * 
     * @param jid
     * @return
     */
    public boolean isOwnJID(JID jid) {
        if (mucManager.getMUCSession() != null) {
            JID localJID = mucManager.getMUCSession().getJID();
            boolean isOwnJID = localJID.equals(jid);
            return isOwnJID;
        }
        return false;
    }

    /**
     * Returns true if the {@link MUCManagerSingletonWrapperChatView} has been
     * joined.
     * 
     * @return
     */
    public boolean joinedSession() {
        return (mucManager.getMUCSession() != null && mucManager
            .getMUCSession().isJoined());
    }

    @Override
    public void setFocus() {
        this.chatControl.setFocus();
    }

    @Override
    public void dispose() {
        this.chatControl.removeChatControlListener(chatControlListener);
        if (mucManager.getMUCSession() != null) {
            detachFromMUCSession(mucManager.getMUCSession());
        }
        mucManager.removeMUCManagerListener(mucManagerListener);
        editorManager.removeSharedEditorListener(sharedEditorListener);
        super.dispose();

        // TODO: Dispose used light colors here
    }
}