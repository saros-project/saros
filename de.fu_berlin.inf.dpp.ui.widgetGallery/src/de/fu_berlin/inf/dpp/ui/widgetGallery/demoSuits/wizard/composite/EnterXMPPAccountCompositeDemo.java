package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.EnterXMPPAccountComposite;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.EnterXMPPAccountCompositeListener;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.IsSarosXMPPServerChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.XMPPServerChangedEvent;
import de.fu_berlin.inf.nebula.utils.LayoutUtils;

@Demo
public class EnterXMPPAccountCompositeDemo extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {
	parent.setLayout(LayoutUtils.createGridLayout());
	showConsole();

	EnterXMPPAccountComposite enterXMPPAccountComposite = new EnterXMPPAccountComposite(
		parent, SWT.BORDER);
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
