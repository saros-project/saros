package saros.ui.widgetGallery.demoSuits.roster;

import java.util.List;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import saros.net.xmpp.JID;
import saros.ui.util.LayoutUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgetGallery.views.WidgetGalleryView;
import saros.ui.widgets.viewer.roster.ContactSelectionComposite;

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
