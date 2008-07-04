package de.fu_berlin.inf.dpp.net.internal;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
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
		Message message = (Message) packet;
		
		RequestPacketExtension packetExtension = PacketExtensions.getJupiterRequestExtension(message);
		
		if(packetExtension != null){
			receiver.processRequest(packet);
		}else{
			receiver.processPacket(packet);
		}
		
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

	public void sendRequest(Request request) {
		// TODO Auto-generated method stub
//		log.info("Sent request: " + request);
//		try {
//			/* create new message for multi chat. */
//			Message newMessage = muc.createMessage();
//			/* add packet extension. */
//			newMessage.addExtension(new ActivitiesPacketExtension(activities));
//			/* add jid property */
//			newMessage.setProperty(JID_PROPERTY, Saros.getDefault().getMyJID()
//					.toString());
//
//			// newMessage.setBody("test");
//			muc.sendMessage(newMessage);
//			PacketProtokollLogger.getInstance().sendPacket(newMessage);
//
//		} catch (XMPPException e) {
//
//			Saros.getDefault().getLog().log(
//					new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
//							"Could not send message, message queued", e));
//		}
	}



}
