package de.fu_berlin.inf.dpp.test.net;
/**
 * this class contains test case for transfering jupiter requests as 
 * jabber packet extensions.
 */
import org.eclipse.core.runtime.Path;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.RequestImpl;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.RequestExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.RequestPacketExtension;

import junit.framework.TestCase;

public class RequestTransmitterTest extends TestCase implements PacketListener, MessageListener  {

	static {
		XMPPConnection.DEBUG_ENABLED = true;
		ProviderManager providermanager = ProviderManager.getInstance();
		providermanager.addExtensionProvider(RequestPacketExtension.ELEMENT, RequestPacketExtension.NAMESPACE,
				new RequestExtensionProvider());
	}
	private XMPPConnection connection1;
	private XMPPConnection connection2;
	Request req;
	
	public RequestTransmitterTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		connection1 = new XMPPConnection("jabber.cc");
		connection1.connect();
		connection1.login("ori78", "123456");
		
		connection2 = new XMPPConnection("jabber.cc");
		connection2.connect();
		connection2.login("ori79", "123456");
		connection2.addPacketListener(this, new MessageTypeFilter(
				Message.Type.chat));
		
		req = new RequestImpl(1,new JupiterVectorTime(1,3),new DeleteOperation(34,"insert text"));
		req.setEditorPath(new Path("hello"));
		req.setJID(new JID("ori78@jabber.cc"));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testSendRequest() throws XMPPException, Exception{
		ChatManager chatmanager = connection1.getChatManager();
		Chat newChat = chatmanager.createChat(connection2.getUser(),this);

		try {
			Message message = new Message();
//			Request req = new RequestImpl(1,new JupiterVectorTime(1,3),new DeleteOperation(34,"insert text"));
//			req.setEditorPath(new Path("hello"));
//			req.setJID(new JID("ori78@jabber.cc"));
		    message.addExtension(new RequestPacketExtension(req));
		    newChat.sendMessage(message);
		}
		catch (XMPPException e) {
		    System.out.println("Error Delivering block");
		}
		Thread.sleep(300);
	}

	public void processPacket(Packet packet) {
		
		Message message = (Message) packet;
		
		RequestPacketExtension packetExtension = (RequestPacketExtension)message.getExtension(RequestPacketExtension.ELEMENT,RequestPacketExtension.NAMESPACE);
		if(packetExtension != null){
			System.out.println("Received request : "+packetExtension.getRequest().toString());
			assertEquals(req,packetExtension.getRequest());
		}else{
			System.out.println("Failure in request packet extension.");
		}
	}

	public void processMessage(Chat chat, Message message) {
		RequestPacketExtension packetExtension = (RequestPacketExtension)message.getExtension(RequestPacketExtension.ELEMENT,
				RequestPacketExtension.NAMESPACE);
		if(packetExtension != null){
			System.out.println("Received request : "+packetExtension.getRequest().toString());
			assertEquals(req,packetExtension.getRequest());
		}else{
			System.out.println("Failure in request packet extension.");
		}
	}
	
	

}
