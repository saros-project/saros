package de.fu_berlin.inf.dpp.ui.actions;

import javax.swing.event.HyperlinkEvent;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
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
    private RosterEntry rosterEntry;

    public SkypeAction(ISelectionProvider provider) {
        super(provider, "Skype this user");
        
        setToolTipText("Start a Skype-VoIP session with this user");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/telephone.png"));
    }
    
    @Override
    public void run() {
        if (rosterEntry == null) return;
        
        String url = SkypeManager.getDefault().getSkypeURL(rosterEntry);
        URLHyperlink link = new URLHyperlink(new Region(0, 0), url);
        link.open();
    }
    
    @Override
    public void selectionChanged(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        rosterEntry = (RosterEntry)selected;
//        
//        if (selection.size() == 1 && selected instanceof RosterEntry) {
//            rosterEntry = (RosterEntry)selected;
//            setEnabled(true);
//        } else {
//            rosterEntry = null;
//            setEnabled(false);
//        }
        
        // TODO disable if user == self
    }
}
