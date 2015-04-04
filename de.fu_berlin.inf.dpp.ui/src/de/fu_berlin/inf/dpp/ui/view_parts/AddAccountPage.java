package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.ui.browser_functions.AddAccountBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;

import java.util.Collections;
import java.util.List;

/**
 * Represents the wizard to add new accounts.
 */
public class AddAccountPage implements BrowserPage {

    @Override
    public String getWebpage() {
        return "html/add-account-page.html";
    }

    @Override
    public List<JavascriptFunction> getJavascriptFunctions() {
        return new AddAccountBrowserFunctions()
                    .getJavascriptFunctions();
    }

    @Override
    public List<Renderer> getRenderer() {
        return Collections.emptyList();
    }
}
