package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.List;
import java.util.Vector;

import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterDocumentServer;

public class ConcurrentDocumentManager {

	private List<JupiterDocumentServer> concurrentDocuments;
	
	public ConcurrentDocumentManager(){
		concurrentDocuments = new Vector<JupiterDocumentServer>();
	}
	
	/*
	 * 1. hinzufügen und löschen von jupiter servern
	 * 2. list mit transmitter threads, die Nachrichten aus den 
	 *    outgoing queues versenden.
	 * 3. Schnittstelle vom Itransmitter zu den einzelnen jupiter
	 *    document servern, um die Nachrichten vom Itransmitter
	 *    weiterzuleiten.
	 * 
	 * 
	 * */
}
