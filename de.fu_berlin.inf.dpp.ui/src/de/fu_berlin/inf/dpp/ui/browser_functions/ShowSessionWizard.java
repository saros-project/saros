package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.pages.SessionWizardPage;

/**
 * Offers a via Javascript invokable method to open and display the
 * {@link SessionWizardPage} dialog.
 * <p>
 * JS-signature: "void __java_showSessionWizard()".
 */
public class ShowSessionWizard extends JavascriptFunction {
    private DialogManager dialogManager;
    private SessionWizardPage sessionWizardPage;
    public static final String JS_NAME = "showSessionWizard";

    // TODO: Rename to openXYZ for more convenient naming

    /**
     * Created by PicoContainer
     * 
     * @param dialogManager
     * @param sessionWizardPage
     * @see HTMLUIContextFactory
     */
    public ShowSessionWizard(DialogManager dialogManager,
        SessionWizardPage sessionWizardPage) {
        super(NameCreator.getConventionName(JS_NAME));
        this.dialogManager = dialogManager;
        this.sessionWizardPage = sessionWizardPage;

    }

    @Override
    public Object function(Object[] arguments) {
        dialogManager.showDialogWindow(sessionWizardPage);
        return null;
    }

}
