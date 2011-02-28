package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class ChatDemoContainer extends DemoContainer {

	public ChatDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new ChatControlDemo(this, "ChatControl");
		/*
		 * User can either display ChatDemo or MultiUserChatDemo Both cannot be
		 * active in the same application because each connects its users
		 * automatically leading to multiple connections with the same
		 * credentials.
		 */
		new ChatDemo(this, "Chat");
		// open(new MultiUserChatDemo(this, "MultiUserChat"));
	}

}
