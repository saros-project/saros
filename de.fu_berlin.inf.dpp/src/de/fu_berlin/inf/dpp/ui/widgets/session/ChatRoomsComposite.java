package de.fu_berlin.inf.dpp.ui.widgets.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.communication.chat.IChat;
import de.fu_berlin.inf.dpp.communication.chat.IChatServiceListener;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChat;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatService;
import de.fu_berlin.inf.dpp.communication.chat.single.SingleUserChatService;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.ChatControl;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.nebula.explanation.ListExplanationComposite.ListExplanation;
import de.fu_berlin.inf.nebula.explanation.explanatory.ListExplanatoryComposite;

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

    protected boolean isSessionRunning;

    protected RosterTracker rosterTracker;

    protected IChat sessionChat;

    protected CTabItem sessionChatErrorTab;

    protected Object mucCreationLock = new Object();

    /**
     * This RosterListener closure is added to the RosterTracker to get
     * notifications when the roster changes.
     */
    protected IRosterListener rosterListener = new IRosterListener() {

        /**
         * This method is mainly called, if the user name is changed, rebuild
         * Chat with uptodate nicknames from history
         */
        public void entriesUpdated(final Collection<String> addresses) {
            Utils.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    log.info("roster entries changed, refreshing chat tabs");

                    Collection<JID> jids = new ArrayList<JID>();
                    for (String address : addresses) {
                        jids.add(new JID(address));
                    }

                    updateChatTabs(jids);
                }
            });
        }

        public void entriesDeleted(Collection<String> addresses) {
            // do nothing
        }

        public void presenceChanged(Presence presence) {
            // do nothing
        }

        public void rosterChanged(Roster roster) {
            // do nothing
        }

        public void entriesAdded(Collection<String> addresses) {
            // do nothing
        }

    };

    /*
     * TODO: see
     * https://sourceforge.net/tracker/?func=detail&aid=3102858&group_id
     * =167540&atid=843362
     */
    @Inject
    protected EditorManager editorManager;

    protected AbstractSharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        @Override
        public void colorChanged() {
            Utils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {
                    for (CTabItem tab : chatRooms.getItems()) {
                        Control control = tab.getControl();
                        if (control instanceof ChatControl)
                            ((ChatControl) control).updateColors();
                    }
                }
            });
        }
    };

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarting(final ISarosSession session) {

            Utils.runSafeSWTSync(log, new Runnable() {
                @Override
                public void run() {
                    isSessionRunning = true;
                }
            });

            final CountDownLatch mucCreationStart = new CountDownLatch(1);

            /*
             * FIXME: the clients should be notified after the chat has been
             * created to join that chat. For slow connections it is possible
             * that ALICE invites BOB, but BOB creates the chat room first. If
             * now CARL joins the session and BOB leaves afterwards, ALICE and
             * CARLs MUC is broken !
             */
            Utils.runSafeAsync(log, new Runnable() {
                @Override
                public void run() {
                    synchronized (mucCreationLock) {
                        mucCreationStart.countDown();
                        multiUserChatService.createChat(session);
                    }
                }
            });

            try {
                mucCreationStart.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void sessionEnded(final ISarosSession session) {
            Utils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {

                    if (sessionChat != null)
                        multiUserChatService.destroyChat(sessionChat);

                    isSessionRunning = false;

                    sessionChat = null;

                    if (ChatRoomsComposite.this.isDisposed())
                        return;

                    if (sessionChatErrorTab != null
                        && !sessionChatErrorTab.isDisposed())
                        sessionChatErrorTab.dispose();

                }
            });
        }
    };

    @Inject
    protected ISarosSessionManager sessionManager;

    static final Color WHITE = Display.getDefault().getSystemColor(
        SWT.COLOR_WHITE);

    protected CTabFolder chatRooms;

    @Inject
    protected MultiUserChatService multiUserChatService;

    @Inject
    protected SingleUserChatService singleUserChatService;

    protected DisposeListener disposeListener = new DisposeListener() {

        @Override
        public void widgetDisposed(DisposeEvent e) {
            CTabItem source = (CTabItem) e.getSource();
            source.getControl().dispose();

            if (chatRooms.getItemCount() == 0) {
                showExplanation(howTo);
            }
        }

    };

    protected IChatServiceListener chatServiceListener = new IChatServiceListener() {

        @Override
        public void chatCreated(final IChat chat, boolean createdLocally) {
            Utils.runSafeSWTAsync(log, new Runnable() {
                @Override
                // FIXME: disposed ?!
                public void run() {
                    boolean isSessionChat = false;

                    if (chat instanceof MultiUserChat) {

                        /*
                         * creation of the session chat is done async., so it is
                         * possible to receive multiple requests from different
                         * Saros sessions
                         */
                        if (sessionChat != null)
                            multiUserChatService.destroyChat(sessionChat);

                        sessionChat = chat;
                        isSessionChat = true;
                    }

                    /*
                     * creation of the session chat is done async., so the
                     * session may have already ended
                     */
                    if (!isSessionRunning && isSessionChat) {
                        multiUserChatService.destroyChat(sessionChat);
                        sessionChat = null;
                    } else {
                        openChat(chat, false);
                    }
                }
            });
        }

        @Override
        public void chatDestroyed(final IChat chat) {
            Utils.runSafeSWTAsync(log, new Runnable() {

                @Override
                public void run() {
                    if (ChatRoomsComposite.this.isDisposed())
                        return;

                    closeChatTab(chat);
                }
            });
        }

        @Override
        public void chatAborted(IChat chat, XMPPException exception) {
            final String errorMessage = "The connection to the chat "
                + chat.getTitle() + " has been reset.";

            Utils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {
                    setErrorMessage(errorMessage);
                }
            });
        }
    };

    public ChatRoomsComposite(Composite parent, int style,
        final RosterTracker rosterTracker) {
        super(parent, style);

        this.rosterTracker = rosterTracker;
        rosterTracker.addRosterListener(rosterListener);

        SarosPluginContext.initComponent(this);

        this.sessionManager.addSarosSessionListener(sessionListener);
        this.editorManager.addSharedEditorListener(sharedEditorListener);
        this.singleUserChatService.addChatServiceListener(chatServiceListener);
        this.multiUserChatService.addChatServiceListener(chatServiceListener);

        this.setLayout(new FillLayout());

        this.chatRooms = new CTabFolder(this, SWT.BOTTOM);
        this.setContentControl(this.chatRooms);

        this.chatRooms.setSimple(true);
        this.chatRooms.setBorderVisible(true);

        /*
         * TODO: The user can open and close Views as he wishes. This means that
         * the live cycle of this ChatView is completely independent of the
         * global MultiUserChat. Therefore we need to correctly validate the
         * MultiUserChat's state when this ChatView is reopened.
         */

        isSessionRunning = sessionManager.getSarosSession() != null;

        showExplanation(howTo);

        this.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                sessionManager.removeSarosSessionListener(sessionListener);

                editorManager.removeSharedEditorListener(sharedEditorListener);

                singleUserChatService
                    .removeChatServiceListener(chatServiceListener);
                multiUserChatService
                    .removeChatServiceListener(chatServiceListener);
                /**
                 * This must be called before finalization otherwise you will
                 * get NPE on RosterTracker.
                 */
                rosterTracker.removeRosterListener(rosterListener);
            }
        });

    }

    /**
     * Create a new single user chat with the given JID and open it.
     * 
     * @param jid
     * @param activateAfterCreation
     *            see {@link ChatRoomsComposite#openChat(IChat, boolean)} *
     */
    public void openChat(JID jid, boolean activateAfterCreation) {
        openChat(singleUserChatService.createChat(jid), activateAfterCreation);
    }

    /**
     * Open the tab for a given chat.
     * 
     * If the the corresponding tab already exists, it will be activated,
     * otherwise a new tab will be created.
     * 
     * @param chat
     *            The chat that should be displayed. If no corresponding chat
     *            tab exists, a new one will be created.
     * @param activateAfterCreation
     *            If a new tab is created, setting this parameter
     *            <code>false</code> will open the tab in background,
     *            <code>true</code> will activate it. If the newly created chat
     *            tab is the only one, it will of course be active anyway. If
     *            the chat tab already exists, this parameter has no effect: the
     *            tab will be activated anyway.
     */
    public void openChat(IChat chat, boolean activateAfterCreation) {
        if (selectExistentTab(chat)) {
            return;
        }

        hideExplanation();

        CTabItem chatTab = createChatTab(chat);
        if (activateAfterCreation || chatRooms.getItemCount() == 1) {
            chatRooms.setSelection(chatTab);
        }
    }

    private CTabItem createChatTab(IChat chat) {
        ChatControl control = new ChatControl(this, chat, chatRooms,
            SWT.BORDER, WHITE, 2);

        CTabItem chatTab;

        if (chat == sessionChat)
            chatTab = new CTabItem(chatRooms, SWT.NONE, 0);
        else
            chatTab = new CTabItem(chatRooms, SWT.CLOSE);

        chatTab.setText(chat.getTitle());
        chatTab.setImage(chatViewImage);
        chatTab.setData(chat);
        chatTab.setControl(control);
        chatTab.addDisposeListener(disposeListener);

        return chatTab;
    }

    public CTabItem getChatTab(IChat chat) {
        for (CTabItem tab : this.chatRooms.getItems()) {
            IChat data = (IChat) tab.getData();

            // do the equal check this way to allow null values in the tab data
            if (chat.equals(data))
                return tab;
        }

        return null;
    }

    private boolean closeChatTab(IChat chat) {
        CTabItem tab = getChatTab(chat);
        if (tab != null && !tab.isDisposed()) {
            tab.dispose();

            if (chatRooms.getItemCount() == 0) {
                showExplanation(howTo);
            }

            return true;
        }

        return false;
    }

    /**
     * Update title and history for chats. A chat is updated if its participants
     * contains any of the given {@link JID}s.
     * 
     * @param jids
     *            JIDs whom the chats should be updated for
     */
    private void updateChatTabs(Collection<JID> jids) {
        if (isDisposed()) {
            return;
        }

        for (CTabItem tab : chatRooms.getItems()) {
            IChat chat = (IChat) tab.getData();
            ChatControl control = (ChatControl) tab.getControl();

            if (!Collections.disjoint(jids, chat.getParticipants())) {
                control.updateDisplayNames();
                tab.setText(chat.getTitle());
            }
        }
    }

    private boolean selectExistentTab(IChat chat) {
        for (CTabItem item : chatRooms.getItems()) {
            if (item.getData().equals(chat)) {
                chatRooms.setSelection(item);
                return true;
            }
        }

        return false;
    }

    public ChatControl getSelectedChatControl() {
        if (!isChatExistent()) {
            return null;
        }

        return (ChatControl) chatRooms.getSelection().getControl();
    }

    public boolean isChatExistent() {
        return chatRooms.getSelection() != null;
    }

    private void setErrorMessage(String message) {

        if (!isSessionRunning || message == null)
            return;

        if (sessionChatErrorTab != null && !sessionChatErrorTab.isDisposed())
            sessionChatErrorTab.dispose();

        ListExplanatoryComposite errorComposite = new ListExplanatoryComposite(
            chatRooms, SWT.NONE);

        ListExplanation explanation = new ListExplanation(SWT.ICON_ERROR,
            message);

        errorComposite.showExplanation(explanation);

        sessionChatErrorTab = new CTabItem(chatRooms, SWT.CLOSE, 0);

        sessionChatErrorTab.setText("Session Chat Error");

        // TODO add an icon

        sessionChatErrorTab.setData(null);
        sessionChatErrorTab.setControl(errorComposite);
        sessionChatErrorTab.addDisposeListener(disposeListener);
        chatRooms.setSelection(sessionChatErrorTab);

        hideExplanation();

    }
}
