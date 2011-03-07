package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotCCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotCComboImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotCheckBox;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotCheckBoxImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotComboImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotLabelImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotList;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotListImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotMenuImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotRadio;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotRadioImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotShellImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotStyledTextImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotTableImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotTextImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotToggleButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotToggleButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotToolbarButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotTreeImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.ChatViewImp;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.SarosSWTBot;

public class RemoteBotImp extends STF implements RemoteBot {

    private static transient RemoteBotImp self;

    private static SWTBot swtBot;

    private static RemoteBotShellImp shell;
    private static RemoteBotButtonImp button;
    private static RemoteBotTreeImp tree;
    private static RemoteBotLabelImp label;
    private static RemoteBotStyledTextImp styledText;
    private static RemoteBotComboImp comboBox;
    private static RemoteBotCComboImp ccomboBox;
    private static RemoteBotToolbarButtonImp toolbarButton;
    private static RemoteBotTextImp text;
    private static RemoteBotTableImp table;
    private static RemoteBotMenuImp menu;
    private static RemoteBotListImp list;
    private static RemoteBotCheckBoxImp checkbox;
    private static RemoteBotRadioImp radio;
    private static RemoteBotToggleButtonImp toggleButton;

    /**
     * {@link ChatViewImp} is a singleton, but inheritance is possible.
     */
    public static RemoteBotImp getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotImp();
        swtBot = SarosSWTBot.getInstance();

        shell = RemoteBotShellImp.getInstance();
        button = RemoteBotButtonImp.getInstance();
        tree = RemoteBotTreeImp.getInstance();
        label = RemoteBotLabelImp.getInstance();
        styledText = RemoteBotStyledTextImp.getInstance();
        comboBox = RemoteBotComboImp.getInstance();
        ccomboBox = RemoteBotCComboImp.getInstance();
        toolbarButton = RemoteBotToolbarButtonImp.getInstance();
        text = RemoteBotTextImp.getInstance();
        table = RemoteBotTableImp.getInstance();
        menu = RemoteBotMenuImp.getInstance();
        list = RemoteBotListImp.getInstance();
        checkbox = RemoteBotCheckBoxImp.getInstance();
        radio = RemoteBotRadioImp.getInstance();

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

    public RemoteBotTree tree() throws RemoteException {
        tree.setWidget(swtBot.tree());
        return tree;
    }

    public RemoteBotTree treeWithLabel(String label) throws RemoteException {
        tree.setWidget(swtBot.treeWithLabel(label));
        return tree;
    }

    public RemoteBotTree treeWithLabel(String label, int index)
        throws RemoteException {
        tree.setWidget(swtBot.treeWithLabel(label, index));
        return tree;
    }

    public RemoteBotTree treeWithId(String key, String value)
        throws RemoteException {
        tree.setWidget(swtBot.treeWithId(key, value));
        return tree;
    }

    public RemoteBotTree treeWithId(String key, String value, int index)
        throws RemoteException {
        tree.setWidget(swtBot.treeWithId(key, value, index));
        return tree;
    }

    public RemoteBotTree treeWithId(String value) throws RemoteException {
        tree.setWidget(swtBot.treeWithId(value));
        return tree;
    }

    public RemoteBotTree treeWithId(String value, int index)
        throws RemoteException {
        tree.setWidget(swtBot.treeWithId(value, index));
        return tree;
    }

    public RemoteBotTree treeInGroup(String inGroup) throws RemoteException {
        tree.setWidget(swtBot.treeInGroup(inGroup));
        return tree;
    }

    public RemoteBotTree treeInGroup(String inGroup, int index)
        throws RemoteException {
        tree.setWidget(swtBot.treeInGroup(inGroup, index));
        return tree;
    }

    public RemoteBotTree tree(int index) throws RemoteException {
        tree.setWidget(swtBot.tree(index));
        return tree;
    }

    public RemoteBotTree treeWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        tree.setWidget(swtBot.treeWithLabelInGroup(label, inGroup));
        return tree;
    }

    public RemoteBotTree treeWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        tree.setWidget(swtBot.treeWithLabelInGroup(label, inGroup, index));
        return tree;
    }

    /**********************************************
     * 
     * Widget shell
     * 
     **********************************************/

    public RemoteBotShell shell(String title) throws RemoteException {
        return shell.setWidget(swtBot.shell(title));

    }

    public List<String> getTitlesOfOpenedShells() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotShell shell : swtBot.shells())
            list.add(shell.getText());
        return list;
    }

    public boolean isShellOpen(String title) throws RemoteException {
        // try {
        // swtBot.shell(title);
        // return true;
        // } catch (WidgetNotFoundException e) {
        // return false;
        // }
        return getTitlesOfOpenedShells().contains(title);
    }

    public void waitUntilShellIsClosed(final String title)
        throws RemoteException {
        swtBot.waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isShellOpen(title);
            }

            public String getFailureMessage() {
                return null;
            }
        });
        swtBot.sleep(10);
    }

    public void waitUntilShellIsOpen(final String title) throws RemoteException {
        swtBot.waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isShellOpen(title);
            }

            public String getFailureMessage() {
                return null;
            }
        });
    }

    public void waitLongUntilShellIsOpen(final String title)
        throws RemoteException {
        waitLongUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isShellOpen(title);
            }

            public String getFailureMessage() {
                return null;
            }
        });
    }

    public RemoteBotShell activeShell() throws RemoteException {
        return shell.setWidget(swtBot.activeShell());

    }

    public String getTextOfActiveShell() throws RemoteException {
        final SWTBotShell activeShell = swtBot.activeShell();
        return activeShell == null ? null : activeShell.getText();
    }

    /**********************************************
     * 
     * Widget button
     * 
     **********************************************/

    public RemoteBotButton buttonWithLabel(String label) throws RemoteException {
        button.setWidget(swtBot.buttonWithLabel(label, 0));
        return button;
    }

    public RemoteBotButton buttonWithLabel(String label, int index)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithLabel(label, index));
        return button;
    }

    public RemoteBotButton button(String mnemonicText) throws RemoteException {
        button.setWidget(swtBot.button(mnemonicText, 0));
        return button;
    }

    public RemoteBotButton button(String mnemonicText, int index)
        throws RemoteException {
        button.setWidget(swtBot.button(mnemonicText, index));
        return button;
    }

    public RemoteBotButton buttonWithTooltip(String tooltip)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithTooltip(tooltip, 0));
        return button;
    }

    public RemoteBotButton buttonWithTooltip(String tooltip, int index)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithTooltip(tooltip, index));
        return button;
    }

    public RemoteBotButton buttonWithId(String key, String value)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithId(key, value));
        return button;
    }

    public RemoteBotButton buttonWithId(String key, String value, int index)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithId(key, value, index));
        return button;
    }

    public RemoteBotButton buttonWithId(String value) throws RemoteException {
        button.setWidget(swtBot.buttonWithId(value));
        return button;
    }

    public RemoteBotButton buttonWithId(String value, int index)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithId(value, index));
        return button;
    }

    public RemoteBotButton buttonInGroup(String inGroup) throws RemoteException {
        button.setWidget(swtBot.buttonInGroup(inGroup));
        return button;
    }

    public RemoteBotButton buttonInGroup(String inGroup, int index)
        throws RemoteException {
        button.setWidget(swtBot.buttonInGroup(inGroup, index));
        return button;
    }

    public RemoteBotButton button() throws RemoteException {
        button.setWidget(swtBot.button());
        return button;
    }

    public RemoteBotButton button(int index) throws RemoteException {
        button.setWidget(swtBot.button(index));
        return button;
    }

    public RemoteBotButton buttonWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithLabelInGroup(label, inGroup));
        return button;
    }

    public RemoteBotButton buttonWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        button.setWidget(swtBot.buttonWithLabelInGroup(label, inGroup, index));
        return button;
    }

    public RemoteBotButton buttonInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        button.setWidget(swtBot.buttonInGroup(mnemonicText, inGroup));
        return button;
    }

    public RemoteBotButton buttonInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        button.setWidget(swtBot.buttonInGroup(mnemonicText, inGroup, index));
        return button;
    }

    public RemoteBotButton buttonWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithTooltipInGroup(tooltip, inGroup));
        return button;
    }

    public RemoteBotButton buttonWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException {
        button.setWidget(swtBot.buttonWithTooltipInGroup(tooltip, inGroup,
            index));
        return button;
    }

    /**********************************************
     * 
     * Widget label
     * 
     **********************************************/
    public RemoteBotLabel label() throws RemoteException {
        label.setWidget(swtBot.label());
        return label;
    }

    public RemoteBotLabel label(String mnemonicText) throws RemoteException {
        label.setWidget(swtBot.label(mnemonicText));
        return label;

    }

    public RemoteBotLabel label(String mnemonicText, int index)
        throws RemoteException {
        label.setWidget(swtBot.label(mnemonicText, index));
        return label;

    }

    public RemoteBotLabel labelWithId(String key, String value)
        throws RemoteException {
        label.setWidget(swtBot.labelWithId(key, value));
        return label;

    }

    public RemoteBotLabel labelWithId(String key, String value, int index)
        throws RemoteException {
        label.setWidget(swtBot.labelWithId(key, value, index));
        return label;

    }

    public RemoteBotLabel labelWithId(String value) throws RemoteException {
        label.setWidget(swtBot.labelWithId(value));
        return label;

    }

    public RemoteBotLabel labelWithId(String value, int index)
        throws RemoteException {
        label.setWidget(swtBot.labelWithId(value, index));
        return label;

    }

    public RemoteBotLabel labelInGroup(String inGroup) throws RemoteException {
        label.setWidget(swtBot.labelInGroup(inGroup));
        return label;

    }

    public RemoteBotLabel labelInGroup(String inGroup, int index)
        throws RemoteException {
        label.setWidget(swtBot.labelInGroup(inGroup, index));
        return label;

    }

    public RemoteBotLabel label(int index) throws RemoteException {
        label.setWidget(swtBot.label(index));
        return label;

    }

    public RemoteBotLabel labelInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        label.setWidget(swtBot.labelInGroup(MENU_CLASS, inGroup));
        return label;

    }

    public RemoteBotLabel labelInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        label.setWidget(swtBot.labelInGroup(mnemonicText, inGroup, index));
        return label;

    }

    public boolean existsStyledText() throws RemoteException {
        long oldTimeout = SWTBotPreferences.TIMEOUT;
        // increase the timeout
        SWTBotPreferences.TIMEOUT = 1000;

        try {
            swtBot.styledText();
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return true;
        } catch (WidgetNotFoundException e) {
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return false;
        }
    }

    public boolean existsLabel() throws RemoteException {
        long oldTimeout = SWTBotPreferences.TIMEOUT;
        // increase the timeout
        SWTBotPreferences.TIMEOUT = 1000;

        try {
            swtBot.label();
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return true;
        } catch (WidgetNotFoundException e) {
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return false;
        }
    }

    public boolean existsLabel(String text) throws RemoteException {
        long oldTimeout = SWTBotPreferences.TIMEOUT;
        // increase the timeout
        SWTBotPreferences.TIMEOUT = 1000;

        try {
            swtBot.label(text);
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return true;
        } catch (WidgetNotFoundException e) {
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return false;
        }
    }

    /**********************************************
     * 
     * Widget styledText
     * 
     **********************************************/

    public RemoteBotStyledText styledTextWithLabel(String label)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithLabel(label, 0));
        return styledText;
    }

    public RemoteBotStyledText styledTextWithLabel(String label, int index)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithLabel(label, index));
        return styledText;
    }

    public RemoteBotStyledText styledText(String text) throws RemoteException {
        styledText.setWidget(swtBot.styledText(text));
        return styledText;
    }

    public RemoteBotStyledText styledText(String text, int index)
        throws RemoteException {
        styledText.setWidget(swtBot.styledText(text, index));
        return styledText;
    }

    public RemoteBotStyledText styledTextWithId(String key, String value)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithId(key, value));
        return styledText;
    }

    public RemoteBotStyledText styledTextWithId(String key, String value, int index)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithId(key, value, index));
        return styledText;
    }

    public RemoteBotStyledText styledTextWithId(String value)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithId(value));
        return styledText;
    }

    public RemoteBotStyledText styledTextWithId(String value, int index)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithId(value, index));
        return styledText;
    }

    public RemoteBotStyledText styledTextInGroup(String inGroup)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextInGroup(inGroup));
        return styledText;
    }

    public RemoteBotStyledText styledTextInGroup(String inGroup, int index)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextInGroup(inGroup, index));
        return styledText;
    }

    public RemoteBotStyledText styledText() throws RemoteException {
        return styledText(0);
    }

    public RemoteBotStyledText styledText(int index) throws RemoteException {
        styledText.setWidget(swtBot.styledText(index));
        return styledText;
    }

    public RemoteBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup) throws RemoteException {
        return styledTextWithLabelInGroup(label, inGroup, 0);
    }

    public RemoteBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithLabelInGroup(label, inGroup,
            index));
        return styledText;
    }

    public RemoteBotStyledText styledTextInGroup(String text, String inGroup)
        throws RemoteException {
        return styledTextInGroup(text, inGroup, 0);
    }

    public RemoteBotStyledText styledTextInGroup(String text, String inGroup,
        int index) throws RemoteException {
        styledText.setWidget(swtBot.styledTextInGroup(text, inGroup, index));
        return styledText;
    }

    /**********************************************
     * 
     * Widget comboBox
     * 
     **********************************************/
    public RemoteBotCombo comboBoxWithLabel(String label) throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithLabel(label));
        return comboBox;
    }

    public RemoteBotCombo comboBoxWithLabel(String label, int index)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithLabel(label, index));
        return comboBox;
    }

    public RemoteBotCombo comboBox(String text) throws RemoteException {
        comboBox.setWidget(swtBot.comboBox(text));
        return comboBox;
    }

    public RemoteBotCombo comboBox(String text, int index) throws RemoteException {
        comboBox.setWidget(swtBot.comboBox(text, index));
        return comboBox;
    }

    public RemoteBotCombo comboBoxWithId(String key, String value)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithId(key, value));
        return comboBox;
    }

    public RemoteBotCombo comboBoxWithId(String key, String value, int index)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithId(key, value, index));
        return comboBox;
    }

    public RemoteBotCombo comboBoxWithId(String value) throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithId(value));
        return comboBox;
    }

    public RemoteBotCombo comboBoxWithId(String value, int index)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithId(value, index));
        return comboBox;
    }

    public RemoteBotCombo comboBoxInGroup(String inGroup) throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxInGroup(inGroup));
        return comboBox;
    }

    public RemoteBotCombo comboBoxInGroup(String inGroup, int index)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxInGroup(inGroup, index));
        return comboBox;
    }

    public RemoteBotCombo comboBox() throws RemoteException {
        comboBox.setWidget(swtBot.comboBox());
        return comboBox;
    }

    public RemoteBotCombo comboBox(int index) throws RemoteException {
        comboBox.setWidget(swtBot.comboBox(index));
        return comboBox;
    }

    public RemoteBotCombo comboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithLabelInGroup(label, inGroup));
        return comboBox;
    }

    public RemoteBotCombo comboBoxWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithLabelInGroup(label, inGroup,
            index));
        return comboBox;
    }

    public RemoteBotCombo comboBoxInGroup(String text, String inGroup)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxInGroup(text, inGroup));
        return comboBox;
    }

    public RemoteBotCombo comboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxInGroup(text, inGroup, index));
        return comboBox;
    }

    /**********************************************
     * 
     * Widget ccomboBox
     * 
     **********************************************/
    public RemoteBotCCombo ccomboBox(String text) throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBox(text));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBox(String text, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBox(text, index));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBoxWithLabel(String label) throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithLabel(label));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBoxWithLabel(String label, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithLabel(label, index));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBoxWithId(String key, String value)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithId(key, value));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBoxWithId(String key, String value, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithId(key, value, index));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBoxWithId(String value) throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithId(value));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBoxWithId(String value, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithId(value, index));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBoxInGroup(String inGroup) throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxInGroup(inGroup));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBoxInGroup(String inGroup, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxInGroup(inGroup, index));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBox() throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBox());
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBox(int index) throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBox(index));
        return ccomboBox;

    }

    public RemoteBotCCombo ccomboBoxInGroup(String text, String inGroup)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxInGroup(text, inGroup));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxInGroup(text, inGroup, index));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithLabelInGroup(label, inGroup));
        return ccomboBox;
    }

    public RemoteBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithLabelInGroup(label, inGroup,
            index));
        return ccomboBox;
    }

    /**********************************************
     * 
     * Widget toolbarButton
     * 
     **********************************************/
    public RemoteBotToolbarButton toolbarButton() throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButton());

    }

    public RemoteBotToolbarButton toolbarButton(int index) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButton(index));

    }

    public boolean existsToolbarButton() throws RemoteException {
        long oldTimeout = SWTBotPreferences.TIMEOUT;
        // increase the timeout
        SWTBotPreferences.TIMEOUT = 1000;

        try {
            swtBot.toolbarButton();
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return true;

        } catch (WidgetNotFoundException e) {
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return false;

        }
    }

    public RemoteBotToolbarButton toolbarButton(String mnemonicText)
        throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButton(mnemonicText));

    }

    public RemoteBotToolbarButton toolbarButton(String mnemonicText, int index)
        throws RemoteException {
        return toolbarButton.setWidget(swtBot
            .toolbarButton(mnemonicText, index));

    }

    public RemoteBotToolbarButton toolbarButtonWithTooltip(String tooltip)
        throws RemoteException {
        return toolbarButton
            .setWidget(swtBot.toolbarButtonWithTooltip(tooltip));

    }

    public RemoteBotToolbarButton toolbarButtonWithTooltip(String tooltip,
        int index) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithTooltip(tooltip,
            index));

    }

    public RemoteBotToolbarButton toolbarButtonWithId(String key, String value)
        throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithId(key, value));

    }

    public RemoteBotToolbarButton toolbarButtonWithId(String key, String value,
        int index) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithId(key, value,
            index));

    }

    public RemoteBotToolbarButton toolbarButtonWithId(String value)
        throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithId(value));

    }

    public RemoteBotToolbarButton toolbarButtonWithId(String value, int index)
        throws RemoteException {
        return toolbarButton
            .setWidget(swtBot.toolbarButtonWithId(value, index));

    }

    public RemoteBotToolbarButton toolbarButtonInGroup(String inGroup)
        throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonInGroup(inGroup));

    }

    public RemoteBotToolbarButton toolbarButtonInGroup(String inGroup, int index)
        throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonInGroup(inGroup,
            index));

    }

    public RemoteBotToolbarButton toolbarButtonInGroup(String mnemonicText,
        String inGroup) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonInGroup(
            mnemonicText, inGroup));

    }

    public RemoteBotToolbarButton toolbarButtonInGroup(String mnemonicText,
        String inGroup, int index) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonInGroup(
            mnemonicText, inGroup, index));

    }

    public RemoteBotToolbarButton toolbarButtonWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithTooltipInGroup(
            tooltip, inGroup));

    }

    public RemoteBotToolbarButton toolbarButtonWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithTooltipInGroup(
            tooltip, inGroup, index));

    }

    /**********************************************
     * 
     * Widget text
     * 
     **********************************************/
    public RemoteBotText textWithLabel(String label) throws RemoteException {
        text.setWidget(swtBot.textWithLabel(label));
        return text;
    }

    public RemoteBotText textWithLabel(String label, int index)
        throws RemoteException {
        text.setWidget(swtBot.textWithLabel(label, index));
        return text;
    }

    public RemoteBotText text(String txt) throws RemoteException {
        text.setWidget(swtBot.text(txt));
        return text;
    }

    public RemoteBotText text(String txt, int index) throws RemoteException {
        text.setWidget(swtBot.text(txt, index));
        return text;

    }

    public RemoteBotText textWithTooltip(String tooltip) throws RemoteException {
        text.setWidget(swtBot.textWithTooltip(tooltip));
        return text;

    }

    public RemoteBotText textWithTooltip(String tooltip, int index)
        throws RemoteException {
        text.setWidget(swtBot.textWithTooltip(tooltip, index));
        return text;

    }

    public RemoteBotText textWithMessage(String message) throws RemoteException {
        text.setWidget(swtBot.textWithMessage(message));
        return text;

    }

    public RemoteBotText textWithMessage(String message, int index)
        throws RemoteException {
        text.setWidget(swtBot.textWithMessage(message, index));
        return text;

    }

    public RemoteBotText textWithId(String key, String value)
        throws RemoteException {
        text.setWidget(swtBot.textWithId(key, value));
        return text;

    }

    public RemoteBotText textWithId(String key, String value, int index)
        throws RemoteException {
        text.setWidget(swtBot.textWithId(key, value, index));
        return text;

    }

    public RemoteBotText textWithId(String value) throws RemoteException {
        text.setWidget(swtBot.textWithId(value));
        return text;

    }

    public RemoteBotText textWithId(String value, int index)
        throws RemoteException {
        text.setWidget(swtBot.textWithId(value, index));
        return text;

    }

    public RemoteBotText textInGroup(String inGroup) throws RemoteException {
        text.setWidget(swtBot.textInGroup(inGroup));
        return text;

    }

    public RemoteBotText textInGroup(String inGroup, int index)
        throws RemoteException {
        text.setWidget(swtBot.textInGroup(inGroup, index));
        return text;

    }

    public RemoteBotText text() throws RemoteException {
        text.setWidget(swtBot.text());
        return text;

    }

    public RemoteBotText text(int index) throws RemoteException {
        text.setWidget(swtBot.text(index));
        return text;

    }

    public RemoteBotText textWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        text.setWidget(swtBot.textWithLabel(label));
        return text;

    }

    public RemoteBotText textWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        text.setWidget(swtBot.textWithLabelInGroup(label, inGroup, index));
        return text;

    }

    public RemoteBotText textInGroup(String txt, String inGroup)
        throws RemoteException {
        text.setWidget(swtBot.textInGroup(txt, inGroup));
        return text;

    }

    public RemoteBotText textInGroup(String txt, String inGroup, int index)
        throws RemoteException {
        text.setWidget(swtBot.textInGroup(txt, inGroup, index));
        return text;

    }

    public RemoteBotText textWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException {
        text.setWidget(swtBot.textWithTooltipInGroup(tooltip, inGroup));
        return text;

    }

    public RemoteBotText textWithTooltipInGroup(String tooltip, String inGroup,
        int index) throws RemoteException {
        text.setWidget(swtBot.textWithTooltipInGroup(tooltip, inGroup, index));
        return text;
    }

    /**********************************************
     * 
     * Widget table
     * 
     **********************************************/

    public boolean existsTable() throws RemoteException {
        long oldTimeout = SWTBotPreferences.TIMEOUT;
        // increase the timeout
        SWTBotPreferences.TIMEOUT = 1000;
        try {
            swtBot.table();
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return true;
        } catch (WidgetNotFoundException e) {
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return false;
        }
    }

    public RemoteBotTable tableWithLabel(String label) throws RemoteException {
        return table.setWidget(swtBot.tableWithLabel(label));
    }

    public RemoteBotTable tableWithLabel(String label, int index)
        throws RemoteException {
        table.setWidget(swtBot.tableWithLabel(label, index));
        return table;
    }

    public RemoteBotTable tableWithId(String key, String value)
        throws RemoteException {
        table.setWidget(swtBot.tableWithId(key, value));
        return table;
    }

    public RemoteBotTable tableWithId(String key, String value, int index)
        throws RemoteException {
        table.setWidget(swtBot.tableWithId(key, value, index));
        return table;
    }

    public RemoteBotTable tableWithId(String value) throws RemoteException {
        table.setWidget(swtBot.tableWithId(value));
        return table;
    }

    public RemoteBotTable tableWithId(String value, int index)
        throws RemoteException {
        return table.setWidget(swtBot.tableWithId(value, index));

    }

    public RemoteBotTable tableInGroup(String inGroup) throws RemoteException {
        return table.setWidget(swtBot.tableInGroup(inGroup));

    }

    public RemoteBotTable tableInGroup(String inGroup, int index)
        throws RemoteException {
        return table.setWidget(swtBot.tableInGroup(inGroup, index));

    }

    public RemoteBotTable table() throws RemoteException {
        return table.setWidget(swtBot.table());

    }

    public RemoteBotTable table(int index) throws RemoteException {
        return table.setWidget(swtBot.table(index));

    }

    public RemoteBotTable tableWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        return table.setWidget(swtBot.tableWithLabelInGroup(label, inGroup));

    }

    public RemoteBotMenu menu(String text) throws RemoteException {
        return menu.setWidget(swtBot.menu(text));

    }

    public RemoteBotMenu menu(String text, int index) throws RemoteException {
        return menu.setWidget(swtBot.menu(text, index));
    }

    public RemoteBotMenu menuWithId(String value) throws RemoteException {
        return menu.setWidget(swtBot.menuWithId(value));
    }

    public RemoteBotMenu menuWithId(String value, int index)
        throws RemoteException {
        return menu.setWidget(swtBot.menuWithId(value, index));
    }

    public RemoteBotMenu menuWithId(String key, String value)
        throws RemoteException {
        return menu.setWidget(swtBot.menuWithId(key, value));
    }

    public RemoteBotMenu menuWithId(String key, String value, int index)
        throws RemoteException {
        return menu.setWidget(swtBot.menuWithId(key, value, index));

    }

    /**********************************************
     * 
     * Widget table
     * 
     **********************************************/

    public RemoteBotList listWithLabel(String label) throws RemoteException {
        list.setWidget(swtBot.listWithLabel(label));
        return list;
    }

    public RemoteBotList listWithLabel(String label, int index)
        throws RemoteException {
        list.setWidget(swtBot.listWithLabel(label, index));
        return list;
    }

    public RemoteBotList listWithId(String key, String value)
        throws RemoteException {
        list.setWidget(swtBot.listWithId(key, value));
        return list;
    }

    public RemoteBotList listWithId(String key, String value, int index)
        throws RemoteException {
        list.setWidget(swtBot.listWithId(key, value, index));
        return list;
    }

    public RemoteBotList listWithId(String value) throws RemoteException {
        list.setWidget(swtBot.listWithId(value));
        return list;
    }

    public RemoteBotList listWithId(String value, int index)
        throws RemoteException {
        list.setWidget(swtBot.listWithId(value, index));
        return list;
    }

    public RemoteBotList listInGroup(String inGroup) throws RemoteException {
        list.setWidget(swtBot.listInGroup(inGroup));
        return list;
    }

    public RemoteBotList listInGroup(String inGroup, int index)
        throws RemoteException {
        list.setWidget(swtBot.listInGroup(inGroup, index));
        return list;
    }

    public RemoteBotList list() throws RemoteException {
        list.setWidget(swtBot.list());
        return list;
    }

    public RemoteBotList list(int index) throws RemoteException {
        list.setWidget(swtBot.list(index));
        return list;
    }

    public RemoteBotList listWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        list.setWidget(swtBot.listWithLabelInGroup(label, inGroup));
        return list;
    }

    public RemoteBotList listWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        list.setWidget(swtBot.listWithLabelInGroup(label, inGroup, index));
        return list;
    }

    /**********************************************
     * 
     * Widget table
     * 
     **********************************************/
    public RemoteBotCheckBox checkBoxWithLabel(String label)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithLabel(label));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxWithLabel(String label, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithLabel(label, index));
        return checkbox;
    }

    public RemoteBotCheckBox checkBox(String mnemonicText) throws RemoteException {
        checkbox.setWidget(swtBot.checkBox(mnemonicText));
        return checkbox;
    }

    public RemoteBotCheckBox checkBox(String mnemonicText, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBox(mnemonicText, index));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxWithTooltip(String tooltip)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithTooltip(tooltip));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxWithTooltip(String tooltip, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithTooltip(tooltip, index));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxWithId(String key, String value)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithId(key, value));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxWithId(String key, String value, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithId(key, value, index));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxWithId(String value) throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithId(value));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxWithId(String value, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithId(value, index));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxInGroup(String inGroup)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxInGroup(inGroup));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxInGroup(String inGroup, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxInGroup(inGroup, index));
        return checkbox;
    }

    public RemoteBotCheckBox checkBox() throws RemoteException {
        checkbox.setWidget(swtBot.checkBox());
        return checkbox;
    }

    public RemoteBotCheckBox checkBox(int index) throws RemoteException {
        checkbox.setWidget(swtBot.checkBox(index));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithLabelInGroup(label, inGroup));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithLabelInGroup(label, inGroup,
            index));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxInGroup(mnemonicText, inGroup));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        checkbox
            .setWidget(swtBot.checkBoxInGroup(mnemonicText, inGroup, index));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithTooltipInGroup(tooltip, inGroup));
        return checkbox;
    }

    public RemoteBotCheckBox checkBoxWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithTooltipInGroup(tooltip, inGroup,
            index));
        return checkbox;
    }

    /**********************************************
     * 
     * Widget: Radio
     * 
     **********************************************/
    public RemoteBotRadio radioWithLabel(String label) throws RemoteException {
        radio.setWidget(swtBot.radioWithLabel(label));
        return radio;
    }

    public RemoteBotRadio radioWithLabel(String label, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithLabel(label));
        return radio;
    }

    public RemoteBotRadio radio(String mnemonicText) throws RemoteException {
        radio.setWidget(SarosSWTBot.getInstance().radio(mnemonicText));
        return radio;
    }

    public RemoteBotRadio radio(String mnemonicText, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radio(mnemonicText, index));
        return radio;
    }

    public RemoteBotRadio radioWithTooltip(String tooltip) throws RemoteException {
        radio.setWidget(swtBot.radioWithTooltip(tooltip));
        return radio;
    }

    public RemoteBotRadio radioWithTooltip(String tooltip, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithTooltip(tooltip, index));
        return radio;
    }

    public RemoteBotRadio radioWithId(String key, String value)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithId(key, value));
        return radio;
    }

    public RemoteBotRadio radioWithId(String key, String value, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithId(key, value, index));
        return radio;
    }

    public RemoteBotRadio radioWithId(String value) throws RemoteException {
        radio.setWidget(swtBot.radioWithId(value));
        return radio;
    }

    public RemoteBotRadio radioWithId(String value, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithId(value, index));
        return radio;
    }

    public RemoteBotRadio radioInGroup(String inGroup) throws RemoteException {
        radio.setWidget(swtBot.radioInGroup(inGroup));
        return radio;
    }

    public RemoteBotRadio radioInGroup(String inGroup, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radioInGroup(inGroup, index));
        return radio;
    }

    public RemoteBotRadio radio() throws RemoteException {
        radio.setWidget(swtBot.radio());
        return radio;
    }

    public RemoteBotRadio radio(int index) throws RemoteException {
        radio.setWidget(swtBot.radio(index));
        return radio;
    }

    public RemoteBotRadio radioWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithLabelInGroup(label, inGroup));
        return radio;
    }

    public RemoteBotRadio radioWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        radio.setWidget(swtBot.radioWithLabelInGroup(label, inGroup, index));
        return radio;
    }

    public RemoteBotRadio radioInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        radio.setWidget(swtBot.radioInGroup(mnemonicText, inGroup));
        return radio;
    }

    public RemoteBotRadio radioInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        radio.setWidget(swtBot.radioInGroup(mnemonicText, inGroup, index));
        return radio;
    }

    public RemoteBotRadio radioWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithTooltipInGroup(tooltip, inGroup));
        return radio;
    }

    public RemoteBotRadio radioWithTooltipInGroup(String tooltip, String inGroup,
        int index) throws RemoteException {
        radio
            .setWidget(swtBot.radioWithTooltipInGroup(tooltip, inGroup, index));
        return radio;
    }

    /**********************************************
     * 
     * Widget toggleButton
     * 
     **********************************************/
    public RemoteBotToggleButton toggleButtonWithLabel(String label)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithLabel(label));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonWithLabel(String label, int index)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithLabel(label));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButton(String mnemonicText)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButton(mnemonicText));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButton(String mnemonicText, int index)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButton(mnemonicText, index));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonWithTooltip(String tooltip)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithTooltip(tooltip));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonWithTooltip(String tooltip, int index)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithTooltip(tooltip, index));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonWithId(String key, String value)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithId(key, value));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonWithId(String key, String value,
        int index) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithId(key, value));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonWithId(String value)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithId(value));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonWithId(String value, int index)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithId(value, index));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonInGroup(String inGroup)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonInGroup(inGroup));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonInGroup(String inGroup, int index)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonInGroup(inGroup, index));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButton() throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButton());
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButton(int index) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButton(index));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonWithLabelInGroup(String label,
        String inGroup) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithLabelInGroup(label,
            inGroup));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithLabelInGroup(label,
            inGroup, index));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonInGroup(String mnemonicText,
        String inGroup) throws RemoteException {
        toggleButton.setWidget(swtBot
            .toggleButtonInGroup(mnemonicText, inGroup));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonInGroup(String mnemonicText,
        String inGroup, int index) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonInGroup(mnemonicText,
            inGroup, index));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithTooltipInGroup(tooltip,
            inGroup));
        return toggleButton;
    }

    public RemoteBotToggleButton toggleButtonWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithTooltipInGroup(tooltip,
            inGroup, index));
        return toggleButton;
    }

    /**********************************************
     * 
     * Wait until
     * 
     **********************************************/
    public void waitUntil(ICondition condition) throws RemoteException {
        swtBot.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }

    public void waitLongUntil(ICondition condition) throws RemoteException {
        swtBot.waitUntil(condition, SarosSWTBotPreferences.SAROS_LONG_TIMEOUT);
    }

    public void waitShortUntil(ICondition condition) throws RemoteException {
        swtBot.waitUntil(condition, SarosSWTBotPreferences.SAROS_SHORT_TIMEOUT);
    }

    /**********************************************
     * 
     * Others
     * 
     **********************************************/
    public void sleep(long millis) throws RemoteException {
        swtBot.sleep(millis);
    }

    public void captureScreenshot(String fileName) throws RemoteException {
        swtBot.captureScreenshot(fileName);
    }

    public String getPathToScreenShot() throws RemoteException {
        Bundle bundle = saros.getBundle();
        log.debug("screenshot's directory: "
            + bundle.getLocation().substring(16) + SCREENSHOTDIR);
        if (getOS() == TypeOfOS.WINDOW)
            return bundle.getLocation().substring(16) + SCREENSHOTDIR;
        else if (getOS() == TypeOfOS.MAC) {
            return "/" + bundle.getLocation().substring(16) + SCREENSHOTDIR;
        }
        return bundle.getLocation().substring(16) + SCREENSHOTDIR;
    }

}
