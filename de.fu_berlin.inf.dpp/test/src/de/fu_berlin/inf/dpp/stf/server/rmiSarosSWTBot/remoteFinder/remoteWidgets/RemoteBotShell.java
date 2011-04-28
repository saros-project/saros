package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.Map;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.StringResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.IRemoteBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.RemoteBot;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.SarosSWTBot;

public class RemoteBotShell extends AbstractRmoteWidget implements
    IRemoteBotShell {
    private static transient RemoteBotShell self;

    public final static String TEXT_FIELD_TYPE_FILTER_TEXT = "type filter text";

    private SWTBotShell widget;

    /**
     * {@link RemoteBotShell} is a singleton, but inheritance is possible.
     */
    public static RemoteBotShell getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotShell();
        return self;
    }

    public IRemoteBotShell setWidget(SWTBotShell shell) {
        this.widget = shell;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    public IRemoteBot bot() {
        RemoteBot botImp = RemoteBot.getInstance();
        // botImp.setBot(swtBotShell.bot());
        botImp.setBot(SarosSWTBot.getInstance());
        return botImp;
    }

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    public boolean activate() throws RemoteException {
        widget.activate();
        return true;
    }

    public void close() throws RemoteException {
        widget.close();
    }

    public void confirm() throws RemoteException {
        activate();
        bot().button().click();
    }

    public void confirm(String buttonText) throws RemoteException {
        activate();
        bot().button(buttonText).click();
    }

    public void confirmWithTree(String buttonText, String... nodes)
        throws RemoteException {
        activate();
        bot().tree().selectTreeItem(nodes);
        bot().button(buttonText).waitUntilIsEnabled();
        bot().button(buttonText).click();
    }

    public void confirmWithTextField(String textLabel, String text,
        String buttonText) throws RemoteException {
        activate();
        bot().textWithLabel(textLabel).setText(text);

        bot().button(buttonText).waitUntilIsEnabled();
        bot().button(buttonText).click();
    }

    public void confirmWithTextFieldAndWait(Map<String, String> labelsAndTexts,
        String buttonText) throws RemoteException {
        activate();
        for (String label : labelsAndTexts.keySet()) {
            String text = labelsAndTexts.get(label);
            bot().textWithLabel(label).setText(text);
        }
        bot().button(buttonText).waitUntilIsEnabled();
        bot().button(buttonText).click();

    }

    public void confirmWithTreeWithWaitingExpand(String buttonText,
        String... nodes) throws RemoteException {
        bot().tree().selectTreeItemAndWait(nodes);
        bot().button(buttonText).click();
    }

    public void confirmWithCheckBox(String buttonText, boolean isChecked)
        throws RemoteException {
        activate();
        if (isChecked)
            bot().checkBox().click();
        bot().button(buttonText).click();

    }

    public void confirmWithCheckBoxs(String buttonText, String... itemNames)
        throws RemoteException {
        waitUntilActive();
        for (String itemName : itemNames) {
            bot().tree().selectTreeItem(itemName).check();
            // bot().table().getTableItem(itemName).check();
        }
        bot().button(buttonText).waitUntilIsEnabled();
        bot().button(buttonText).click();
    }

    public void confirmWithTable(String itemName, String buttonText)
        throws RemoteException {
        waitUntilActive();
        try {
            bot().table().select(itemName);
            bot().button(buttonText).waitUntilIsEnabled();
            bot().button(buttonText).click();
            // waitUntilShellCloses(shellName);
        } catch (WidgetNotFoundException e) {
            log.error("tableItem" + itemName + "can not be fund!");
        }
    }

    public void confirmWithTreeWithFilterText(String rootOfTreeNode,
        String teeNode, String buttonText) throws RemoteException {
        waitUntilActive();
        bot().text(TEXT_FIELD_TYPE_FILTER_TEXT).setText(teeNode);
        bot().tree().waitUntilItemExists(rootOfTreeNode);
        bot().tree().selectTreeItem(rootOfTreeNode, teeNode);
        bot().button(buttonText).waitUntilIsEnabled();
        bot().button(buttonText).click();
        // waitUntilShellCloses(shellName);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    public boolean isEnabled() throws RemoteException {
        return widget.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return widget.isVisible();
    }

    public String getText() throws RemoteException {
        return widget.getText();
    }

    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

    public String getErrorMessage() throws RemoteException {
        activate();
        final String errorMessage = UIThreadRunnable
            .syncExec(new StringResult() {
                public String run() {
                    WizardDialog dialog = (WizardDialog) widget.widget
                        .getData();
                    return dialog.getErrorMessage();
                }
            });
        if (errorMessage == null) {
            throw new WidgetNotFoundException("Could not find errorMessage!");
        }
        return errorMessage;
    }

    public String getMessage() throws RemoteException {
        activate();
        final String message = UIThreadRunnable.syncExec(new StringResult() {
            public String run() {
                WizardDialog dialog = (WizardDialog) widget.widget.getData();
                return dialog.getMessage();
            }
        });
        if (message == null) {
            throw new WidgetNotFoundException("Could not find message!");
        }
        return message;
    }

    public boolean existsTableItem(String label) throws RemoteException {
        activate();
        return bot().table().containsItem(label);
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/

    public void waitUntilActive() throws RemoteException {
        stfBot.waitUntil(SarosConditions.ShellActive(widget));
    }

    public void waitShortUntilIsClosed() throws RemoteException {
        stfBot.waitShortUntil(SarosConditions.isShellClosed(widget));
    }

    public void waitLongUntilIsClosed() throws RemoteException {
        stfBot.waitLongUntil(SarosConditions.isShellClosed(widget));
    }

}
