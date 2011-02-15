package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShellImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;

public class STFBotImp extends EclipseComponentImp implements STFBot {

    private static transient STFBotImp self;

    private static SWTBot swtBot;

    private static STFBotShellImp shell;
    private static STFBotButtonImp button;

    /**
     * {@link ChatViewImp} is a singleton, but inheritance is possible.
     */
    public static STFBotImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotImp();
        swtBot = bot;
        shell = STFBotShellImp.getInstance();
        button = STFBotButtonImp.getInstance();

        return self;
    }

    public void setBot(SWTBot bot) {
        swtBot = bot;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * Widget tree
     * 
     **********************************************/

    public STFBotTree tree() throws RemoteException {
        stfTree.setSWTBotTree(swtBot.tree());
        return stfTree;
    }

    /**********************************************
     * 
     * Widget shell
     * 
     **********************************************/

    public void closeAllShells() throws RemoteException {
        bot.closeAllShells();

    }

    public STFBotShell shell(String title) throws RemoteException {
        shell.setShellTitle(title);
        return shell;
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

    /**********************************************
     * 
     * Widget button
     * 
     **********************************************/

    public STFBotButton buttonWithLabel(String label) throws RemoteException {
        button.setSwtBotButton(swtBot.buttonWithLabel(label, 0));
        return button;
    }

    public STFBotButton buttonWithLabel(String label, int index)
        throws RemoteException {
        button.setSwtBotButton(swtBot.buttonWithLabel(label, index));
        return button;
    }

    public STFBotButton button(String mnemonicText) throws RemoteException {
        button.setSwtBotButton(swtBot.button(mnemonicText, 0));
        return button;
    }

    public STFBotButton button(String mnemonicText, int index)
        throws RemoteException {
        button.setSwtBotButton(swtBot.button(mnemonicText, index));
        return button;
    }

    public STFBotButton buttonWithTooltip(String tooltip)
        throws RemoteException {
        button.setSwtBotButton(swtBot.buttonWithTooltip(tooltip, 0));
        return button;
    }

    public STFBotButton buttonWithTooltip(String tooltip, int index)
        throws RemoteException {
        button.setSwtBotButton(swtBot.buttonWithTooltip(tooltip, index));
        return button;
    }

    public STFBotButton buttonWithId(String key, String value)
        throws RemoteException {
        button.setSwtBotButton(swtBot.buttonWithId(key, value));
        return button;
    }

    public STFBotButton buttonWithId(String key, String value, int index)
        throws RemoteException {
        button.setSwtBotButton(swtBot.buttonWithId(key, value, index));
        return button;
    }

    public STFBotButton buttonWithId(String value) throws RemoteException {
        button.setSwtBotButton(swtBot.buttonWithId(value));
        return button;
    }

    public STFBotButton buttonWithId(String value, int index)
        throws RemoteException {
        button.setSwtBotButton(swtBot.buttonWithId(value, index));
        return button;
    }

    public STFBotButton buttonInGroup(String inGroup) throws RemoteException {
        button.setSwtBotButton(swtBot.buttonInGroup(inGroup));
        return button;
    }

    public STFBotButton buttonInGroup(String inGroup, int index)
        throws RemoteException {
        button.setSwtBotButton(swtBot.buttonInGroup(inGroup, index));
        return button;
    }

    public STFBotButton button() throws RemoteException {
        button.setSwtBotButton(swtBot.button());
        return button;
    }

    public STFBotButton button(int index) throws RemoteException {
        button.setSwtBotButton(swtBot.button(index));
        return button;
    }

    public STFBotButton buttonWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        button.setSwtBotButton(swtBot.buttonWithLabelInGroup(label, inGroup));
        return button;
    }

    public STFBotButton buttonWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        button.setSwtBotButton(swtBot.buttonWithLabelInGroup(label, inGroup,
            index));
        return button;
    }

    public STFBotButton buttonInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        button.setSwtBotButton(swtBot.buttonInGroup(mnemonicText, inGroup));
        return button;
    }

    public STFBotButton buttonInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        button.setSwtBotButton(swtBot.buttonInGroup(mnemonicText, inGroup,
            index));
        return button;
    }

    public STFBotButton buttonWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException {
        button.setSwtBotButton(swtBot
            .buttonWithTooltipInGroup(tooltip, inGroup));
        return button;
    }

    public STFBotButton buttonWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException {
        button.setSwtBotButton(swtBot.buttonWithTooltipInGroup(tooltip,
            inGroup, index));
        return button;
    }

    /**********************************************
     * 
     * Widget label
     * 
     **********************************************/
    public STFBotLabel label() throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.label());
        return stfLabel;
    }

    public STFBotLabel label(String mnemonicText) throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.label(mnemonicText));
        return stfLabel;

    }

    public STFBotLabel label(String mnemonicText, int index)
        throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.label(mnemonicText, index));
        return stfLabel;

    }

    public STFBotLabel labelWithId(String key, String value)
        throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.labelWithId(key, value));
        return stfLabel;

    }

    public STFBotLabel labelWithId(String key, String value, int index)
        throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.labelWithId(key, value, index));
        return stfLabel;

    }

    public STFBotLabel labelWithId(String value) throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.labelWithId(value));
        return stfLabel;

    }

    public STFBotLabel labelWithId(String value, int index)
        throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.labelWithId(value, index));
        return stfLabel;

    }

    public STFBotLabel labelInGroup(String inGroup) throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.labelInGroup(inGroup));
        return stfLabel;

    }

    public STFBotLabel labelInGroup(String inGroup, int index)
        throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.labelInGroup(inGroup, index));
        return stfLabel;

    }

    public STFBotLabel label(int index) throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.label(index));
        return stfLabel;

    }

    public STFBotLabel labelInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.labelInGroup(MENU_CLASS, inGroup));
        return stfLabel;

    }

    public STFBotLabel labelInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        stfLabel.setSwtBotLabel(swtBot.labelInGroup(mnemonicText, inGroup,
            index));
        return stfLabel;

    }

    public boolean existsLabel() throws RemoteException {
        try {
            swtBot.label();
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean existsLabel(String text) throws RemoteException {
        try {
            swtBot.label(text);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**********************************************
     * 
     * Widget styledText
     * 
     **********************************************/

    public STFBotStyledText styledTextWithLabel(String label)
        throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledTextWithLabel(label, 0));
        return stfStyledText;
    }

    public STFBotStyledText styledTextWithLabel(String label, int index)
        throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledTextWithLabel(label,
            index));
        return stfStyledText;
    }

    public STFBotStyledText styledText(String text) throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledText(text));
        return stfStyledText;
    }

    public STFBotStyledText styledText(String text, int index)
        throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledText(text, index));
        return stfStyledText;
    }

    public STFBotStyledText styledTextWithId(String key, String value)
        throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledTextWithId(key, value));
        return stfStyledText;
    }

    public STFBotStyledText styledTextWithId(String key, String value, int index)
        throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledTextWithId(key, value,
            index));
        return stfStyledText;
    }

    public STFBotStyledText styledTextWithId(String value)
        throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledTextWithId(value));
        return stfStyledText;
    }

    public STFBotStyledText styledTextWithId(String value, int index)
        throws RemoteException {
        stfStyledText
            .setSwtBotStyledText(swtBot.styledTextWithId(value, index));
        return stfStyledText;
    }

    public STFBotStyledText styledTextInGroup(String inGroup)
        throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledTextInGroup(inGroup));
        return stfStyledText;
    }

    public STFBotStyledText styledTextInGroup(String inGroup, int index)
        throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledTextInGroup(inGroup,
            index));
        return stfStyledText;
    }

    public STFBotStyledText styledText() throws RemoteException {
        return styledText(0);
    }

    public STFBotStyledText styledText(int index) throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledText(index));
        return stfStyledText;
    }

    public STFBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup) throws RemoteException {
        return styledTextWithLabelInGroup(label, inGroup, 0);
    }

    public STFBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledTextWithLabelInGroup(
            label, inGroup, index));
        return stfStyledText;
    }

    public STFBotStyledText styledTextInGroup(String text, String inGroup)
        throws RemoteException {
        return styledTextInGroup(text, inGroup, 0);
    }

    public STFBotStyledText styledTextInGroup(String text, String inGroup,
        int index) throws RemoteException {
        stfStyledText.setSwtBotStyledText(swtBot.styledTextInGroup(text,
            inGroup, index));
        return stfStyledText;
    }

    /**********************************************
     * 
     * Widget comboBox
     * 
     **********************************************/
    public STFBotCombo comboBoxWithLabel(String label) throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBoxWithLabel(label));
        return stfCombo;
    }

    public STFBotCombo comboBoxWithLabel(String label, int index)
        throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBoxWithLabel(label, index));
        return stfCombo;
    }

    public STFBotCombo comboBox(String text) throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBox(text));
        return stfCombo;
    }

    public STFBotCombo comboBox(String text, int index) throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBox(text, index));
        return stfCombo;
    }

    public STFBotCombo comboBoxWithId(String key, String value)
        throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBoxWithId(key, value));
        return stfCombo;
    }

    public STFBotCombo comboBoxWithId(String key, String value, int index)
        throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBoxWithId(key, value, index));
        return stfCombo;
    }

    public STFBotCombo comboBoxWithId(String value) throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBoxWithId(value));
        return stfCombo;
    }

    public STFBotCombo comboBoxWithId(String value, int index)
        throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBoxWithId(value, index));
        return stfCombo;
    }

    public STFBotCombo comboBoxInGroup(String inGroup) throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBoxInGroup(inGroup));
        return stfCombo;
    }

    public STFBotCombo comboBoxInGroup(String inGroup, int index)
        throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBoxInGroup(inGroup, index));
        return stfCombo;
    }

    public STFBotCombo comboBox() throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBox());
        return stfCombo;
    }

    public STFBotCombo comboBox(int index) throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBox(index));
        return stfCombo;
    }

    public STFBotCombo comboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        stfCombo
            .setSwtBotCombo(swtBot.comboBoxWithLabelInGroup(label, inGroup));
        return stfCombo;
    }

    public STFBotCombo comboBoxWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBoxWithLabelInGroup(label, inGroup,
            index));
        return stfCombo;
    }

    public STFBotCombo comboBoxInGroup(String text, String inGroup)
        throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBoxInGroup(text, inGroup));
        return stfCombo;
    }

    public STFBotCombo comboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException {
        stfCombo.setSwtBotCombo(swtBot.comboBoxInGroup(text, inGroup, index));
        return stfCombo;
    }

    public STFBotCCombo ccomboBox(String text) throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBox(text));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBox(String text, int index)
        throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBox(text, index));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBoxWithLabel(String label) throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBoxWithLabel(label));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBoxWithLabel(String label, int index)
        throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBoxWithLabel(label, index));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBoxWithId(String key, String value)
        throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBoxWithId(key, value));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBoxWithId(String key, String value, int index)
        throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBoxWithId(key, value, index));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBoxWithId(String value) throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBoxWithId(value));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBoxWithId(String value, int index)
        throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBoxWithId(value, index));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBoxInGroup(String inGroup) throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBoxInGroup(inGroup));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBoxInGroup(String inGroup, int index)
        throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBoxInGroup(inGroup, index));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBox() throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBox());
        return stfCCombo;
    }

    public STFBotCCombo ccomboBox(int index) throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBox(index));
        return stfCCombo;

    }

    public STFBotCCombo ccomboBoxInGroup(String text, String inGroup)
        throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBoxInGroup(text, inGroup));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException {
        stfCCombo
            .setSwtBotCCombo(swtBot.ccomboBoxInGroup(text, inGroup, index));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBoxWithLabelInGroup(label,
            inGroup));
        return stfCCombo;
    }

    public STFBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        stfCCombo.setSwtBotCCombo(swtBot.ccomboBoxWithLabelInGroup(label,
            inGroup, index));
        return stfCCombo;
    }

}
