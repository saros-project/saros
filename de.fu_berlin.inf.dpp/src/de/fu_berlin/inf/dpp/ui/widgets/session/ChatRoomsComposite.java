package de.fu_berlin.inf.dpp.ui.widgets.session;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.jivesoftware.smackx.ChatState;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
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
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.ui.sounds.SoundManager;
import de.fu_berlin.inf.dpp.ui.sounds.SoundPlayer;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.ChatControl;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.IChatControlListener;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.MessageEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.ListExplanationComposite.ListExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory.ListExplanatoryComposite;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This component shows chat he right side of the {@link SarosView}
 * 
 * @author patbit
 */

public class ChatRoomsComposite extends ListExplanatoryComposite {

    private static final Logger log = Logger
        .getLogger(ChatRoomsComposite.class);

    /**
     * Default image for ChatView.
     */
    public static final Image chatViewImage = ImageManager
        .getImage("icons/view16/chat_misc.png");

    /**
     * Image while composing a message.
     */
    public static final Image composingImage = ImageManager
        .getImage("icons/view16/cmpsg_misc.png");

    protected ListExplanation howTo = new ListExplanation(SWT.ICON_INFORMATION,
        "To share projects you can either:", "Right-click on a project",
        "Right-click on a buddy", "Use the Saros menu in the Eclipse menu bar");

    @Inject
    /*
     * TODO: see
     * https://sourceforge.net/tracker/?func=detail&aid=3102858&group_id
     * =167540&atid=843362
     */
    protected EditorManager editorManager;
    protected AbstractSharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        @Override
        public void colorChanged() {
            if (chatControl != null && !chatControl.isDisposed()) {
                refreshFromHistory();
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
            attachToMUCSession(mucSession);
        }

        @Override
        public void mucSessionLeft(MUCSession mucSession) {
            detachFromMUCSession(mucSession);
        }
    };

    /**
     * Handles events on the {@link MUCManagerSingletonWrapperChatView}
     */
    protected IMUCSessionListener mucSessionListener = new IMUCSessionListener() {
        public void joined(final JID jid) {
            addChatLine(new MUCSessionHistoryJoinElement(jid, new Date()));

            if (isOwnJID(jid)) {
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {

                        hideExplanation();
                        chatRoom1 = new CTabItem(chatRooms, SWT.NONE);
                        chatRoom1.setText("Chatroom 1");
                        chatRoom1.setImage(chatViewImage);
                        chatRoom1.setControl(chatControl);
                        chatRooms.setSelection(0);
                    }
                });
            }
        }

        public void left(final JID jid) {
            addChatLine(new MUCSessionHistoryLeaveElement(jid, new Date()));

            if (isOwnJID(jid)) {
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        showExplanation(howTo);
                        if (chatRoom1 != null && !chatRoom1.isDisposed()) {
                            chatRoom1.dispose();
                        }

                        if (chatRooms != null && !chatRooms.isDisposed()) {
                            chatRooms.setSelection(0);
                        }
                    }
                });
            }
        }

        public void messageReceived(final JID jid, final String message) {
            addChatLine(new MUCSessionHistoryMessageReceptionElement(jid,
                new Date(), message));

            if (!isOwnJID(jid)) {
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        SoundPlayer.playSound(SoundManager.MESSAGE_RECEIVED);
                    }
                });
            } else {
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        SoundPlayer.playSound(SoundManager.MESSAGE_SENT);
                    }
                });
            }
        }

        public void stateChanged(final JID sender, final ChatState state) {
            log.debug("Received ChatState from " + sender + ": "
                + state.toString());

            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    if (mucManager.getMUCSession() != null) {
                        if (mucManager.getMUCSession().getForeignStatesCount(
                            ChatState.composing) > 0) {
                            chatRooms.getSelection().setImage(composingImage);
                        } else {
                            chatRooms.getSelection().setImage(chatViewImage);
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

            if (chatControl.getInputText().isEmpty()) {
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

        }
    };

    @Inject
    protected SarosSessionManager sessionManager;
    @Inject
    protected MUCManagerSingletonWrapperChatView mucManager;

    Color white = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

    CTabFolder chatRooms;
    ChatControl chatControl;

    protected CTabItem chatRoom1;

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

    public ChatRoomsComposite(Composite parent, int style) {
        super(parent, style);

        SarosPluginContext.initComponent(this);

        this.editorManager.addSharedEditorListener(sharedEditorListener);
        this.mucManager.addMUCManagerListener(mucManagerListener);

        this.setLayout(new FillLayout());

        this.chatRooms = new CTabFolder(this, SWT.BOTTOM);
        this.setContentControl(this.chatRooms);

        this.chatRooms.setSimple(true);
        this.chatRooms.setBorderVisible(true);

        this.chatControl = new ChatControl(this.chatRooms, SWT.BORDER, white,
            white, 2);
        this.chatControl.addChatControlListener(chatControlListener);

        /*
         * IMPORTANT: The user can open and close Views as he wishes. This means
         * that the live cycle of this ChatView is completely independent of the
         * global MUCSession. Therefore we need to correctly validate the
         * MUCSession's state when this ChatView is reopened.
         */
        if (this.joinedSession()) {
            this.attachToMUCSession(mucManager.getMUCSession());
            chatRoom1 = new CTabItem(chatRooms, SWT.NONE);
            chatRoom1.setText("Chatroom 1");
            chatRoom1.setImage(chatViewImage);
            chatRoom1.setControl(chatControl);
            chatRooms.setSelection(0);
            hideExplanation();
        } else {
            showExplanation(howTo);
        }

        // Show already received messages
        this.refreshFromHistory();
        chatRooms.setSelection(0);

        this.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {

                chatControl.removeChatControlListener(chatControlListener);
                if (mucManager.getMUCSession() != null) {
                    detachFromMUCSession(mucManager.getMUCSession());
                }
                mucManager.removeMUCManagerListener(mucManagerListener);
                editorManager.removeSharedEditorListener(sharedEditorListener);
            }
        });

    }

    public void attachToMUCSession(MUCSession mucSession) {
        mucSession.addMUCSessionListener(mucSessionListener);
    }

    public void detachFromMUCSession(MUCSession mucSession) {
        mucSession.removeMUCSessionListener(mucSessionListener);
    }

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
            colorCache.put(jid, SarosAnnotation.getUserColor(user));
        }
        final String sender = senderCache.get(jid);
        final Color color = colorCache.get(jid);

        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                if (chatControl == null || chatControl.isDisposed()
                    || sender == null || color == null)
                    return;
                log.debug("Sender: " + sender);
                log.debug("Color: " + color);
                chatControl.addChatLine(sender, color, message, receivedOn);
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

}
