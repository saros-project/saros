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
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * A action for skyping other users.
 * 
 * @author rdjemili
 */
public class SkypeAction extends SelectionProviderAction {

    private static final Logger log = Logger.getLogger(SkypeAction.class
        .getName());

    protected String skypeURL;

    @Inject
    protected SkypeManager skypeManager;

    public SkypeAction(ISelectionProvider provider) {
        super(provider, "Skype this user");

        Saros.reinject(this);

        setEnabled(false);

        setToolTipText("Start a Skype-VoIP session with this user");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/telephone.png"));
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
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
        final Object item = selection.getFirstElement();

        if ((selection.size() != 1) || !(item instanceof RosterEntry)) {
            setEnabled(false);
        } else {
            setEnabled(false);
            Util.runSafeAsync("SkypeAction-", log, new Runnable() {
                public void run() {
                    setEnabled(false);
                    skypeURL = skypeManager.getSkypeURL((RosterEntry) item);
                    setEnabled(skypeURL != null);
                }
            });
        }
    }
}
