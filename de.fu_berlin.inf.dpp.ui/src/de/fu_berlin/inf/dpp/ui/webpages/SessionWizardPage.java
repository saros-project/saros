package de.fu_berlin.inf.dpp.ui.webpages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.browser_functions.CloseSessionInvitationWizard;
import de.fu_berlin.inf.dpp.ui.browser_functions.GetValidJID;
import de.fu_berlin.inf.dpp.ui.browser_functions.SendInvitation;
import de.fu_berlin.inf.dpp.ui.renderer.ProjectListRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer;

/**
 * Represents the Saros Session invitation Wizard. This wizard is used to start
 * a Session by sharing Projects with multiple contacts.
 */
public class SessionWizardPage implements BrowserPage {

    public static final String WEB_PAGE = "html/dist/start-session-wizard.html";

    private final List<JavascriptFunction> browserFunctions = new ArrayList<JavascriptFunction>();

    // Injection of used Renderer
    @Inject
    private StateRenderer stateRenderer;
    @Inject
    private ProjectListRenderer projectListRenderer;
    // Inject of provided BrowserFunctions
    @Inject
    private CloseSessionInvitationWizard closeSessionInvitationWizard;
    @Inject
    private SendInvitation sendInvitation;
    @Inject
    private GetValidJID getValidJID;

    public SessionWizardPage() {
        SarosPluginContext.initComponent(this);
        // Add BrowserFunctions once
        browserFunctions.add(closeSessionInvitationWizard);
        browserFunctions.add(getValidJID);
        browserFunctions.add(sendInvitation);
    }

    @Override
    public String getWebpage() {
        return WEB_PAGE;
    }

    @Override
    public String getTitle() {
        return HTMLUIStrings.START_SESSION_WIZARD_TITLE;
    }

    @Override
    public List<JavascriptFunction> getJavascriptFunctions() {
        return this.browserFunctions;
    }

    @Override
    public List<Renderer> getRenderer() {
        return Arrays.asList(stateRenderer, projectListRenderer);
    }
}
