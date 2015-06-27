package de.fu_berlin.inf.dpp.ui.webpages;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactSpecificBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.MainPageBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the Saros main view.
 */
public class MainPage implements BrowserPage {

    public static final String WEB_PAGE = "html/dist/main-page.html";

    private final AccountRenderer accountRenderer;

    private final StateRenderer contactListRenderer;

    private final MainPageBrowserFunctions mainPageBrowserFunctions;

    private final ContactSpecificBrowserFunctions contactSpecificBrowserFunctions;

    public MainPage(AccountRenderer accountRenderer,
        StateRenderer contactListRenderer,
        MainPageBrowserFunctions mainPageBrowserFunctions,
        ContactSpecificBrowserFunctions contactSpecificBrowserFunctions) {
        this.accountRenderer = accountRenderer;
        this.contactListRenderer = contactListRenderer;
        this.mainPageBrowserFunctions = mainPageBrowserFunctions;
        this.contactSpecificBrowserFunctions = contactSpecificBrowserFunctions;
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

        List<JavascriptFunction> javaScriptFunctions = new ArrayList<JavascriptFunction>();
        javaScriptFunctions.addAll(mainPageBrowserFunctions
            .getJavascriptFunctions());
        javaScriptFunctions.addAll(contactSpecificBrowserFunctions
            .getJavascriptFunctions());

        return javaScriptFunctions;
    }

    @Override
    public List<Renderer> getRenderer() {
        return Arrays.asList(accountRenderer, contactListRenderer);
    }

}
