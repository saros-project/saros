package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.wizard.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DescriptiveDemo;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.EnterXMPPAccountComposite;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.EnterXMPPAccountCompositeListener;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.IsSarosXMPPServerChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.XMPPServerChangedEvent;

public class EnterXMPPAccountCompositeDemo extends DescriptiveDemo {

	public EnterXMPPAccountCompositeDemo(DemoContainer parent, String title) {
		super(parent, title);
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void createContent(Composite parent) {
		parent.setLayout(LayoutUtils.createGridLayout());
		showConsole();

		EnterXMPPAccountComposite enterXMPPAccountComposite = new EnterXMPPAccountComposite(
				parent, SWT.NONE);
		enterXMPPAccountComposite.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, true));
		enterXMPPAccountComposite
				.addEnterXMPPAccountCompositeListener(new EnterXMPPAccountCompositeListener() {
					public void isSarosXMPPServerChanged(
							IsSarosXMPPServerChangedEvent event) {
						addConsoleMessage("isSarosXMPPServerChanged changed to "
								+ ((event.isSarosXMPPServer()) ? "true"
										: "false"));
					}

					public void xmppServerValidityChanged(
							XMPPServerChangedEvent event) {
						addConsoleMessage("XMPP server "
								+ event.getXMPPServer()
								+ " is "
								+ ((event.isXMPPServerValid()) ? "valid"
										: "invalid"));
					}
				});
	}
}
