package de.fu_berlin.inf.dpp.ui.widgets.viewer.session;

import java.io.IOException;

import javax.jmdns.JmDNS;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.model.TreeLabelProvider;
import de.fu_berlin.inf.dpp.ui.model.mdns.MDNSComparator;
import de.fu_berlin.inf.dpp.ui.model.mdns.MDNSContentProvider;
import de.fu_berlin.inf.dpp.ui.model.session.SessionComparator;
import de.fu_berlin.inf.dpp.ui.model.session.SessionContentProvider;
import de.fu_berlin.inf.dpp.ui.model.session.SessionInput;

/**
 * This {@link Composite} displays the {@link SarosSession} and the Local Area
 * Network via MDNS in parallel.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE and those supported by {@link SessionDisplayComposite}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @author srossbach
 * 
 */
public final class MDNSSessionDisplayComposite extends SessionDisplayComposite {

    private static final Logger LOG = Logger
        .getLogger(MDNSSessionDisplayComposite.class);

    private JmDNS jmDNS;

    public MDNSSessionDisplayComposite(Composite parent, int style) {
        super(parent, style);
    }

    @Override
    protected void configureViewer(TreeViewer viewer) {

        try {
            jmDNS = JmDNS.create();
        } catch (IOException e) {
            jmDNS = null;
            LOG.error(e);
        }

        viewer.setContentProvider(new SessionContentProvider(
            new MDNSContentProvider()));

        viewer.setComparator(new SessionComparator(new MDNSComparator()));
        viewer.setLabelProvider(new TreeLabelProvider());
        viewer.setUseHashlookup(true);
    }

    @Override
    protected void updateViewer() {
        checkWidget();
        getViewer().setInput(
            new SessionInput(sessionManager.getSarosSession(), jmDNS));
    }
}