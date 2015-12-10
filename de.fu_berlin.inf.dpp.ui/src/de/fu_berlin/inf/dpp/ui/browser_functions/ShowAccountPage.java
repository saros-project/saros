package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.webpages.AccountPage;

/**
 * Offers a via Javascript invokable method to open and display the
 * {@link AccountPage} dialog.
 * <p>
 * JS-signature: "void __java_showAccountPage()".
 */
public class ShowAccountPage extends JavascriptFunction {
    private DialogManager dialogManager;
    private AccountPage accountPage;
    public static final String JS_NAME = "showAccountPage";

    /**
     * Created by PicoContainer
     * 
     * @param dialogManager
     * @param accountPage
     * @see HTMLUIContextFactory
     */
    public ShowAccountPage(DialogManager dialogManager, AccountPage accountPage) {
        super(NameCreator.getConventionName(JS_NAME));
        this.dialogManager = dialogManager;
        this.accountPage = accountPage;
    }

    @Override
    public Object function(Object[] arguments) {
        this.dialogManager.showDialogWindow(accountPage);
        return null;
    }

}
