package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.ui.chat.chatControl.ChatControl;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.CharacterEnteredEvent;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.IChatListener;
import de.fu_berlin.inf.dpp.ui.chat.chatControl.events.MessageEnteredEvent;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;
import de.fu_berlin.inf.dpp.ui.widgets.explanation.explanatory.SimpleExplanatoryComposite;
import de.fu_berlin.inf.dpp.util.ColorUtil;

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
	
	@Override
	public void createPartControls(Composite parent) {
		Color red = new Color(parent.getDisplay(), 255, 128, 128);
		Color lightRed = ColorUtil.scaleColor(red, .75);

		Color green = new Color(Display.getDefault(), 128, 255, 128);
		Color lightGreen = ColorUtil.scaleColor(green, .75);

		user1 = "bkahlert@saros-con.imp.fu-berlin.de/Saros";
		user1_color = lightRed;

		user2 = "Maria Spiering";
		user2_color = lightGreen;
		
		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new GridLayout(1, false));

		Composite demoControls = createDemoControls(root, SWT.NONE);
		demoControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.explanatoryComposite = createSimpleExplanatoryComposite(root, SWT.NONE);
		this.explanatoryComposite.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, true, true));
		

		addChatLines();
	}

	public Composite createDemoControls(Composite parent, int style) {
		Composite demoControls = new Composite(parent, style);
		demoControls.setLayout(new FillLayout(SWT.HORIZONTAL));

		Button newLine = new Button(demoControls, SWT.PUSH);
		newLine.setText("new short line");
		newLine.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ChatControlDemo.this.chatControl.addChatLine(user1,
						user1_color, "Hallo Hallo");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Button newLongLine = new Button(demoControls, SWT.PUSH);
		newLongLine.setText("new long line");
		newLongLine.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ChatControlDemo.this.chatControl
						.addChatLine(
								user2,
								user2_color,
								"Hallo Hallo Hallo v v vHallo Hallo Hallo v Hallo Hallo Hallo Hallo Hallo Hallo Hallo Hallo Hallo Hallo Hallo Hallo HalloHallo Hallo HalloHalloHalloHallo Hallo");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Button expl = new Button(demoControls, SWT.PUSH);
		expl.setText("show explanation");
		expl.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				SimpleExplanation howToExplanation = new SimpleExplanation(
						SWT.ICON_INFORMATION,
						"In order to use this chat you need to be connected to a Saros session.");
				ChatControlDemo.this.explanatoryComposite
						.showExplanation(howToExplanation);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Button explHide = new Button(demoControls, SWT.PUSH);
		explHide.setText("hide explanation");
		explHide.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ChatControlDemo.this.explanatoryComposite
						.hideExplanation();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		Button setFocus = new Button(demoControls, SWT.PUSH);
		setFocus.setText("set focus on control");
		setFocus.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ChatControlDemo.this.chatControl.setFocus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		return demoControls;
	}
	
	public SimpleExplanatoryComposite createSimpleExplanatoryComposite(Composite parent, int style) {
		SimpleExplanatoryComposite explanatoryComposite = new SimpleExplanatoryComposite(parent, style);
		
		this.chatControl = new ChatControl(explanatoryComposite, SWT.BORDER,
				explanatoryComposite.getDisplay().getSystemColor(
						SWT.COLOR_WHITE), explanatoryComposite.getDisplay()
						.getSystemColor(SWT.COLOR_WHITE), 2);
		
		explanatoryComposite.setContentControl(this.chatControl);
		
		this.chatControl.addChatListener(new IChatListener() {
			public void messageEntered(MessageEnteredEvent event) {
				System.out.println("Message entered: "
						+ event.getEnteredMessage());
			}

			@Override
			public void characterEntered(CharacterEnteredEvent event) {
				System.out.println("Character entered: " + event.getEnteredCharacter());
			}
		});
		
		return explanatoryComposite;
	}

	public void addChatLines() {
		this.chatControl.addChatLine(user1, user1_color, "Hallo");
		this.chatControl
				.addChatLine(
						user1,
						user1_color,
						"Hallo Hallo Hallo v v vHallo Hallo Hallo v Hallo Hallo Hallo Hallo Hallo Hallo Hallo Hallo Hallo Hallo Hallo Hallo HalloHallo Hallo HalloHalloHalloHallo Hallo");
		this.chatControl.addChatLine(user1, user1_color, "Hallo");

		this.chatControl.addChatLine(user2, user2_color, "Hallo dsds");
		this.chatControl.addChatLine(user2, user2_color, "Hallosdsfdfdfsdfsd");
		this.chatControl.addChatLine(user2, user2_color, "Hallofdsfdsfdfsd");

		this.chatControl.addChatLine(user1, user1_color, "Hallo");

		this.chatControl.addChatLine(user2, user2_color, "Hallosdsfdfdfsdfsd");

		this.chatControl.addChatLine(user1, user1_color, "Hallo");
		this.chatControl.addChatLine(user1, user1_color, "Hallo");
		this.chatControl.addChatLine(user1, user1_color, "Hallo");

		this.chatControl.addChatLine(user2, user2_color, "Hallo dsds");
		this.chatControl.addChatLine(user2, user2_color, "Hallosdsfdfdfsdfsd");
		this.chatControl.addChatLine(user2, user2_color, "Hallofdsfdsfdfsd");

		this.chatControl.addChatLine(user1, user1_color, "Hallo");

		this.chatControl.addChatLine(user2, user2_color, "Hallosdsfdfdfsdfsd");
	}
}
