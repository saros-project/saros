package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.roster;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.views.WidgetGalleryView;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.ContactSelectionComposite;
import java.util.List;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

@Demo
public class BuddyDisplayCompositeDemo extends AbstractDemo {
  protected Label selectedJIDs;

  @Override
  public void createDemo(final Composite parent) {
    parent.setLayout(LayoutUtils.createGridLayout());

    ContactSelectionComposite buddyDisplayComposite =
        new ContactSelectionComposite(parent, SWT.BORDER | SWT.MULTI);
    buddyDisplayComposite.setLayoutData(LayoutUtils.createFillGridData());
    WidgetGalleryView.selectionProviderIntermediate.setSelectionProviderDelegate(
        buddyDisplayComposite.getViewer());

    /*
     * Display currently selected buddies
     */
    final Label selectedJIDs = new Label(parent, SWT.WRAP);
    selectedJIDs.setLayoutData(LayoutUtils.createFillHGrabGridData());
    SelectionUtils.getSelectionService()
        .addSelectionListener(
            new ISelectionListener() {
              @Override
              public void selectionChanged(IWorkbenchPart part, ISelection selection) {
                List<JID> buddies = SelectionUtils.getAdaptableObjects(selection, JID.class);

                String text;

                if (buddies.size() > 0) {
                  StringBuffer sb = new StringBuffer();
                  sb.append("Selected buddies (" + buddies.size() + "):");
                  for (int i = 0; i < buddies.size(); i++) {
                    sb.append("\n" + buddies.get(i).toString());
                  }
                  text = sb.toString();
                } else {
                  text = "No buddies selected.";
                }

                if (selection != null && !selectedJIDs.isDisposed()) {
                  selectedJIDs.setText(text);
                  selectedJIDs.getParent().layout();
                }
              }
            });
  }
}
