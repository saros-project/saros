package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterClient;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterServer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.OperationSerializer;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.SynchronizedQueue;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.net.JID;

public class Serializer extends Thread implements OperationSerializer{

	private static Logger logger = Logger.getLogger(Serializer.class);
	
	JupiterServer server;
	
	private boolean run = true;
	
	public Serializer(JupiterServer server){
		this.server = server;
		start();
	}
	
	public void run(){
		logger.debug("Start Serializer");
		
		Request request = null;
		HashMap<JID , JupiterClient> proxies = null;
		JupiterClient proxy = null;
		while(run){
			try {
				/* get next request in queue. */
				request = server.getNextRequestInSynchronizedQueue();
				
				proxies = server.getProxies();
				/* 1. execute receive action at appropriate proxy client. */
				proxy = proxies.get(request.getJID());
				Operation op = proxy.receiveRequest(request);
				/* 2. execute generate action at other proxy clients. */
				for(JID j : proxies.keySet()){
					proxy =  proxies.get(j);
					
					if(!j.toString().equals(request.getJID().toString())){
						/* create submit op as local proxy operation and send to client. */
						proxy.generateRequest(op);
					}
					
				}
				
			} catch (InterruptedException e) {
				logger.warn("Interrupt Exception",e);
			} catch (TransformationException e) {
				// TODO Auto-generated catch block
				logger.error("Transformation Exception ",e);

			}
		}
	}
}
