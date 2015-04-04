package de.fu_berlin.inf.dpp.ui.webpages;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.ui.browser_functions.AddContactBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;

import java.util.Collections;
import java.util.List;

/**
 * Represents the wizard to add new contacts.
 */
public class AddContactPage implements BrowserPage {

    public static final String WEB_PAGE = "html/add-contact-page.html";
    
    private final AddContactBrowserFunctions browserFunctions;

    public AddContactPage(AddContactBrowserFunctions browserFunctions) {
        this.browserFunctions = browserFunctions;
    }

    @Override
    public String getWebpage() {
        return WEB_PAGE;
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
