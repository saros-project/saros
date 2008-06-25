package de.fu_berlin.inf.dpp.concurrent.management;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.concurrent.IDriverManager;
import de.fu_berlin.inf.dpp.net.JID;

public class DriverDocument {

	private static Logger logger = Logger.getLogger(DriverDocument.class);
	
	private IPath editor;
	
	private HashMap<JID, JID> currentDriver;
	
	public DriverDocument(IPath editor){
		this.editor = editor;
		this.currentDriver = new HashMap<JID, JID>();
	}
	
	public IPath getEditor(){
		return this.editor;
	}

	public void addDriver(JID jid) {
		/* if driver not exists in list. */
		if(!isDriver(jid)){
			this.currentDriver.put(jid, jid);
		}else{
			logger.debug("Driver "+jid+" is already Driver for "+this.editor.lastSegment().toString());
		}
	}

	public boolean isDriver(JID jid) {
		return currentDriver.containsKey(jid);
	}
	
	public boolean noDriver(){
		return currentDriver.isEmpty();
	}

	public void removeDriver(JID jid) {
		if(isDriver(jid)){
			this.currentDriver.remove(jid);
		}else{
			logger.warn("JID "+jid+" is not driver for this document "+this.editor.lastSegment().toString());
		}
		
	}
}
