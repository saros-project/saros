package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.composite;

import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.EnterXMPPAccountComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

@Demo
public class EnterXMPPAccountCompositeDemo extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(LayoutUtils.createGridLayout());
    showConsole();

    EnterXMPPAccountComposite enterXMPPAccountComposite =
        new EnterXMPPAccountComposite(parent, SWT.BORDER);
    enterXMPPAccountComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
  }
}
