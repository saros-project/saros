package de.fu_berlin.inf.dpp.test.jupiter.text.network;

import java.util.HashMap;

import de.fu_berlin.inf.dpp.jupiter.Request;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;

/**
 * this class simulate a network.
 * @author troll
 *
 */
public class SimulateNetzwork implements NetworkConnection{

	private HashMap<JID, NetworkEventHandler> clients;
	
	public SimulateNetzwork(){
		clients = new HashMap<JID, NetworkEventHandler>();
	}
	
	public void sendOperation(JID jid, Request req){
		if(clients.containsKey(jid)){
			clients.get(jid).receiveNetworkEvent(req);
		}
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
