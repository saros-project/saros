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
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityManager;
import de.fu_berlin.inf.dpp.project.IActivityProvider;

public class ConcurrentDocumentManager implements ConcurrentManager {

	private static Logger logger = Logger.getLogger(Logger.class);

	/** Jupiter server instance documents */
	private HashMap<IPath, JupiterDocumentServer> concurrentDocuments;

	/** current open editor at client side. */
	private HashMap<IPath, JupiterClient> clientDocs;

	private List<User> drivers;

	private User host;

	private JID myJID;

	private Side side;

	public ConcurrentDocumentManager(Side side, User host, JID myJID) {

		if (side == Side.HOST_SIDE) {
			concurrentDocuments = new HashMap<IPath, JupiterDocumentServer>();
		}

		this.clientDocs = new HashMap<IPath, JupiterClient>();
		drivers = new Vector<User>();
		this.side = side;
		this.host = host;
		this.myJID = myJID;
	}

	public void addDriver(User jid) {
		drivers.add(jid);

	}

	public void removeDriver(User jid) {
		drivers.remove(jid);
	}

	public List<User> getDriver() {

		return drivers;
	}

	public boolean isDriver(User jid) {
		return drivers.contains(jid);
	}

	public IActivity activityCreated(IActivity activity) {
//		/* create appropriate jupiter document clients. */
//		activityCreatedOnClient(activity);
//
//		if (side == Side.HOST_SIDE) {
//			activityCreatedOnHost(activity);
//		}

		editorActivitiy(activity, true);
		
		return activity;
	}

//	private void activityCreatedOnClient(IActivity activity) {
//		if (activity instanceof EditorActivity) {
//			/* create and closed local jupiter clients. */
//			editorActivitiy(activity, true);
//		}
//
//		if (activity instanceof TextEditActivity) {
//
//		}
//	}

	private void editorActivitiy(IActivity activity, boolean local) {
		if (!isHost() || local) {
			if (activity instanceof EditorActivity) {
				EditorActivity editor = (EditorActivity) activity;
				/* if new editor opened */
				if (editor.getType() == Type.Activated) {
					/* no jupiter client exists for this editor */
					if (!clientDocs.containsKey(editor.getPath())) {
						// TODO: add Request forwarder
						JupiterClient jupiter = new JupiterDocumentClient(
								myJID, null);
						jupiter.setEditor(editor.getPath());
						/* add to current docs */
						clientDocs.put(editor.getPath(), jupiter);
					}
					//send EditorActivity to project host.
					
					
				}
				if (editor.getType() == Type.Closed) {
					/* remove editor form jupiter concurrent mechanism. */
					if (clientDocs.containsKey(editor.getPath())) {
						clientDocs.remove(editor.getPath());
					}
				}
			}
		}
		/* managing of jupiter server documents. */
		if(isHost()){
			/* Editor activities. */
			if (activity instanceof EditorActivity) {
				EditorActivity editor = (EditorActivity) activity;
				/* if new editor opened */
				if (editor.getType() == Type.Activated) {
					/* create new jupiter document server. */
					if (!concurrentDocuments.containsKey(editor.getPath())) {
						JupiterDocumentServer jup = new JupiterDocumentServer();
						jup.setEditor(editor.getPath());

						/* create host proxy */
						jup.addProxyClient(host.getJid());
						/* create client proxy if remote activity. */
						if(!local){
							jup.addProxyClient(new JID(editor.getSource()));
						}
						/* add to server list. */
						concurrentDocuments.put(editor.getPath(), jup);

						/* create host jupiter client for local request handling. */
						if (!clientDocs.containsKey(editor.getPath())) {
							JupiterClient jupiter = new JupiterDocumentClient(
									myJID, null);
							jupiter.setEditor(editor.getPath());
						}

					}
				}
				/* if document closed. */
				if (editor.getType() == Type.Closed) {
					if(!local){
						/* remove remote client from proxy list. */
						JupiterDocumentServer serverDoc =  concurrentDocuments.get(editor.getPath());
						if(serverDoc != null){
							/* remove remote client. */
							serverDoc.removeProxyClient(new JID(editor.getSource()));
							/* TODO: if only host is exists.*/
//							if(serverDoc.getProxies().size() == 1){
//								
//							}
						}
					}
				}
			}
		}
	}

	private void activityCreatedOnHost(IActivity activity) {
		/* Editor activities. */
		if (activity instanceof EditorActivity) {
			EditorActivity editor = (EditorActivity) activity;
			/* if new editor opened */
			if (editor.getType() == Type.Activated) {
				/* create new jupiter document server. */
				if (!concurrentDocuments.containsKey(editor.getPath())) {
					JupiterDocumentServer jup = new JupiterDocumentServer();
					jup.setEditor(editor.getPath());

					/* create host proxy */
					jup.addProxyClient(host.getJid());
					/* create client proxy. */
					jup.addProxyClient(new JID(editor.getSource()));
					/* add to server list. */
					concurrentDocuments.put(editor.getPath(), jup);

					/* create host jupiter client for local request handling. */
					if (!clientDocs.containsKey(editor.getPath())) {
						JupiterClient jupiter = new JupiterDocumentClient(
								myJID, null);
						jupiter.setEditor(editor.getPath());
					}

				}
			}
			/* if document closed. */
			if (editor.getType() == Type.Closed) {

			}
		}
		if (activity instanceof TextEditActivity) {
			TextEditActivity text = (TextEditActivity) activity;

		}
	}

	public IActivity exec(IActivity activity) {

		editorActivitiy(activity, false);
		
		
		return activity;
	}

	public boolean isHost() {
		if (side == Side.HOST_SIDE) {
			return true;
		}
		return false;
	}

	public void setHost(User host) {
		this.host = host;
	}

	/*
	 * 1. hinzufügen und löschen von jupiter servern 2. list mit transmitter
	 * threads, die Nachrichten aus den outgoing queues versenden. 3.
	 * Schnittstelle vom Itransmitter zu den einzelnen jupiter document servern,
	 * um die Nachrichten vom Itransmitter weiterzuleiten.
	 * 
	 * 
	 */
}
