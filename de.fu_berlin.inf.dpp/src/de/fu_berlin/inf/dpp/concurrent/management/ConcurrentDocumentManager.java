package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.concurrent.ConcurrentManager;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.RequestForwarder;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterTimestampFactory;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.RequestImpl;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.IActivitySequencer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityManager;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;

public class ConcurrentDocumentManager implements ConcurrentManager {

	private static Logger logger = Logger.getLogger(Logger.class);

	/** Jupiter server instance documents */
	private HashMap<IPath, JupiterDocumentServer> concurrentDocuments;

	/** current open editor at client side. */
	private HashMap<IPath, JupiterClient> clientDocs;

	private List<JID> drivers;

	private JID host;

	private JID myJID;

	private Side side;

	private RequestForwarder forwarder;

	private IActivitySequencer sequencer;

	public ConcurrentDocumentManager(Side side, User host, JID myJID) {

		if (side == Side.HOST_SIDE) {
			concurrentDocuments = new HashMap<IPath, JupiterDocumentServer>();
		}

		this.clientDocs = new HashMap<IPath, JupiterClient>();
		drivers = new Vector<JID>();
		this.side = side;
		this.host = host.getJid();
		this.myJID = myJID;
	}

	public void setActivitySequencer(IActivitySequencer sequencer) {
		this.sequencer = sequencer;
	}

	public void setRequestForwarder(RequestForwarder f) {
		this.forwarder = f;
	}

	public RequestForwarder getRequestForwarder() {
		return this.forwarder;
	}

	public void addDriver(JID jid) {
		drivers.add(jid);
	}

	public void removeDriver(JID jid) {
		drivers.remove(jid);
	}

	public List<JID> getDriver() {

		return drivers;
	}

	public boolean isDriver(JID jid) {
		return drivers.contains(jid);
	}

	/**
	 * 
	 */
	public IActivity activityCreated(IActivity activity) {

		// editorActivitiy(activity, true);

		editorActivitiy(activity);

		if (createdTextEditActivity(activity)) {
			/* handled by jupiter and is sended by request transmitting. */
			return null;
		}
		return activity;
	}

	/**
	 * handled closed editor activity to remove the local jupiter clients.
	 * 
	 * @param activity
	 */
	private void editorActivitiy(IActivity activity) {
		if (activity instanceof EditorActivity) {
			EditorActivity editor = (EditorActivity) activity;

			if (!isHostSide()) {
				if (editor.getType() == Type.Closed) {
					// TODO: closing jupiter client for this document.
				}
			}

		}
	}

	/**
	 * handles text edit activities with jupiter.
	 * 
	 * @param activity
	 * @return true if activity is transformed with jupiter.
	 */
	private boolean createdTextEditActivity(IActivity activity) {

		if (activity instanceof TextEditActivity) {
			TextEditActivity textEdit = (TextEditActivity) activity;
			// if (!isHostSide()) {
			/**
			 * lokal erzeugte operation beim client 1. Aufruf von
			 * generateRequest beim client. Änderungen wurden bereits im Editor
			 * geschrieben. 2. versenden der Änderungen an Server (später)
			 */
			JupiterClient jupClient = null;
			/* no jupiter client already exists for this editor text edit */
			if (!clientDocs.containsKey(textEdit.getEditor())) {
				jupClient = new JupiterDocumentClient(this.myJID,
						this.forwarder);
				jupClient.setEditor(textEdit.getEditor());
				clientDocs.put(textEdit.getEditor(), jupClient);
			}

			/* generate request. */
			jupClient = clientDocs.get(textEdit.getEditor());
			if (jupClient != null) {
				Operation op = getOperation(textEdit);
				/* sync with local jupiter client */
				Request req = jupClient.generateRequest(op);

				/* already set and forward inside of jup client. */
				// /* add appropriate Editor path. */
				// req.setEditorPath(textEdit.getEditor());
				// /* transmit request */
				// forwarder.forwardOutgoingRequest(req);
				return true;
			}
			// }
		}
		return false;
	}

	private void execTextEditActivity(Request request) {

		// if (!isHostSide()) {
		/**
		 * lokal erzeugte operation beim client 1. Aufruf von generateRequest
		 * beim client. Änderungen wurden bereits im Editor geschrieben. 2.
		 * versenden der Änderungen an Server (später)
		 */
		JupiterClient jupClient = null;
		/* no jupiter client already exists for this editor text edit */
		if (!clientDocs.containsKey(request.getEditorPath())) {
			jupClient = new JupiterDocumentClient(this.myJID, this.forwarder);
			jupClient.setEditor(request.getEditorPath());
			clientDocs.put(request.getEditorPath(), jupClient);
		}

		/* generate request. */
		jupClient = clientDocs.get(request.getEditorPath());
		if (jupClient != null) {
			/* operational transformation. */
			Operation op;
			try {
				op = jupClient.receiveRequest(request);
			} catch (TransformationException e) {
				logger.error("Error during transformation: ", e);
				return;
			}

			for (TextEditActivity textEdit : getTextEditActivity(op)) {
				textEdit.setEditor(request.getEditorPath());
				textEdit.setSource(request.getJID().toString());
				/* execute activity in activity sequencer. */
				sequencer.execTransformedActivity(textEdit);
			}
			// return textEdit;
		}
		// }
		// return null;
	}

	public IActivity exec(IActivity activity) {

		if (activity instanceof EditorActivity) {
			EditorActivity editorAc = (EditorActivity) activity;

			if (isHostSide()) {
				JID sourceJID = new JID(editorAc.getSource());

				/* if one driver activate a new editor. */
				if (drivers.contains(sourceJID)
						&& (editorAc.getType() == Type.Activated || editorAc
								.getType() == Type.Closed)) {
					/* start jupiter proxy for this driver. */
					if (concurrentDocuments.containsKey(editorAc.getPath())) {
						JupiterServer server = concurrentDocuments.get(editorAc
								.getPath());

						/* client has no proxy for this editor. */
						if (!server.isExist(sourceJID)) {
							if (editorAc.getType() == Type.Activated) {
								server.addProxyClient(sourceJID);
								/* update vector time for new proxy. */
								// TODO: stop serializer and after this update
								// vector time.
								server.updateVectorTime(myJID, sourceJID);
								// TODO: forward vector time method.

								/* get vector time of host for this editor path. */
								try {
									
									JupiterClient jupC = clientDocs.get(editorAc.getPath());
									if(jupC != null){
									Timestamp ts = jupC.getTimestamp();

									/* create update vector time request. */
									Request updateRequest = new RequestImpl(0,
											new JupiterVectorTime(ts
													.getComponents()[1], ts
													.getComponents()[0]),
											new TimestampOperation());
									updateRequest.setEditorPath(editorAc
											.getPath());
									updateRequest.setJID(sourceJID);

									forwarder
											.forwardOutgoingRequest(updateRequest);
									}
								} catch (Exception e) {

									logger
											.error("Error during get timestamp of host proxy for "
													+ editorAc.getPath(),e);
								}
							}
						} else {
							/* remove proxy for this jid. */
							if (editorAc.getType() == Type.Closed) {
								server.removeProxyClient(sourceJID);
							}
						}
					} else {
						/* create new jupiter proxy client. */
						if (editorAc.getType() == Type.Activated) {
							Request createRequest = new RequestImpl(0,
									new JupiterVectorTime(0, 0),
									new TimestampOperation());
							createRequest.setEditorPath(editorAc.getPath());
							createRequest.setJID(sourceJID);
							
							
						}
					}
				}
			}
		}

		if (activity instanceof TextEditActivity) {
			// check for jupiter client documents
			TextEditActivity text = (TextEditActivity) activity;
			if (clientDocs.containsKey(text.getEditor())) {
				/* activity have to be transformed with jupiter on this client. */
				return null;
			}
		}

		return activity;
	}

	public boolean isHostSide() {
		if (side == Side.HOST_SIDE) {
			return true;
		}
		return false;
	}

	public boolean isHost(JID jid) {
		if (jid.equals(host)) {
			return true;
		}
		return false;
	}

	public void setHost(JID host) {
		this.host = host;
	}

	/**
	 * convert TextEditActivity to Operation op
	 * 
	 * @param text
	 * @return
	 */
	public Operation getOperation(TextEditActivity text) {
		Operation op = null;
		// delete activity
		if (text.replace > 0 && text.text.length() == 0) {
			/* string placeholder in length of delete area. */
			String placeholder = "";
			for (int i = 0; i < text.replace; i++) {
				placeholder += 1;
			}
			op = new DeleteOperation(text.offset, placeholder);
		}
		// insert activity
		if (text.replace == 0 && text.text.length() > 0) {
			op = new InsertOperation(text.offset, text.text);
		}
		return op;
	}

	/**
	 * Convert Operation op to text edit activity. NoOperation will ignore.
	 * 
	 * @param op
	 *            incoming transformed operation.
	 * @return List with executable text edit activities.
	 */
	public List<TextEditActivity> getTextEditActivity(Operation op) {
		List<TextEditActivity> result = new Vector<TextEditActivity>();
		TextEditActivity textEdit = null;
		if (op instanceof DeleteOperation) {
			DeleteOperation del = (DeleteOperation) op;
			textEdit = new TextEditActivity(del.getPosition(), "", del
					.getTextLength());
			result.add(textEdit);
		}
		if (op instanceof InsertOperation) {
			InsertOperation ins = (InsertOperation) op;
			textEdit = new TextEditActivity(ins.getPosition(), ins.getText(), 0);
			result.add(textEdit);
		}
		if (op instanceof SplitOperation) {
			SplitOperation split = (SplitOperation) op;
			TextEditActivity op1 = getTextEditActivity(split.getFirst()).get(0);
			TextEditActivity op2 = getTextEditActivity(split.getSecond())
					.get(0);

			/*
			 * if operation one is delete operation the offset of second
			 * operation has to modified.
			 */
			if (op1.replace > 0 && op1.text.length() == 0) {
				op2 = new TextEditActivity(op2.offset - op1.replace, "",
						op2.replace);
			}
			result.add(op1);
			result.add(op2);
		}

		return result;
	}

	// private void editorActivitiy(IActivity activity, boolean local) {
	// if (!isHostSide() || local) {
	// if (activity instanceof EditorActivity) {
	// EditorActivity editor = (EditorActivity) activity;
	// /* if new editor opened */
	// if (editor.getType() == Type.Activated) {
	// /* no jupiter client exists for this editor */
	// if (!clientDocs.containsKey(editor.getPath())) {
	// // TODO: add Request forwarder
	// JupiterClient jupiter = new JupiterDocumentClient(
	// myJID, null);
	// jupiter.setEditor(editor.getPath());
	// /* add to current docs */
	// clientDocs.put(editor.getPath(), jupiter);
	// }
	// // send EditorActivity to project host.
	//
	// }
	// if (editor.getType() == Type.Closed) {
	// /* remove editor form jupiter concurrent mechanism. */
	// if (clientDocs.containsKey(editor.getPath())) {
	// clientDocs.remove(editor.getPath());
	// }
	// }
	// }
	// }
	// /* managing of jupiter server documents. */
	// if (isHostSide()) {
	// /* Editor activities. */
	// if (activity instanceof EditorActivity) {
	// EditorActivity editor = (EditorActivity) activity;
	// /* if new editor opened */
	// if (editor.getType() == Type.Activated) {
	// /* create new jupiter document server. */
	// if (!concurrentDocuments.containsKey(editor.getPath())) {
	// JupiterDocumentServer jup = new JupiterDocumentServer(
	// forwarder);
	// jup.setEditor(editor.getPath());
	//
	// /* create host proxy */
	// jup.addProxyClient(host.getJid());
	// /* create client proxy if remote activity. */
	// if (!local) {
	// jup.addProxyClient(new JID(editor.getSource()));
	// }
	// /* add to server list. */
	// concurrentDocuments.put(editor.getPath(), jup);
	//
	// /*
	// * create host jupiter client for local request
	// * handling.
	// */
	// if (!clientDocs.containsKey(editor.getPath())) {
	// JupiterClient jupiter = new JupiterDocumentClient(
	// myJID, null);
	// jupiter.setEditor(editor.getPath());
	// }
	//
	// }
	// }
	// /* if document closed. */
	// if (editor.getType() == Type.Closed) {
	// if (!local) {
	// /* remove remote client from proxy list. */
	// JupiterDocumentServer serverDoc = concurrentDocuments
	// .get(editor.getPath());
	// if (serverDoc != null) {
	// /* remove remote client. */
	// serverDoc.removeProxyClient(new JID(editor
	// .getSource()));
	// /* TODO: if only host is exists. */
	// // if(serverDoc.getProxies().size() == 1){
	// //
	// // }
	// }
	// }
	// }
	// }
	// }
	// }

	/*
	 * 1. hinzufügen und löschen von jupiter servern 2. list mit transmitter
	 * threads, die Nachrichten aus den outgoing queues versenden. 3.
	 * Schnittstelle vom Itransmitter zu den einzelnen jupiter document servern,
	 * um die Nachrichten vom Itransmitter weiterzuleiten.
	 * 
	 * 
	 */

	/**
	 * sync received request with right jupiter server document and local
	 * client.
	 * 
	 */
	public void receiveRequest(Request request) {

		/* 1. Sync with jupiter server component. */
		if (isHostSide()) {

			/* if host side and server jupiter side of request */
			if (isHost(request.getJID()) && request.getSiteId() == 0) {
				/* request already has transformed and have to be execute. */
				// return execTextEditActivity(request);
				execTextEditActivity(request);
				return;
			}

			JupiterDocumentServer docServer = null;
			/**
			 * if no jupiter document server exists.
			 */
			if (!concurrentDocuments.containsKey(request.getEditorPath())) {
				/* create new document server. */
				docServer = new JupiterDocumentServer(forwarder);
				// docServer = new JupiterDocumentServer();
				docServer.setEditor(request.getEditorPath());
				/* create new local host document client. */
				docServer.addProxyClient(host);
				if (!isHost(request.getJID())) {
					docServer.addProxyClient(request.getJID());
				}
				concurrentDocuments.put(request.getEditorPath(), docServer);
			}
			docServer = concurrentDocuments.get(request.getEditorPath());
			try {
				/* check if sender id exists in proxy list. */
				if (!docServer.getProxies().containsKey(request.getJID())) {
					docServer.addProxyClient(request.getJID());
				}
			} catch (InterruptedException ie) {
				logger.error("Error during get proxy list of jupiter server.",
						ie);
			}

			/* sync request with jupiter document server. */
			docServer.addRequest(request);

			return;
		} else {
			/* update timestamp of local jupiter client. */
			if (request.getOperation() instanceof TimestampOperation) {
				if (clientDocs.containsKey(request.getEditorPath())) {
					logger.info("update vector time : "
							+ request.getEditorPath());
					JupiterClient jupClient = clientDocs.get(request
							.getEditorPath());
					try {
						jupClient.updateVectorTime(request.getTimestamp());
					} catch (TransformationException e) {
						logger.error("Error during update jupiter client for "
								+ request.getEditorPath(), e);
					}
				}
				else{
					/* if no jupiter client exists.*/
					JupiterClient client = new JupiterDocumentClient(this.myJID, this.forwarder);
					client.setEditor(request.getEditorPath());
					try {
						client.updateVectorTime(request.getTimestamp());
						clientDocs.put(request.getEditorPath(), client);
					} catch (TransformationException e) {
						logger.error("Error during update jupiter client for "
								+ request.getEditorPath(), e);
					}
					
				}
			}
			else{

			/*
			 * 2. receive request in local client component and return the
			 * transformed operation as IActivity.
			 */
			execTextEditActivity(request);
			// return execTextEditActivity(request);
			}
		}
	}

	public void driverChanged(JID driver, boolean replicated) {
		/*
		 * 1. check if driver exists. 2. add new driver or remove driver. 3.
		 */
		// HOST
		if (isHostSide()) {
			/* if driver changed to observer */
			if (drivers.contains(driver)) {
				userLeft(driver);
			}
			/* new driver added to project. */
			else {
				drivers.add(driver);
				//TODO: add driver to current open document proxy ?
			}
		}
		// CLIENT
		else {
			if (driver.equals(myJID)) {
				clientDocs.clear();
			}
		}

	}

	public void userJoined(JID user) {
		// do nothing

	}

	public void userLeft(JID user) {
		if (isHostSide()) {
			/* remove user from driver list */
			drivers.remove(user);

			/* remove user proxies from jupiter server. */
			for (JupiterServer server : concurrentDocuments.values()) {
				if (server.isExist(user)) {
					server.removeProxyClient(user);

					/* if only host has an proxy */

				}
			}
		}
	}

}
