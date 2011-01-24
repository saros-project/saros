package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat.chatRoom.ChatRoomParticipant;
import de.fu_berlin.inf.dpp.ui.widgetGallery.widgets.DemoExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.ChatControl;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.ChatControlAdapter;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.MessageEnteredEvent;

public class ChatDemo extends Demo {
	public static final int NUM_PARTICIPANTS = 3;

	ChatRoomParticipant[] chatRoomParticipants = null;

	public ChatDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		parent.setLayout(new GridLayout(NUM_PARTICIPANTS, false));

		DemoExplanation expl = new DemoExplanation(parent,
				"This demo shows a multi user chat using " + NUM_PARTICIPANTS
						+ "x" + NUM_PARTICIPANTS + " "
						+ Chat.class.getSimpleName() + "s.");
		expl.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false,
				NUM_PARTICIPANTS, 1));

		try {
			chatRoomParticipants = ChatRoomParticipant.create(NUM_PARTICIPANTS,
					parent);
		} catch (XMPPException e) {
			e.printStackTrace();
			return;
		}

		for (final ChatRoomParticipant chatRoomParticipant : chatRoomParticipants) {
			chatRoomParticipant.getChatControl().setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, true));

			// Paket reception
			addChatManagerListener(chatRoomParticipant);

			// Paket dispatch
			Chat[] chats = createChats(chatRoomParticipant,
					chatRoomParticipants);
			addChatControlListener(chatRoomParticipant.getChatControl(), chats);
		}
	}

	/**
	 * Creates a chat for each participant and the sender.
	 * 
	 * @param from
	 *            1st chat participant
	 * @param to
	 *            2nd participants
	 * @return
	 */
	protected Chat[] createChats(ChatRoomParticipant from,
			ChatRoomParticipant[] to) {
		final Chat[] chats = new Chat[to.length];
		ChatManager chatManager = from.getConnection().getChatManager();
		for (int i = 0; i < to.length; i++) {
			chats[i] = chatManager.createChat(to[i].getConnection().getUser(),
					null);
		}
		return chats;
	}

	/**
	 * Prepares a chat control so entered messages are send to all chats
	 * 
	 * @param chatControl
	 *            the control that fires the {@link MessageEnteredEvent}s
	 * @param chats
	 *            to which to send the entered message
	 */
	protected void addChatControlListener(ChatControl chatControl,
			final Chat[] chats) {
		chatControl.addChatControlListener(new ChatControlAdapter() {
			@Override
			public void messageEntered(MessageEnteredEvent event) {
				try {
					for (Chat chat : chats) {
						chat.sendMessage(event.getEnteredMessage());
					}
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Adds listeners to the participants {@link ChatManager} which listen for
	 * incoming chat request. To every newly created chat a listener gets added.
	 * This listener redirects the receives message to {@link ChatControl}.
	 * 
	 * @param chatRoomParticipant
	 */
	protected void addChatManagerListener(
			final ChatRoomParticipant chatRoomParticipant) {
		ChatManager chatManager = chatRoomParticipant.getConnection()
				.getChatManager();
		chatManager.addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean createdLocally) {
				chat.addMessageListener(new MessageListener() {

					@Override
					public void processMessage(Chat chat, final Message message) {

						final String sender = message.getFrom();
						final Color color = ChatRoomParticipant.getByUserJID(
								message.getFrom()).getColor();
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								chatRoomParticipant.getChatControl()
										.addChatLine(sender, color,
												message.getBody(), new Date());
							}
						});

					}
				});
			}
		});
	}
}
