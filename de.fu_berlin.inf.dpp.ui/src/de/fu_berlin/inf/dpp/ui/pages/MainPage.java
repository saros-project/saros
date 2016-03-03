package de.fu_berlin.inf.dpp.ui.pages;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
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

    /**
     * Created by PicoContainer
     * 
     * @see HTMLUIContextFactory
     */
    public MainPage(

    AccountRenderer accountRenderer,

    StateRenderer stateRenderer,

    AddContact addContact,

    DeleteContact deleteContact,

    RenameContact renameContact,

    GetValidJID getValidJID,

    ConnectAccount connectAccount,

    DisconnectAccount disconnectAccount,

    ShowSessionWizard showSessionWizard,

    ShowAccountPage showAccountPage) {

        super(HTML_DOC_NAME, HTMLUIStrings.MAIN_PAGE_TITLE);

        this.addBrowserFunctions(addContact, connectAccount, disconnectAccount,
            deleteContact, getValidJID, renameContact, showAccountPage,
            showSessionWizard);
        this.addRenderer(stateRenderer, accountRenderer);
    }

}
