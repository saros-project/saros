package de.fu_berlin.inf.dpp.ui.webpages;

import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.browser_functions.AddContact;
import de.fu_berlin.inf.dpp.ui.browser_functions.ConnectAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.DeleteContact;
import de.fu_berlin.inf.dpp.ui.browser_functions.DisconnectAccount;
import de.fu_berlin.inf.dpp.ui.browser_functions.GetValidJID;
import de.fu_berlin.inf.dpp.ui.browser_functions.RenameContact;
import de.fu_berlin.inf.dpp.ui.browser_functions.ShowAccountPage;
import de.fu_berlin.inf.dpp.ui.browser_functions.ShowSessionWizard;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer;

/**
 * Represents the Saros main view.
 */
public class MainPage extends AbstractBrowserPage {

    public static final String HTML_DOC_NAME = "main-page.html";

    // Injection of used Renderer
    @Inject
    private AccountRenderer accountRenderer;
    @Inject
    private StateRenderer stateRenderer;

    // Injection of provided BrowserFunctions
    @Inject
    private AddContact addContact;
    @Inject
    private DeleteContact deleteContact;
    @Inject
    private RenameContact renameContact;
    @Inject
    private GetValidJID getValidJID;
    @Inject
    private ConnectAccount connectAccount;
    @Inject
    private DisconnectAccount disconnectAccount;
    @Inject
    private ShowSessionWizard showSessionWizard;
    @Inject
    private ShowAccountPage showAccountPage;

    public MainPage() {
        super(HTML_DOC_NAME, HTMLUIStrings.MAIN_PAGE_TITLE);
        SarosPluginContext.initComponent(this);

        this.addBrowserFunctions(addContact, connectAccount, disconnectAccount,
            deleteContact, getValidJID, renameContact, showAccountPage,
            showSessionWizard);
        this.addRenderer(stateRenderer, accountRenderer);
    }

}
