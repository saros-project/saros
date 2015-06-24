package de.fu_berlin.inf.dpp.ui.webpages;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.ui.browser_functions.MainPageBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.ContactListRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the Saros main view.
 */
public class MainPage implements BrowserPage {

    public static final String WEB_PAGE = "html/dist/main-page.html";

    private final AccountRenderer accountRenderer;

    private final ContactListRenderer contactListRenderer;

    private final MainPageBrowserFunctions browserFunctions;

    public MainPage(AccountRenderer accountRenderer,
        ContactListRenderer contactListRenderer,
        MainPageBrowserFunctions browserFunctions) {
        this.accountRenderer = accountRenderer;
        this.contactListRenderer = contactListRenderer;
        this.browserFunctions = browserFunctions;
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

        return  browserFunctions.getJavascriptFunctions();
    }

    @Override
    public List<Renderer> getRenderer() {

        return Arrays.asList(accountRenderer, contactListRenderer);
    }

}
