package de.fu_berlin.inf.dpp.net.jingle;

public interface IFileTransferTransmitter extends Runnable, IJingleFileTransferConnection{

	public void setTransmit(boolean transmit);
	
	public void start();
	
	public void stop();

}
