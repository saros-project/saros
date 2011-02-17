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
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCComboImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotComboImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotLabelImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShellImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotStyledTextImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTableImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTextImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToolbarButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;

public class STFBotImp extends EclipseComponentImp implements STFBot {

    private static transient STFBotImp self;

    private static SWTBot swtBot;

    private static STFBotShellImp shell;
    private static STFBotButtonImp button;
    private static STFBotTreeImp tree;
    private static STFBotLabelImp label;
    private static STFBotStyledTextImp styledText;
    private static STFBotComboImp comboBox;
    private static STFBotCComboImp ccomboBox;
    private static STFBotToolbarButtonImp toolbarButton;
    private static STFBotTextImp text;
    private static STFBotTableImp table;

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
        tree = STFBotTreeImp.getInstance();
        label = STFBotLabelImp.getInstance();
        styledText = STFBotStyledTextImp.getInstance();
        comboBox = STFBotComboImp.getInstance();
        ccomboBox = STFBotCComboImp.getInstance();
        toolbarButton = STFBotToolbarButtonImp.getInstance();
        text = STFBotTextImp.getInstance();
        table = STFBotTableImp.getInstance();

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
        tree.setSWTBotTree(swtBot.tree());
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
        label.setSwtBotLabel(swtBot.label());
        return label;
    }

    public STFBotLabel label(String mnemonicText) throws RemoteException {
        label.setSwtBotLabel(swtBot.label(mnemonicText));
        return label;

    }

    public STFBotLabel label(String mnemonicText, int index)
        throws RemoteException {
        label.setSwtBotLabel(swtBot.label(mnemonicText, index));
        return label;

    }

    public STFBotLabel labelWithId(String key, String value)
        throws RemoteException {
        label.setSwtBotLabel(swtBot.labelWithId(key, value));
        return label;

    }

    public STFBotLabel labelWithId(String key, String value, int index)
        throws RemoteException {
        label.setSwtBotLabel(swtBot.labelWithId(key, value, index));
        return label;

    }

    public STFBotLabel labelWithId(String value) throws RemoteException {
        label.setSwtBotLabel(swtBot.labelWithId(value));
        return label;

    }

    public STFBotLabel labelWithId(String value, int index)
        throws RemoteException {
        label.setSwtBotLabel(swtBot.labelWithId(value, index));
        return label;

    }

    public STFBotLabel labelInGroup(String inGroup) throws RemoteException {
        label.setSwtBotLabel(swtBot.labelInGroup(inGroup));
        return label;

    }

    public STFBotLabel labelInGroup(String inGroup, int index)
        throws RemoteException {
        label.setSwtBotLabel(swtBot.labelInGroup(inGroup, index));
        return label;

    }

    public STFBotLabel label(int index) throws RemoteException {
        label.setSwtBotLabel(swtBot.label(index));
        return label;

    }

    public STFBotLabel labelInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        label.setSwtBotLabel(swtBot.labelInGroup(MENU_CLASS, inGroup));
        return label;

    }

    public STFBotLabel labelInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        label.setSwtBotLabel(swtBot.labelInGroup(mnemonicText, inGroup, index));
        return label;

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
        styledText.setSwtBotStyledText(swtBot.styledTextWithLabel(label, 0));
        return styledText;
    }

    public STFBotStyledText styledTextWithLabel(String label, int index)
        throws RemoteException {
        styledText
            .setSwtBotStyledText(swtBot.styledTextWithLabel(label, index));
        return styledText;
    }

    public STFBotStyledText styledText(String text) throws RemoteException {
        styledText.setSwtBotStyledText(swtBot.styledText(text));
        return styledText;
    }

    public STFBotStyledText styledText(String text, int index)
        throws RemoteException {
        styledText.setSwtBotStyledText(swtBot.styledText(text, index));
        return styledText;
    }

    public STFBotStyledText styledTextWithId(String key, String value)
        throws RemoteException {
        styledText.setSwtBotStyledText(swtBot.styledTextWithId(key, value));
        return styledText;
    }

    public STFBotStyledText styledTextWithId(String key, String value, int index)
        throws RemoteException {
        styledText.setSwtBotStyledText(swtBot.styledTextWithId(key, value,
            index));
        return styledText;
    }

    public STFBotStyledText styledTextWithId(String value)
        throws RemoteException {
        styledText.setSwtBotStyledText(swtBot.styledTextWithId(value));
        return styledText;
    }

    public STFBotStyledText styledTextWithId(String value, int index)
        throws RemoteException {
        styledText.setSwtBotStyledText(swtBot.styledTextWithId(value, index));
        return styledText;
    }

    public STFBotStyledText styledTextInGroup(String inGroup)
        throws RemoteException {
        styledText.setSwtBotStyledText(swtBot.styledTextInGroup(inGroup));
        return styledText;
    }

    public STFBotStyledText styledTextInGroup(String inGroup, int index)
        throws RemoteException {
        styledText
            .setSwtBotStyledText(swtBot.styledTextInGroup(inGroup, index));
        return styledText;
    }

    public STFBotStyledText styledText() throws RemoteException {
        return styledText(0);
    }

    public STFBotStyledText styledText(int index) throws RemoteException {
        styledText.setSwtBotStyledText(swtBot.styledText(index));
        return styledText;
    }

    public STFBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup) throws RemoteException {
        return styledTextWithLabelInGroup(label, inGroup, 0);
    }

    public STFBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException {
        styledText.setSwtBotStyledText(swtBot.styledTextWithLabelInGroup(label,
            inGroup, index));
        return styledText;
    }

    public STFBotStyledText styledTextInGroup(String text, String inGroup)
        throws RemoteException {
        return styledTextInGroup(text, inGroup, 0);
    }

    public STFBotStyledText styledTextInGroup(String text, String inGroup,
        int index) throws RemoteException {
        styledText.setSwtBotStyledText(swtBot.styledTextInGroup(text, inGroup,
            index));
        return styledText;
    }

    /**********************************************
     * 
     * Widget comboBox
     * 
     **********************************************/
    public STFBotCombo comboBoxWithLabel(String label) throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBoxWithLabel(label));
        return comboBox;
    }

    public STFBotCombo comboBoxWithLabel(String label, int index)
        throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBoxWithLabel(label, index));
        return comboBox;
    }

    public STFBotCombo comboBox(String text) throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBox(text));
        return comboBox;
    }

    public STFBotCombo comboBox(String text, int index) throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBox(text, index));
        return comboBox;
    }

    public STFBotCombo comboBoxWithId(String key, String value)
        throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBoxWithId(key, value));
        return comboBox;
    }

    public STFBotCombo comboBoxWithId(String key, String value, int index)
        throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBoxWithId(key, value, index));
        return comboBox;
    }

    public STFBotCombo comboBoxWithId(String value) throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBoxWithId(value));
        return comboBox;
    }

    public STFBotCombo comboBoxWithId(String value, int index)
        throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBoxWithId(value, index));
        return comboBox;
    }

    public STFBotCombo comboBoxInGroup(String inGroup) throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBoxInGroup(inGroup));
        return comboBox;
    }

    public STFBotCombo comboBoxInGroup(String inGroup, int index)
        throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBoxInGroup(inGroup, index));
        return comboBox;
    }

    public STFBotCombo comboBox() throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBox());
        return comboBox;
    }

    public STFBotCombo comboBox(int index) throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBox(index));
        return comboBox;
    }

    public STFBotCombo comboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        comboBox
            .setSwtBotCombo(swtBot.comboBoxWithLabelInGroup(label, inGroup));
        return comboBox;
    }

    public STFBotCombo comboBoxWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBoxWithLabelInGroup(label, inGroup,
            index));
        return comboBox;
    }

    public STFBotCombo comboBoxInGroup(String text, String inGroup)
        throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBoxInGroup(text, inGroup));
        return comboBox;
    }

    public STFBotCombo comboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException {
        comboBox.setSwtBotCombo(swtBot.comboBoxInGroup(text, inGroup, index));
        return comboBox;
    }

    /**********************************************
     * 
     * Widget ccomboBox
     * 
     **********************************************/
    public STFBotCCombo ccomboBox(String text) throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBox(text));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBox(String text, int index)
        throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBox(text, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithLabel(String label) throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBoxWithLabel(label));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithLabel(String label, int index)
        throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBoxWithLabel(label, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithId(String key, String value)
        throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBoxWithId(key, value));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithId(String key, String value, int index)
        throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBoxWithId(key, value, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithId(String value) throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBoxWithId(value));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithId(String value, int index)
        throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBoxWithId(value, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxInGroup(String inGroup) throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBoxInGroup(inGroup));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxInGroup(String inGroup, int index)
        throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBoxInGroup(inGroup, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBox() throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBox());
        return ccomboBox;
    }

    public STFBotCCombo ccomboBox(int index) throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBox(index));
        return ccomboBox;

    }

    public STFBotCCombo ccomboBoxInGroup(String text, String inGroup)
        throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBoxInGroup(text, inGroup));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException {
        ccomboBox
            .setSwtBotCCombo(swtBot.ccomboBoxInGroup(text, inGroup, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBoxWithLabelInGroup(label,
            inGroup));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        ccomboBox.setSwtBotCCombo(swtBot.ccomboBoxWithLabelInGroup(label,
            inGroup, index));
        return ccomboBox;
    }

    /**********************************************
     * 
     * Widget toolbarButton
     * 
     **********************************************/
    public STFBotToolbarButton toolbarButton() throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot.toolbarButton());
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButton(int index) throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot.toolbarButton(index));
        return toolbarButton;
    }

    public boolean existsToolbarButton() throws RemoteException {
        try {
            swtBot.toolbarButton();
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public STFBotToolbarButton toolbarButton(String mnemonicText)
        throws RemoteException {
        toolbarButton
            .setSwtBotToolbarButton(swtBot.toolbarButton(mnemonicText));
        return toolbarButton;

    }

    public STFBotToolbarButton toolbarButton(String mnemonicText, int index)
        throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot.toolbarButton(mnemonicText,
            index));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonWithTooltip(String tooltip)
        throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot
            .toolbarButtonWithTooltip(tooltip));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonWithTooltip(String tooltip,
        int index) throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot.toolbarButtonWithTooltip(
            tooltip, index));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonWithId(String key, String value)
        throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot.toolbarButtonWithId(key,
            value));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonWithId(String key, String value,
        int index) throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot.toolbarButtonWithId(key,
            value, index));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonWithId(String value)
        throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot.toolbarButtonWithId(value));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonWithId(String value, int index)
        throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot.toolbarButtonWithId(value,
            index));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonInGroup(String inGroup)
        throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot
            .toolbarButtonInGroup(inGroup));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonInGroup(String inGroup, int index)
        throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot.toolbarButtonInGroup(
            inGroup, index));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonInGroup(String mnemonicText,
        String inGroup) throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot.toolbarButtonInGroup(
            mnemonicText, inGroup));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonInGroup(String mnemonicText,
        String inGroup, int index) throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot.toolbarButtonInGroup(
            mnemonicText, inGroup, index));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot
            .toolbarButtonWithTooltipInGroup(tooltip, inGroup));
        return toolbarButton;
    }

    public STFBotToolbarButton toolbarButtonWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException {
        toolbarButton.setSwtBotToolbarButton(swtBot
            .toolbarButtonWithTooltipInGroup(tooltip, inGroup, index));
        return toolbarButton;
    }

    /**********************************************
     * 
     * Widget text
     * 
     **********************************************/
    public STFBotText textWithLabel(String label) throws RemoteException {
        text.setSwtBotText(swtBot.textWithLabel(label));
        return text;
    }

    public STFBotText textWithLabel(String label, int index)
        throws RemoteException {
        text.setSwtBotText(swtBot.textWithLabel(label, index));
        return text;
    }

    public STFBotText text(String txt) throws RemoteException {
        text.setSwtBotText(swtBot.text(txt));
        return text;
    }

    public STFBotText text(String txt, int index) throws RemoteException {
        text.setSwtBotText(swtBot.text(txt, index));
        return text;

    }

    public STFBotText textWithTooltip(String tooltip) throws RemoteException {
        text.setSwtBotText(swtBot.textWithTooltip(tooltip));
        return text;

    }

    public STFBotText textWithTooltip(String tooltip, int index)
        throws RemoteException {
        text.setSwtBotText(swtBot.textWithTooltip(tooltip, index));
        return text;

    }

    public STFBotText textWithMessage(String message) throws RemoteException {
        text.setSwtBotText(swtBot.textWithMessage(message));
        return text;

    }

    public STFBotText textWithMessage(String message, int index)
        throws RemoteException {
        text.setSwtBotText(swtBot.textWithMessage(message, index));
        return text;

    }

    public STFBotText textWithId(String key, String value)
        throws RemoteException {
        text.setSwtBotText(swtBot.textWithId(key, value));
        return text;

    }

    public STFBotText textWithId(String key, String value, int index)
        throws RemoteException {
        text.setSwtBotText(swtBot.textWithId(key, value, index));
        return text;

    }

    public STFBotText textWithId(String value) throws RemoteException {
        text.setSwtBotText(swtBot.textWithId(value));
        return text;

    }

    public STFBotText textWithId(String value, int index)
        throws RemoteException {
        text.setSwtBotText(swtBot.textWithId(value, index));
        return text;

    }

    public STFBotText textInGroup(String inGroup) throws RemoteException {
        text.setSwtBotText(swtBot.textInGroup(inGroup));
        return text;

    }

    public STFBotText textInGroup(String inGroup, int index)
        throws RemoteException {
        text.setSwtBotText(swtBot.textInGroup(inGroup, index));
        return text;

    }

    public STFBotText text() throws RemoteException {
        text.setSwtBotText(swtBot.text());
        return text;

    }

    public STFBotText text(int index) throws RemoteException {
        text.setSwtBotText(swtBot.text(index));
        return text;

    }

    public STFBotText textWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        text.setSwtBotText(swtBot.textWithLabel(label));
        return text;

    }

    public STFBotText textWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        text.setSwtBotText(swtBot.textWithLabelInGroup(label, inGroup, index));
        return text;

    }

    public STFBotText textInGroup(String txt, String inGroup)
        throws RemoteException {
        text.setSwtBotText(swtBot.textInGroup(txt, inGroup));
        return text;

    }

    public STFBotText textInGroup(String txt, String inGroup, int index)
        throws RemoteException {
        text.setSwtBotText(swtBot.textInGroup(txt, inGroup, index));
        return text;

    }

    public STFBotText textWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException {
        text.setSwtBotText(swtBot.textWithTooltipInGroup(tooltip, inGroup));
        return text;

    }

    /**********************************************
     * 
     * Widget table
     * 
     **********************************************/
    public STFBotText textWithTooltipInGroup(String tooltip, String inGroup,
        int index) throws RemoteException {
        text.setSwtBotText(swtBot.textWithTooltipInGroup(tooltip, inGroup,
            index));
        return text;
    }

    public STFBotTable tableWithLabel(String label) throws RemoteException {
        table.setSwtBotTable(swtBot.tableWithLabel(label));
        return table;
    }

    public STFBotTable tableWithLabel(String label, int index)
        throws RemoteException {
        table.setSwtBotTable(swtBot.tableWithLabel(label, index));
        return table;
    }

    public STFBotTable tableWithId(String key, String value)
        throws RemoteException {
        table.setSwtBotTable(swtBot.tableWithId(key, value));
        return table;
    }

    public STFBotTable tableWithId(String key, String value, int index)
        throws RemoteException {
        table.setSwtBotTable(swtBot.tableWithId(key, value, index));
        return table;
    }

    public STFBotTable tableWithId(String value) throws RemoteException {
        table.setSwtBotTable(swtBot.tableWithId(value));
        return table;
    }

    public STFBotTable tableWithId(String value, int index)
        throws RemoteException {
        table.setSwtBotTable(swtBot.tableWithId(value, index));
        return table;
    }

    public STFBotTable tableInGroup(String inGroup) throws RemoteException {
        table.setSwtBotTable(swtBot.tableInGroup(inGroup));
        return table;
    }

    public STFBotTable tableInGroup(String inGroup, int index)
        throws RemoteException {
        table.setSwtBotTable(swtBot.tableInGroup(inGroup, index));
        return table;
    }

    public STFBotTable table() throws RemoteException {
        table.setSwtBotTable(swtBot.table());
        return table;
    }

    public STFBotTable table(int index) throws RemoteException {
        table.setSwtBotTable(swtBot.table(index));
        return table;
    }

    public STFBotTable tableWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        table.setSwtBotTable(swtBot.tableWithLabelInGroup(label, inGroup));
        return table;
    }

}
