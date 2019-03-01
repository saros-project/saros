package saros.ui.widgetGallery.demoSuits.chat;

import java.util.Date;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import saros.communication.chat.ChatElement;
import saros.net.xmpp.JID;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgetGallery.demoSuits.chat.chatRoom.ChatRoomParticipant;

@Demo("This demo shows a multi user chat using 3x3 chats.")
public class ChatDemo extends AbstractDemo {
  public static final int NUM_PARTICIPANTS = 3;

  protected static ChatRoomParticipant[] chatRoomParticipants = null;

  Button connectButton;
  Button disconnectButton;

  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(new GridLayout(NUM_PARTICIPANTS, false));

    try {
      chatRoomParticipants = ChatRoomParticipant.create(NUM_PARTICIPANTS, parent);
    } catch (XMPPException e) {
      e.printStackTrace();
      return;
    }

    for (final ChatRoomParticipant chatRoomParticipant : chatRoomParticipants) {
      chatRoomParticipant
          .getChatControl()
          .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }
    Composite demoControls = createDemoControls(parent, SWT.NONE);
    demoControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, NUM_PARTICIPANTS, 1));
  }

  public Composite createDemoControls(Composite parent, int style) {
    Composite demoControls = new Composite(parent, style);
    demoControls.setLayout(new RowLayout(SWT.HORIZONTAL));

    connectButton = new Button(demoControls, SWT.PUSH);
    connectButton.setText("connect");
    connectButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            connect();
          }
        });
    connectButton.setEnabled(true);

    disconnectButton = new Button(demoControls, SWT.PUSH);
    disconnectButton.setText("disconnect");
    disconnectButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            disconnect();
          }
        });
    disconnectButton.setEnabled(false);

    return demoControls;
  }

  protected void connect() {
    connectButton.setEnabled(false);
    disconnectButton.setEnabled(true);

    for (ChatRoomParticipant chatRoomParticipant : ChatDemo.chatRoomParticipants)
      chatRoomParticipant.connect();

    for (final ChatRoomParticipant chatRoomParticipant : ChatDemo.chatRoomParticipants)
      chatRoomParticipant.createChats(
          ChatDemo.chatRoomParticipants,
          new MessageListener() {
            @Override
            public void processMessage(Chat chat, final Message message) {

              final String sender = message.getFrom();

              final Color color =
                  ChatRoomParticipant.getByUserJID(chatRoomParticipants, message.getFrom())
                      .getColor();
              Display.getDefault()
                  .asyncExec(
                      new Runnable() {

                        @Override
                        public void run() {
                          chatRoomParticipant
                              .getChatControl()
                              .addChatLine(
                                  new ChatElement(message.getBody(), new JID(sender), new Date()));
                        }
                      });
            }
          });
  }

  protected void disconnect() {
    connectButton.setEnabled(true);
    disconnectButton.setEnabled(false);

    for (ChatRoomParticipant chatRoomParticipant : ChatDemo.chatRoomParticipants)
      chatRoomParticipant.destroyChats();
    for (ChatRoomParticipant chatRoomParticipant : ChatDemo.chatRoomParticipants)
      chatRoomParticipant.disconnect();
  }
}
