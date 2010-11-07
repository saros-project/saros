package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.IChatListener;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.MessageEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat.chatRoom.ChatRoomParticipant;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat.chatRoom.ChatRoomUtil;

public class ChatRoomDemo extends Demo {
	ChatRoomParticipant[] chatRoomParticipants = null;

	public ChatRoomDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		parent.setLayout(new GridLayout(3, false));

		try {
			chatRoomParticipants = ChatRoomUtil.createChatRoomParticipants(3,
					parent);
		} catch (XMPPException e) {
			e.printStackTrace();
			return;
		}

		for (ChatRoomParticipant chatRoomParticipant : chatRoomParticipants) {
			chatRoomParticipant.getChatControl().setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, true));

			chatRoomParticipant.getChatControl().addChatListener(
					new IChatListener() {

						@Override
						public void chatCleared(ChatClearedEvent event) {
							// TODO Auto-generated method stub

						}

						@Override
						public void messageEntered(MessageEnteredEvent event) {
							// TODO Auto-generated method stub

						}

						@Override
						public void characterEntered(CharacterEnteredEvent event) {
							// TODO Auto-generated method stub

						}
					});

			ChatManager chatManager = chatRoomParticipant.getConnection()
					.getChatManager();
			// TODO
			// http://www.igniterealtime.org/builds/smack/docs/3.1.0/javadoc/org/jivesoftware/smack/XMPPConnection.html
		}
	}
}
