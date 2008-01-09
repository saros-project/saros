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
	
	@Override
	public void setConnection(XMPPConnection connection, IReceiver receiver) {
		this.connection = connection;
		this.connection.addPacketListener(this, new MessageTypeFilter(Message.Type.chat));
		setReceiver(receiver);
		
	}

	@Override
	public void processPacket(Packet packet) {
		log.debug("incoming packet");
		receiver.processPacket(packet);
		
	}

	
	
	@Override
	public void setReceiver(IReceiver receiver) {
		this.receiver = receiver;
		
	}



}
