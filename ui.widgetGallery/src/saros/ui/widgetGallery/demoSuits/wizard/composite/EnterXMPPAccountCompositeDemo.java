package saros.ui.widgetGallery.demoSuits.wizard.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import saros.ui.util.LayoutUtils;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.wizard.EnterXMPPAccountComposite;

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
