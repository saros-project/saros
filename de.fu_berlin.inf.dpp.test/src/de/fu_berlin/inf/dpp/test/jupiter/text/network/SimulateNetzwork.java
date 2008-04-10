package de.fu_berlin.inf.dpp.test.jupiter.text.network;

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.jupiter.Request;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.NetworkRequest;

/**
 * this class simulate a network.
 * @author troll
 *
 */
public class SimulateNetzwork implements NetworkConnection{

	private static Logger logger = Logger.getLogger(SimulateNetzwork.class);
	
	private HashMap<JID, NetworkEventHandler> clients;
	
	
	
	public SimulateNetzwork(){
		clients = new HashMap<JID, NetworkEventHandler>();
		
	}
	
	
	
	
	private void sendOperation(NetworkRequest req){
		if(clients.containsKey(req.getTo())){
			logger.debug("send message to "+req.getTo());
			clients.get(req.getTo()).receiveNetworkEvent(req);
		}
	}
	
	public void sendOperation(final NetworkRequest req, final int delay){
		new Thread(new Runnable(){
			public void run() {
				logger.debug("Delay in send operation "+req.getRequest().getOperation().toString()+" of "+delay+" millis");
				try {
				Thread.sleep(delay);
				sendOperation(req);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}}).start();
	}
	
	private void sendOperation(JID jid, Request req){
		if(clients.containsKey(jid)){
			clients.get(jid).receiveNetworkEvent(req);
		}
	}
	
	@Deprecated
	public void sendOperation(final JID jid, final Request req, final int delay) {
		
			new Thread(new Runnable(){
				public void run() {
					logger.debug("Delay in send operation "+req.getOperation().toString()+" of "+delay+" millis to "+jid);
					try {
					Thread.sleep(delay);
					sendOperation(jid, req);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}}).start();
	
	}
	
	public void addClient(NetworkEventHandler remote){
		if(!clients.containsKey(remote.getJID())){
			clients.put(remote.getJID(), remote);
		}
	}
	
	public void removeClient(NetworkEventHandler remote){
		clients.remove(remote.getJID());
	}



}
