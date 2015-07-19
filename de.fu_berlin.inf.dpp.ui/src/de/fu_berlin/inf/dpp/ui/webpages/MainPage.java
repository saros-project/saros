package de.fu_berlin.inf.dpp.ui.webpages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
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
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer;

/**
 * Represents the Saros main view.
 */
public class MainPage implements BrowserPage {
    // TODO: Extract common path prefix "html/dist/" to BrowserPage
    public static final String WEB_PAGE = "html/dist/main-page.html";
    private final List<JavascriptFunction> browserFunctions = new ArrayList<JavascriptFunction>();

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
        SarosPluginContext.initComponent(this);
        // Add BrowserFunctions once
        browserFunctions.add(addContact);
        browserFunctions.add(connectAccount);
        browserFunctions.add(disconnectAccount);
        browserFunctions.add(deleteContact);
        browserFunctions.add(getValidJID);
        browserFunctions.add(renameContact);
        browserFunctions.add(showAccountPage);
        browserFunctions.add(showSessionWizard);

    }

    @Override
    public String getWebpage() {
        return WEB_PAGE;
    }

    @Override
    public String getTitle() {
        return HTMLUIStrings.MAIN_PAGE_TITLE;
    }

    @Override
    public List<JavascriptFunction> getJavascriptFunctions() {
        return this.browserFunctions;
    }

    @Override
    public List<Renderer> getRenderer() {
        return Arrays.asList(accountRenderer, stateRenderer);
    }
}
