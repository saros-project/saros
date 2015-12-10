package de.fu_berlin.inf.dpp.ui.webpages;

import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.browser_functions.CloseSessionInvitationWizard;
import de.fu_berlin.inf.dpp.ui.browser_functions.GetValidJID;
import de.fu_berlin.inf.dpp.ui.browser_functions.SendInvitation;
import de.fu_berlin.inf.dpp.ui.renderer.ProjectListRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer;

/**
 * Represents the Saros Session invitation Wizard. This wizard is used to start
 * a Session by sharing Projects with multiple contacts.
 */
public class SessionWizardPage extends AbstractBrowserPage {

    public static final String HTML_DOC_NAME = "start-session-wizard.html";
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

    /**
     * Created by PicoContainer
     * 
     * @see HTMLUIContextFactory
     */
    public SessionWizardPage() {
        super(HTML_DOC_NAME, HTMLUIStrings.START_SESSION_WIZARD_TITLE);
        SarosPluginContext.initComponent(this);

        this.addBrowserFunctions(closeSessionInvitationWizard, getValidJID,
            sendInvitation);
        this.addRenderer(stateRenderer, projectListRenderer);
    }
}
