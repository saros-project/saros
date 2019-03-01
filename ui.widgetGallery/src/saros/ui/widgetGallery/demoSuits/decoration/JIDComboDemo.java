package saros.ui.widgetGallery.demoSuits.decoration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.ui.util.LayoutUtils;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.decoration.JIDCombo;

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
