package de.fu_berlin.inf.dpp.ui.webpages;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.ui.browser_functions.AddAccountBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;

import java.util.Collections;
import java.util.List;

/**
 * Represents the wizard to add new accounts.
 */
public class AddAccountPage implements BrowserPage {

    public static final String WEB_PAGE = "html/add-account-page.html";
    
    private final AddAccountBrowserFunctions browserFunctions;

    public AddAccountPage(AddAccountBrowserFunctions browserFunctions) {
        this.browserFunctions = browserFunctions;
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

        return browserFunctions.getJavascriptFunctions();
    }

    @Override
    public List<Renderer> getRenderer() {

        return Collections.emptyList();
    }
}
