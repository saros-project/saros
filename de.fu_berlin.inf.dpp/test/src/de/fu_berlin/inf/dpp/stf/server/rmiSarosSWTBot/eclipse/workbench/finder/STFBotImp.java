package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;

public class STFBotImp extends EclipseComponentImp implements STFBot {

    private static SWTBot swtBot;

    private static transient STFBotImp self;

    /**
     * {@link ChatViewImp} is a singleton, but inheritance is possible.
     */
    public static STFBotImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotImp();
        swtBot = bot;

        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/
    public void setBot(SWTBot bot) {
        this.swtBot = bot;
    }

    public STFBotTree tree() throws RemoteException {
        stfTree.setSWTBotTree(swtBot.tree());
        return stfTree;
    }

    public STFBotShell shell(String title) throws RemoteException {
        stfShell.setShellTitle(title);
        return stfShell;
    }

    public List<String> getTitlesOfOpenedShells() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotShell shell : bot.shells())
            list.add(shell.getText());
        return list;
    }

    public boolean isShellOpen(String title) throws RemoteException {
        return getTitlesOfOpenedShells().contains(title);
    }

    public void waitsUntilIsShellClosed(final String title)
        throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isShellOpen(title);
            }

            public String getFailureMessage() {
                return null;
            }
        });

        bot.sleep(10);
    }

    public void waitUntilShellOpen(final String title) throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isShellOpen(title);
            }

            public String getFailureMessage() {
                return null;
            }
        });
    }

    public STFBotButton buttonWithLabel(String label) throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonWithLabel(label, 0));
        return stfButton;
    }

    public STFBotButton buttonWithLabel(String label, int index)
        throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonWithLabel(label, index));
        return stfButton;
    }

    public STFBotButton button(String mnemonicText) throws RemoteException {
        stfButton.setSwtBotButton(swtBot.button(mnemonicText, 0));
        return stfButton;
    }

    public STFBotButton button(String mnemonicText, int index)
        throws RemoteException {
        stfButton.setSwtBotButton(swtBot.button(mnemonicText, index));
        return stfButton;
    }

    public STFBotButton buttonWithTooltip(String tooltip) throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonWithTooltip(tooltip, 0));
        return stfButton;
    }

    public STFBotButton buttonWithTooltip(String tooltip, int index)
        throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonWithTooltip(tooltip, index));
        return stfButton;
    }

    public STFBotButton buttonWithId(String key, String value)
        throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonWithId(key, value));
        return stfButton;
    }

    public STFBotButton buttonWithId(String key, String value, int index)
        throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonWithId(key, value, index));
        return stfButton;
    }

    public STFBotButton buttonWithId(String value) throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonWithId(value));
        return stfButton;
    }

    public STFBotButton buttonWithId(String value, int index)
        throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonWithId(value, index));
        return stfButton;
    }

    public STFBotButton buttonInGroup(String inGroup) throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonInGroup(inGroup));
        return stfButton;
    }

    public STFBotButton buttonInGroup(String inGroup, int index)
        throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonInGroup(inGroup, index));
        return stfButton;
    }

    public STFBotButton button() throws RemoteException {
        stfButton.setSwtBotButton(swtBot.button());
        return stfButton;
    }

    public STFBotButton button(int index) throws RemoteException {
        stfButton.setSwtBotButton(swtBot.button(index));
        return stfButton;
    }

    public STFBotButton buttonWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        stfButton
            .setSwtBotButton(swtBot.buttonWithLabelInGroup(label, inGroup));
        return stfButton;
    }

    public STFBotButton buttonWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonWithLabelInGroup(label, inGroup,
            index));
        return stfButton;
    }

    public STFBotButton buttonInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonInGroup(mnemonicText, inGroup));
        return stfButton;
    }

    public STFBotButton buttonInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonInGroup(mnemonicText, inGroup,
            index));
        return stfButton;
    }

    public STFBotButton buttonWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonWithTooltipInGroup(tooltip,
            inGroup));
        return stfButton;
    }

    public STFBotButton buttonWithTooltipInGroup(String tooltip, String inGroup,
        int index) throws RemoteException {
        stfButton.setSwtBotButton(swtBot.buttonWithTooltipInGroup(tooltip,
            inGroup, index));
        return stfButton;
    }

    public STFBotLabel label() throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.label());
        return stfLabel;
    }

    public STFBotStyledText styledText() throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledText());
        return stfStyledText;
    }

}
