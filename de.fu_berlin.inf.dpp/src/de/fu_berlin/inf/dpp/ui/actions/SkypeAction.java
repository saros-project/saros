package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jivesoftware.smack.RosterEntry;

import de.fu_berlin.inf.dpp.SkypeManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;

/**
 * A action for skyping other users.
 * 
 * @author rdjemili
 */
public class SkypeAction extends SelectionProviderAction {
    private String skypeURL;

    public SkypeAction(ISelectionProvider provider) {
        super(provider, "Skype this user");
        setEnabled(false);

        setToolTipText("Start a Skype-VoIP session with this user");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/telephone.png"));
    }

    @Override
    public void run() {
        if (skypeURL == null)
            return;

        URLHyperlink link = new URLHyperlink(new Region(0, 0), skypeURL);
        link.open();
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        final Object item = selection.getFirstElement();

        if (selection.size() != 1 || !(item instanceof RosterEntry)) {
            setEnabled(false);

        } else {
            new Thread(new Runnable() {
                public void run() {
                    setEnabled(false);
                    SkypeManager sm = SkypeManager.getDefault();
                    String url = sm.getSkypeURL((RosterEntry)item);
                    setEnabled(url != null);
                }
            }).start();
        }
    }
}
