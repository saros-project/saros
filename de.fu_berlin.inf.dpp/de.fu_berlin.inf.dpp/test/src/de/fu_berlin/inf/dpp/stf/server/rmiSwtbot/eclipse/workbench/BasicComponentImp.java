package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class BasicComponentImp extends EclipseComponent implements
    BasicComponent {

    // public static EclipseBasicObjectImp classVariable;
    private static final boolean SCREENSHOTS = true;

    private static transient BasicComponentImp eclipseBasicObjectImp;

    /**
     * {@link BasicComponentImp} is a singleton, but inheritance is
     * possible.
     */
    public static BasicComponentImp getInstance() {
        if (eclipseBasicObjectImp != null)
            return eclipseBasicObjectImp;
        eclipseBasicObjectImp = new BasicComponentImp();
        return eclipseBasicObjectImp;
    }

    public void sleep(long millis) throws RemoteException {
        bot.sleep(millis);
    }

    public void captureScreenshot(String filename) throws RemoteException {
        if (SCREENSHOTS)
            bot.captureScreenshot(filename);
    }

    // // FIXME If the file doesn't exist, this method hits the
    // // SWTBotPreferences.TIMEOUT (5000ms) while waiting on a tree node.
    // public boolean isJavaClassExistInGui(String projectName, String pkg,
    // String className) throws RemoteException {
    // showViewPackageExplorer();
    // activatePackageExplorerView();
    // return isTreeItemExist(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
    // projectName, "src", pkg, className + ".java");
    // }

    public boolean isTextWithLabelEqualWithText(String label, String text)
        throws RemoteException {
        return bot.textWithLabel(label).getText().equals(text);
    }

    public void clickButton(String mnemonicText) throws RemoteException {
        bot.button(mnemonicText).click();
    }

    @Override
    protected void precondition() throws RemoteException {
        // TODO Auto-generated method stub

    }

}
