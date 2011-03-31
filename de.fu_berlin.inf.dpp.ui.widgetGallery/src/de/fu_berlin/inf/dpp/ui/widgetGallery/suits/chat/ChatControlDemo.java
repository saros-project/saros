package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat;

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

import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.ChatControl;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.ChatClearedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.IChatControlListener;
import de.fu_berlin.inf.dpp.ui.widgets.chatControl.events.MessageEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory.SimpleExplanatoryComposite;

public class ChatControlDemo extends Demo {
	public String user1;
	public Color user1_color;

	public String user2;
	public Color user2_color;

	SimpleExplanatoryComposite explanatoryComposite;
	ChatControl chatControl;

	protected SimpleExplanation howToExplanation = new SimpleExplanation(
			SWT.ICON_INFORMATION,
			"In order to use this chat you need to be connected to a Saros session.");

	public ChatControlDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	public void createPartControls(Composite parent) {
		Color red = new Color(parent.getDisplay(), 141, 206, 231);
		Color green = new Color(Display.getDefault(), 191, 187, 130);

		user1 = "bkahlert@saros-con.imp.fu-berlin.de/Saros";
		user1_color = red;

		user2 = "Maria Spiering";
		user2_color = green;

		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(LayoutUtils.createGridLayout());

		Composite demoControls = createDemoControls(root, SWT.NONE);
		demoControls
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.explanatoryComposite = createSimpleExplanatoryComposite(root,
				SWT.NONE);
		this.explanatoryComposite.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, true, true));

		addChatLines();
	}

	public Composite createDemoControls(Composite parent, int style) {
		Composite demoControls = new Composite(parent, style);
		demoControls.setLayout(new RowLayout(SWT.HORIZONTAL));

		Button newLine = new Button(demoControls, SWT.PUSH);
		newLine.setText("new short line");
		newLine.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				ChatControlDemo.this.chatControl
						.addChatLine(
								user1,
								user1_color,
								"Nam semper dolor sed nibh adipiscing eget rhoncus ligula posuere.",
								new Date());
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Button newLongLine = new Button(demoControls, SWT.PUSH);
		newLongLine.setText("new long line");
		newLongLine.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				ChatControlDemo.this.chatControl
						.addChatLine(
								user2,
								user2_color,
								"Maecenas iaculis justo enim. Curabitur fringilla tristique suscipit. Nam suscipit ullamcorper imperdiet. Aenean fermentum tempus leo sit amet auctor. Proin eu mi nibh, vel tincidunt massa. Nulla lacus nisi, interdum quis facilisis quis, fermentum non leo. Aliquam erat volutpat. Sed euismod, mauris quis luctus volutpat, leo nisl hendrerit mauris, at.",
								new Date());
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Button expl = new Button(demoControls, SWT.PUSH);
		expl.setText("show explanation");
		expl.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				SimpleExplanation howToExplanation = new SimpleExplanation(
						SWT.ICON_INFORMATION,
						"In order to use this chat you need to be connected to a Saros session.");
				ChatControlDemo.this.explanatoryComposite
						.showExplanation(howToExplanation);
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Button explHide = new Button(demoControls, SWT.PUSH);
		explHide.setText("hide explanation");
		explHide.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				ChatControlDemo.this.explanatoryComposite.hideExplanation();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Button setFocus = new Button(demoControls, SWT.PUSH);
		setFocus.setText("set focus on control");
		setFocus.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				ChatControlDemo.this.chatControl.setFocus();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		return demoControls;
	}

	public SimpleExplanatoryComposite createSimpleExplanatoryComposite(
			Composite parent, int style) {
		SimpleExplanatoryComposite explanatoryComposite = new SimpleExplanatoryComposite(
				parent, style);

		this.chatControl = new ChatControl(explanatoryComposite, SWT.BORDER,
				explanatoryComposite.getDisplay().getSystemColor(
						SWT.COLOR_WHITE), explanatoryComposite.getDisplay()
						.getSystemColor(SWT.COLOR_WHITE), 2);

		explanatoryComposite.setContentControl(this.chatControl);

		this.chatControl.addChatControlListener(new IChatControlListener() {
			public void messageEntered(MessageEnteredEvent event) {
				addConsoleMessage("Message entered: "
						+ event.getEnteredMessage());
			}

			public void characterEntered(CharacterEnteredEvent event) {
				addConsoleMessage("Character entered: "
						+ event.getEnteredCharacter());
			}

			public void chatCleared(ChatClearedEvent event) {
				System.out.println("Chat cleared");
			}
		});

		return explanatoryComposite;
	}

	public void addChatLines() {
		this.chatControl.addChatLine(user1, user1_color, "Hallo", new Date(
				1288666777));
		this.chatControl
				.addChatLine(
						user1,
						user1_color,
						"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam.",
						new Date(1288686777));
		this.chatControl
				.addChatLine(user1, user1_color,
						"Mauris vehicula pulvinar ornare. Morbi.", new Date(
								1288766777));

		this.chatControl
				.addChatLine(
						user2,
						user2_color,
						"Fusce pulvinar posuere porttitor. Mauris tempus convallis metus, id elementum.",
						new Date(1288866777));
		this.chatControl
				.addChatLine(
						user2,
						user2_color,
						"Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit...",
						new Date(1288966777));
		this.chatControl.addChatLine(user2, user2_color,
				"Praesent sed tempor sem. Pellentesque.", new Date(1289066777));

		this.chatControl.addChatLine(user1, user1_color,
				"Curabitur tempus sodales diam nec pulvinar. Suspendisse.",
				new Date(1289166777));

		this.chatControl.addChatLine(user2, user2_color,
				"Duis nibh purus, sodales vel.", new Date(1289266777));

		this.chatControl
				.addChatLine(
						user1,
						user1_color,
						"Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Phasellus tortor lacus, eleifend eget dignissim at, hendrerit eget justo. Donec at risus at dolor consequat dignissim. Cras imperdiet luctus metus elementum porta. Maecenas id diam eu libero.",
						new Date(1289366777));
		this.chatControl.addChatLine(user1, user1_color,
				"Suspendisse vulputate odio eros. Aliquam.", new Date(
						1289466777));
		this.chatControl.addChatLine(user1, user1_color,
				"Vivamus at libero vel purus.", new Date(1289566777));

		this.chatControl
				.addChatLine(user2, user2_color,
						"In tempor consequat porttitor. Vivamus.", new Date(
								1289566777));
		this.chatControl.addChatLine(user2, user2_color,
				"Lorem ipsum dolor sit amet.", new Date(1289666777));
		this.chatControl.addChatLine(user2, user2_color,
				"Praesent non cursus mauris. Mauris dignissim.", new Date(
						1289766777));

		this.chatControl.addChatLine(user1, user1_color,
				"Curabitur porttitor, eros eget tincidunt.", new Date(
						1289666777));

		this.chatControl.addChatLine(user2, user2_color,
				"Suspendisse varius lacinia massa vel.", new Date(1298666777));
	}
}
