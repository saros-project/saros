package de.fu_berlin.inf.dpp.ui;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
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
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;

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
	
	private static final int[] WEIGHTS = {75, 25};
	
	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());

		SashForm sash = new SashForm(rootComposite, SWT.VERTICAL);
		
		viewer = new SourceViewer(sash, null, null, true, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
		viewer.configure(new TextSourceViewerConfiguration(EditorsUI.getPreferenceStore()));
		viewer.setDocument(new Document());
		final StyledText chatText = viewer.getTextWidget();
		
		inputText = new Text(sash, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		inputText.setEditable(false);
		
		sash.setWeights(WEIGHTS);
		
		inputText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
					case SWT.CR :
					case SWT.KEYPAD_CR :
						if (e.stateMask == 0) {
							String text = inputText.getText();
							inputText.setText(""); //$NON-NLS-1$

								if (!text.equals("")) { //$NON-NLS-1$
									Saros.getDefault().getMessagingManager().getSession().sendMessage(text);
								}
								// append("ID-TODO", text);
						}
						break;
				}
			}
		});
		
		// TODO add disconnect-action
		 Action connectAction = new Action("Connect...") {
			public void run() {
				log.debug("MUC connecting");
				MessagingManager mm = Saros.getDefault().getMessagingManager();
				inputText.setEditable(true);
				try {
					mm.connectMultiUserChat();
				} catch (XMPPException e) {
					log.error("couldn't connect!");
				}
			}
		};
		connectAction.setToolTipText("Connecting to Chat");
		connectAction.setImageDescriptor(SarosUI.getImageDescriptor("/icons/connect.png"));

		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(connectAction);


		// register ChatView as chat listener
		MessagingManager mm = Saros.getDefault().getMessagingManager();
		mm.addChatListener(this);
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void invitationReceived(IIncomingInvitationProcess invitation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionEnded(ISharedProject session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionStarted(ISharedProject session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionStateChanged(XMPPConnection connection,
			ConnectionState newState) {
	}

	public void displayMessage(String body) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				// 
			}
		});
		
	}

	@Override
	public void chatMessageAdded(final String sender, final String message) {
		log.debug("Received Message from " + sender + ": " + message);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				int prefixPos = sender.indexOf('/')+1;
				viewer.getTextWidget().append(
						sender.substring(prefixPos,sender.indexOf('/', prefixPos))
						+ ": " + message.substring(1) + "\n");
			}
		});
		
	}

}


