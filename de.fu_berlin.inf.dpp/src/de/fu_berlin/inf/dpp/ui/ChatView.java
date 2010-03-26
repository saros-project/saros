package de.fu_berlin.inf.dpp.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.part.ViewPart;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.MessagingManager;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.MessagingManager.IChatListener;
import de.fu_berlin.inf.dpp.MessagingManager.MultiChatSession;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * ChatView
 * 
 * @author ologa
 * @author ?
 */
@Component(module = "ui")
public class ChatView extends ViewPart implements IConnectionListener,
    IChatListener {

    private static Logger log = Logger.getLogger(ChatView.class);

    protected Text inputText;

    protected MultiChatSession session;

    protected SourceViewer viewer;

    protected boolean joined = false;

    protected Action connectAction;

    private static final int[] WEIGHTS = { 75, 25 };

    @Inject
    protected MessagingManager messagingManager;

    @Inject
    protected Saros saros;

    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected RosterTracker rosterTracker;

    protected boolean sessionStarted = false;

    protected Map<JID, String> chatUsers = new HashMap<JID, String>();

    protected ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionEnded(ISharedProject session) {
            log.debug("Session ended! Chat closed!");
            sessionStarted = false;
            ChatView.this.session = null;
            connectionStateChanged(saros.getConnection(), saros
                .getConnectionState());

        }

        @Override
        public void sessionStarted(ISharedProject session) {

            log.debug("Session started!");
            sessionStarted = true;
            connectionStateChanged(saros.getConnection(), saros
                .getConnectionState());
            if (!chatUsers.containsKey(saros.getMyJID()))
                chatUsers.put(saros.getMyJID(), "You");

        }

    };

    protected class ChatSessionRosterListener implements IRosterListener {
        public void changed(final Collection<String> addresses) {
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {

                    ISharedProject sharedProject = sessionManager
                        .getSharedProject();
                    if (sharedProject == null)
                        return;

                    for (String address : addresses) {
                        JID jid = sharedProject
                            .getResourceQualifiedJID(new JID(address));
                        if (jid == null)
                            continue;

                        User user = sharedProject.getUser(jid);
                        if (user == null)
                            continue;

                        updateHumanReadableName(user);

                    }
                }
            });
        }

        public void presenceChanged(Presence presence) {
            // not needed
        }

        public void entriesUpdated(Collection<String> addresses) {
            changed(addresses);
        }

        public void entriesDeleted(Collection<String> addresses) {
            // not needed
        }

        public void entriesAdded(Collection<String> addresses) {
            changed(addresses);
        }

        public void rosterChanged(Roster roster) {
            // not needed
        }
    }

    public ChatView() {
        Saros.reinject(this);

        sessionManager.addSessionListener(sessionListener);
        IRosterListener rosterListener = new ChatSessionRosterListener();
        rosterTracker.addRosterListener(rosterListener);
        log.debug("RosterListener added!");
        log.debug("SessionListener added!");

        if (sessionManager.getSharedProject() == null) {
            sessionStarted = false;
            log.debug("no session started");
        } else {
            sessionStarted = true;
            log.debug("session started");
            if (!chatUsers.containsKey(saros.getMyJID()))
                chatUsers.put(saros.getMyJID(), "You");
        }

    }

    @Override
    public void createPartControl(Composite parent) {
        Composite rootComposite = new Composite(parent, SWT.NONE);
        rootComposite.setLayout(new FillLayout());

        SashForm sash = new SashForm(rootComposite, SWT.VERTICAL);

        this.viewer = new SourceViewer(sash, null, null, true, SWT.BORDER
            | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
        this.viewer.configure(new TextSourceViewerConfiguration(EditorsUI
            .getPreferenceStore()));
        this.viewer.setDocument(new Document());
        this.viewer.getTextWidget();

        this.inputText = new Text(sash, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        this.inputText
            .setText("To Join the chat please use the connect button. (You have to be in a shared project session)");
        this.inputText.setEditable(false);

        sash.setWeights(ChatView.WEIGHTS);

        this.inputText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.keyCode) {
                case SWT.CR:
                case SWT.KEYPAD_CR:
                    if (e.stateMask == 0) {
                        String text = ChatView.this.inputText.getText();
                        ChatView.this.inputText.setText("");

                        if (!text.equals("")) {
                            messagingManager.getSession().sendMessage(text);
                        }
                    }
                    break;
                }
            }
        });

        this.viewer.addTextListener(new ITextListener() {

            public void textChanged(TextEvent event) {

                // scrolls down, so the last line is visible
                int lines = ChatView.this.viewer.getDocument()
                    .getNumberOfLines();
                ChatView.this.viewer.setTopIndex(lines);
            }
        });

        this.connectAction = new Action("Connect/DisConnect") {

            @Override
            public void run() {

                if (ChatView.this.joined) {
                    ChatView.this.session.sendMessage("is leaving the chat...");
                    ChatView.this.inputText
                        .setText("You have left the chat. To re-enter the chat please use the connect button.");
                    messagingManager.disconnectMultiUserChat();
                    ChatView.this.session = null;
                    ChatView.this.inputText.setEditable(false);

                    ChatView.this.joined = false;
                    ChatView.this.connectAction.setImageDescriptor(SarosUI
                        .getImageDescriptor("/icons/disconnect.png"));
                } else {
                    try {
                        messagingManager.connectMultiUserChat();
                        ChatView.this.joined = true;
                        ChatView.this.viewer.setDocument(new Document());
                        ChatView.this.inputText.setEditable(true);
                        ChatView.this.inputText.setText("");
                        ChatView.this.connectAction.setImageDescriptor(SarosUI
                            .getImageDescriptor("/icons/connect.png"));
                        ChatView.this.session = messagingManager.getSession();
                        ChatView.this.session
                            .sendMessage("has joined the chat");
                    } catch (XMPPException e) {
                        ChatView.this.viewer.getDocument().set(
                            "Error: Couldn't connect - " + e);
                    }
                }
            }
        };

        if (this.joined) {
            this.connectAction.setImageDescriptor(SarosUI
                .getImageDescriptor("/icons/connect.png"));
        } else {
            this.connectAction.setImageDescriptor(SarosUI
                .getImageDescriptor("/icons/disconnect.png"));
        }

        if (saros.isConnected() && sessionStarted == true) {
            this.connectAction.setEnabled(true);
        } else {
            this.connectAction.setEnabled(false);
        }
        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(this.connectAction);

        // register ChatView as chat listener

        messagingManager.addChatListener(this);

        // register as connection listener
        saros.addListener(this);
    }

    @Override
    public void setFocus() {
        // not needed
    }

    /**
     * If connection or session closed, disable the connect / disconnect button
     */
    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState) {
        if (newState == ConnectionState.CONNECTED && sessionStarted == true) {
            this.connectAction.setEnabled(true);
        } else if (newState == ConnectionState.NOT_CONNECTED
            || sessionStarted == false
            || newState == ConnectionState.DISCONNECTING
            || newState == ConnectionState.CONNECTING) {
            log.debug("Session closed!");
            this.connectAction.setEnabled(false);
        }
    }

    /**
     * Print messages to TextWidget
     * 
     */
    public void chatMessageAdded(final String sender, final String message) {
        ChatView.log.debug("Received Message from " + sender + ": " + message);
        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                int prefixPos = sender.indexOf('/') + 1;
                String m = message.startsWith("\n") ? message.substring(1)
                    : message;
                DateTime dt = new DateTime();
                DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
                String time = fmt.print(dt);
                String prefix = "["
                    + getHumanReadableSender(sender.substring(prefixPos, sender
                        .length())) + " (" + time + ")]: ";
                String message = m + "\n";
                ChatView.this.viewer.getTextWidget().append(prefix);
                ChatView.this.viewer.getTextWidget().append(message);
            }
        });
    }

    /**
     * 
     * @param sender
     *            Sender JID as String
     * @return Nickname of the user or "You" if its the local user
     */
    public String getHumanReadableSender(String sender) {

        JID senderJID = new JID(sender);
        if (chatUsers.containsKey(senderJID)) {
            return chatUsers.get(senderJID);
        } else if (sender.contains("/")) {
            return sender.substring(0, sender.indexOf('/', 0));
        } else {
            return sender;
        }
    }

    public void updateHumanReadableName(User user) {

        chatUsers.remove(user.getJID());
        chatUsers.put(user.getJID(), user.getHumanReadableName());

        log.debug("New ChatUser added / updated to ChatUser Collection: "
            + user.getJID() + " - " + user.getHumanReadableName());
    }

}