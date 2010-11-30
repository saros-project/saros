package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat.chatRoom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.ui.chat.chatControl.ChatControl;
import de.fu_berlin.inf.dpp.util.ColorUtil;

public class ChatRoomParticipant {
	protected static String server = "jabber.ccc.de";
	protected static String[] usernames = new String[] { "alice1_fu",
			"bob1_fu", "carl1_fu" };
	protected static String[] passwords = new String[] { "dddfffggg",
			"dddfffggg", "dddfffggg" };
	protected static String[] nicknames = new String[] { "Bjšrn", "Maria",
			"Lin" };
	protected static RGB[] colors = new RGB[] { new RGB(255, 128, 128),
			new RGB(128, 255, 128), new RGB(255, 255, 128) };

	protected static List<ChatRoomParticipant> all_participants = new ArrayList<ChatRoomParticipant>();

	protected XMPPConnection connection;
	protected ChatControl chatControl;
	protected String user;
	protected Color color;

	public static ChatRoomParticipant[] create(int num, Composite parent)
			throws XMPPException {
		if (num > 3 || num < 0)
			return null;

		Color colorDisplayBackground = parent.getDisplay().getSystemColor(
				SWT.COLOR_WHITE);
		Color colorInputBackground = parent.getDisplay().getSystemColor(
				SWT.COLOR_WHITE);

		ChatRoomParticipant[] chatRoomParticipants = new ChatRoomParticipant[num];
		for (int i = 0; i < num; i++) {
			XMPPConnection connection = new XMPPConnection(server);
			connection.connect();
			connection.login(usernames[i], passwords[i]);

			ChatControl chatControl = new ChatControl(parent, SWT.BORDER,
					colorDisplayBackground, colorInputBackground, 2);

			String nickname = nicknames[i];

			Color color = new Color(parent.getDisplay(), ColorUtil.scaleColor(
					colors[i], 0.75));

			chatRoomParticipants[i] = new ChatRoomParticipant(connection,
					chatControl, nickname, color);

			all_participants.add(chatRoomParticipants[i]);
		}

		return chatRoomParticipants;
	}

	public static ChatRoomParticipant getByUserJID(String userJID) {
		for (ChatRoomParticipant chatRoomParticipant : all_participants) {
			try {
				if (chatRoomParticipant.getConnection().getUser()
						.equals(userJID))
					return chatRoomParticipant;
			} catch (Exception e) {
				break;
			}
		}
		return null;
	}

	protected ChatRoomParticipant(XMPPConnection connection,
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
