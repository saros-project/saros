package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.chat;

import de.fu_berlin.inf.dpp.communication.chat.ChatElement;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChat;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatPreferences;
import de.fu_berlin.inf.dpp.communication.chat.muc.MultiUserChatService;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleExplanationComposite.SimpleExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleExplanatoryComposite;
import de.fu_berlin.inf.dpp.ui.widgets.chat.ChatControl;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.IChatControlListener;
import de.fu_berlin.inf.dpp.ui.widgets.chat.events.MessageEnteredEvent;
import java.util.Date;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.picocontainer.annotations.Inject;

@Demo
public class ChatControlDemo extends AbstractDemo {
  public String user1;
  public Color user1_color;

  public String user2;
  public Color user2_color;

  SimpleExplanatoryComposite explanatoryComposite;
  ChatControl chatControl;

  @Inject protected MultiUserChatService multiUserChatService;

  @Inject
  protected SimpleExplanation howToExplanation =
      new SimpleExplanation(
          SWT.ICON_INFORMATION,
          "In order to use this chat you need to be connected to a Saros session.");

  @Override
  public void createDemo(Composite parent) {
    Color red = new Color(parent.getDisplay(), 141, 206, 231);
    Color green = new Color(Display.getDefault(), 191, 187, 130);

    user1 = "bkahlert@saros-con.imp.fu-berlin.de/Saros";
    user1_color = red;

    user2 = "Maria Spiering";
    user2_color = green;

    Composite root = new Composite(parent, SWT.NONE);
    root.setLayout(LayoutUtils.createGridLayout());

    Composite demoControls = createDemoControls(root, SWT.NONE);
    demoControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

    this.explanatoryComposite = createSimpleExplanatoryComposite(root, SWT.NONE);
    this.explanatoryComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    addChatLines();
  }

  public Composite createDemoControls(Composite parent, int style) {
    Composite demoControls = new Composite(parent, style);
    demoControls.setLayout(new RowLayout(SWT.HORIZONTAL));

    Button newLine = new Button(demoControls, SWT.PUSH);
    newLine.setText("new short line");
    newLine.addSelectionListener(
        new SelectionListener() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            ChatControlDemo.this.chatControl.addChatLine(
                new ChatElement(
                    "Nam semper dolor sed nibh adipiscing eget rhoncus ligula posuere.",
                    new JID(user1),
                    new Date()));
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            // Not interested
          }
        });

    Button newLongLine = new Button(demoControls, SWT.PUSH);
    newLongLine.setText("new long line");
    newLongLine.addSelectionListener(
        new SelectionListener() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            ChatControlDemo.this.chatControl.addChatLine(
                new ChatElement(
                    "Maecenas iaculis justo enim. Curabitur fringilla tristique suscipit. Nam suscipit ullamcorper imperdiet. Aenean fermentum tempus leo sit amet auctor. Proin eu mi nibh, vel tincidunt massa. Nulla lacus nisi, interdum quis facilisis quis, fermentum non leo. Aliquam erat volutpat. Sed euismod, mauris quis luctus volutpat, leo nisl hendrerit mauris, at.",
                    new JID(user2),
                    new Date()));
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            // Not interested
          }
        });

    Button expl = new Button(demoControls, SWT.PUSH);
    expl.setText("show explanation");
    expl.addSelectionListener(
        new SelectionListener() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            SimpleExplanation howToExplanation =
                new SimpleExplanation(
                    SWT.ICON_INFORMATION,
                    "In order to use this chat you need to be connected to a Saros session.");
            ChatControlDemo.this.explanatoryComposite.showExplanation(howToExplanation);
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            // Not interested
          }
        });

    Button explHide = new Button(demoControls, SWT.PUSH);
    explHide.setText("hide explanation");
    explHide.addSelectionListener(
        new SelectionListener() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            ChatControlDemo.this.explanatoryComposite.hideExplanation();
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            // Not interested
          }
        });

    Button setFocus = new Button(demoControls, SWT.PUSH);
    setFocus.setText("set focus on control");
    setFocus.addSelectionListener(
        new SelectionListener() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            ChatControlDemo.this.chatControl.setFocus();
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            // Not interested
          }
        });

    return demoControls;
  }

  public SimpleExplanatoryComposite createSimpleExplanatoryComposite(Composite parent, int style) {
    SimpleExplanatoryComposite explanatoryComposite = new SimpleExplanatoryComposite(parent, style);

    MultiUserChatPreferences preferences =
        new MultiUserChatPreferences(null, "demoRoom", "password");

    this.chatControl =
        new ChatControl(
            null,
            new MultiUserChat(null, preferences),
            parent,
            SWT.BORDER,
            explanatoryComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE),
            2);

    explanatoryComposite.setContentControl(this.chatControl);

    this.chatControl.addChatControlListener(
        new IChatControlListener() {
          @Override
          public void messageEntered(MessageEnteredEvent event) {
            addConsoleMessage("Message entered: " + event.getEnteredMessage());
          }

          @Override
          public void characterEntered(CharacterEnteredEvent event) {
            addConsoleMessage("Character entered: " + event.getEnteredCharacter());
          }

          @Override
          public void chatCleared(ChatClearedEvent event) {
            System.out.println("Chat cleared");
          }
        });

    return explanatoryComposite;
  }

  public void addChatLines() {
    this.chatControl.addChatLine(new ChatElement("Hallo", new JID(user1), new Date(1288666777)));
    this.chatControl.addChatLine(
        new ChatElement(
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam.",
            new JID(user1),
            new Date(1288686777)));
    this.chatControl.addChatLine(
        new ChatElement(
            "Mauris vehicula pulvinar ornare. Morbi.", new JID(user1), new Date(1288766777)));

    this.chatControl.addChatLine(
        new ChatElement(
            "Fusce pulvinar posuere porttitor. Mauris tempus convallis metus, id elementum.",
            new JID(user2),
            new Date(1288866777)));
    this.chatControl.addChatLine(
        new ChatElement(
            "Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit...",
            new JID(user2),
            new Date(1288966777)));
    this.chatControl.addChatLine(
        new ChatElement(
            "Praesent sed tempor sem. Pellentesque.", new JID(user2), new Date(1289066777)));

    this.chatControl.addChatLine(
        new ChatElement(
            "Curabitur tempus sodales diam nec pulvinar. Suspendisse.",
            new JID(user1),
            new Date(1289166777)));

    this.chatControl.addChatLine(
        new ChatElement("Duis nibh purus, sodales vel.", new JID(user2), new Date(1289266777)));

    this.chatControl.addChatLine(
        new ChatElement(
            "Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Phasellus tortor lacus, eleifend eget dignissim at, hendrerit eget justo. Donec at risus at dolor consequat dignissim. Cras imperdiet luctus metus elementum porta. Maecenas id diam eu libero.",
            new JID(user1),
            new Date(1289366777)));
    this.chatControl.addChatLine(
        new ChatElement(
            "Suspendisse vulputate odio eros. Aliquam.", new JID(user1), new Date(1289466777)));
    this.chatControl.addChatLine(
        new ChatElement("Vivamus at libero vel purus.", new JID(user1), new Date(1289566777)));

    this.chatControl.addChatLine(
        new ChatElement(
            "In tempor consequat porttitor. Vivamus.", new JID(user2), new Date(1289566777)));
    this.chatControl.addChatLine(
        new ChatElement("Lorem ipsum dolor sit amet.", new JID(user2), new Date(1289666777)));
    this.chatControl.addChatLine(
        new ChatElement(
            "Praesent non cursus mauris. Mauris dignissim.", new JID(user2), new Date(1289766777)));

    this.chatControl.addChatLine(
        new ChatElement(
            "Curabitur porttitor, eros eget tincidunt.", new JID(user1), new Date(1289666777)));

    this.chatControl.addChatLine(
        new ChatElement(
            "Suspendisse varius lacinia massa vel.", new JID(user2), new Date(1298666777)));
  }
}
