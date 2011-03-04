package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class TeamCImp extends Component implements TeamC {

    private static transient TeamCImp self;

    private STFBotTreeItem treeItem;

    /**
     * {@link TeamCImp} is a singleton, but inheritance is possible.
     */
    public static TeamCImp getInstance() {
        if (self != null)
            return self;
        self = new TeamCImp();
        return self;
    }

    public void setTreeItem(STFBotTreeItem view) {
        this.treeItem = view;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void shareProject(String repositoryURL) throws RemoteException {
        treeItem.contextMenu(CM_TEAM, CM_SHARE_PROJECT_OF_TEAM).click();

        STFBotShell shell = bot().shell(SHELL_SHARE_PROJECT);
        shell.confirmWithTable(TABLE_ITEM_REPOSITORY_TYPE_SVN, NEXT);

        if (shell.bot().table().containsItem(repositoryURL)) {
            shell.confirmWithTable(repositoryURL, NEXT);
        } else {
            shell.bot().radio(LABEL_CREATE_A_NEW_REPOSITORY_LOCATION).click();
            shell.bot().button(NEXT).click();
            shell.bot().comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
        }
        shell.bot().button(FINISH).waitUntilIsEnabled();
        shell.bot().button(FINISH).click();
        bot().waitUntilShellIsClosed(SHELL_SHARE_PROJECT);
    }

    public void shareProjectConfiguredWithSVNInfos(String repositoryURL)
        throws RemoteException {

        String[] contexts = { CM_TEAM, CM_SHARE_PROJECT_OF_TEAM };

        treeItem.contextMenu(contexts).click();

        STFBotShell shell = bot().shell(SHELL_SHARE_PROJECT);
        shell.confirmWithTable(TABLE_ITEM_REPOSITORY_TYPE_SVN, NEXT);
        log.debug("SVN share project text: " + shell.bot().text());
        shell.bot().button(FINISH).waitUntilIsEnabled();
        shell.bot().button(FINISH).click();
        bot().waitUntilShellIsClosed(SHELL_SHARE_PROJECT);
    }

    public void shareProjectUsingSpecifiedFolderName(String repositoryURL,
        String specifiedFolderName) throws RemoteException {
        String[] contexts = { CM_TEAM, CM_SHARE_PROJECT_OF_TEAM };

        treeItem.contextMenu(contexts).click();

        bot().shell(SHELL_SHARE_PROJECT).confirmWithTable(
            TABLE_ITEM_REPOSITORY_TYPE_SVN, NEXT);

        STFBotShell shell = bot().shell(SHELL_SHARE_PROJECT);
        STFBotTable table = shell.bot().table();

        if (table == null || !table.containsItem(repositoryURL)) {
            // close window
            shell.close();
            // in svn repos view: enter url

            bot().openViewById(VIEW_SVN_REPOSITORIES_ID);
            STFBotView view = bot().view(VIEW_SVN_REPOSITORIES);

            view.show();
            final boolean viewWasOpen = bot().isViewOpen(VIEW_SVN_REPOSITORIES);
            bot().view(VIEW_SVN_REPOSITORIES)
                .toolbarButton("Add SVN Repository").click();

            bot().waitUntilShellIsOpen("Add SVN Repository");
            STFBotShell shell2 = bot().shell("Add SVN Repository");
            shell2.activate();
            shell2.bot().comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
            shell2.bot().button(FINISH).click();
            bot().waitUntilShellIsClosed("Add SVN Repository");
            if (!viewWasOpen)
                bot().view(VIEW_SVN_REPOSITORIES).close();
            // recur...
            shareProjectUsingSpecifiedFolderName(repositoryURL,
                specifiedFolderName);
            return;
        }

        bot().shell(SHELL_SHARE_PROJECT).confirmWithTable(repositoryURL, NEXT);
        STFBotShell shell3 = bot().shell(SHELL_SHARE_PROJECT);
        shell3.bot().radio("Use specified folder name:").click();
        shell3.bot().text().setText(specifiedFolderName);
        shell3.bot().button(FINISH).click();
        bot().shell("Remote Project Exists").waitUntilActive();
        bot().shell("Remote Project Exists").confirm(YES);
        try {
            bot().shell("Confirm Open Perspective").waitUntilActive();
            bot().shell("Confirm Open Perspective").confirm(NO);
        } catch (TimeoutException e) {
            // ignore
        }
        // bot().shell(SHELL_SHARE_PROJECT).waitsUntilIsShellClosed();
    }

    public void importProjectFromSVN(String repositoryURL)
        throws RemoteException {
        bot().menu(MENU_FILE).menu("Import...").click();
        STFBotShell shell = bot().shell(SHELL_IMPORT);
        shell.confirmWithTreeWithFilterText(TABLE_ITEM_REPOSITORY_TYPE_SVN,
            "Checkout Projects from SVN", NEXT);
        if (shell.bot().table().containsItem(repositoryURL)) {
            bot().shell("Checkout from SVN").confirmWithTable(repositoryURL,
                NEXT);
        } else {
            shell.bot().radio("Create a new repository location").click();
            shell.bot().button(NEXT).click();
            shell.bot().comboBoxWithLabel("Url:").setText(repositoryURL);
            shell.bot().button(NEXT).click();
            bot().shell("Checkout from SVN").waitUntilActive();
        }
        bot().shell("Checkout from SVN").confirmWithTreeWithWaitingExpand(
            "Checkout from SVN", FINISH, repositoryURL, "trunk", "examples");
        bot().shell("SVN Checkout").waitUntilActive();
        bot().waitUntilShellIsClosed("SVN Checkout");
    }

    public void disConnect() throws RemoteException {
        String[] contexts = { CM_TEAM, CM_DISCONNECT };
        treeItem.contextMenu(contexts).click();
        bot().shell(SHELL_CONFIRM_DISCONNECT_FROM_SVN).confirm(YES);
    }

    public void revert() throws RemoteException {
        String[] contexts = { CM_TEAM, CM_REVERT };
        treeItem.contextMenu(contexts).click();
        bot().shell(SHELL_REVERT).confirm(OK);
        bot().waitUntilShellIsClosed(SHELL_REVERT);
    }

    public void update(String versionID) throws RemoteException {
        switchToAnotherRevision(versionID);
    }

   

    private void switchToAnotherRevision(String versionID)
        throws RemoteException {
        String[] contexts = { CM_TEAM, CM_SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION };

        treeItem.contextMenu(contexts).click();

        STFBotShell shell = bot().shell(SHELL_SWITCH);
        shell.waitUntilActive();
        if (shell.bot().checkBox(LABEL_SWITCH_TOHEAD_REVISION).isChecked())
            shell.bot().checkBox(LABEL_SWITCH_TOHEAD_REVISION).click();
        shell.bot().textWithLabel(LABEL_REVISION).setText(versionID);
        shell.bot().button(OK).click();
        if (bot().isShellOpen(SHELL_SVN_SWITCH))
            bot().waitUntilShellIsClosed(SHELL_SVN_SWITCH);
    }

}
