package de.fu_berlin.inf.dpp.net.jingle;

/**
 * 
 * @author orieger
 *
 */
public class JingleFileTransferProcessMonitor {

	private boolean complete = false;
	
	public JingleFileTransferProcessMonitor(){
		
	}
	
	public boolean isDone(){
		return complete;
	}
	
	public void setComplete(boolean status){
		this.complete = status;
	}
	
}
