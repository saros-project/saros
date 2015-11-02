package de.fu_berlin.inf.dpp.intellij.ui.views;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.actions.ConnectServerAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.DisconnectServerAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.NotImplementedAction;
import de.fu_berlin.inf.dpp.intellij.ui.tree.SessionAndContactsTreeView;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.ConnectButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.ConsistencyButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.FollowButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.LeaveSessionButton;
import de.fu_berlin.inf.dpp.intellij.ui.views.buttons.SimpleButton;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.picocontainer.annotations.Inject;

import javax.swing.JToolBar;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Saros toolbar. Displays several buttons for interacting with Saros.
 * <p/>
 * FIXME: Replace by IDEA toolbar class.
 */
public class SarosToolbar extends JToolBar {
    public static final String ADD_CONTACT_ICON_PATH = "/icons/famfamfam/contact_add_tsk.png";
    public static final String OPEN_REFS_ICON_PATH = "/icons/famfamfam/test_con.gif";

    private final SessionAndContactsTreeView sarosTree;

    @Inject
    private XMPPConnectionService connectionService;

    private final ActionListener treeActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent action) {
            if (action.getSource() instanceof ConnectServerAction || action
                .getSource() instanceof DisconnectServerAction) {

                if (connectionService.isConnected()) {
                    sarosTree.renderConnected();
                } else {
                    sarosTree.renderDisconnected();
                }
            }
        }
    };

    public SarosToolbar(SessionAndContactsTreeView sarosTree) {
        super("Saros IDEA toolbar");
        this.sarosTree = sarosTree;
        SarosPluginContext.initComponent(this);
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        addToolbarButtons();
    }

    private void addToolbarButtons() {

        ConnectButton connectionButton = new ConnectButton();
        connectionButton.addActionListenerToActions(treeActionListener);
        add(connectionButton);

        add(new SimpleButton(new NotImplementedAction("addContact"),
            "Add contact to list", ADD_CONTACT_ICON_PATH, "addContact"));

        add(new SimpleButton(new NotImplementedAction("preferences"),
            "Open preferences", OPEN_REFS_ICON_PATH, "preferences"));

        add(new FollowButton());

        add(new ConsistencyButton());

        add(new LeaveSessionButton());
    }
}
