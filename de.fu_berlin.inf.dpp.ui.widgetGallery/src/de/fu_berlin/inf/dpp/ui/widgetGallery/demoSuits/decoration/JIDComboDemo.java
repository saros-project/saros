package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.decoration;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.decoration.JIDCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.picocontainer.annotations.Inject;

@Demo
public class JIDComboDemo extends AbstractDemo {
  @Inject XMPPAccountStore xmppAccountStore;

  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(LayoutUtils.createGridLayout());

    Combo combo = new Combo(parent, SWT.BORDER);
    combo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

    new JIDCombo(combo);

    showXMPPAccounts();
  }

  protected void showXMPPAccounts() {
    SarosPluginContext.initComponent(this);
    addConsoleMessage("XMPPAccounts:");
    for (XMPPAccount xmppAccount : xmppAccountStore.getAllAccounts()) {
      addConsoleMessage("- " + xmppAccount.toString());
    }
  }
}
