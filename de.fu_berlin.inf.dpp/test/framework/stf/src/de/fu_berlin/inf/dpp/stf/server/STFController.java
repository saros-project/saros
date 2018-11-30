package de.fu_berlin.inf.dpp.stf.server;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.context.IContainerContext;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.impl.ControlBotImpl;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.impl.AccountManipulatorImpl;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.impl.NetworkManipulatorImpl;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.EclipseHTMLWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.impl.HTMLBotImpl;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLCheckbox;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLInputField;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLMultiSelect;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLProgressBar;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLRadioGroup;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLSelect;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLTextElement;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLTree;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLView;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotCCombo;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotCLabel;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotCTabItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotChatLine;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotCheckBox;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotList;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotPerspective;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotRadio;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotStyledText;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotTableItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotText;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToggleButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToolbarDropDownButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToolbarPushButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToolbarRadioButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotToolbarToggleButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotViewMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.impl.ContextMenusInPEView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.NewC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.RefactorC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.RunAsContextMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.ShareWithC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl.ContextMenusInContactListArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl.ContextMenusInSessionArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.impl.WorkTogetherOnContextMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.impl.MenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl.SarosMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.impl.WindowMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.impl.SarosPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.ConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.PackageExplorerView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.impl.Views;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl.Chatroom;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl.SarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.whiteboard.impl.SarosWhiteboardView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.whiteboard.impl.WhiteboardFigure;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal.impl.InternalImpl;
import de.fu_berlin.inf.dpp.stf.shared.Constants;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * The STF Controller is responsible to register all exported objects and also changes some Saros
 * configurations to ensure proper execution of the STF test cases.
 */
public class STFController {

  private static final transient Logger log = Logger.getLogger(STFController.class);

  private static Registry registry;

  public static void start(int port, IContainerContext context) throws RemoteException {

    Thread.setDefaultUncaughtExceptionHandler(
        new UncaughtExceptionHandler() {

          @Override
          public void uncaughtException(Thread thread, Throwable error) {
            log.error("uncaught exception in thread: " + thread, error);
          }
        });

    /*
     * HACK this is not the way OSGi works but it currently fulfill its
     * purpose
     */
    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    try {
      // change the context class loader so Log4J will find the appenders
      Thread.currentThread().setContextClassLoader(STFController.class.getClassLoader());

      PropertyConfigurator.configure(
          STFController.class.getClassLoader().getResource("saros_stf.log4j.properties"));
    } finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    performConfigurationCheck();

    List<String> propertyKeys =
        Arrays.asList(System.getProperties().keySet().toArray(new String[0]));

    Collections.sort(propertyKeys);

    for (String key : propertyKeys)
      log.info("java property: " + key + " = " + System.getProperty(key));

    StfRemoteObject.setContext(context);

    IPreferenceStore preferenceStore = context.getComponent(IPreferenceStore.class);

    if (preferenceStore != null) {
      preferenceStore.setDefault(
          EclipsePreferenceConstants.AUTO_STOP_EMPTY_SESSION, MessageDialogWithToggle.ALWAYS);
      preferenceStore.setToDefault(EclipsePreferenceConstants.AUTO_STOP_EMPTY_SESSION);
    }

    // Revert March 2013 color hack
    if (preferenceStore != null)
      preferenceStore.setToDefault(PreferenceConstants.FAVORITE_SESSION_COLOR_ID);

    /*
     * as the default account file is shared across all Eclipse instances
     * create / use a file located in the current workspace
     */

    XMPPAccountStore store = context.getComponent(XMPPAccountStore.class);

    if (store != null) {

      File file = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();

      file = new File(file, ".metadata");
      file = new File(file, ".saros_stf_accounts");

      store.setAccountFile(file, null);
    }

    try {
      registry = LocateRegistry.createRegistry(port);
    } catch (RemoteException e) {
      registry = LocateRegistry.getRegistry(port);
    }

    // remove this conditional when the HTML GUI replaces the old one
    if (Saros.useHtmlGui()) {
      HTMLSTFRemoteObject.setContext(context);

      /*
       * export HTML bots
       */
      exportObject(EclipseHTMLWorkbenchBot.getInstance(), "htmlViewBot");
      exportObject(HTMLBotImpl.getInstance(), "htmlBot");

      /*
       * export HTML widgets
       */
      exportObject(RemoteHTMLView.getInstance(), "htmlView");
      exportObject(RemoteHTMLButton.getInstance(), "htmlButton");
      exportObject(RemoteHTMLInputField.getInstance(), "htmlInputField");
      exportObject(RemoteHTMLCheckbox.getInstance(), "htmlCheckbox");
      exportObject(RemoteHTMLRadioGroup.getInstance(), "htmlRadioGroup");
      exportObject(RemoteHTMLSelect.getInstance(), "htmlSelect");
      exportObject(RemoteHTMLMultiSelect.getInstance(), "htmlMultiSelect");
      exportObject(RemoteHTMLProgressBar.getInstance(), "htmlProgressBar");
      exportObject(RemoteHTMLTextElement.getInstance(), "htmlTextElement");
      exportObject(RemoteHTMLTree.getInstance(), "htmlTree");
    }

    /*
     * bots' family
     */
    exportObject(RemoteWorkbenchBot.getInstance(), "workbenchBot");
    exportObject(SuperBot.getInstance(), "superBot");
    exportObject(ControlBotImpl.getInstance(), "controlBot");

    /*
     * export remoteWidgets
     */
    exportObject(RemoteBotButton.getInstance(), "button");
    exportObject(RemoteBotCCombo.getInstance(), "ccombo");
    exportObject(RemoteBotCLabel.getInstance(), "clabel");
    exportObject(RemoteBotChatLine.getInstance(), "chatLine");
    exportObject(RemoteBotCheckBox.getInstance(), "checkBox");
    exportObject(RemoteBotCombo.getInstance(), "combo");
    exportObject(RemoteBotCTabItem.getInstance(), "cTabItem");
    exportObject(RemoteBotEditor.getInstance(), "editor");
    exportObject(RemoteBotLabel.getInstance(), "label");
    exportObject(RemoteBotList.getInstance(), "list");
    exportObject(RemoteBotMenu.getInstance(), "menu");
    exportObject(RemoteBotPerspective.getInstance(), "perspective");
    exportObject(RemoteBotRadio.getInstance(), "radio");
    exportObject(RemoteBotShell.getInstance(), "shell");
    exportObject(RemoteBotStyledText.getInstance(), "styledText");
    exportObject(RemoteBotTable.getInstance(), "table");
    exportObject(RemoteBotTableItem.getInstance(), "tableItem");
    exportObject(RemoteBotText.getInstance(), "text");
    exportObject(RemoteBotToggleButton.getInstance(), "toggleButton");
    exportObject(RemoteBotToolbarButton.getInstance(), "toolbarButton");
    exportObject(RemoteBotToolbarDropDownButton.getInstance(), "toolbarDropDownButton");
    exportObject(RemoteBotToolbarPushButton.getInstance(), "toolbarPushButon");
    exportObject(RemoteBotToolbarRadioButton.getInstance(), "toolbarRadioButton");
    exportObject(RemoteBotToolbarToggleButton.getInstance(), "toolbarToggleButton");
    exportObject(RemoteBotTree.getInstance(), "tree");
    exportObject(RemoteBotTreeItem.getInstance(), "treeItem");
    exportObject(RemoteBotView.getInstance(), "view");
    exportObject(RemoteBotViewMenu.getInstance(), "viewMenu");

    /*
     * remote eclipse components
     */
    exportObject(PackageExplorerView.getInstance(), "packageExplorerView");
    exportObject(ProgressView.getInstance(), "progressView");
    exportObject(SarosView.getInstance(), "rosterView");
    exportObject(ConsoleView.getInstance(), "consoleView");

    /*
     * whiteboard specific components
     */
    exportObject(SarosWhiteboardView.getInstance(), "sarosWhiteboardView");
    exportObject(WhiteboardFigure.getInstance(), "whiteboardFigure");

    /*
     * SuperBot components
     */

    exportObject(NewC.getInstance(), "fileM");
    exportObject(RefactorC.getInstance(), "refactorM");
    exportObject(WindowMenu.getInstance(), "windowM");
    exportObject(SarosMenu.getInstance(), "sarosM");
    exportObject(RunAsContextMenu.getInstance(), "runAsC");
    exportObject(ShareWithC.getInstance(), "shareWithC");
    exportObject(ContextMenusInPEView.getInstance(), "contextMenu");
    exportObject(ContextMenusInContactListArea.getInstance(), "contactsContextMenu");

    exportObject(ContextMenusInSessionArea.getInstance(), "sessionContextMenu");

    exportObject(WorkTogetherOnContextMenu.getInstance(), "workTogetherOnC");
    exportObject(Chatroom.getInstance(), "chatroom");
    exportObject(SarosPreferences.getInstance(), "sarosPreferences");
    exportObject(Views.getInstance(), "views");
    exportObject(MenuBar.getInstance(), "menuBar");
    exportObject(InternalImpl.getInstance(), "internal");

    /*
     * ControlBot components
     */

    exportObject(NetworkManipulatorImpl.getInstance(), "networkManipulator");
    exportObject(AccountManipulatorImpl.getInstance(), "accountManipulator");

    try {
      for (String s : registry.list()) log.debug("registered Object: " + s);
    } catch (AccessException e) {
      log.error("failed on access", e);
    } catch (RemoteException e) {
      log.error("failed", e);
    }
  }

  /** Add a shutdown hook to unbind exported Object from registry. */
  private static void addShutdownHook(final String name) {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                try {
                  if (registry != null && name != null) registry.unbind(name);
                } catch (RemoteException e) {
                  log.warn("Failed to unbind: " + name, e);
                } catch (NotBoundException e) {
                  log.warn("Failed to unbind: " + name, e);
                }
              }
            });
  }

  /** Export object by given name on our local RMI Registry. */
  private static Remote exportObject(Remote exportedObject, String exportName) {
    try {
      Remote remoteObject = UnicastRemoteObject.exportObject(exportedObject, 0);
      addShutdownHook(exportName);
      registry.bind(exportName, remoteObject);
      return remoteObject;
    } catch (RemoteException e) {
      log.error("could not export the object " + exportName, e);
    } catch (AlreadyBoundException e) {
      log.error("could not bind the object " + exportName + ", because it is bound already.", e);
    }
    return null;
  }

  private static void performConfigurationCheck() {

    log.info("checking initialization of constants class: " + Constants.class);
    Class<Constants> constantClass = Constants.class;

    Field[] fields = constantClass.getDeclaredFields();

    for (Field field : fields) {

      int modifiers = field.getModifiers();

      if (!(Modifier.isPublic(modifiers)
          && Modifier.isStatic(modifiers)
          && Modifier.isFinal(modifiers)))
        throw new IllegalArgumentException(field + " is not public, static and final");

      if (!String.class.isAssignableFrom(field.getType())) continue;

      Object object;
      try {
        object = field.get(null);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      if (object == null) throw new IllegalStateException(field + " has not been initialized");

      log.trace("field '" + field.getName() + "' initialized with '" + object + "'");
    }
  }
}
