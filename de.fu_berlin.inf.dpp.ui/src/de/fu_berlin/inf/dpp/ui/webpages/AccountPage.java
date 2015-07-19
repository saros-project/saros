package de.fu_berlin.inf.dpp.ui.webpages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.browser_functions.CloseAccountWizard;
import de.fu_berlin.inf.dpp.ui.browser_functions.SaveAccount;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;

/**
 * Represents the wizard to add new accounts.
 */
public class AccountPage implements BrowserPage {
    // TODO: NOT USED AT THE MOMENT! Create HTML page and open it in the
    // main-page.html by calling "__java_showAccountWizard();"
    public static final String WEB_PAGE = "html/dist/account-page.html";

    private final List<JavascriptFunction> browserFunctions = new ArrayList<JavascriptFunction>();

    @Inject
    private SaveAccount saveAccount;
    @Inject
    private CloseAccountWizard closeAccountWizard;

    public AccountPage() {
        SarosPluginContext.initComponent(this);
        // Add BrowserFunctions once
        browserFunctions.add(closeAccountWizard);
        browserFunctions.add(saveAccount);
    }

    @Override
    public String getWebpage() {
        return WEB_PAGE;
    }

    @Override
    public String getTitle() {
        return HTMLUIStrings.ADD_ACCOUNT_PAGE_TITLE;
    }

    @Override
    public List<JavascriptFunction> getJavascriptFunctions() {
        return this.browserFunctions;
    }

    @Override
    public List<Renderer> getRenderer() {
        return Collections.emptyList();
    }
}
