package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat.chatRoom;

import org.eclipse.swt.graphics.Color;
import org.jivesoftware.smack.XMPPConnection;

import de.fu_berlin.inf.dpp.ui.chat.chatControl.ChatControl;

public class ChatRoomParticipant {
	protected XMPPConnection connection;
	protected ChatControl chatControl;
	protected String user;
	protected Color color;

	public ChatRoomParticipant(XMPPConnection connection,
			ChatControl chatControl, String user, Color color) {
		super();
		this.connection = connection;
		this.chatControl = chatControl;
		this.user = user;
		this.color = color;
	}

	public XMPPConnection getConnection() {
		return connection;
	}

	public ChatControl getChatControl() {
		return chatControl;
	}

	public String getUser() {
		return user;
	}

	public Color getColor() {
		return color;
	}

}
