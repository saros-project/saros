package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot;

import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotCCombo;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotCLabel;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotCTabItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotCheckBox;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotList;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotRadio;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotText;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToggleButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTree;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

/* FIXME either just link to the wrapped SWTBot methods or copy the IFSPEC */
public interface IRemoteBot extends Remote {

  /**
   * ********************************************
   *
   * <p>Widget tree
   *
   * <p>********************************************
   */
  /** @see SWTBot#tree() */
  public IRemoteBotTree tree() throws RemoteException;

  /**
   * @param label the label on the widget.
   * @return a {@link IRemoteBotTree} with the specified <code>label</code>.
   */
  public IRemoteBotTree treeWithLabel(String label) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotTree} with the specified <code>label</code>.
   */
  public IRemoteBotTree treeWithLabel(String label, int index) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @return a {@link IRemoteBotTree} with the specified <code>key/value</code>.
   */
  public IRemoteBotTree treeWithId(String key, String value) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotTree} with the specified <code>key/value</code>.
   */
  public IRemoteBotTree treeWithId(String key, String value, int index) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @return a {@link IRemoteBotTree} with the specified <code>value</code>.
   */
  public IRemoteBotTree treeWithId(String value) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @param index the index of the widget.
   * @return a {@link IRemoteBotTree} with the specified <code>value</code>.
   */
  public IRemoteBotTree treeWithId(String value, int index) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotTree} with the specified <code>inGroup</code>.
   */
  public IRemoteBotTree treeInGroup(String inGroup) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotTree} with the specified <code>inGroup</code>.
   */
  public IRemoteBotTree treeInGroup(String inGroup, int index) throws RemoteException;

  /**
   * @param index the index of the widget.
   * @return a {@link IRemoteBotTree} with the specified <code>none</code>.
   */
  public IRemoteBotTree tree(int index) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotTree} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotTree treeWithLabelInGroup(String label, String inGroup) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotTree} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotTree treeWithLabelInGroup(String label, String inGroup, int index)
      throws RemoteException;

  /**
   * ********************************************
   *
   * <p>Widget shell
   *
   * <p>********************************************
   */

  /**
   * @see SWTBot#shell(String)
   * @throws RemoteException
   */
  public IRemoteBotShell shell(String title) throws RemoteException;

  /**
   * @return list of titles of all opened shells
   * @throws RemoteException
   */
  public List<String> getOpenShellNames() throws RemoteException;

  /**
   * @param title the title of the shell
   * @return<tt>true</tt>, if the given shell is open.
   * @throws RemoteException
   */
  public boolean isShellOpen(String title) throws RemoteException;

  /**
   * waits until the given Shell is closed.
   *
   * @param title the title of the shell.
   * @throws RemoteException ;;
   */
  public void waitUntilShellIsClosed(final String title) throws RemoteException;

  public void waitLongUntilShellIsClosed(final String title) throws RemoteException;

  /**
   * waits until the given Shell is open.
   *
   * @param title the title of the shell.
   * @throws RemoteException ;;
   */
  public void waitUntilShellIsOpen(final String title) throws RemoteException;

  public void waitLongUntilShellIsOpen(final String title) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>Widget button
   *
   * <p>********************************************
   */
  /** @see SWTBot#buttonWithLabel(String) */
  public IRemoteBotButton buttonWithLabel(String label) throws RemoteException;

  /** @see SWTBot#buttonWithLabelInGroup(String, String, int) */
  public IRemoteBotButton buttonWithLabel(String label, int index) throws RemoteException;

  /** @see SWTBot#button(String) */
  public IRemoteBotButton button(String mnemonicText) throws RemoteException;

  /** @see SWTBot#button(String, int) */
  public IRemoteBotButton button(String mnemonicText, int index) throws RemoteException;

  /** @see SWTBot#buttonWithTooltip(String) */
  public IRemoteBotButton buttonWithTooltip(String tooltip) throws RemoteException;

  public IRemoteBotButton buttonWithTooltip(String tooltip, int index) throws RemoteException;

  /** @see SWTBot#buttonWithId(String, String) */
  public IRemoteBotButton buttonWithId(String key, String value) throws RemoteException;

  /** @see SWTBot#buttonWithId(String, String, int) */
  public IRemoteBotButton buttonWithId(String key, String value, int index) throws RemoteException;

  /** @see SWTBot#buttonWithId(String) */
  public IRemoteBotButton buttonWithId(String value) throws RemoteException;

  /** @see SWTBot#buttonWithId(String,int) */
  public IRemoteBotButton buttonWithId(String value, int index) throws RemoteException;

  /** @see SWTBot#buttonInGroup(String) */
  public IRemoteBotButton buttonInGroup(String inGroup) throws RemoteException;

  /** @see SWTBot#buttonInGroup(String,int) */
  public IRemoteBotButton buttonInGroup(String inGroup, int index) throws RemoteException;

  /** @see SWTBot#button() */
  public IRemoteBotButton button() throws RemoteException;

  /** @see SWTBot#button(int) */
  public IRemoteBotButton button(int index) throws RemoteException;

  /** @see SWTBot#buttonWithLabelInGroup(String, String) */
  public IRemoteBotButton buttonWithLabelInGroup(String label, String inGroup)
      throws RemoteException;

  /** @see SWTBot#buttonWithLabelInGroup(String, String,int) */
  public IRemoteBotButton buttonWithLabelInGroup(String label, String inGroup, int index)
      throws RemoteException;

  /** @see SWTBot#buttonInGroup(String, String) */
  public IRemoteBotButton buttonInGroup(String mnemonicText, String inGroup) throws RemoteException;

  /** @see SWTBot#buttonInGroup(String, String, int) . */
  public IRemoteBotButton buttonInGroup(String mnemonicText, String inGroup, int index)
      throws RemoteException;

  /** @see SWTBot#buttonWithTooltipInGroup(String, String) */
  public IRemoteBotButton buttonWithTooltipInGroup(String tooltip, String inGroup)
      throws RemoteException;

  /** @see SWTBot#buttonWithTooltipInGroup(String, String, int) */
  public IRemoteBotButton buttonWithTooltipInGroup(String tooltip, String inGroup, int index)
      throws RemoteException;

  /**
   * ********************************************
   *
   * <p>Widget label
   *
   * <p>********************************************
   */
  public IRemoteBotLabel label() throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @return a {@link IRemoteBotLabel} with the specified <code>mnemonicText</code>.
   */
  public IRemoteBotLabel label(String mnemonicText) throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotLabel} with the specified <code>mnemonicText</code>.
   */
  public IRemoteBotLabel label(String mnemonicText, int index) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @return a {@link IRemoteBotLabel} with the specified <code>key/value</code>.
   */
  public IRemoteBotLabel labelWithId(String key, String value) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotLabel} with the specified <code>key/value</code>.
   */
  public IRemoteBotLabel labelWithId(String key, String value, int index) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @return a {@link IRemoteBotLabel} with the specified <code>value</code>.
   */
  public IRemoteBotLabel labelWithId(String value) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @param index the index of the widget.
   * @return a {@link IRemoteBotLabel} with the specified <code>value</code>.
   */
  public IRemoteBotLabel labelWithId(String value, int index) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotLabel} with the specified <code>inGroup</code> .
   */
  public IRemoteBotLabel labelInGroup(String inGroup) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotLabel} with the specified <code>inGroup</code> .
   */
  public IRemoteBotLabel labelInGroup(String inGroup, int index) throws RemoteException;

  /**
   * @param index the index of the widget.
   * @return a {@link IRemoteBotLabel} with the specified <code>none</code>.
   */
  public IRemoteBotLabel label(int index) throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotLabel} with the specified <code>mnemonicText</code> with the
   *     specified <code>inGroup</code> .
   */
  public IRemoteBotLabel labelInGroup(String mnemonicText, String inGroup) throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotLabel} with the specified <code>mnemonicText</code> with the
   *     specified <code>inGroup</code> .
   */
  public IRemoteBotLabel labelInGroup(String mnemonicText, String inGroup, int index)
      throws RemoteException;

  public boolean existsStyledText() throws RemoteException;

  public boolean existsLabel() throws RemoteException;

  public boolean existsLabelInGroup(String groupName) throws RemoteException;

  public boolean existsLabel(String text) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>Widget CLabel
   *
   * <p>********************************************
   */
  public boolean existsCLabel() throws RemoteException;

  public IRemoteBotCLabel clabel() throws RemoteException;

  public IRemoteBotCLabel clabel(String text) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>Widget styledText
   *
   * <p>********************************************
   */
  /** @see SWTBot#styledText(String) */
  public IRemoteBotStyledText styledTextWithLabel(String label) throws RemoteException;

  /** @see SWTBot#styledTextWithLabel(String, int) */
  public IRemoteBotStyledText styledTextWithLabel(String label, int index) throws RemoteException;

  /** @see SWTBot#styledText(String) */
  public IRemoteBotStyledText styledText(String text) throws RemoteException;

  /** @see SWTBot#styledText(String, int) */
  public IRemoteBotStyledText styledText(String text, int index) throws RemoteException;

  /** @see SWTBot#styledTextWithId(String, String) */
  public IRemoteBotStyledText styledTextWithId(String key, String value) throws RemoteException;

  /** @see SWTBot#styledTextWithId(String, String, int) */
  public IRemoteBotStyledText styledTextWithId(String key, String value, int index)
      throws RemoteException;

  /** @see SWTBot#styledTextWithId(String) */
  public IRemoteBotStyledText styledTextWithId(String value) throws RemoteException;

  /** @see SWTBot#styledTextWithId(String, int) */
  public IRemoteBotStyledText styledTextWithId(String value, int index) throws RemoteException;

  /** @see SWTBot#styledTextInGroup(String) */
  public IRemoteBotStyledText styledTextInGroup(String inGroup) throws RemoteException;

  /** @see SWTBot#styledTextInGroup(String, int) */
  public IRemoteBotStyledText styledTextInGroup(String inGroup, int index) throws RemoteException;

  /** @see SWTBot#styledText() */
  public IRemoteBotStyledText styledText() throws RemoteException;

  /** @see SWTBot#styledText(int) */
  public IRemoteBotStyledText styledText(int index) throws RemoteException;

  /** @see SWTBot#styledTextWithLabelInGroup(String, String) */
  public IRemoteBotStyledText styledTextWithLabelInGroup(String label, String inGroup)
      throws RemoteException;

  /** @see SWTBot#styledTextWithLabelInGroup(String, String, int) */
  public IRemoteBotStyledText styledTextWithLabelInGroup(String label, String inGroup, int index)
      throws RemoteException;

  /** @see SWTBot#styledTextInGroup(String, String) */
  public IRemoteBotStyledText styledTextInGroup(String text, String inGroup) throws RemoteException;

  /** @see SWTBot#styledTextInGroup(String, String, int) */
  public IRemoteBotStyledText styledTextInGroup(String text, String inGroup, int index)
      throws RemoteException;

  /**
   * ********************************************
   *
   * <p>Widget comboBox
   *
   * <p>********************************************
   */
  /**
   * @param label the label on the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>label</code>.
   */
  public IRemoteBotCombo comboBoxWithLabel(String label) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>label</code>.
   */
  public IRemoteBotCombo comboBoxWithLabel(String label, int index) throws RemoteException;

  /**
   * @param text the text on the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>text</code>.
   */
  public IRemoteBotCombo comboBox(String text) throws RemoteException;

  /**
   * @param text the text on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>text</code>.
   */
  public IRemoteBotCombo comboBox(String text, int index) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @return a {@link IRemoteBotCombo} with the specified <code>key/value</code>.
   */
  public IRemoteBotCombo comboBoxWithId(String key, String value) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>key/value</code>.
   */
  public IRemoteBotCombo comboBoxWithId(String key, String value, int index) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @return a {@link IRemoteBotCombo} with the specified <code>value</code>.
   */
  public IRemoteBotCombo comboBoxWithId(String value) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>value</code>.
   */
  public IRemoteBotCombo comboBoxWithId(String value, int index) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>inGroup</code> .
   */
  public IRemoteBotCombo comboBoxInGroup(String inGroup) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>inGroup</code> .
   */
  public IRemoteBotCombo comboBoxInGroup(String inGroup, int index) throws RemoteException;

  /** @return a {@link IRemoteBotCombo} with the specified <code>none</code>. */
  public IRemoteBotCombo comboBox() throws RemoteException;

  /**
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>none</code>.
   */
  public IRemoteBotCombo comboBox(int index) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCombo comboBoxWithLabelInGroup(String label, String inGroup)
      throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCombo comboBoxWithLabelInGroup(String label, String inGroup, int index)
      throws RemoteException;

  /**
   * @param text the text on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>text</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCombo comboBoxInGroup(String text, String inGroup) throws RemoteException;

  /**
   * @param text the text on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCombo} with the specified <code>text</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCombo comboBoxInGroup(String text, String inGroup, int index)
      throws RemoteException;

  /**
   * @param text the text on the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>text</code>.
   */
  public IRemoteBotCCombo ccomboBox(String text) throws RemoteException;

  /**
   * @param text the text on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>text</code>.
   */
  public IRemoteBotCCombo ccomboBox(String text, int index) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>label</code>.
   */
  public IRemoteBotCCombo ccomboBoxWithLabel(String label) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>label</code>.
   */
  public IRemoteBotCCombo ccomboBoxWithLabel(String label, int index) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @return a {@link IRemoteBotCCombo} with the specified <code>key/value</code>.
   */
  public IRemoteBotCCombo ccomboBoxWithId(String key, String value) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>key/value</code>.
   */
  public IRemoteBotCCombo ccomboBoxWithId(String key, String value, int index)
      throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @return a {@link IRemoteBotCCombo} with the specified <code>value</code>.
   */
  public IRemoteBotCCombo ccomboBoxWithId(String value) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>value</code>.
   */
  public IRemoteBotCCombo ccomboBoxWithId(String value, int index) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>inGroup</code>.
   */
  public IRemoteBotCCombo ccomboBoxInGroup(String inGroup) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>inGroup</code>.
   */
  public IRemoteBotCCombo ccomboBoxInGroup(String inGroup, int index) throws RemoteException;

  /** @return a {@link IRemoteBotCCombo} with the specified <code>none</code>. */
  public IRemoteBotCCombo ccomboBox() throws RemoteException;

  /**
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>none</code>.
   */
  public IRemoteBotCCombo ccomboBox(int index) throws RemoteException;

  /**
   * @param text the text on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>text</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCCombo ccomboBoxInGroup(String text, String inGroup) throws RemoteException;

  /**
   * @param text the text on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>text</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCCombo ccomboBoxInGroup(String text, String inGroup, int index)
      throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup)
      throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCCombo} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCCombo ccomboBoxWithLabelInGroup(String label, String inGroup, int index)
      throws RemoteException;

  public IRemoteBotToolbarButton toolbarButton() throws RemoteException;

  public IRemoteBotToolbarButton toolbarButton(int index) throws RemoteException;

  public boolean existsToolbarButton() throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>mnemonicText</code>.
   */
  public IRemoteBotToolbarButton toolbarButton(String mnemonicText) throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>mnemonicText</code>.
   */
  public IRemoteBotToolbarButton toolbarButton(String mnemonicText, int index)
      throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>tooltip</code>.
   */
  public IRemoteBotToolbarButton toolbarButtonWithTooltip(String tooltip) throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>tooltip</code>.
   */
  public IRemoteBotToolbarButton toolbarButtonWithTooltip(String tooltip, int index)
      throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>key/value</code>.
   */
  public IRemoteBotToolbarButton toolbarButtonWithId(String key, String value)
      throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>key/value</code>.
   */
  public IRemoteBotToolbarButton toolbarButtonWithId(String key, String value, int index)
      throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>value</code>.
   */
  public IRemoteBotToolbarButton toolbarButtonWithId(String value) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>value</code>.
   */
  public IRemoteBotToolbarButton toolbarButtonWithId(String value, int index)
      throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>inGroup</code>.
   */
  public IRemoteBotToolbarButton toolbarButtonInGroup(String inGroup) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>inGroup</code>.
   */
  public IRemoteBotToolbarButton toolbarButtonInGroup(String inGroup, int index)
      throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>mnemonicText</code> with the
   *     specified <code>inGroup</code> .
   */
  public IRemoteBotToolbarButton toolbarButtonInGroup(String mnemonicText, String inGroup)
      throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>mnemonicText</code> with the
   *     specified <code>inGroup</code> .
   */
  public IRemoteBotToolbarButton toolbarButtonInGroup(
      String mnemonicText, String inGroup, int index) throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>tooltip</code> with the
   *     specified <code>inGroup</code>.
   */
  public IRemoteBotToolbarButton toolbarButtonWithTooltipInGroup(String tooltip, String inGroup)
      throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToolbarButton} with the specified <code>tooltip</code> with the
   *     specified <code>inGroup</code>.
   */
  public IRemoteBotToolbarButton toolbarButtonWithTooltipInGroup(
      String tooltip, String inGroup, int index) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>Widget text
   *
   * <p>********************************************
   */

  /**
   * @param label the label on the widget.
   * @return a {@link IRemoteBotText} with the specified <code>label</code>.
   */
  public IRemoteBotText textWithLabel(String label) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotText} with the specified <code>label</code>.
   */
  public IRemoteBotText textWithLabel(String label, int index) throws RemoteException;

  /**
   * @param text the text on the widget.
   * @return a {@link IRemoteBotText} with the specified <code>text</code>.
   */
  public IRemoteBotText text(String text) throws RemoteException;

  /**
   * @param text the text on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotText} with the specified <code>text</code>.
   */
  public IRemoteBotText text(String text, int index) throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @return a {@link IRemoteBotText} with the specified <code>tooltip</code>.
   */
  public IRemoteBotText textWithTooltip(String tooltip) throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotText} with the specified <code>tooltip</code>.
   */
  public IRemoteBotText textWithTooltip(String tooltip, int index) throws RemoteException;

  /**
   * @param message the message on the widget.
   * @return a {@link IRemoteBotText} with the specified <code>message</code>.
   */
  public IRemoteBotText textWithMessage(String message) throws RemoteException;

  /**
   * @param message the message on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotText} with the specified <code>message</code>.
   */
  public IRemoteBotText textWithMessage(String message, int index) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @return a {@link IRemoteBotText} with the specified <code>key/value</code>.
   */
  public IRemoteBotText textWithId(String key, String value) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotText} with the specified <code>key/value</code>.
   */
  public IRemoteBotText textWithId(String key, String value, int index) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @return a {@link IRemoteBotText} with the specified <code>value</code>.
   */
  public IRemoteBotText textWithId(String value) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @param index the index of the widget.
   * @return a {@link IRemoteBotText} with the specified <code>value</code>.
   */
  public IRemoteBotText textWithId(String value, int index) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotText} with the specified <code>inGroup</code>.
   */
  public IRemoteBotText textInGroup(String inGroup) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotText} with the specified <code>inGroup</code>.
   */
  public IRemoteBotText textInGroup(String inGroup, int index) throws RemoteException;

  /** @return a {@link IRemoteBotText} with the specified <code>none</code>. */
  public IRemoteBotText text() throws RemoteException;

  /**
   * @param index the index of the widget.
   * @return a {@link IRemoteBotText} with the specified <code>none</code>.
   */
  public IRemoteBotText text(int index) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotText} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotText textWithLabelInGroup(String label, String inGroup) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotText} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotText textWithLabelInGroup(String label, String inGroup, int index)
      throws RemoteException;

  /**
   * @param text the text on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotText} with the specified <code>text</code> with the specified <code>
   *     inGroup</code>.
   */
  public IRemoteBotText textInGroup(String text, String inGroup) throws RemoteException;

  /**
   * @param text the text on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotText} with the specified <code>text</code> with the specified <code>
   *     inGroup</code>.
   */
  public IRemoteBotText textInGroup(String text, String inGroup, int index) throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotText} with the specified <code>tooltip</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotText textWithTooltipInGroup(String tooltip, String inGroup)
      throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotText} with the specified <code>tooltip</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotText textWithTooltipInGroup(String tooltip, String inGroup, int index)
      throws RemoteException;

  /*
   * Widget table
   */
  public boolean existsTable() throws RemoteException;

  public boolean existsTableInGroup(String groupName) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @return a {@link IRemoteBotTable} with the specified <code>label</code>.
   */
  public IRemoteBotTable tableWithLabel(String label) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotTable} with the specified <code>label</code>.
   */
  public IRemoteBotTable tableWithLabel(String label, int index) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @return a {@link IRemoteBotTable} with the specified <code>key/value</code>.
   */
  public IRemoteBotTable tableWithId(String key, String value) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotTable} with the specified <code>key/value</code>.
   */
  public IRemoteBotTable tableWithId(String key, String value, int index) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @return a {@link IRemoteBotTable} with the specified <code>value</code>.
   */
  public IRemoteBotTable tableWithId(String value) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @param index the index of the widget.
   * @return a {@link IRemoteBotTable} with the specified <code>value</code>.
   */
  public IRemoteBotTable tableWithId(String value, int index) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotTable} with the specified <code>inGroup</code> .
   */
  public IRemoteBotTable tableInGroup(String inGroup) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotTable} with the specified <code>inGroup</code> .
   */
  public IRemoteBotTable tableInGroup(String inGroup, int index) throws RemoteException;

  /** @return a {@link IRemoteBotTable} with the specified <code>none</code>. */
  public IRemoteBotTable table() throws RemoteException;

  /**
   * @param index the index of the widget.
   * @return a {@link IRemoteBotTable} with the specified <code>none</code>.
   */
  public IRemoteBotTable table(int index) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotTable} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotTable tableWithLabelInGroup(String label, String inGroup) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>Widget menu
   *
   * <p>********************************************
   */

  /**
   * @param text the text on the menu.
   * @return a menu item that matches the specified text.
   */
  public IRemoteBotMenu menu(String text) throws RemoteException;

  /**
   * @param text the text on the menu.
   * @param index the index of the menu, in case there are multiple menus with the same text.
   * @return a menu item that matches the specified text.
   */
  public IRemoteBotMenu menu(String text, int index) throws RemoteException;

  /**
   * @param value the value of the id.
   * @return a wrapper around a @{link Menu} with the specified key/value pair for its id.
   */
  public IRemoteBotMenu menuWithId(String value) throws RemoteException;

  /**
   * @param value the value of the id.
   * @param index the index of the menu item, in case there are multiple shells with the same text.
   * @return a wrapper around a @{link Menu} with the specified key/value pair for its id.
   */
  public IRemoteBotMenu menuWithId(String value, int index) throws RemoteException;

  /**
   * @param key the key of the id.
   * @param value the value of the id.
   * @return a wrapper around a @{link Menu} with the specified key/value pair for its id.
   */
  public IRemoteBotMenu menuWithId(String key, String value) throws RemoteException;

  /**
   * @param key the key of the id.
   * @param value the value of the id.
   * @param index the index of the menu item, in case there are multiple shells with the same text.
   * @return a wrapper around a @{link Menu} with the specified key/value pair for its id.
   */
  public IRemoteBotMenu menuWithId(String key, String value, int index) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>Widget list
   *
   * <p>********************************************
   */
  /**
   * @param label the label on the widget.
   * @return a {@link IRemoteBotList} with the specified <code>label</code>.
   */
  public IRemoteBotList listWithLabel(String label) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotList} with the specified <code>label</code>.
   */
  public IRemoteBotList listWithLabel(String label, int index) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @return a {@link IRemoteBotList} with the specified <code>key/value</code>.
   */
  public IRemoteBotList listWithId(String key, String value) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotList} with the specified <code>key/value</code>.
   */
  public IRemoteBotList listWithId(String key, String value, int index) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @return a {@link IRemoteBotList} with the specified <code>value</code>.
   */
  public IRemoteBotList listWithId(String value) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @param index the index of the widget.
   * @return a {@link IRemoteBotList} with the specified <code>value</code>.
   */
  public IRemoteBotList listWithId(String value, int index) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotList} with the specified <code>inGroup</code>.
   */
  public IRemoteBotList listInGroup(String inGroup) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotList} with the specified <code>inGroup</code>.
   */
  public IRemoteBotList listInGroup(String inGroup, int index) throws RemoteException;

  /** @return a {@link IRemoteBotList} with the specified <code>none</code>. */
  public IRemoteBotList list() throws RemoteException;

  /**
   * @param index the index of the widget.
   * @return a {@link IRemoteBotList} with the specified <code>none</code>.
   */
  public IRemoteBotList list(int index) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotList} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotList listWithLabelInGroup(String label, String inGroup) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotList} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotList listWithLabelInGroup(String label, String inGroup, int index)
      throws RemoteException;

  /**
   * @return the title of the active shell.
   * @throws RemoteException
   */
  public String getTextOfActiveShell() throws RemoteException;

  public IRemoteBotShell activeShell() throws RemoteException;

  /**
   * @param label the label on the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>label</code>.
   */
  public IRemoteBotCheckBox checkBoxWithLabel(String label) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>label</code>.
   */
  public IRemoteBotCheckBox checkBoxWithLabel(String label, int index) throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>mnemonicText</code>.
   */
  public IRemoteBotCheckBox checkBox(String mnemonicText) throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>mnemonicText</code>.
   */
  public IRemoteBotCheckBox checkBox(String mnemonicText, int index) throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>tooltip</code>.
   */
  public IRemoteBotCheckBox checkBoxWithTooltip(String tooltip) throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>tooltip</code>.
   */
  public IRemoteBotCheckBox checkBoxWithTooltip(String tooltip, int index) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>key/value</code>.
   */
  public IRemoteBotCheckBox checkBoxWithId(String key, String value) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>key/value</code>.
   */
  public IRemoteBotCheckBox checkBoxWithId(String key, String value, int index)
      throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @return a {@link IRemoteBotCheckBox} with the specified <code>value</code>.
   */
  public IRemoteBotCheckBox checkBoxWithId(String value) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>value</code>.
   */
  public IRemoteBotCheckBox checkBoxWithId(String value, int index) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>inGroup</code>.
   */
  public IRemoteBotCheckBox checkBoxInGroup(String inGroup) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>inGroup</code>.
   */
  public IRemoteBotCheckBox checkBoxInGroup(String inGroup, int index) throws RemoteException;

  /** @return a {@link IRemoteBotCheckBox} with the specified <code>none</code> . */
  public IRemoteBotCheckBox checkBox() throws RemoteException;

  /**
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>none</code> .
   */
  public IRemoteBotCheckBox checkBox(int index) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCheckBox checkBoxWithLabelInGroup(String label, String inGroup)
      throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCheckBox checkBoxWithLabelInGroup(String label, String inGroup, int index)
      throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>mnemonicText</code> with the
   *     specified <code>inGroup</code> .
   */
  public IRemoteBotCheckBox checkBoxInGroup(String mnemonicText, String inGroup)
      throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>mnemonicText</code> with the
   *     specified <code>inGroup</code> .
   */
  public IRemoteBotCheckBox checkBoxInGroup(String mnemonicText, String inGroup, int index)
      throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>tooltip</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCheckBox checkBoxWithTooltipInGroup(String tooltip, String inGroup)
      throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotCheckBox} with the specified <code>tooltip</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotCheckBox checkBoxWithTooltipInGroup(String tooltip, String inGroup, int index)
      throws RemoteException;

  /**
   * @param label the label on the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>label</code>.
   */
  public IRemoteBotRadio radioWithLabel(String label) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>label</code>.
   */
  public IRemoteBotRadio radioWithLabel(String label, int index) throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>mnemonicText</code>.
   */
  public IRemoteBotRadio radio(String mnemonicText) throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>mnemonicText</code>.
   */
  public IRemoteBotRadio radio(String mnemonicText, int index) throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>tooltip</code> .
   */
  public IRemoteBotRadio radioWithTooltip(String tooltip) throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>tooltip</code> .
   */
  public IRemoteBotRadio radioWithTooltip(String tooltip, int index) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @return a {@link IRemoteBotRadio} with the specified <code>key/value</code>.
   */
  public IRemoteBotRadio radioWithId(String key, String value) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>key/value</code>.
   */
  public IRemoteBotRadio radioWithId(String key, String value, int index) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @return a {@link IRemoteBotRadio} with the specified <code>value</code>.
   */
  public IRemoteBotRadio radioWithId(String value) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @param index the index of the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>value</code>.
   */
  public IRemoteBotRadio radioWithId(String value, int index) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>inGroup</code> .
   */
  public IRemoteBotRadio radioInGroup(String inGroup) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>inGroup</code> .
   */
  public IRemoteBotRadio radioInGroup(String inGroup, int index) throws RemoteException;

  /** @return a {@link IRemoteBotRadio} with the specified <code>none</code>. */
  public IRemoteBotRadio radio() throws RemoteException;

  /**
   * @param index the index of the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>none</code>.
   */
  public IRemoteBotRadio radio(int index) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotRadio radioWithLabelInGroup(String label, String inGroup) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>label</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotRadio radioWithLabelInGroup(String label, String inGroup, int index)
      throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>mnemonicText</code> with the
   *     specified <code>inGroup</code> .
   */
  public IRemoteBotRadio radioInGroup(String mnemonicText, String inGroup) throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>mnemonicText</code> with the
   *     specified <code>inGroup</code> .
   */
  public IRemoteBotRadio radioInGroup(String mnemonicText, String inGroup, int index)
      throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>tooltip</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotRadio radioWithTooltipInGroup(String tooltip, String inGroup)
      throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotRadio} with the specified <code>tooltip</code> with the specified
   *     <code>inGroup</code>.
   */
  public IRemoteBotRadio radioWithTooltipInGroup(String tooltip, String inGroup, int index)
      throws RemoteException;

  /**
   * ********************************************
   *
   * <p>Widget toggleButton
   *
   * <p>********************************************
   */
  /**
   * @param label the label on the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>label</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithLabel(String label) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>label</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithLabel(String label, int index)
      throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>mnemonicText</code>.
   */
  public IRemoteBotToggleButton toggleButton(String mnemonicText) throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>mnemonicText</code>.
   */
  public IRemoteBotToggleButton toggleButton(String mnemonicText, int index) throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>tooltip</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithTooltip(String tooltip) throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>tooltip</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithTooltip(String tooltip, int index)
      throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>key/value</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithId(String key, String value) throws RemoteException;

  /**
   * @param key the key set on the widget.
   * @param value the value for the key.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>key/value</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithId(String key, String value, int index)
      throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @return a {@link IRemoteBotToggleButton} with the specified <code>value</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithId(String value) throws RemoteException;

  /**
   * @param value the value for the key {@link
   *     org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences#DEFAULT_KEY} .
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>value</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithId(String value, int index) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>inGroup</code>.
   */
  public IRemoteBotToggleButton toggleButtonInGroup(String inGroup) throws RemoteException;

  /**
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>inGroup</code>.
   */
  public IRemoteBotToggleButton toggleButtonInGroup(String inGroup, int index)
      throws RemoteException;

  /** @return a {@link IRemoteBotToggleButton} with the specified <code>none</code> . */
  public IRemoteBotToggleButton toggleButton() throws RemoteException;

  /**
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>none</code> .
   */
  public IRemoteBotToggleButton toggleButton(int index) throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>label</code> with the
   *     specified <code>inGroup</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithLabelInGroup(String label, String inGroup)
      throws RemoteException;

  /**
   * @param label the label on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>label</code> with the
   *     specified <code>inGroup</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithLabelInGroup(
      String label, String inGroup, int index) throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>mnemonicText</code> with the
   *     specified <code>inGroup</code> .
   */
  public IRemoteBotToggleButton toggleButtonInGroup(String mnemonicText, String inGroup)
      throws RemoteException;

  /**
   * @param mnemonicText the mnemonicText on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>mnemonicText</code> with the
   *     specified <code>inGroup</code> .
   */
  public IRemoteBotToggleButton toggleButtonInGroup(String mnemonicText, String inGroup, int index)
      throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param inGroup the inGroup on the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>tooltip</code> with the
   *     specified <code>inGroup</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithTooltipInGroup(String tooltip, String inGroup)
      throws RemoteException;

  /**
   * @param tooltip the tooltip on the widget.
   * @param inGroup the inGroup on the widget.
   * @param index the index of the widget.
   * @return a {@link IRemoteBotToggleButton} with the specified <code>tooltip</code> with the
   *     specified <code>inGroup</code>.
   */
  public IRemoteBotToggleButton toggleButtonWithTooltipInGroup(
      String tooltip, String inGroup, int index) throws RemoteException;

  public IRemoteBotCTabItem cTabItem() throws RemoteException;

  /*
   * Wait until
   */
  public void waitUntil(ICondition condition) throws RemoteException;

  public void waitLongUntil(ICondition condition) throws RemoteException;

  public void waitShortUntil(ICondition condition) throws RemoteException;

  public void logMessage(String message) throws RemoteException;

  /**
   * @see SWTBot#captureScreenshot(String)
   * @throws RemoteException
   */
  public void captureScreenshot(String fileName) throws RemoteException;
}
