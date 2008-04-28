package de.fu_berlin.inf.dpp.net.internal;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.net.IChatManager;
import de.fu_berlin.inf.dpp.net.IReceiver;



public class PrivateChatManager implements IChatManager{

	private static Logger log = Logger.getLogger(PrivateChatManager.class.getName());
	
	private XMPPConnection connection;
	
	private IReceiver receiver;
	
	/*
	 * (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.net.IChatManager#setConnection(org.jivesoftware.smack.XMPPConnection, de.fu_berlin.inf.dpp.net.IReceiver)
	 */
	public void setConnection(XMPPConnection connection, IReceiver receiver) {
		this.connection = connection;
		this.connection.addPacketListener(this, new MessageTypeFilter(Message.Type.chat));
		setReceiver(receiver);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.smack.packet.Packet)
	 */
	public void processPacket(Packet packet) {
		log.debug("incoming packet");
		receiver.processPacket(packet);
		
	}

	
	
	/*
	 * (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.net.IChatManager#setReceiver(de.fu_berlin.inf.dpp.net.IReceiver)
	 */
	public void setReceiver(IReceiver receiver) {
		this.receiver = receiver;
		
	}


	/*
	 * (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.net.IChatManager#isConnected()
	 */
	public boolean isConnected() {
		if(connection.isConnected() && receiver != null){
			return true;
		}
		return false;
	}

	public void sendActivity() {
		// TODO Auto-generated method stub
		
	}



}
