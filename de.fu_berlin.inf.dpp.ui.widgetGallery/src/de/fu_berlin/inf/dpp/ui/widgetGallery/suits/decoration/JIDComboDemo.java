package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.decoration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DescriptiveDemo;
import de.fu_berlin.inf.dpp.ui.widgets.decoration.JIDCombo;

public class JIDComboDemo extends DescriptiveDemo {
	@Inject
	XMPPAccountStore xmppAccountStore;

	public JIDComboDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void createContent(Composite parent) {
		parent.setLayout(LayoutUtils.createGridLayout());

		Combo combo = new Combo(parent, SWT.BORDER);
		combo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		new JIDCombo(combo);

		showXMPPAccounts();
	}

	protected void showXMPPAccounts() {
		Saros.injectDependenciesOnly(this);
		addConsoleMessage("XMPPAccounts:");
		for (XMPPAccount xmppAccount : xmppAccountStore.getAllAccounts()) {
			addConsoleMessage("- " + xmppAccount.toString());
		}
	}
}
