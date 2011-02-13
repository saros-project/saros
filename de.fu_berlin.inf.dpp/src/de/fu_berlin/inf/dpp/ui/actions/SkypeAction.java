package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jivesoftware.smack.RosterEntry;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SkypeManager;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.ui.RosterView.TreeItem;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * A action for skyping other users.
 * 
 * @author rdjemili
 */
@Component(module = "net")
public class SkypeAction extends SelectionProviderAction {

    private static final Logger log = Logger.getLogger(SkypeAction.class
        .getName());

    protected String skypeURL;

    @Inject
    protected SkypeManager skypeManager;

    public SkypeAction(ISelectionProvider provider) {
        super(provider, "Skype this buddy");

        Saros.injectDependenciesOnly(this);

        setEnabled(false);

        setToolTipText("Start a Skype-VoIP session with this buddy");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/telephone.png"));
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Utils.runSafeSync(log, new Runnable() {
            public void run() {
                runOpenSkype();
            }
        });
    }

    public void runOpenSkype() {
        if (this.skypeURL == null) {
            return;
        }

        URLHyperlink link = new URLHyperlink(new Region(0, 0), this.skypeURL);
        link.open();
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {

        if (selection.size() != 1) {
            setEnabled(false);
            return;
        }

        final RosterEntry rosterEntry = ((TreeItem) selection.getFirstElement())
            .getRosterEntry();
        if (rosterEntry == null) {
            setEnabled(false);
            return;
        }
        Utils.runSafeAsync("SkypeAction-", log, new Runnable() {
            public void run() {
                Utils.runSafeSWTSync(log, new Runnable() {
                    public void run() {
                        setEnabled(false);
                    }
                });
                skypeURL = skypeManager.getSkypeURL(rosterEntry.getUser());
                Utils.runSafeSWTSync(log, new Runnable() {
                    public void run() {
                        setEnabled(skypeURL != null);
                    }
                });
            }
        });
    }
}
