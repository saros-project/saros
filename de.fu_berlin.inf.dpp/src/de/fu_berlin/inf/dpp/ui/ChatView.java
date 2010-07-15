package de.fu_berlin.inf.dpp.ui;

import java.awt.Toolkit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.part.ViewPart;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.MessagingManager;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.MessagingManager.IChatListener;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Saros' Chat View.
 * 
 * @author ologa
 * @author ahaferburg
 */
@Component(module = "ui")
public class ChatView extends ViewPart {

    private static Logger log = Logger.getLogger(ChatView.class);

    /** Text input field. */
    protected Text inputText;

    /** Displays the chat. */
    protected SourceViewer viewer;

    private static final int[] WEIGHTS = { 75, 25 };

    @Inject
    protected MessagingManager messagingManager;

    @Inject
    protected Saros saros;

    @Inject
    protected RosterTracker rosterTracker;

    @Inject
    protected SessionManager sessionManager;

    protected boolean sessionStarted = false;

    protected Map<JID, String> chatUsers = new HashMap<JID, String>();

    protected Action beepAction;

    protected IPreferenceStore prefStore;

    protected final String SELF_REFERENCE = "You";

    protected IRosterListener rosterListener = new IRosterListener() {
        public void update(final Collection<String> addresses) {
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
            update(addresses);
        }

        public void entriesDeleted(Collection<String> addresses) {
            // not needed
        }

        public void entriesAdded(Collection<String> addresses) {
            update(addresses);
        }

        public void rosterChanged(Roster roster) {
            // not needed
        }
    };

    protected IChatListener chatListener = new IChatListener() {
        /**
         * Print messages to TextWidget
         * 
         */
        public void chatMessageAdded(final String sender, final String message) {
            ChatView.log.debug("Received Message from " + sender + ": "
                + message);
            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    String prefix;

                    int prefixPos = sender.indexOf('/') + 1;
                    DateTime dt = new DateTime();
                    DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
                    String time = fmt.print(dt);
                    String senderAddress = sender.substring(prefixPos, sender
                        .length());
                    String humanReadableSender = getHumanReadableSender(senderAddress);
                    prefix = String.format("[%s (%s)]: ", humanReadableSender,
                        time);

                    String m = message.startsWith("\n") ? message.substring(1)
                        : message;
                    String message = m + "\n";
                    SourceViewer viewer2 = ChatView.this.viewer;
                    StyledText textWidget = viewer2.getTextWidget();
                    if (textWidget != null) {
                        // Make the prefix bold to separate individual messages
                        // better
                        StyleRange prefixStyle = new StyleRange();
                        prefixStyle.start = textWidget.getText().length();
                        prefixStyle.length = prefix.length() - 1;
                        prefixStyle.fontStyle = SWT.BOLD;

                        textWidget.append(prefix);
                        textWidget.append(message);

                        textWidget.setStyleRange(prefixStyle);
                    }

                    if (prefStore.getBoolean(PreferenceConstants.BEEP_UPON_IM)
                        && !humanReadableSender.equals(SELF_REFERENCE))
                        Toolkit.getDefaultToolkit().beep();
                }
            });
        }

        public void chatJoined() {
            if (!chatUsers.containsKey(saros.getMyJID()))
                chatUsers.put(saros.getMyJID(), SELF_REFERENCE);

            ISharedProject sharedProject = sessionManager.getSharedProject();
            if (sharedProject != null) {
                for (User user : sharedProject.getParticipants()) {
                    if (!user.isLocal())
                        updateHumanReadableName(user);
                }
            }
            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    inputText.setText("");
                    inputText.setEditable(true);
                }
            });
        }

        public void chatLeft() {
            Util.runSafeSWTAsync(log, new Runnable() {
                public void run() {
                    inputText.setText("You have left the chat. To re-enter "
                        + "the chat please join a Saros session.");
                    inputText.setEditable(false);
                }
            });
        }
    };

    public ChatView() {
        Saros.reinject(this);

        rosterTracker.addRosterListener(rosterListener);
        log.debug("RosterListener added!");
        log.debug("SessionListener added!");

        if (sessionManager.getSharedProject() == null) {
            log.debug("session started");
            if (!chatUsers.containsKey(saros.getMyJID()))
                chatUsers.put(saros.getMyJID(), SELF_REFERENCE);

        }
        this.prefStore = saros.getPreferenceStore();
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

        this.inputText = new Text(sash, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
            | SWT.WRAP);
        this.inputText.setText("To use the chat please "
            + "join a shared project session.");
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
                        text = text.trim();
                        ChatView.this.inputText.setText("");

                        if (!text.equals("")
                            && messagingManager.getSession() != null) {
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

        // Register the chat listener.
        // Run a possible join() in a separate thread to prevent the opening of
        // this view from blocking the SWT thread.
        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                messagingManager.addChatListener(chatListener);
            }
        });

        this.beepAction = new Action("Toggle beep") {

            @Override
            public void run() {
                boolean newBeepValue = prefStore
                    .getBoolean(PreferenceConstants.BEEP_UPON_IM) ? false
                    : true;

                prefStore.setValue(PreferenceConstants.BEEP_UPON_IM,
                    newBeepValue);

                if (newBeepValue == true)
                    this.setImageDescriptor(SarosUI
                        .getImageDescriptor("/icons/speaker_on.png"));
                else
                    this.setImageDescriptor(SarosUI
                        .getImageDescriptor("/icons/speaker_off.png"));

            }
        };

        if (prefStore.getBoolean(PreferenceConstants.BEEP_UPON_IM)) {
            this.beepAction.setImageDescriptor(SarosUI
                .getImageDescriptor("/icons/speaker_on.png"));
        } else {
            this.beepAction.setImageDescriptor(SarosUI
                .getImageDescriptor("/icons/speaker_off.png"));
        }

        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(this.beepAction);

    }

    @Override
    public void dispose() {
        rosterTracker.removeRosterListener(rosterListener);
        messagingManager.removeChatListener(chatListener);
        log.debug("RosterListener and IChatListener removed.");

        super.dispose();
    }

    @Override
    public void setFocus() {
        // not needed
    }

    /**
     * 
     * @param sender
     *            Sender JID as String
     * @return Nickname of the user or "You" if its the local user
     */
    protected String getHumanReadableSender(String sender) {

        JID senderJID = new JID(sender);
        if (chatUsers.containsKey(senderJID)) {
            return chatUsers.get(senderJID);
        } else if (sender.contains("/")) {
            return sender.substring(0, sender.indexOf('/', 0));
        } else {
            return sender;
        }
    }

    protected void updateHumanReadableName(User user) {
        JID jid = user.getJID();
        String nickName = Util.getNickname(saros, jid);
        if (nickName == null)
            nickName = user.getHumanReadableName();
        chatUsers.put(jid, nickName);

        log.debug("New ChatUser added / updated to ChatUser Collection: " + jid
            + " - " + nickName);
    }

}