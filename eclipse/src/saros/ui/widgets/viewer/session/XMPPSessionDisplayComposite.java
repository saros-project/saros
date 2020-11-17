package saros.ui.widgets.viewer.session;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.internal.SarosSession;
import saros.ui.model.TreeLabelProvider;
import saros.ui.model.roster.RosterComparator;
import saros.ui.model.roster.RosterContentProvider;
import saros.ui.model.session.SessionComparator;
import saros.ui.model.session.SessionContentProvider;
import saros.ui.model.session.SessionInput;

/**
 * This {@link Composite} displays the {@link SarosSession} and the Contact list in parallel.
 *
 * <p>This composite does <strong>NOT</strong> handle setting the layout.
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>NONE and those supported by {@link SessionDisplayComposite}
 *   <dt><b>Events:</b>
 *   <dd>(none)
 * </dl>
 */
public final class XMPPSessionDisplayComposite extends SessionDisplayComposite {

  @Inject private XMPPContactsService contactsService;

  public XMPPSessionDisplayComposite(Composite parent, int style) {
    super(parent, style);

    addDisposeListener(
        e -> {
          contactsService = null;
        });
  }

  @Override
  protected void configureViewer(TreeViewer viewer) {
    viewer.setContentProvider(new SessionContentProvider(new RosterContentProvider()));

    viewer.setLabelProvider(new TreeLabelProvider());
    viewer.setComparator(new SessionComparator(new RosterComparator()));
    viewer.setUseHashlookup(true);
  }

  @Override
  protected void updateViewer() {
    checkWidget();

    getViewer().setInput(new SessionInput(sessionManager.getSession(), contactsService));
  }
}
