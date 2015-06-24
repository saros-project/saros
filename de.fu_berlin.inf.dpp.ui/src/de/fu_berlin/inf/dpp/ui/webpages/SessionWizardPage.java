package de.fu_berlin.inf.dpp.ui.webpages;

import java.util.Arrays;
import java.util.List;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.ui.browser_functions.SessionWizardPageBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.renderer.ContactListRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.ProjectListRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;

/**
 * Represents the Saros Session invitation Wizard. This wizard is used to start
 * a Session by sharing Projects with multiple contacts.
 */
public class SessionWizardPage implements BrowserPage {

    public static final String WEB_PAGE = "html/dist/start-session-wizard.html";

    private final Renderer contactListRenderer;

    private final ProjectListRenderer projectListRenderer;

    private final SessionWizardPageBrowserFunctions browserFunctions;

    public SessionWizardPage(ContactListRenderer contactListRenderer,
        ProjectListRenderer projectListRenderer,
        SessionWizardPageBrowserFunctions browserFunctions) {
        this.contactListRenderer = contactListRenderer;
        this.projectListRenderer = projectListRenderer;
        this.browserFunctions = browserFunctions;
    }

    @Override
    public String getWebpage() {
        return WEB_PAGE;
    }

    @Override
    public String getTitle() { return HTMLUIStrings.START_SESSION_WIZARD_TITLE; }

    @Override
    public List<JavascriptFunction> getJavascriptFunctions() {
        return browserFunctions.getJavascriptFunctions();
    }

    @Override
    public List<Renderer> getRenderer() {
        return Arrays.asList(contactListRenderer, projectListRenderer);
    }
}
