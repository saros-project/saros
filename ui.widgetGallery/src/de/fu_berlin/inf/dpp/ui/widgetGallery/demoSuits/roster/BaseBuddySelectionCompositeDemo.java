package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.roster;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.ContactSelectionComposite;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

@Demo("This demo shows a Composite that reflects the currently selected buddies in the workbench.")
public class BaseBuddySelectionCompositeDemo extends AbstractDemo {
  protected ContactSelectionComposite baseBuddySelectionComposite;

  protected ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          baseBuddySelectionComposite.setSelectedContacts(
              SelectionRetrieverFactory.getSelectionRetriever(JID.class).getOverallSelection());
        }
      };

  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(new GridLayout(1, false));

    baseBuddySelectionComposite = new ContactSelectionComposite(parent, SWT.BORDER);
    baseBuddySelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
    super.dispose();
  }
}
