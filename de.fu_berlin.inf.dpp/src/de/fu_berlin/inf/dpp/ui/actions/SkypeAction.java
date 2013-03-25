package de.fu_berlin.inf.dpp.ui.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.SkypeManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * A action for skyping other JIDs.
 * 
 * @author rdjemili
 */
@Component(module = "net")
public class SkypeAction extends Action implements Disposable {

    private static final Logger log = Logger.getLogger(SkypeAction.class
        .getName());

    protected IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(PreferenceConstants.SKYPE_USERNAME)) {
                updateEnablement();
            }
        }
    };

    protected ISelectionListener selectionListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    @Inject
    protected SkypeManager skypeManager;

    public SkypeAction() {
        super(Messages.SkypeAction_title);

        SarosPluginContext.initComponent(this);

        setToolTipText(Messages.SkypeAction_tooltip);
        setImageDescriptor(new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
                return ImageManager.ELCL_BUDDY_SKYPE_CALL.getImageData();
            }
        });

        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);
        updateEnablement();
    }

    public void updateEnablement() {
        try {
            List<JID> buddies = SelectionRetrieverFactory
                .getSelectionRetriever(JID.class).getSelection();
            setEnabled(buddies.size() == 1
                && skypeManager
                    .getSkypeURLNonBlock(buddies.get(0).getBareJID()) != null);
        } catch (NullPointerException e) {
            this.setEnabled(false);
        } catch (Exception e) {
            if (!PlatformUI.getWorkbench().isClosing())
                log.error("Unexcepted error while updating enablement", e); //$NON-NLS-1$
        }
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Utils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                final List<JID> participants = SelectionRetrieverFactory
                    .getSelectionRetriever(JID.class).getSelection();

                if (participants.size() == 1) {
                    Utils.runSafeAsync("SkypeAction", log, new Runnable() { //$NON-NLS-1$
                            @Override
                            public void run() {
                                SWTUtils.runSafeSWTSync(log, new Runnable() {
                                    @Override
                                    public void run() {
                                        setEnabled(false);
                                    }
                                });
                                final String skypeURL = skypeManager
                                    .getSkypeURL(participants.get(0)
                                        .getBareJID());
                                if (skypeURL != null) {
                                    SWTUtils.runSafeSWTSync(log,
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                URLHyperlink link = new URLHyperlink(
                                                    new Region(0, 0), skypeURL);
                                                link.open();
                                            }
                                        });
                                }

                            }
                        });
                } else {
                    log.warn("More than one participant selected."); //$NON-NLS-1$
                }
            }
        });
    }

    @Override
    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
    }
}
