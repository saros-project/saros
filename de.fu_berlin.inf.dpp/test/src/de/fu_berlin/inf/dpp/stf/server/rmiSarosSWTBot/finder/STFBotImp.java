package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCComboImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCheckBox;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCheckBoxImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotComboImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotLabelImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotList;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotListImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotMenuImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotRadio;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotRadioImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShellImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotStyledTextImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTableImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotText;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTextImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToggleButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToggleButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotToolbarButtonImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.SarosSWTBot;

public class STFBotImp extends STF implements STFBot {

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
    private static STFBotMenuImp menu;
    private static STFBotListImp list;
    private static STFBotCheckBoxImp checkbox;
    private static STFBotRadioImp radio;
    private static STFBotToggleButtonImp toggleButton;

    /**
     * {@link ChatViewImp} is a singleton, but inheritance is possible.
     */
    public static STFBotImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotImp();
        swtBot = SarosSWTBot.getInstance();

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
        menu = STFBotMenuImp.getInstance();
        list = STFBotListImp.getInstance();
        checkbox = STFBotCheckBoxImp.getInstance();
        radio = STFBotRadioImp.getInstance();

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
        tree.setWidget(swtBot.tree());
        return tree;
    }

    public STFBotTree treeWithLabel(String label) throws RemoteException {
        tree.setWidget(swtBot.treeWithLabel(label));
        return tree;
    }

    public STFBotTree treeWithLabel(String label, int index)
        throws RemoteException {
        tree.setWidget(swtBot.treeWithLabel(label, index));
        return tree;
    }

    public STFBotTree treeWithId(String key, String value)
        throws RemoteException {
        tree.setWidget(swtBot.treeWithId(key, value));
        return tree;
    }

    public STFBotTree treeWithId(String key, String value, int index)
        throws RemoteException {
        tree.setWidget(swtBot.treeWithId(key, value, index));
        return tree;
    }

    public STFBotTree treeWithId(String value) throws RemoteException {
        tree.setWidget(swtBot.treeWithId(value));
        return tree;
    }

    public STFBotTree treeWithId(String value, int index)
        throws RemoteException {
        tree.setWidget(swtBot.treeWithId(value, index));
        return tree;
    }

    public STFBotTree treeInGroup(String inGroup) throws RemoteException {
        tree.setWidget(swtBot.treeInGroup(inGroup));
        return tree;
    }

    public STFBotTree treeInGroup(String inGroup, int index)
        throws RemoteException {
        tree.setWidget(swtBot.treeInGroup(inGroup, index));
        return tree;
    }

    public STFBotTree tree(int index) throws RemoteException {
        tree.setWidget(swtBot.tree(index));
        return tree;
    }

    public STFBotTree treeWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        tree.setWidget(swtBot.treeWithLabelInGroup(label, inGroup));
        return tree;
    }

    public STFBotTree treeWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        tree.setWidget(swtBot.treeWithLabelInGroup(label, inGroup, index));
        return tree;
    }

    /**********************************************
     * 
     * Widget shell
     * 
     **********************************************/

    public STFBotShell shell(String title) throws RemoteException {
        return shell.setWidget(swtBot.shell(title));

    }

    public List<String> getTitlesOfOpenedShells() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotShell shell : swtBot.shells())
            list.add(shell.getText());
        return list;
    }

    public boolean isShellOpen(String title) throws RemoteException {
        try {
            swtBot.shell(title);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
        // return getTitlesOfOpenedShells().contains(title);
    }

    public void waitsUntilShellIsClosed(final String title)
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

    public STFBotShell activeShell() throws RemoteException {
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

    public STFBotButton buttonWithLabel(String label) throws RemoteException {
        button.setWidget(swtBot.buttonWithLabel(label, 0));
        return button;
    }

    public STFBotButton buttonWithLabel(String label, int index)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithLabel(label, index));
        return button;
    }

    public STFBotButton button(String mnemonicText) throws RemoteException {
        button.setWidget(swtBot.button(mnemonicText, 0));
        return button;
    }

    public STFBotButton button(String mnemonicText, int index)
        throws RemoteException {
        button.setWidget(swtBot.button(mnemonicText, index));
        return button;
    }

    public STFBotButton buttonWithTooltip(String tooltip)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithTooltip(tooltip, 0));
        return button;
    }

    public STFBotButton buttonWithTooltip(String tooltip, int index)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithTooltip(tooltip, index));
        return button;
    }

    public STFBotButton buttonWithId(String key, String value)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithId(key, value));
        return button;
    }

    public STFBotButton buttonWithId(String key, String value, int index)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithId(key, value, index));
        return button;
    }

    public STFBotButton buttonWithId(String value) throws RemoteException {
        button.setWidget(swtBot.buttonWithId(value));
        return button;
    }

    public STFBotButton buttonWithId(String value, int index)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithId(value, index));
        return button;
    }

    public STFBotButton buttonInGroup(String inGroup) throws RemoteException {
        button.setWidget(swtBot.buttonInGroup(inGroup));
        return button;
    }

    public STFBotButton buttonInGroup(String inGroup, int index)
        throws RemoteException {
        button.setWidget(swtBot.buttonInGroup(inGroup, index));
        return button;
    }

    public STFBotButton button() throws RemoteException {
        button.setWidget(swtBot.button());
        return button;
    }

    public STFBotButton button(int index) throws RemoteException {
        button.setWidget(swtBot.button(index));
        return button;
    }

    public STFBotButton buttonWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithLabelInGroup(label, inGroup));
        return button;
    }

    public STFBotButton buttonWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        button.setWidget(swtBot.buttonWithLabelInGroup(label, inGroup, index));
        return button;
    }

    public STFBotButton buttonInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        button.setWidget(swtBot.buttonInGroup(mnemonicText, inGroup));
        return button;
    }

    public STFBotButton buttonInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        button.setWidget(swtBot.buttonInGroup(mnemonicText, inGroup, index));
        return button;
    }

    public STFBotButton buttonWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException {
        button.setWidget(swtBot.buttonWithTooltipInGroup(tooltip, inGroup));
        return button;
    }

    public STFBotButton buttonWithTooltipInGroup(String tooltip,
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
    public STFBotLabel label() throws RemoteException {
        label.setWidget(swtBot.label());
        return label;
    }

    public STFBotLabel label(String mnemonicText) throws RemoteException {
        label.setWidget(swtBot.label(mnemonicText));
        return label;

    }

    public STFBotLabel label(String mnemonicText, int index)
        throws RemoteException {
        label.setWidget(swtBot.label(mnemonicText, index));
        return label;

    }

    public STFBotLabel labelWithId(String key, String value)
        throws RemoteException {
        label.setWidget(swtBot.labelWithId(key, value));
        return label;

    }

    public STFBotLabel labelWithId(String key, String value, int index)
        throws RemoteException {
        label.setWidget(swtBot.labelWithId(key, value, index));
        return label;

    }

    public STFBotLabel labelWithId(String value) throws RemoteException {
        label.setWidget(swtBot.labelWithId(value));
        return label;

    }

    public STFBotLabel labelWithId(String value, int index)
        throws RemoteException {
        label.setWidget(swtBot.labelWithId(value, index));
        return label;

    }

    public STFBotLabel labelInGroup(String inGroup) throws RemoteException {
        label.setWidget(swtBot.labelInGroup(inGroup));
        return label;

    }

    public STFBotLabel labelInGroup(String inGroup, int index)
        throws RemoteException {
        label.setWidget(swtBot.labelInGroup(inGroup, index));
        return label;

    }

    public STFBotLabel label(int index) throws RemoteException {
        label.setWidget(swtBot.label(index));
        return label;

    }

    public STFBotLabel labelInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        label.setWidget(swtBot.labelInGroup(MENU_CLASS, inGroup));
        return label;

    }

    public STFBotLabel labelInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        label.setWidget(swtBot.labelInGroup(mnemonicText, inGroup, index));
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
        styledText.setWidget(swtBot.styledTextWithLabel(label, 0));
        return styledText;
    }

    public STFBotStyledText styledTextWithLabel(String label, int index)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithLabel(label, index));
        return styledText;
    }

    public STFBotStyledText styledText(String text) throws RemoteException {
        styledText.setWidget(swtBot.styledText(text));
        return styledText;
    }

    public STFBotStyledText styledText(String text, int index)
        throws RemoteException {
        styledText.setWidget(swtBot.styledText(text, index));
        return styledText;
    }

    public STFBotStyledText styledTextWithId(String key, String value)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithId(key, value));
        return styledText;
    }

    public STFBotStyledText styledTextWithId(String key, String value, int index)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithId(key, value, index));
        return styledText;
    }

    public STFBotStyledText styledTextWithId(String value)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithId(value));
        return styledText;
    }

    public STFBotStyledText styledTextWithId(String value, int index)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithId(value, index));
        return styledText;
    }

    public STFBotStyledText styledTextInGroup(String inGroup)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextInGroup(inGroup));
        return styledText;
    }

    public STFBotStyledText styledTextInGroup(String inGroup, int index)
        throws RemoteException {
        styledText.setWidget(swtBot.styledTextInGroup(inGroup, index));
        return styledText;
    }

    public STFBotStyledText styledText() throws RemoteException {
        return styledText(0);
    }

    public STFBotStyledText styledText(int index) throws RemoteException {
        styledText.setWidget(swtBot.styledText(index));
        return styledText;
    }

    public STFBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup) throws RemoteException {
        return styledTextWithLabelInGroup(label, inGroup, 0);
    }

    public STFBotStyledText styledTextWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException {
        styledText.setWidget(swtBot.styledTextWithLabelInGroup(label, inGroup,
            index));
        return styledText;
    }

    public STFBotStyledText styledTextInGroup(String text, String inGroup)
        throws RemoteException {
        return styledTextInGroup(text, inGroup, 0);
    }

    public STFBotStyledText styledTextInGroup(String text, String inGroup,
        int index) throws RemoteException {
        styledText.setWidget(swtBot.styledTextInGroup(text, inGroup, index));
        return styledText;
    }

    /**********************************************
     * 
     * Widget comboBox
     * 
     **********************************************/
    public STFBotCombo comboBoxWithLabel(String label) throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithLabel(label));
        return comboBox;
    }

    public STFBotCombo comboBoxWithLabel(String label, int index)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithLabel(label, index));
        return comboBox;
    }

    public STFBotCombo comboBox(String text) throws RemoteException {
        comboBox.setWidget(swtBot.comboBox(text));
        return comboBox;
    }

    public STFBotCombo comboBox(String text, int index) throws RemoteException {
        comboBox.setWidget(swtBot.comboBox(text, index));
        return comboBox;
    }

    public STFBotCombo comboBoxWithId(String key, String value)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithId(key, value));
        return comboBox;
    }

    public STFBotCombo comboBoxWithId(String key, String value, int index)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithId(key, value, index));
        return comboBox;
    }

    public STFBotCombo comboBoxWithId(String value) throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithId(value));
        return comboBox;
    }

    public STFBotCombo comboBoxWithId(String value, int index)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithId(value, index));
        return comboBox;
    }

    public STFBotCombo comboBoxInGroup(String inGroup) throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxInGroup(inGroup));
        return comboBox;
    }

    public STFBotCombo comboBoxInGroup(String inGroup, int index)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxInGroup(inGroup, index));
        return comboBox;
    }

    public STFBotCombo comboBox() throws RemoteException {
        comboBox.setWidget(swtBot.comboBox());
        return comboBox;
    }

    public STFBotCombo comboBox(int index) throws RemoteException {
        comboBox.setWidget(swtBot.comboBox(index));
        return comboBox;
    }

    public STFBotCombo comboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithLabelInGroup(label, inGroup));
        return comboBox;
    }

    public STFBotCombo comboBoxWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxWithLabelInGroup(label, inGroup,
            index));
        return comboBox;
    }

    public STFBotCombo comboBoxInGroup(String text, String inGroup)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxInGroup(text, inGroup));
        return comboBox;
    }

    public STFBotCombo comboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException {
        comboBox.setWidget(swtBot.comboBoxInGroup(text, inGroup, index));
        return comboBox;
    }

    /**********************************************
     * 
     * Widget ccomboBox
     * 
     **********************************************/
    public STFBotCCombo ccomboBox(String text) throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBox(text));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBox(String text, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBox(text, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithLabel(String label) throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithLabel(label));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithLabel(String label, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithLabel(label, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithId(String key, String value)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithId(key, value));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithId(String key, String value, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithId(key, value, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithId(String value) throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithId(value));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithId(String value, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithId(value, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxInGroup(String inGroup) throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxInGroup(inGroup));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxInGroup(String inGroup, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxInGroup(inGroup, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBox() throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBox());
        return ccomboBox;
    }

    public STFBotCCombo ccomboBox(int index) throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBox(index));
        return ccomboBox;

    }

    public STFBotCCombo ccomboBoxInGroup(String text, String inGroup)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxInGroup(text, inGroup));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxInGroup(String text, String inGroup, int index)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxInGroup(text, inGroup, index));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        ccomboBox.setWidget(swtBot.ccomboBoxWithLabelInGroup(label, inGroup));
        return ccomboBox;
    }

    public STFBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup,
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
    public STFBotToolbarButton toolbarButton() throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButton());

    }

    public STFBotToolbarButton toolbarButton(int index) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButton(index));

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
        return toolbarButton.setWidget(swtBot.toolbarButton(mnemonicText));

    }

    public STFBotToolbarButton toolbarButton(String mnemonicText, int index)
        throws RemoteException {
        return toolbarButton.setWidget(swtBot
            .toolbarButton(mnemonicText, index));

    }

    public STFBotToolbarButton toolbarButtonWithTooltip(String tooltip)
        throws RemoteException {
        return toolbarButton
            .setWidget(swtBot.toolbarButtonWithTooltip(tooltip));

    }

    public STFBotToolbarButton toolbarButtonWithTooltip(String tooltip,
        int index) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithTooltip(tooltip,
            index));

    }

    public STFBotToolbarButton toolbarButtonWithId(String key, String value)
        throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithId(key, value));

    }

    public STFBotToolbarButton toolbarButtonWithId(String key, String value,
        int index) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithId(key, value,
            index));

    }

    public STFBotToolbarButton toolbarButtonWithId(String value)
        throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithId(value));

    }

    public STFBotToolbarButton toolbarButtonWithId(String value, int index)
        throws RemoteException {
        return toolbarButton
            .setWidget(swtBot.toolbarButtonWithId(value, index));

    }

    public STFBotToolbarButton toolbarButtonInGroup(String inGroup)
        throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonInGroup(inGroup));

    }

    public STFBotToolbarButton toolbarButtonInGroup(String inGroup, int index)
        throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonInGroup(inGroup,
            index));

    }

    public STFBotToolbarButton toolbarButtonInGroup(String mnemonicText,
        String inGroup) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonInGroup(
            mnemonicText, inGroup));

    }

    public STFBotToolbarButton toolbarButtonInGroup(String mnemonicText,
        String inGroup, int index) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonInGroup(
            mnemonicText, inGroup, index));

    }

    public STFBotToolbarButton toolbarButtonWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithTooltipInGroup(
            tooltip, inGroup));

    }

    public STFBotToolbarButton toolbarButtonWithTooltipInGroup(String tooltip,
        String inGroup, int index) throws RemoteException {
        return toolbarButton.setWidget(swtBot.toolbarButtonWithTooltipInGroup(
            tooltip, inGroup, index));

    }

    /**********************************************
     * 
     * Widget text
     * 
     **********************************************/
    public STFBotText textWithLabel(String label) throws RemoteException {
        text.setWidget(swtBot.textWithLabel(label));
        return text;
    }

    public STFBotText textWithLabel(String label, int index)
        throws RemoteException {
        text.setWidget(swtBot.textWithLabel(label, index));
        return text;
    }

    public STFBotText text(String txt) throws RemoteException {
        text.setWidget(swtBot.text(txt));
        return text;
    }

    public STFBotText text(String txt, int index) throws RemoteException {
        text.setWidget(swtBot.text(txt, index));
        return text;

    }

    public STFBotText textWithTooltip(String tooltip) throws RemoteException {
        text.setWidget(swtBot.textWithTooltip(tooltip));
        return text;

    }

    public STFBotText textWithTooltip(String tooltip, int index)
        throws RemoteException {
        text.setWidget(swtBot.textWithTooltip(tooltip, index));
        return text;

    }

    public STFBotText textWithMessage(String message) throws RemoteException {
        text.setWidget(swtBot.textWithMessage(message));
        return text;

    }

    public STFBotText textWithMessage(String message, int index)
        throws RemoteException {
        text.setWidget(swtBot.textWithMessage(message, index));
        return text;

    }

    public STFBotText textWithId(String key, String value)
        throws RemoteException {
        text.setWidget(swtBot.textWithId(key, value));
        return text;

    }

    public STFBotText textWithId(String key, String value, int index)
        throws RemoteException {
        text.setWidget(swtBot.textWithId(key, value, index));
        return text;

    }

    public STFBotText textWithId(String value) throws RemoteException {
        text.setWidget(swtBot.textWithId(value));
        return text;

    }

    public STFBotText textWithId(String value, int index)
        throws RemoteException {
        text.setWidget(swtBot.textWithId(value, index));
        return text;

    }

    public STFBotText textInGroup(String inGroup) throws RemoteException {
        text.setWidget(swtBot.textInGroup(inGroup));
        return text;

    }

    public STFBotText textInGroup(String inGroup, int index)
        throws RemoteException {
        text.setWidget(swtBot.textInGroup(inGroup, index));
        return text;

    }

    public STFBotText text() throws RemoteException {
        text.setWidget(swtBot.text());
        return text;

    }

    public STFBotText text(int index) throws RemoteException {
        text.setWidget(swtBot.text(index));
        return text;

    }

    public STFBotText textWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        text.setWidget(swtBot.textWithLabel(label));
        return text;

    }

    public STFBotText textWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        text.setWidget(swtBot.textWithLabelInGroup(label, inGroup, index));
        return text;

    }

    public STFBotText textInGroup(String txt, String inGroup)
        throws RemoteException {
        text.setWidget(swtBot.textInGroup(txt, inGroup));
        return text;

    }

    public STFBotText textInGroup(String txt, String inGroup, int index)
        throws RemoteException {
        text.setWidget(swtBot.textInGroup(txt, inGroup, index));
        return text;

    }

    public STFBotText textWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException {
        text.setWidget(swtBot.textWithTooltipInGroup(tooltip, inGroup));
        return text;

    }

    /**********************************************
     * 
     * Widget table
     * 
     **********************************************/
    public STFBotText textWithTooltipInGroup(String tooltip, String inGroup,
        int index) throws RemoteException {
        text.setWidget(swtBot.textWithTooltipInGroup(tooltip, inGroup, index));
        return text;
    }

    public STFBotTable tableWithLabel(String label) throws RemoteException {
        return table.setWidget(swtBot.tableWithLabel(label));
    }

    public STFBotTable tableWithLabel(String label, int index)
        throws RemoteException {
        table.setWidget(swtBot.tableWithLabel(label, index));
        return table;
    }

    public STFBotTable tableWithId(String key, String value)
        throws RemoteException {
        table.setWidget(swtBot.tableWithId(key, value));
        return table;
    }

    public STFBotTable tableWithId(String key, String value, int index)
        throws RemoteException {
        table.setWidget(swtBot.tableWithId(key, value, index));
        return table;
    }

    public STFBotTable tableWithId(String value) throws RemoteException {
        table.setWidget(swtBot.tableWithId(value));
        return table;
    }

    public STFBotTable tableWithId(String value, int index)
        throws RemoteException {
        return table.setWidget(swtBot.tableWithId(value, index));

    }

    public STFBotTable tableInGroup(String inGroup) throws RemoteException {
        return table.setWidget(swtBot.tableInGroup(inGroup));

    }

    public STFBotTable tableInGroup(String inGroup, int index)
        throws RemoteException {
        return table.setWidget(swtBot.tableInGroup(inGroup, index));

    }

    public STFBotTable table() throws RemoteException {
        return table.setWidget(swtBot.table());

    }

    public STFBotTable table(int index) throws RemoteException {
        return table.setWidget(swtBot.table(index));

    }

    public STFBotTable tableWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        return table.setWidget(swtBot.tableWithLabelInGroup(label, inGroup));

    }

    public STFBotMenu menu(String text) throws RemoteException {
        return menu.setWidget(swtBot.menu(text));

    }

    public STFBotMenu menu(String text, int index) throws RemoteException {
        return menu.setWidget(swtBot.menu(text, index));
    }

    public STFBotMenu menuWithId(String value) throws RemoteException {
        return menu.setWidget(swtBot.menuWithId(value));
    }

    public STFBotMenu menuWithId(String value, int index)
        throws RemoteException {
        return menu.setWidget(swtBot.menuWithId(value, index));
    }

    public STFBotMenu menuWithId(String key, String value)
        throws RemoteException {
        return menu.setWidget(swtBot.menuWithId(key, value));
    }

    public STFBotMenu menuWithId(String key, String value, int index)
        throws RemoteException {
        return menu.setWidget(swtBot.menuWithId(key, value, index));

    }

    /**********************************************
     * 
     * Widget table
     * 
     **********************************************/

    public STFBotList listWithLabel(String label) throws RemoteException {
        list.setWidget(swtBot.listWithLabel(label));
        return list;
    }

    public STFBotList listWithLabel(String label, int index)
        throws RemoteException {
        list.setWidget(swtBot.listWithLabel(label, index));
        return list;
    }

    public STFBotList listWithId(String key, String value)
        throws RemoteException {
        list.setWidget(swtBot.listWithId(key, value));
        return list;
    }

    public STFBotList listWithId(String key, String value, int index)
        throws RemoteException {
        list.setWidget(swtBot.listWithId(key, value, index));
        return list;
    }

    public STFBotList listWithId(String value) throws RemoteException {
        list.setWidget(swtBot.listWithId(value));
        return list;
    }

    public STFBotList listWithId(String value, int index)
        throws RemoteException {
        list.setWidget(swtBot.listWithId(value, index));
        return list;
    }

    public STFBotList listInGroup(String inGroup) throws RemoteException {
        list.setWidget(swtBot.listInGroup(inGroup));
        return list;
    }

    public STFBotList listInGroup(String inGroup, int index)
        throws RemoteException {
        list.setWidget(swtBot.listInGroup(inGroup, index));
        return list;
    }

    public STFBotList list() throws RemoteException {
        list.setWidget(swtBot.list());
        return list;
    }

    public STFBotList list(int index) throws RemoteException {
        list.setWidget(swtBot.list(index));
        return list;
    }

    public STFBotList listWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        list.setWidget(swtBot.listWithLabelInGroup(label, inGroup));
        return list;
    }

    public STFBotList listWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        list.setWidget(swtBot.listWithLabelInGroup(label, inGroup, index));
        return list;
    }

    /**********************************************
     * 
     * Widget table
     * 
     **********************************************/
    public STFBotCheckBox checkBoxWithLabel(String label)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithLabel(label));
        return checkbox;
    }

    public STFBotCheckBox checkBoxWithLabel(String label, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithLabel(label, index));
        return checkbox;
    }

    public STFBotCheckBox checkBox(String mnemonicText) throws RemoteException {
        checkbox.setWidget(swtBot.checkBox(mnemonicText));
        return checkbox;
    }

    public STFBotCheckBox checkBox(String mnemonicText, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBox(mnemonicText, index));
        return checkbox;
    }

    public STFBotCheckBox checkBoxWithTooltip(String tooltip)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithTooltip(tooltip));
        return checkbox;
    }

    public STFBotCheckBox checkBoxWithTooltip(String tooltip, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithTooltip(tooltip, index));
        return checkbox;
    }

    public STFBotCheckBox checkBoxWithId(String key, String value)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithId(key, value));
        return checkbox;
    }

    public STFBotCheckBox checkBoxWithId(String key, String value, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithId(key, value, index));
        return checkbox;
    }

    public STFBotCheckBox checkBoxWithId(String value) throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithId(value));
        return checkbox;
    }

    public STFBotCheckBox checkBoxWithId(String value, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithId(value, index));
        return checkbox;
    }

    public STFBotCheckBox checkBoxInGroup(String inGroup)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxInGroup(inGroup));
        return checkbox;
    }

    public STFBotCheckBox checkBoxInGroup(String inGroup, int index)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxInGroup(inGroup, index));
        return checkbox;
    }

    public STFBotCheckBox checkBox() throws RemoteException {
        checkbox.setWidget(swtBot.checkBox());
        return checkbox;
    }

    public STFBotCheckBox checkBox(int index) throws RemoteException {
        checkbox.setWidget(swtBot.checkBox(index));
        return checkbox;
    }

    public STFBotCheckBox checkBoxWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithLabelInGroup(label, inGroup));
        return checkbox;
    }

    public STFBotCheckBox checkBoxWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithLabelInGroup(label, inGroup,
            index));
        return checkbox;
    }

    public STFBotCheckBox checkBoxInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxInGroup(mnemonicText, inGroup));
        return checkbox;
    }

    public STFBotCheckBox checkBoxInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        checkbox
            .setWidget(swtBot.checkBoxInGroup(mnemonicText, inGroup, index));
        return checkbox;
    }

    public STFBotCheckBox checkBoxWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException {
        checkbox.setWidget(swtBot.checkBoxWithTooltipInGroup(tooltip, inGroup));
        return checkbox;
    }

    public STFBotCheckBox checkBoxWithTooltipInGroup(String tooltip,
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
    public STFBotRadio radioWithLabel(String label) throws RemoteException {
        radio.setWidget(swtBot.radioWithLabel(label));
        return radio;
    }

    public STFBotRadio radioWithLabel(String label, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithLabel(label));
        return radio;
    }

    public STFBotRadio radio(String mnemonicText) throws RemoteException {
        radio.setWidget(SarosSWTBot.getInstance().radio(mnemonicText));
        return radio;
    }

    public STFBotRadio radio(String mnemonicText, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radio(mnemonicText, index));
        return radio;
    }

    public STFBotRadio radioWithTooltip(String tooltip) throws RemoteException {
        radio.setWidget(swtBot.radioWithTooltip(tooltip));
        return radio;
    }

    public STFBotRadio radioWithTooltip(String tooltip, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithTooltip(tooltip, index));
        return radio;
    }

    public STFBotRadio radioWithId(String key, String value)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithId(key, value));
        return radio;
    }

    public STFBotRadio radioWithId(String key, String value, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithId(key, value, index));
        return radio;
    }

    public STFBotRadio radioWithId(String value) throws RemoteException {
        radio.setWidget(swtBot.radioWithId(value));
        return radio;
    }

    public STFBotRadio radioWithId(String value, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithId(value, index));
        return radio;
    }

    public STFBotRadio radioInGroup(String inGroup) throws RemoteException {
        radio.setWidget(swtBot.radioInGroup(inGroup));
        return radio;
    }

    public STFBotRadio radioInGroup(String inGroup, int index)
        throws RemoteException {
        radio.setWidget(swtBot.radioInGroup(inGroup, index));
        return radio;
    }

    public STFBotRadio radio() throws RemoteException {
        radio.setWidget(swtBot.radio());
        return radio;
    }

    public STFBotRadio radio(int index) throws RemoteException {
        radio.setWidget(swtBot.radio(index));
        return radio;
    }

    public STFBotRadio radioWithLabelInGroup(String label, String inGroup)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithLabelInGroup(label, inGroup));
        return radio;
    }

    public STFBotRadio radioWithLabelInGroup(String label, String inGroup,
        int index) throws RemoteException {
        radio.setWidget(swtBot.radioWithLabelInGroup(label, inGroup, index));
        return radio;
    }

    public STFBotRadio radioInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        radio.setWidget(swtBot.radioInGroup(mnemonicText, inGroup));
        return radio;
    }

    public STFBotRadio radioInGroup(String mnemonicText, String inGroup,
        int index) throws RemoteException {
        radio.setWidget(swtBot.radioInGroup(mnemonicText, inGroup, index));
        return radio;
    }

    public STFBotRadio radioWithTooltipInGroup(String tooltip, String inGroup)
        throws RemoteException {
        radio.setWidget(swtBot.radioWithTooltipInGroup(tooltip, inGroup));
        return radio;
    }

    public STFBotRadio radioWithTooltipInGroup(String tooltip, String inGroup,
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
    public STFBotToggleButton toggleButtonWithLabel(String label)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithLabel(label));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonWithLabel(String label, int index)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithLabel(label));
        return toggleButton;
    }

    public STFBotToggleButton toggleButton(String mnemonicText)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButton(mnemonicText));
        return toggleButton;
    }

    public STFBotToggleButton toggleButton(String mnemonicText, int index)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButton(mnemonicText, index));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonWithTooltip(String tooltip)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithTooltip(tooltip));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonWithTooltip(String tooltip, int index)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithTooltip(tooltip, index));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonWithId(String key, String value)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithId(key, value));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonWithId(String key, String value,
        int index) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithId(key, value));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonWithId(String value)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithId(value));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonWithId(String value, int index)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithId(value, index));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonInGroup(String inGroup)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonInGroup(inGroup));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonInGroup(String inGroup, int index)
        throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonInGroup(inGroup, index));
        return toggleButton;
    }

    public STFBotToggleButton toggleButton() throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButton());
        return toggleButton;
    }

    public STFBotToggleButton toggleButton(int index) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButton(index));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonWithLabelInGroup(String label,
        String inGroup) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithLabelInGroup(label,
            inGroup));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonWithLabelInGroup(String label,
        String inGroup, int index) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithLabelInGroup(label,
            inGroup, index));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonInGroup(String mnemonicText,
        String inGroup) throws RemoteException {
        toggleButton.setWidget(swtBot
            .toggleButtonInGroup(mnemonicText, inGroup));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonInGroup(String mnemonicText,
        String inGroup, int index) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonInGroup(mnemonicText,
            inGroup, index));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonWithTooltipInGroup(String tooltip,
        String inGroup) throws RemoteException {
        toggleButton.setWidget(swtBot.toggleButtonWithTooltipInGroup(tooltip,
            inGroup));
        return toggleButton;
    }

    public STFBotToggleButton toggleButtonWithTooltipInGroup(String tooltip,
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
