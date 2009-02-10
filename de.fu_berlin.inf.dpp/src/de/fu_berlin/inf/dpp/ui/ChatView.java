package de.fu_berlin.inf.dpp.ui;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.part.ViewPart;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.MessagingManager;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.MessagingManager.IChatListener;
import de.fu_berlin.inf.dpp.MessagingManager.MultiChatSession;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;

public class ChatView extends ViewPart implements ISessionListener,
    IConnectionListener, IChatListener {

    private static Logger log = Logger.getLogger(ChatView.class.getName());

    private Text inputText;

    private MultiChatSession session;

    private SourceViewer viewer;

    private boolean joined = false;

    private Action connectAction;

    private static final int[] WEIGHTS = { 75, 25 };

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
            .setText("To Join the chat please use the connect button.");
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
                        ChatView.this.inputText.setText(""); //$NON-NLS-1$

                        if (!text.equals("")) { //$NON-NLS-1$
                            Saros.getDefault().getMessagingManager()
                                .getSession().sendMessage(text);
                        }
                        /* append("ID-TODO", text); */
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
                MessagingManager mm = Saros.getDefault().getMessagingManager();
                Saros.getDefault().getConnection().getUser();
                if (ChatView.this.joined) {
                    try {
                        ChatView.this.session
                            .sendMessage("is leaving the chat...");
                        ChatView.this.inputText
                            .setText("You have left the chat. To re-enter the chat please use the connect button.");
                        mm.disconnectMultiUserChat();
                        ChatView.this.session = null;
                        ChatView.this.inputText.setEditable(false);

                    } catch (XMPPException e) {
                        ChatView.this.viewer.getDocument().set(
                            "Error: Couldn't disconnect");
                    }
                    ChatView.this.joined = false;
                    ChatView.this.connectAction.setImageDescriptor(SarosUI
                        .getImageDescriptor("/icons/disconnect.png"));
                } else {
                    try {
                        mm.connectMultiUserChat();
                        ChatView.this.joined = true;
                        ChatView.this.viewer.setDocument(new Document());
                        ChatView.this.inputText.setEditable(true);
                        ChatView.this.inputText.setText("");
                        ChatView.this.connectAction.setImageDescriptor(SarosUI
                            .getImageDescriptor("/icons/connect.png"));
                        ChatView.this.session = Saros.getDefault()
                            .getMessagingManager().getSession();
                        ChatView.this.session
                            .sendMessage("has joined the chat");
                    } catch (XMPPException e) {
                        ChatView.this.viewer.getDocument().set(
                            "Error: Couldn't connect");
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

        if (Saros.getDefault().isConnected()) {
            this.connectAction.setEnabled(true);
        } else {
            this.connectAction.setEnabled(false);
        }
        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(this.connectAction);

        // register ChatView as chat listener
        MessagingManager mm = Saros.getDefault().getMessagingManager();
        mm.addChatListener(this);

        // register as connection listener
        Saros.getDefault().addListener(this);
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    public void invitationReceived(IIncomingInvitationProcess invitation) {
        // TODO Auto-generated method stub

    }

    public void sessionEnded(ISharedProject session) {
        // TODO Auto-generated method stub

    }

    public void sessionStarted(ISharedProject session) {
        // TODO Auto-generated method stub

    }

    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState) {
        if (newState == ConnectionState.CONNECTED) {
            this.connectAction.setEnabled(true);
        } else if (newState == ConnectionState.NOT_CONNECTED) {
            // TODO do a little bit more here...
            this.connectAction.setEnabled(false);
        }
    }

    public void chatMessageAdded(final String sender, final String message) {
        ChatView.log.debug("Received Message from " + sender + ": " + message);
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                int prefixPos = sender.indexOf('/') + 1;
                String m = message.startsWith("\n") ? message.substring(1)
                    : message;
                ChatView.this.viewer.getTextWidget().append(
                    sender.substring(prefixPos, sender.indexOf('/', prefixPos))
                        + ": " + m + "\n");
            }
        });
    }

}