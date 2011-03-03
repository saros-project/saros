package de.fu_berlin.inf.dpp.whiteboard.net;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.whiteboard.gef.model.GEFRecordFactory;
import de.fu_berlin.inf.dpp.whiteboard.sxe.ISXEMessageHandler;
import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;
import de.fu_berlin.inf.dpp.whiteboard.sxe.net.SXEOutgoingSynchronizationProcess;

/**
 * This class makes the interconnection between Saros and SXE.
 * 
 * The singleton instantiated on plug-in StartUp (see plugin.xml).
 * 
 * @author jurke
 * 
 */
/*
 * Note: this class is not yet final because a lot of changes are about to
 * happen respective the Saros invitation or network layer
 */
public class WhiteboardManager {

	private static Logger log = Logger.getLogger(WhiteboardManager.class);

	protected ISarosSession sarosSession;

	protected SXEController controller;

	private static WhiteboardManager instance = new WhiteboardManager();

	public static WhiteboardManager getInstance() {
		return instance;
	}

	/**
	 * The session listener registers the ISharedProjectListener, let's the host
	 * initialize a local session and enables for other peers to be invited.
	 */
	protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
		@Override
		public void sessionStarted(ISarosSession session) {
			log.debug("Whiteboard sssion started");
			sarosSession = session;
			sarosSession.addListener(sharedProjectListener);

			sxeTransmitter = new SarosSXETransmitter(sarosSession);
			controller.initNetwork(sxeTransmitter);

			if (sarosSession.isHost()) {
				controller.startSession();
			} else {
				sxeTransmitter.enableInvitation(controller);
			}

		}

		@Override
		public void sessionEnded(ISarosSession project) {
			sarosSession.removeListener(sharedProjectListener);
			controller.setDisconnected();

			/*
			 * dispose because we do not want to be invited when not in a Saros
			 * session and the transmitter will be recreated on start
			 */
			sxeTransmitter.dispose();
		}
	};

	protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

		/*
		 * called after receiving user list (outgoing) or in UserListHandler
		 * createArchive(), executed by the Job of the
		 * OutgoingInvitationProcess, perfect moment to start synchronization
		 */
		@Override
		public void userJoined(User user) {

			if (sarosSession.isHost()) {
				SXEOutgoingSynchronizationProcess inv = new SXEOutgoingSynchronizationProcess(
						controller, sxeTransmitter, user.getJID().toString());
				inv.start(SubMonitor.convert(new NullProgressMonitor()));
			}
		}

	};

	@Inject
	private SarosSessionManager sessionManager;

	private SarosSXETransmitter sxeTransmitter;

	private WhiteboardManager() {

		SarosPluginContext.reinject(this);

		sessionManager.addSarosSessionListener(sessionListener);

		log.debug("WhiteboardManager instantiated");

		controller = new SXEController(new GEFRecordFactory());
	}

	public ISXEMessageHandler getSXEMessageHandler() {
		return controller;
	}

	public void dispose() {
		controller.clear();
		if (sxeTransmitter != null)
			sxeTransmitter.dispose();
	}

}
