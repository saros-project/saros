package saros.ui.widgetGallery.demoSuits.rosterSession;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import saros.ui.util.LayoutUtils;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgets.viewer.session.XMPPSessionDisplayComposite;

@Demo(
    "This BuddyDisplayComposite IPostSelectionProvider.\nIf you selected buddies and switch to another buddy demo you\n"
        + "will notice that the selection has been updated there, too.")
public class BuddySessionDisplayCompositeDemo extends AbstractDemo {
  protected Label selectedJIDs;

  @Override
  public void createDemo(final Composite parent) {
    parent.setLayout(LayoutUtils.createGridLayout());

    XMPPSessionDisplayComposite buddySessionDisplayComposite =
        new XMPPSessionDisplayComposite(parent, SWT.BORDER | SWT.MULTI);
    buddySessionDisplayComposite.setLayoutData(LayoutUtils.createFillGridData());
  }
}
